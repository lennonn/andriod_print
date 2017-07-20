package com.will.bluetoothprinterdemo.utils;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.will.bluetoothprinterdemo.R;
import com.will.bluetoothprinterdemo.ui.BasePrintActivity;

/**
 * Created by Administrator on 2017/7/20.
 */

public class ConnectBlue extends BasePrintActivity{
    @Override
    public void onConnected(BluetoothSocket socket, int taskType) {
        switch (taskType){
            case 2:
                Intent intent=getIntent();
                String content=intent.getStringExtra("content");
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);
                PrintUtil.printTest(socket, bitmap,content);
                break;
        }
    }
}
