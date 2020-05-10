package com.example.ble_test;

import android.bluetooth.BluetoothDevice;

public class CostumedBluetoothDevice {

    private BluetoothDevice m_device;
    private int m_rssi;
    public CostumedBluetoothDevice(BluetoothDevice device, int rssi) {
        m_device = device;
        m_rssi = rssi;
    }

    public BluetoothDevice get_device() {
        return m_device;
    }

    public int get_rssi(){
        return m_rssi;
    }
}
