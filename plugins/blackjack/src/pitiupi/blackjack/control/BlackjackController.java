package pitiupi.blackjack.control;

import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import pitiupi.application.messaging.MessageDispatcher;
import pitiupi.application.messaging.SendTarget;
import pitiupi.application.messaging.TransportMode;
import pitiupi.blackjack.control.BlackjackView;
import pitiupi.blackjack.model.BlackjackTable;
import pitiupi.blackjack.model.TableSnapshot;
import pitiupi.blackjack.net.JoinRejectedMessage;
import pitiupi.blackjack.net.JoinRequestMessage;
import pitiupi.blackjack.net.PlayerActionMessage;
import pitiupi.blackjack.net.TableClosedMessage;
import pitiupi.blackjack.net.TableInviteMessage;
import pitiupi.blackjack.net.TableStateMessage;
import pitiupi.control.ConnectionManager;
import pitiupi.models.Messages.Message;
import pitiupi.presentation.shell.LocalNetworkInfo;
import pitiupi.presentation.shell.PeerIpv4;

public final class BlackjackController {
    public static final int TURN_SECONDS = 30;
    private final ConnectionManager connectionManager;
    private final ExecutorService sender = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "blackjack-sender");
        thread.setDaemon(true);
        return thread;
    });
    private final SecureRandom random = new SecureRandom();
    private BlackjackView view;
    private Role role = Role.IDLE;
    private BlackjackTable table;
    private Timer turnTimer;
    private String timedTurnId;
    private long timedSeq = -1L;
    private String hostIp;
    private String hostName;
    private String tableId;
    private long lastSeqSeen = -1L;
    private String pendingInviteHostIp;

    public BlackjackController(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void setView(BlackjackView blackjackView) {
        this.view = blackjackView;
    }

    public Role role() {
        return this.role;
    }

    public String myId() {
        return LocalNetworkInfo.bestLocalIpv4();
    }

    private String myName() {
        return this.connectionManager.getUserName();
    }

    public void openTable() {
        if (this.role == Role.HOST) {
            return;
        }
        if (this.role == Role.GUEST) {
            this.leaveTable();
        }
        String string = UUID.randomUUID().toString();
        this.table = new BlackjackTable(string, this.myId(), this.myName(), this.random);
        this.table.setTurnSeconds(30);
        this.role = Role.HOST;
        this.view.onRoleChanged(this.role, "Você é a banca. Convide pessoas pela lista lateral ou pelo botão.");
        this.publish();
    }

    public void closeTable() {
        if (this.role != Role.HOST) {
            return;
        }
        TableClosedMessage tableClosedMessage = new TableClosedMessage(this.table.snapshot().tableId(), this.myName());
        for (String string : this.table.guestIds()) {
            this.sendTcp(tableClosedMessage, string);
        }
        this.stopTurnTimer();
        this.table = null;
        this.role = Role.IDLE;
        this.view.clearTable();
        this.view.appendLog("Você fechou a mesa.");
        this.view.onRoleChanged(this.role, "");
    }

    public void startRound() {
        if (this.role != Role.HOST) {
            return;
        }
        if (!this.table.startRound()) {
            this.view.appendLog("Não foi possível começar: " + this.table.snapshot().lastEvent());
        }
        this.publish();
    }

    public void invite(String string, String string2) {
        this.openTable();
        String string3 = PeerIpv4.normalize((String)string);
        this.sendTcp(new TableInviteMessage(this.table.snapshot().tableId(), this.myName(), this.myId()), string3);
        this.view.appendLog("Convite enviado para " + BlackjackController.label(string2, string3) + " (TCP).");
    }

    public void inviteEveryone() {
        this.openTable();
        TableInviteMessage tableInviteMessage = new TableInviteMessage(this.table.snapshot().tableId(), this.myName(), this.myId());
        this.sender.execute(() -> {
            int n = MessageDispatcher.send((ConnectionManager)this.connectionManager, (Message)tableInviteMessage, (SendTarget)SendTarget.broadcastAll(), (TransportMode)TransportMode.MULTICAST_UDP);
            SwingUtilities.invokeLater(() -> this.view.appendLog(n > 0 ? "Convite enviado para a sala inteira (UDP multicast)." : "Falha ao enviar o convite por multicast."));
        });
    }

    public void onJoinRequest(JoinRequestMessage joinRequestMessage) {
        String string = PeerIpv4.normalize((String)joinRequestMessage.getPlayerIp());
        if (this.role != Role.HOST) {
            this.sendTcp(new JoinRejectedMessage(this.myName(), "não há mesa aberta"), string);
            return;
        }
        BlackjackTable.JoinResult joinResult = this.table.join(string, joinRequestMessage.getPlayerName());
        if (joinResult == BlackjackTable.JoinResult.FULL) {
            this.sendTcp(new JoinRejectedMessage(this.myName(), "mesa cheia (máximo 5)"), string);
            this.view.appendLog(joinRequestMessage.getPlayerName() + " tentou entrar, mas a mesa está cheia.");
            return;
        }
        this.publish();
    }

    public void onPlayerAction(PlayerActionMessage playerActionMessage) {
        if (this.role != Role.HOST || !Objects.equals(playerActionMessage.getTableId(), this.table.snapshot().tableId())) {
            return;
        }
        String string = PeerIpv4.normalize((String)playerActionMessage.getPlayerIp());
        boolean bl = playerActionMessage.getAction() == PlayerActionMessage.Action.LEAVE ? this.table.leave(string) : this.applyMove(string, playerActionMessage.getAction());
        if (bl) {
            this.publish();
        }
    }

    private boolean applyMove(String string, PlayerActionMessage.Action action) {
        return switch (action) {
            case HIT -> this.table.hit(string);
            case STAND -> this.table.stand(string);
            default -> false;
        };
    }

    private void publish() {
        TableSnapshot tableSnapshot = this.table.snapshot();
        this.showLocally(tableSnapshot);
        TableStateMessage tableStateMessage = new TableStateMessage(tableSnapshot);
        for (String string : this.table.guestIds()) {
            this.sendTcp(tableStateMessage, string);
        }
        this.restartTurnTimerIfNeeded(tableSnapshot);
    }

    private void restartTurnTimerIfNeeded(TableSnapshot tableSnapshot) {
        String string = tableSnapshot.currentTurnId();
        if (string == null) {
            this.stopTurnTimer();
            return;
        }
        if (string.equals(this.timedTurnId) && tableSnapshot.eventSeq() == this.timedSeq) {
            return;
        }
        this.stopTurnTimer();
        this.timedTurnId = string;
        this.timedSeq = tableSnapshot.eventSeq();
        this.turnTimer = new Timer(30000, actionEvent -> {
            if (this.role == Role.HOST && this.table.forceStand(string, "demorou demais")) {
                this.publish();
            }
        });
        this.turnTimer.setRepeats(false);
        this.turnTimer.start();
    }

    private void stopTurnTimer() {
        if (this.turnTimer != null) {
            this.turnTimer.stop();
            this.turnTimer = null;
        }
        this.timedTurnId = null;
        this.timedSeq = -1L;
    }

    public void requestJoin(String string) {
        String string2 = PeerIpv4.normalize((String)string);
        if (string2.isBlank()) {
            this.view.appendLog("Informe o IP da banca.");
            return;
        }
        if (this.role == Role.HOST) {
            this.view.appendLog("Feche a sua mesa antes de entrar na de outra pessoa.");
            return;
        }
        if (this.role == Role.GUEST && !string2.equals(this.hostIp)) {
            this.leaveTable();
        }
        this.view.hideInvite();
        this.pendingInviteHostIp = null;
        this.hostIp = string2;
        this.view.appendLog("Pedindo para entrar na mesa de " + string2 + " (TCP)…");
        this.sendTcp(new JoinRequestMessage(this.myName(), this.myId()), string2);
    }

    public void leaveTable() {
        if (this.role != Role.GUEST && this.hostIp == null) {
            return;
        }
        if (this.hostIp != null && this.tableId != null) {
            this.sendTcp(new PlayerActionMessage(this.tableId, this.myId(), PlayerActionMessage.Action.LEAVE), this.hostIp);
        }
        this.resetGuest();
        this.view.clearTable();
        this.view.appendLog("Você saiu da mesa.");
        this.view.onRoleChanged(this.role, "");
    }

    public void onInvite(TableInviteMessage tableInviteMessage) {
        if (this.role != Role.IDLE) {
            return;
        }
        String string = PeerIpv4.normalize((String)tableInviteMessage.getHostIp());
        if (string.equals(this.myId())) {
            return;
        }
        this.pendingInviteHostIp = string;
        this.view.showInvite(tableInviteMessage.getHostName(), string);
        this.view.appendLog(tableInviteMessage.getHostName() + " (" + string + ") convidou você para uma mesa de 21.");
    }

    public void acceptInvite() {
        if (this.pendingInviteHostIp != null) {
            this.requestJoin(this.pendingInviteHostIp);
        }
    }

    public void declineInvite() {
        this.pendingInviteHostIp = null;
        this.view.hideInvite();
    }

    public void onState(TableStateMessage tableStateMessage) {
        TableSnapshot tableSnapshot = tableStateMessage.getSnapshot();
        if (tableSnapshot == null || this.role == Role.HOST) {
            return;
        }
        if (this.hostIp == null) {
            return;
        }
        if (this.role == Role.GUEST && !tableSnapshot.tableId().equals(this.tableId)) {
            return;
        }
        if (tableSnapshot.findSeat(this.myId()) == null) {
            return;
        }
        if (this.role != Role.GUEST) {
            this.role = Role.GUEST;
            this.tableId = tableSnapshot.tableId();
            this.hostName = tableSnapshot.hostName();
            this.lastSeqSeen = -1L;
            this.view.onRoleChanged(this.role, "Você está na mesa de " + this.hostName + ".");
        }
        if (tableSnapshot.eventSeq() <= this.lastSeqSeen) {
            return;
        }
        this.showLocally(tableSnapshot);
    }

    public void onJoinRejected(JoinRejectedMessage joinRejectedMessage) {
        if (this.role == Role.GUEST) {
            return;
        }
        this.view.appendLog(joinRejectedMessage.getHostName() + " recusou: " + joinRejectedMessage.getReason() + ".");
        this.resetGuest();
    }

    public void onTableClosed(TableClosedMessage tableClosedMessage) {
        if (this.role != Role.GUEST || !Objects.equals(tableClosedMessage.getTableId(), this.tableId)) {
            return;
        }
        this.resetGuest();
        this.view.clearTable();
        this.view.appendLog(tableClosedMessage.getHostName() + " fechou a mesa.");
        this.view.onRoleChanged(this.role, "");
    }

    private void resetGuest() {
        this.role = Role.IDLE;
        this.hostIp = null;
        this.hostName = null;
        this.tableId = null;
        this.lastSeqSeen = -1L;
    }

    public void hit() {
        this.move(PlayerActionMessage.Action.HIT);
    }

    public void stand() {
        this.move(PlayerActionMessage.Action.STAND);
    }

    private void move(PlayerActionMessage.Action action) {
        if (this.role == Role.HOST) {
            if (this.applyMove(this.myId(), action)) {
                this.publish();
            }
        } else if (this.role == Role.GUEST) {
            this.sendTcp(new PlayerActionMessage(this.tableId, this.myId(), action), this.hostIp);
        }
    }

    public void shutdown() {
        if (this.role == Role.HOST) {
            this.closeTable();
        } else if (this.role == Role.GUEST) {
            this.leaveTable();
        }
    }

    private void showLocally(TableSnapshot tableSnapshot) {
        if (tableSnapshot.eventSeq() > this.lastSeqSeen && !tableSnapshot.lastEvent().isBlank()) {
            this.view.appendLog(tableSnapshot.lastEvent());
        }
        this.lastSeqSeen = tableSnapshot.eventSeq();
        this.view.showSnapshot(tableSnapshot, this.myId());
    }

    private void sendTcp(Message message, String string) {
        this.sender.execute(() -> {
            boolean bl;
            try {
                InetAddress inetAddress = InetAddress.getByName(string);
                bl = MessageDispatcher.send((ConnectionManager)this.connectionManager, (Message)message, (SendTarget)SendTarget.singlePeer((InetAddress)inetAddress), (TransportMode)TransportMode.TCP) > 0;
            }
            catch (Exception exception) {
                bl = false;
            }
            if (!bl) {
                SwingUtilities.invokeLater(() -> this.view.appendLog("Falha ao enviar para " + string + " (TCP)."));
            }
        });
    }

    private static String label(String string, String string2) {
        return string == null || string.isBlank() ? string2 : string + " (" + string2 + ")";
    }

    public static enum Role {
        IDLE,
        HOST,
        GUEST;

    }
}

