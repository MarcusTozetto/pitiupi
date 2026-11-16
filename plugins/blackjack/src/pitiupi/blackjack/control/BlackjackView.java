package pitiupi.blackjack.control;

import pitiupi.blackjack.control.BlackjackController;
import pitiupi.blackjack.model.TableSnapshot;

public interface BlackjackView {
    public void showSnapshot(TableSnapshot var1, String var2);

    public void clearTable();

    public void appendLog(String var1);

    public void onRoleChanged(BlackjackController.Role var1, String var2);

    public void showInvite(String var1, String var2);

    public void hideInvite();
}

