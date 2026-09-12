package com.kai.casinox;

import android.graphics.*;
import android.os.Bundle;
import android.content.Context;

public class GameActivityV24 extends GameActivity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        int game = Math.max(0, Math.min(21, getIntent().getIntExtra("game", 0)));
        view = new EnhancedGameView(this, game);
        setContentView(view);
    }

    static class EnhancedGameView extends GameActivity.GameView {
        int ticket = -1, drawNumber = -1, drawMatches = 0;
        ArcadeGameEngine.DiceResult neonDice;
        int diceChoice = 0;
        boolean towerActive = false;
        int towerLevel = 0;
        int wheelSegment = -1;
        boolean royalActive = false;
        int royalFirst = -1, royalSecond = -1, royalChoice = -1;

        EnhancedGameView(Context c, int game) {
            super(c, game);
            if (game >= 15 && game <= 17) ticket = ArcadeGameEngine.quickPick(rng, game - 13);
        }

        @Override String typeLabel() {
            if (game == 15) return "2D LUCKY DRAW • QUICK PICK";
            if (game == 16) return "3D LUCKY DRAW • QUICK PICK";
            if (game == 17) return "4D LUCKY DRAW • QUICK PICK";
            if (game == 18) return "NEON DICE • OVER / UNDER 7";
            if (game == 19) return "COIN TOWER • RISK LADDER";
            if (game == 20) return "LUCKY WHEEL • 12 SEGMENTS";
            if (game == 21) return "ROYAL CARDS • HIGHER / LOWER";
            return super.typeLabel();
        }

        @Override String actionLabel() {
            if (game >= 15 && game <= 17) return "DRAW NUMBER";
            if (game == 18) return "ROLL 2 DICE";
            if (game == 20) return "SPIN WHEEL";
            return super.actionLabel();
        }

        @Override void generic(Canvas c) {
            if (game >= 15 && game <= 17) drawLucky(c);
            else if (game == 18) drawNeonDice(c);
            else if (game == 19) drawCoinTower(c);
            else if (game == 20) drawLuckyWheel(c);
            else if (game == 21) drawRoyalCards(c);
            else super.generic(c);
        }

        void drawLucky(Canvas c) {
            int digits = game - 13;
            float cx = getWidth() / 2f;
            txt(c, "YOUR QUICK PICK", d(25), d(145), 6.2f, MUTED, true);
            rr(c, d(24), d(160), getWidth() - d(24), d(229), 18, PANEL2);
            ctr(c, ArcadeGameEngine.number(ticket, digits), cx, d(207), 31, GOLD2, true);
            txt(c, "DRAW RESULT", d(25), d(264), 6.2f, MUTED, true);
            rr(c, d(24), d(279), getWidth() - d(24), d(348), 18, PANEL3);
            ctr(c, drawNumber < 0 ? repeat("–", digits) : ArcadeGameEngine.number(drawNumber, digits), cx, d(326), 31, drawNumber < 0 ? MUTED : CYAN, true);

            String match = drawNumber < 0 ? "Match digits from the right" : (drawMatches == 0 ? "NO MATCH" : drawMatches + " DIGIT" + (drawMatches > 1 ? "S" : "") + " MATCHED");
            ctr(c, match, cx, d(378), 6.5f, drawMatches > 0 ? GOLD2 : MUTED, true);
            button(c, d(24), d(393), d(134), d(428), "NEW TICKET", 8000, PANEL3);
            txt(c, digits == 2 ? "Exact pays 75x" : digits == 3 ? "Exact pays 400x" : "Exact pays 2500x", d(150), d(416), 6.2f, MUTED, true);
            message(c, d(462));
        }

        String repeat(String s, int count) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < count; i++) { if (i > 0) b.append(' '); b.append(s); }
            return b.toString();
        }

        void drawNeonDice(Canvas c) {
            float cx = getWidth() / 2f;
            int a = neonDice == null ? 0 : neonDice.a, b = neonDice == null ? 0 : neonDice.b;
            drawDie(c, cx - d(82), d(165), a);
            drawDie(c, cx + d(18), d(165), b);
            ctr(c, neonDice == null ? "TOTAL —" : "TOTAL " + neonDice.total, cx, d(272), 18, neonDice == null ? MUTED : GOLD2, true);

            String[] labels = {"UNDER 7", "EXACT 7", "OVER 7"};
            float w = (getWidth() - d(48)) / 3f;
            for (int i = 0; i < 3; i++) {
                float x = d(16) + i * (w + d(8));
                boolean selected = diceChoice == i;
                rr(c, x, d(316), x + w, d(354), 11, selected ? BLUE : PANEL2);
                ctr(c, labels[i], x + w / 2f, d(341), 6.2f, selected ? WHITE : MUTED, true);
                hit(x, d(312), x + w, d(358), 8100 + i);
            }
            ctr(c, "UNDER / OVER pays 2x • EXACT 7 pays 5x", cx, d(383), 6.1f, MUTED, true);
            message(c, d(427));
        }

        void drawDie(Canvas c, float x, float y, int value) {
            rr(c, x, y, x + d(64), y + d(64), 15, WHITE);
            sr(c, x, y, x + d(64), y + d(64), 15, alpha(CYAN, 90), .8f);
            ctr(c, value == 0 ? "•" : String.valueOf(value), x + d(32), y + d(42), 24, Color.rgb(16, 22, 38), true);
        }

        void drawCoinTower(Canvas c) {
            float left = d(42), right = getWidth() - d(42);
            txt(c, "CLIMB HIGHER FOR A BIGGER CASH-OUT", d(24), d(142), 6.2f, MUTED, true);
            for (int floor = 5; floor >= 1; floor--) {
                int level = floor;
                float y = d(160 + (5 - floor) * 51);
                boolean reached = towerLevel >= level;
                boolean next = towerActive && towerLevel + 1 == level;
                int col = reached ? alpha(GREEN, 55) : next ? alpha(GOLD, 45) : PANEL2;
                rr(c, left, y, right, y + d(39), 12, col);
                sr(c, left, y, right, y + d(39), 12, reached ? alpha(GREEN, 130) : next ? alpha(GOLD, 130) : alpha(LINE, 120), .7f);
                txt(c, "LEVEL " + level, left + d(14), y + d(25), 7.2f, reached ? GREEN : WHITE, true);
                String mult = String.format(java.util.Locale.US, "%.2fx", ArcadeGameEngine.TOWER_MULT[level]);
                txt(c, mult, right - d(55), y + d(25), 7.2f, reached ? GREEN : GOLD2, true);
            }
            String status = towerActive ? "SAFE AT LEVEL " + towerLevel + " • NEXT SUCCESS " + Math.round(ArcadeGameEngine.TOWER_CHANCE[Math.min(towerLevel,4)] * 100) + "%" : "START A NEW TOWER ROUND";
            ctr(c, status, getWidth() / 2f, d(438), 6.4f, towerActive ? CYAN : MUTED, true);
            message(c, d(476));
        }

        void drawLuckyWheel(Canvas c) {
            float cx = getWidth() / 2f, cy = d(246), radius = d(94);
            RectF wheel = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
            for (int i = 0; i < ArcadeGameEngine.WHEEL_MULT.length; i++) {
                p.setStyle(Paint.Style.FILL);
                int col = i % 3 == 0 ? alpha(GOLD, 150) : i % 3 == 1 ? alpha(CYAN, 115) : PANEL3;
                p.setColor(col);
                c.drawArc(wheel, -90 + i * 30, 28, true, p);
            }
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(d(3)); p.setColor(GOLD2); c.drawCircle(cx, cy, radius, p); p.setStyle(Paint.Style.FILL);
            rr(c, cx - d(34), cy - d(24), cx + d(34), cy + d(24), 14, BG);
            String center = wheelSegment < 0 ? "SPIN" : multiplierLabel(ArcadeGameEngine.WHEEL_MULT[wheelSegment]);
            ctr(c, center, cx, cy + d(6), 12, GOLD2, true);
            Path pointer = new Path(); pointer.moveTo(cx, cy - radius - d(14)); pointer.lineTo(cx - d(10), cy - radius + d(4)); pointer.lineTo(cx + d(10), cy - radius + d(4)); pointer.close(); p.setColor(RED); c.drawPath(pointer, p);
            if (wheelSegment >= 0) ctr(c, "SEGMENT " + (wheelSegment + 1) + " • " + multiplierLabel(ArcadeGameEngine.WHEEL_MULT[wheelSegment]), cx, d(376), 7.1f, CYAN, true);
            else ctr(c, "12 segments • up to 10x", cx, d(376), 6.4f, MUTED, true);
            message(c, d(425));
        }

        String multiplierLabel(double mult) {
            if (mult == 0) return "0x";
            if (mult == Math.rint(mult)) return ((int) mult) + "x";
            return String.format(java.util.Locale.US, "%.1fx", mult);
        }

        void drawRoyalCards(Canvas c) {
            float cx = getWidth() / 2f;
            txt(c, "FIRST CARD", d(32), d(151), 6.2f, MUTED, true);
            txt(c, "NEXT CARD", getWidth() - d(96), d(151), 6.2f, MUTED, true);
            drawRoyalCard(c, cx - d(118), d(172), royalFirst);
            drawRoyalCard(c, cx + d(34), d(172), royalSecond);
            ctr(c, royalActive ? "WILL THE NEXT CARD BE HIGHER OR LOWER?" : "DEAL A CARD TO BEGIN", cx, d(330), 6.6f, royalActive ? CYAN : MUTED, true);
            if (royalActive) {
                button(c, d(24), d(356), cx - d(7), d(397), "HIGHER", 8300, BLUE);
                button(c, cx + d(7), d(356), getWidth() - d(24), d(397), "LOWER", 8301, PANEL3);
            } else if (royalSecond > 0) {
                String result = royalSecond == royalFirst ? "PUSH" : (royalChoice == 0 ? (royalSecond > royalFirst ? "HIGHER ✓" : "LOWER ✕") : (royalSecond < royalFirst ? "LOWER ✓" : "HIGHER ✕"));
                ctr(c, result, cx, d(386), 9, royalSecond == royalFirst ? CYAN : msg.startsWith("WIN") ? GREEN : RED, true);
            }
            message(c, d(440));
        }

        void drawRoyalCard(Canvas c, float x, float y, int rank) {
            rr(c, x, y, x + d(84), y + d(122), 14, rank < 0 ? PANEL2 : WHITE);
            sr(c, x, y, x + d(84), y + d(122), 14, rank < 0 ? LINE : alpha(GOLD, 120), .8f);
            ctr(c, rank < 0 ? "?" : ArcadeGameEngine.cardLabel(rank), x + d(42), y + d(70), 30, rank < 0 ? MUTED : Color.rgb(18, 23, 38), true);
        }

        @Override void betBar(Canvas c) {
            if (game == 19) {
                float top = getHeight() - d(121);
                if (!towerActive) {
                    betAdjust(c, top);
                    button(c, d(165), top + d(10), getWidth() - d(20), top + d(53), "START TOWER", 6200, BLUE);
                } else {
                    txt(c, "STAKE " + money(bet), d(20), top + d(18), 6, MUTED, true);
                    txt(c, "SAFE " + multiplierLabel(ArcadeGameEngine.TOWER_MULT[towerLevel]), d(20), top + d(42), 8.2f, GOLD2, true);
                    float mid = getWidth() / 2f;
                    button(c, d(123), top + d(10), mid - d(6), top + d(53), "CLIMB", 8200, BLUE);
                    button(c, mid + d(6), top + d(10), getWidth() - d(20), top + d(53), "CASH OUT", 8201, GREEN);
                }
                return;
            }
            if (game == 21) {
                float top = getHeight() - d(121);
                if (!royalActive) {
                    betAdjust(c, top);
                    button(c, d(165), top + d(10), getWidth() - d(20), top + d(53), "DEAL FIRST CARD", 6200, BLUE);
                } else {
                    txt(c, "STAKE", d(20), top + d(18), 5.8f, MUTED, true);
                    txt(c, money(bet), d(20), top + d(43), 8.4f, GOLD2, true);
                    button(c, d(112), top + d(10), getWidth()/2f - d(6), top + d(53), "HIGHER", 8300, BLUE);
                    button(c, getWidth()/2f + d(6), top + d(10), getWidth() - d(20), top + d(53), "LOWER", 8301, PANEL3);
                }
                return;
            }
            super.betBar(c);
        }

        @Override void play() {
            if (game >= 15 && game <= 17) {
                if (!charge(bet)) return;
                int digits = game - 13;
                drawNumber = ArcadeGameEngine.quickPick(rng, digits);
                drawMatches = ArcadeGameEngine.suffixMatches(ticket, drawNumber, digits);
                long payout = ArcadeGameEngine.drawPayout(bet, digits, drawMatches);
                String result = payout > 0 ? "WIN +" + money(payout) : "DRAW COMPLETE";
                settle(payout, false, result);
            } else if (game == 18) {
                if (!charge(bet)) return;
                neonDice = new ArcadeGameEngine.DiceResult(rng);
                long payout = neonDice.payout(bet, diceChoice);
                settle(payout, false, payout > 0 ? "WIN +" + money(payout) : "TOTAL " + neonDice.total + " • NO WIN");
            } else if (game == 19) {
                startTower();
            } else if (game == 20) {
                if (!charge(bet)) return;
                wheelSegment = ArcadeGameEngine.spinWheel(rng);
                long payout = ArcadeGameEngine.wheelPayout(bet, wheelSegment);
                settle(payout, false, payout > 0 ? "WIN +" + money(payout) : "WHEEL • 0x");
            } else if (game == 21) {
                startRoyal();
            } else super.play();
            invalidate();
        }

        void startTower() {
            if (towerActive) return;
            if (!charge(bet)) return;
            towerActive = true;
            towerLevel = 0;
            msg = "TOWER STARTED • CHOOSE CLIMB OR CASH OUT";
        }

        void climbTower() {
            if (!towerActive) return;
            if (ArcadeGameEngine.towerClimb(rng, towerLevel)) {
                towerLevel++;
                if (towerLevel >= 5) {
                    long payout = ArcadeGameEngine.towerPayout(bet, towerLevel);
                    towerActive = false;
                    settle(payout, false, "WIN +" + money(payout) + " • TOWER CLEARED");
                } else {
                    msg = "LEVEL " + towerLevel + " SAFE • " + multiplierLabel(ArcadeGameEngine.TOWER_MULT[towerLevel]);
                }
            } else {
                towerActive = false;
                st.reward(0, game, false);
                msg = "TOWER FELL • ROUND LOST";
            }
        }

        void cashTower() {
            if (!towerActive) return;
            long payout = ArcadeGameEngine.towerPayout(bet, towerLevel);
            towerActive = false;
            settle(payout, false, "CASHED +" + money(payout) + " • " + multiplierLabel(ArcadeGameEngine.TOWER_MULT[towerLevel]));
        }

        void startRoyal() {
            if (royalActive) { msg = "CHOOSE HIGHER OR LOWER"; return; }
            if (!charge(bet)) return;
            royalFirst = ArcadeGameEngine.card(rng);
            royalSecond = -1;
            royalChoice = -1;
            royalActive = true;
            msg = "FIRST CARD " + ArcadeGameEngine.cardLabel(royalFirst) + " • MAKE YOUR CALL";
        }

        void resolveRoyal(int choice) {
            if (!royalActive) return;
            royalChoice = choice;
            royalSecond = ArcadeGameEngine.card(rng);
            long payout = ArcadeGameEngine.royalPayout(bet, royalFirst, royalSecond, choice);
            royalActive = false;
            String result;
            if (royalSecond == royalFirst) result = "PUSH • STAKE RETURNED";
            else if (payout > 0) result = "WIN +" + money(payout);
            else result = "ROUND LOST";
            settle(payout, false, result);
        }

        @Override void handle(int id) {
            if (id == 8000) {
                if (!roundLocked()) { int digits = game - 13; ticket = ArcadeGameEngine.quickPick(rng, digits); drawNumber = -1; drawMatches = 0; msg = "NEW QUICK PICK READY"; }
                invalidate(); return;
            }
            if (id >= 8100 && id < 8103) { diceChoice = id - 8100; msg = "BET SELECTED"; invalidate(); return; }
            if (id == 8200) { climbTower(); invalidate(); return; }
            if (id == 8201) { cashTower(); invalidate(); return; }
            if (id == 8300 || id == 8301) { resolveRoyal(id - 8300); invalidate(); return; }
            super.handle(id);
        }

        @Override boolean roundLocked() {
            return towerActive || royalActive || super.roundLocked();
        }

        @Override void abandonActiveRound() {
            if (towerActive) { towerActive = false; st.reward(0, game, false); }
            if (royalActive) { royalActive = false; st.reward(0, game, false); }
            super.abandonActiveRound();
        }
    }
}
