package com.alkaid.test;

import android.util.Log;

import com.alkaid.winerapp.Util;

/**
 * Created by alkaid on 2015/4/8.
 */
public class C2sLoginPacket extends  C2sPacket {
    private int num;
    public C2sLoginPacket(byte[] data) throws Exception {
        super(data);
        String numStr=new String(data);
//        this.num= Util.byteArrayToInt(data);
        this.num=Integer.valueOf(numStr);
        Log.d("Alkaid", "C2sLoginPacket num=" + num);
    }

    public int getNum() {
        return num;
    }
}
