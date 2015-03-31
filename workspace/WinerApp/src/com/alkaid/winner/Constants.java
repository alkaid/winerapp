package com.alkaid.winner;

import java.util.UUID;

public class Constants {
	public static final String SMS_FROM_ADDRESS_EXTRA = "com.smartmap.demo.SMS_FROM_ADDRESS";
    public static final String SMS_FROM_DISPLAY_NAME_EXTRA = "com.smartmap.demo.SMS_FROM_DISPLAY_NAME";
    public static final String SMS_MESSAGE_EXTRA = "com.smartmap.demo.SMS_MESSAGE";
    //DIY
//    public static final UUID MY_UUID = UUID.fromString("0000110e-0000-1000-8000-00805f9b34fb");
    //SPP
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //HFP
//    public static final UUID MY_UUID = UUID.fromString("0000111e-0000-1000-8000-00805f9b34fb");
    //HSP
//    public static final UUID MY_UUID = UUID.fromString("00001108-0000-1000-8000-00805f9b34fb");
    /** 众鸿短信平台号码 */
//    public static final String SMS_SERVER_NO="18603035095";
    //测试用号�?
    public static final String SMS_SERVER_NO="13631652575";

    /** 是否�?启debug */
    public static final boolean D=true;
    /** 是否�?启Toast打印log */
    public static final boolean T=true;
    /** 目标设备mac */
	public static final String TARGET_DEVICES_ADDRESS="D8:B3:77:E0:93:0E";	//htc mhh
//	public static final String TARGET_DEVICES_ADDRESS="04:18:0F:30:AB:A8";  //nexus s lc
	
	public static final String LIST_DEVICES="list_devices";
	public static final String SELECTED_INDEX="selected_index";
}
