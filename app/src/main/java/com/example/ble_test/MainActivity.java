package com.example.ble_test;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_BROADCAST;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_INDICATE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
import static com.example.ble_test.Constants.CCCD_HM_10;
import static com.example.ble_test.Constants.MAC_ADDR_BLE_COMBO;
import static com.example.ble_test.Constants.REQUEST_ENABLE_BT;
import static com.example.ble_test.Constants.REQUEST_FINE_LOCATION;
import static com.example.ble_test.Constants.SCAN_PERIOD;
import static com.example.ble_test.Constants.TAG;
import static com.example.ble_test.Constants.filtered_rssi_log_fileName;
import static com.example.ble_test.Constants.isMacAddressFilterEnabled;
import static com.example.ble_test.Constants.raw_rssi_log_fileName;
import static com.example.ble_test.Constants.scan_time_milisecond;
import static com.example.ble_test.ListViewBtnAdapter.BTN_INDEX_MAX;
import static com.example.ble_test.ListViewBtnAdapter.BTN_INDICATE_INDEX;
import static com.example.ble_test.ListViewBtnAdapter.BTN_NOTIFY_INDEX;
import static com.example.ble_test.ListViewBtnAdapter.BTN_READ_INDEX;
import static com.example.ble_test.ListViewBtnAdapter.BTN_SEND_INDEX;


public class MainActivity extends AppCompatActivity  implements ListViewBtnAdapter.ListBtnClickListener {

   //// GUI variables
    // text view for status
    private TextView tv_status_;
    // text view for device info
    private TextView tv_device_info_;
    // text view for read
    private TextView tv_rssi_;
    private TextView tv_filtered_rssi_;

    DataManager dataManager;

    // button for start scan
    private Button btn_config_;
    // button for start scan
    private Button btn_scan_, btn_watch_;
    // button for start connect
    private Button btn_connect_;
    // button for stop connection
    private Button btn_stop_;
    // button for send data
    //private Button btn_send_;
    // button for read data
    //private Button btn_read_;

    //private EditText edit_input_send_;
    //private TextView tv_read_;

    // button for show paired devices
    private Button btn_show_;
    private View line_match_characteristics_;
    private TextView match_characteristics_tv;

    private ListView listview;
    private ArrayList<String> items;
    private ArrayAdapter adapter;

    private ListView match_characteristics_listview;
    private ArrayList<ListViewBtnItem> match_characteristics_items;
    private ArrayAdapter match_characteristics_adapter;
    private int  target_characteristic_index;

    private int target_device_index;
    private String target_device_mac;
    private String target_device_name;
    //private BluetoothDevice target_device;

    private HashMap<String, String> deviceIdTables;

    // ble adapter
    private BluetoothAdapter ble_adapter_;
    // flag for scanning
    private boolean is_scanning_= false;
    // flag for connection
    private boolean connected_= false;
    // scan results
    private HashMap<String, CostumedBluetoothDevice> scan_results_;
    // scan callback
    private ScanCallback scan_cb_;
    // ble scanner
    private BluetoothLeScanner ble_scanner_;
    // scan handler
    private Handler scan_handler_;
    private Handler watch_handler_;


    // watching state
    private boolean isWatching = false;

    // selected characteristics
    private int characteristic_item_position_cur_selected;

    // notification config flag
    private boolean isNotificaionEable = false;

    // BLE Gatt
    private BluetoothGatt ble_gatt_;

    // service uuid, ctrl command uuid, ctrl response uuid
    /*
    public static String SERVICE_STRING = "0000aab0-f845-40fa-995d-658a43feea4c";
    public static UUID UUID_TDCS_SERVICE= UUID.fromString(SERVICE_STRING);
    public static String CHARACTERISTIC_COMMAND_STRING = "0000AAB1-F845-40FA-995D-658A43FEEA4C";
    public static UUID UUID_CTRL_COMMAND = UUID.fromString( CHARACTERISTIC_COMMAND_STRING );
    public static String CHARACTERISTIC_RESPONSE_STRING = "0000AAB2-F845-40FA-995D-658A43FEEA4C";
    public static UUID UUID_CTRL_RESPONSE = UUID.fromString( CHARACTERISTIC_RESPONSE_STRING );
    */

    public static String TARGET_CHARACTERISTIC_UUID;

    // hm10 bluetooth module UUID
    // https://tech.devgear.co.kr/delphi_qna/440362
    /*
          ServiceUUID    : '{0000FFE0-0000-1000-8000-00805F9B34FB}';
          ReadUUID       : '{0000FFE1-0000-1000-8000-00805F9B34FB}';
          WriteUUID      : '{0000FFE1-0000-1000-8000-00805F9B34FB}
    */
    // ??? ???? ????
    // https://postpop.tistory.com/17
    // https://m.blog.naver.com/juke45ef/220834141429



    private KalmanFilter mKalmanRSSI;
    private int rawRSSI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // KalmanFiltered RSSI
        mKalmanRSSI = new KalmanFilter(0.0f);

        // RSSI log
        dataManager = new DataManager(MainActivity.this);

        // listview 생성 및 adapter 지정.
        items = new ArrayList<String>() ;
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_checked, items) ;

        listview = (ListView) findViewById(R.id.listview1) ;
        listview.setAdapter(adapter) ;

        //match_characteristics_items = new ArrayList<String>() ;
        //match_characteristics_adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_checked, match_characteristics_items) ;

        //match_characteristics_listview = (ListView) findViewById(R.id.match_characteristics_listview) ;
        //match_characteristics_listview.setAdapter(match_characteristics_adapter) ;

        match_characteristics_items = new ArrayList<ListViewBtnItem>() ;
        match_characteristics_adapter = new ListViewBtnAdapter(this, R.layout.listview_btn_item, match_characteristics_items,this) ;

        match_characteristics_listview = (ListView) findViewById(R.id.match_characteristics_listview) ;
        match_characteristics_listview.setAdapter(match_characteristics_adapter) ;


        //// get instances of gui objects
        btn_config_ = findViewById(R.id.btn_config);
        // status textview
        tv_status_= findViewById( R.id.tv_status );
        // device info
        tv_device_info_= findViewById( R.id.tv_device_info );
        // raw rssi
        tv_rssi_= findViewById( R.id.tv_rssi );
        // filetered rssi
        tv_filtered_rssi_ = findViewById( R.id.tv_filtered_rssi );
        // scan button
        btn_scan_= findViewById( R.id.btn_scan );
        // watch device
        btn_watch_= findViewById( R.id.btn_watch );
        // connect device
        btn_connect_= findViewById( R.id.btn_connect );
        // line matched characteristics
        line_match_characteristics_ = findViewById( R.id.line_match_characteristics );
        line_match_characteristics_.setVisibility(View.GONE);
        match_characteristics_tv = findViewById( R.id.tv_match_characteristics );
        match_characteristics_tv.setVisibility(View.GONE);

        //btn_send_ = findViewById( R.id.btn_send );
        //btn_read_ = findViewById( R.id.btn_read );
        //edit_input_send_ = findViewById( R.id.et_input_send );
        //tv_read_ = findViewById( R.id.tv_read );
        //btn_send_.setVisibility(View.GONE);
        //btn_read_.setVisibility(View.GONE);
        //edit_input_send_.setVisibility(View.GONE);
        //tv_read_.setVisibility(View.GONE);


        // ble manager
        BluetoothManager ble_manager;
        ble_manager= (BluetoothManager)getSystemService( Context.BLUETOOTH_SERVICE );
        // set ble adapter
        ble_adapter_= ble_manager.getAdapter();
        // intialize ble scanner
        ble_scanner_ = ble_adapter_.getBluetoothLeScanner();


        //// set click event handler
        btn_scan_.setOnClickListener( (v) -> { startScan(v); });
        btn_watch_.setOnClickListener( (v) -> {
            if (!isWatching) {
                isWatching = true;
                startWatch(v);
            }
            else {
                isWatching = false;
                watch_handler_= new Handler();
                watch_handler_.postDelayed( this::stopWatch, 100 );   // TODO
            }

        });

        btn_connect_.setOnClickListener( (v) -> { StartConnect(v); });

        btn_config_.setOnClickListener( (v) -> { StartConfigurations(v); });

        //btn_send_.setOnClickListener( (v) -> { sendData(v); });

        //btn_read_.setOnClickListener( (v) -> { readData(v); });
    }


    @Override
    protected void onResume() {
        super.onResume();
        // finish app if the BLE is not supported
        if( !getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE ) ) {
            Log.d( TAG, "FEATURE_BLUETOOTH_LE is supported." );
            finish();
        }
    }

    private void StartConfigurations( View v ) {
        Intent intent = new Intent(this, ConfigActivity.class);
        startActivity(intent);
    }

    /*
    Start Watch device
     */
    private void startWatch( View v ) {

        // init tv state
        tv_device_info_.setText("<Device>");
        tv_rssi_.setText("<RSSI>");
        tv_filtered_rssi_.setText("<RSSI>");

        // init scanned items in listview
        int count, checked;
        count = adapter.getCount();

        if (count > 0) {
            // 현재 선택된 아이템의 position 획득.
            checked = listview.getCheckedItemPosition();

            if (checked > -1 && checked < count) {
                // set checked index
                target_device_index = checked;
                Log.d(TAG, "checked index: " + target_device_index);
                // get target device
                target_device_mac = items.get(checked);
                // update data of target device
                watchTargetDevice();
            }
        }
    }


    private void StartConnect(View v) {
        // init scanned items in listview
        int count, checked;
        count = adapter.getCount();

        if (count > 0) {
            // 현재 선택된 아이템의 position 획득.
            checked = listview.getCheckedItemPosition();

            if (checked > -1 && checked < count) {
                // set checked index
                target_device_index = checked;
                Log.d(TAG, "checked index: " + target_device_index);
                // get target device
                target_device_mac = items.get(checked);
                // update data of target device

                BluetoothDevice target_ble_device = scan_results_.get(target_device_mac).get_device();
                connectDevice(target_ble_device);
            }
        }
    }

    /*
    Connect to the ble device
    */
    private void connectDevice( BluetoothDevice _device ) {
        // update the status
        if (null !=_device.getName()) {
            tv_status_.setText( "Connecting to " + _device.getName() );
            tv_device_info_.setText(_device.getName());

        }
        else {
            tv_status_.setText( "Connecting to " + _device.getAddress() );
            tv_device_info_.setText(_device.getAddress());
        }
        Log.d( TAG, "Connecting to " + _device.getAddress() );

        GattClientCallback gatt_client_cb= new GattClientCallback();
        ble_gatt_= _device.connectGatt( this, false, gatt_client_cb );  // TODO false ????? ??????? ?????? ??????? ?????? ?o????? ????.
    }

    /*
    Disconnect Gatt Server
    */
    public void disconnectGattServer() {
        Log.d( TAG, "Closing Gatt connection" );
        // reset the connection flag
        connected_= false;
        // disconnect and close the gatt
        if( ble_gatt_ != null ) {
            ble_gatt_.disconnect();
            ble_gatt_.close();
        }
    }

    @Override
    public void onListBtnClick(int position) {


        int characteristic_item_position = position / BTN_INDEX_MAX;
        characteristic_item_position_cur_selected = characteristic_item_position;
        ListViewBtnItem characteristic_item = match_characteristics_items.get(characteristic_item_position);
        String target_characteristic_uuid = characteristic_item.getUuid();

        int property_int = position % BTN_INDEX_MAX;

        Log.d(TAG, "item_position: " + characteristic_item_position);
        Log.d(TAG, "property_int: " + property_int);

        switch(property_int){
            case BTN_SEND_INDEX:

                //for(int i=0;i<match_characteristics_items.size();i++){
                    View view=match_characteristics_listview.getChildAt(characteristic_item_position);
                    if (view == null) {
                        Toast.makeText(this, "Error: try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    EditText editText= (EditText) view.findViewById(R.id.et_send);
                    String msg = editText.getText().toString();
                    if(msg.isEmpty()) {
                        Log.d(TAG, "position: " + characteristic_item_position);
                        Toast.makeText(this, "Message is empty.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                //}

                /*
                if (characteristic_item.isSendMessageEmpty()){

                }
                */

                sendData(target_characteristic_uuid, msg);
                Toast.makeText(this, "Send Message: " + msg, Toast.LENGTH_SHORT).show() ;
                break;
            case BTN_READ_INDEX:
                readData(target_characteristic_uuid);
                Toast.makeText(this, "Reading..", Toast.LENGTH_SHORT).show() ;
                break;
            case BTN_NOTIFY_INDEX:
                if (!isNotificaionEable) {
                    config_notification(target_characteristic_uuid ,true);
                }
                else{
                    config_notification(target_characteristic_uuid ,false);
                }
                break;
            case BTN_INDICATE_INDEX:
                Toast.makeText(this, "Not supported yet", Toast.LENGTH_SHORT).show() ;
                break;
        }
    }



    /*
    Gatt Client Callback class
    */
    private class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange( BluetoothGatt _gatt, int _status, int _new_state ) {
            super.onConnectionStateChange( _gatt, _status, _new_state );

            // read rssi after connection
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int count = 0;
                    while(true){
                        try {
                            boolean rssiStatus = _gatt.readRemoteRssi();
                            //Log.d(TAG, "GattClientCallback readRemoteRssi");
                            Thread.sleep(500);
                            if (scan_time_milisecond * (1000 / 500) == count++) {
                                tv_status_.setText( "Connected (Reading RSSI is done)" );
                                break;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

            if( _status == BluetoothGatt.GATT_FAILURE ) {
                disconnectGattServer();
                Log.e( TAG, "Fail (GATT_FAILURE)" );

                return;
            } else if( _status != BluetoothGatt.GATT_SUCCESS ) {
                disconnectGattServer();
                Log.e( TAG, "Fail (NOT GATT_SUCCESS)" );
                return;
            }
            if( _new_state == BluetoothProfile.STATE_CONNECTED ) {

                Log.d( TAG, "Connected to the GATT server" );

                // update the connection status message
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                tv_status_.setText( "Connected" );
                            }
                        });
                    }
                }).start();

                // set the connection flag
                connected_= true;

                Log.d( TAG, "Try to discover services" );

                // try to discover services
                _gatt.discoverServices();

            } else if ( _new_state == BluetoothProfile.STATE_DISCONNECTED ) {
                disconnectGattServer();
                Log.e( TAG, "Fail (STATE_DISCONNECTED)" );
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status){
            if (status == BluetoothGatt.GATT_SUCCESS) {

                int filteredRSSI = (int) mKalmanRSSI.update(rssi);

                // update ui
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                tv_rssi_.setText(Integer.toString(rssi));
                                tv_filtered_rssi_.setText(Integer.toString(filteredRSSI));
                            }
                        });
                    }
                }).start();

                // save rssi log
                dataManager.fileSaveInExtenalStorage(raw_rssi_log_fileName, Integer.toString(rssi));
                dataManager.fileSaveInExtenalStorage(filtered_rssi_log_fileName, Integer.toString(filteredRSSI));
                //Log.d(TAG, String.format("BluetoothGatt ReadRssi[%d]", rssi));
            }
        }

        @Override
        public void onServicesDiscovered( BluetoothGatt _gatt, int _status ) {
            super.onServicesDiscovered( _gatt, _status );
            // check if the discovery failed
            if( _status != BluetoothGatt.GATT_SUCCESS ) {
                Log.e( TAG, "Device service discovery failed, status: " + _status );
                return;
            }
            // find discovered characteristics
            List<BluetoothGattCharacteristic> matching_characteristics= BluetoothUtils.findBLECharacteristics( _gatt );
            if( matching_characteristics.isEmpty() ) {
                Log.e( TAG, "Unable to find characteristics." );
                return;
            }

            for(BluetoothGattCharacteristic match_characteristic : matching_characteristics){
                UUID match_characteristics_uuid = match_characteristic.getUuid();
                int property = match_characteristic.getProperties();
                String property_string = "";

                if ((property & (0x1 << getMultiple(PROPERTY_BROADCAST)))== (0x1 << getMultiple(PROPERTY_BROADCAST)))
                {
                    if (property_string.isEmpty()) property_string += ("BROADCAST");
                    else property_string += (", BROADCAST");
                }
                if ((property & (0x1 << getMultiple(PROPERTY_EXTENDED_PROPS)))== (0x1 << getMultiple(PROPERTY_EXTENDED_PROPS)))
                {
                    if (property_string.isEmpty()) property_string += ("EXTENDED_PROPS");
                    else property_string += (", EXTENDED_PROPS");
                }
                if ((property & (0x1 << getMultiple(PROPERTY_INDICATE)))== (0x1 << getMultiple(PROPERTY_INDICATE)))
                {
                    if (property_string.isEmpty()) property_string += ("INDICATE");
                    else property_string += (", INDICATE");
                }
                if ((property & (0x1 << getMultiple(PROPERTY_NOTIFY)))== (0x1 << getMultiple(PROPERTY_NOTIFY)))
                {
                    if (property_string.isEmpty()) property_string += ("NOTIFY");
                    else property_string += (", NOTIFY");
                }
                if ((property & (0x1 << getMultiple(PROPERTY_READ)))== (0x1 << getMultiple(PROPERTY_READ)))
                {
                    if (property_string.isEmpty()) property_string += ("READ");
                    else property_string += (", READ");
                }
                if ((property & (0x1 << getMultiple(PROPERTY_SIGNED_WRITE)))== (0x1 << getMultiple(PROPERTY_SIGNED_WRITE)))
                {
                    if (property_string.isEmpty()) property_string += ("SIGNED_WRITE");
                    else property_string += (", SIGNED_WRITE");
                }
                if ((property & (0x1 << getMultiple(PROPERTY_WRITE)))== (0x1 << getMultiple(PROPERTY_WRITE)))
                {
                    if (property_string.isEmpty()) property_string += ("WRITE");
                    else property_string += (", WRITE");
                }
                if ((property & (0x1 << getMultiple(PROPERTY_WRITE_NO_RESPONSE)))== (0x1 << getMultiple(PROPERTY_WRITE_NO_RESPONSE)))
                {
                    if (property_string.isEmpty()) property_string += ("WRITE_NO_RESPONSE");
                    else property_string += (", WRITE_NO_RESPONSE");
                }

                Log.d(TAG, "property_int: " + property);
                Log.d(TAG, "property_string: " + property_string);
                //match_characteristics_items.add(match_characteristics_uuid.toString() + "[" + property_string + "]");
                ListViewBtnItem item = new ListViewBtnItem() ;
                item.setUuid(match_characteristics_uuid.toString()); ;
                item.setProperty(property_string);
                item.setPropertyValue_int(property);
                match_characteristics_items.add(item);

            }

            // log for successful discovery
            Log.d( TAG, "Services discovery is successful" );

            // update ui
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            tv_status_.setText( "Connected (Discovery done)" );

                            if (!matching_characteristics.isEmpty()){
                                // update matched characteristics
                                line_match_characteristics_.setVisibility(View.VISIBLE);
                                match_characteristics_tv.setVisibility(View.VISIBLE);
                                //btn_send_.setVisibility(View.VISIBLE);
                                //btn_read_.setVisibility(View.VISIBLE);
                                //edit_input_send_.setVisibility(View.VISIBLE);
                                //tv_read_.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }).start();


        }

        @Override
        public void onCharacteristicChanged( BluetoothGatt _gatt, BluetoothGattCharacteristic _characteristic ) {
            super.onCharacteristicChanged( _gatt, _characteristic );

            Log.d( TAG, "characteristic changed: " + _characteristic.getUuid().toString() );
            readCharacteristic( _characteristic );
        }

        @Override
        public void onCharacteristicWrite( BluetoothGatt _gatt, BluetoothGattCharacteristic _characteristic, int _status ) {
            super.onCharacteristicWrite( _gatt, _characteristic, _status );
            if( _status == BluetoothGatt.GATT_SUCCESS ) {
                Log.d( TAG, "Characteristic written successfully" );
            } else {
                Log.e( TAG, "Characteristic write unsuccessful, status: " + _status) ;
                disconnectGattServer();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d (TAG, "Characteristic read successfully" );
                readCharacteristic(characteristic);
            } else {
                Log.e( TAG, "Characteristic read unsuccessful, status: " + status);
                // Trying to read from the Time Characteristic? It doesnt have the property or permissions
                // set to allow this. Normally this would be an error and you would want to:
                // disconnectGattServer();
            }
        }

        /*
        Log the value of the characteristic
        @param characteristic
         */
        private void readCharacteristic( BluetoothGattCharacteristic _characteristic ) {

            // update ui
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            byte[] msg= _characteristic.getValue();
                            String msg_string = msg.toString();
                            Log.d(TAG, "read: " + msg_string);
                            View view=match_characteristics_listview.getChildAt(characteristic_item_position_cur_selected);
                            TextView tv_read= view.findViewById(R.id.tv_read);
                            tv_read.setText(msg_string);
                        }
                    });
                }
            }).start();
        }

    }



    /*
    Watch device
     */
    private void watchTargetDevice(){
        tv_status_.setText("Device Watching...");
        Log.d( TAG, "Device Watching..." );

        // check ble adapter and ble enabled
        if (ble_adapter_ == null || !ble_adapter_.isEnabled()) {
            requestEnableBLE();
            tv_status_.setText("Scanning Failed: ble not enabled");
            Log.d( TAG, "Scanning Failed: ble not enabled." );
            return;
        }
        // check if location permission
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            tv_status_.setText("Scanning Failed: no fine location permission");
            Log.d( TAG, "Scanning Failed: no fine location permission." );
            return;
        }


        // setup scan filters for Service UUID
        /*
        List<ScanFilter> filters= new ArrayList<>();
        ScanFilter scan_filter= new ScanFilter.Builder()
           .setServiceUuid( new ParcelUuid( UUID_TDCS_SERVICE ) )
           .build();
        filters.add( scan_filter );
        */


        //// 1) set scan filters for MAC
        List<ScanFilter> filters = new ArrayList<>();

        ScanFilter scan_filter = new ScanFilter.Builder()
                .setDeviceAddress( target_device_mac )
                .build();
        filters.add( scan_filter );


        //// 2) scan settings
        // set low power scan mode
        ScanSettings settings= new ScanSettings.Builder()
                .setScanMode( ScanSettings.SCAN_MODE_LOW_POWER )
                .build();

        // 3) scan callback
        scan_results_= new HashMap<>();
        scan_cb_= new TargetBLEScanCallback( scan_results_ );

        //// now ready to scan
        // start scan
        ble_scanner_.startScan( filters, settings, scan_cb_ );
        // set scanning flag
        is_scanning_= true;
    }

    /*
    Start BLE scan
     */
    private void startScan( View v ) {

        // init rssi log file
        dataManager.init();

        // init tv state
        tv_device_info_.setText("<Device>");
        tv_rssi_.setText("<RSSI>");
        tv_filtered_rssi_.setText("<RSSI>");

        // init scanned items in listview
        items.clear();
        match_characteristics_items.clear();

        tv_status_.setText("Scanning...");
        Log.d( TAG, "Scanning..." );
        // check ble adapter and ble enabled
        if (ble_adapter_ == null || !ble_adapter_.isEnabled()) {
            requestEnableBLE();
            tv_status_.setText("Scanning Failed: ble not enabled");
            Log.d( TAG, "Scanning Failed: ble not enabled." );
            return;
        }
        // check if location permission
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            tv_status_.setText("Scanning Failed: no fine location permission");
            Log.d( TAG, "Scanning Failed: no fine location permission." );
            return;
        }

        // check if file read/write permission
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions( new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            return;
        }
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions( new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            return;
        }

        // init line matched characteristics
        line_match_characteristics_.setVisibility(View.GONE);
        match_characteristics_tv.setVisibility(View.GONE);
        //btn_send_.setVisibility(View.GONE);
        //btn_read_.setVisibility(View.GONE);
        //edit_input_send_.setVisibility(View.GONE);
        //tv_read_.setVisibility(View.GONE);

        // init connection
        disconnectGattServer();

        // setup scan filters for Service UUID
        /*
        List<ScanFilter> filters= new ArrayList<>();
        ScanFilter scan_filter= new ScanFilter.Builder()
           .setServiceUuid( new ParcelUuid( UUID_TDCS_SERVICE ) )
           .build();
        filters.add( scan_filter );
        */


        //// 1) set scan filters for MAC
        List<ScanFilter> filters = new ArrayList<>();

        if (isMacAddressFilterEnabled) {
            ScanFilter scan_filter = new ScanFilter.Builder()
                    .setDeviceAddress( MAC_ADDR_BLE_COMBO )
                    .build();
            filters.add( scan_filter );
        }



        //// 2) scan settings
        // set low power scan mode
        ScanSettings settings= new ScanSettings.Builder()
                .setScanMode( ScanSettings.SCAN_MODE_LOW_POWER )
                .build();

        // 3) scan callback
        scan_results_= new HashMap<>();
        scan_cb_= new BLEScanCallback( scan_results_ );


        //// now ready to scan
        // start scan
        ble_scanner_.startScan( filters, settings, scan_cb_ );
        // set scanning flag
        is_scanning_= true;

        scan_handler_= new Handler();
        scan_handler_.postDelayed( this::stopScan, SCAN_PERIOD );

    }


    /*
    Stop scanning
     */
    private void stopScan() {
        // check pre-conditions
        // stop scanning
        ble_scanner_.stopScan( scan_cb_ );

        // reset flags
        scan_cb_= null;
        is_scanning_= false;
        scan_handler_= null;
        // update the status
        tv_status_.setText( "scanning is complete." );
        Log.d( TAG, "scanning is complete." );

        for( Map.Entry<String, CostumedBluetoothDevice> _result : scan_results_.entrySet() ){
            // get scanned device
            BluetoothDevice device = _result.getValue().get_device();
            // get scanned device MAC address
            String device_address = device.getAddress();
            // get scanned device name
            String device_name =  device.getName();
            // get scanned rssi.
            int rssi = _result.getValue().get_rssi();

            //items.add(device_name + ", " +  device_address + ", " + rssi);
            //items.add(device_address);

            //BleDeviceId deviceId = new BleDeviceId(device_name, device_address);
            items.add(device_address);
            //deviceIdTables.put(device_address, device_name);
        }
    }

    /*
    Stop watching
     */
    private void stopWatch() {

        // check pre-conditions
        // stop scanning
        ble_scanner_.stopScan( scan_cb_ );

        // reset flags
        scan_cb_= null;
        is_scanning_= false;
        watch_handler_= null;
        // update the status
        tv_status_.setText( "Device Watch is complete." );
        Log.d( TAG, "Device Watch is complete." );
    }

    private void config_notification(String _target_characteristic_uuid, boolean notify_on) {

        // find command characteristics from the GATT server
        BluetoothGattCharacteristic cmd_characteristic= BluetoothUtils.findCommandCharacteristic( ble_gatt_ ,_target_characteristic_uuid);
        // disconnect if the characteristic is not found
        if( cmd_characteristic == null ) {
            Log.e( TAG, "Unable to find cmd characteristic: " +  _target_characteristic_uuid);
            //disconnectGattServer();
            return;
        }
        ble_gatt_.setCharacteristicNotification(cmd_characteristic, true);

        BluetoothGattDescriptor descriptor = cmd_characteristic.getDescriptor(UUID.fromString(CCCD_HM_10));
        View view_notify = match_characteristics_listview.getChildAt(characteristic_item_position_cur_selected);
        TextView tv_notify = view_notify.findViewById(R.id.tv_notify);
        if (notify_on) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            tv_notify.setText("Enabled");
            isNotificaionEable = true;
        }
        else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            tv_notify.setText("Disabled");
            isNotificaionEable = false;
        }
        ble_gatt_.writeDescriptor(descriptor);
    }

    /*
    Read Data
     */
    private void readData( String _target_characteristic_uuid ) {

        // find command characteristics from the GATT server
        BluetoothGattCharacteristic cmd_characteristic= BluetoothUtils.findCommandCharacteristic( ble_gatt_ ,_target_characteristic_uuid);
        // disconnect if the characteristic is not found
        if( cmd_characteristic == null ) {
            Log.e( TAG, "Unable to find cmd characteristic: " +  _target_characteristic_uuid);
            //disconnectGattServer();
            return;
        }

        ble_gatt_.readCharacteristic(cmd_characteristic);
    }

    /*
    Send Data
     */
    private void sendData( String _target_characteristic_uuid, String send_msg) {
        // check connection
        if( !connected_ )
        {
            Log.e( TAG, "Failed to sendData due to no connection" );
            return;
        }

        // private ListView match_characteristics_listview;
        // private ArrayList<String> match_characteristics_items;
        // private ArrayAdapter match_characteristics_adapter;

        /*
        ListViewBtnItem targetItem;
        int count, checked;
        count = match_characteristics_adapter.getCount();

        if (count > 0) {
            // 현재 선택된 아이템의 position 획득.
            checked = match_characteristics_listview.getCheckedItemPosition();

            if (checked > -1 && checked < count) {
                // set checked index
                target_characteristic_index = checked;
                // get target device

                targetItem = match_characteristics_items.get(checked);
                TARGET_CHARACTERISTIC_UUID = targetItem.getUuid();
                //TARGET_CHARACTERISTIC_UUID = match_characteristics_items.get(checked);
                //TARGET_CHARACTERISTIC_UUID = TARGET_CHARACTERISTIC_UUID.substring(0, TARGET_CHARACTERISTIC_UUID.lastIndexOf("["));
                Log.d(TAG, "checked index: " + target_characteristic_index);
                Log.d(TAG, "TARGET_CHARACTERISTIC_UUID: " + TARGET_CHARACTERISTIC_UUID);

            }
            else
            {
                Toast toast = Toast.makeText(this.getApplicationContext(),"Select the characteristic.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }
        */

        // find command characteristics from the GATT server
        BluetoothGattCharacteristic cmd_characteristic= BluetoothUtils.findCommandCharacteristic( ble_gatt_ ,_target_characteristic_uuid);
        // disconnect if the characteristic is not found
        if( cmd_characteristic == null ) {
            Log.e( TAG, "Unable to find cmd characteristic: " +  _target_characteristic_uuid);
            //disconnectGattServer();
            return;
        }
        // start stimulation
        _send_data( cmd_characteristic, send_msg);
    }





    /*
    Start stimulation
    @param cmd_characteristic command characteristic instance
    @param program_id stimulation program id
     */
    private void _send_data( BluetoothGattCharacteristic _cmd_characteristic, String send_msg) {

        boolean isByteType = false;
        if (send_msg.charAt(0) == '0' && send_msg.charAt(1) == 'x')
        {
            send_msg = send_msg.substring(2, send_msg.length());
            isByteType = true;
        }

        byte[] cmd_bytes;
        if (isByteType) {
            int send_msg_int = Integer.parseInt(send_msg, 16);
            /*
            if (send_msg_int > 0xffffffff) {
                Toast.makeText(this, "Message is empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            */
            Log.d(TAG, "send_msg_int: " + Integer.toString(send_msg_int));
            cmd_bytes = ByteBuffer.allocate(4).putInt(send_msg_int).array();
        }
        else {
            cmd_bytes = send_msg.getBytes();
        }


        /*
        byte[] cmd_bytes= new byte[20];

        for (int i=0; i<cmd_string.length() && i<20 ; i++){
            cmd_bytes[i] = (byte) cmd_string.charAt(i);
        }
        */

        // set values to the characteristic
        _cmd_characteristic.setValue( cmd_bytes );
        // write the characteristic
        boolean success= ble_gatt_.writeCharacteristic( _cmd_characteristic );
        // check the result
        if( success ) {
            Log.d( TAG, "Write succeeds: " + send_msg);
        }
        else
        {
            Log.e( TAG, "Failed to write command" );
        }
    }




    /*
    Request BLE enable
    */
    private void requestEnableBLE() {
        Intent ble_enable_intent= new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
        startActivityForResult( ble_enable_intent, REQUEST_ENABLE_BT ); // TODO BLE, BT ?????? ???????

    }

    /*
    Request Fine Location permission
     */
    private void requestLocationPermission() {
        requestPermissions( new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION );  // TODO REQEUST_COARSE_LOCATION ????
    }


    private int getMultiple(int in){
        int ret = 0;
        if (in <= 0) {
            return -1;
        }
        while((in = in/2) !=0){

            ret++;
        }
        return ret;
    }


    /*
    BLE Scan Callback class
    */
    private class BLEScanCallback extends ScanCallback {
        private HashMap<String, CostumedBluetoothDevice> cb_scan_results_;
        /*
        Constructor
         */
        BLEScanCallback( HashMap<String, CostumedBluetoothDevice> _scan_results) {
            cb_scan_results_= _scan_results;
        }

        /*
        @Override
        public void onScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d("scan",device.getName() + " RSSI :" + rssi + " Record " + scanRecord);
            //bleList.addDevice(device,rssi);//블루투스 장치를 리스트 어뎁터에 추가
            //bleList.notifyDataSetChanged();//리스트뷰 갱신
        }
        */

        @Override
        public void onScanResult( int _callback_type, ScanResult _result ) {
            Log.d( TAG, "onScanResult" );
            addScanResult( _result );
        }

        @Override
        public void onBatchScanResults( List<ScanResult> _results ) {
            Log.d( TAG, "onBatchScanResults" );

            for( ScanResult result: _results ) {
                addScanResult( result );
            }

            tv_status_.setText("device scan has finished.");

        }

        @Override
        public void onScanFailed( int _error ) {
            Log.e( TAG, "BLE scan failed with code " +_error );
        }

        /*
        Add scan result
         */
        private void addScanResult( ScanResult _result ) {
            // get scanned device
            BluetoothDevice device = _result.getDevice();
            // get scanned device MAC address
            String device_address = device.getAddress();
            // get scanned device name
            String device_name =  device.getName();
            // get scanned rssi.
            int rssi = _result.getRssi();

            // add the device to the result list if not redundant.
            CostumedBluetoothDevice _costumed_ble_device_data = new CostumedBluetoothDevice(device, rssi);
            cb_scan_results_.put( device_address, _costumed_ble_device_data );

        }
    }

    /*
    Target BLE Scan Callback class
    */
    private class TargetBLEScanCallback extends ScanCallback {
        private String TAG = "TargetBLEScanCallback";
        private HashMap<String, CostumedBluetoothDevice> cb_scan_results_;
        /*
        Constructor
         */
        TargetBLEScanCallback( HashMap<String, CostumedBluetoothDevice> _scan_results) {
            cb_scan_results_= _scan_results;
        }

        @Override
        public void onScanResult( int _callback_type, ScanResult _result ) {
            Log.d( TAG, "onScanResult" );
            addWatchResult( _result );
        }

        @Override
        public void onBatchScanResults( List<ScanResult> _results ) {
            Log.d( TAG, "onBatchScanResults" );

            for( ScanResult result: _results ) {
                addWatchResult( result );
            }
        }

        @Override
        public void onScanFailed( int _error ) {
            Log.e( TAG, "BLE scan failed with code " +_error );
        }

        /*
        Add scan result
         */
        private void addWatchResult( ScanResult _result ) {
            // get scanned device
            BluetoothDevice device = _result.getDevice();
            // get scanned device MAC address
            String device_address = device.getAddress();
            // get scanned device name
            String device_name =  device.getName();
            // get scanned rssi.
            //int rssi = _result.getRssi();

            int rssi = _result.getRssi();
            int filteredRSSI = (int) mKalmanRSSI.update(rssi);

            // add the device to the result list if not redundant.
            //CostumedBluetoothDevice _costumed_ble_device_data = new CostumedBluetoothDevice(device, rssi);
            //cb_scan_results_.put( device_address, _costumed_ble_device_data );

            // update device info
            Log.d(TAG, "Watching device info: " + device_address + "/ rssi: " + Integer.toString(rawRSSI));

            // listview 갱신.
            //adapter.notifyDataSetChanged();
            //listview.setAdapter(adapter);

            tv_device_info_.setText(device_name + " (" + device_address + ")");
            tv_rssi_.setText(Integer.toString(rssi));
            tv_filtered_rssi_.setText(Integer.toString(filteredRSSI));

            // save rssi log
            dataManager.fileSaveInExtenalStorage(raw_rssi_log_fileName, Integer.toString(rssi));
            dataManager.fileSaveInExtenalStorage(filtered_rssi_log_fileName, Integer.toString(filteredRSSI));

        }
    }

}

