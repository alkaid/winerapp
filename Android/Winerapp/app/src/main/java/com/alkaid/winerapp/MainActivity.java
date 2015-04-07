package com.alkaid.winerapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener{
    private static String TAG="Winerapp";
    private BluetoothClientOp btop;
    private List<String> deviceAddresses=new ArrayList<String>();
    private View layBar,layMain;
    private ProgressDialog pdg;
    private PacketReader reader;
    private Status status;
    private ImageView imgTurnForward,imgTurnBack,imgLightStatus,imgSwitchStatus,imgCurMoto,imgCurTpd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layBar=findViewById(R.id.layBar);
        layMain=findViewById(R.id.layMain);
        imgTurnForward = (ImageView) findViewById(R.id.imgTurnForward);
        imgTurnBack= (ImageView) findViewById(R.id.imgTurnBack);
        imgLightStatus= (ImageView) findViewById(R.id.imgLightStatus);
        imgSwitchStatus= (ImageView) findViewById(R.id.imgSwitchStatus);
        imgCurMoto= (ImageView) findViewById(R.id.imgCurMoto);
        imgCurTpd= (ImageView) findViewById(R.id.imgCurTpd);
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
                if(success) {
                    dismissPdg();
                    Toast.makeText(getApplicationContext(), R.string.matchedConnected, Toast.LENGTH_SHORT).show();
                    pdg=ProgressDialog.show(MainActivity.this,null,MainActivity.this.getString(R.string.beginVerify),true,true,new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            btop.cancel();
                            dismissPdg();
                            handleError(getString(R.string.cancelVerifyDevice));
                        }
                    });
                    status=new Status();
                    startReader();
                    //开始验证链接
                    int randNo = (int) (Math.random() * 1000);
                    randNo = randNo < 100 ? randNo + 100 : randNo;
                    status.setAuthCode(randNo);
                    try {
                        Util.writeSocketData(btop.getMmSocket(),randNo);
                    } catch (IOException e) {
                        Log.e(TAG,"Write auth code to server error!",e);
                        handleError(e.getMessage());
                    }
                }else{
                    handleError(getString(R.string.connectionFailed));
                }
            }
        };
        initView(true);

    }

    private void startReader(){
        if(null!=reader) reader.shutdown();
        reader=new PacketReader(btop.getMmSocket());
        reader.setPacketReadListener(new PacketReader.PacketReadListener() {
            @Override
            public void onPacketRead(M2sPacket packet) {
                if(packet instanceof M2sLoginPacket){
                    M2sLoginPacket loginPacket=(M2sLoginPacket)packet;
                    int verfication=Util.encode(status.getAuthCode());
                    Log.d(TAG,"上位机verficaiton="+verfication+" 下位机verfication="+loginPacket.getVerification());
                    if(verfication==loginPacket.getVerification()){
                        status.setMotoNums(loginPacket.getMotoNums());
                        //验证成功
                        initView(false);
                    }else{
                        handleError(getString(R.string.verifyFailed));
                    }
                }else if(packet instanceof M2sDefaultResponse){
                    switch (status.getCurCmd()){
                        //TODO 根据之前的命令更新状态

                    }
                    updateStatusView();
                }
            }
            @Override
            public void onException(Exception e) {
                Log.e(TAG,"",e);
                handleError(getString(R.string.unknowException)+"\n"+e.getMessage());
            }
        });
        reader.startup();
    }

    private void updateStatusView(){
        switch (status.getTurnStatus()){
            case Status.TURN_STATUS_ALL:
                imgTurnBack.setVisibility(View.VISIBLE);
                imgTurnForward.setVisibility(View.VISIBLE);
                break;
            case Status.TURN_STATUS_BACK:
                imgTurnBack.setVisibility(View.VISIBLE);
                imgTurnForward.setVisibility(View.INVISIBLE);
                break;
            case Status.TURN_STATUS_FORWARD:
                imgTurnBack.setVisibility(View.INVISIBLE);
                imgTurnForward.setVisibility(View.VISIBLE);
                break;
        }
        if(status.isLightOn()){
            imgLightStatus.setImageResource(R.drawable.ico_light_01);
        }else{
            imgLightStatus.setImageResource(R.drawable.ico_light_05);
        }
        if(status.isSwitchOn()){
            imgSwitchStatus.setImageResource(R.drawable.ico_turn_01);
        }else{
            imgSwitchStatus.setImageResource(R.drawable.ico_turn_00);
        }

    }

    private Bitmap drawNumImg(int num){
        //原图 83*138px  42*69dp
        float w= TypedValue.applyDimension(TypedValue.TYPE_DIMENSION,42,getResources().getDisplayMetrics());
        float h= TypedValue.applyDimension(TypedValue.TYPE_DIMENSION,69,getResources().getDisplayMetrics());
        float spacing=TypedValue.applyDimension(TypedValue.TYPE_DIMENSION,3,getResources().getDisplayMetrics());
        Bitmap result=null;
        List<Integer> nums=new ArrayList<Integer>();
        while (num!=0){
            nums.add(num%10);
            num/=10;
        }
        float maxWidth=w*nums.size()+spacing*(nums.size()-1);
        result=Bitmap.createBitmap((int)maxWidth,(int)h, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(result);
        Paint mPaint = new Paint();
        for(int i=nums.size()-1;i>=0;i--){
            int id = getResources().getIdentifier("number"+nums.get(i),"drawable",getPackageName());
            Bitmap imgNo= BitmapFactory.decodeResource(this.getResources(),id);

            canvas.drawBitmap(left,top,imgNo);
        }
        return result;
    }

    private void sendCmd(int cmd){
        try {
            Util.writeSocketData(btop.getMmSocket(),cmd);
        } catch (IOException e) {
            Log.e(TAG,"",e);
            handleError(getString(R.string.unknowException)+"\n"+e.getMessage());
        }
    }


    @Override
    public void onClick(View v) {
        int cmd=-1;
        switch (v.getId()){
            case R.id.imgLight:
                cmd = status.isLightOn()?Status.CMD_LIGHT_OFF:Status.CMD_LIGHT_ON;
                break;
            case R.id.imgMoto:
                cmd=Status.CMD_MOTO;
                break;
            case R.id.imgSwitch:
                cmd = status.isSwitchOn()?Status.CMD_SWITCH_OFF:Status.CMD_SWITCH_ON;
                break;
            case R.id.imgTpd:
                cmd=Status.CMD_TPD;
                break;
            case R.id.imgTurn:
                switch (status.getTurnStatus()){
                    case Status.TURN_STATUS_FORWARD:
                        cmd=Status.CMD_TURN_BACK;
                        break;
                    case Status.TURN_STATUS_BACK:
                        cmd=Status.TURN_STATUS_ALL;
                        break;
                    case Status.TURN_STATUS_ALL:
                        cmd=Status.CMD_TURN_FOWARD;
                        break;
                }
                break;
            default:
                return;
        }
        pdg=ProgressDialog.show(MainActivity.this,null,null,true,false);
        status.setCurCmd(cmd);
        sendCmd(cmd);
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
            updateStatusView();
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
        btop.cancel();
        if(null!=reader){
            reader.shutdown();
        }
        msg+="\nPlease retry or exit.";
        AlertDialog.Builder b=new AlertDialog.Builder(this)
                .setMessage(msg).setCancelable(false)
                .setPositiveButton(R.string.btn_retry,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
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
