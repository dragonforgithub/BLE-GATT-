package com.android.NanoClient;

import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.android.NanoServer.INanoMethod;
import com.android.NanoServer.INanoMethodCallback;

public class MainActivity extends AppCompatActivity {

    private boolean mBound = false;
    private INanoMethod NanoMethodService;

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
        Log.d("==========>NanoAppClient ", str);
    }

    static
    {
        System.loadLibrary("nanoApp");
        System.loadLibrary("nanoApp_jni");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*System.load("/data/data/com.android.NanoClient/lib/libnanoApp.so");
        System.load("/data/data/com.android.NanoClient/lib/libnanoApp_jni.so");*/

        NanoOpen();

        super.onCreate(savedInstanceState);
        //requestPermission();
        setContentView(R.layout.activity_main);

        /**
         * 注册回调按钮
         */
        findViewById(R.id.registerCallback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBound) {
                    //alert("未连接到远程服务");
                    return;
                }
                try {
                    if (NanoMethodService != null) {
                        //调用函数registerCallBack
                        NanoMethodService.registerCallBack(mCallback);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        /**
         * 卸载回调按钮
         */
        findViewById(R.id.unregisterCallback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBound) {
                    //alert("未连接到远程服务");
                    return;
                }
                try {
                    if (NanoMethodService != null) {
                        //调用函数unregisterCallBack
                        NanoMethodService.unregisterCallBack(mCallback);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        OS_PRINTF("onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onStart () {
        OS_PRINTF("onStart");
        if (!mBound) {
            OS_PRINTF("BindService");
            attemptToBindService();
        }
        super.onStart();
    }

    @Override
    protected void onStop () {
        OS_PRINTF("onStop");
        if (mBound) {
            OS_PRINTF("unbindService");
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }
    /**
     * 尝试与服务端建立连接
     */
    private void attemptToBindService() {
        Intent intent = new Intent();
        intent.setAction("com.android.aidl.server");
        intent.setPackage("com.android.NanoServer");
        //startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NanoMethodService = INanoMethod.Stub.asInterface(service);
            if (NanoMethodService != null) {
                mBound = true;
            }

            OS_PRINTF("client connected");
            //textView1 = (TextView) findViewById(R.id.textView);
            //textView1.setText("hello");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            OS_PRINTF("nano-client disconnected");
            mBound = false;
        }
    };

    private INanoMethodCallback mCallback = new INanoMethodCallback.Stub() {
        /**
         * 事件回调函数
         */
        public void VoiceDataEvent(byte[] data, int datalen) throws RemoteException {
            //OS_PRINTF("数据接收 : "+bytesToHexString(data));
            OS_PRINTF("VoiceDataEvent " + datalen);
        }

        /**
         * 事件回调函数
         */
        public void VoiceKeyEvent(int keycode) throws RemoteException {
            OS_PRINTF("VoiceKeyEvent " + keycode);
        }
    };//逗号不能少

    //c层回调上来的语音数据方法
    public void OnDataReceived(byte[] buffer, int size) {
        //自行执行回调后的操作
        OS_PRINTF("size: " + size);
    }

    //c层回调上来的按键事件方法
    public void OnVoiceKeyAction(int action) {
        //自行执行回调后的操作
        OS_PRINTF("action: " + action);
    }

    //调到C层的方法
    private native int NanoOpen();

    private native void NanoProcData(byte[] data,int datelen);
}