package pitiupi.blackjack.GUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import pitiupi.blackjack.model.Card;
import pitiupi.blackjack.model.TableSnapshot;

public final class TableCanvas
extends JComponent {
    private static final int CARD_W = 58;
    private static final int CARD_H = 82;
    private static final Color FELT = new Color(17, 102, 58);
    private static final Color FELT_DARK = new Color(10, 70, 40);
    private static final Color GOLD = new Color(240, 200, 80);
    private TableSnapshot snapshot;
    private String myId;

    public TableCanvas() {
        this.setPreferredSize(new Dimension(720, 400));
        this.setMinimumSize(new Dimension(400, 300));
        this.setFont(new Font("SansSerif", 0, 12));
    }

    public void show(TableSnapshot tableSnapshot, String string) {
        this.snapshot = tableSnapshot;
        this.myId = string;
        this.repaint();
    }

    public void clear() {
        this.snapshot = null;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D)graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int n = this.getWidth();
        int n2 = this.getHeight();
        graphics2D.setPaint(new GradientPaint(0.0f, 0.0f, FELT, 0.0f, n2, FELT_DARK));
        graphics2D.fillRect(0, 0, n, n2);
        graphics2D.setColor(new Color(255, 255, 255, 40));
        graphics2D.setStroke(new BasicStroke(3.0f));
        graphics2D.drawRoundRect(12, 12, n - 24, n2 - 24, 40, 40);
        if (this.snapshot == null) {
            this.drawEmpty(graphics2D, n, n2);
            graphics2D.dispose();
            return;
        }
        this.drawHeader(graphics2D, n);
        this.drawDealer(graphics2D, n);
        this.drawSeats(graphics2D, n, n2);
        graphics2D.dispose();
    }

    private void drawEmpty(Graphics2D graphics2D, int n, int n2) {
        graphics2D.setColor(GOLD);
        graphics2D.setFont(this.getFont().deriveFont(1, 34.0f));
        TableCanvas.centered(graphics2D, "21", n / 2, n2 / 2 - 20);
        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(this.getFont().deriveFont(0, 14.0f));
        TableCanvas.centered(graphics2D, "Abra uma mesa (você vira a banca) ou entre na mesa de alguém.", n / 2, n2 / 2 + 14);
        TableCanvas.centered(graphics2D, "A banca compra até 17 · Blackjack paga 3:2 · aposta fixa por rodada", n / 2, n2 / 2 + 36);
    }

    private void drawHeader(Graphics2D graphics2D, int n) {
        graphics2D.setColor(new Color(255, 255, 255, 200));
        graphics2D.setFont(this.getFont().deriveFont(0, 12.0f));
        String string = "Mesa de " + this.snapshot.hostName() + " · rodada " + this.snapshot.round() + " · aposta " + this.snapshot.bet() + " fichas";
        graphics2D.drawString(string, 26, 32);
        String string2 = this.snapshot.phase().label();
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        graphics2D.drawString(string2, n - 26 - fontMetrics.stringWidth(string2), 32);
    }

    private void drawDealer(Graphics2D graphics2D, int n) {
        ArrayList<Card> arrayList = this.snapshot.dealerCards();
        int n2 = 52;
        graphics2D.setColor(GOLD);
        graphics2D.setFont(this.getFont().deriveFont(1, 15.0f));
        boolean bl = arrayList.contains(null);
        String string = arrayList.isEmpty() ? "" : " — " + this.snapshot.dealerVisibleTotal() + (bl ? " + ?" : "");
        TableCanvas.centered(graphics2D, "BANCA" + string, n / 2, n2 + 4);
        this.drawHand(graphics2D, arrayList, n / 2, n2 + 16, false);
    }

    private void drawSeats(Graphics2D graphics2D, int n, int n2) {
        ArrayList<TableSnapshot.SeatView> arrayList = this.snapshot.seats();
        int n3 = Math.max(1, arrayList.size());
        int n4 = 24;
        int n5 = 10;
        int n6 = Math.min(230, (n - 2 * n4 - (n3 - 1) * n5) / n3);
        int n7 = Math.max(170, n2 - 200);
        int n8 = n3 * n6 + (n3 - 1) * n5;
        int n9 = (n - n8) / 2;
        int n10 = n2 - n7 - 22;
        for (TableSnapshot.SeatView seatView : arrayList) {
            this.drawSeat(graphics2D, seatView, n9, n10, n6, n7);
            n9 += n6 + n5;
        }
    }

    private void drawSeat(Graphics2D graphics2D, TableSnapshot.SeatView seatView, int n, int n2, int n3, int n4) {
        Color color;
        Object object;
        boolean bl = this.snapshot.isTurnOf(seatView.playerId());
        boolean bl2 = seatView.playerId().equals(this.myId);
        graphics2D.setColor(new Color(0, 0, 0, bl2 ? 70 : 45));
        graphics2D.fillRoundRect(n, n2, n3, n4, 18, 18);
        if (bl) {
            graphics2D.setColor(GOLD);
            graphics2D.setStroke(new BasicStroke(3.0f));
            graphics2D.drawRoundRect(n, n2, n3, n4, 18, 18);
        } else if (bl2) {
            graphics2D.setColor(new Color(255, 255, 255, 120));
            graphics2D.setStroke(new BasicStroke(1.5f));
            graphics2D.drawRoundRect(n, n2, n3, n4, 18, 18);
        }
        int n5 = n + n3 / 2;
        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(this.getFont().deriveFont(1, 14.0f));
        String string = seatView.name() + (bl2 ? " (você)" : "") + (seatView.host() ? " ★" : "");
        TableCanvas.centered(graphics2D, TableCanvas.fit(graphics2D, string, n3 - 12), n5, n2 + 20);
        graphics2D.setFont(this.getFont().deriveFont(0, 12.0f));
        graphics2D.setColor(new Color(255, 255, 255, 190));
        TableCanvas.centered(graphics2D, seatView.chips() + " fichas", n5, n2 + 37);
        this.drawHand(graphics2D, seatView.cards(), n5, n2 + 46, true);
        int n6 = n2 + 46 + 82 + 22;
        if (!seatView.cards().isEmpty()) {
            graphics2D.setFont(this.getFont().deriveFont(1, 16.0f));
            graphics2D.setColor(seatView.total() > 21 ? new Color(255, 120, 120) : Color.WHITE);
            TableCanvas.centered(graphics2D, Integer.toString(seatView.total()), n5, n6);
        }
        graphics2D.setFont(this.getFont().deriveFont(0, 12.0f));
        if (seatView.result() != TableSnapshot.RoundResult.NONE) {
            object = seatView.result().label() + TableCanvas.payoutText(seatView.lastPayout());
            color = switch (seatView.result()) {
                case WIN, BLACKJACK_WIN -> new Color(140, 255, 150);
                case LOSE -> new Color(255, 140, 140);
                default -> new Color(230, 230, 230);
            };
        } else if (bl) {
            object = bl2 ? "▶ sua vez!" : "▶ decidindo…";
            color = GOLD;
        } else {
            object = seatView.status().label();
            color = new Color(220, 220, 220);
        }
        graphics2D.setColor(color);
        TableCanvas.centered(graphics2D, TableCanvas.fit(graphics2D, (String)object, n3 - 10), n5, n6 + 18);
    }

    private static String payoutText(int n) {
        if (n > 0) {
            return " (+" + n + ")";
        }
        if (n < 0) {
            return " (" + n + ")";
        }
        return "";
    }

    private void drawHand(Graphics2D graphics2D, List<Card> list, int n, int n2, boolean bl) {
        if (list.isEmpty()) {
            return;
        }
        int n3 = bl ? Math.min(62, 26) : 66;
        int n4 = 58 + n3 * (list.size() - 1);
        int n5 = n - n4 / 2;
        for (Card card : list) {
            this.drawCard(graphics2D, card, n5, n2);
            n5 += n3;
        }
    }

    private void drawCard(Graphics2D graphics2D, Card card, int n, int n2) {
        graphics2D.setColor(new Color(0, 0, 0, 60));
        graphics2D.fillRoundRect(n + 2, n2 + 3, 58, 82, 10, 10);
        if (card == null) {
            graphics2D.setColor(new Color(35, 60, 140));
            graphics2D.fillRoundRect(n, n2, 58, 82, 10, 10);
            Graphics2D graphics2D2 = (Graphics2D)graphics2D.create();
            graphics2D2.clipRect(n, n2, 58, 82);
            graphics2D2.setColor(new Color(255, 255, 255, 70));
            graphics2D2.setStroke(new BasicStroke(1.0f));
            for (int i = -82; i < 58; i += 8) {
                graphics2D2.drawLine(n + i, n2, n + i + 82, n2 + 82);
            }
            graphics2D2.dispose();
            graphics2D.setColor(Color.WHITE);
            graphics2D.setStroke(new BasicStroke(2.0f));
            graphics2D.drawRoundRect(n + 3, n2 + 3, 52, 76, 8, 8);
            return;
        }
        graphics2D.setColor(Color.WHITE);
        graphics2D.fillRoundRect(n, n2, 58, 82, 10, 10);
        graphics2D.setColor(new Color(180, 180, 180));
        graphics2D.setStroke(new BasicStroke(1.0f));
        graphics2D.drawRoundRect(n, n2, 58, 82, 10, 10);
        graphics2D.setColor(card.suit().isRed() ? new Color(200, 30, 40) : new Color(25, 25, 25));
        graphics2D.setFont(this.getFont().deriveFont(1, 15.0f));
        graphics2D.drawString(card.rankLabel(), n + 6, n2 + 18);
        graphics2D.setFont(this.getFont().deriveFont(0, 13.0f));
        graphics2D.drawString(card.suit().symbol(), n + 6, n2 + 33);
        graphics2D.setFont(this.getFont().deriveFont(0, 30.0f));
        TableCanvas.centered(graphics2D, card.suit().symbol(), n + 29 + 4, n2 + 41 + 18);
    }

    private static void centered(Graphics2D graphics2D, String string, int n, int n2) {
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        graphics2D.drawString(string, n - fontMetrics.stringWidth(string) / 2, n2);
    }

    private static String fit(Graphics2D graphics2D, String string, int n) {
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        if (fontMetrics.stringWidth(string) <= n) {
            return string;
        }
        String string2 = string;
        while (string2.length() > 1 && fontMetrics.stringWidth(string2 + "…") > n) {
            string2 = string2.substring(0, string2.length() - 1);
        }
        return string2 + "…";
    }
}

