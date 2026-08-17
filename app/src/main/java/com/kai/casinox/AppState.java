package com.kai.casinox;

import android.content.*;

public class AppState {
    private final SharedPreferences p;
    public long coins, biggestWin;
    public int xp, gamesPlayed, spins, wins, uniqueMask, loginDay;
    public boolean music, sfx, vibration;
    public AppState(Context c){
        p=c.getSharedPreferences("kai_casino_x_v2",0);
        coins=p.getLong("coins",1_000_000L); xp=p.getInt("xp",0); gamesPlayed=p.getInt("gamesPlayed",0);
        biggestWin=p.getLong("biggestWin",0); spins=p.getInt("spins",0); wins=p.getInt("wins",0); uniqueMask=p.getInt("uniqueMask",0);
        loginDay=p.getInt("loginDay",1); music=p.getBoolean("music",true); sfx=p.getBoolean("sfx",true); vibration=p.getBoolean("vibration",true);
        save();
    }
    public void save(){p.edit().putLong("coins",coins).putInt("xp",xp).putInt("gamesPlayed",gamesPlayed).putLong("biggestWin",biggestWin).putInt("spins",spins).putInt("wins",wins).putInt("uniqueMask",uniqueMask).putInt("loginDay",loginDay).putBoolean("music",music).putBoolean("sfx",sfx).putBoolean("vibration",vibration).apply();}
    public boolean bet(long v){if(v<=0||coins<v)return false; coins-=v; save(); return true;}
    public void reward(long v,int gameId,boolean slot){coins+=v; gamesPlayed++; xp+=10+(int)Math.min(40,v/10000); if(slot)spins++; if(v>0)wins++; if(v>biggestWin)biggestWin=v; if(gameId>=0&&gameId<30)uniqueMask|=(1<<gameId); save();}
    public String tier(){if(xp<500)return "BRONZE";if(xp<1500)return "SILVER";if(xp<4000)return "GOLD";if(xp<8000)return "PLATINUM";if(xp<15000)return "DIAMOND";return "KAI ELITE";}
    public int level(){return 1+xp/500;}
    public int uniqueGames(){return Integer.bitCount(uniqueMask);}
    public boolean missionPlay10(){return gamesPlayed>=10;} public boolean missionSpin25(){return spins>=25;} public boolean missionThreeGames(){return uniqueGames()>=3;} public boolean missionWin5(){return wins>=5;}
    public long dailyReward(){long[] a={10000,15000,25000,35000,50000,75000,100000};return a[(loginDay-1)%7];}
    public void claimDaily(){coins+=dailyReward();xp+=50;loginDay=loginDay%7+1;save();}
}