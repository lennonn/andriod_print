package com.will.bluetoothprinterdemo.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.will.bluetoothprinterdemo.R;
import com.will.bluetoothprinterdemo.utils.BluetoothUtil;
import com.will.bluetoothprinterdemo.utils.ConnectBlue;
import com.will.bluetoothprinterdemo.utils.PrintUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private WebView mWebView;
    private BluetoothSocket mSocket;
    private BasePrintActivity.BluetoothStateReceiver mBluetoothStateReceiver;
    private AsyncTask mConnectTask;
    private ProgressDialog mProgressDialog;
    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView) findViewById(R.id.webview);
        //mWebView.loadUrl("file:///android_asset/js_java_interaction.html");//加载本地asset下面的js_java_interaction.html文件
        mWebView.loadUrl("http://1u52s05192.51mypc.cn:9081/yulinchemical/jsp/android/login.jsp");//加载本地assets下面的js_java_interaction.html文件
        //mWebView.loadUrl("http://192.168.1.19:8080/yulinchemical/jsp/android/login.jsp");//加载本地assets下面的js_java_interaction.html文件

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);//打开js支持
        /**
         * 打开js接口給H5调用，参数1为本地类名，参数2为别名；h5用window.别名.类名里的方法名才能调用方法里面的内容，例如：window.android.back();
         * */
        mWebView.addJavascriptInterface(new JsInteration(), "android");
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());
    }


    final class MyWebViewClient extends WebViewClient{
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.contains("androidCallback")){
                onClick(view);
            }
            view.loadUrl(url);
           //
            return true;
        }
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d("WebView","onPageStarted");
            super.onPageStarted(view, url, favicon);
        }
        public void onPageFinished(WebView view, String url) {
            Log.d("WebView","onPageFinished ");
            super.onPageFinished(view, url);
            /*if(url.contains("ladingNoticeId")){
                onClick(view);
            }*/
        }

    }

    final class InJavaScriptLocalObj {
        public void showSource(String html) {
            Log.d("HTML", html);
        }
    }

    /**
     * 自己写一个类，里面是提供给H5访问的方法
     * */
    public class JsInteration {

        @JavascriptInterface//一定要写，不然H5调不到这个方法
        public String back() {
            return "我是java里的方法返回值";
        }
    }

    /**
     * 获取所有已配对的打印类设备
     */
    public static List<BluetoothDevice> getPairedPrinterDevices() {
        return getSpecificDevice(BluetoothClass.Device.Major.IMAGING);
    }

    /**
     * 从已配对设配中，删选出某一特定类型的设备展示
     * @param deviceClass
     * @return
     */
    public static List<BluetoothDevice> getSpecificDevice(int deviceClass){
        List<BluetoothDevice> devices = BluetoothUtil.getPairedDevices();
        List<BluetoothDevice> printerDevices = new ArrayList<>();

        for (BluetoothDevice device : devices) {
            BluetoothClass klass = device.getBluetoothClass();
            // 关于蓝牙设备分类参考 http://stackoverflow.com/q/23273355/4242112
            if (klass.getMajorDeviceClass() == deviceClass)
                printerDevices.add(device);
        }

        return printerDevices;
    }


    /**
     * 获取所有已配对的设备
     */
    public static List<BluetoothDevice> getPairedDevices() {
        List deviceList = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device);
            }
        }
        return deviceList;
    }
    //点击按钮，访问H5里带返回值的方法
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onClick(View v) {
        Log.e("TAG", "onClick: ");

//    mWebView.loadUrl("JavaScript:show()");//直接访问H5里不带返回值的方法，show()为H5里的方法


        //传固定字符串可以直接用单引号括起来
       // mWebView.loadUrl("javascript:alertMessage('哈哈')");//访问H5里带参数的方法，alertMessage(message)为H5里的方法

        //当出入变量名时，需要用转义符隔开
       // String content="9880";
      //  mWebView.loadUrl("javascript:alertMessage(\""   +content+   "\")"   );


        //Android调用有返回值js方法，安卓4.4以上才能用这个方法
        mWebView.evaluateJavascript("loadingInfo()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                Log.d(TAG, "js返回的结果为=" + value);
                //Toast.makeText(MainActivity.this,"js返回的结果为=" + value,Toast.LENGTH_LONG).show();

                List<BluetoothDevice> printerDevices = getPairedDevices();
                value = value.substring(1,value.length()-1);
                String[] printContent=value.split(";");
                if(!BluetoothUtil.isBluetoothOn()){
                    mWebView.loadUrl("javascript:devicesConnectInfo('蓝牙未打开，请打开蓝牙','" + printContent[1] + "')");
                }else {
                    BluetoothSocket socket = null;
                    for (BluetoothDevice bd : printerDevices) {
                        socket = BluetoothUtil.connectDevice(bd);
                        if (socket != null) break;
                    }
                    if (socket == null) {
                        mWebView.loadUrl("javascript:devicesConnectInfo('没有可用的蓝牙设备','" + printContent[1] + "')");

                    } else {
                        PrintUtil.printTest(socket, null, printContent[0]);
                        mWebView.loadUrl("javascript:printFinishCall('" + printContent[1] + "')");
                    }
                }
            }
        });


    }


}
