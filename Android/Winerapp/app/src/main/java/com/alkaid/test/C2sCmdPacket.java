package com.alkaid.test;

import android.util.Log;

import com.alkaid.winerapp.Util;

/**
 * Created by alkaid on 2015/4/8.
 */
public class C2sCmdPacket extends  C2sPacket {
    private int cmd;
    public C2sCmdPacket(byte[] data) throws Exception {
        super(data);
        this.cmd= Util.byteArrayToInt(data);
        Log.d("Alkaid", "C2sCmdPacket cmd=" + cmd);
    }

    public int getCmd() {
        return cmd;
    }
}
