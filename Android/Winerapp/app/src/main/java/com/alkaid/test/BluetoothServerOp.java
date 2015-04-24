package com.alkaid.test;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.alkaid.winerapp.Constants;
import com.alkaid.winerapp.Util;

import java.io.IOException;

/**
 * 封装蓝牙操作<br/>
 * 初始化：<br/>
 * 1.初始化时应传入message（要发�?�给服务端的数据�?<br/>
 * 2.初始化时应传入标识符isMatchBeforeConnect表明蓝牙连接的方式，是直接用socket连接，还是先主动发起配对再连接�??<br/>
 * 3.初始化时可以传入�?个目标设备mac或其它标识，也可以不传�??<br/>
 * 		a.若有目标设备，则该类搜索到该设备后自动连接该设备<br/>
 * 		b.若没有目标设�?,则在搜索设备结束后由该类的子类继�? onFoundDevicesButNotTarget()方法来进行后续操作，常见的用法是弹出提示让用户�?�择�?个设备进行连接，此时调用该类�? connect方法即可�?<br/>
 * 4.请注意connect()方法的返回�?? �? connect() 方法返回BT_FIRST_MATCH 代表蓝牙连接过程先经过了配对过程，则子类必须实现生命周期 onMatchedConnected()方法以完成连接完成后的后续操作�??<br/>
 * @author lincong
 *
 */
public class BluetoothServerOp {
	private String TAG="BluetoothServerOp";
	private Context ctx;

	private BluetoothAdapter mbtApapter;
    private BluetoothServerSocket msSocket;
	private BluetoothSocket mmSocket;
	private boolean isBluetoothEnable=false;

	public BluetoothServerOp(Context ctx){
		this.ctx=ctx;
        init();
	}

	public void init(){
		mbtApapter=BluetoothAdapter.getDefaultAdapter();
	     // If the adapter is null, then Bluetooth is not supported
	        if (mbtApapter == null) {
	            Log.e(TAG, "Bluetooth is not available");
	            return;
	        }
	        //打开蓝牙 �? �?始搜索设�?
	        if(!mbtApapter.isEnabled()){
	        	mbtApapter.enable();
	        }else{
	        	isBluetoothEnable=true;
	        }
        if(isBluetoothEnable){
            AcceptThread t=new AcceptThread();
            t.start();
        }
	}

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mbtApapter.listenUsingRfcommWithServiceRecord("Alkaid Test", Constants.MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
//                    mmServerSocket.close();
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    private void manageConnectedSocket(final BluetoothSocket socket) {
        PacketReader reader=new PacketReader(socket);
        com.alkaid.winerapp.PacketReader.logined=false;
        reader.setPacketReadListener(new PacketReader.PacketReadListener() {
            @Override
            public void onPacketRead(final C2sPacket packet) {
                if(packet instanceof C2sLoginPacket){
                    int encodeNo=Util.encode(((C2sLoginPacket) packet).getNum());
                    String data=encodeNo+"08";
                    try {
                        Util.writeSocketData(socket,data.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else if(packet instanceof C2sCmdPacket){
                    ((Activity)ctx).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ctx, ((C2sCmdPacket)packet).getCmd()+"",Toast.LENGTH_SHORT).show();
                        }
                    });
                    try {
                        Util.writeSocketData(socket,0xAA,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onException(Exception e) {
                e.printStackTrace();
            }
        });
        reader.startup();
    }
}
