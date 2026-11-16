package pitiupi.blackjack.net;

import pitiupi.models.Messages.Message;

public class TableClosedMessage
extends Message {
    private static final long serialVersionUID = 1L;
    private String tableId;
    private String hostName;

    public TableClosedMessage() {
    }

    public TableClosedMessage(String string, String string2) {
        this.tableId = string;
        this.hostName = string2;
    }

    public String getTableId() {
        return this.tableId;
    }

    public String getHostName() {
        return this.hostName;
    }
}

