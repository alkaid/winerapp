package com.alkaid.winerapp;

/**
 * Created by Alkaid on 2015/4/6.
 */
public class M2sLoginPacket extends M2sPacket {
    private String initData;
    private int verification;
    private int motoNums;
    public M2sLoginPacket(byte[] data)throws Exception{
        super(data);
        this.initData=new String(data);
        String motoNumStr=initData.substring(initData.length()-2,initData.length());
        String verificationStr=initData.substring(0,initData.length()-2);
        motoNums=Integer.parseInt(motoNumStr);
        verification=Integer.parseInt(verificationStr);
    }
}
