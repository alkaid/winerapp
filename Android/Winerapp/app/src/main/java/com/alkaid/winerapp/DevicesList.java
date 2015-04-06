package com.alkaid.winerapp;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class DevicesList extends ListActivity {
	private Messenger mService = null; 
	private boolean isFromService=false;
	private boolean mBound=false;
	private ServiceConnection mConnection;
	private static String TAG="DevicesList";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.devices);
		List<String> list = getIntent().getStringArrayListExtra(Constants.LIST_DEVICES);
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		this.setListAdapter(adapter);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		// 判断引用页是否是service 若是 则绑定service
//		isFromService = getIntent().getBooleanExtra(
//				SmsBluetoothService.FROM_SMS_BLUETOOTH_SERVICE, false);
//		if (isFromService) {
//			mConnection=new MServiceConnection();
//			bindService(new Intent(this, SmsBluetoothService.class), mConnection, 
//		            Context.BIND_AUTO_CREATE); 
//		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent data=new Intent();
		data.putExtra(Constants.SELECTED_INDEX, position);
		setResult(RESULT_OK, data);
		if(Constants.D) Log.d(TAG,"Is from service? "+isFromService);
		if(isFromService){
			sendSelected(position);
		}
		finish();
	}
	
	@Override 
    protected void onStop() { 
        super.onStop(); 
        if(isFromService){
	        if (mBound) { 
	        	if(Constants.D) Log.d(TAG,TAG+" unbind service");
	            unbindService(mConnection); 
	            mBound = false; 
	        } 
        }
    } 
	
	private class MServiceConnection implements ServiceConnection{ 
        public void onServiceConnected(ComponentName className, IBinder service) { 
        	if(Constants.D) Log.d(TAG,TAG+" connected service");
            mService = new Messenger(service); 
            mBound = true; 
        } 
 
        public void onServiceDisconnected(ComponentName className) { 
        	if(Constants.D) Log.d(TAG,TAG+" disconnected service");
            mService = null; 
            mBound = false; 
        } 
    }; 
 
    public void sendSelected(int selectedIndex) { 
        /*if (!mBound) return; 
        Message msg = Message.obtain(null, SmsBluetoothService.MSG_SELECTED, selectedIndex, 0); 
        try { 
        	if(Constants.D) Log.d(TAG,TAG+" send message(selected device) to service");
            mService.send(msg); 
        } catch (RemoteException e) { 
            Log.e(TAG, "Send msg to Remote service error",e); 
        } */
    } 

    @Override
    public void onBackPressed() {
    	/*Intent di = new Intent();
    	di.setClass(this, SmsBluetoothService.class);
    	stopService(di);*/
    	super.onBackPressed();
    }

}
