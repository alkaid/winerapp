package com.alkaid.winner;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TabHost tabHost=(TabHost) findViewById(android.R.id.tabhost);
        TabSpec page1 = tabHost.newTabSpec("tab1")  
                .setIndicator(null,getResources().getDrawable(R.drawable.bar_light));  
        tabHost.addTab(page1);  
          
        TabSpec page2 = tabHost.newTabSpec("tab2")  
        		.setIndicator(null,getResources().getDrawable(R.drawable.bar_switch));
        tabHost.addTab(page2);  
          
        TabSpec page3 = tabHost.newTabSpec("tab3")  
        		.setIndicator(null,getResources().getDrawable(R.drawable.bar_moto));  
        tabHost.addTab(page3);  
        
        TabSpec page4 = tabHost.newTabSpec("tab4")  
        		.setIndicator(null,getResources().getDrawable(R.drawable.bar_turn));  
        tabHost.addTab(page4);  
        
        TabSpec page5 = tabHost.newTabSpec("tab5")  
        		.setIndicator(null,getResources().getDrawable(R.drawable.bar_tpd));  
        tabHost.addTab(page5);  
        
    }
}
