package com.example.ble_test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_BROADCAST;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_INDICATE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;

public class ListViewBtnAdapter extends ArrayAdapter implements View.OnClickListener  {

    final public static int BTN_SEND_INDEX = 0;
    final public static int BTN_READ_INDEX = 1;
    final public static int BTN_NOTIFY_INDEX = 2;
    final public static int BTN_INDICATE_INDEX = 3;
    final public static int BTN_INDEX_MAX = 4;

    // 버튼 클릭 이벤트를 위한 Listener 인터페이스 정의.
    public interface ListBtnClickListener {
        void onListBtnClick(int position) ;
    }

    // 생성자로부터 전달된 resource id 값을 저장.
    int resourceId ;
    // 생성자로부터 전달된 ListBtnClickListener  저장.
    private ListBtnClickListener listBtnClickListener ;


    // ListViewBtnAdapter 생성자. 마지막에 ListBtnClickListener 추가.
    ListViewBtnAdapter(Context context, int resource, ArrayList<ListViewBtnItem> list, ListBtnClickListener clickListener) {
        super(context, resource, list) ;

        // resource id 값 복사. (super로 전달된 resource를 참조할 방법이 없음.)
        this.resourceId = resource ;

        this.listBtnClickListener = clickListener ;
    }

    // 새롭게 만든 Layout을 위한 View를 생성하는 코드
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position ;
        final Context context = parent.getContext();

        // 생성자로부터 저장된 resourceId(listview_btn_item)에 해당하는 Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(this.resourceId/*R.layout.listview_btn_item*/, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)로부터 위젯에 대한 참조 획득
        final ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1);
        final TextView property = (TextView) convertView.findViewById(R.id.property);
        final TextView uuid = (TextView) convertView.findViewById(R.id.uuid);


        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        final ListViewBtnItem listViewItem = (ListViewBtnItem) getItem(position);

        // 아이템 내 각 위젯에 데이터 반영
        iconImageView.setImageDrawable(listViewItem.getIcon());
        uuid.setText(listViewItem.getUuid());
        property.setText(listViewItem.getProperty());


        // btn_send
        Button _btn_send = (Button) convertView.findViewById(R.id.btn_send);
        _btn_send.setTag(position * BTN_INDEX_MAX + BTN_SEND_INDEX);
        _btn_send.setOnClickListener(this);
        _btn_send.setVisibility(View.GONE);

        // et_send
        EditText _et_send = (EditText) convertView.findViewById(R.id.et_send);
        _et_send.setVisibility(View.GONE);

        // btn_read
        Button _btn_read = (Button) convertView.findViewById(R.id.btn_read);
        _btn_read.setTag(position * BTN_INDEX_MAX + BTN_READ_INDEX);
        _btn_read.setOnClickListener(this);
        _btn_read.setVisibility(View.GONE);

        // tv_read
        TextView _tv_read = (TextView) convertView.findViewById(R.id.tv_read);
        _tv_read.setVisibility(View.GONE);

        // btn_notify
        Button _btn_notify = (Button) convertView.findViewById(R.id.btn_notify);
        _btn_notify.setTag(position * BTN_INDEX_MAX + BTN_NOTIFY_INDEX);
        _btn_notify.setOnClickListener(this);
        _btn_notify.setVisibility(View.GONE);

        // tv_notify
        TextView _tv_notify = (TextView) convertView.findViewById(R.id.tv_notify);
        _tv_notify.setVisibility(View.GONE);

        // btn_indicate
        Button _btn_indicate = (Button) convertView.findViewById(R.id.btn_indicate);
        _btn_indicate.setTag(position * BTN_INDEX_MAX + BTN_INDICATE_INDEX);
        _btn_indicate.setOnClickListener(this);
        _btn_indicate.setVisibility(View.GONE);

        // tv_indicate
        TextView _tv_indicate = (TextView) convertView.findViewById(R.id.tv_indicate);
        _tv_indicate.setVisibility(View.GONE);

        // Visibility config as to property
        iconImageView.setVisibility(View.GONE);

        int property_int = listViewItem.getPropertyValue_int();
        if ((property_int & (0x1 << getMultiple(PROPERTY_BROADCAST)))== (0x1 << getMultiple(PROPERTY_BROADCAST)))
        {
            ;
        }
        if ((property_int & (0x1 << getMultiple(PROPERTY_EXTENDED_PROPS)))== (0x1 << getMultiple(PROPERTY_EXTENDED_PROPS)))
        {
            ;
        }
        if ((property_int & (0x1 << getMultiple(PROPERTY_INDICATE)))== (0x1 << getMultiple(PROPERTY_INDICATE)))
        {
            _btn_indicate.setVisibility(View.VISIBLE);
            _tv_indicate.setVisibility(View.VISIBLE);
        }
        if ((property_int & (0x1 << getMultiple(PROPERTY_NOTIFY)))== (0x1 << getMultiple(PROPERTY_NOTIFY)))
        {
            _btn_notify.setVisibility(View.VISIBLE);
            _tv_notify.setVisibility(View.VISIBLE);
        }
        if ((property_int & (0x1 << getMultiple(PROPERTY_READ)))== (0x1 << getMultiple(PROPERTY_READ)))
        {
            _btn_read.setVisibility(View.VISIBLE);
            _tv_read.setVisibility(View.VISIBLE);
        }
        if ((property_int & (0x1 << getMultiple(PROPERTY_SIGNED_WRITE)))== (0x1 << getMultiple(PROPERTY_SIGNED_WRITE)))
        {
            ;
        }
        if ((property_int & (0x1 << getMultiple(PROPERTY_WRITE)))== (0x1 << getMultiple(PROPERTY_WRITE)))
        {
            _btn_send.setVisibility(View.VISIBLE);
            _et_send.setVisibility(View.VISIBLE);
        }
        if ((property_int & (0x1 << getMultiple(PROPERTY_WRITE_NO_RESPONSE)))== (0x1 << getMultiple(PROPERTY_WRITE_NO_RESPONSE)))
        {
            ;
        }

        return convertView;
    }

    // button2가 눌려졌을 때 실행되는 onClick함수.
    public void onClick(View v) {
        // ListBtnClickListener(MainActivity)의 onListBtnClick() 함수 호출.
        if (this.listBtnClickListener != null) {
            this.listBtnClickListener.onListBtnClick((int)v.getTag()) ;
        }
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

}
