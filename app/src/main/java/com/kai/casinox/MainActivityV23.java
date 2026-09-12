package com.kai.casinox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class MainActivityV23 extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(android.graphics.Color.rgb(4,7,16));
        getWindow().setNavigationBarColor(android.graphics.Color.rgb(4,7,16));
        setContentView(new LauncherView(this));
    }

    static class LauncherView extends MainActivity.CasinoView {
        LauncherView(Context c){super(c);}
        @Override void handle(int id){
            if(id>=2000 && id<2100){
                Intent intent=new Intent(getContext(),GameActivityV24.class);
                intent.putExtra("game",id-2000);
                getContext().startActivity(intent);
                return;
            }
            super.handle(id);
        }
    }
}
