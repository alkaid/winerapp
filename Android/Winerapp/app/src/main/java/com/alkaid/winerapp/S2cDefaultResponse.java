package com.alkaid.winerapp;

import android.util.Log;

/**
 * Created by Alkaid on 2015/4/6.
 */
public class S2cDefaultResponse extends S2cPacket {
    public static final int RIGHT_RESPONSE=0xAA;
    private int response;
    public S2cDefaultResponse(byte[] data) throws Exception {
        super(data);
        response=Util.byteArrayToInt(data);
        if(Constants.D) Log.d("S2cDefaultResponse","response="+response);
        if(response!=RIGHT_RESPONSE){
            throw new Exception("Response isn't 0xAA,response is "+response);
        }
    }
}
