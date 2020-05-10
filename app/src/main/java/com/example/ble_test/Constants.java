package com.example.ble_test;

import java.util.UUID;

public class Constants {
    // tag for log message
    public static String TAG= "ClientActivity";
    // used to identify adding bluetooth names
    public static int REQUEST_ENABLE_BT= 1;
    // used to request fine location permission
    public static int REQUEST_FINE_LOCATION= 2;

    /// CCCD
    public static String CCCD_HM_10 = "00002902-0000-1000-8000-00805F9B34FB";   // HM-10

    //// focus v3 service. refer. https://www.foc.us/bluetooth-low-energy-api
    // service and uuid
    //public static String SERVICE_STRING = "0000aab0-f845-40fa-995d-658a43feea4c";
    public static String SERVICE_STRING_HM_10 = "0000FFE0-0000-1000-8000-00805F9B34FB";   // HM-10
    public static String SERVICE_STRING_BLE_COMBO = "0000A00A-0000-1000-8000-00805F9B34FB";
    //public static UUID UUID_TDCS_SERVICE= UUID.fromString(SERVICE_STRING);
    // command uuid
    //public static String CHARACTERISTIC_COMMAND_STRING = "0000AAB1-F845-40FA-995D-658A43FEEA4C";
    public static String CHARACTERISTIC_COMMAND_STRING = "0000FFE1-0000-1000-8000-00805F9B34FB";
    public static UUID UUID_CTRL_COMMAND = UUID.fromString( CHARACTERISTIC_COMMAND_STRING );
    // response uuid
    // public static String CHARACTERISTIC_RESPONSE_STRING = "0000AAB2-F845-40FA-995D-658A43FEEA4C";
    public static String CHARACTERISTIC_RESPONSE_STRING = "0000FFE1-0000-1000-8000-00805F9B34FB";
    public static UUID UUID_CTRL_RESPONSE = UUID.fromString( CHARACTERISTIC_COMMAND_STRING );
    // focus MAC address
    //public final static String MAC_ADDR= "78:A5:04:58:A7:92";
    public final static String MAC_ADDR= "90:E2:02:92:95:21";
    // scan period
    public static final long SCAN_PERIOD = 5000;

    public static String raw_rssi_log_fileName = "raw_rssi";
    public static String filtered_rssi_log_fileName = "filtered_rssi";
}

