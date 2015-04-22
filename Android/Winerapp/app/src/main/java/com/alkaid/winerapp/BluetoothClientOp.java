package com.alkaid.winerapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
public class BluetoothClientOp {
	private String TAG="BluetoothClientOp";
	private Context ctx;
	
	private BluetoothAdapter mbtApapter;
	private List<String> deviceAddresses=new ArrayList<String>();
	private List<String> displayDevices=new ArrayList<String>();
	/** 是否找到目标设备 */
	private boolean isFoundTarget=false;
	/** 目标设备mac */
	private String targetDevicesAddress;	
	
	/** 目标设备 */
	private BluetoothDevice targetDevices;
	/** 连接方式：是否主动发起配�? 默认为false */
	private boolean isMatchBeforeConnect=false;
	/** 搜索重试次数 */
	private int SEARCH_COUNT=0;
	/** 搜索重试次数计数 */
	private int search_count=0;
	/** 连接重试次数 */
	private int CONNECT_COUNT=0;
	/** 连接重试次数计数 */
	private int connect_count=0;
	/** 配对重试次数计数 */
	private int match_count=0;
	/** client socket */
	private BluetoothSocket mmSocket;
	private SearchDevicesReceiver searchDevicesReceiver;
    private boolean hasRegisteredReceiver=false;
	/** 蓝牙是否本来就已经开�? */
	private boolean isBluetoothEnable=false;
	
	/** 连接方式：先配对 */
	public static int BT_MATCH_FIRST=1;
	/** 连接方式：没有经过配对直接连�? */
	public static int BT_DIRECT_CONNECT=2;
	/** 连接方式：连接失败或配对失败 */
	public static int BT_FAILED=-1;
	/**
	 * 
	 */
	public BluetoothClientOp(Context ctx){
		this.ctx=ctx;
	}
	/**
	 * 
	 * @param ctx
	 * @param isMatchBeforeConnect 连接方式：是否主动发起配�? 默认为false
	 */
	public BluetoothClientOp(Context ctx,boolean isMatchBeforeConnect){
		this.ctx=ctx;
		this.isMatchBeforeConnect=isMatchBeforeConnect;
	}
	/**
	 * 
	 * @param ctx context
	 * @param targetDevicesAddress 目标设备地址
	 */
	public BluetoothClientOp(Context ctx,String targetDevicesAddress){
		this.ctx=ctx;
		this.targetDevicesAddress=targetDevicesAddress;
	}
	/**
	 * 
	 * @param ctx context
	 * @param targetDevicesAddress 目标设备地址
	 * @param isMatchBeforeConnect 连接方式：是否主动发起配�? 默认为false
	 */
	public BluetoothClientOp(Context ctx,String targetDevicesAddress,boolean isMatchBeforeConnect){
		this.ctx=ctx;
		this.targetDevicesAddress=targetDevicesAddress;
		this.isMatchBeforeConnect=isMatchBeforeConnect;
	}

    public void operation(){
        operation(false);
    }
	
	public void operation(boolean waitingMatch){
		mbtApapter=BluetoothAdapter.getDefaultAdapter();
	     // If the adapter is null, then Bluetooth is not supported
	        if (mbtApapter == null) {
	            Log.e(TAG,"Bluetooth is not available");
	            onNotBluetoothAvailable();
	            return;
	        }
	        //打开蓝牙 �? �?始搜索设�?
	        if(!mbtApapter.isEnabled()){
	        	mbtApapter.enable();
	        }else{
	        	isBluetoothEnable=true;
	        }
//	        System.out.println(mbtApapter.getScanMode());
//	        System.out.println(mbtApapter.getScanMode()==BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
	        // 注册Receiver来获取蓝牙设备相关的结果
            if(!hasRegisteredReceiver) {
                searchDevicesReceiver = new SearchDevicesReceiver();
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);// 用BroadcastReceiver来取得搜索结�?
                filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                ctx.registerReceiver(searchDevicesReceiver, filter);
                hasRegisteredReceiver=true;
            }
	        if(Constants.D) Log.d(TAG,"BluetoothReceiver注册成功 ");
	        Util.toast(ctx, "BluetoothReceiver注册成功 ");
	        if(isBluetoothEnable&&!waitingMatch){
	        	searchDevice();
	        }
	}
	
	protected void onNotBluetoothAvailable() {
	}

	public void cancel(){
		if(null!=mmSocket){
	    	try {
				mmSocket.close();
			} catch (IOException e2) {
				Log.e(TAG,
						"unable to close() socket during connection failure",
						e2);
			}
    	}
        if(hasRegisteredReceiver) {
            ctx.unregisterReceiver(searchDevicesReceiver);
        }
        hasRegisteredReceiver=false;
        isBluetoothEnable=false;
        isFoundTarget=false;
        connect_count=0;
        match_count=0;
        search_count=0;
		if(Constants.D) Log.d(TAG,"BluetoothReceiver注销成功 ");
		Util.toast(ctx, "BluetoothReceiver注销成功 ");
	}
	
	 //蓝牙状�?�监�? 包含搜索蓝牙、搜索完成后�?启服务端和客户端   的�?�辑
    private class SearchDevicesReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if(Constants.D) Log.d(TAG,"searchDevicesReceiver has onReceive");
			String action=intent.getAction();
			//当蓝牙成功打�?后开始搜索附近蓝牙设�?
			if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
				if(mbtApapter.isEnabled()){
					 searchDevice();
				}
			}
			// 搜索设备时，取得设备的MAC地址  
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {  
                BluetoothDevice device = intent  
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); 
                String isPair= device.getBondState() == BluetoothDevice.BOND_BONDED?"Bonded":"Unbond";
//                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {  
                    String str = isPair+"|" + device.getName() + "|"  
                            + device.getAddress();  
                    deviceAddresses.add(device.getAddress());
                    displayDevices.add(str);
                    if(Constants.D) Log.d(TAG,"搜索到设备： "+str);
                    Util.toast(ctx, "搜索到设备： "+str);
                    //若找到目标设�? 立即结束搜索
                    if(device.getAddress().equals(targetDevicesAddress)){
                    	isFoundTarget=true;
                    	mbtApapter.cancelDiscovery();
                    }
//                }  
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	if(Constants.D) Log.d(TAG,"搜索结束");
            	Util.toast(ctx, "搜索结束 ");
                if (deviceAddresses.isEmpty()) {
                	if(search_count>SEARCH_COUNT){
	                	if(Constants.D) Log.d(TAG,"没有搜到任何设备");
	                	Util.toast(ctx, "没有搜到任何设备 ");
	                	onNotDiscoveryDevices();
	                	return;
                	}
                	// 找不到设备时  搜索重试N次再放弃
                	if(search_count<=SEARCH_COUNT){
                		searchDevice();
                		search_count++;
                	}
                }else if(isFoundTarget){
                	targetDevices=mbtApapter.getRemoteDevice(targetDevicesAddress);
                	if(Constants.D) Log.d(TAG,"找到设备"+targetDevices);
                	Util.toast(ctx, "找到设备"+targetDevices);
                	connect(targetDevices);
                }else{
                	onFoundDevicesButNotTarget(deviceAddresses,displayDevices);
                }
            }else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){   
            	targetDevices = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);   
                switch (targetDevices.getBondState()) {   
                case BluetoothDevice.BOND_BONDING:  
                	if(Constants.D) Log.d(TAG,"正在配对......"+search_count);
                	Util.toast(ctx, "正在配对......"+search_count);
                    break;   
                case BluetoothDevice.BOND_BONDED:   
                	if(Constants.D) Log.d(TAG,"完成配对......"+search_count);
                	Util.toast(ctx, "完成配对......"+search_count);
                    boolean success=directConnect(targetDevices);//连接设备   
                    onMatchedConnected(success);
                    break;   
                case BluetoothDevice.BOND_NONE:   
                	if(Constants.D) Log.d(TAG,"取消配对......"+search_count);
                	Util.toast(ctx, "取消配对......"+search_count);
                	onMatchedConnected(false);
                	break;
                default:   
                    break;   
                }   
            }   
		}


    }
    
    /**
     * 搜索到附近的设备但没发现目标�?
     * @param deviceAddresses  设备地址列表
     * @param displayDevices   用来显示的文字列�?
     */
    protected void onFoundDevicesButNotTarget(List<String> deviceAddresses,
    		List<String> displayDevices) {
    }
    protected void onNotDiscoveryDevices() {
    }
    
    /**�?始搜索设�?*/
    private void searchDevice() {
        if (!mbtApapter.isDiscovering()) {
        	mbtApapter.cancelDiscovery();
        }
        deviceAddresses.clear();
        displayDevices.clear();
        mbtApapter.startDiscovery();
        if(Constants.D) Log.d(TAG,"开始搜索,搜索重试次数="+search_count);
        Util.toast(ctx, "开始搜索,搜索重试次数="+search_count);
    }
    
    /**
     * 连接蓝牙设备并写入数据，根据初始化参数分为配对方式和直连方式<br/>
     * 请注意返回�?? 若方法返回BT_FIRST_MATCH 代表蓝牙连接过程先经过了配对过程，则子类必须实现生命周期 onMatchedConnected()方法以完成连接完成后的后续操作�??
     * @param device
     * @return
     * 
     */
    public int connect(BluetoothDevice device){
//    	getUUID(device);
    	if(isMatchBeforeConnect){
    		return match(device);
    	}else{
    		if(directConnect(device))
    			return BT_DIRECT_CONNECT;
    		else
    			return BT_FAILED;
    	}
    }
    
    /**
     * 配对蓝牙设备
     * @param device
     */
    private int match(BluetoothDevice device){
        if (device.getBondState() == BluetoothDevice.BOND_NONE) { 
        	targetDevices=device;	//将要配对的设备赋值给targetDevices以便配对成功的监听事件取得设�?
            boolean matchSuccess = matchRetry(device);
            if(matchSuccess){
    			if(Constants.D) Log.d(TAG,"调用配对方法成功，等待配对监听... ");
    			Util.toast(ctx, "调用配对方法成功，等待配对监听...");
    			return BT_MATCH_FIRST;
    		}else{
    			if(Constants.D) Log.d(TAG,"设备配对重试"+CONNECT_COUNT+"次后失败");
    			Util.toast(ctx, "设备配对重试"+CONNECT_COUNT+"次后失败");
    			onMatchFailed();
    			return BT_FAILED;
    		}
        }else if(device.getBondState() == BluetoothDevice.BOND_BONDED){   
            directConnect(device); 
            return BT_DIRECT_CONNECT;
        }   
        return BT_FAILED;
    }
    /**
     * 配对 有重试机�?
     * @param device
     * @return
     */
    private boolean matchRetry(BluetoothDevice device){
		if(Constants.D) Log.d(TAG,"开始配对...，配对重试次数:"+match_count);
		Util.toast(ctx, "开始配对...，配对重试次数:"+match_count);
		Boolean isMatchSuccess=false;
		Exception exp=null;
		try {
			//利用反射方法调用BluetoothDevice.createBond(BluetoothDevice remoteDevice);   
			Method createBondMethod = BluetoothDevice.class
					.getMethod("createBond");
			isMatchSuccess = (Boolean) createBondMethod.invoke(device);
		} catch (Exception e) {
			Log.e(TAG,"配对异常",e);
			exp=e;
			isMatchSuccess=false;
		}
		if(!isMatchSuccess){
			match_count++;
			if(match_count>CONNECT_COUNT){
				if(null==exp) exp=new Exception("配对失败，请重试");
				onException(exp);
			}else{
				isMatchSuccess=matchRetry(device);
			}
		}
		return isMatchSuccess;
	}
    
    /**
     * 连接蓝牙设备并写入数�?
     * @param device
     * @return
     */
	private boolean directConnect(BluetoothDevice device) {
//		getUUID(device);
		BluetoothSocket tmp = null;
		try {
			tmp = device.createRfcommSocketToServiceRecord(Constants.MY_UUID);
			if(Constants.D) Log.d(TAG,"mmSocket="+tmp);
		} catch (IOException e1) {
			Log.e(TAG, "connect() failed", e1);
			return false;
		}
		mmSocket = tmp;
		boolean successConnect=connectRetry(mmSocket);
		if(successConnect){
			if(Constants.D) Log.d(TAG,"socket连接成功 ");
			Util.toast(ctx, "socket连接成功");
            onMatchedConnected(true);
		}else{
			if(Constants.D) Log.d(TAG,"socket连接重试"+CONNECT_COUNT+"次后失败");
			Util.toast(ctx, "socket连接重试"+CONNECT_COUNT+"次后失败");
			onConnectionFailed();
			return false;
		}
//		if(Constants.D) Log.d(TAG,"写入数据�?"+device+"  mmSocket="+mmSocket+"  message="+message.getBytes());
//		Util.toast(ctx, "写入数据�?"+device+"  mmSocket="+mmSocket+"  message="+message.getBytes());
//    	try {
//			Util.writeSocketData(mmSocket, message.getBytes());
//			return true;
//		} catch (IOException e) {
//			Log.e(TAG,"write data with socket error",e);
//			onException(e);
//		}
    	return true;
	}
	
	/**
	 * 连接 有重试机�?
	 * @param socket
	 * @return
	 */
	private boolean connectRetry(BluetoothSocket socket){
		if(Constants.D) Log.d(TAG,"socket连接ing...，socket连接重试次数:"+connect_count);
		Util.toast(ctx, "socket连接ing...，socket连接重试次数:"+connect_count);
		try {
			mmSocket.connect();
			return true;
		} catch (IOException e) {
			Log.e(TAG,"unable to connect server,maybe server closed or server is busy now",e);
			connect_count++;
			//重试N此后仍然失败则回调onConnectionFailed()
			if(connect_count>CONNECT_COUNT){
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG,"unable to close() socket during connection failure",e2);
				}
				onException(e);
				return false;
			}else{
				//重试
				connectRetry(socket);
				return false;
			}
		}
	}
	
	/**
	 * 调用BlueDevice.getUuids()方法
	 * @param device
	 */
	private void getUUID(BluetoothDevice device){
		try {
			Method getUid=BluetoothDevice.class.getMethod("getUuids");
			ParcelUuid[] uuids = (ParcelUuid[]) getUid.invoke(device);
			for(ParcelUuid uuid:uuids){
				UUID uid = uuid.getUuid();
				if(Constants.D) Log.d(TAG, "get uuid:"+uid.toString());
				Util.toast(ctx, "get uuid:"+uid.toString());
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 配对连接完成�?*
	 * @param success 是否连接成功
	 */
	public void onMatchedConnected(boolean success){
		if(Constants.D) Log.d(TAG,TAG+" matched & connected!");
	}
    
	public void onMatchFailed() {
    	if(Constants.D) Log.d(TAG,TAG+" match failed!");
	}
	
    public void onConnectionFailed() {
    	if(Constants.D) Log.d(TAG,TAG+" connect failed!");
	}

	public BluetoothAdapter getMbtApapter() {
		return mbtApapter;
	}
    
    public void onException(Exception e){
    	
    }
	public BluetoothSocket getMmSocket() {
		return mmSocket;
	}
    
    
}
