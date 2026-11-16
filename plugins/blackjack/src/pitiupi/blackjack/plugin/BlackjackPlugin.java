package pitiupi.blackjack.plugin;

import java.util.List;
import javax.swing.SwingUtilities;
import pitiupi.api.plugin.PeerActionsRegistrar;
import pitiupi.api.plugin.PluginWorkspace;
import pitiupi.application.peeractions.PeerAction;
import pitiupi.application.peeractions.PeerActionTarget;
import pitiupi.application.peeractions.PeerTarget;
import pitiupi.blackjack.GUI.BlackjackPanel;
import pitiupi.blackjack.control.BlackjackController;
import pitiupi.blackjack.net.JoinRejectedMessage;
import pitiupi.blackjack.net.JoinRequestMessage;
import pitiupi.blackjack.net.PlayerActionMessage;
import pitiupi.blackjack.net.TableClosedMessage;
import pitiupi.blackjack.net.TableInviteMessage;
import pitiupi.blackjack.net.TableStateMessage;
import pitiupi.control.ConnectionManager;
import pitiupi.models.Messages.Message;
import pitiupi.models.Plugin;

public class BlackjackPlugin
extends Plugin {
    private PluginWorkspace workspace;
    private BlackjackController controller;
    private BlackjackPanel panel;

    public void createPlugin(ConnectionManager connectionManager, PluginWorkspace pluginWorkspace) {
        this.workspace = pluginWorkspace;
        this.controller = new BlackjackController(connectionManager);
        this.panel = new BlackjackPanel(this.controller);
        this.bindShellWorkspaceContent(this.panel);
        this.installPeerActions(pluginWorkspace.peerActions());
    }

    public void installPeerActions(PeerActionsRegistrar peerActionsRegistrar) {
        peerActionsRegistrar.register(PeerAction.of((String)"blackjack.room", (String)"Abrir mesa de 21 e convidar todos", (PeerActionTarget)PeerActionTarget.ALL_PEERS, peerTarget -> {
            this.openTab();
            this.controller.inviteEveryone();
        }));
        peerActionsRegistrar.register(PeerAction.of((String)"blackjack.invite", (String)"Convidar para minha mesa de 21", (PeerActionTarget)PeerActionTarget.SINGLE_PEER, peerTarget -> {
            this.openTab();
            if (peerTarget instanceof PeerTarget.Single) {
                PeerTarget.Single single = (PeerTarget.Single)peerTarget;
                this.panel.invitePeer(single.peer());
            }
        }));
        peerActionsRegistrar.register(PeerAction.of((String)"blackjack.join", (String)"Entrar na mesa de 21 desta pessoa", (PeerActionTarget)PeerActionTarget.SINGLE_PEER, peerTarget -> {
            this.openTab();
            if (peerTarget instanceof PeerTarget.Single) {
                PeerTarget.Single single = (PeerTarget.Single)peerTarget;
                this.panel.joinPeer(single.peer());
            }
        }));
    }

    private void openTab() {
        this.workspace.openOrFocusTab(this.getIdentifier(), this.getDisplayName());
    }

    public List<Class<? extends Message>> getMessages() {
        return List.of(TableInviteMessage.class, JoinRequestMessage.class, JoinRejectedMessage.class, PlayerActionMessage.class, TableStateMessage.class, TableClosedMessage.class);
    }

    public void receiveMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            if (this.controller == null) {
                return;
            }
            if (message instanceof TableInviteMessage) {
                TableInviteMessage tableInviteMessage = (TableInviteMessage)message;
                this.controller.onInvite(tableInviteMessage);
                if (this.controller.role() == BlackjackController.Role.IDLE) {
                    this.openTab();
                    this.workspace.setStatusMessage(tableInviteMessage.getHostName() + " convidou você para uma mesa de 21.");
                }
            } else if (message instanceof JoinRequestMessage) {
                JoinRequestMessage joinRequestMessage = (JoinRequestMessage)message;
                this.controller.onJoinRequest(joinRequestMessage);
            } else if (message instanceof JoinRejectedMessage) {
                JoinRejectedMessage joinRejectedMessage = (JoinRejectedMessage)message;
                this.controller.onJoinRejected(joinRejectedMessage);
            } else if (message instanceof PlayerActionMessage) {
                PlayerActionMessage playerActionMessage = (PlayerActionMessage)message;
                this.controller.onPlayerAction(playerActionMessage);
            } else if (message instanceof TableStateMessage) {
                TableStateMessage tableStateMessage = (TableStateMessage)message;
                this.controller.onState(tableStateMessage);
            } else if (message instanceof TableClosedMessage) {
                TableClosedMessage tableClosedMessage = (TableClosedMessage)message;
                this.controller.onTableClosed(tableClosedMessage);
            }
        });
    }

    public void onShellTabClosed() {
        if (this.controller != null) {
            this.controller.shutdown();
        }
    }

    public String getName() {
        return "Blackjack";
    }

    public String getDisplayName() {
        return "Vinte e Um (21)";
    }

    public String getAuthor() {
        return "Marcus";
    }

    public String getVersion() {
        return "1.0.0";
    }

    public String getIdentifier() {
        return "pitiupi.blackjack";
    }
}

