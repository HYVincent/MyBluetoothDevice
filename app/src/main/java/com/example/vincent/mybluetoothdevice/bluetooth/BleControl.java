package com.example.vincent.mybluetoothdevice.bluetooth;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import com.example.vincent.mybluetoothdevice.base.App;
import com.example.vincent.mybluetoothdevice.config.Config;
import com.example.vincent.mybluetoothdevice.utils.HexUtil;
import com.example.vincent.mybluetoothdevice.utils.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice.bluetooth
 * @class describe 蓝牙控制类
 * @date 2018/1/23 11:45
 */

public class BleControl {

    private static final String TAG = BleControl.class.getSimpleName();
    private static BleControl instance;
    private Context mContext;
    //蓝牙适配器，操作蓝牙状态
    private BluetoothAdapter mBleAdapter;
    private BluetoothManager mBleManager;
    private BluetoothGatt mBleGatt;
    private BluetoothGattCharacteristic mBleGattCharacteristic;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    //获取到所有服务的集合
    private HashMap<String, Map<String, BluetoothGattCharacteristic>> servicesMap = new HashMap<>();

    //true 扫描 false 未扫描
    private boolean scanning = false;
    private boolean isPrintfBleStatus =false;
    private BleStatusChangeNotificationListener statusChangeNotificationListener;
    //默认蓝牙扫描时间 10s
    private static final long BLE_SCAN_TIME_OUT = 10 * 1000;
    //连接超时
    private static final long BLE_CONNECT_TIME_OUT = 10 * 1000;
    //蓝牙状态，打开...
    public static final int BLE_STATUS_OPEN = 101;
    //蓝牙状态，关闭
    public static final int BLE_STATUS_CLOSE = 102;
    //蓝牙状态，开始扫描
    public static final int BLE_STATUS_SCAN_START = 103;
    //蓝牙状态，扫描结束
    public static final int BLE_STATUS_SCAN_STOP = 104;
    //蓝牙状态，扫描中断，用户手动终止
    public static final int BLE_STATUS_SCAN_BREAK = 105;
    //蓝牙状态，开始连接设备
    public static final int BLE_STATUS_SCAN_CONNECTING = 106;
    //找不到service uuid
    public static final int BLE_STATUS_CONNECT_NO_FOUND_SERVICE_UUID = 116;
    //蓝牙状态，连接失败
    public static final int BLE_STATUS_CONNECT_FAILE = 107;
    //蓝牙状态，连接超时
    public static final int BLE_STATUS_CONNECT_TIME_OUT = 108;
    //蓝牙状态，连接成功
    public static final int BLE_STATUS_CONNECT_SUCCESS = 109;
    //蓝牙状态，设备断开之后重新连接
    public static final int BLE_STATUS_BREAK_RECONNECTION = 110;
    //数据发送失败
    public static final int BLE_STATUS_SEND_DATE_FAILE = 111;
    //数据发送失败
    public static final int BLE_STATUS_SEND_DATE_SUCCESS = 112;
    //发送数据，但是蓝牙此时未连接
    public static final int BLE_STATUS_NO_CONNECTED = 113;
    //蓝牙连接成功之后成功创建了可通信通道，表示可以向设备发送数据了
    public static final int BLE_CONNECT_STATUS_UNBLOCKED = 114;
    //蓝牙连接成功之后创建了可通信通道失败 这时数据监听会失败，失败之后收不到设备数据
    public static final int BLE_CONNECT_STATUS_ACCEPT_FAIL = 115;
    //搜索到的蓝牙地址
    private List<String > address = new ArrayList<>();
    //搜索到的蓝牙设备列表
    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private BleScanResultListener scanResultListener;
    private BleDataChangeNotificationListener dataChangeNotificationListener;
    private String currentConnectAddress;
    //是否连接 true 连接 false 断开
    private boolean isConnect = false;
    //此属性一般不用修改
    private static final String BLUETOOTH_NOTIFY_D = "00002902-0000-1000-8000-00805f9b34fb";

    /**
     * 蓝牙设备服务UUID
     */
    private static final String SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    /**
     * 通知UUID  读取数据
     */
    private static final String UUID_NOTIFY = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    /**
     * 写数据UUID
     */
    private static final String UUID_WRITER = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";

    public boolean isConnect() {
        return isConnect;
    }

    /**
     * 初始化蓝牙
     * @param mContext
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public BleControl initBle(Context mContext){
        if(this.mContext == null){
            this.mContext = mContext.getApplicationContext();
            mBleManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if(mBleManager == null){
                Log.e(TAG, "initBle: Bluetooth manager is null");
            }else {
                mBleAdapter = mBleManager.getAdapter();
            }
            if(mBleAdapter == null){
                Log.e(TAG, "initBle: Bluetooth adapter is null");
            }
            if(isEnable()){
                Log.d(TAG, "initBle: bluetooth status is open");
            }else {
                Log.d(TAG, "initBle: bluetooth status is close");
            }
        }
        return this;
    }


    /**
     * 蓝牙连接结果回调
     * @param bluetoothDevice
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void connect(BluetoothDevice bluetoothDevice, boolean enable){
        if(bluetoothDevice != null){
            connect(bluetoothDevice.getAddress(),enable);
        }
    }

    /**
     *
     * @param address
     * @param enable true 失败就重试 false 连接一次
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean connect(String address,boolean enable){
        if(mBleAdapter == null){
            if(hasStatusChangeNotificationListener()){
                statusChangeNotificationListener.onChangeStatus(BLE_STATUS_CONNECT_FAILE);
            }
            return false;
        }
        //如果扫描没有结束，那就停止扫描
        if(scanning){
            stopBleScan(true);
        }
        final BluetoothDevice device = mBleAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            if(hasStatusChangeNotificationListener()){
                statusChangeNotificationListener.onChangeStatus(BLE_STATUS_CONNECT_FAILE);
            }
            return false;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isConnect){
                    if(hasStatusChangeNotificationListener()){
                        statusChangeNotificationListener.onChangeStatus(BLE_STATUS_CONNECT_TIME_OUT);
                    }
                    disConnection();
                }
            }
        },BLE_CONNECT_TIME_OUT);
        currentConnectAddress = address;
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        /**
         * 第二个参数表示是否需要自动连接。如果设置为 true, 表示如果设备断开了，会不断的尝试自动连接。设置为 false 表示只进行一次连接尝试。
         第三个参数是连接后进行的一系列操作的回调，例如连接和断开连接的回调，发现服务的回调，成功写入数据，成功读取数据的回调等等
         */
        mBleGatt = device.connectGatt(mContext, enable, mGattCallback); //该函数才是真正的去进行连接
        if(hasStatusChangeNotificationListener()){
            statusChangeNotificationListener.onChangeStatus(BLE_STATUS_SCAN_CONNECTING);
        }
        return true;
    }

    /**
     * 断开连接
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void disConnection() {
        if (null == mBleAdapter || null == mBleGatt) {
            Log.e(TAG, "disconnection error maybe no init");
            return;
        }
        mBleGatt.disconnect();
        isConnect = false;
        reset();
    }

    /**
     * 重置数据
     */
    private void reset() {
        Log.d(TAG, "reset: 清空集合。。");
        isConnect = false;
        servicesMap.clear();
    }

    /**
     * 发送数据
     *
     * @param value         指令
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void writeBuffer(String value) {
        if(TextUtils.isEmpty(value)){
            Log.d(TAG, "writeBuffer: the value is null,no send!");
            return;
        }
        if (!isEnable()) {
            if(hasStatusChangeNotificationListener()){
                statusChangeNotificationListener.onChangeStatus(BLE_STATUS_SEND_DATE_FAILE);
            }
            Log.e(TAG, "date send faile 1，data = " + value);
            return;
        }
        if(mBleGatt == null || !isConnect){
            if(hasStatusChangeNotificationListener()){
                statusChangeNotificationListener.onChangeStatus(BLE_STATUS_NO_CONNECTED);
            }
            return;
        }
        if (mBleGattCharacteristic == null) {
            mBleGattCharacteristic = getBluetoothGattCharacteristic(getServiceUUID(), getWriteUUID());
        }

        if (null == mBleGattCharacteristic) {
            if(hasStatusChangeNotificationListener()){
                statusChangeNotificationListener.onChangeStatus(BLE_STATUS_SEND_DATE_FAILE);
            }
            Log.e(TAG, "date send faile 2，data = " + value);
            return;
        }

        //设置数组进去
        mBleGattCharacteristic.setValue(HexUtil.hexStringToBytes(value));
        //发送
        boolean b = mBleGatt.writeCharacteristic(mBleGattCharacteristic);
        Log.e(TAG, "send: " + b + " data：" + value);
        if(b){
            if(hasStatusChangeNotificationListener()){
                statusChangeNotificationListener.onChangeStatus(BLE_STATUS_SEND_DATE_SUCCESS);
            }
        }else {
            if(hasStatusChangeNotificationListener()){
                statusChangeNotificationListener.onChangeStatus(BLE_STATUS_SEND_DATE_FAILE);
            }
        }
    }

    /**
     * 获取写数据的UUID
     * @return
     */
    private String getWriteUUID() {
        String writeUUID = App.getSpUtil().getString(UUID_WRITER,"");
        if(TextUtils.isEmpty(writeUUID)){
            writeUUID = UUID_WRITER;
        }
        Log.d(TAG, "getWriteUUID: UUID_WRITER= "+writeUUID);
        return writeUUID;
    }

    /**
     * 获取设备ServiceUUID
     * @return
     */
    private String getServiceUUID() {
        String uuid = App.getSpUtil().getString(Config.SERVICE_UUID,"");
        if(TextUtils.isEmpty(uuid)){
            uuid = SERVICE_UUID;
        }
        Log.d(TAG, "getServiceUUID: service uuid --->"+uuid);
        return uuid;
    }


    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            Log.d(TAG, "onPhyUpdate: ");
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
            Log.d(TAG, "onPhyRead: ");
        }

        //此方法连接成功之后才会调用
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            //这一个方法有三个参数，第一个就蓝牙设备的 Gatt 服务连接类。
            //第二个参数代表是否成功执行了连接操作，如果为 BluetoothGatt.GATT_SUCCESS 表示成功执行连接操作，
            // 第三个参数才有效，否则说明这次连接尝试不成功。有时候，我们会遇到 status == 133 的情况，根据网上大部分人的说法，
            // 这是因为 Android 最多支持连接 6 到 7 个左右的蓝牙设备，如果超出了这个数量就无法再连接了。所以当我们断开蓝牙设备的连接时，
            // 还必须调用 BluetoothGatt#close 方法释放连接资源。否则，在多次尝试连接蓝牙设备之后很快就会超出这一个限制，导致出现这一个错误再也无法连接蓝牙设备。
            if(status == BluetoothGatt.GATT_SUCCESS){
                // 第三个参数代表当前设备的连接状态，如果 newState == BluetoothProfile.STATE_CONNECTED 说明设备已经连接，
                // 可以进行下一步的操作了（发现蓝牙服务，也就是 Service）。当蓝牙设备断开连接时，这一个方法也会被回调，
                // 其中的 newState == BluetoothProfile.STATE_DISCONNECTED。
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices(); //执行到这里其实蓝牙已经连接成功了
                    Log.d(TAG, "onConnectionStateChange: 蓝牙连接成功..");
                    isConnect = true;
                    if(hasStatusChangeNotificationListener()){
                        statusChangeNotificationListener.onChangeStatus(BLE_STATUS_CONNECT_SUCCESS);
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    //断开了
                    isConnect = false;
                    //掉线了就关闭连接释放资源
                    gatt.close();
                    mBleGatt = null;
                    if(!TextUtils.isEmpty(currentConnectAddress)&& isEnable()){
                        Log.d(TAG, "onConnectionStateChange: 设备断开连接，正在尝试重新连接...");
                        connect(currentConnectAddress,false);
                        if(hasStatusChangeNotificationListener()){
                            statusChangeNotificationListener.onChangeStatus(BLE_STATUS_BREAK_RECONNECTION);
                        }
                    }else{
                        Log.d(TAG, "onConnectionStateChange: 设备断开连接...");
                    }
                }else if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    //断线
                    isConnect = false;
                    gatt.close();
                    mBleGatt = null;
                }
            }else {
                if(hasStatusChangeNotificationListener()){
                    statusChangeNotificationListener.onChangeStatus(BLE_STATUS_CONNECT_FAILE);
                }
            }
        }

       /* 在成功连接到蓝牙设备之后才能进行这一个步骤，也就是说在 BluetoothGattCalbackl#onConnectionStateChang 方法被成功回调且表示成功
        连接之后调用 BluetoothGatt#discoverService 这一个方法。当这一个方法被调用之后，系统会异步执行发现服务的过程，直到 BluetoothGattCallback#onServicesDiscovered
        被系统回调之后，手机设备和蓝牙设备才算是真正建立了可通信的连接。*/
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (null != mBleGatt && status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = mBleGatt.getServices();
                for (int i = 0; i < services.size(); i++) {
                    HashMap<String, BluetoothGattCharacteristic> charMap = new HashMap<>();
                    BluetoothGattService bluetoothGattService = services.get(i);
                    String serviceUuid = bluetoothGattService.getUuid().toString();
                    List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
                    for (int j = 0; j < characteristics.size(); j++) {
                        charMap.put(characteristics.get(j).getUuid().toString(), characteristics.get(j));
                    }
                    servicesMap.put(serviceUuid, charMap);
                }
                BluetoothGattCharacteristic NotificationCharacteristic=getBluetoothGattCharacteristic(getServiceUUID(),getNotifyUUID());
                if (NotificationCharacteristic==null) {
                    return;
                }
                //建立可通信通道
                boolean isCreateCommunicationAisle  =  enableNotification(true,NotificationCharacteristic);
                if(!isCreateCommunicationAisle){
                    if(hasStatusChangeNotificationListener()){
                        statusChangeNotificationListener.onChangeStatus(BLE_CONNECT_STATUS_ACCEPT_FAIL);
                    }
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "callback characteristic read status " + status
                    + " in thread " + Thread.currentThread());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicRead--> 读取数据:" + characteristic.getValue());

                byte[] data =  characteristic.getValue();
                Log.d(TAG, "onCharacteristicRead: data =" + StringUtils.byteToString(data));
            }
        }

        //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发onCharacteristicWrite
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if(hasStatusChangeNotificationListener()){
                    statusChangeNotificationListener.onChangeStatus(BLE_STATUS_SEND_DATE_SUCCESS);
                }
                Log.e(TAG, "Send data success!");
            } else {
                if(hasStatusChangeNotificationListener()){
                    statusChangeNotificationListener.onChangeStatus(BLE_STATUS_SEND_DATE_FAILE);
                }
                Log.e(TAG, "Send data failed!");
            }
        }

        //收到数据通知
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
                if(dataChangeNotificationListener != null){
                    byte[] rec = characteristic.getValue();
                    dataChangeNotificationListener.onDatas(rec);
                }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            //可读
            Log.d(TAG, "onDescriptorRead: ");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            //可写。。
            Log.d(TAG, "onDescriptorWrite: ");
            if(hasStatusChangeNotificationListener()){
                statusChangeNotificationListener.onChangeStatus(BLE_CONNECT_STATUS_UNBLOCKED);
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            Log.d(TAG, "onReliableWriteCompleted: ");
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.d(TAG, "onReadRemoteRssi: ");
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.d(TAG, "onMtuChanged: ");
        }
    };

    /**
     * 获取读数据UUID
     * @return
     */
    private String getNotifyUUID() {
        String notifyUUID = App.getSpUtil().getString(Config.UUID_NOTIFY,"");
        if(TextUtils.isEmpty(notifyUUID)){
            notifyUUID = UUID_NOTIFY;
        }
        Log.d(TAG, "getNotifyUUID: NOTIFY_UUID = "+notifyUUID);
        return notifyUUID;
    }


    /**
     * 设置通知
     *
     * @param enable         true为开启false为关闭
     * @param characteristic 通知特征
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean enableNotification(boolean enable, BluetoothGattCharacteristic characteristic) {
        if (mBleGatt == null || characteristic == null) {
            return false;
        }
        if (!mBleGatt.setCharacteristicNotification(characteristic, enable)) {
            return false;
        }
        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(UUID.fromString(BLUETOOTH_NOTIFY_D));
        if (clientConfig == null) {
            return false;
        }
        if (enable) {
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return mBleGatt.writeDescriptor(clientConfig);
    }






    /**
     * 根据服务UUID和特征UUID,获取一个特征{@link BluetoothGattCharacteristic}
     *
     * @param serviceUUID   服务UUID
     * @param characterUUID 特征UUID
     */
    private BluetoothGattCharacteristic getBluetoothGattCharacteristic(String serviceUUID, String characterUUID) {
        if (!isEnable()) {
            throw new IllegalArgumentException(" Bluetooth is no enable please call BluetoothAdapter.enable()");
        }
        if (null == mBleGatt) {
            Log.e(TAG, "mBluetoothGatt is null");
            return null;
        }

        //找服务
        Map<String, BluetoothGattCharacteristic> bluetoothGattCharacteristicMap = servicesMap.get(serviceUUID);
        if (null == bluetoothGattCharacteristicMap) {
            Log.e(TAG, "Not found the serviceUUID!");
            if(hasStatusChangeNotificationListener()){
                statusChangeNotificationListener.onChangeStatus(BLE_STATUS_CONNECT_NO_FOUND_SERVICE_UUID);
            }
            return null;
        }

        //找特征
        Set<Map.Entry<String, BluetoothGattCharacteristic>> entries = bluetoothGattCharacteristicMap.entrySet();
        BluetoothGattCharacteristic gattCharacteristic = null;
        for (Map.Entry<String, BluetoothGattCharacteristic> entry : entries) {
            if (characterUUID.equals(entry.getKey())) {
                gattCharacteristic = entry.getValue();
                break;
            }
        }
        return gattCharacteristic;
    }

    //------------------------------------蓝牙相关方法----------------------------------

    /**
     * sacn扫描回调 5.0以上用
     */
    private ScanCallback mScanCallback = new ScanCallback() {

        /**
         * // callbackType：确定这个回调是如何触发的
         // result：包括4.3版本的蓝牙信息，信号强度rssi，和广播数据scanRecord
         * @param callbackType
         * @param result
         */
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device =result.getDevice();
//            result.getRssi()  信号信息。。。
            if (device != null){
                //过滤掉其他设备
                addBluetoothDevice(device);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {

            Log.d(TAG,"onBatchScanResults");
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG,"onScanFailed"+errorCode);
        }
    };

    //4.3以上
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        /**
         *
         * @param device  就是扫描到的蓝牙对象，里面各种跟蓝牙有关的信息
         * @param rssi rssi信号强度，这个值是个负数，范围一般为0到-100，负数越大，代表信号越弱，一般如果超过-90，连接会出现不理想的情况
         * @param bytes  scanRecord广播数据，里面的数据就是蓝牙设备希望手机在连接它之前，让手机知道的信息
         */
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,byte[] bytes) {
            if (device != null){
                addBluetoothDevice(device);
            }
        }
    };

    /**
     * 扫描蓝牙
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void scanBle(final BleScanResultListener listener){
        this.scanResultListener = listener;
        if(mBleAdapter == null){
            Log.e(TAG, "scanBle: mBleAdapter is null");
            return;
        }
        if(!isEnable()){
            //蓝牙暂未开启，去打开
            openBle();
            //并在3s之后自动开启扫描..
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(isEnable() && listener != null){
                        Log.d(TAG, "run: 自动开始扫描...");
                        scanBle(listener);
                    }
                }
            },3000);
        }
        if(scanning){
            Log.d(TAG, "scanBle: 蓝牙扫描未结束，不能再次重复扫描..");
            return;
        }
        mHandler.postDelayed(new Runnable() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run() {
                stopBleScan(true);
            }
        }, BLE_SCAN_TIME_OUT);
        //android 5.0 以前
        if (Build.VERSION.SDK_INT < 21){
            mBleAdapter.stopLeScan(mLeScanCallback);
            mBleAdapter.startLeScan(mLeScanCallback);
            if(hasStatusChangeNotificationListener()){
                statusChangeNotificationListener.onChangeStatus(BLE_STATUS_SCAN_START);
            }
            Log.d(TAG, "scanDevice: start scan bluetooth device ...");
            scanning = true;
        } else {
            BluetoothLeScanner scanner = mBleAdapter.getBluetoothLeScanner();
//            scanner.stopScan(mScanCallback);//java.lang.NullPointerException: Attempt to invoke virtual method 'void android.bluetooth.le.BluetoothLeScanner.stopScan(android.bluetooth.le.ScanCallback)' on a null object reference
            if(scanner != null){
                scanner.startScan(mScanCallback);
                if(hasStatusChangeNotificationListener()){
                    statusChangeNotificationListener.onChangeStatus(BLE_STATUS_SCAN_START);
                }
                Log.d(TAG, "scanDevice: start scan bluetooth device ...");
                scanning = true;
            }else {
                Log.d(TAG, "scanDevice: scan ble faile,because scanner is null");
            }
        }
    }

    /**
     * 停止扫描蓝牙
     * @param isAutoStop true 10s之后自动停止搜索，或者是连接某个蓝牙时停止搜索 false用户手动停止搜索
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopBleScan(boolean isAutoStop){
        if(!scanning){
            return;
        }
        if(isAutoStop){
            Log.d(TAG, "stopBleScan: 自动停止..");
        }else {
            Log.d(TAG, "stopBleScan: 手动停止搜索..");
            if(hasStatusChangeNotificationListener()){
                statusChangeNotificationListener.onChangeStatus(BLE_STATUS_SCAN_BREAK);
            }
        }
        if(mBleAdapter != null && scanning){
            scanning = false;
            mBleAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            mBleAdapter.stopLeScan(mLeScanCallback);
            if(hasStatusChangeNotificationListener()){
                statusChangeNotificationListener.onChangeStatus(BLE_STATUS_SCAN_STOP);
            }
            Log.d(TAG, "stopBleScan:  stop scan bluetooth device ...");
        }
    }



    public void registerNotification(Activity activity){
        if(mContext != null && mReceiver != null){
            activity.registerReceiver(mReceiver, makeFilter());
        }
    }

    /**
     * 取消注册
     */
    public void unRegister(Activity activity){
        if(activity != null && mReceiver != null){
            activity.unregisterReceiver(mReceiver);
        }
    }

    //注销蓝牙监听
    private IntentFilter makeFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
    }

    /**
     * 当前蓝牙是否打开
     * @return true 开 false 关闭
     */
    public boolean isEnable(){
        if(mBleAdapter != null && mBleAdapter.isEnabled()){
            return true;
        }
        return false;
    }

    /**
     * 打开蓝牙
     * @return
     */
    public BleControl openBle(){
        if(!isEnable()){
            //检测蓝牙是否打开
            if(mBleAdapter!= null){
                //打开蓝牙 打开方式有两种 1、隐式打开  2、显示打开
                //隐式打开蓝牙 这种方式不会弹出打开弹出框
                mBleAdapter.enable();
                if(hasStatusChangeNotificationListener()){
                    statusChangeNotificationListener.onChangeStatus(BLE_STATUS_OPEN);
                }
            }else {
                Log.e(TAG, "openBle: mBleAdapter is null" );
            }
        }
        return this;
    }

    /**
     * 关闭蓝牙
     */
    public void closeBle(){
        if(mBleAdapter != null){
            scanning = false;
            mBleAdapter.disable();
            if(hasStatusChangeNotificationListener()){
                statusChangeNotificationListener.onChangeStatus(BLE_STATUS_CLOSE);
            }
        }
    }

    /**
     * 判断是否设置了监听器
     * @return
     */
    private boolean hasStatusChangeNotificationListener(){
        if(statusChangeNotificationListener != null){
            return true;
        }else {
            return false;
        }
    }

    public void setStatusChangeNotificationListener(BleStatusChangeNotificationListener statusChangeNotificationListener) {
        this.statusChangeNotificationListener = statusChangeNotificationListener;
    }

    public interface BleStatusChangeNotificationListener{
        /**
         * 蓝牙状态该表
         * @param status
         */
        void onChangeStatus(int status);

    }


    public interface BleDataChangeNotificationListener{

        void onDatas(byte[] datas);

    }


    public synchronized static  BleControl getInstance() {
        if(instance == null){
            instance = new BleControl();
        }
        return instance;
    }



    /**
     * 蓝牙状态改变的广播通知
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    if(hasStatusChangeNotificationListener()){
                        statusChangeNotificationListener.onChangeStatus(blueState);
                    }else {
                        Log.d(TAG, "onReceive: 请注册蓝牙广播。。");
                    }
                    if(isPrintfBleStatus){
                        switch (blueState) {
                            case BluetoothAdapter.SCAN_MODE_NONE:
                                Log.d(TAG, "onReceive: 没有扫描到设备");
                                break;
                            case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                                Log.d(TAG, "onReceive: 可连接。。。");
                                break;
                            case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                                Log.d(TAG, "onReceive: 可被发现...");
                                break;
                            case BluetoothAdapter.STATE_TURNING_ON:
                                Log.d(TAG, "onReceive: 正在打开蓝牙。。");
                                break;
                            case BluetoothAdapter.STATE_ON:
                                Log.d(TAG, "onReceive: 蓝牙打开");
                                break;
                            case BluetoothAdapter.STATE_TURNING_OFF:
                                Log.d(TAG, "onReceive: 蓝牙正在关闭..");
                                break;
                            case BluetoothAdapter.STATE_OFF:
                                Log.d(TAG, "onReceive: 蓝牙关闭");
                                scanning = false;
                                isConnect = false;
                                break;
                            default:break;
                        }
                    }
                    break;
                default:break;
            }
        }
    };

    /**
     * 设置蓝牙可见性
     * @param activity
     * @param times
     */
    public void setBluetoothIsVisible(Activity activity,long times){
        //启动修改蓝牙可见性的Intent
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //设置蓝牙可见性的时间，方法本身规定最多可见300秒
//        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, times);
        activity.startActivity(intent);
    }

    /**
     * 设置蓝牙是可见的，听说这个方法可以实现永久可见
     */
    public void setBluetoothIsVisible (){
        //声明一个class类
        Class serviceManager = null;
        try {
            //得到这个class的类
            serviceManager = Class.forName("android.bluetooth.BluetoothAdapter");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //声明一个方法
        Method method = null;
        try {
            //得到指定的类中的方法
            method = serviceManager.getMethod("setDiscoverableTimeout", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            //调用这个方法
            method.invoke(serviceManager.newInstance(), 30);//根据测试，发现这一函数的参数无论传递什么值，都是永久可见的
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭永久可见性
     */
    public void closeDiscoverableTimeout() {
        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode =BluetoothAdapter.class.getMethod("setScanMode", int.class,int.class);
            setScanMode.setAccessible(true);

            setDiscoverableTimeout.invoke(adapter, 1);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE,1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface BleScanResultListener{
        void onScanResult(List<BluetoothDevice> bluetoothDevices);
    }

    public void setDataChangeNotificationListener(BleDataChangeNotificationListener dataChangeNotificationListener) {
        this.dataChangeNotificationListener = dataChangeNotificationListener;
    }

    /**
     * 把扫描到的蓝牙设备添加到集合
     */
    private synchronized void addBluetoothDevice(BluetoothDevice device){
        if(!hasExist(device.getAddress())){
            address.add(device.getAddress());
            bluetoothDevices.add(device);
            scanResultListener.onScanResult(bluetoothDevices);
        }
    }

    /**
     * 去重判断
     * @param deviceAddress
     * @return
     */
    private  boolean hasExist(String deviceAddress){
        for (String string : address){
            if(TextUtils.equals(string,deviceAddress)){
                return true;
            }
        }
        return false;
    }

}
