package com.example.ble_test;

public class BleDeviceId {
    private String device_name;
    private String device_mac;
    public BleDeviceId(String _deivce_name, String _device_mac) {
        device_mac = _device_mac;
        device_name = _deivce_name;
    }

    public String getDeviceMac() {
        return device_mac;
    }

    public String getDeviceName() {
        return device_name;
    }
}
