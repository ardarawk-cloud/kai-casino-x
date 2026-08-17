package com.kai.casinox;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.*;
import android.content.*;
import java.util.*;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(7,17,31));
        getWindow().setNavigationBarColor(Color.rgb(7,17,31));
        setContentView(new CasinoView(this));
    }

    static class CasinoView extends View {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Random rng = new Random();
        final SharedPreferences prefs;
        int credits;
        int page = 0;
        int playerTotal = 0, dealerTotal = 0;
        boolean bjRound = false;
        int rouletteResult = -1;
        String rouletteMessage = "Choose RED or BLACK";
        String slotMessage = "Tap SPIN to play";
        String[] reels = {"7","KX","★"};
        final int bg = Color.rgb(7,17,31);
        final int panel = Color.rgb(13,30,51);
        final int panel2 = Color.rgb(17,39,65);
        final int blue = Color.rgb(42,116,255);
        final int cyan = Color.rgb(72,204,255);
        final int gold = Color.rgb(229,190,96);
        final int white = Color.rgb(239,246,255);
        final int muted = Color.rgb(145,164,187);

        CasinoView(Context c) {
            super(c);
            prefs = c.getSharedPreferences("kai_casino_x", Context.MODE_PRIVATE);
            credits = prefs.getInt("credits", 10000);
            p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        void save() { prefs.edit().putInt("credits", credits).apply(); }
        float W(){ return getWidth(); }
        float H(){ return getHeight(); }
        float dp(float v){ return v * getResources().getDisplayMetrics().density; }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            c.drawColor(bg);
            drawGlow(c, W()*0.78f, dp(85), dp(160), blue, 42);
            drawHeader(c);
            if(page==0) drawHome(c);
            if(page==1) drawBlackjack(c);
            if(page==2) drawRoulette(c);
            if(page==3) drawSlots(c);
        }

        void drawGlow(Canvas c,float x,float y,float r,int color,int alpha){
            p.setShader(new RadialGradient(x,y,r,withAlpha(color,alpha),Color.TRANSPARENT,Shader.TileMode.CLAMP));
            c.drawCircle(x,y,r,p); p.setShader(null);
        }
        int withAlpha(int color,int a){ return Color.argb(a,Color.red(color),Color.green(color),Color.blue(color)); }
        void text(Canvas c,String s,float x,float y,float size,int color,boolean bold){
            p.setShader(null); p.setColor(color); p.setTextSize(dp(size));
            p.setTypeface(Typeface.create("sans", bold?Typeface.BOLD:Typeface.NORMAL));
            c.drawText(s,x,y,p);
        }
        void center(Canvas c,String s,float x,float y,float size,int color,boolean bold){
            p.setTextSize(dp(size)); p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));
            p.setColor(color); c.drawText(s,x-p.measureText(s)/2f,y,p);
        }
        void round(Canvas c,float l,float t,float r,float b,float rad,int color){
            p.setColor(color); p.setStyle(Paint.Style.FILL); p.setShader(null); c.drawRoundRect(l,t,r,b,dp(rad),dp(rad),p);
        }
        void stroke(Canvas c,float l,float t,float r,float b,float rad,int color,float sw){
            p.setColor(color); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(sw)); c.drawRoundRect(l,t,r,b,dp(rad),dp(rad),p); p.setStyle(Paint.Style.FILL);
        }

        void drawHeader(Canvas c){
            float y=dp(38);
            drawLogo(c,dp(30),y,dp(22));
            text(c,"KAI CASINO X",dp(60),y+dp(7),18,white,true);
            text(c,"PRIVATE DEMO CLUB",dp(60),y+dp(24),8,muted,true);
            round(c,W()-dp(142),dp(22),W()-dp(18),dp(64),14,panel2);
            text(c,"DEMO CREDITS",W()-dp(130),dp(38),8,muted,true);
            text(c,String.format(Locale.US,"%,d",credits),W()-dp(130),dp(56),15,gold,true);
        }

        void drawLogo(Canvas c,float x,float y,float r){
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2.2f)); p.setColor(gold); c.drawCircle(x,y,r,p);
            p.setStrokeWidth(dp(5)); p.setColor(blue); c.drawArc(x-r*.68f,y-r*.68f,x+r*.68f,y+r*.68f,-60,235,false,p);
            p.setStyle(Paint.Style.FILL);
            Path k=new Path(); k.moveTo(x-r*.35f,y-r*.48f); k.lineTo(x-r*.13f,y-r*.48f); k.lineTo(x-r*.13f,y-r*.06f); k.lineTo(x+r*.22f,y-r*.48f); k.lineTo(x+r*.49f,y-r*.48f); k.lineTo(x+r*.08f,y); k.lineTo(x+r*.5f,y+r*.5f); k.lineTo(x+r*.2f,y+r*.5f); k.lineTo(x-r*.13f,y+r*.08f); k.lineTo(x-r*.13f,y+r*.5f); k.lineTo(x-r*.35f,y+r*.5f); k.close(); p.setColor(white); c.drawPath(k,p);
        }

        void drawHome(Canvas c){
            text(c,"PLAY SMART.",dp(22),dp(122),33,white,true);
            text(c,"PLAY FOR FUN.",dp(22),dp(158),33,cyan,true);
            text(c,"Premium casino-style games with virtual credits only.",dp(22),dp(184),11,muted,false);
            text(c,"No deposits • No cash-out • No real-money wagering",dp(22),dp(203),10,gold,true);

            gameCard(c,dp(18),dp(232),W()-dp(18),dp(342),"BLACKJACK","21 TABLE","Classic cards • dealer AI",1,"A♠");
            gameCard(c,dp(18),dp(355),W()-dp(18),dp(465),"ROULETTE","EUROPEAN 0–36","Red / Black demo table",2,"●");
            gameCard(c,dp(18),dp(478),W()-dp(18),dp(588),"KX SLOTS","3-REEL ORIGINAL","Fast virtual-credit spins",3,"7");

            round(c,dp(18),dp(610),W()-dp(18),dp(677),18,panel);
            text(c,"CONTROL CENTER",dp(34),dp(634),10,muted,true);
            text(c,"Session guardrail",dp(34),dp(657),15,white,true);
            text(c,"Reset demo wallet",W()-dp(164),dp(657),12,cyan,true);
            text(c,"18+ visual theme • Entertainment simulation only",dp(22),H()-dp(22),9,muted,false);
        }

        void gameCard(Canvas c,float l,float t,float r,float b,String title,String tag,String sub,int id,String mark){
            round(c,l,t,r,b,22,panel);
            stroke(c,l,t,r,b,22,withAlpha(blue,90),1);
            round(c,l+dp(14),t+dp(14),l+dp(82),b-dp(14),16,panel2);
            center(c,mark,l+dp(48),t+dp(66),31,id==2?Color.rgb(236,77,93):gold,true);
            text(c,tag,l+dp(98),t+dp(28),9,cyan,true);
            text(c,title,l+dp(98),t+dp(58),21,white,true);
            text(c,sub,l+dp(98),t+dp(81),10,muted,false);
            round(c,r-dp(54),t+dp(35),r-dp(17),t+dp(72),12,blue);
            center(c,"›",r-dp(35),t+dp(61),25,white,true);
        }

        void pageTitle(Canvas c,String eyebrow,String title,String sub){
            text(c,eyebrow,dp(20),dp(112),9,cyan,true);
            text(c,title,dp(20),dp(145),28,white,true);
            text(c,sub,dp(20),dp(168),10,muted,false);
        }
        void button(Canvas c,float l,float t,float r,float b,String s,boolean primary){
            round(c,l,t,r,b,15,primary?blue:panel2); stroke(c,l,t,r,b,15,primary?withAlpha(cyan,100):withAlpha(white,30),1);
            center(c,s,(l+r)/2f,t+(b-t)/2f+dp(5),12,primary?white:cyan,true);
        }
        void homeButton(Canvas c){ button(c,dp(20),H()-dp(74),W()-dp(20),H()-dp(24),"← BACK TO LOBBY",false); }

        void drawBlackjack(Canvas c){
            pageTitle(c,"TABLE 01","BLACKJACK","Demo bet: 100 credits • Dealer stands on 17");
            round(c,dp(20),dp(195),W()-dp(20),dp(450),24,panel);
            text(c,"DEALER",dp(38),dp(225),10,muted,true);
            center(c,bjRound?String.valueOf(dealerTotal):"—",W()/2,dp(290),52,white,true);
            text(c,"PLAYER",dp(38),dp(340),10,muted,true);
            center(c,bjRound?String.valueOf(playerTotal):"—",W()/2,dp(407),52,gold,true);
            if(!bjRound){ button(c,dp(20),dp(478),W()-dp(20),dp(535),"DEAL • 100",true); }
            else { button(c,dp(20),dp(478),W()/2-dp(6),dp(535),"HIT",true); button(c,W()/2+dp(6),dp(478),W()-dp(20),dp(535),"STAND",false); }
            text(c,"Blackjack pays 2× demo stake. Bust loses stake.",dp(22),dp(565),10,muted,false);
            homeButton(c);
        }

        void startBJ(){
            if(credits<100){ playerTotal=dealerTotal=0; bjRound=false; return; }
            credits-=100; playerTotal=card()+card(); dealerTotal=card()+card(); bjRound=true; save();
            if(playerTotal==21) finishBJ(true);
        }
        int card(){ return 2+rng.nextInt(10); }
        void hitBJ(){ if(!bjRound)return; playerTotal+=card(); if(playerTotal>21) finishBJ(false); }
        void standBJ(){ if(!bjRound)return; while(dealerTotal<17) dealerTotal+=card(); finishBJ(playerTotal<=21 && (dealerTotal>21 || playerTotal>=dealerTotal)); }
        void finishBJ(boolean win){ if(win) credits+=200; bjRound=false; save(); invalidate(); }

        void drawRoulette(Canvas c){
            pageTitle(c,"TABLE 02","EUROPEAN ROULETTE","Demo stake: 100 credits • Single-zero wheel");
            round(c,dp(20),dp(195),W()-dp(20),dp(420),24,panel);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(13)); p.setColor(Color.rgb(210,55,73)); c.drawCircle(W()/2,dp(292),dp(70),p);
            p.setStrokeWidth(dp(6)); p.setColor(Color.rgb(28,33,43)); c.drawCircle(W()/2,dp(292),dp(55),p); p.setStyle(Paint.Style.FILL);
            center(c,rouletteResult<0?"?":String.valueOf(rouletteResult),W()/2,dp(311),47,rouletteResult==0?gold:white,true);
            center(c,rouletteMessage,W()/2,dp(392),11,muted,true);
            button(c,dp(20),dp(450),W()/2-dp(6),dp(510),"BET RED • 100",true);
            button(c,W()/2+dp(6),dp(450),W()-dp(20),dp(510),"BET BLACK • 100",false);
            text(c,"Correct color returns 2× demo stake. Zero loses color bets.",dp(22),dp(544),10,muted,false);
            homeButton(c);
        }
        boolean isRed(int n){ int[] r={1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36}; for(int x:r)if(x==n)return true; return false; }
        void roulette(boolean red){
            if(credits<100){ rouletteMessage="Not enough demo credits"; invalidate(); return; }
            credits-=100; rouletteResult=rng.nextInt(37); boolean win=rouletteResult!=0 && (isRed(rouletteResult)==red);
            if(win)credits+=200; rouletteMessage=(rouletteResult==0?"ZERO":(isRed(rouletteResult)?"RED":"BLACK"))+(win?" • WIN +100":" • TRY AGAIN"); save(); invalidate();
        }

        void drawSlots(Canvas c){
            pageTitle(c,"GAME 03","KX SLOTS","Original 3-reel demo game • Spin: 100 credits");
            round(c,dp(20),dp(195),W()-dp(20),dp(410),24,panel);
            float gap=dp(10), l=dp(35), rw=(W()-dp(70)-gap*2)/3f;
            for(int i=0;i<3;i++){ float x=l+i*(rw+gap); round(c,x,dp(235),x+rw,dp(340),18,panel2); center(c,reels[i],x+rw/2,dp(306),34,i==0?gold:white,true); }
            center(c,slotMessage,W()/2,dp(383),11,muted,true);
            button(c,dp(20),dp(445),W()-dp(20),dp(510),"SPIN • 100",true);
            text(c,"3 matching = +1,000 • 2 matching = +200 demo credits",dp(22),dp(545),10,muted,false);
            homeButton(c);
        }
        void spin(){
            if(credits<100){slotMessage="Not enough demo credits";invalidate();return;}
            credits-=100; String[] s={"7","KX","★","BAR","X"}; for(int i=0;i<3;i++)reels[i]=s[rng.nextInt(s.length)];
            if(reels[0].equals(reels[1])&&reels[1].equals(reels[2])){ credits+=1000; slotMessage="JACKPOT • +900 NET"; }
            else if(reels[0].equals(reels[1])||reels[1].equals(reels[2])||reels[0].equals(reels[2])){ credits+=200; slotMessage="PAIR • +100 NET"; }
            else slotMessage="NO MATCH • TRY AGAIN"; save(); invalidate();
        }

        void resetWallet(){ credits=10000; save(); rouletteResult=-1; rouletteMessage="Choose RED or BLACK"; slotMessage="Tap SPIN to play"; invalidate(); }

        @Override public boolean onTouchEvent(android.view.MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP)return true; float x=e.getX(), y=e.getY();
            if(page==0){
                if(y>dp(232)&&y<dp(342))page=1;
                else if(y>dp(355)&&y<dp(465))page=2;
                else if(y>dp(478)&&y<dp(588))page=3;
                else if(y>dp(610)&&y<dp(685))resetWallet();
            } else if(y>H()-dp(90)){ page=0; }
            else if(page==1 && y>dp(465)&&y<dp(550)){ if(!bjRound)startBJ(); else if(x<W()/2)hitBJ(); else standBJ(); }
            else if(page==2 && y>dp(435)&&y<dp(525)){ roulette(x<W()/2); }
            else if(page==3 && y>dp(430)&&y<dp(525)){ spin(); }
            invalidate(); return true;
        }
    }
}
