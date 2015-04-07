package com.alkaid.winerapp;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Util {
	private static String TAG="SmsBluetoothUtil";
	public static void writeSocketData(BluetoothSocket socket,byte[] data) throws IOException{
		OutputStream mmOutStream=null;
		try {
			mmOutStream = socket.getOutputStream();
    	} catch (IOException e) {
    		Log.e(TAG, "get outputStream error", e);
    		throw e;
    	}
		try {
			mmOutStream.write(data);
		} catch (IOException e) {
			Log.e(TAG, "write outputStream error", e);
			throw e;
		}
	}
    public static void writeSocketData(BluetoothSocket socket,int cmd) throws IOException{
        OutputStream mmOutStream=null;
        DataOutputStream dos=null;
        try {
            mmOutStream = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "get outputStream error", e);
            throw e;
        }
        try {
            dos=new DataOutputStream(mmOutStream);
            dos.write(cmd);
        } catch (IOException e) {
            Log.e(TAG, "write outputStream error", e);
            throw e;
        }
    }
	public static byte[] readSocketData(BluetoothSocket socket){
    	InputStream mmInStream = null;
    	try {
    		mmInStream = socket.getInputStream();
    	} catch (IOException e) {
    		Log.e(TAG, "get inputStream error", e);
    	}
        Log.d(TAG, "geted inputStream from socket!");
        byte[] data=null;
		try {
			data = readInputStream(mmInStream);
			Log.d(TAG, "geted data from socket! data="+data);
		} catch (IOException e) {
			Log.e(TAG, "read inputStream error", e);
		}
		return data;
    }
	public static byte[] readInputStream(InputStream inStream)
			throws IOException {
		Log.d(TAG, "read inputStream begin!");
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			Log.d(TAG, "reading1...len="+len);
			outSteam.write(buffer, 0, len);
			Log.d(TAG, "reading2...byte="+new String(outSteam.toByteArray()));
		}
		Log.d(TAG, "read inputStream end!");
		outSteam.close();
		inStream.close();
		Log.d(TAG, "close inputStream!");
		return outSteam.toByteArray();
	}
	public static void toast(Context context,String text){
		if(Constants.T){
			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
		}
	}

    public static int byteArrayToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    /**
     * 加密
     * @param num  100=<num<999
     * @return
     */
    public static int encode(int num){
        int ab=num/10;
        int c=0;
        c= num%2!=0? num%14 : num%36;
        int d=ab*c;
        int result=num+d;
        return result;
    }
}
