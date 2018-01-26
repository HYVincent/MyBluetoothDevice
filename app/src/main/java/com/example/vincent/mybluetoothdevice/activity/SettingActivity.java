package com.example.vincent.mybluetoothdevice.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.vincent.mybluetoothdevice.base.App;
import com.example.vincent.mybluetoothdevice.bluetooth.BleControl;
import com.example.vincent.mybluetoothdevice.config.Config;
import com.example.vincent.mybluetoothdevice.R;
import com.example.vincent.mybluetoothdevice.dialog.InputContentDialog;

/**
 * @author Administrator QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice
 * @class describe
 * @date 2018/1/26 11:12
 */

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        findViewById(R.id.btn_default).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BleControl.getInstance().isConnect()){
                    toast("恢复默认之前先断开设备");
                }else {
                    App.getSpUtil().putString(Config.SERVICE_UUID,"");
                    App.getSpUtil().putString(Config.SERVICE_UUID,"");
                    App.getSpUtil().putString(Config.SERVICE_UUID,"");
                    App.getSpUtil().putString(Config.SERVICE_UUID,"");
                    toast("清除成功");
                }
            }
        });
        findViewById(R.id.btn_service_uuid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BleControl.getInstance().isConnect()){
                    toast("设置之前先断开设备");
                }else {
                    setServiceUUID();
                }

            }
        });
        findViewById(R.id.btn_notify_uuid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BleControl.getInstance().isConnect()){
                    toast("设置之前先断开设备");
                }else {
                    setNotifyUUID();
                }
            }
        });
        findViewById(R.id.btn_write_uuid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BleControl.getInstance().isConnect()){
                    toast("设置之前先断开设备");
                }else {
                    setWriteUUID();
                }
            }
        });
    }

    private void setWriteUUID() {
        InputContentDialog dialog = new InputContentDialog(SettingActivity.this);
        dialog.setCheckNull(true,"请输入设备Write UUID")
                .setInputContentListener(new InputContentDialog.InputContentListener() {
                    @Override
                    public void input(String content) {
                        App.getSpUtil().putString(Config.UUID_WRITER,content);
                    }
                })
                .show();
    }

    private void setNotifyUUID() {
        InputContentDialog dialog = new InputContentDialog(SettingActivity.this);
        dialog.setCheckNull(true,"请输入设备Notify UUID")
                .setInputContentListener(new InputContentDialog.InputContentListener() {
                    @Override
                    public void input(String content) {
                        App.getSpUtil().putString(Config.UUID_NOTIFY,content);
                    }
                })
                .show();
    }

    /**
     * 设置ServiceUUID
     */
    private void setServiceUUID() {
        InputContentDialog dialog = new InputContentDialog(SettingActivity.this);
        dialog.setCheckNull(true,"请输入设备Service UUID")
                .setInputContentListener(new InputContentDialog.InputContentListener() {
                    @Override
                    public void input(String content) {
                        App.getSpUtil().putString(Config.SERVICE_UUID,content);
                    }
                })
                .show();

    }

    private void toast(String str) {
        Toast.makeText(SettingActivity.this,str,Toast.LENGTH_SHORT).show();
    }
}
