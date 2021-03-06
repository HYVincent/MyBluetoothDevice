package com.example.vincent.mybluetoothdevice.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.vincent.mybluetoothdevice.R;
import com.example.vincent.mybluetoothdevice.bluetooth.BleControl;
import com.example.vincent.mybluetoothdevice.bluetooth.BluetoothEntity;
import com.example.vincent.mybluetoothdevice.entity.DataEntity;
import com.example.vincent.mybluetoothdevice.utils.HexUtil;
import com.example.vincent.mybluetoothdevice.utils.JNIUtils;
import com.example.vincent.mybluetoothdevice.utils.MainHandler;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private static final String TAG = "MainActivity";
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
            addLogs(2,"蓝牙已经打开!");
        }else {
            addLogs(2,"蓝牙已关闭!");
        }
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                checkPermissions();
            }
        });
        findViewById(R.id.btn_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,SettingActivity.class));
            }
        });
        BleControl.getInstance().registerNotification(MainActivity.this);
        BleControl.getInstance().setStatusChangeNotificationListener(new BleControl.BleStatusChangeNotificationListener() {
            @Override
            public void onChangeStatus(int status) {
                checkStatus(status);
            }
        });
        findViewById(R.id.btn_clear_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataEntities.clear();
                msgAdapter.setData(dataEntities);
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               String tag =  HexUtil.bytesToHexString(JNIUtils.getInstance().sendSystemTime0x13(1,1,1,1,1,1));
                Log.d(TAG, "Test: "+tag);
                sendData(tag);
//                sendData(etInput.getText().toString());
               /* String tag0x17 = HexUtil.bytesToHexString(JNIUtils.getInstance().
                        sendSetSystemStatusWithInfo0x17(0,0,0,0));
                Log.d(TAG, "onClick: 0x17:"+tag0x17);
                sendData(tag0x17);*/

               /*String tag0x15 = HexUtil.bytesToHexString(JNIUtils.getInstance().sendAlertSwitch0x15WithInfo0x15(0,0,0,0));
                Log.d(TAG, "onClick: 0x15:"+tag0x15);
                sendData(tag0x15);*/

                /*String tag0x13 = HexUtil.bytesToHexString(JNIUtils.getInstance().sendSystemTime0x13(18,1,26,17,17,40));
                Log.d(TAG, "onClick: 0x15:"+tag0x13);
                sendData(tag0x13);*/

               /* String tag0x14 = HexUtil.bytesToHexString(JNIUtils.getInstance().getAlertStatus0x14());
                sendData(tag0x14);*/

                /*byte[] datas = new byte[8];
                datas[0] = 0x7f;
                datas[1] = 0x02;
                datas[2] = 0x00;
                datas[3] = (byte) 0x86;
                datas[4] = 0x00;
                datas[5] = 0x00;
                datas[6] = (byte) 0x86;
                datas[7] = (byte) 0xf7;
                Log.d(TAG, "onDatas: 解析数据-->"+HexUtil.bytesToHexString(datas)+" "+datas.length);
                JNIUtils.getInstance().test(datas,8);*/
            }
        });
        BleControl.getInstance().setDataChangeNotificationListener(new BleControl.BleDataChangeNotificationListener() {
            @Override
            public void onDatas(byte[] datas) {
                addLogs(0,HexUtil.bytesToHexString(datas));
                Log.d(TAG, "onDatas: 解析数据-->"+HexUtil.bytesToHexString(datas));
                JNIUtils.getInstance().judgeDataType(datas);
               /* if(datas[0] == 0x7f) {
                    System.out.println("-----------");
                    switch (datas[3]){
                        case (byte) 0x86:
                            String toBinaryString = AnalysisDataUtils.analysis0x86(datas);
                            Log.d(TAG, "onDatas: toBinaryString= " + toBinaryString);
                            break;
                        default:
                            Log.d(TAG, "onDatas: default...");
                            break;
                    }
                }else {
                    Log.d(TAG, "onDatas: 该数据不是0x7f开头的数据..");
                }*/
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
            addLogs(2,"请输入指令!");
            return;
        }
//        String str = HexUtil.bytesToHexString(JNIUtils.getInstance().setSystemTime(1,1,1,1,1,1));
//        String str = HexUtil.bytesToHexString(JNIUtils.getInstance().getSystemFunction());
        BleControl.getInstance().writeBuffer(s);
        addLogs(1,s);
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void checkStatus(int status) {
        switch (status) {
            case BluetoothAdapter.SCAN_MODE_NONE:
                addLogs(2,"没有扫描到设备");
                break;
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                Log.d(TAG, "onReceive: 可连接...");
                break;
            case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                Log.d(TAG, "onReceive: 可被发现...");
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                addLogs(2,"正在打开蓝牙...");
                Log.d(TAG, "onReceive: ");
                break;
            case BluetoothAdapter.STATE_ON:
                addLogs(2,"蓝牙已打开!");
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                addLogs(2,"蓝牙正在关闭...");
                break;
            case BluetoothAdapter.STATE_OFF:
                addLogs(2,"蓝牙已关闭!");
                JNIUtils.getInstance().clearTotalData();
                break;
            case BleControl.BLE_STATUS_SCAN_CONNECTING:
                addLogs(2,"正在连接蓝牙...");
                refreshView(connectPosition,BleControl.BLE_STATUS_SCAN_CONNECTING);
                break;
            case BleControl.BLE_STATUS_CONNECT_FAILE:
                refreshView(connectPosition,BleControl.BLE_STATUS_CONNECT_FAILE);
                addLogs(2,"蓝牙连接失败!");
                JNIUtils.getInstance().clearTotalData();
                break;
            case BleControl.BLE_STATUS_CONNECT_SUCCESS:
                refreshView(connectPosition,BleControl.BLE_STATUS_CONNECT_SUCCESS);
                addLogs(2,"蓝牙连接成功!");
                break;
            case BleControl.BLE_CONNECT_STATUS_UNBLOCKED:
                addLogs(2,"已创建可通信交流通道，可正常发送数据!");
                addLogs(2,"正在获取系统功能..");
                getSystemFunction();
                break;
            case BleControl.BLE_CONNECT_STATUS_ACCEPT_FAIL:
                addLogs(2,"创建可通信交流通道失败，数据发送将会失败");
                break;
            case BleControl.BLE_STATUS_CONNECT_NO_FOUND_SERVICE_UUID:
                addLogs(2,"找不到设备的ServiceUUID,请到设置中重新设置设备的ServiceUUID!");
                break;
            case BleControl.BLE_STATUS_BREAK_RECONNECTION:
                addLogs(2,"设备已断开，正在重新连接..");
                refreshView(connectPosition,BleControl.BLE_STATUS_BREAK_RECONNECTION);
                break;
            case BleControl.BLE_STATUS_CONNECT_TIME_OUT:
                addLogs(2,"设备连接超时!");
                refreshView(connectPosition,BleControl.BLE_STATUS_CONNECT_TIME_OUT);
                break;
            case BleControl.BLE_STATUS_SEND_DATE_FAILE:
                addLogs(2,"数据发送失败!");
                break;
            case BleControl.BLE_STATUS_SEND_DATE_SUCCESS:
                addLogs(2,"数据发送成功!");
                break;
            case BleControl.BLE_STATUS_DEVICE_ACCEPT_DATA:
                addLogs(2,"设备已经接收到数据！");
                break;
            case BleControl.BLE_STATUS_SCAN_START:
                addLogs(2,"开始扫描蓝牙..");
                MainHandler.getInstance().post(new Runnable() {
                    @Override
                    public void run() {
                        if(bluetoothDevices != null && bluetoothDevices.size()>0){
                            bluetoothDevices.clear();
                            adapter.setDatas(bluetoothDevices);
                        }
                    }
                });
                break;
            case BleControl.BLE_STATUS_NO_CONNECTED:
                addLogs(2,"数据发送失败，蓝牙未连接!");
                break;
            case BleControl.BLE_STATUS_SCAN_STOP:
                if(bluetoothDevices.size() == 0){
                    addLogs(2,"没有找到设备!");
                }else {
                    addLogs(2,"停止扫描,总共找到"+bluetoothDevices.size()+"个设备!");
                }
                break;
            default:break;
        }
    }

    /**
     * 蓝牙连接成功之后获取系统功能信息,如果马上就发送信息，则会失败，这里暂停一下子
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void getSystemFunction() {
        addLogs(1,HexUtil.bytesToHexString(JNIUtils.getInstance().getSystemFunction0x18()));
        BleControl.getInstance().writeBuffer(HexUtil.bytesToHexString(JNIUtils.getInstance().getSystemFunction0x18()));
    }

    /**
     * 添加信息提示
     * @param type 0 收 1 发 3  状态
     * @param content
     */
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
                rlvLog.animate();
                rlvLog.smoothScrollToPosition(dataEntities.size());
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
        BleControl.getInstance().unRegister(MainActivity.this);
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
                if(BleControl.getInstance().isConnect()){
                    addLogs(2,"手动断开蓝牙..");
                    BleControl.getInstance().disConnection(false);
                }else {
                    BleControl.getInstance().connect(bluetoothDevices.get(position).getAddress(),true);
                }
            }
        });
    }


    //----------------------------------------------------以下代码为权限检查相关----------------------------------------------------------------------


    private String[] LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int CHECK_CODE = 0x11;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @AfterPermissionGranted(CHECK_CODE)
    public void checkPermissions() {
        if(hasPermission()){
            scanDevice();
        }else {
            EasyPermissions.requestPermissions(MainActivity.this,
                    "扫描蓝牙需要定位权限",
                    CHECK_CODE,
                    LOCATION);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanDevice(){
        BleControl.getInstance().scanBle(new BleControl.BleScanResultListener() {
            @Override
            public void onScanResult(List<BluetoothDevice> bluetoothDevices) {
                //如果列表为空，则可能是定位权限没开
                Log.d(TAG, "onScanResult: "+bluetoothDevices.size());
                getDatas(bluetoothDevices);
            }
        });
    }

    private boolean hasPermission(){
        return EasyPermissions.hasPermissions(MainActivity.this,LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        scanDevice();
        if(requestCode == CHECK_CODE){
            addLogs(2,"已获得权限,开始扫描蓝牙...");
        }
    }

    //这个方法是第二次拒绝的时候才调用了

    /**
     * 被拒绝的权限列表
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
//        Log.d(TAG, "onPermissionsDenied: "+ JSONArray.toJSONString(perms));
        if(requestCode == CHECK_CODE){
            addLogs(2,"拒绝打开位置权限，无法扫描蓝牙设备!");
        }
    }
}
