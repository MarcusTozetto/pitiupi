package pitiupi.blackjack.net;

import pitiupi.models.Messages.Message;

public class JoinRequestMessage
extends Message {
    private static final long serialVersionUID = 1L;
    private String playerName;
    private String playerIp;

    public JoinRequestMessage() {
    }

    public JoinRequestMessage(String string, String string2) {
        this.playerName = string;
        this.playerIp = string2;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public String getPlayerIp() {
        return this.playerIp;
    }
}

