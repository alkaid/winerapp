package com.alkaid.winerapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener{
    private static String TAG="Winerapp";
    private BluetoothClientOp btop;
    private List<String> deviceAddresses=new ArrayList<String>();
    private View layBar,layMain;
    private ProgressDialog pdg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layBar=findViewById(R.id.layBar);
        layMain=findViewById(R.id.layMain);
        btop = new BluetoothClientOp(this,true){
            @Override
            protected void onNotBluetoothAvailable() {
                super.onNotBluetoothAvailable();
                handleError(getString(R.string.notBluetoothAvailable));
            }
            @Override
            protected void onNotDiscoveryDevices() {
                super.onNotDiscoveryDevices();
                handleError(getString(R.string.notDiscoveryDevices));
            }
            @Override
            protected void onFoundDevicesButNotTarget(
                    List<String> deviceAddresses, List<String> displayDevices) {
                super.onFoundDevicesButNotTarget(deviceAddresses, displayDevices);
                dismissPdg();
                MainActivity.this.deviceAddresses=deviceAddresses;
                Intent intent=new Intent(MainActivity.this,DevicesList.class);
                intent.putStringArrayListExtra(Constants.LIST_DEVICES, (ArrayList<String>) displayDevices);
//				intent.putExtra(SmsBluetoothService.FROM_SMS_BLUETOOTH_SERVICE, true);
//				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //TODO 此处有bug 若连发短信,只显示1次提示选择设备的Dialog,导致除了第一条之外的其他短信全部被丢掉  考虑用队列解决
                MainActivity.this.startActivityForResult(intent,0);
            }
            @Override
            public void onConnectionFailed() {
                super.onConnectionFailed();
                handleError(getString(R.string.connectionFailed));
            }
            @Override
            public void onMatchFailed() {
                super.onMatchFailed();
                handleError(getString(R.string.matchFailed));
            }
            @Override
            public void onException(Exception e) {
                super.onException(e);
                handleError(getString(R.string.unknowException)+e.getMessage());
            }
            @Override
            public void onMatchedConnected(boolean success) {
                super.onMatchedConnected(success);
                dismissPdg();
                Toast.makeText(getApplicationContext(), R.string.matchedConnected, Toast.LENGTH_SHORT).show();
                //开始验证链接
                int randNo= (int) (Math.random()*1000);
                randNo=randNo<100?randNo+100:randNo;
                initView(false);
            }
        };
        initView(true);

    }

    private int encode(int num){
        return num;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgLight:
                break;
            case R.id.imgMoto:
                break;
            case R.id.imgSwitch:
                break;
            case R.id.imgTpd:
                break;
            case R.id.imgTurn:
                break;
            default:
                break;
        }
    }

    private void initView(boolean loading){
        if(loading){
            layBar.setVisibility(View.GONE);
            layMain.setBackgroundResource(R.drawable.loading_bg);
            pdg=ProgressDialog.show(this,null,getString(R.string.tip_find_device),true,true,new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    btop.cancel();
                    dismissPdg();
                    handleError(getString(R.string.cancelChooseDevice));
                }
            });
            btop.cancel();
            if(Constants.D) Log.d(TAG, "开始蓝牙操作");
            Util.toast(getApplicationContext(), "开始蓝牙操作");
            btop.operation();
        }else {
            layBar.setVisibility(View.VISIBLE);
            layMain.setBackgroundResource(R.drawable.main_bg);
        }
    }

    private void dismissPdg(){
        if(pdg!=null&&pdg.isShowing()){
            pdg.dismiss();
            pdg=null;
        }
    }

    private void handleError(String msg){
        dismissPdg();
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        msg+="\nPlease retry or exit.";
        AlertDialog.Builder b=new AlertDialog.Builder(this)
                .setMessage(msg).setCancelable(false)
                .setPositiveButton(R.string.btn_retry,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        btop.cancel();
                        pdg=ProgressDialog.show(MainActivity.this,null,getString(R.string.tip_find_device),true,true,new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                btop.cancel();
                                finish();
                            }
                        });
                        btop.operation();
                    }
                }).setNegativeButton(R.string.btn_exit,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                ;
        b.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==Activity.RESULT_OK){
            if(Constants.D) Log.d(TAG,"SmsBluetoothService handle message with selected device");
            int selectedIndex=data.getIntExtra(Constants.SELECTED_INDEX, -1);
            if(selectedIndex<0) {
                handleError(getString(R.string.cancelChooseDevice));
                return;
            }
            dismissPdg();
            pdg=ProgressDialog.show(this,null,getString(R.string.tip_connect_device),true,true,new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    btop.cancel();
                    dismissPdg();
                    handleError(getString(R.string.cancelConnectDevice));
                }
            });
            String address=deviceAddresses.get(selectedIndex);
            BluetoothAdapter btAdapter=btop.getMbtApapter();
            BluetoothDevice device=btAdapter.getRemoteDevice(address);
            int status=btop.connect(device);
        }else{
            handleError(getString(R.string.cancelChooseDevice));
        }
    }
}
