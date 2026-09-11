package com.kai.casinox;

import java.util.*;

public final class GameEngine {
    private GameEngine() {}

    public static final String[] SLOT_SYMBOLS = {"A","K","Q","J","10","7","W","S"};

    public static class SlotResult {
        public final String[][] grid;
        public final long payout;
        public final int winningLines;
        SlotResult(String[][] grid, long payout, int winningLines) {
            this.grid = grid; this.payout = payout; this.winningLines = winningLines;
        }
    }

    public static SlotResult spin(Random rng, long bet) {
        String[][] grid = new String[3][5];
        for (int r=0;r<3;r++) for (int c=0;c<5;c++) {
            int roll = rng.nextInt(100);
            String s;
            if (roll < 5) s = "W";
            else if (roll < 10) s = "S";
            else s = SLOT_SYMBOLS[rng.nextInt(6)];
            grid[r][c] = s;
        }
        int[][] lines = {{0,0,0,0,0},{1,1,1,1,1},{2,2,2,2,2},{0,1,2,1,0},{2,1,0,1,2}};
        long total = 0; int wins = 0;
        for (int[] line: lines) {
            String target = null; int count = 0;
            for (int c=0;c<5;c++) {
                String s = grid[line[c]][c];
                if (target == null && !"W".equals(s)) target = s;
                if (target == null || target.equals(s) || "W".equals(s)) count++;
                else break;
            }
            if (count >= 3) {
                wins++;
                long lineBet = Math.max(1, bet / lines.length);
                long mult = count == 3 ? 2 : count == 4 ? 5 : 12;
                if ("7".equals(target)) mult *= 2;
                total += lineBet * mult;
            }
        }
        int scatters = 0;
        for (String[] row: grid) for (String s: row) if ("S".equals(s)) scatters++;
        if (scatters >= 3) total += bet * (scatters == 3 ? 2 : scatters == 4 ? 5 : 10);
        return new SlotResult(grid, total, wins);
    }

    public enum RouletteBet { RED, BLACK, ODD, EVEN, LOW, HIGH }
    private static final Set<Integer> REDS = new HashSet<>(Arrays.asList(1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36));
    public static boolean rouletteWin(int n, RouletteBet bet) {
        if (n == 0) return false;
        switch (bet) {
            case RED: return REDS.contains(n);
            case BLACK: return !REDS.contains(n);
            case ODD: return (n & 1) == 1;
            case EVEN: return (n & 1) == 0;
            case LOW: return n <= 18;
            case HIGH: return n >= 19;
            default: return false;
        }
    }
    public static boolean rouletteRed(int n) { return REDS.contains(n); }

    public static class Card {
        public final int rank; public final int suit;
        Card(int rank, int suit){this.rank=rank;this.suit=suit;}
        public String label(){
            String[] rs={"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
            String[] ss={"♠","♥","♦","♣"};
            return rs[rank-1]+ss[suit];
        }
    }
    public static class BlackjackRound {
        private final ArrayList<Card> deck = new ArrayList<>();
        public final ArrayList<Card> player = new ArrayList<>();
        public final ArrayList<Card> dealer = new ArrayList<>();
        public boolean over = false;
        public String outcome = "YOUR MOVE";
        public double returnMultiplier = 0;
        BlackjackRound(Random rng){
            for(int s=0;s<4;s++) for(int r=1;r<=13;r++) deck.add(new Card(r,s));
            Collections.shuffle(deck,rng);
            player.add(draw()); dealer.add(draw()); player.add(draw()); dealer.add(draw());
            if (value(player)==21) finishDealer();
        }
        private Card draw(){return deck.remove(deck.size()-1);}
        public void hit(){
            if(over)return; player.add(draw());
            int v=value(player); if(v>21){over=true;outcome="BUST • DEALER WINS";returnMultiplier=0;} else if(v==21) stand();
        }
        public void stand(){ if(over)return; finishDealer(); }
        private void finishDealer(){
            while(value(dealer)<17) dealer.add(draw());
            int pv=value(player), dv=value(dealer);
            over=true;
            boolean natural = player.size()==2 && pv==21;
            boolean dealerNatural = dealer.size()==2 && dv==21;
            if(pv>21){outcome="BUST • DEALER WINS";returnMultiplier=0;}
            else if(natural && !dealerNatural){outcome="BLACKJACK";returnMultiplier=2.5;}
            else if(dv>21 || pv>dv){outcome="PLAYER WINS";returnMultiplier=2.0;}
            else if(pv==dv){outcome="PUSH";returnMultiplier=1.0;}
            else {outcome="DEALER WINS";returnMultiplier=0;}
        }
        public static int value(List<Card> hand){
            int total=0,aces=0; for(Card c:hand){int v=c.rank==1?11:Math.min(c.rank,10);total+=v;if(c.rank==1)aces++;}
            while(total>21&&aces-->0) total-=10; return total;
        }
    }

    public enum SicBoBet { BIG, SMALL, ODD, EVEN }
    public static class SicBoResult {
        public final int a,b,c,total; public final boolean triple;
        SicBoResult(int a,int b,int c){this.a=a;this.b=b;this.c=c;this.total=a+b+c;this.triple=a==b&&b==c;}
        public boolean wins(SicBoBet bet){
            if(triple)return false;
            switch(bet){case BIG:return total>=11;case SMALL:return total<=10;case ODD:return total%2==1;case EVEN:return total%2==0;default:return false;}
        }
    }
    public static SicBoResult sicBo(Random rng){return new SicBoResult(1+rng.nextInt(6),1+rng.nextInt(6),1+rng.nextInt(6));}

    public enum BaccaratBet { PLAYER, BANKER, TIE }
    public static class BaccaratResult {
        public final ArrayList<Integer> player = new ArrayList<>();
        public final ArrayList<Integer> banker = new ArrayList<>();
        public final int playerTotal, bankerTotal;
        BaccaratResult(Random rng){
            ArrayList<Integer> shoe=new ArrayList<>();
            for(int d=0;d<6;d++) for(int i=0;i<52;i++){int rank=i%13+1;shoe.add(rank>=10?0:rank);}
            Collections.shuffle(shoe,rng);
            player.add(pop(shoe)); banker.add(pop(shoe)); player.add(pop(shoe)); banker.add(pop(shoe));
            int p=total(player), b=total(banker); Integer third=null;
            if(p<8&&b<8){
                if(p<=5){third=pop(shoe);player.add(third);}
                b=total(banker);
                if(third==null){if(b<=5)banker.add(pop(shoe));}
                else if(b<=2 || (b==3&&third!=8) || (b==4&&third>=2&&third<=7) || (b==5&&third>=4&&third<=7) || (b==6&&third>=6&&third<=7)) banker.add(pop(shoe));
            }
            playerTotal=total(player);bankerTotal=total(banker);
        }
        private static int pop(ArrayList<Integer> s){return s.remove(s.size()-1);}
        private static int total(List<Integer> h){int n=0;for(int v:h)n+=v;return n%10;}
        public BaccaratBet winner(){return playerTotal>bankerTotal?BaccaratBet.PLAYER:bankerTotal>playerTotal?BaccaratBet.BANKER:BaccaratBet.TIE;}
        public double returnMultiplier(BaccaratBet bet){
            BaccaratBet w=winner(); if(bet!=w)return 0; if(w==BaccaratBet.TIE)return 9.0; if(w==BaccaratBet.BANKER)return 1.95; return 2.0;
        }
    }
}
