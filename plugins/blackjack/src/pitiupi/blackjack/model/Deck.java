package pitiupi.blackjack.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;
import pitiupi.blackjack.model.Card;

public final class Deck {
    private final Deque<Card> cards = new ArrayDeque<Card>();

    public Deck(Random random) {
        ArrayList<Card> arrayList = new ArrayList<Card>(52);
        for (Card.Suit suit : Card.Suit.values()) {
            for (int i = 1; i <= 13; ++i) {
                arrayList.add(new Card(i, suit));
            }
        }
        Collections.shuffle(arrayList, random);
        this.cards.addAll(arrayList);
    }

    public Deck(List<Card> list) {
        this.cards.addAll(list);
    }

    public Card draw() {
        Card card = this.cards.pollFirst();
        if (card == null) {
            throw new IllegalStateException("baralho vazio");
        }
        return card;
    }

    public int remaining() {
        return this.cards.size();
    }
}

