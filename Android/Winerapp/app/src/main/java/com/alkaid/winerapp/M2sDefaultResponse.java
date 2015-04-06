package com.alkaid.winerapp;

import android.util.Log;

/**
 * Created by Alkaid on 2015/4/6.
 */
public class M2sDefaultResponse extends M2sPacket {
    public static final int RIGHT_RESPONSE=0xAA;
    private int response;
    public M2sDefaultResponse(byte[] data) throws Exception {
        super(data);
        response=Util.byteArrayToInt(data);
        if(Constants.D) Log.d("M2sDefaultResponse","response="+response);
        if(response!=RIGHT_RESPONSE){
            throw new Exception("Response isn't 0xAA,response is "+response);
        }
    }
}
