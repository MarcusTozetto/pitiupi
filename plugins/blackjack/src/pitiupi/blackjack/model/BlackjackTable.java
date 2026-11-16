package pitiupi.blackjack.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import pitiupi.blackjack.model.Card;
import pitiupi.blackjack.model.Deck;
import pitiupi.blackjack.model.HandValue;
import pitiupi.blackjack.model.TableSnapshot;

public final class BlackjackTable {
    public static final int MAX_PLAYERS = 5;
    public static final int START_CHIPS = 100;
    public static final int BET = 10;
    public static final int DEALER_STANDS_AT = 17;
    private final String tableId;
    private final String hostId;
    private final String hostName;
    private final Supplier<Deck> deckFactory;
    private final List<Seat> seats = new ArrayList<Seat>();
    private final List<Card> dealer = new ArrayList<Card>();
    private Deck deck;
    private TableSnapshot.Phase phase = TableSnapshot.Phase.LOBBY;
    private int round;
    private int turnIndex = -1;
    private String lastEvent = "";
    private long eventSeq;
    private int turnSeconds;

    public BlackjackTable(String string, String string2, String string3, Random random) {
        this(string, string2, string3, () -> new Deck(random));
    }

    public BlackjackTable(String string, String string2, String string3, Supplier<Deck> supplier) {
        this.tableId = string;
        this.hostId = string2;
        this.hostName = string3;
        this.deckFactory = supplier;
        this.seats.add(new Seat(string2, string3, true));
        this.event(string3 + " abriu a mesa e é a banca.");
    }

    public void setTurnSeconds(int n) {
        this.turnSeconds = n;
    }

    public JoinResult join(String string, String string2) {
        Seat seat = this.find(string);
        if (seat != null) {
            seat.name = string2;
            this.event(string2 + " voltou à mesa.");
            return JoinResult.ALREADY_SEATED;
        }
        if (this.seats.size() >= 5) {
            return JoinResult.FULL;
        }
        this.seats.add(new Seat(string, string2, false));
        this.event(string2 + (this.phase == TableSnapshot.Phase.PLAYING ? " sentou na mesa e entra na próxima rodada." : " sentou na mesa."));
        return JoinResult.OK;
    }

    public boolean leave(String string) {
        int n = this.indexOf(string);
        if (n < 0 || this.seats.get((int)n).host) {
            return false;
        }
        Seat seat = this.seats.remove(n);
        String string2 = seat.name + " saiu da mesa.";
        if (this.phase == TableSnapshot.Phase.PLAYING) {
            if (n < this.turnIndex) {
                --this.turnIndex;
            } else if (n == this.turnIndex) {
                --this.turnIndex;
                this.event(string2);
                this.advanceTurn();
                return true;
            }
        }
        this.event(string2);
        return true;
    }

    public boolean isSeated(String string) {
        return this.find(string) != null;
    }

    public List<String> guestIds() {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (Seat seat : this.seats) {
            if (seat.host) continue;
            arrayList.add(seat.id);
        }
        return arrayList;
    }

    public boolean startRound() {
        if (this.phase == TableSnapshot.Phase.PLAYING) {
            return false;
        }
        this.deck = this.deckFactory.get();
        this.dealer.clear();
        ++this.round;
        int n = 0;
        for (Seat object : this.seats) {
            object.cards.clear();
            object.result = TableSnapshot.RoundResult.NONE;
            object.lastPayout = 0;
            if (object.chips < 10) {
                object.status = TableSnapshot.SeatStatus.OUT;
                continue;
            }
            object.chips -= 10;
            object.status = TableSnapshot.SeatStatus.PLAYING;
            ++n;
        }
        if (n == 0) {
            --this.round;
            this.event("Ninguém tem fichas para apostar.");
            return false;
        }
        for (int i = 0; i < 2; ++i) {
            for (Seat seat : this.seats) {
                if (seat.status != TableSnapshot.SeatStatus.PLAYING) continue;
                seat.cards.add(this.deck.draw());
            }
            this.dealer.add(this.deck.draw());
        }
        for (Seat seat : this.seats) {
            if (seat.status != TableSnapshot.SeatStatus.PLAYING || !HandValue.isBlackjack(seat.cards)) continue;
            seat.status = TableSnapshot.SeatStatus.BLACKJACK;
        }
        this.phase = TableSnapshot.Phase.PLAYING;
        this.turnIndex = -1;
        this.event("Rodada " + this.round + ": cartas distribuídas. Banca mostra " + String.valueOf(this.dealer.get(0)) + ".");
        this.advanceTurn();
        return true;
    }

    public boolean hit(String string) {
        Seat seat = this.currentSeatIf(string);
        if (seat == null) {
            return false;
        }
        Card card = this.deck.draw();
        seat.cards.add(card);
        int n = HandValue.total(seat.cards);
        if (n > 21) {
            seat.status = TableSnapshot.SeatStatus.BUST;
            this.event(seat.name + " pediu " + String.valueOf(card) + " e estourou com " + n + ".");
            this.advanceTurn();
        } else if (n == 21) {
            seat.status = TableSnapshot.SeatStatus.STOOD;
            this.event(seat.name + " pediu " + String.valueOf(card) + " e fez 21!");
            this.advanceTurn();
        } else {
            this.event(seat.name + " pediu " + String.valueOf(card) + " (" + n + ").");
        }
        return true;
    }

    public boolean stand(String string) {
        Seat seat = this.currentSeatIf(string);
        if (seat == null) {
            return false;
        }
        seat.status = TableSnapshot.SeatStatus.STOOD;
        this.event(seat.name + " parou com " + HandValue.total(seat.cards) + ".");
        this.advanceTurn();
        return true;
    }

    public boolean forceStand(String string, String string2) {
        Seat seat = this.currentSeatIf(string);
        if (seat == null) {
            return false;
        }
        seat.status = TableSnapshot.SeatStatus.STOOD;
        this.event(seat.name + " " + string2 + " e parou com " + HandValue.total(seat.cards) + ".");
        this.advanceTurn();
        return true;
    }

    public String currentTurnId() {
        if (this.phase != TableSnapshot.Phase.PLAYING || this.turnIndex < 0 || this.turnIndex >= this.seats.size()) {
            return null;
        }
        return this.seats.get((int)this.turnIndex).id;
    }

    public TableSnapshot.Phase phase() {
        return this.phase;
    }

    private Seat currentSeatIf(String string) {
        if (this.phase != TableSnapshot.Phase.PLAYING || this.turnIndex < 0 || this.turnIndex >= this.seats.size()) {
            return null;
        }
        Seat seat = this.seats.get(this.turnIndex);
        return seat.id.equals(string) && seat.status == TableSnapshot.SeatStatus.PLAYING ? seat : null;
    }

    private void advanceTurn() {
        for (int i = this.turnIndex + 1; i < this.seats.size(); ++i) {
            if (this.seats.get((int)i).status != TableSnapshot.SeatStatus.PLAYING) continue;
            this.turnIndex = i;
            return;
        }
        this.turnIndex = -1;
        this.dealerPlaysAndSettles();
    }

    private void dealerPlaysAndSettles() {
        boolean bl = false;
        for (Seat seat : this.seats) {
            if (seat.status != TableSnapshot.SeatStatus.STOOD) continue;
            bl = true;
            break;
        }
        if (bl) {
            while (HandValue.total(this.dealer) < 17) {
                this.dealer.add(this.deck.draw());
            }
        }
        int n = HandValue.total(this.dealer);
        boolean bl2 = HandValue.isBlackjack(this.dealer);
        boolean bl3 = n > 21;
        StringBuilder stringBuilder = new StringBuilder("Banca revela ").append(this.dealer).append(" = ").append(n).append(bl3 ? " (estourou)" : (bl2 ? " (blackjack)" : "")).append(". ");
        for (Seat seat : this.seats) {
            if (!seat.inRound()) continue;
            int n2 = HandValue.total(seat.cards);
            TableSnapshot.RoundResult roundResult = seat.status == TableSnapshot.SeatStatus.BUST ? TableSnapshot.RoundResult.LOSE : (seat.status == TableSnapshot.SeatStatus.BLACKJACK ? (bl2 ? TableSnapshot.RoundResult.PUSH : TableSnapshot.RoundResult.BLACKJACK_WIN) : (bl2 ? TableSnapshot.RoundResult.LOSE : (bl3 || n2 > n ? TableSnapshot.RoundResult.WIN : (n2 == n ? TableSnapshot.RoundResult.PUSH : TableSnapshot.RoundResult.LOSE))));
            int n3 = switch (roundResult) {
                case WIN -> 20;
                case BLACKJACK_WIN -> 25;
                case PUSH -> 10;
                default -> 0;
            };
            seat.chips += n3;
            seat.result = roundResult;
            seat.lastPayout = n3 - 10;
            stringBuilder.append(seat.name).append(' ').append(roundResult.label()).append(". ");
        }
        this.phase = TableSnapshot.Phase.ROUND_OVER;
        this.event(stringBuilder.toString().trim());
    }

    public TableSnapshot snapshot() {
        int n;
        ArrayList<Card> arrayList = new ArrayList<Card>(this.dealer);
        if (this.phase == TableSnapshot.Phase.PLAYING && arrayList.size() >= 2) {
            arrayList.set(1, null);
            n = HandValue.total(arrayList);
        } else {
            n = HandValue.total(arrayList);
        }
        ArrayList<TableSnapshot.SeatView> arrayList2 = new ArrayList<TableSnapshot.SeatView>();
        for (Seat seat : this.seats) {
            arrayList2.add(new TableSnapshot.SeatView(seat.id, seat.name, seat.host, new ArrayList<Card>(seat.cards), HandValue.total(seat.cards), seat.chips, seat.status, seat.result, seat.lastPayout));
        }
        return new TableSnapshot(this.tableId, this.hostName, this.hostId, this.phase, this.round, arrayList, n, arrayList2, this.currentTurnId(), this.lastEvent, this.eventSeq, 10, this.turnSeconds);
    }

    private void event(String string) {
        this.lastEvent = string;
        ++this.eventSeq;
    }

    private Seat find(String string) {
        int n = this.indexOf(string);
        return n < 0 ? null : this.seats.get(n);
    }

    private int indexOf(String string) {
        for (int i = 0; i < this.seats.size(); ++i) {
            if (!this.seats.get((int)i).id.equals(string)) continue;
            return i;
        }
        return -1;
    }

    private static final class Seat {
        final String id;
        String name;
        final boolean host;
        final List<Card> cards = new ArrayList<Card>();
        int chips = 100;
        TableSnapshot.SeatStatus status = TableSnapshot.SeatStatus.WAITING;
        TableSnapshot.RoundResult result = TableSnapshot.RoundResult.NONE;
        int lastPayout;

        Seat(String string, String string2, boolean bl) {
            this.id = string;
            this.name = string2;
            this.host = bl;
        }

        boolean inRound() {
            return this.status == TableSnapshot.SeatStatus.PLAYING || this.status == TableSnapshot.SeatStatus.STOOD || this.status == TableSnapshot.SeatStatus.BUST || this.status == TableSnapshot.SeatStatus.BLACKJACK;
        }
    }

    public static enum JoinResult {
        OK,
        ALREADY_SEATED,
        FULL;

    }
}

