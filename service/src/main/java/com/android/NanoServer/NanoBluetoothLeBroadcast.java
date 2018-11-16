package com.android.NanoServer;

import android.annotation.SuppressLint;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

@SuppressLint("NewApi")
public class NanoBluetoothLeBroadcast extends BroadcastReceiver {

    private final String TAG = "NanoBluetoothLeBroadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action == null) return;

        //adb shell am broadcast -a android.action.nano.START
        //在Android TV 版本上无法开机接收到蓝牙开启广播，只能监听开机广播启动配对服务
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            enablePairService(context, true);
        } else if (action.equals("android.action.nano.START")) {
            enablePairService(context, true);
        } else if (action.equals("android.action.nano.STOP")) {
            enablePairService(context, false);
        }
    }

    public void enablePairService(Context context, boolean pair) {
        Intent autoPairService = new Intent(context, com.android.NanoServer.NanoBluetoothLeService.class);
        if(pair){
            context.startService(autoPairService);
        } else {
            context.stopService(autoPairService);
        }
    }

}