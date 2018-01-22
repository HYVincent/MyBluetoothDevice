package com.example.vincent.mybluetoothdevice;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vincent.mybluetoothdevice.bluetooth.BluetoothEntity;
import com.example.vincent.mybluetoothdevice.bluetooth.BluetoothUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothDevice";
    private List<BluetoothEntity> bluetoothDevices = new ArrayList<>();
    private RecyclerView rlv;
    private BlueListAdapter adapter;

    private TextView tvStatus;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rlv = findViewById(R.id.rlv_list);
        initRecycleView();
        tvStatus = findViewById(R.id.textView);
        findViewById(R.id.textView2).setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View view) {
                BluetoothUtils.getBluetoothUtils(MainActivity.this).openBluetooth();
            }
        });
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothUtils.getBluetoothUtils(MainActivity.this).closeBluetooth();
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if(bluetoothDevices != null && bluetoothDevices.size()>0){
                    bluetoothDevices.clear();
                    adapter.setDatas(bluetoothDevices);
                }
                BluetoothUtils.getBluetoothUtils(MainActivity.this).searchBluetoothDevice(new BluetoothUtils.BluetoothScanResultListener() {
                    @Override
                    public void scanResult(List<BluetoothDevice> bluetoothDevicess) {
                        getDatas(bluetoothDevicess);
                    }
                });
            }
        });
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                BluetoothUtils.getBluetoothUtils(MainActivity.this).stopBluetoothScan();
            }
        });

        BluetoothUtils.getBluetoothUtils(MainActivity.this).register(new BluetoothUtils.BluetoothStatusChangeListener() {
            @Override
            public void bluetoothStatus(int status) {
                switch (status) {
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "onReceive: 没有扫描到设备");
                        tvStatus.setText("没有扫描到设备");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "onReceive: 可连接。。。");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "onReceive: 可被发现...");
                        tvStatus.setText("可被发现");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        tvStatus.setText("正在打开蓝牙..");
                        Log.d(TAG, "onReceive: 正在打开蓝牙。。");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        tvStatus.setText("蓝牙打开");
                        Log.d(TAG, "onReceive: 蓝牙打开");
                        Log.e("TAG", "STATE_ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        tvStatus.setText("蓝牙正在关闭....");
                        Log.d(TAG, "onReceive: 蓝牙正在关闭..");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        tvStatus.setText("蓝牙关闭");
                        Log.d(TAG, "onReceive: 蓝牙关闭");
                        break;
                    case BluetoothUtils.BLUETOOTH_STATUS_CONNECTING:
                        Log.d(TAG, "bluetoothStatus: 正在连接蓝牙...");
                        refreshView(connectPosition,BluetoothUtils.BLUETOOTH_STATUS_CONNECTING);
                        break;
                    case BluetoothUtils.BLUETOOTH_STATUS_CONNECT_FAIL:
                        refreshView(connectPosition,BluetoothUtils.BLUETOOTH_STATUS_CONNECT_FAIL);
                        Log.d(TAG, "bluetoothStatus: 蓝牙连接失败");
                        break;
                    case BluetoothUtils.BLUETOOTH_STATUS_CONNECT_SUCCESS:
                        refreshView(connectPosition,BluetoothUtils.BLUETOOTH_STATUS_CONNECT_SUCCESS);
                        Log.d(TAG, "bluetoothStatus: 蓝牙连接成功");
                        break;
                    case BluetoothUtils.BLUETOOTH_STATUS_CONNECT_RETRY:
                        refreshView(connectPosition,BluetoothUtils.BLUETOOTH_STATUS_CONNECT_RETRY);
                        break;
                    case BluetoothUtils.BLUETOOTH_STATUS_START_SCAN:
                        Log.d(TAG, "bluetoothStatus: 开始扫描蓝牙..");
                        break;
                    case BluetoothUtils.BLUETOOTH_STATUS_STOP_SCAN:
                        Log.d(TAG, "bluetoothStatus: 蓝牙扫描停止。。");
                        if(bluetoothDevices.size() == 0){
                            toastMsg("没有找到设备..");
                        }else {
                            toastMsg("停止扫描");
                        }
                        break;
                    default:break;
                }
            }
        });
    }

    private void getDatas(List<BluetoothDevice> bluetoothDevicess) {
        bluetoothDevices.clear();
        for (BluetoothDevice bluetoothDevice: bluetoothDevicess){
            BluetoothEntity entity = new BluetoothEntity();
            entity.setAddress(bluetoothDevice.getAddress());
            entity.setName(bluetoothDevice.getName());
            bluetoothDevices.add(entity);
        }
        adapter.setDatas(bluetoothDevices);
    }

    private int connectPosition = -1;

    private void refreshView(final int position,final int status){
        if(position != -1){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BluetoothEntity device = bluetoothDevices.get(position);
                    device.setStatus(status);
                    adapter.notifyItemChanged(position);
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothUtils.getBluetoothUtils(MainActivity.this).unRegister();
    }

    private void initRecycleView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rlv.setLayoutManager(linearLayoutManager);
        adapter = new BlueListAdapter(MainActivity.this);
        adapter.setDatas(bluetoothDevices);
        rlv.setAdapter(adapter);
        adapter.setListItemOnClickListener(new BlueListAdapter.BlueListItemOnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onItemClick(View view, int position) {
                connectPosition = position;
                BluetoothUtils.getBluetoothUtils(MainActivity.this).connectDevice(bluetoothDevices.get(position).getAddress());
            }
        });
    }

    private void toastMsg(String msg){
        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
    }




}
