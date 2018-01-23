package com.example.vincent.mybluetoothdevice;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vincent.mybluetoothdevice.bluetooth.BleControl;
import com.example.vincent.mybluetoothdevice.bluetooth.BluetoothEntity;
import com.example.vincent.mybluetoothdevice.utils.HexUtil;
import com.example.vincent.mybluetoothdevice.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothDevice";
    private List<BluetoothEntity> bluetoothDevices = new ArrayList<>();
    private List<DataEntity> dataEntities = new ArrayList<>();
    private RecyclerView rlv;
    private RecyclerView rlvLog;
    private BlueListAdapter adapter;
    private MsgAdapter msgAdapter;
    private EditText etInput;
    private Button btnSend;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BleControl.getInstance().initBle(MainActivity.this);

        rlv = findViewById(R.id.rlv_list);
        rlvLog = findViewById(R.id.rlv_list_data);
        initRecycleView();
        initRecycleViewLog();
        etInput = findViewById(R.id.et_input);
        btnSend = findViewById(R.id.btn_send);
        if(BleControl.getInstance().isEnable()){
            addLogs(2,"蓝牙已经打开");
        }else {
            addLogs(2,"蓝牙已关闭");
        }
        findViewById(R.id.textView2).setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View view) {
                BleControl.getInstance().openBle();
            }
        });
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BleControl.getInstance().closeBle();
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
                BleControl.getInstance().scanBle(new BleControl.BleScanResultListener() {
                    @Override
                    public void onScanResult(List<BluetoothDevice> bluetoothDevices) {
                        getDatas(bluetoothDevices);
                    }
                });
            }
        });
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                BleControl.getInstance().stopBleScan();
            }
        });

        BleControl.getInstance().registerNotification(MainActivity.this);
        BleControl.getInstance().setStatusChangeNotificationListener(new BleControl.BleStatusChangeNotificationListener() {
            @Override
            public void onChangeStatus(int status) {
                checkStatus(status);
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData(etInput.getText().toString());
            }
        });
        BleControl.getInstance().setDataChangeNotificationListener(new BleControl.BleDataChangeNotificationListener() {
            @Override
            public void onDatas(byte[] datas) {
                addLogs(0,HexUtil.bytesToHexString(datas));
            }
        });
    }

    private LinearLayoutManager linearLayoutManager;
    private void initRecycleViewLog() {
        linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rlvLog.setLayoutManager(linearLayoutManager);
        msgAdapter = new MsgAdapter(MainActivity.this);
        msgAdapter.setData(dataEntities);
        rlvLog.setAdapter(msgAdapter);
    }


    /**
     * 向蓝牙设备发送数据
     * @param s
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void sendData(String s) {
        if(TextUtils.isEmpty(s)){
            toastMsg("未输入");
            return;
        }
        BleControl.getInstance().writeBuffer(s);
        addLogs(1,s);
    }


    private void checkStatus(int status) {
        switch (status) {
            case BluetoothAdapter.SCAN_MODE_NONE:
                addLogs(2,"没有扫描到设备");
                break;
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                Log.d(TAG, "onReceive: 可连接。。。");
                break;
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                Log.d(TAG, "onReceive: 可被发现...");
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                addLogs(2,"正在打开蓝牙。。");
                Log.d(TAG, "onReceive: ");
                break;
            case BluetoothAdapter.STATE_ON:
                addLogs(2,"蓝牙打开。。");
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                addLogs(2,"蓝牙正在关闭。。");
                break;
            case BluetoothAdapter.STATE_OFF:
                addLogs(2,"蓝牙关闭。。");
                break;
            case BleControl.BLE_STATUS_SCAN_CONNECTING:
                addLogs(2,"正在连接蓝牙。。");
                refreshView(connectPosition,BleControl.BLE_STATUS_SCAN_CONNECTING);
                break;
            case BleControl.BLE_STATUS_CONNECT_FAILE:
                refreshView(connectPosition,BleControl.BLE_STATUS_CONNECT_FAILE);
                addLogs(2,"蓝牙连接失败。。");
                break;
            case BleControl.BLE_STATUS_CONNECT_SUCCESS:
                refreshView(connectPosition,BleControl.BLE_STATUS_CONNECT_SUCCESS);
                addLogs(2,"蓝牙连接成功。。");
                break;
            case BleControl.BLE_STATUS_BREAK_RECONNECTION:
                refreshView(connectPosition,BleControl.BLE_STATUS_BREAK_RECONNECTION);
                break;
            case BleControl.BLE_STATUS_CONNECT_TIME_OUT:
                refreshView(connectPosition,BleControl.BLE_STATUS_CONNECT_TIME_OUT);
                break;
            case BleControl.BLE_STATUS_SEND_DATE_FAILE:
                toastMsg("数据发送失败");
                addLogs(2,"数据发送失败。。");
                break;
            case BleControl.BLE_STATUS_SEND_DATE_SUCCESS:
                toastMsg("数据发送成功");
                addLogs(2,"数据发送成功");
                break;
            case BleControl.BLE_STATUS_SCAN_START:
                addLogs(2,"开始扫描蓝牙..");
                Log.d(TAG, "bluetoothStatus: 开始扫描蓝牙..");
                break;
            case BleControl.BLE_STATUS_SCAN_STOP:
                addLogs(2,"蓝牙扫描停止..");
                Log.d(TAG, "bluetoothStatus: 蓝牙扫描停止。。");
                if(bluetoothDevices.size() == 0){
                    addLogs(2,"没有找到设备..");
                }else {
                    addLogs(2,"停止扫描,已找到"+bluetoothDevices.size()+"个设备");
                }
                break;
            default:break;
        }
    }

    private void addLogs(int type,String content){
        DataEntity dataEntity = new DataEntity();
//                dataEntity.setMsg(StringUtils.byteToString(datas));
        dataEntity.setMsg(content);
        dataEntity.setType(type);
        dataEntity.setTime(System.currentTimeMillis());
        dataEntities.add(dataEntity);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgAdapter.setData(dataEntities);
                rlvLog.smoothScrollBy(dataEntities.size(),500);
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
                    if(position > bluetoothDevices.size()){
                        return;
                    }
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
        BleControl.getInstance().unRegister();
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
                BleControl.getInstance().connect(bluetoothDevices.get(position).getAddress(),true);
            }
        });
    }

    private void toastMsg(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,msg,Toast.LENGTH_LONG).show();
            }
        });
    }
}
