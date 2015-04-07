package com.alkaid.winerapp;

import android.util.Log;

/**
 * Created by Alkaid on 2015/4/6.
 */
public class S2cLoginPacket extends S2cPacket {
    private String initData;
    private int verification;
    private int motoNums;
    public S2cLoginPacket(byte[] data)throws Exception{
        super(data);
        this.initData=new String(data);
        if(Constants.D) Log.d("Alkaid", "S2cLoginPacket str=" + initData);
        String motoNumStr=initData.substring(initData.length()-2,initData.length());
        String verificationStr=initData.substring(0,initData.length()-2);
        motoNums=Integer.parseInt(motoNumStr);
        verification=Integer.parseInt(verificationStr);
    }

    public String getInitData() {
        return initData;
    }

    public int getVerification() {
        return verification;
    }

    public int getMotoNums() {
        return motoNums;
    }
}
