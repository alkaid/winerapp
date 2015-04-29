/**
 * $RCSfile$
 * $Revision$
 * $Date$
 *
 * Copyright 2003-2007 Jive Software.
 *
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alkaid.winerapp;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Packet接收解析类
 * @author lincong
 *
 */
public class PacketReader {
    private static boolean logined=false;
    private Thread readerThread;
    private boolean done;
     private  BluetoothSocket socket;
     private PacketReadListener packetReadListener;


    public PacketReader(BluetoothSocket socket) {
        this.socket = socket;
    }

     public void setPacketReadListener(PacketReadListener packetReadListener) {
         this.packetReadListener = packetReadListener;
     }

    public static boolean isLogined() {
        //TODO Test
//        return logined;
        return true;
    }

    public static void setLogined(boolean logined) {
        PacketReader.logined = logined;
    }

    /**
     * Initializes the reader in order to be used. The reader is initialized during the
     * first connection and when reconnecting due to an abruptly disconnection.
     */
    protected void init() {
        done = false;
        setLogined(false);
        readerThread = new Thread() {
            public void run() {
                parsePackets(this);
            }
        };
        readerThread.setDaemon(true);
    }

    public void startup() {
        init();
        readerThread.start();
    }

    /**
     * Shuts the packet reader down.
     */
    public void shutdown() {
        // Notify connection listeners of the connection closing if done hasn't already been set.
        setLogined(false);
        done = true;
    }

    /**
     * Parse top-level packets in order to process them further.
     *
     * @param thread the thread that is being used by the reader to parse incoming packets.
     */
    private void parsePackets(Thread thread) {
        try {
            InputStream mmInStream = null;
            byte[] buffer = new byte[4096];
            int len = 0;
            mmInStream=socket.getInputStream();
            do {
                ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
                len = mmInStream.read(buffer);
                Log.d("", "reading1...len=" + len);
                if(len!=-1){
                    outSteam.write(buffer, 0, len);
                }
                byte[] data=outSteam.toByteArray();
                S2cPacket packet=null;
                if(!isLogined()){
                    packet=new S2cLoginPacket(data);
                    setLogined(true);
                }else{
                    packet=new S2cDefaultResponse(data);
                }
                if(null!=packet&&null!=packetReadListener){
                    packetReadListener.onPacketRead(packet);
                }
            } while (!done && thread == readerThread);
        }
        catch (Exception e) {
            if (!done) {
                done = true;
                if(null!=packetReadListener){
                    packetReadListener.onException(e);
                }
            }
        }
    }

     public interface PacketReadListener{
         public void onPacketRead(S2cPacket packet);
         public void onException(Exception e);
     }
    
}
