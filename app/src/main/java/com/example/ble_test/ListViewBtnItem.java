package com.example.ble_test;

import android.graphics.drawable.Drawable;

public class ListViewBtnItem {
    private Drawable iconDrawable ;
    private String property_string ;
    private int property_int ;

    private String uuid_string ;
    private boolean isProperty;

    /*
    int 형으로.. 나눠서..
     */
    public static final int PROPERTY_READ = 0x02;
    public static final int PROPERTY_WRITE = 0x08;
    public static final int PROPERTY_NOTIFY = 0x10;
    public static final int PROPERTY_INDICATE = 0x20;


    public void setIcon(Drawable icon) {
        iconDrawable = icon ;
    }
    public void setUuid(String _uuid_string) {
        uuid_string = _uuid_string ;
    }
    public void setProperty(String _property_string) {
        property_string = _property_string ;
    }
    public void setPropertyValue_int(int _property_int) {
        property_int = _property_int;
    }

    public Drawable getIcon() {
        return this.iconDrawable ;
    }
    public String getUuid() {
        return this.uuid_string ;
    }
    public String getProperty() {
        return this.property_string ;
    }
    public int getPropertyValue_int() {
        return this.property_int ;
    }

}