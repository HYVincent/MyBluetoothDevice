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
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Vincent QQ:1032006226
 * @version v1.0
 * @name MyBluetoothDevice
 * @page com.example.vincent.mybluetoothdevice.bluetooth
 * @class describe 蓝牙工具类
 * @date 2018/1/22 9:36
 */

public class BluetoothUtils {

    private static final String TAG = BluetoothUtils.class.getSimpleName();
    private static BluetoothUtils bluetoothUtils;
    private static Context mContext;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter blueAdapter;
    //是否支持低功耗蓝牙
    private boolean isSupportBle;
    //是否正在扫描蓝牙设备
    private boolean isScan = false;
    //扫描到的蓝牙设备
    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    //搜索到的蓝牙地址列表
    private  List<String> address = new ArrayList<>();
    //扫描结果监听
    private BluetoothScanResultListener listener;
    //通知蓝牙状态改变
    private BluetoothStatusChangeListener statusChangeListener;
    //设置工具类是否打印蓝牙状态该表log
    private boolean isPrintfBluetoothStatus = false;

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mCharacteristic;
    private BluetoothGattCharacteristic mCharacteristicNotice;

    //配合设置15s之后关闭蓝牙扫描
    private Handler mHandler = new Handler();
    //蓝牙扫描时间为15s，15s之后停止搜索
    private static long SCAN_PERIOD = 15 * 1000;
    //上一次连接的蓝牙地址
    private String mBluetoothDeviceAddress;
    //蓝牙状态，正在连接...
    public static final int BLUETOOTH_STATUS_CONNECTING = 101;
    //蓝牙状态，连接成功
    public static final int BLUETOOTH_STATUS_CONNECT_SUCCESS = 102;
    //蓝牙状态,连接失败
    public static final int BLUETOOTH_STATUS_CONNECT_FAIL = 103;
    //蓝牙断开，重新连接
    public static final int BLUETOOTH_STATUS_CONNECT_RETRY = 104;
    //开始扫描蓝牙设备
    public static final int BLUETOOTH_STATUS_START_SCAN = 105;
    //蓝牙停止扫描
    public static final int BLUETOOTH_STATUS_STOP_SCAN = 106;
    //可操作特征值时发送的消息
    public static final int CHARACTERISTIC_ACCESSIBLE = 107;




    private static final String serviceUuid ="";
    private static final String characterUuid ="";
    private static final String characterUuidNotice ="";

    public static String clientUuid = "00002902-0000-1000-8000-00805f9b34fb";

    //初始化
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothUtils(Context context){
        mContext = context;
        bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        blueAdapter = bluetoothManager.getAdapter();
        isSupportBle = mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static BluetoothUtils getBluetoothUtils(Context context) {
        if(bluetoothUtils == null){
            bluetoothUtils = new BluetoothUtils(context);
        }
        return bluetoothUtils;
    }

    /**
     * 检查蓝牙是否存在
     * @return true 存在  false 不存在
     */
    private boolean hasBluetooth(){
        if(blueAdapter != null){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 打开蓝牙
     */
    public void openBluetooth(){
        if(!hasBluetooth()){
            return;
        }
        //检测蓝牙是否打开
        if(blueAdapter!= null && !blueAdapter.isEnabled()){
            //打开蓝牙 打开方式有两种 1、隐式打开  2、显示打开
            //隐式打开蓝牙 这种方式不会弹出打开弹出框
            blueAdapter.enable();
        }
    }

    /**
     * 关闭蓝牙
     */
    public void closeBluetooth(){
        if(!hasBluetooth()){
            return;
        }
        if(blueAdapter != null && blueAdapter.isEnabled()){
            isScan = false;
            blueAdapter.disable();
        }
    }

    /**
     * 连接设备
     * @param device
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void connectDevice(BluetoothDevice device){
        if(device != null){
            connectDevice(device.getAddress());
        }
    }

    /**
     * 获取已连接设备的设备名
     *
     * @return 字符串形式的设备名
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public String getDeviceName() {
        if(mBluetoothGatt != null){
            return mBluetoothGatt.getDevice().getName();
        }else {
            return "";
        }
    }

    /**
     * 连接设备
     * @param address
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean connectDevice(final String address) {
        if(isScan){
            Log.d(TAG, "connectDevice: start connect "+ address+",stop scan bluetooth device ..");
            stopBluetoothScan();
        }
        if (mContext == null ||blueAdapter == null || TextUtils.isEmpty(address)) {
            Log.w(TAG,"BluetoothAdapter not initialized or unspecified address.");
            if(statusChangeListener != null){
                statusChangeListener.bluetoothStatus(BLUETOOTH_STATUS_CONNECT_FAIL);
            }
            return false;
        }
        // Previously connected device. Try to reconnect. (先前连接的设备。 尝试重新连接)
        if (mBluetoothDeviceAddress != null&& address.equals(mBluetoothDeviceAddress)&& mBluetoothGatt != null) {
            Log.d(TAG,"Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                Log.d(TAG, "connect: 连接成功");
                if(statusChangeListener != null){
                    statusChangeListener.bluetoothStatus(BLUETOOTH_STATUS_CONNECTING);
                }
                return true;
            } else {
                if(statusChangeListener != null){
                    statusChangeListener.bluetoothStatus(BLUETOOTH_STATUS_CONNECT_FAIL);
                }
                return false;
            }
        }
        final BluetoothDevice device = blueAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            if(statusChangeListener != null){
                statusChangeListener.bluetoothStatus(BLUETOOTH_STATUS_CONNECT_FAIL);
            }
            return false;
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        /**
         * 第二个参数表示是否需要自动连接。如果设置为 true, 表示如果设备断开了，会不断的尝试自动连接。设置为 false 表示只进行一次连接尝试。
         第三个参数是连接后进行的一系列操作的回调，例如连接和断开连接的回调，发现服务的回调，成功写入数据，成功读取数据的回调等等
         */
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback); //该函数才是真正的去进行连接
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        if(statusChangeListener != null){
            statusChangeListener.bluetoothStatus(BLUETOOTH_STATUS_CONNECTING);
        }
        return true;
    }


    /**
     * 停止扫描蓝牙设备
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopBluetoothScan(){
        if(blueAdapter != null && isScan){
            if(mLeScanCallback != null){
                blueAdapter.stopLeScan(mLeScanCallback);
            }
            if(mScanCallback != null){
                blueAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            }
            if(statusChangeListener != null){
                statusChangeListener.bluetoothStatus(BLUETOOTH_STATUS_STOP_SCAN);
            }
        }
    }


    /**
     * 扫描蓝牙设备
     * @param bluetoothScanResultListener 扫描结果监听器
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void searchBluetoothDevice(BluetoothScanResultListener bluetoothScanResultListener){
        listener = bluetoothScanResultListener;
        if(isScan){
            Log.d(TAG, "searchBluetoothDevice: 正在扫描，请勿从夫点击..");
            return;
        }
        if(!hasBluetooth()){
            Log.d(TAG, "searchBluetoothDevice: 设备不支持蓝牙");
            return;
        }
        if(!blueAdapter.isEnabled()){
            openBluetooth();
        }
        scanDevice(true);
    }


    /**
     * 扫描
     * @param enable true 开始扫描 false停止扫描
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanDevice(boolean enable){
        if(blueAdapter == null){
            return;
        }
        if (enable && blueAdapter.isEnabled()) {
            // Stops scanning after a pre-defined scan period.
            // 预先定义停止蓝牙扫描的时间（因为蓝牙扫描需要消耗较多的电量）
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScan = false;
                    blueAdapter.stopLeScan(mLeScanCallback);
                    if(blueAdapter.getBluetoothLeScanner() != null){
                        blueAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                    }
                    if(statusChangeListener != null){
                        statusChangeListener.bluetoothStatus(BLUETOOTH_STATUS_STOP_SCAN);
                    }
                }
            }, SCAN_PERIOD);
            isScan = true;
            //android 5.0 以前
            if (Build.VERSION.SDK_INT < 21){
                blueAdapter.stopLeScan(mLeScanCallback);
                blueAdapter.startLeScan(mLeScanCallback);
                Log.d(TAG, "scanDevice: start scan bluetooth device ...");
                if(statusChangeListener != null){
                    statusChangeListener.bluetoothStatus(BLUETOOTH_STATUS_START_SCAN);
                }
            } else {
                BluetoothLeScanner scanner = blueAdapter.getBluetoothLeScanner();
//            scanner.stopScan(mScanCallback);//java.lang.NullPointerException: Attempt to invoke virtual method 'void android.bluetooth.le.BluetoothLeScanner.stopScan(android.bluetooth.le.ScanCallback)' on a null object reference
                if(scanner != null){
                    scanner.startScan(mScanCallback);
                    Log.d(TAG, "scanDevice: start scan bluetooth device ...");
                    if(statusChangeListener != null){
                        statusChangeListener.bluetoothStatus(BLUETOOTH_STATUS_START_SCAN);
                    }
                }else {
                    Log.d(TAG, "scanDevice: scanner is null");
                }
            }
        } else {
            isScan = false;
            if(blueAdapter.getBluetoothLeScanner()!= null){
                blueAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            }
            blueAdapter.stopLeScan(mLeScanCallback);
        }
    }

    //sacn扫描回调 5.0以上用
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
     * 注册广播，接受状态
     */
    public void register(BluetoothStatusChangeListener bluetoothScanResultListener){
        this.statusChangeListener = bluetoothScanResultListener;
        //注册蓝牙监听
        mContext.registerReceiver(mReceiver, makeFilter());
    }

    /**
     * 取消注册
     */
    public void unRegister(){
        mContext.unregisterReceiver(mReceiver);
    }

    //注销蓝牙监听
//mContext.unregisterReceiver(mReceiver);
    private IntentFilter makeFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return filter;
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
                    if(statusChangeListener != null){
                        statusChangeListener.bluetoothStatus(blueState);
                    }else {
                        Log.d(TAG, "onReceive: 请注册蓝牙广播。。");
                    }
                    /*if(isPrintfBluetoothStatus){
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
                                break;
                            default:break;
                        }
                    }
                    break;*/
                default:break;
            }
        }
    };


    /**
     * 开启特征值的notification，然后才能读取数据
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void setCharacteristicNotification() {
        if(mBluetoothGatt == null){
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(mCharacteristicNotice, true);
        BluetoothGattDescriptor descriptor = mCharacteristic.
                getDescriptor(UUID.fromString(clientUuid));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
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
                    if(statusChangeListener != null){
                        statusChangeListener.bluetoothStatus(BLUETOOTH_STATUS_CONNECT_SUCCESS);
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    //断开了
                    if(!TextUtils.isEmpty(mBluetoothDeviceAddress)){
                        Log.d(TAG, "onConnectionStateChange: 设备断开连接，正在尝试重新连接...");
                        connectDevice(mBluetoothDeviceAddress);
                        if(statusChangeListener != null){
                            statusChangeListener.bluetoothStatus(BLUETOOTH_STATUS_CONNECT_RETRY);
                        }
                    }else{
                        Log.d(TAG, "onConnectionStateChange: 设备断开连接...");
                    }
                }else if (newState == BluetoothProfile.STATE_CONNECTED) {//断开设备连接
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    gatt.close();
                }
            }else {
                if(statusChangeListener != null){
                    statusChangeListener.bluetoothStatus(BLUETOOTH_STATUS_CONNECT_FAIL);
                }
            }
        }

        //表示建立可通信连接
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d(TAG, "onServicesDiscovered: ");
            if(status == BluetoothGatt.GATT_SUCCESS){

                BluetoothGattService service = gatt.getService(UUID
                        .fromString(serviceUuid));
                mCharacteristic = service.getCharacteristic(UUID
                        .fromString(characterUuid));

                mCharacteristicNotice = service.getCharacteristic(UUID
                        .fromString(characterUuidNotice));

                //开启通知
                setCharacteristicNotification();

                if(statusChangeListener != null){
                    statusChangeListener.bluetoothStatus(CHARACTERISTIC_ACCESSIBLE);
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

                // 读取数据
                /*BluetoothGattService service = gattt.getService(SERVICE_UUID);
                BluetoothGattCharacteristic characteristic = gatt.getCharacteristic(CHARACTER_UUID);
                gatt.readCharacteristic();*/

            }
        }

        //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发onCharacteristicWrite
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "callback characteristic write in thread " + Thread.currentThread());
            /*if(!characteristic.getValue().equal(sendValue)) {
                // 执行重发策略
                gatt.writeCharacteristic(characteristic);
            }*/
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "onCharacteristicChanged: ");
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorRead: ");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorWrite: ");
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
     * 向设备中写入数据
     */
    public void sendValues(String sendValue){
        //往蓝牙数据通道的写入数据
//        BluetoothGattService service = gattt.getService(SERVICE_UUID);
//        BluetoothGattCharacteristic characteristic = gatt.getCharacteristic(CHARACTER_UUID);
//        characteristic.setValue(sendValue);
//        gatt.writeCharacteristic(characteristic);

    }


    public interface BluetoothScanResultListener{
        void scanResult(List<BluetoothDevice> bluetoothDevices);
    }

    public interface BluetoothStatusChangeListener{

        void bluetoothStatus(int status);

    }

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

    /**
     * 把扫描到的蓝牙设备添加到集合
     */
    private synchronized void addBluetoothDevice(BluetoothDevice device){
        if(!hasExist(device.getAddress())){
            address.add(device.getAddress());
            bluetoothDevices.add(device);
            listener.scanResult(bluetoothDevices);
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
