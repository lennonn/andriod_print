package com.will.bluetoothprinterdemo.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2017/7/20.
 */

public class BluetoothStateReceiver  extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
        switch (state) {
            case BluetoothAdapter.STATE_TURNING_ON:
                toast("蓝牙已开启");
                break;

            case BluetoothAdapter.STATE_TURNING_OFF:
                toast("蓝牙已关闭");
                break;
        }
        onBluetoothStateChanged(intent);
    }
}
