package com.alkaid.winerapp;

/**
 * Created by alkaid on 2015/4/8.
 */
public class Packet {
    protected byte[] data;
    public Packet(byte[] data)throws Exception{
        this.data=data;
    }
}
