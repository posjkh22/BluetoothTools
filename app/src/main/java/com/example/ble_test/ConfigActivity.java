package com.example.ble_test;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;
import static com.example.ble_test.Constants.scan_time_milisecond;


public class ConfigActivity extends AppCompatActivity {

    private Button btn_config_kalman_q_value_;
    private Button btn_config_kalman_r_value_;
    private Button btn_config_scan_time_;

    private EditText et_config_kalman_q_value_;
    private EditText et_config_kalman_r_value_;
    private EditText et_config_scan_time_;

    private TextView tv_config_kalman_q_value_;
    private TextView tv_config_kalman_r_value_;
    private TextView tv_config_scan_time_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);


        btn_config_kalman_q_value_ = (Button) findViewById(R.id.btn_config_kalman_q_value);
        btn_config_kalman_r_value_ = (Button) findViewById(R.id.btn_config_kalman_r_value);
        btn_config_scan_time_ = (Button) findViewById(R.id.btn_config_scan_time);

        et_config_kalman_q_value_ = (EditText) findViewById(R.id.et_config_kalman_q_value);
        et_config_kalman_r_value_ = (EditText) findViewById(R.id.et_config_kalman_r_value);
        et_config_scan_time_ = (EditText) findViewById(R.id.et_config_scan_time);

        tv_config_kalman_q_value_ = (TextView) findViewById(R.id.tv_config_kalman_q_value);
        tv_config_kalman_r_value_ = (TextView) findViewById(R.id.tv_config_kalman_r_value);
        tv_config_scan_time_ = (TextView) findViewById(R.id.tv_config_scan_time);

        tv_config_kalman_q_value_.setText(Double.toString(KalmanFilter.getQvalue()));
        tv_config_kalman_r_value_.setText(Double.toString(KalmanFilter.getRvalue()));
        tv_config_scan_time_.setText(Integer.toString(scan_time_milisecond));


        btn_config_kalman_q_value_.setOnClickListener( (v) -> { setKalmanQvalue(v); });
        btn_config_kalman_r_value_.setOnClickListener( (v) -> { setKalmanRvalue(v); });
        btn_config_scan_time_.setOnClickListener( (v) -> { setScanTime(v); });

    }

    public void setKalmanQvalue(View v) {
        if (et_config_kalman_q_value_.isEnabled()) {
            String q_value = et_config_kalman_q_value_.getText().toString();
            if (q_value.matches("") == false) {
                tv_config_kalman_q_value_.setText(q_value);
                KalmanFilter.setQvalue(Double.parseDouble(q_value));
            }
            else {
                Toast.makeText(this, "Please, input the q value.", Toast.LENGTH_SHORT).show();
                return;
            }
            et_config_kalman_q_value_.setEnabled(false);
        }
        else {
            et_config_kalman_q_value_.setText("");
            et_config_kalman_q_value_.setEnabled(true);
        }
    }

    public void setKalmanRvalue(View v) {
        if (et_config_kalman_r_value_.isEnabled()) {
            String r_value = et_config_kalman_r_value_.getText().toString();
            if (r_value.matches("") == false) {
                tv_config_kalman_r_value_.setText(r_value);
                KalmanFilter.setRvalue(Double.parseDouble(r_value));
            }
            else {
                Toast.makeText(this, "Please, input the q value.", Toast.LENGTH_SHORT).show();
                return;
            }
            et_config_kalman_r_value_.setEnabled(false);
        }
        else {
            et_config_kalman_r_value_.setText("");
            et_config_kalman_r_value_.setEnabled(true);
        }
    }


    public void setScanTime(View v) {
        if (et_config_scan_time_.isEnabled()) {
            String scan_time = et_config_scan_time_.getText().toString();
            if (scan_time.matches("") == false) {
                scan_time_milisecond = Integer.parseInt(scan_time);
                tv_config_scan_time_.setText(scan_time);
            }
            else {
                Toast.makeText(this, "Please, input the scan time.", Toast.LENGTH_SHORT).show();
                return;
            }
            et_config_scan_time_.setEnabled(false);
        }
        else {
            et_config_scan_time_.setText("");
            et_config_scan_time_.setEnabled(true);
        }
    }

}
