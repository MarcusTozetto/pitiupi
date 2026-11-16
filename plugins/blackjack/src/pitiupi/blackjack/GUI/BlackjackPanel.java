package pitiupi.blackjack.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.net.InetAddress;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;
import pitiupi.application.presence.UserPeerView;
import pitiupi.blackjack.GUI.TableCanvas;
import pitiupi.blackjack.control.BlackjackController;
import pitiupi.blackjack.control.BlackjackView;
import pitiupi.blackjack.model.TableSnapshot;
import pitiupi.presentation.shell.PeerIpv4;

public final class BlackjackPanel
extends JPanel
implements BlackjackView {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final BlackjackController controller;
    private final TableCanvas canvas = new TableCanvas();
    private final JTextArea log = new JTextArea(6, 40);
    private final JLabel roleLabel = new JLabel();
    private final JButton openButton = new JButton("Abrir mesa (ser a banca)");
    private final JButton inviteAllButton = new JButton("Convidar a sala (multicast)");
    private final JButton newRoundButton = new JButton("Nova rodada");
    private final JButton closeButton = new JButton("Fechar mesa");
    private final JLabel ipLabel = new JLabel("IP da banca:");
    private final JTextField ipField = new JTextField(12);
    private final JButton joinButton = new JButton("Entrar na mesa");
    private final JButton leaveButton = new JButton("Sair da mesa");
    private final JButton hitButton = new JButton("Pedir carta");
    private final JButton standButton = new JButton("Parar");
    private final JPanel inviteBanner = new JPanel(new FlowLayout(0, 8, 4));
    private final JLabel inviteLabel = new JLabel();
    private TableSnapshot lastSnapshot;
    private String myId;

    public BlackjackPanel(BlackjackController blackjackController) {
        super(new BorderLayout());
        this.controller = blackjackController;
        blackjackController.setView(this);
        this.buildUi();
        this.onRoleChanged(BlackjackController.Role.IDLE, "");
    }

    private void buildUi() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, 1));
        this.inviteBanner.setBackground(new Color(255, 243, 205));
        this.inviteBanner.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 190, 100)));
        JButton jButton = new JButton("Aceitar");
        JButton jButton2 = new JButton("Recusar");
        jButton.addActionListener(actionEvent -> this.controller.acceptInvite());
        jButton2.addActionListener(actionEvent -> this.controller.declineInvite());
        this.inviteBanner.add(this.inviteLabel);
        this.inviteBanner.add(jButton);
        this.inviteBanner.add(jButton2);
        this.inviteBanner.setVisible(false);
        jPanel.add(this.inviteBanner);
        JPanel jPanel2 = new JPanel(new FlowLayout(0, 6, 4));
        jPanel2.add(this.openButton);
        jPanel2.add(this.inviteAllButton);
        jPanel2.add(this.newRoundButton);
        jPanel2.add(this.closeButton);
        jPanel2.add(this.ipLabel);
        jPanel2.add(this.ipField);
        jPanel2.add(this.joinButton);
        jPanel2.add(this.leaveButton);
        jPanel.add(jPanel2);
        JPanel jPanel3 = new JPanel(new FlowLayout(0, 6, 2));
        this.hitButton.setFont(this.hitButton.getFont().deriveFont(1));
        this.standButton.setFont(this.standButton.getFont().deriveFont(1));
        jPanel3.add(this.hitButton);
        jPanel3.add(this.standButton);
        jPanel3.add(this.roleLabel);
        jPanel.add(jPanel3);
        this.openButton.addActionListener(actionEvent -> this.controller.openTable());
        this.inviteAllButton.addActionListener(actionEvent -> this.controller.inviteEveryone());
        this.newRoundButton.addActionListener(actionEvent -> this.controller.startRound());
        this.closeButton.addActionListener(actionEvent -> this.controller.closeTable());
        this.joinButton.addActionListener(actionEvent -> this.controller.requestJoin(this.ipField.getText()));
        this.ipField.addActionListener(actionEvent -> this.controller.requestJoin(this.ipField.getText()));
        this.leaveButton.addActionListener(actionEvent -> this.controller.leaveTable());
        this.hitButton.addActionListener(actionEvent -> this.controller.hit());
        this.standButton.addActionListener(actionEvent -> this.controller.stand());
        this.log.setEditable(false);
        this.log.setLineWrap(true);
        this.log.setWrapStyleWord(true);
        this.log.setFont(new Font("Monospaced", 0, 12));
        ((DefaultCaret)this.log.getCaret()).setUpdatePolicy(2);
        JScrollPane jScrollPane = new JScrollPane(this.log);
        jScrollPane.setBorder(BorderFactory.createTitledBorder("Histórico"));
        JSplitPane jSplitPane = new JSplitPane(0, this.canvas, jScrollPane);
        jSplitPane.setResizeWeight(0.78);
        jSplitPane.setBorder(null);
        this.add((Component)jPanel, "North");
        this.add((Component)jSplitPane, "Center");
    }

    public void joinPeer(UserPeerView userPeerView) {
        userPeerView.getInetAddress().ifPresent(inetAddress -> {
            String string = PeerIpv4.fromInetAddress((InetAddress)inetAddress);
            this.ipField.setText(string);
            this.controller.requestJoin(string);
        });
    }

    public void invitePeer(UserPeerView userPeerView) {
        userPeerView.getInetAddress().ifPresent(inetAddress -> this.controller.invite(PeerIpv4.fromInetAddress((InetAddress)inetAddress), userPeerView.getDisplayName()));
    }

    @Override
    public void showSnapshot(TableSnapshot tableSnapshot, String string) {
        this.lastSnapshot = tableSnapshot;
        this.myId = string;
        this.canvas.show(tableSnapshot, string);
        this.refreshButtons();
    }

    @Override
    public void clearTable() {
        this.lastSnapshot = null;
        this.canvas.clear();
        this.refreshButtons();
    }

    @Override
    public void appendLog(String string) {
        this.log.append("[" + LocalTime.now().format(CLOCK) + "] " + string + "\n");
    }

    @Override
    public void onRoleChanged(BlackjackController.Role role, String string) {
        this.roleLabel.setText((String)(string == null ? "" : "   " + string));
        this.refreshButtons();
    }

    @Override
    public void showInvite(String string, String string2) {
        this.inviteLabel.setText("♠ " + string + " (" + string2 + ") convidou você para uma mesa de 21.");
        this.inviteBanner.setVisible(true);
        this.revalidate();
    }

    @Override
    public void hideInvite() {
        this.inviteBanner.setVisible(false);
        this.revalidate();
    }

    private void refreshButtons() {
        BlackjackController.Role role = this.controller.role();
        boolean bl = role == BlackjackController.Role.IDLE;
        boolean bl2 = role == BlackjackController.Role.HOST;
        boolean bl3 = role == BlackjackController.Role.GUEST;
        this.openButton.setVisible(bl);
        this.inviteAllButton.setVisible(bl || bl2);
        this.ipLabel.setVisible(bl);
        this.ipField.setVisible(bl);
        this.joinButton.setVisible(bl);
        this.newRoundButton.setVisible(bl2);
        this.closeButton.setVisible(bl2);
        this.leaveButton.setVisible(bl3);
        TableSnapshot.Phase phase = this.lastSnapshot == null ? null : this.lastSnapshot.phase();
        this.newRoundButton.setEnabled(bl2 && phase != TableSnapshot.Phase.PLAYING);
        boolean bl4 = this.lastSnapshot != null && this.lastSnapshot.isTurnOf(this.myId);
        this.hitButton.setVisible(!bl);
        this.standButton.setVisible(!bl);
        this.hitButton.setEnabled(bl4);
        this.standButton.setEnabled(bl4);
        if (bl) {
            this.roleLabel.setText("");
        }
        this.revalidate();
        this.repaint();
    }
}

