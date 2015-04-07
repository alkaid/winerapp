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

 /**
 * Packet接收解析类
 * @author lincong
 *
 */
class PacketReader {
    public static boolean logined=false;
    private Thread readerThread;
    private boolean done;
     private  BluetoothSocket socket;
     private PacketReadListener packetReadListener;


    protected PacketReader(BluetoothSocket socket) {
        this.socket = socket;
        this.init();
    }

     public void setPacketReadListener(PacketReadListener packetReadListener) {
         this.packetReadListener = packetReadListener;
     }

     /**
     * Initializes the reader in order to be used. The reader is initialized during the
     * first connection and when reconnecting due to an abruptly disconnection.
     */
    protected void init() {
        done = false;
        logined=false;
        readerThread = new Thread() {
            public void run() {
                parsePackets(this);
            }
        };
        readerThread.setDaemon(true);
    }

    public void startup() {
        readerThread.start();
    }

    /**
     * Shuts the packet reader down.
     */
    public void shutdown() {
        // Notify connection listeners of the connection closing if done hasn't already been set.
        if (!done) {
            logined=false;
        }
        done = true;
    }

    /**
     * Parse top-level packets in order to process them further.
     *
     * @param thread the thread that is being used by the reader to parse incoming packets.
     */
    private void parsePackets(Thread thread) {
        try {
            do {
                M2sPacket packet=null;
                byte[] data=Util.readSocketData(socket);
                if(!logined){
                    packet=new M2sLoginPacket(data);
                    logined=true;
                }else{
                    packet=new M2sDefaultResponse(data);
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
         public void onPacketRead(M2sPacket packet);
         public void onException(Exception e);
     }
    
}
