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
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
    private ProgressDialog pdg;
    private AlertDialog errorDialog;
    private PacketReader reader;
    private Status status;
    private View layBar,layMain,layContent;
    private ImageView imgTurnForward,imgTurnBack,imgLightStatus,imgSwitchStatus,imgCurMoto,imgCurTpd;
    private ImageView imgLight,imgMoto,imgSwitch,imgTpd,imgTurn;
    private Handler mHandler;

    private static final int MSG_WHAT_INIT_VIEW_UNLOAD=1;
    private static final int MSG_WHAT_INIT_VIEW_LOAD=2;
    private static final int MSG_WHAT_ERROR=3;
    private static final int MSG_WHAT_UPDATE_STATUS=4;
    private static final int MSG_WHAT_VERIFY_ERROR=5;

    private AnimationDrawable animLightOn,animSwitchOn,animTurnBack,animTurnForward;


//    private static final String BUNDLE_KEY_ERRORMSG="BUNDLE_KEY_ERRORMSG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //find view
        layBar=findViewById(R.id.layBar);
        layMain=findViewById(R.id.layMain);
        layContent=findViewById(R.id.layContent);
        imgTurnForward = (ImageView) findViewById(R.id.imgTurnForward);
        imgTurnBack= (ImageView) findViewById(R.id.imgTurnBack);
        imgLightStatus= (ImageView) findViewById(R.id.imgLightStatus);
        imgSwitchStatus= (ImageView) findViewById(R.id.imgSwitchStatus);
        imgCurMoto= (ImageView) findViewById(R.id.imgCurMoto);
        imgCurTpd= (ImageView) findViewById(R.id.imgCurTpd);

        imgLight= (ImageView) findViewById(R.id.imgLight);
        imgMoto= (ImageView) findViewById(R.id.imgMoto);
        imgSwitch= (ImageView) findViewById(R.id.imgSwitch);
        imgTpd= (ImageView) findViewById(R.id.imgTpd);
        imgTurn= (ImageView) findViewById(R.id.imgTurn);
        //init anim
        animLightOn= (AnimationDrawable) getResources().getDrawable(R.drawable.anim_light_on);
        animSwitchOn= (AnimationDrawable) getResources().getDrawable(R.drawable.anim_switch_on);
        animTurnBack= (AnimationDrawable) getResources().getDrawable(R.drawable.anim_turn_back);
        animTurnForward= (AnimationDrawable) getResources().getDrawable(R.drawable.anim_turn_forward);
        //Test
        status=new Status();
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
                    //TODO test
                    /*pdg=ProgressDialog.show(MainActivity.this,null,MainActivity.this.getString(R.string.beginVerify),true,true,new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            shutdown();
                            dismissPdg();
                            handleError(getString(R.string.cancelVerifyDevice));
                        }
                    });*/
                    status=new Status();
                    startReader();
                    //开始验证链接
                    int randNo = (int) (Math.random() * 1000);
                    randNo = randNo < 100 ? randNo + 100 : randNo;
                    if(randNo>127){
                        int length=String.valueOf(randNo).length();
                        int high = randNo/ (int)Math.pow(10,length-1);
                        int low=randNo%10;
                        randNo=high*10+low;
                    }
                    status.setAuthCode(randNo);
                    //Test
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    /*try {
                        //Util.writeSocketData(btop.getMmSocket(),randNo,2);
//                        Util.writeSocketData(btop.getMmSocket(),String.valueOf(randNo).getBytes());
                        Util.writeSocketData(btop.getMmSocket(),randNo,1);
                    } catch (IOException e) {
                        Log.e(TAG,"Write auth code to server error!",e);
                        handleError(e.getMessage());
                    }*/
                }else{
                    handleError(getString(R.string.connectionFailed));
                }
            }
        };
        mHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_WHAT_INIT_VIEW_LOAD:
                        initView(true);
                        break;
                    case MSG_WHAT_INIT_VIEW_UNLOAD:
                        initView(false);
                        break;
                    case MSG_WHAT_ERROR:
                        String errMsg= (String) msg.obj;
                        handleError(errMsg);
                        break;
                    case MSG_WHAT_VERIFY_ERROR:
                        String emsg= (String) msg.obj;
                        handleError(emsg);
                        break;
                    case MSG_WHAT_UPDATE_STATUS:
//                        dismissPdg();
                        enableOrDisableUI(true);
                        updateStatusView();
                        break;
                    default:
                        break;
                }
            }
        };
        layBar.setVisibility(View.INVISIBLE);
        layContent.setVisibility(View.INVISIBLE);
        initView(true);

    }

    private void shutdown(){
        if(null!=reader) reader.shutdown();
        btop.cancel();
    }

    private void startReader(){
        if(null!=reader) reader.shutdown();
        if(null==reader) {
            reader = new PacketReader(btop.getMmSocket());
            //TODO 应有超时监控
            reader.setPacketReadListener(new PacketReader.PacketReadListener() {
                @Override
                public void onPacketRead(S2cPacket packet) {
                    if (packet instanceof S2cLoginPacket) {
                        S2cLoginPacket loginPacket = (S2cLoginPacket) packet;
//                        int verfication = Util.encode(status.getAuthCode());
                        int verfication=status.getAuthCode();
                        Log.d(TAG, "上位机randNo=" + status.getAuthCode() + " verficaiton=" + verfication + " 下位机verfication=" + loginPacket.getVerification());
                        if (verfication == loginPacket.getVerification()) {
                            status.setMotoNums(loginPacket.getMotoNums());
                            //验证成功
//                        initView(false);
                            mHandler.sendEmptyMessage(MSG_WHAT_INIT_VIEW_UNLOAD);
                        } else {
                            Message msg = mHandler.obtainMessage(MSG_WHAT_VERIFY_ERROR);
                            msg.obj = getString(R.string.verifyFailed);
                            mHandler.sendMessage(msg);
                        }
                    } else if (packet instanceof S2cDefaultResponse) {
                        switch (status.getCurCmd()) {
                            //根据之前的命令更新状态
                            case Status.CMD_LIGHT_OFF:
                                status.setLightOn(false);
                                break;
                            case Status.CMD_LIGHT_ON:
                                status.setLightOn(true);
                                break;
                            case Status.CMD_MOTO:
                                status.changeMoto();
                                break;
                            case Status.CMD_SWITCH_OFF:
                                status.setSwitchOn(false);
                                break;
                            case Status.CMD_SWITCH_ON:
                                status.setSwitchOn(true);
                                break;
                            case Status.CMD_TPD:
                                status.changeTpd();
                                break;
                            case Status.CMD_TURN_ALL:
                                status.setTurnStatus(Status.TURN_STATUS_ALL);
                                break;
                            case Status.CMD_TURN_BACK:
                                status.setTurnStatus(Status.TURN_STATUS_BACK);
                                break;
                            case Status.CMD_TURN_FOWARD:
                                status.setTurnStatus(Status.TURN_STATUS_FORWARD);
                                break;
                        }
                        mHandler.sendEmptyMessage(MSG_WHAT_UPDATE_STATUS);
                    }
                }

                @Override
                public void onException(Exception e) {
                    Log.e(TAG, "", e);
                    Message msg=mHandler.obtainMessage(MSG_WHAT_ERROR);
                    if(e instanceof IOException){
                        msg.obj=getString(R.string.connectException);
                    }else {
                        msg.obj = getString(R.string.unknowException) + "\n" + e.getMessage();
                    }
                    mHandler.sendMessage(msg);
                }
            });
        }
        reader.startup();
    }

    private void updateStatusView(){
        if(PacketReader.logined) {
            imgTurnBack.setImageDrawable(animTurnBack);
            animTurnBack.stop();
            animTurnBack.start();
            imgTurnForward.setImageDrawable(animTurnForward);
            animTurnForward.stop();
            animTurnForward.start();

            if (status.isLightOn()) {
                imgLightStatus.setImageDrawable(animLightOn);
                animLightOn.stop();
                animLightOn.start();
            } else {
                imgLightStatus.setImageResource(R.drawable.ico_light_00);
            }
            if (status.isSwitchOn()) {
                imgSwitchStatus.setImageDrawable(animSwitchOn);
                animSwitchOn.stop();
                animSwitchOn.start();
            } else {
                imgSwitchStatus.setImageResource(R.drawable.ico_turn_00);
            }

        }else {
            animTurnBack.stop();
            animTurnForward.stop();
            animSwitchOn.stop();
            animLightOn.stop();
            imgTurnForward.setImageResource(R.drawable.r01);
            imgTurnBack.setImageResource(R.drawable.l01);
            if (status.isLightOn()) {
                imgLightStatus.setImageResource(R.drawable.ico_light_05);
            } else {
                imgLightStatus.setImageResource(R.drawable.ico_light_00);
            }
            if (status.isSwitchOn()) {
                imgSwitchStatus.setImageResource(R.drawable.ico_turn_01);
            } else {
                imgSwitchStatus.setImageResource(R.drawable.ico_turn_00);
            }
        }
        switch (status.getTurnStatus()) {
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
        imgCurMoto.setImageBitmap(drawNumImg(status.getCurMoto()));
        imgCurTpd.setImageBitmap(drawNumImg(status.getTpd()));
    }

    private Bitmap drawNumImg(int num){
        //原图 83*138px  42*69dp
        float w= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,42,getResources().getDisplayMetrics());
        float h= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,69,getResources().getDisplayMetrics());
        float spacing=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,3,getResources().getDisplayMetrics());
        Bitmap result=null;
        List<Integer> nums=new ArrayList<Integer>();
        if(num==0){
            nums.add(0);
        }
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
            float left=(nums.size()-1-i)*(w+spacing);
            canvas.drawBitmap(imgNo,left,0,mPaint);
        }
        return result;
    }

    private void sendCmd(int cmd){
        try {
            Util.writeSocketData(btop.getMmSocket(),cmd,1);
        } catch (IOException e) {
            Log.e(TAG,"",e);
            handleError(getString(R.string.unknowException)+"\n"+e.getMessage());
        }
    }


    @Override
    public void onClick(View v) {
        int cmd=-1;
        if(!PacketReader.logined){
            handleError(getString(R.string.notConnectedWhenSendCmd));
            return;
        }
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
                        cmd=Status.CMD_TURN_ALL;
                        break;
                    case Status.TURN_STATUS_ALL:
                        cmd=Status.CMD_TURN_FOWARD;
                        break;
                }
                break;
            default:
                return;
        }
//        pdg=ProgressDialog.show(MainActivity.this,null,null,true,false);
        enableOrDisableUI(false);
        status.setCurCmd(cmd);
        sendCmd(cmd);
    }

    private void enableOrDisableUI(boolean enable){
        imgLight.setEnabled(enable);
        imgMoto.setEnabled(enable);
        imgSwitch.setEnabled(enable);
        imgTpd.setEnabled(enable);
        imgTurn.setEnabled(enable);
    }

    private void initView(boolean loading){
        if(loading){
            layMain.setBackgroundResource(R.drawable.loading_bg);
            pdg=ProgressDialog.show(this,null,getString(R.string.tip_find_device),true,true,new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    shutdown();
                    dismissPdg();
                    handleError(getString(R.string.cancelSearchDevice));
                }
            });
            shutdown();
            if(Constants.D) Log.d(TAG, "开始蓝牙操作");
            Util.toast(getApplicationContext(), "开始蓝牙操作");
            btop.operation();
        }else {
            layBar.setVisibility(View.VISIBLE);
            layContent.setVisibility(View.VISIBLE);
            layMain.setBackgroundResource(R.drawable.main_bg);
            updateStatusView();
            dismissPdg();
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
//        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        shutdown();
        initView(false);
        msg+=getString(R.string.tip_error_append);
        if(null!=errorDialog&&errorDialog.isShowing()){
            errorDialog.setMessage(msg);
        }else {
            errorDialog = new AlertDialog.Builder(this)
                    .setMessage(msg).setCancelable(false)
                    .setPositiveButton(R.string.btn_retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            pdg = ProgressDialog.show(MainActivity.this, null, getString(R.string.tip_find_device), true, true, new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    shutdown();
                                    handleError(getString(R.string.cancelSearchDevice));
                                }
                            });
                            btop.operation();
                        }
                    }).setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
            ;
            errorDialog.show();
        }
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
            pdg=ProgressDialog.show(this,null,getString(R.string.tip_connect_device),true,true,new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    shutdown();
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
