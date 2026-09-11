package com.kai.casinox;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(4, 7, 16));
        getWindow().setNavigationBarColor(Color.rgb(4, 7, 16));
        setContentView(new CasinoView(this));
    }

    static class Hit {
        final RectF rect;
        final int id;

        Hit(RectF rect, int id) {
            this.rect = rect;
            this.id = id;
        }
    }

    static class CasinoView extends View {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Random rng = new Random();
        final AppState st;
        final ArrayList<Hit> hits = new ArrayList<>();

        int screen = 0;
        int game = -1;
        int bet = 1000;
        float scroll = 0;
        float lastY = 0;
        boolean moving = false;
        boolean winFx = false;
        long fxAt = 0;
        String msg = "READY TO PLAY";

        final int BG = Color.rgb(4, 7, 16);
        final int PANEL = Color.rgb(14, 20, 38);
        final int PANEL_2 = Color.rgb(20, 29, 52);
        final int PANEL_3 = Color.rgb(27, 38, 66);
        final int LINE = Color.rgb(45, 59, 91);
        final int WHITE = Color.rgb(247, 249, 255);
        final int MUTED = Color.rgb(143, 156, 183);
        final int GOLD = Color.rgb(247, 199, 77);
        final int GOLD_2 = Color.rgb(255, 226, 139);
        final int BLUE = Color.rgb(47, 111, 237);
        final int CYAN = Color.rgb(71, 210, 255);
        final int PURPLE = Color.rgb(143, 85, 255);
        final int RED = Color.rgb(238, 72, 103);
        final int GREEN = Color.rgb(50, 211, 153);

        final String[] names = {
                "Dragon Fortune", "Golden Phoenix", "Lucky Panda", "Royal Tiger",
                "Zeus Thunder", "Mystic Temple", "Ocean Treasure", "Pirate Gold",
                "Neon Fruits", "Fortune 888", "European Roulette", "Blackjack Royale",
                "Baccarat Elite", "Sic Bo Arena", "KAI X Crash", "2D Lucky Draw",
                "3D Lucky Draw", "4D Lucky Draw", "Neon Dice", "Coin Tower",
                "Lucky Wheel", "Royal Cards"
        };

        final String[] marks = {
                "龍", "鳳", "P", "虎", "⚡", "◆", "≈", "☠",
                "7", "888", "●", "A♠", "B", "⚂", "X", "2D",
                "3D", "4D", "⚄", "C", "◎", "♛"
        };

        CasinoView(Context context) {
            super(context);
            st = new AppState(context);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        float d(float value) {
            return value * getResources().getDisplayMetrics().density;
        }

        int alpha(int color, int value) {
            return Color.argb(value, Color.red(color), Color.green(color), Color.blue(color));
        }

        void text(Canvas c, String s, float x, float y, float size, int color, boolean bold) {
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            p.setTextSize(d(size));
            p.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
            c.drawText(s, x, y, p);
        }

        void center(Canvas c, String s, float x, float y, float size, int color, boolean bold) {
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            p.setTextSize(d(size));
            p.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
            c.drawText(s, x - p.measureText(s) / 2f, y, p);
        }

        void round(Canvas c, float left, float top, float right, float bottom, float radius, int color) {
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(color);
            c.drawRoundRect(left, top, right, bottom, d(radius), d(radius), p);
        }

        void strokeRound(Canvas c, float left, float top, float right, float bottom, float radius, int color, float width) {
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(d(width));
            p.setColor(color);
            c.drawRoundRect(left, top, right, bottom, d(radius), d(radius), p);
            p.setStyle(Paint.Style.FILL);
        }

        void divider(Canvas c, float left, float y, float right) {
            p.setColor(alpha(LINE, 150));
            p.setStrokeWidth(d(1));
            c.drawLine(left, y, right, y, p);
        }

        String money(long value) {
            return String.format(Locale.US, "%,d", value);
        }

        void hit(float left, float top, float right, float bottom, int id) {
            hits.add(new Hit(new RectF(left, top, right, bottom), id));
        }

        @Override
        protected void onDraw(Canvas c) {
            hits.clear();
            c.drawColor(BG);

            p.setShader(new RadialGradient(
                    getWidth() * .78f, d(40), d(330), alpha(BLUE, 62), Color.TRANSPARENT, Shader.TileMode.CLAMP));
            c.drawCircle(getWidth() * .78f, d(40), d(330), p);

            p.setShader(new RadialGradient(
                    d(20), getHeight() * .62f, d(260), alpha(PURPLE, 28), Color.TRANSPARENT, Shader.TileMode.CLAMP));
            c.drawCircle(d(20), getHeight() * .62f, d(260), p);
            p.setShader(null);

            if (screen == 10) {
                drawGame(c);
            } else {
                header(c);
                if (screen == 0) home(c);
                if (screen == 1) library(c);
                if (screen == 2) bonus(c);
                if (screen == 3) vip(c);
                if (screen == 4) profile(c);
                bottom(c);
            }

            if (winFx && System.currentTimeMillis() - fxAt < 1050) {
                drawWinFx(c);
                postInvalidateDelayed(16);
            } else {
                winFx = false;
            }
        }

        void logo(Canvas c, float x, float y) {
            p.setShader(new LinearGradient(x - d(18), y - d(18), x + d(18), y + d(18), GOLD_2, GOLD, Shader.TileMode.CLAMP));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(d(2.2f));
            c.drawCircle(x, y, d(18), p);
            p.setShader(null);
            p.setStrokeWidth(d(4.5f));
            p.setColor(BLUE);
            c.drawArc(x - d(12), y - d(12), x + d(12), y + d(12), -58, 235, false, p);
            p.setStyle(Paint.Style.FILL);
            center(c, "K", x, y + d(6), 17, WHITE, true);
        }

        void header(Canvas c) {
            logo(c, d(27), d(31));
            text(c, "KAI CASINO", d(52), d(28), 14, WHITE, true);
            text(c, "X", d(139), d(28), 14, GOLD, true);
            text(c, "VIRTUAL ENTERTAINMENT", d(52), d(43), 6.2f, MUTED, true);

            float left = getWidth() - d(142);
            round(c, left, d(11), getWidth() - d(10), d(54), 15, alpha(PANEL_2, 240));
            strokeRound(c, left, d(11), getWidth() - d(10), d(54), 15, alpha(LINE, 180), .8f);
            text(c, "KAI COINS", left + d(12), d(27), 6.3f, MUTED, true);
            text(c, "◆ " + money(st.coins), left + d(12), d(45), 10, GOLD_2, true);
        }

        void hero(Canvas c) {
            float left = d(10), top = d(68), right = getWidth() - d(10), bottom = top + d(160);
            RectF r = new RectF(left, top, right, bottom);
            p.setShader(new LinearGradient(r.left, r.top, r.right, r.bottom,
                    Color.rgb(12, 47, 105), Color.rgb(43, 17, 75), Shader.TileMode.CLAMP));
            c.drawRoundRect(r, d(26), d(26), p);
            p.setShader(null);
            strokeRound(c, left, top, right, bottom, 26, alpha(CYAN, 65), .8f);

            p.setColor(alpha(GOLD, 24));
            c.drawCircle(right - d(42), top + d(42), d(48), p);
            p.setColor(alpha(CYAN, 22));
            c.drawCircle(right - d(78), bottom - d(26), d(60), p);

            round(c, left + d(15), top + d(14), left + d(116), top + d(35), 10, alpha(CYAN, 28));
            text(c, "VIRTUAL PLAY ONLY", left + d(25), top + d(29), 6.4f, CYAN, true);
            text(c, "YOUR CASINO,", left + d(20), top + d(66), 21, WHITE, true);
            text(c, "REIMAGINED.", left + d(20), top + d(92), 21, GOLD_2, true);
            text(c, "22 instant games • zero real-money wagering", left + d(20), top + d(113), 7, alpha(WHITE, 190), false);

            round(c, left + d(20), top + d(126), left + d(126), top + d(151), 11, BLUE);
            center(c, "EXPLORE GAMES", left + d(73), top + d(143), 7, WHITE, true);
            hit(left + d(15), top + d(120), left + d(136), top + d(157), 1000);

            text(c, "JACKPOT", right - d(112), top + d(30), 6.4f, MUTED, true);
            text(c, money(128450000L + (System.currentTimeMillis() / 50) % 50000), right - d(112), top + d(49), 9.5f, GOLD_2, true);
            text(c, "DEMO COINS", right - d(112), top + d(64), 5.7f, MUTED, true);
        }

        void categoryStrip(Canvas c, float y) {
            String[] labels = {"FEATURED", "SLOTS", "TABLE", "ARCADE", "CRASH"};
            float x = d(10);
            for (int i = 0; i < labels.length; i++) {
                float width = d(labels[i].length() * 6.4f + 22);
                if (x + width > getWidth() - d(8)) break;
                round(c, x, y, x + width, y + d(30), 12, i == 0 ? alpha(GOLD, 30) : alpha(PANEL_2, 230));
                strokeRound(c, x, y, x + width, y + d(30), 12,
                        i == 0 ? alpha(GOLD, 100) : alpha(LINE, 140), .7f);
                center(c, labels[i], x + width / 2, y + d(20), 6.6f, i == 0 ? GOLD_2 : MUTED, true);
                hit(x, y, x + width, y + d(32), 1100 + i);
                x += width + d(6);
            }
        }

        void sectionTitle(Canvas c, String title, String sub, float y, boolean action) {
            text(c, title, d(12), y, 12, WHITE, true);
            if (sub != null) text(c, sub, d(12), y + d(16), 6.3f, MUTED, false);
            if (action) {
                text(c, "VIEW ALL", getWidth() - d(62), y, 6.4f, CYAN, true);
                hit(getWidth() - d(82), y - d(18), getWidth(), y + d(14), 1000);
            }
        }

        void home(Canvas c) {
            hero(c);
            categoryStrip(c, d(239));
            sectionTitle(c, "HOT RIGHT NOW", "Curated picks for quick play", d(291), true);
            gameGrid(c, 0, 4, d(12), d(320));
            sectionTitle(c, "TRENDING TABLES", null, d(583), true);
            wide(c, 10, d(12), d(598));
            wide(c, 11, d(12), d(669));
        }

        void gameGrid(Canvas c, int start, int count, float left, float top) {
            float gap = d(8);
            float w = (getWidth() - d(32) - gap) / 2f;
            for (int k = 0; k < count; k++) {
                int i = start + k;
                float x = left + (k % 2) * (w + gap);
                float y = top + (k / 2) * d(124);
                gameCard(c, i, x, y, w, d(115));
            }
        }

        int gameColor(int i) {
            switch (i % 6) {
                case 0: return GOLD;
                case 1: return CYAN;
                case 2: return PURPLE;
                case 3: return RED;
                case 4: return GREEN;
                default: return BLUE;
            }
        }

        void artwork(Canvas c, int i, float left, float top, float right, float bottom) {
            int accent = gameColor(i);
            p.setShader(new LinearGradient(left, top, right, bottom,
                    Color.rgb(18, 27, 53), alpha(accent, 125), Shader.TileMode.CLAMP));
            c.drawRoundRect(left, top, right, bottom, d(14), d(14), p);
            p.setShader(null);
            p.setColor(alpha(accent, 35));
            c.drawCircle(right - d(18), top + d(16), d(25), p);
            c.drawCircle(left + d(20), bottom - d(14), d(29), p);
            center(c, marks[i], (left + right) / 2f, top + (bottom - top) * .64f, 24, accent, true);
        }

        void gameCard(Canvas c, int i, float x, float y, float w, float h) {
            round(c, x, y, x + w, y + h, 18, alpha(PANEL, 245));
            strokeRound(c, x, y, x + w, y + h, 18, alpha(LINE, 145), .7f);
            artwork(c, i, x + d(6), y + d(6), x + w - d(6), y + d(67));

            String badge = i < 4 ? "HOT" : (i % 3 == 0 ? "NEW" : "PLAY");
            int badgeColor = i < 4 ? RED : gameColor(i);
            round(c, x + d(9), y + d(9), x + d(47), y + d(25), 7, alpha(badgeColor, 210));
            center(c, badge, x + d(28), y + d(20.5f), 5.7f, WHITE, true);

            text(c, names[i], x + d(9), y + d(87), 8.1f, WHITE, true);
            text(c, i < 10 ? "SLOT" : gameType(i), x + d(9), y + d(103), 5.8f, MUTED, true);
            round(c, x + w - d(43), y + d(77), x + w - d(8), y + d(107), 10, alpha(BLUE, 235));
            center(c, "›", x + w - d(25.5f), y + d(98), 17, WHITE, true);
            hit(x, y, x + w, y + h, 2000 + i);
        }

        String gameType(int i) {
            if (i == 10 || i == 11 || i == 12 || i == 13) return "TABLE";
            if (i == 14) return "CRASH";
            if (i >= 15 && i <= 17) return "DRAW";
            return "ARCADE";
        }

        void wide(Canvas c, int i, float x, float y) {
            float right = getWidth() - d(12);
            round(c, x, y, right, y + d(61), 17, alpha(PANEL, 245));
            strokeRound(c, x, y, right, y + d(61), 17, alpha(LINE, 140), .7f);
            artwork(c, i, x + d(7), y + d(7), x + d(63), y + d(54));
            text(c, names[i], x + d(76), y + d(24), 10.6f, WHITE, true);
            text(c, gameType(i) + " • INSTANT PLAY", x + d(76), y + d(43), 6.2f, MUTED, true);
            round(c, right - d(48), y + d(13), right - d(10), y + d(49), 11, alpha(BLUE, 235));
            center(c, "›", right - d(29), y + d(38), 19, WHITE, true);
            hit(x, y, right, y + d(61), 2000 + i);
        }

        void library(Canvas c) {
            text(c, "GAME LIBRARY", d(12), d(84), 20, WHITE, true);
            text(c, "22 playable demo games", d(12), d(104), 7.3f, MUTED, false);
            round(c, getWidth() - d(94), d(74), getWidth() - d(12), d(104), 12, alpha(PANEL_2, 230));
            center(c, "ALL GAMES", getWidth() - d(53), d(94), 6.4f, CYAN, true);

            float gap = d(8);
            float w = (getWidth() - d(32) - gap) / 2f;
            float top = d(121) - scroll;
            for (int i = 0; i < names.length; i++) {
                float x = d(12) + (i % 2) * (w + gap);
                float y = top + (i / 2) * d(124);
                if (y > -d(125) && y < getHeight() - d(58)) gameCard(c, i, x, y, w, d(115));
            }
        }

        void bonus(Canvas c) {
            text(c, "BONUS CENTER", d(12), d(84), 20, WHITE, true);
            text(c, "Progress and virtual rewards", d(12), d(104), 7.2f, MUTED, false);

            float l = d(12), t = d(121), r = getWidth() - d(12);
            round(c, l, t, r, t + d(111), 20, alpha(PANEL, 245));
            strokeRound(c, l, t, r, t + d(111), 20, alpha(GOLD, 75), .8f);
            round(c, l + d(14), t + d(14), l + d(93), t + d(35), 10, alpha(GOLD, 28));
            text(c, "DAILY REWARD", l + d(24), t + d(29), 6.2f, GOLD_2, true);
            text(c, "DAY " + st.loginDay, l + d(16), t + d(61), 10, WHITE, true);
            text(c, "+" + money(st.dailyReward()) + " COINS", l + d(16), t + d(88), 16, GOLD_2, true);
            round(c, r - d(104), t + d(48), r - d(15), t + d(91), 13, BLUE);
            center(c, "CLAIM", r - d(59.5f), t + d(75), 8, WHITE, true);
            hit(r - d(112), t + d(40), r - d(9), t + d(99), 3000);

            text(c, "MISSIONS", d(12), d(267), 11.5f, WHITE, true);
            text(c, "Complete goals to earn more demo coins", d(12), d(284), 6.3f, MUTED, false);
            String[] labels = {"Play 10 games", "Spin 25 times", "Play 3 game types", "Win 5 rounds"};
            for (int i = 0; i < labels.length; i++) {
                float y = d(300 + i * 64);
                boolean complete = st.mission(i), claimed = st.claimed(i);
                round(c, d(12), y, getWidth() - d(12), y + d(53), 15, alpha(PANEL, 245));
                strokeRound(c, d(12), y, getWidth() - d(12), y + d(53), 15, alpha(LINE, 130), .7f);
                round(c, d(23), y + d(12), d(52), y + d(41), 10,
                        claimed ? alpha(GREEN, 45) : complete ? alpha(GOLD, 40) : alpha(PANEL_3, 220));
                center(c, claimed ? "✓" : String.valueOf(i + 1), d(37.5f), y + d(32), 9,
                        claimed ? GREEN : complete ? GOLD : MUTED, true);
                text(c, labels[i], d(64), y + d(22), 9.2f, WHITE, true);
                text(c, "+25,000 coins • +100 XP", d(64), y + d(40), 6.1f, MUTED, false);
                int btn = claimed ? PANEL_3 : complete ? BLUE : PANEL_2;
                round(c, getWidth() - d(92), y + d(11), getWidth() - d(22), y + d(42), 10, btn);
                center(c, claimed ? "DONE" : complete ? "CLAIM" : "LOCKED",
                        getWidth() - d(57), y + d(31.5f), 6.2f,
                        claimed ? GREEN : complete ? WHITE : MUTED, true);
                hit(getWidth() - d(100), y + d(5), getWidth() - d(15), y + d(48), 3010 + i);
            }
        }

        void vip(Canvas c) {
            text(c, "VIP CLUB", d(12), d(84), 20, WHITE, true);
            text(c, "Prestige earned only through demo play", d(12), d(104), 7.2f, MUTED, false);

            float l = d(12), t = d(121), r = getWidth() - d(12);
            p.setShader(new LinearGradient(l, t, r, t + d(125), Color.rgb(31, 26, 43), Color.rgb(18, 23, 43), Shader.TileMode.CLAMP));
            c.drawRoundRect(l, t, r, t + d(125), d(21), d(21), p);
            p.setShader(null);
            strokeRound(c, l, t, r, t + d(125), 21, alpha(GOLD, 90), .8f);
            text(c, st.tier(), l + d(17), t + d(38), 22, GOLD_2, true);
            text(c, "LEVEL " + st.level(), l + d(17), t + d(61), 8.5f, WHITE, true);
            text(c, "XP " + money(st.xp), l + d(17), t + d(81), 7.2f, MUTED, false);

            float progress = (st.xp % 500) / 500f;
            round(c, l + d(17), t + d(95), r - d(17), t + d(108), 7, PANEL_3);
            round(c, l + d(17), t + d(95), l + d(17) + (r - l - d(34)) * progress, t + d(108), 7, GOLD);
            text(c, Math.round(progress * 100) + "% TO NEXT LEVEL", l + d(17), t + d(121), 5.8f, MUTED, true);

            text(c, "STATUS LADDER", d(12), d(286), 11.5f, WHITE, true);
            String[] tiers = {"BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "KAI ELITE"};
            for (int i = 0; i < tiers.length; i++) {
                float y = d(309 + i * 52);
                boolean active = i < Math.min(6, 1 + st.xp / 1500);
                round(c, d(12), y, getWidth() - d(12), y + d(42), 14, alpha(PANEL, 235));
                text(c, active ? "◆" : "◇", d(24), y + d(27), 10, active ? GOLD : MUTED, true);
                text(c, tiers[i], d(47), y + d(25), 9.4f, active ? WHITE : MUTED, true);
                text(c, active ? "UNLOCKED" : "LOCKED", getWidth() - d(82), y + d(25), 6.1f, active ? GREEN : MUTED, true);
            }
        }

        void profile(Canvas c) {
            text(c, "PLAYER PROFILE", d(12), d(84), 20, WHITE, true);
            text(c, "Local demo progress", d(12), d(104), 7.2f, MUTED, false);

            float l = d(12), t = d(121), r = getWidth() - d(12);
            round(c, l, t, r, t + d(134), 20, alpha(PANEL, 245));
            strokeRound(c, l, t, r, t + d(134), 20, alpha(LINE, 145), .8f);
            p.setShader(new LinearGradient(l + d(16), t + d(20), l + d(80), t + d(84), BLUE, PURPLE, Shader.TileMode.CLAMP));
            c.drawRoundRect(l + d(16), t + d(20), l + d(80), t + d(84), d(22), d(22), p);
            p.setShader(null);
            center(c, "KX", l + d(48), t + d(62), 19, WHITE, true);
            text(c, st.tier(), l + d(98), t + d(39), 13, GOLD_2, true);
            text(c, "LEVEL " + st.level() + " • XP " + money(st.xp), l + d(98), t + d(62), 7.5f, MUTED, false);
            text(c, st.gamesPlayed + " PLAYED  •  " + st.wins + " WINS", l + d(98), t + d(83), 7.1f, WHITE, true);
            divider(c, l + d(16), t + d(101), r - d(16));
            text(c, "BIGGEST WIN", l + d(16), t + d(121), 6.2f, MUTED, true);
            text(c, money(st.biggestWin) + " COINS", l + d(105), t + d(121), 7.8f, CYAN, true);

            text(c, "PREFERENCES", d(12), d(293), 11.5f, WHITE, true);
            setting(c, 310, "MUSIC", "Ambient casino audio", st.music, 0);
            setting(c, 372, "SFX", "Game feedback sounds", st.sfx, 1);
            setting(c, 434, "VIBRATION", "Haptic feedback", st.vibration, 2);
            round(c, d(12), d(515), getWidth() - d(12), d(569), 15, alpha(PANEL, 235));
            text(c, "VIRTUAL CREDITS ONLY", d(25), d(538), 7.2f, GOLD_2, true);
            text(c, "No deposit • no withdrawal • no cash-out", d(25), d(556), 6.2f, MUTED, false);
        }

        void setting(Canvas c, float yy, String label, String sub, boolean on, int id) {
            float y = d(yy);
            round(c, d(12), y, getWidth() - d(12), y + d(52), 15, alpha(PANEL, 242));
            strokeRound(c, d(12), y, getWidth() - d(12), y + d(52), 15, alpha(LINE, 125), .7f);
            text(c, label, d(25), y + d(22), 9.1f, WHITE, true);
            text(c, sub, d(25), y + d(39), 6.1f, MUTED, false);
            float l = getWidth() - d(79), r = getWidth() - d(24);
            round(c, l, y + d(12), r, y + d(40), 14, on ? BLUE : PANEL_3);
            p.setColor(WHITE);
            c.drawCircle(on ? r - d(14) : l + d(14), y + d(26), d(9), p);
            hit(d(12), y, getWidth() - d(12), y + d(52), 4000 + id);
        }

        void bottom(Canvas c) {
            float y = getHeight() - d(60);
            round(c, d(7), y, getWidth() - d(7), getHeight() - d(6), 20, alpha(Color.rgb(8, 13, 27), 250));
            strokeRound(c, d(7), y, getWidth() - d(7), getHeight() - d(6), 20, alpha(LINE, 120), .8f);
            String[] icons = {"⌂", "▦", "✦", "♛", "◎"};
            String[] labels = {"HOME", "GAMES", "BONUS", "VIP", "PROFILE"};
            for (int i = 0; i < 5; i++) {
                float left = i * getWidth() / 5f, right = (i + 1) * getWidth() / 5f, x = (left + right) / 2f;
                if (screen == i) round(c, x - d(20), y + d(6), x + d(20), y + d(29), 11, alpha(GOLD, 25));
                center(c, icons[i], x, y + d(21), 13, screen == i ? GOLD_2 : MUTED, true);
                center(c, labels[i], x, y + d(42), 5.8f, screen == i ? GOLD_2 : MUTED, true);
                hit(left, y, right, getHeight(), 5000 + i);
            }
        }

        void drawGame(Canvas c) {
            text(c, "‹ BACK", d(12), d(36), 8.5f, CYAN, true);
            hit(0, 0, d(74), d(58), 6000);
            text(c, names[game], d(80), d(34), 11.5f, WHITE, true);
            text(c, gameTypeLabel(game), d(80), d(49), 5.9f, MUTED, true);

            float walletLeft = getWidth() - d(123);
            round(c, walletLeft, d(14), getWidth() - d(10), d(51), 13, alpha(PANEL_2, 238));
            center(c, "◆ " + money(st.coins), (walletLeft + getWidth() - d(10)) / 2f, d(38), 8.3f, GOLD_2, true);

            if (game < 10) slot(c);
            else if (game == 10) roulette(c);
            else if (game == 11) blackjack(c);
            else if (game == 12) baccarat(c);
            else if (game == 13) sicbo(c);
            else if (game == 14) crash(c);
            else if (game >= 15 && game <= 17) lottery(c);
            else arcade(c);
            betBar(c);
        }

        String gameTypeLabel(int id) {
            if (id < 10) return "VIDEO SLOT • DEMO";
            if (id == 10) return "EUROPEAN ROULETTE • DEMO";
            if (id == 11) return "BLACKJACK • DEMO";
            if (id == 12) return "BACCARAT • DEMO";
            if (id == 13) return "SIC BO • DEMO";
            if (id == 14) return "CRASH STYLE • DEMO";
            if (id >= 15 && id <= 17) return "SIMULATED DRAW • DEMO";
            return "ARCADE • DEMO";
        }

        void stage(Canvas c, String eyebrow) {
            round(c, d(10), d(72), getWidth() - d(10), d(101), 12, alpha(GOLD, 20));
            text(c, eyebrow, d(22), d(92), 6.8f, GOLD_2, true);
            float top = d(109), bottom = getHeight() - d(137);
            round(c, d(10), top, getWidth() - d(10), bottom, 24, alpha(PANEL, 246));
            strokeRound(c, d(10), top, getWidth() - d(10), bottom, 24, alpha(LINE, 145), .8f);
        }

        void slot(Canvas c) {
            stage(c, "5×3 VIDEO SLOT");
            float left = d(24), top = d(145), gap = d(5);
            float rw = (getWidth() - d(48) - gap * 4) / 5f;
            String[] symbols = {"A", "K", "Q", "J", "10", "W", "S", "7", "♛", "龍"};
            for (int col = 0; col < 5; col++) {
                for (int row = 0; row < 3; row++) {
                    float x = left + col * (rw + gap), y = top + row * d(72);
                    round(c, x, y, x + rw, y + d(64), 11, PANEL_2);
                    strokeRound(c, x, y, x + rw, y + d(64), 11, alpha(LINE, 140), .6f);
                    String s = symbols[(game * 3 + col * 5 + row + rng.nextInt(2)) % symbols.length];
                    center(c, s, x + rw / 2f, y + d(41), 17, s.equals("W") ? GOLD_2 : s.equals("S") ? CYAN : WHITE, true);
                }
            }
            center(c, "WILD • SCATTER • BONUS • FREE SPINS", getWidth() / 2f, d(384), 6.8f, MUTED, true);
            resultMessage(c, d(426));
        }

        void roulette(Canvas c) {
            stage(c, "EUROPEAN ROULETTE");
            float cx = getWidth() / 2f, cy = d(245);
            for (int i = 0; i < 18; i++) {
                p.setColor(i % 2 == 0 ? RED : Color.rgb(31, 33, 42));
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(d(14));
                c.drawArc(cx - d(78), cy - d(78), cx + d(78), cy + d(78), i * 20, 16, false, p);
            }
            p.setStyle(Paint.Style.FILL);
            p.setColor(GREEN);
            c.drawCircle(cx, cy, d(47), p);
            center(c, "0–36", cx, cy + d(7), 19, WHITE, true);
            center(c, "RED   BLACK   ODD   EVEN   1–18   19–36", cx, d(367), 7.1f, WHITE, true);
            resultMessage(c, d(414));
        }

        void blackjack(Canvas c) {
            stage(c, "BLACKJACK ROYALE");
            text(c, "DEALER", d(25), d(149), 6.8f, MUTED, true);
            cards(c, d(42), d(164), false);
            text(c, "PLAYER", d(25), d(310), 6.8f, MUTED, true);
            cards(c, d(42), d(325), true);
            center(c, "HIT    STAND    DOUBLE    SPLIT", getWidth() / 2f, d(453), 7.8f, CYAN, true);
            resultMessage(c, d(491));
        }

        void cards(Canvas c, float x, float y, boolean red) {
            for (int i = 0; i < 3; i++) {
                round(c, x + i * d(72), y, x + d(58) + i * d(72), y + d(86), 10, Color.rgb(239, 242, 248));
                center(c, i == 2 ? "?" : (red ? "A♥" : "K♠"), x + d(29) + i * d(72), y + d(47), 15,
                        red ? RED : Color.BLACK, true);
            }
        }

        void baccarat(Canvas c) {
            stage(c, "BACCARAT ELITE");
            text(c, "PLAYER", d(54), d(154), 7.5f, CYAN, true);
            text(c, "BANKER", getWidth() - d(111), d(154), 7.5f, GOLD_2, true);
            cards(c, d(28), d(177), true);
            cards(c, getWidth() / 2f + d(5), d(177), false);
            center(c, "PLAYER     BANKER     TIE", getWidth() / 2f, d(389), 8.3f, WHITE, true);
            resultMessage(c, d(438));
        }

        void sicbo(Canvas c) {
            stage(c, "SIC BO ARENA");
            float cx = getWidth() / 2f;
            for (int i = 0; i < 3; i++) {
                round(c, cx - d(115) + i * d(82), d(176), cx - d(55) + i * d(82), d(236), 13, WHITE);
                center(c, "⚄", cx - d(85) + i * d(82), d(216), 24, Color.BLACK, true);
            }
            center(c, "BIG   SMALL   ODD   EVEN", cx, d(325), 8.3f, CYAN, true);
            center(c, "TOTAL • DOUBLE • TRIPLE • NUMBER", cx, d(361), 7.2f, MUTED, true);
            resultMessage(c, d(419));
        }

        void crash(Canvas c) {
            stage(c, "KAI X CRASH");
            Path path = new Path();
            path.moveTo(d(36), d(400));
            path.cubicTo(d(98), d(365), d(153), d(285), getWidth() - d(55), d(164));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(d(5));
            p.setColor(CYAN);
            c.drawPath(path, p);
            p.setStyle(Paint.Style.FILL);
            center(c, "X", getWidth() - d(67), d(177), 25, GOLD_2, true);
            center(c, String.format(Locale.US, "%.2fx", 1.0 + rng.nextDouble() * 4.5), getWidth() / 2f, d(284), 34, WHITE, true);
            center(c, "CASH OUT BEFORE THE SIMULATED CRASH", getWidth() / 2f, d(423), 6.6f, MUTED, true);
        }

        void lottery(Canvas c) {
            stage(c, "INTERNAL SIMULATED DRAW");
            int digits = game == 15 ? 2 : game == 16 ? 3 : 4;
            StringBuilder n = new StringBuilder();
            for (int i = 0; i < digits; i++) n.append(rng.nextInt(10));
            center(c, n.toString(), getWidth() / 2f, d(246), 48, GOLD_2, true);
            center(c, "DRAW NUMBER", getWidth() / 2f, d(294), 6.5f, MUTED, true);
            center(c, "PREVIOUS RESULTS   •   MY TICKETS", getWidth() / 2f, d(355), 7.4f, CYAN, true);
            resultMessage(c, d(414));
        }

        void arcade(Canvas c) {
            stage(c, "ARCADE CASINO GAME");
            center(c, marks[game], getWidth() / 2f, d(263), 66, gameColor(game), true);
            center(c, "FAST PLAY • RANDOMIZED VIRTUAL OUTCOME", getWidth() / 2f, d(365), 6.6f, MUTED, true);
            resultMessage(c, d(420));
        }

        void resultMessage(Canvas c, float y) {
            int color = msg.startsWith("WIN") ? GOLD_2 : msg.startsWith("INSUFFICIENT") ? RED : CYAN;
            round(c, d(24), y - d(23), getWidth() - d(24), y + d(10), 12, alpha(color, 18));
            center(c, msg, getWidth() / 2f, y, 8.4f, color, true);
        }

        void betBar(Canvas c) {
            float top = getHeight() - d(121);
            round(c, d(10), top, getWidth() - d(10), getHeight() - d(65), 18, alpha(Color.rgb(8, 14, 30), 248));
            strokeRound(c, d(10), top, getWidth() - d(10), getHeight() - d(65), 18, alpha(LINE, 150), .8f);
            text(c, "BET", d(21), top + d(18), 5.8f, MUTED, true);
            round(c, d(20), top + d(24), d(151), top + d(51), 10, PANEL_2);
            center(c, "−", d(37), top + d(43), 13, CYAN, true);
            center(c, money(bet), d(86), top + d(43), 7.8f, WHITE, true);
            center(c, "+", d(135), top + d(43), 13, CYAN, true);
            hit(d(15), top + d(17), d(59), top + d(57), 6100);
            hit(d(113), top + d(17), d(159), top + d(57), 6101);
            round(c, d(166), top + d(11), getWidth() - d(20), top + d(53), 14, BLUE);
            center(c, game < 10 ? "SPIN" : "PLAY", (d(166) + getWidth() - d(20)) / 2f, top + d(38), 10.5f, WHITE, true);
            hit(d(160), top + d(5), getWidth() - d(12), top + d(60), 6200);
        }

        void play() {
            if (!st.bet(bet)) {
                msg = "INSUFFICIENT KAI COINS";
                invalidate();
                return;
            }
            long payout = 0;
            int roll = rng.nextInt(100);
            if (game < 10) {
                if (roll < 8) payout = bet * 8L;
                else if (roll < 28) payout = bet * 2L;
            } else {
                if (roll < 12) payout = bet * 5L;
                else if (roll < 45) payout = bet * 2L;
            }
            if (payout > 0) {
                st.reward(payout, game, game < 10);
                msg = "WIN +" + money(payout) + " COINS";
                winFx = true;
                fxAt = System.currentTimeMillis();
            } else {
                st.reward(0, game, game < 10);
                msg = "ROUND COMPLETE";
            }
            invalidate();
        }

        void drawWinFx(Canvas c) {
            long dt = System.currentTimeMillis() - fxAt;
            float q = Math.min(1f, dt / 700f);
            for (int i = 0; i < 18; i++) {
                double angle = i * .72 + dt * .004;
                float radius = d(28 + i * 4) * q;
                float x = getWidth() / 2f + (float) Math.cos(angle) * radius;
                float y = getHeight() / 2f + (float) Math.sin(angle) * radius;
                p.setColor(i % 2 == 0 ? GOLD : CYAN);
                c.drawCircle(x, y, d(3 + (i % 3)), p);
            }
            round(c, getWidth() / 2f - d(82), getHeight() / 2f - d(34), getWidth() / 2f + d(82), getHeight() / 2f + d(22), 18, alpha(BG, 225));
            strokeRound(c, getWidth() / 2f - d(82), getHeight() / 2f - d(34), getWidth() / 2f + d(82), getHeight() / 2f + d(22), 18, alpha(GOLD, 130), 1);
            center(c, "BIG WIN", getWidth() / 2f, getHeight() / 2f + d(3), 20, GOLD_2, true);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            float x = e.getX(), y = e.getY();
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                lastY = y;
                moving = false;
                return true;
            }
            if (e.getAction() == MotionEvent.ACTION_MOVE) {
                if (screen == 1) {
                    float dy = lastY - y;
                    if (Math.abs(dy) > 2) moving = true;
                    scroll = Math.max(0, Math.min(d(900), scroll + dy));
                    lastY = y;
                    invalidate();
                }
                return true;
            }
            if (e.getAction() != MotionEvent.ACTION_UP) return true;
            if (moving) return true;
            for (Hit h : new ArrayList<>(hits)) {
                if (h.rect.contains(x, y)) {
                    handle(h.id);
                    return true;
                }
            }
            return true;
        }

        void handle(int id) {
            if (id == 1000) {
                screen = 1;
                scroll = 0;
            } else if (id >= 1100 && id < 1110) {
                screen = 1;
                scroll = 0;
            } else if (id >= 2000 && id < 2100) {
                game = id - 2000;
                screen = 10;
                msg = "READY TO PLAY";
            } else if (id == 3000) {
                st.claimDaily();
            } else if (id >= 3010 && id < 3014) {
                int mission = id - 3010;
                if (st.mission(mission) && !st.claimed(mission)) st.claimMission(mission);
            } else if (id >= 4000 && id < 4003) {
                int setting = id - 4000;
                if (setting == 0) st.music = !st.music;
                if (setting == 1) st.sfx = !st.sfx;
                if (setting == 2) st.vibration = !st.vibration;
                st.save();
            } else if (id >= 5000 && id < 5005) {
                screen = id - 5000;
                scroll = 0;
            } else if (id == 6000) {
                screen = 1;
                game = -1;
            } else if (id == 6100) {
                bet = Math.max(100, bet / 2);
            } else if (id == 6101) {
                bet = Math.min(100000, bet * 2);
            } else if (id == 6200) {
                play();
            }
            invalidate();
        }
    }
}
