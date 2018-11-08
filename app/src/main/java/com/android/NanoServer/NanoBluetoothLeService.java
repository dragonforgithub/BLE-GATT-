package com.android.NanoServer;

import android.app.Service;
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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import com.android.NanoServer.INanoMethod;
import com.android.NanoServer.INanoMethodCallback;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class NanoBluetoothLeService extends Service {
//public class NanoBluetoothLeService extends Activity{
    private static NanoBluetoothLeService mThis = null;
    private final String ATVV_SERVICE           = "AB5EFEF1-5A21-4F05-BC7D-AF01F617B664";
    private final String ATVV_SERVICE_NOTIFY   = "AB5EFEF3-5A21-4F05-BC7D-AF01F617B664";
    private final String ATVV_SERVICE_WRITE    = "AB5EFEF2-5A21-4F05-BC7D-AF01F617B664";
    private final String ATVV_SERVICE_CONTROL  = "AB5EFEF4-5A21-4F05-BC7D-AF01F617B664";
    private final UUID   ATVV_SERVICE_UUID = UUID.fromString(ATVV_SERVICE);
    private final UUID   ATVV_SERVICE_NOTIFY_UUID = UUID.fromString(ATVV_SERVICE_NOTIFY);
    private final UUID   ATVV_SERVICE_WRITE_UUID = UUID.fromString(ATVV_SERVICE_WRITE);
    private final UUID   ATVV_SERVICE_CONTROL_UUID = UUID.fromString(ATVV_SERVICE_CONTROL);
    private BluetoothGattCharacteristic WriteCmdChar = null;
    public static final String ACTION_CONNECTION_STATE_CHANGED = "android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED";
    final  RemoteCallbackList<INanoMethodCallback> mCallbacks = new RemoteCallbackList<>();
    // 标记BLE设备是否忙
    private boolean BleIsBusy;
    // BLE 接收到数据缓存
    private byte[] BleRecBuff ;
    // BLE接收到一个包
    private boolean fEnableNotificaThreadIsRun = false;
    private boolean fBleRecVendorPacketOk = false;
    private boolean fBleSendVendorPacketOk = false;
    private boolean mBound = false;
    private boolean isStop = true;
    private BluetoothGattService bluetoothGattATVService;
    private boolean running = false;
    /*-----------------------------------------------------------------------------
     Function Name:   OS_PRINTF
     Input		:
     Output		:
     Return 		:
     Describe		:打印函数
     -------------------------------------------------------------------------------*/
    private void OS_PRINTF(String... args) {
        String str = "";
        for (int i = 0; i < args.length; i++) {
            str += args[i];
            if (i != args.length - 1) {
                str += ", ";
            }
        }
        Log.d("==========>NanoAppService ", str);
    }

    @Override
    public void onCreate() {

        System.loadLibrary("nanoApp");
        System.loadLibrary("nanoApp_jni");

        OS_PRINTF("Bluetooth Le Service onCreate.");
        NanoOpen();
        BleIsBusy = false;
        fBleRecVendorPacketOk = false;
        fBleSendVendorPacketOk = false;
        fEnableNotificaThreadIsRun = false;
        BleRecBuff = new byte[22];
        Nano_BoardcastRegister();
        initialize(null,null);
        isStop = false;
    }

    public void NanoWriteCommandData(byte data[])
    {
        BluetoothGattCharacteristic Write = bluetoothGattATVService.getCharacteristic(ATVV_SERVICE_WRITE_UUID);
        if(Write == null)
            return;
        Write.setWriteType(Write.WRITE_TYPE_NO_RESPONSE);
        OS_PRINTF("WriteType " + Write.getWriteType());
        Write.setValue(data);
        writeCharacteristic(Write);
        OS_PRINTF("write " + bytesToHexString(data));
    }

    public void NanoWriteRequestData(byte data[])
    {
        BluetoothGattCharacteristic Write = bluetoothGattATVService.getCharacteristic(ATVV_SERVICE_WRITE_UUID);
        if(Write == null)
            return;
        Write.setWriteType(Write.WRITE_TYPE_DEFAULT);
        OS_PRINTF("WriteType " + Write.getWriteType());
        Write.setValue(data);
        writeCharacteristic(Write);
        OS_PRINTF("write " + bytesToHexString(data));
    }

    /*-----------------------------------------------------------------------------
        Function Name:   Nano_ProgressThread
        Input		:
        Output		:
        Return 		:
        Describe		: 进度条显示
        -------------------------------------------------------------------------------*/
    private class Nano_CmdWriteTestThread extends Thread {
        @Override
        public void run() {
            int Progress[] = new int[1];

            try {
                while (running)
                {
                    byte OpenMic[] = {(byte)0xC,(byte)0x00,(byte)0x01};
                    NanoWriteCommandData(OpenMic);
                    Thread.sleep(2);
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public NanoBluetoothLeService() {
        mThis = this;
    }
/*
    private final IBinder binder = new LocalBinder();
    public class LocalBinder extends Binder {
        public NanoBluetoothLeService getService() {
            return NanoBluetoothLeService.this;
        }
    }*/
    /*
    @Override
    public IBinder onBind(Intent intent) {
        OS_PRINTF("Bluetooth Le Service onBind");
        return binder;
    }*/
    /*-----------------------------------------------------------------------------
     Function Name: onBind	:
     Describe		:
     -------------------------------------------------------------------------------*/
    public IBinder onBind(Intent intent) {
        //OS_PRINTF("service on bind,intent = %s",intent.toString());
        OS_PRINTF("onBind");
        return binder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        OS_PRINTF("onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        OS_PRINTF("onStartCommand");
        return START_REDELIVER_INTENT;
    }

    private void  StopServiceSelf(String sReason){
    }

    @Override
    public void onDestroy() {
        OS_PRINTF("onDestroy");
        //mCallbacks.kill();
        //close();
        isStop = true;
        //NanoClose();
        super.onDestroy();
    }

    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothDevice mBluetoothDevice = null ;
    public BluetoothGattCharacteristic VendorOut_Characteristic = null;
    public BluetoothGattCharacteristic VendorIn_Characteristic = null;
    public BluetoothGattCharacteristic Buzzer_Characteristic = null;
    List<BluetoothGattCharacteristic> HidCharacteristicList = null;

    /**
     * Convert byte[] to hex string
     *
     * @param src byte[] data
     * @return hex string
     */
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private void ResetBluetoothValue() {
        mBluetoothManager = null;
        mBluetoothAdapter = null;
        mBluetoothGatt = null;
        mBluetoothDevice = null ;
        VendorOut_Characteristic = null;
        VendorIn_Characteristic = null;
        Buzzer_Characteristic = null;
        BleIsBusy = false;
    }

    public void enablePairService(Context context, boolean pair) {
        //Log.d(TAG, "===== >> enablePairService:  " + pair);
        /*OS_PRINTF("===== >> enablePairService:  " + pair);
        Intent autoPairService = new Intent(context, NanoBluetoothLeService.class);
        if(pair){
            context.startService(autoPairService);
        } else {
            context.stopService(autoPairService);
        }*/
    }
    // createBond 匹配状态 已经接受ACTION_UUID后进行连接
    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            OS_PRINTF(action );
            if(action.equals("android.action.nano.START")){
                OS_PRINTF("START");
                enablePairService(context, true);
            }else if(action.equals("android.action.nano.STOP")){
                OS_PRINTF("STOP");
            }
        }
    };

    // createBond 匹配状态 已经接受ACTION_UUID后进行连接
    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (device == null) return;

        if (action.equals("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED")) {
            int connectState = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_CONNECTED);
            if (connectState == BluetoothProfile.STATE_DISCONNECTED) { // 连接失败
                OS_PRINTF("Disconnect");

            } else if (connectState == BluetoothProfile.STATE_CONNECTING) {

            } else if (connectState == BluetoothProfile.STATE_CONNECTED) { // 连接成功
                //OS_PRINTF("连接成功...");
                OS_PRINTF("Connected");
                initialize(null,null);
            }
        }
        }
    };

        /*----------------------------------------------------------------------------
        Function Name:	Nano_BoardcastRegister
        Input		:
        Output		:
        Return 		:
        Describe	:	注册广播
        -------------------------------------------------------------------------------*/
        public void Nano_BoardcastRegister() {
            IntentFilter intentFilter1 = new IntentFilter();
            intentFilter1.addAction(ACTION_CONNECTION_STATE_CHANGED);
            registerReceiver(mBluetoothReceiver, intentFilter1);
            // 骞挎挱鎺ユ敹鍣?
            IntentFilter intentInSteadOfStatic = new IntentFilter();
            intentInSteadOfStatic.addAction("android.action.nano.START_LESCAN");
            intentInSteadOfStatic.addAction("android.action.nano.STOP_LESCAN");
            intentInSteadOfStatic.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
            intentInSteadOfStatic.addAction("android.bluetooth.input.profile.action.CONNECTION_STATE_CHANGED");
            intentInSteadOfStatic.addAction("com.android.aidl.server");
            registerReceiver(mBluetoothStateReceiver, intentInSteadOfStatic);
        }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize(String name, String addr) {
    
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            OS_PRINTF("Unable to initialize BluetoothManager.");
            return false;
        }
		
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            OS_PRINTF("Unable to obtain a BluetoothAdapter.");
            return false;
        }
		
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        OS_PRINTF("bonded device size:" + devices.size());
        if (devices.size() > 0) {
            for (Iterator<BluetoothDevice> it = devices.iterator(); it.hasNext(); ) {
                BluetoothDevice device = it.next();
                String mBluetoothDeviceName = device.getName();
                String mBluetoothDeviceAddress = device.getAddress();
                OS_PRINTF("Find Bluetooth device name:" + mBluetoothDeviceName);
                //if(device.getName().equalsIgnoreCase("kk309"))
                if(mBluetoothGatt == null)
                {
                    mBluetoothDevice = device;
                    mBluetoothGatt = mBluetoothDevice.connectGatt(this, false, mGattCallbacks);
                    OS_PRINTF("Find Remote:" + mBluetoothDeviceName);
                    return true;
                }else {
                    OS_PRINTF("mBluetoothGatt " + mBluetoothGatt );
                }
            }
        }
        return false;
    }

        /**
         * After using a given BLE device, the app must call this method to ensure
         * resources are released properly.
         */
        public void close() {
            if (mBluetoothGatt != null) {
                mBluetoothGatt.close();
				mBluetoothGatt = null;
            }
        }

        private void enableNotification(boolean enable, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (gatt == null || characteristic == null)
                return; //这一步必须要有 否则收不到通知
            gatt.setCharacteristicNotification(characteristic, enable);
        }
        //==============================================================================================
        /**
         * GATT client callbacks
         * mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
         */
        private BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback() {
            @Override
		/*?????????*/
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                try {
                    switch (newState) {
                        case BluetoothProfile.STATE_CONNECTED:
                            if (gatt != null) {
                                mBluetoothGatt = gatt;
                                mBluetoothGatt.discoverServices();
                                OS_PRINTF("Bluetooth device connected ");
                                OS_PRINTF("onConnectionStateChange (" + gatt.getDevice().getAddress() + ") "
                                        + newState + " status: " + status);
                            } else {
                                OS_PRINTF("device connected,but gatt is empty.");
                            }
                            break;
                        case BluetoothProfile.STATE_DISCONNECTED:
                            close();
                            OS_PRINTF("Ble disconnected.");
                            // 如果在升级过程中，需要显示升级失败，BLE断连
			//                        BleDisconnectOtaErrBroad();
                            break;
                        default:
                            OS_PRINTF("New state not processed: " + newState);
                            break;
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            @Override
            //发现服务，在蓝牙连接的时候会调用
            public void onServicesDiscovered(BluetoothGatt gatt, int status) 
            {
                if (status == gatt.GATT_SUCCESS) 
				{
                    //BluetoothGattService ATVVService = gatt.getService(ATVV_SERVICE_UUID);
					List<BluetoothGattService> list = mBluetoothGatt.getServices();
					for (BluetoothGattService bluetoothGattService:list){
			            String str = bluetoothGattService.getUuid().toString();
			            if(bluetoothGattService.getUuid().toString().equalsIgnoreCase(ATVV_SERVICE))
                        {
                            bluetoothGattATVService = bluetoothGattService;
                            OS_PRINTF("[S-UUID]", " : " + str);
                            List<BluetoothGattCharacteristic> gattCharacteristics = bluetoothGattService.getCharacteristics();
                            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                                //OS_PRINTF("  [C-UUID]", " : " + gattCharacteristic.getUuid());
                                //if (gattCharacteristic.getUuid().toString().equalsIgnoreCase(ATVV_SERVICE_NOTIFY))
                                {
                                    OS_PRINTF("  [C-UUID]", " : " + gattCharacteristic.getUuid());
                                    OS_PRINTF("  Enable Notify");
                                    ////必须要有，否则接收不到数据
                                    enableNotification(true, gatt, gattCharacteristic);
                                    if(gattCharacteristic.getUuid().toString().equalsIgnoreCase(ATVV_SERVICE_WRITE)){
                                        WriteCmdChar = gattCharacteristic;
                                    }
                                }
                            }
                        }
					}
            	}
         	}

            @Override
		/*????????*/
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {
                //OS_PRINTF("Data:"+bytesToHexString(characteristic.getValue()));
                /*if(isStop == true)
                    return;*/

                if (characteristic.getUuid().toString().equalsIgnoreCase("AB5EFEF4-5A21-4F05-BC7D-AF01F617B664"))
                {
                    byte data[] = characteristic.getValue();
                    OS_PRINTF("Contrl Data:"+bytesToHexString(characteristic.getValue()));
                    OS_PRINTF("Data length " + data.length);
                    if(data.length == 1)
                    {
                        if(data[0] == 8)
                        {
                            BluetoothGattCharacteristic writeChar = bluetoothGattATVService.getCharacteristic(ATVV_SERVICE_WRITE_UUID);
                            OS_PRINTF("UUID " + writeChar.getUuid().toString());
                            //byte OpenMic[] = {(byte)0xC,(byte)0x80,(byte)0x0};
                            byte OpenMic[] = {(byte)0xC,(byte)0x00,(byte)0x01};
                            /*writeChar.setValue(OpenMic);
                            writeCharacteristic(writeChar);
                            OS_PRINTF("write " + bytesToHexString(OpenMic));*/
                            NanoWriteCommandData(OpenMic);
                            /*if (running == false)
                            {
                                running = true;
                                Nano_CmdWriteTestThread test = new Nano_CmdWriteTestThread();
                                test.start();
                            }*/
                        }else if(data[0] == 0){
                            NanoProcData(characteristic.getValue(),characteristic.getValue().length);
                        }
                    }
                    /*if (characteristic.getValue().length == 8) {  // key input
                        //KeyReceivedCallback(characteristic.getValue());
                        NanoProcData(characteristic.getValue(),characteristic.getValue().length);
                    } else {  // vendor input
                        if (characteristic.getValue().length == 20) {
                            NanoProcData(characteristic.getValue(),characteristic.getValue().length);
                        } else {
                            OS_PRINTF("Reception length error.");
                        }
                    }*/
                    //OS_PRINTF("R:" + GetByteString(characteristic.getValue(), 4));
                }else if(characteristic.getUuid().toString().equalsIgnoreCase("AB5EFEF3-5A21-4F05-BC7D-AF01F617B664")){
                    //OS_PRINTF("Voice Data:"+bytesToHexString(characteristic.getValue()) + " length:" + characteristic.getValue().length);
                    NanoProcData(characteristic.getValue(),characteristic.getValue().length);
                    byte OpenMic[] = {(byte)0xC,(byte)0x00,(byte)0x01};
                    /*wirtechar.setValue(OpenMic);
                    writeCharacteristic(wirtechar);
                    OS_PRINTF("write " + bytesToHexString(OpenMic));*/
                    NanoWriteCommandData(OpenMic);
                }
                super.onCharacteristicChanged(gatt, characteristic);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic, int status) {
                /*if(isStop == true)
                    return;*/
                super.onCharacteristicRead(gatt, characteristic, status);
                OS_PRINTF("onCharacteristicRead");
                if (characteristic.getUuid().toString().equals("00002a19-0000-1000-8000-00805f9b34fb")) {
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic, int status) {
                /*if(isStop == true)
                    return;*/
                BleIsBusy = false;
                super.onCharacteristicWrite(gatt, characteristic, status);
                OS_PRINTF("onCharacteristicWrite");
                if (characteristic.getUuid().toString().equals("00002a4d-0000-1000-8000-00805f9b34fb")) {
                    fBleSendVendorPacketOk = true;
                }
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt,
                                         BluetoothGattDescriptor descriptor, int status) {
                /*if(isStop == true)
                    return;*/
                BleIsBusy = false;
                OS_PRINTF("onDescriptorRead");
//            TmpDesBuf = descriptor.getValue();
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt,
                                          BluetoothGattDescriptor descriptor, int status) {
                OS_PRINTF("onDescriptorWrite: " + descriptor.getUuid().toString());
                /*if(isStop == true)
                    return;*/
                BleIsBusy = false;
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                OS_PRINTF("onReadRemoteRssi");
            }
        };

        //==============================================================================================
        private boolean checkGatt() {
            if (mBluetoothAdapter == null) {
                return false;
            }
            if (mBluetoothGatt == null) {
                return false;
            }

           /* if (BleIsBusy) {
                OS_PRINTF("LeService busy");
                return false;
            }*/
            return true;
        }

        /**
         * Enables or disables notification on a give characteristic.
         *
         * @param characteristic Characteristic to act on.
         * @param enable         If true, enable notification. False otherwise.
         */
        private boolean setCharacteristicNotification(
                BluetoothGattCharacteristic characteristic,
                BluetoothGattDescriptor clientConfig, boolean enable) {
            if (!checkGatt())
                return false;
            boolean ok = false;
            OS_PRINTF("set Characteristic Notification:" + characteristic.toString());
            if (mBluetoothGatt.setCharacteristicNotification(characteristic, enable)) {
                if (clientConfig != null) {
                    if (enable) {
                        ok = clientConfig
                                .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    } else {
                        ok = clientConfig
                                .setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    }
                    if (ok) {
                        BleIsBusy = true;
                        clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        ok = mBluetoothGatt.writeDescriptor(clientConfig);
                    }
                }
            }
            return ok;
        }

        private boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
            if (!checkGatt())
                return false;

            BleIsBusy = true;
            return mBluetoothGatt.writeCharacteristic(characteristic);
        }


        public String GetByteString(byte buf[], int len) {
            String tmp = "";
            if (len > buf.length) {
                len = buf.length;
            }
            for (int i = 0; i < len; i++) {
                tmp += String.format("%02x,", buf[i]);
            }
            return tmp;
        }

        private boolean OperationWait(int ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }

        //==============================================================================================
        void KeyReceivedCallback(byte keys[]) {
        }

        //==============================================================================================
        private void EnableAllNotificationThread() {
            OS_PRINTF("Start enable all notification thread...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    fEnableNotificaThreadIsRun = true;
                    OperationWait(500);
                    if ((mBluetoothGatt != null) && (VendorIn_Characteristic != null)) {
                        OS_PRINTF("Enable Vendor In Notification.");
                        try {
                            setCharacteristicNotification(VendorIn_Characteristic, null, true);
                        } catch (Exception e) {
                            OS_PRINTF("Err:vendor In Notification err.");
                        }
                    }
                    OS_PRINTF("End enable all notification thread.");
                    fEnableNotificaThreadIsRun = false;
                }
            }).start();
        }

    //c层回调上来的语音数据方法
    public void OnDataReceived(byte[] buffer, int size) {
        //自行执行回调后的操作
        OS_PRINTF("OnDataReceived: " + size);
        if (mCallbacks == null) {
            OS_PRINTF("mCallbacks is null");
            return;
        }
        final int len = mCallbacks.beginBroadcast();
        if (len > 1) {
            OS_PRINTF("Too many clients");
            return;
        }

        for (int i = 0; i < len; i++) {
            try {
                mCallbacks.getBroadcastItem(i).VoiceDataEvent(buffer,size);    // 通知回调
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCallbacks.finishBroadcast();
    }

    //c层回调上来的按键事件方法
    public void OnVoiceKeyAction(int action) {
        //自行执行回调后的操作
        OS_PRINTF("====>OnVoiceKeyAction: " + action);
        if(mCallbacks == null) {
            OS_PRINTF("mCallbacks is null");
            return;
        }
        final int len = mCallbacks.beginBroadcast();
        if (len > 1) {
            OS_PRINTF("Too many clients");
            return;
        }
        for (int i = 0; i < len; i++) {
            try {
                mCallbacks.getBroadcastItem(i).VoiceKeyEvent(action);    // 通知回调
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCallbacks.finishBroadcast();
    }

    /*-----------------------------------------------------------------------------
     Function Name:
     Input		:


     Output		:
     Return 		:
     Describe		:创建IPC binder
     -------------------------------------------------------------------------------*/
    private final INanoMethod.Stub binder = new INanoMethod.Stub() {

        /*提供registerActivity方法*/
        public void registerActivity() throws RemoteException {
            OS_PRINTF("registerActivity");
            // mMyActivity = MyActivity;
        }
        /*提供registerCallBack方法*/
        public void registerCallBack(INanoMethodCallback callback) throws RemoteException {
            OS_PRINTF("registerCallBack");
            mCallbacks.register(callback);
        }
        /*提供RegisterCallBack方法*/
        public void unregisterCallBack(INanoMethodCallback callback) throws RemoteException {
            OS_PRINTF("unregisterCallBack");
            mCallbacks.unregister(callback);
        }
    };
    //调到C层的方法
    private native int NanoOpen();
    private native int NanoClose();
    private native void NanoProcData(byte[] data,int datelen);
}
