package pitiupi.blackjack.model;

import java.io.Serializable;

public record Card(int rank, Suit suit) implements Serializable
{
    private static final long serialVersionUID = 1L;

    public Card {
        if (rank < 1 || rank > 13) {
            throw new IllegalArgumentException("rank inválido: " + rank);
        }
        if (suit == null) {
            throw new IllegalArgumentException("naipe nulo");
        }
    }

    public int points() {
        return Math.min(this.rank, 10);
    }

    public boolean isAce() {
        return this.rank == 1;
    }

    public String rankLabel() {
        return switch (this.rank) {
            case 1 -> "A";
            case 11 -> "J";
            case 12 -> "Q";
            case 13 -> "K";
            default -> Integer.toString(this.rank);
        };
    }

    @Override
    public String toString() {
        return this.rankLabel() + this.suit.symbol();
    }

    public static enum Suit {
        ESPADAS("♠", false),
        COPAS("♥", true),
        OUROS("♦", true),
        PAUS("♣", false);

        private final String symbol;
        private final boolean red;

        private Suit(String string2, boolean bl) {
            this.symbol = string2;
            this.red = bl;
        }

        public String symbol() {
            return this.symbol;
        }

        public boolean isRed() {
            return this.red;
        }
    }
}

