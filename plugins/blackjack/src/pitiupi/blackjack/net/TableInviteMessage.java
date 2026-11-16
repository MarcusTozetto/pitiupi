package pitiupi.blackjack.net;

import pitiupi.models.Messages.Message;

public class TableInviteMessage
extends Message {
    private static final long serialVersionUID = 1L;
    private String tableId;
    private String hostName;
    private String hostIp;

    public TableInviteMessage() {
    }

    public TableInviteMessage(String string, String string2, String string3) {
        this.tableId = string;
        this.hostName = string2;
        this.hostIp = string3;
    }

    public String getTableId() {
        return this.tableId;
    }

    public String getHostName() {
        return this.hostName;
    }

    public String getHostIp() {
        return this.hostIp;
    }
}

