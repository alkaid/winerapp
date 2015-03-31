package com.alkaid.winner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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
}
