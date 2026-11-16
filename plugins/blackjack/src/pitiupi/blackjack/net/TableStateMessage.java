package pitiupi.blackjack.net;

import pitiupi.blackjack.model.TableSnapshot;
import pitiupi.models.Messages.Message;

public class TableStateMessage
extends Message {
    private static final long serialVersionUID = 1L;
    private TableSnapshot snapshot;

    public TableStateMessage() {
    }

    public TableStateMessage(TableSnapshot tableSnapshot) {
        this.snapshot = tableSnapshot;
    }

    public TableSnapshot getSnapshot() {
        return this.snapshot;
    }
}

