package com.kai.casinox;

import java.util.Locale;
import java.util.Random;

final class ArcadeGameEngine {
    private ArcadeGameEngine() {}

    static int pow10(int digits) {
        int v = 1;
        for (int i = 0; i < digits; i++) v *= 10;
        return v;
    }

    static int quickPick(Random rng, int digits) {
        return rng.nextInt(pow10(digits));
    }

    static String number(int value, int digits) {
        return String.format(Locale.US, "%0" + digits + "d", value);
    }

    static int suffixMatches(int ticket, int draw, int digits) {
        int matches = 0;
        for (int i = 0; i < digits; i++) {
            if (ticket % 10 != draw % 10) break;
            matches++;
            ticket /= 10;
            draw /= 10;
        }
        return matches;
    }

    static long drawPayout(long bet, int digits, int matches) {
        if (matches <= 0) return 0;
        if (matches == digits) {
            if (digits == 2) return bet * 75L;
            if (digits == 3) return bet * 400L;
            return bet * 2500L;
        }
        if (matches == 1) return bet * 2L;
        if (matches == 2) return bet * (digits == 3 ? 8L : 5L);
        if (matches == 3) return bet * 25L;
        return 0;
    }

    static final class DiceResult {
        final int a, b, total;
        DiceResult(Random rng) {
            a = 1 + rng.nextInt(6);
            b = 1 + rng.nextInt(6);
            total = a + b;
        }
        long payout(long bet, int choice) {
            if (choice == 0 && total < 7) return bet * 2L;
            if (choice == 1 && total == 7) return bet * 5L;
            if (choice == 2 && total > 7) return bet * 2L;
            return 0;
        }
    }

    static final double[] TOWER_MULT = {1.0, 1.30, 1.80, 2.70, 4.20, 7.00};
    static final double[] TOWER_CHANCE = {0.82, 0.70, 0.58, 0.46, 0.34};

    static boolean towerClimb(Random rng, int currentLevel) {
        if (currentLevel < 0 || currentLevel >= TOWER_CHANCE.length) return false;
        return rng.nextDouble() < TOWER_CHANCE[currentLevel];
    }

    static long towerPayout(long bet, int level) {
        int safe = Math.max(0, Math.min(level, TOWER_MULT.length - 1));
        return Math.max(0L, Math.round(bet * TOWER_MULT[safe]));
    }

    static final double[] WHEEL_MULT = {0, 1, 0, 1.5, 2, 0, 3, 1, 5, 0, 2, 10};

    static int spinWheel(Random rng) {
        return rng.nextInt(WHEEL_MULT.length);
    }

    static long wheelPayout(long bet, int segment) {
        if (segment < 0 || segment >= WHEEL_MULT.length) return 0;
        return Math.max(0L, Math.round(bet * WHEEL_MULT[segment]));
    }

    static int card(Random rng) {
        return 1 + rng.nextInt(13);
    }

    static String cardLabel(int rank) {
        if (rank == 1) return "A";
        if (rank == 11) return "J";
        if (rank == 12) return "Q";
        if (rank == 13) return "K";
        return String.valueOf(rank);
    }

    static long royalPayout(long bet, int first, int second, int choice) {
        if (second == first) return bet;
        boolean correct = choice == 0 ? second > first : second < first;
        return correct ? bet * 2L : 0L;
    }
}
