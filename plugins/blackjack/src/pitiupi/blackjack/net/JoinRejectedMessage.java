package pitiupi.blackjack.net;

import pitiupi.models.Messages.Message;

public class JoinRejectedMessage
extends Message {
    private static final long serialVersionUID = 1L;
    private String hostName;
    private String reason;

    public JoinRejectedMessage() {
    }

    public JoinRejectedMessage(String string, String string2) {
        this.hostName = string;
        this.reason = string2;
    }

    public String getHostName() {
        return this.hostName;
    }

    public String getReason() {
        return this.reason;
    }
}

