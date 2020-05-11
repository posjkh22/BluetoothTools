package com.example.ble_test;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import androidx.core.content.ContextCompat;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.example.ble_test.Constants.filtered_rssi_log_fileName;
import static com.example.ble_test.Constants.raw_rssi_log_fileName;

public class DataManager {

    private String folder_name = "BluetoothScan";
    private Context mContext;

    public DataManager(Context context){

        mContext = context;
    }


    public void init(){
        initRSSILogFiles();
    }

    public void initRSSILogFiles(){

        // init rssi log file
        String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String rootPath = storagePath + "/" + folder_name + "/";

        File rssi_log_file = new File(rootPath + raw_rssi_log_fileName + ".txt");
        File filtered_rssi_log_file = new File(rootPath + filtered_rssi_log_fileName + ".txt");

        try {
            int permissionCheck = ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                rssi_log_file.delete();
                filtered_rssi_log_file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void fileSaveInExtenalStorage(String filename, String text) {

        // 출처: https://stackoverflow.com/questions/34651771/file-createnewfile-method-throws-exception-in-android-m
        String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String rootPath = storagePath + "/" + folder_name + "/";
        String fileName = filename;

        File root = new File(rootPath);
        if(!root.mkdirs()) {
            ;
            //Log.i("Test", "This path is already exist: " + root.getAbsolutePath());
        }

        File file = new File(rootPath + fileName + ".txt");
        try {
            int permissionCheck = ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                if (!file.createNewFile()) {
                    ;
                    //Log.i("Test", "This file is already exist: " + file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
            //buf.append(MakeDetailItemToText.makingText(context, text));
            buf.write(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    public static void save(Context context) {

        String filename = "name";
        try {
            FileOutputStream fos = context.openFileOutput(filename + ".txt", Context.MODE_APPEND);
            PrintWriter out = new PrintWriter(fos);
            out.println(info);
            out.close();
            Log.i("TAG", "complete to file save");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static File load(String name, Context context) {
        File file = null;
        try {
            file = context.getFileStreamPath(name + ".txt");
            Log.i("TAG", "complete to file load");
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
    */

}
