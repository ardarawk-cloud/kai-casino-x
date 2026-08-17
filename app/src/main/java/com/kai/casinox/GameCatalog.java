package com.kai.casinox;

public class GameCatalog {
    public static final int SLOT=0, ROULETTE=1, BLACKJACK=2, BACCARAT=3, SICBO=4, CRASH=5, LOTTERY=6, ARCADE=7;
    public static final String[] NAME={
      "Dragon Fortune","Golden Phoenix","Lucky Panda","Royal Tiger","Zeus Thunder","Mystic Temple","Ocean Treasure","Pirate Gold","Neon Fruits","Fortune 888",
      "European Roulette","Royal Blackjack","Imperial Baccarat","KAI Sic Bo","KX Nova Crash","KAI 2D Draw","KAI 3D Draw","KAI 4D Draw","Neon Hi-Lo","Royal Wheel","Color Dice","Coin Duel"
    };
    public static final int[] TYPE={SLOT,SLOT,SLOT,SLOT,SLOT,SLOT,SLOT,SLOT,SLOT,SLOT,ROULETTE,BLACKJACK,BACCARAT,SICBO,CRASH,LOTTERY,LOTTERY,LOTTERY,ARCADE,ARCADE,ARCADE,ARCADE};
    public static final String[] TAG={"HOT","JACKPOT","HOT","NEW","JACKPOT","NEW","HOT","TRENDING","NEW","JACKPOT","TABLE","TABLE","TABLE","TABLE","CRASH","LOTTERY","LOTTERY","LOTTERY","ARCADE","ARCADE","ARCADE","ARCADE"};
    public static final String[] MARK={"龍","鳳","🐼","虎","⚡","◇","🐚","☠","🍒","888","●","A♠","B","⚂","🚀","2D","3D","4D","↑↓","◎","⚄","◐"};
    public static int count(){return NAME.length;}
    public static String category(int i){int t=TYPE[i];return t==SLOT?"SLOTS":t==ROULETTE||t==BLACKJACK||t==BACCARAT||t==SICBO?"TABLE":t==CRASH?"CRASH":t==LOTTERY?"LOTTERY":"ARCADE";}
}