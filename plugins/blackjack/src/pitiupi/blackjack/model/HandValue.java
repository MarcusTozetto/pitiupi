package pitiupi.blackjack.model;

import java.util.List;
import pitiupi.blackjack.model.Card;

public final class HandValue {
    private HandValue() {
    }

    public static int total(List<Card> list) {
        int n = 0;
        boolean bl = false;
        for (Card card : list) {
            if (card == null) continue;
            n += card.points();
            bl |= card.isAce();
        }
        if (bl && n + 10 <= 21) {
            n += 10;
        }
        return n;
    }

    public static boolean isBlackjack(List<Card> list) {
        return list.size() == 2 && list.get(0) != null && list.get(1) != null && HandValue.total(list) == 21;
    }

    public static boolean isBust(List<Card> list) {
        return HandValue.total(list) > 21;
    }
}

