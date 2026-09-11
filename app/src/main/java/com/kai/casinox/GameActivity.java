package com.kai.casinox;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.content.Context;
import java.util.*;

public class GameActivity extends Activity {
    GameView view;
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(4,7,16));
        getWindow().setNavigationBarColor(Color.rgb(4,7,16));
        int game=Math.max(0,Math.min(21,getIntent().getIntExtra("game",0)));
        view=new GameView(this,game);
        setContentView(view);
    }
    @Override public void onBackPressed(){
        if(view!=null)view.abandonActiveRound();
        super.onBackPressed();
    }

    static class Hit { final RectF r; final int id; Hit(RectF r,int id){this.r=r;this.id=id;} }

    static class GameView extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Random rng=new Random();
        final AppState st;
        final ArrayList<Hit> hits=new ArrayList<>();
        final int game;
        int bet=1000;
        String msg="SELECT YOUR PLAY";
        boolean winFx=false; long fxAt=0;

        final int BG=Color.rgb(4,7,16), PANEL=Color.rgb(14,20,38), PANEL2=Color.rgb(21,30,54), PANEL3=Color.rgb(28,40,69);
        final int LINE=Color.rgb(48,63,96), WHITE=Color.rgb(247,249,255), MUTED=Color.rgb(145,158,184);
        final int GOLD=Color.rgb(247,199,77), GOLD2=Color.rgb(255,227,145), BLUE=Color.rgb(47,111,237), CYAN=Color.rgb(71,210,255);
        final int RED=Color.rgb(238,72,103), GREEN=Color.rgb(50,211,153);

        final String[] names={"Dragon Fortune","Golden Phoenix","Lucky Panda","Royal Tiger","Zeus Thunder","Mystic Temple","Ocean Treasure","Pirate Gold","Neon Fruits","Fortune 888","European Roulette","Blackjack Royale","Baccarat Elite","Sic Bo Arena","KAI X Crash","2D Lucky Draw","3D Lucky Draw","4D Lucky Draw","Neon Dice","Coin Tower","Lucky Wheel","Royal Cards"};

        GameEngine.SlotResult slotResult;
        GameEngine.RouletteBet rouletteBet=GameEngine.RouletteBet.RED; int rouletteNumber=-1;
        GameEngine.BlackjackRound blackjack; long blackjackWager=0; boolean blackjackSettled=true;
        GameEngine.BaccaratBet baccaratBet=GameEngine.BaccaratBet.PLAYER; GameEngine.BaccaratResult baccarat;
        GameEngine.SicBoBet sicBet=GameEngine.SicBoBet.BIG; GameEngine.SicBoResult sic;
        boolean crashRunning=false; long crashStart=0,crashWager=0; double crashPoint=0,crashMultiplier=1.0;
        int genericRoll=-1;

        GameView(Context c,int game){super(c);this.game=game;st=new AppState(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        float d(float v){return v*getResources().getDisplayMetrics().density;}
        int alpha(int c,int a){return Color.argb(a,Color.red(c),Color.green(c),Color.blue(c));}
        void txt(Canvas c,String s,float x,float y,float z,int col,boolean b){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(col);p.setTextSize(d(z));p.setTypeface(Typeface.create("sans-serif",b?Typeface.BOLD:Typeface.NORMAL));c.drawText(s,x,y,p);}
        void ctr(Canvas c,String s,float x,float y,float z,int col,boolean b){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(col);p.setTextSize(d(z));p.setTypeface(Typeface.create("sans-serif",b?Typeface.BOLD:Typeface.NORMAL));c.drawText(s,x-p.measureText(s)/2f,y,p);}
        void rr(Canvas c,float l,float t,float r,float b,float rad,int col){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(col);c.drawRoundRect(l,t,r,b,d(rad),d(rad),p);}
        void sr(Canvas c,float l,float t,float r,float b,float rad,int col,float w){p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d(w));p.setColor(col);c.drawRoundRect(l,t,r,b,d(rad),d(rad),p);p.setStyle(Paint.Style.FILL);}
        void hit(float l,float t,float r,float b,int id){hits.add(new Hit(new RectF(l,t,r,b),id));}
        String money(long v){return String.format(Locale.US,"%,d",v);}

        @Override protected void onDraw(Canvas c){
            hits.clear();c.drawColor(BG);
            p.setShader(new RadialGradient(getWidth()*.75f,d(40),d(320),alpha(BLUE,62),Color.TRANSPARENT,Shader.TileMode.CLAMP));c.drawCircle(getWidth()*.75f,d(40),d(320),p);p.setShader(null);
            header(c); stage(c);
            if(game<10)slot(c); else if(game==10)roulette(c); else if(game==11)blackjack(c); else if(game==12)baccarat(c); else if(game==13)sicbo(c); else if(game==14)crash(c); else generic(c);
            if(game!=11 && game!=14)betBar(c);
            if(winFx && System.currentTimeMillis()-fxAt<900){winFx(c);postInvalidateDelayed(16);} else winFx=false;
        }

        void header(Canvas c){
            txt(c,"‹ BACK",d(12),d(36),8.5f,CYAN,true);hit(0,0,d(76),d(58),1);
            txt(c,names[game],d(82),d(31),11.5f,WHITE,true);txt(c,"REAL PLAY ENGINE • VIRTUAL CREDITS",d(82),d(47),5.8f,MUTED,true);
            float l=getWidth()-d(126);rr(c,l,d(14),getWidth()-d(10),d(52),13,alpha(PANEL2,245));ctr(c,"◆ "+money(st.coins),(l+getWidth()-d(10))/2,d(39),8.2f,GOLD2,true);
        }
        void stage(Canvas c){
            rr(c,d(10),d(72),getWidth()-d(10),getHeight()-d(139),24,alpha(PANEL,248));sr(c,d(10),d(72),getWidth()-d(10),getHeight()-d(139),24,alpha(LINE,150),.8f);
            rr(c,d(23),d(86),d(148),d(111),11,alpha(GOLD,22));txt(c,typeLabel(),d(35),d(103),6.2f,GOLD2,true);
        }
        String typeLabel(){if(game<10)return"5×3 SLOT • 5 LINES";if(game==10)return"EUROPEAN ROULETTE";if(game==11)return"BLACKJACK • DEALER 17";if(game==12)return"BACCARAT";if(game==13)return"SIC BO";if(game==14)return"CRASH";return"ARCADE DRAW";}

        void slot(Canvas c){
            float left=d(24),top=d(137),gap=d(5),rw=(getWidth()-d(48)-gap*4)/5f;
            String[][] grid=slotResult!=null?slotResult.grid:null;String[] seed={"A","K","Q","J","10","7","W","S"};
            for(int col=0;col<5;col++)for(int row=0;row<3;row++){
                float x=left+col*(rw+gap),y=top+row*d(70);rr(c,x,y,x+rw,y+d(62),11,PANEL2);sr(c,x,y,x+rw,y+d(62),11,alpha(LINE,135),.6f);
                String s=grid==null?seed[(row*5+col+game)%seed.length]:grid[row][col];int colr="W".equals(s)?GOLD2:"S".equals(s)?CYAN:WHITE;ctr(c,s,x+rw/2,y+d(40),16,colr,true);
            }
            ctr(c,"5 PAYLINES • WILD SUBSTITUTES • 3+ SCATTER PAYS",getWidth()/2f,d(374),6.2f,MUTED,true);message(c,d(414));
        }

        void roulette(Canvas c){
            float cx=getWidth()/2f,cy=d(226);
            for(int i=0;i<18;i++){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(d(14));p.setColor(i%2==0?RED:Color.rgb(31,33,42));c.drawArc(cx-d(76),cy-d(76),cx+d(76),cy+d(76),i*20,16,false,p);}p.setStyle(Paint.Style.FILL);p.setColor(rouletteNumber==0?GREEN:PANEL3);c.drawCircle(cx,cy,d(48),p);
            ctr(c,rouletteNumber<0?"0–36":String.valueOf(rouletteNumber),cx,cy+d(8),20,rouletteNumber<0?WHITE:rouletteNumber==0?WHITE:GameEngine.rouletteRed(rouletteNumber)?RED:WHITE,true);
            String[] labels={"RED","BLACK","ODD","EVEN","1–18","19–36"};GameEngine.RouletteBet[] vals=GameEngine.RouletteBet.values();
            float w=(getWidth()-d(48))/3f;for(int i=0;i<6;i++){float x=d(16)+(i%3)*(w+d(8)),y=d(325)+(i/3)*d(43);boolean sel=rouletteBet==vals[i];rr(c,x,y,x+w,y+d(34),10,sel?alpha(BLUE,235):PANEL2);ctr(c,labels[i],x+w/2,y+d(22),6.5f,sel?WHITE:MUTED,true);hit(x,y,x+w,y+d(36),100+i);}message(c,d(425));
        }

        void blackjack(Canvas c){
            GameEngine.BlackjackRound r=blackjack;
            txt(c,"DEALER",d(25),d(137),6.5f,MUTED,true);drawHand(c,r==null?null:r.dealer,d(24),d(150),r!=null&&!r.over);
            int dv=r==null?0:GameEngine.BlackjackRound.value(r.dealer);txt(c,r==null?"":(r.over?"TOTAL "+dv:"SHOWING"),d(25),d(244),6.4f,MUTED,true);
            txt(c,"PLAYER",d(25),d(285),6.5f,MUTED,true);drawHand(c,r==null?null:r.player,d(24),d(298),false);int pv=r==null?0:GameEngine.BlackjackRound.value(r.player);txt(c,r==null?"":("TOTAL "+pv),d(25),d(393),6.4f,CYAN,true);
            if(r==null||r.over){button(c,d(20),d(430),getWidth()-d(20),d(474),"DEAL • "+money(bet),6200,BLUE);}else{
                float w=(getWidth()-d(56))/3f;button(c,d(16),d(430),d(16)+w,d(474),"HIT",7000,BLUE);button(c,d(24)+w,d(430),d(24)+2*w,d(474),"STAND",7001,PANEL3);button(c,d(32)+2*w,d(430),getWidth()-d(16),d(474),"DOUBLE",7002,alpha(GOLD,180));
            }
            message(c,d(511));betAdjust(c,d(535));
        }
        void drawHand(Canvas c,List<GameEngine.Card> hand,float x,float y,boolean hideSecond){
            if(hand==null){for(int i=0;i<2;i++){rr(c,x+i*d(68),y,x+d(55)+i*d(68),y+d(80),9,PANEL2);sr(c,x+i*d(68),y,x+d(55)+i*d(68),y+d(80),9,LINE,.7f);}return;}
            for(int i=0;i<hand.size();i++){float l=x+i*d(59);rr(c,l,y,l+d(49),y+d(80),9,Color.rgb(240,243,249));String s=(hideSecond&&i==1)?"?":hand.get(i).label();int col=(s.contains("♥")||s.contains("♦"))?RED:Color.BLACK;ctr(c,s,l+d(24.5f),y+d(44),11,col,true);}
        }

        void baccarat(Canvas c){
            txt(c,"PLAYER",d(31),d(148),7,CYAN,true);txt(c,"BANKER",getWidth()-d(89),d(148),7,GOLD2,true);
            if(baccarat==null){ctr(c,"—",d(92),d(226),38,MUTED,true);ctr(c,"—",getWidth()-d(92),d(226),38,MUTED,true);}else{ctr(c,handDigits(baccarat.player),d(92),d(216),16,WHITE,true);ctr(c,String.valueOf(baccarat.playerTotal),d(92),d(258),28,CYAN,true);ctr(c,handDigits(baccarat.banker),getWidth()-d(92),d(216),16,WHITE,true);ctr(c,String.valueOf(baccarat.bankerTotal),getWidth()-d(92),d(258),28,GOLD2,true);}
            String[] ls={"PLAYER","BANKER","TIE"};GameEngine.BaccaratBet[] vs=GameEngine.BaccaratBet.values();float w=(getWidth()-d(48))/3f;for(int i=0;i<3;i++){float x=d(16)+i*(w+d(8));boolean sel=baccaratBet==vs[i];rr(c,x,d(320),x+w,d(356),10,sel?BLUE:PANEL2);ctr(c,ls[i],x+w/2,d(343),6.5f,sel?WHITE:MUTED,true);hit(x,d(318),x+w,d(360),200+i);}message(c,d(405));
        }
        String handDigits(List<Integer> h){StringBuilder b=new StringBuilder();for(int i=0;i<h.size();i++){if(i>0)b.append("  ");b.append(h.get(i)==0?"10":h.get(i));}return b.toString();}

        void sicbo(Canvas c){
            float cx=getWidth()/2f;int[] dice=sic==null?new int[]{0,0,0}:new int[]{sic.a,sic.b,sic.c};for(int i=0;i<3;i++){float l=cx-d(112)+i*d(78);rr(c,l,d(165),l+d(58),d(223),13,WHITE);ctr(c,dice[i]==0?"•":String.valueOf(dice[i]),l+d(29),d(203),22,Color.BLACK,true);}ctr(c,sic==null?"TOTAL —":"TOTAL "+sic.total,cx,d(270),18,sic==null?MUTED:GOLD2,true);
            String[] ls={"BIG 11–17","SMALL 4–10","ODD","EVEN"};GameEngine.SicBoBet[] vs=GameEngine.SicBoBet.values();float w=(getWidth()-d(40))/2f;for(int i=0;i<4;i++){float x=d(12)+(i%2)*(w+d(8)),y=d(306)+(i/2)*d(44);boolean sel=sicBet==vs[i];rr(c,x,y,x+w,y+d(35),10,sel?BLUE:PANEL2);ctr(c,ls[i],x+w/2,y+d(23),6.4f,sel?WHITE:MUTED,true);hit(x,y,x+w,y+d(37),300+i);}message(c,d(419));
        }

        void crash(Canvas c){
            long now=System.currentTimeMillis();if(crashRunning){crashMultiplier=1.0+(now-crashStart)/2800.0;if(crashMultiplier>=crashPoint){crashMultiplier=crashPoint;crashRunning=false;st.reward(0,game,false);msg="CRASHED @ "+fmt(crashPoint)+"x";}else postInvalidateDelayed(32);}
            float cx=getWidth()/2f;ctr(c,fmt(crashMultiplier)+"x",cx,d(255),42,crashRunning?CYAN:WHITE,true);txt(c,"CRASH POINT",d(24),d(314),6,MUTED,true);txt(c,crashRunning?"HIDDEN UNTIL ROUND ENDS":crashPoint>0?fmt(crashPoint)+"x":"—",d(24),d(335),9,GOLD2,true);
            if(crashRunning)button(c,d(20),d(386),getWidth()-d(20),d(434),"CASH OUT • "+fmt(crashMultiplier)+"x",7100,GREEN);else button(c,d(20),d(386),getWidth()-d(20),d(434),"START ROUND • "+money(bet),6200,BLUE);message(c,d(480));betAdjust(c,d(514));
        }
        String fmt(double v){return String.format(Locale.US,"%.2f",v);}

        void generic(Canvas c){
            ctr(c,genericRoll<0?"?":String.valueOf(genericRoll),getWidth()/2f,d(260),64,genericRoll<0?MUTED:GOLD2,true);ctr(c,"Each play resolves a complete virtual round",getWidth()/2f,d(330),6.5f,MUTED,true);message(c,d(414));
        }

        void message(Canvas c,float y){int col=msg.startsWith("WIN")||msg.startsWith("BLACKJACK")||msg.startsWith("CASHED")?GOLD2:msg.startsWith("INSUFFICIENT")||msg.startsWith("CRASHED")||msg.startsWith("BUST")?RED:CYAN;rr(c,d(24),y-d(23),getWidth()-d(24),y+d(10),12,alpha(col,18));ctr(c,msg,getWidth()/2f,y,8.1f,col,true);}
        void betBar(Canvas c){float top=getHeight()-d(121);betAdjust(c,top);button(c,d(165),top+d(10),getWidth()-d(20),top+d(53),actionLabel(),6200,BLUE);}
        void betAdjust(Canvas c,float top){txt(c,"BET",d(20),top+d(18),5.8f,MUTED,true);rr(c,d(20),top+d(24),d(151),top+d(51),10,PANEL2);ctr(c,"−",d(37),top+d(43),13,CYAN,true);ctr(c,money(bet),d(86),top+d(43),7.8f,WHITE,true);ctr(c,"+",d(135),top+d(43),13,CYAN,true);hit(d(15),top+d(17),d(59),top+d(57),6100);hit(d(113),top+d(17),d(159),top+d(57),6101);}
        String actionLabel(){if(game<10)return"SPIN";if(game==10)return"SPIN ROULETTE";if(game==12)return"DEAL BACCARAT";if(game==13)return"ROLL DICE";return"PLAY ROUND";}
        void button(Canvas c,float l,float t,float r,float b,String label,int id,int col){rr(c,l,t,r,b,13,col);ctr(c,label,(l+r)/2,t+(b-t)*.64f,7.5f,WHITE,true);hit(l,t,r,b,id);}

        void play(){
            if(game<10){if(!charge(bet))return;slotResult=GameEngine.spin(rng,bet);settle(slotResult.payout,true,slotResult.payout>0?"WIN +"+money(slotResult.payout):"NO LINE WIN");}
            else if(game==10){if(!charge(bet))return;rouletteNumber=rng.nextInt(37);boolean win=GameEngine.rouletteWin(rouletteNumber,rouletteBet);settle(win?bet*2L:0,false,win?"WIN • "+rouletteNumber:"RESULT • "+rouletteNumber);}
            else if(game==11){dealBlackjack();}
            else if(game==12){if(!charge(bet))return;baccarat=new GameEngine.BaccaratResult(rng);long pay=(long)(bet*baccarat.returnMultiplier(baccaratBet));settle(pay,false,pay>0?"WIN • "+baccarat.winner():"WINNER • "+baccarat.winner());}
            else if(game==13){if(!charge(bet))return;sic=GameEngine.sicBo(rng);boolean win=sic.wins(sicBet);settle(win?bet*2L:0,false,win?"WIN • TOTAL "+sic.total:(sic.triple?"TRIPLE • HOUSE":"TOTAL "+sic.total));}
            else if(game==14){startCrash();}
            else {if(!charge(bet))return;genericRoll=rng.nextInt(game>=15&&game<=17?(game==15?100:game==16?1000:10000):100);int roll=rng.nextInt(100);long pay=roll<8?bet*5L:roll<43?bet*2L:0;settle(pay,false,pay>0?"WIN +"+money(pay):"ROUND COMPLETE");}
            invalidate();
        }
        boolean charge(long amount){if(!st.bet(amount)){msg="INSUFFICIENT KAI COINS";invalidate();return false;}return true;}
        void settle(long payout,boolean slot,String text){st.reward(payout,game,slot);msg=text;if(payout>bet){winFx=true;fxAt=System.currentTimeMillis();}}

        void dealBlackjack(){if(blackjack!=null&&!blackjack.over)return;if(!charge(bet))return;blackjackWager=bet;blackjackSettled=false;blackjack=new GameEngine.BlackjackRound(rng);msg=blackjack.outcome;if(blackjack.over)settleBlackjack();}
        void settleBlackjack(){if(blackjack==null||blackjackSettled||!blackjack.over)return;blackjackSettled=true;long pay=(long)Math.round(blackjackWager*blackjack.returnMultiplier);st.reward(pay,game,false);msg=blackjack.outcome;if(pay>blackjackWager){winFx=true;fxAt=System.currentTimeMillis();}}
        void blackjackHit(){if(blackjack==null||blackjack.over)return;blackjack.hit();msg=blackjack.outcome;if(blackjack.over)settleBlackjack();}
        void blackjackStand(){if(blackjack==null||blackjack.over)return;blackjack.stand();settleBlackjack();}
        void blackjackDouble(){if(blackjack==null||blackjack.over||blackjack.player.size()!=2)return;long add=blackjackWager;if(!charge(add))return;blackjackWager+=add;blackjack.hit();if(!blackjack.over)blackjack.stand();settleBlackjack();}

        void startCrash(){if(crashRunning)return;if(!charge(bet))return;crashWager=bet;crashPoint=1.05+rng.nextDouble()*5.95;crashMultiplier=1.0;crashStart=System.currentTimeMillis();crashRunning=true;msg="ROUND LIVE • CASH OUT ANYTIME";postInvalidateDelayed(16);}
        void cashCrash(){if(!crashRunning)return;long pay=(long)(crashWager*crashMultiplier);crashRunning=false;st.reward(pay,game,false);msg="CASHED @ "+fmt(crashMultiplier)+"x • +"+money(pay);if(pay>crashWager){winFx=true;fxAt=System.currentTimeMillis();}invalidate();}
        void abandonActiveRound(){if(crashRunning){crashRunning=false;st.reward(0,game,false);}if(blackjack!=null&&!blackjack.over&&!blackjackSettled){blackjackSettled=true;st.reward(0,game,false);}}

        void winFx(Canvas c){long dt=System.currentTimeMillis()-fxAt;float q=Math.min(1f,dt/600f);for(int i=0;i<16;i++){double a=i*.7+dt*.004;float r=d(24+i*4)*q,x=getWidth()/2f+(float)Math.cos(a)*r,y=getHeight()/2f+(float)Math.sin(a)*r;p.setColor(i%2==0?GOLD:CYAN);c.drawCircle(x,y,d(3+i%3),p);} }

        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX(),y=e.getY();for(Hit h:new ArrayList<>(hits))if(h.r.contains(x,y)){handle(h.id);return true;}return true;}
        void handle(int id){
            if(id==1){abandonActiveRound();((Activity)getContext()).finish();return;}
            if(id==6100){if(!roundLocked())bet=Math.max(100,bet/2);}
            else if(id==6101){if(!roundLocked())bet=Math.min(100000,bet*2);}
            else if(id==6200)play();
            else if(id>=100&&id<106)rouletteBet=GameEngine.RouletteBet.values()[id-100];
            else if(id>=200&&id<203)baccaratBet=GameEngine.BaccaratBet.values()[id-200];
            else if(id>=300&&id<304)sicBet=GameEngine.SicBoBet.values()[id-300];
            else if(id==7000)blackjackHit();else if(id==7001)blackjackStand();else if(id==7002)blackjackDouble();else if(id==7100)cashCrash();invalidate();
        }
        boolean roundLocked(){return crashRunning||(blackjack!=null&&!blackjack.over);}
    }
}
