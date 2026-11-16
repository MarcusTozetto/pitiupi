package pitiupi.blackjack.net;

import pitiupi.models.Messages.Message;

public class PlayerActionMessage
extends Message {
    private static final long serialVersionUID = 1L;
    private String tableId;
    private String playerIp;
    private Action action;

    public PlayerActionMessage() {
    }

    public PlayerActionMessage(String string, String string2, Action action) {
        this.tableId = string;
        this.playerIp = string2;
        this.action = action;
    }

    public String getTableId() {
        return this.tableId;
    }

    public String getPlayerIp() {
        return this.playerIp;
    }

    public Action getAction() {
        return this.action;
    }

    public static enum Action {
        HIT,
        STAND,
        LEAVE;

    }
}

