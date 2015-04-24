package com.alkaid.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/**
 * Created by alkaid on 2015/4/8.
 */
public class BtServerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new View(this));
        BluetoothServerOp bsop=new BluetoothServerOp(this);

    }
}
