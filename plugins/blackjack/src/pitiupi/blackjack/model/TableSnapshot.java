package pitiupi.blackjack.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import pitiupi.blackjack.model.Card;

public record TableSnapshot(String tableId, String hostName, String hostId, Phase phase, int round, ArrayList<Card> dealerCards, int dealerVisibleTotal, ArrayList<SeatView> seats, String currentTurnId, String lastEvent, long eventSeq, int bet, int turnSeconds) implements Serializable
{
    private static final long serialVersionUID = 1L;

    public SeatView findSeat(String string) {
        for (SeatView seatView : this.seats) {
            if (!seatView.playerId().equals(string)) continue;
            return seatView;
        }
        return null;
    }

    public boolean isTurnOf(String string) {
        return this.phase == Phase.PLAYING && string != null && string.equals(this.currentTurnId);
    }

    public List<SeatView> seatList() {
        return this.seats;
    }

    public static enum Phase {
        LOBBY("Aguardando a primeira rodada"),
        PLAYING("Rodada em andamento"),
        ROUND_OVER("Rodada encerrada");

        private final String label;

        private Phase(String string2) {
            this.label = string2;
        }

        public String label() {
            return this.label;
        }
    }

    public record SeatView(String playerId, String name, boolean host, ArrayList<Card> cards, int total, int chips, SeatStatus status, RoundResult result, int lastPayout) implements Serializable
    {
        private static final long serialVersionUID = 1L;
    }

    public static enum RoundResult {
        NONE(""),
        WIN("ganhou"),
        BLACKJACK_WIN("ganhou com blackjack"),
        PUSH("empatou"),
        LOSE("perdeu");

        private final String label;

        private RoundResult(String string2) {
            this.label = string2;
        }

        public String label() {
            return this.label;
        }
    }

    public static enum SeatStatus {
        WAITING("aguardando próxima rodada"),
        PLAYING("jogando"),
        STOOD("parou"),
        BUST("estourou"),
        BLACKJACK("blackjack!"),
        OUT("sem fichas");

        private final String label;

        private SeatStatus(String string2) {
            this.label = string2;
        }

        public String label() {
            return this.label;
        }
    }
}

