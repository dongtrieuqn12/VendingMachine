package com.felhr.serialportexample.Others;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.serialportexample.R;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class test extends Activity {

    private UsbManager usbManager;
    private UsbService usbService;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialDevice;
    private MyHandler myHandler;
    private String buffer = "";
    private static final int USB_VENDOR_ID = 0x04E8; // A7
    private static final int USB_PRODUCT_ID = 0x6860;

    private static final String TAG = "hodongtrieu";

    private static boolean checkDevice = false;

    Button send;
    EditText ed_text;
    TextView textSerial;

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(myHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        myHandler = new MyHandler(this,textSerial);

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        initView();
    }

    private void initView() {
        send = findViewById(R.id.send);
        ed_text = findViewById(R.id.ed_text);
        textSerial = findViewById(R.id.textSerial);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUsbConnection();
        SetOnClickButton();
        if(checkDevice == true) {
            Timer timerObj = new Timer();
            TimerTask timerTaskObj = new TimerTask() {
                public void run() {
                    //perform your action here
                    serialDevice.read(callback);
                }
            };
            timerObj.schedule(timerTaskObj, 500, 15000);
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<test> mActivity;
        private TextView text;

        public MyHandler(test activity,TextView text) {
            mActivity = new WeakReference<>(activity);
            this.text = text;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    //Toast.makeText(MainActivity.this,data,Toast.LENGTH_LONG).show();
                    text.setText(data + "\n");
                    break;
            }
        }
    }

    private void SetOnClickButton() {
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ed_text.getText().toString().equals(null)) {
                    //do nothing
                } else {
                    serialDevice.write(HexStrToByteArray(ed_text.getText().toString()));
                    textSerial.setText(ed_text.getText().toString() + "\n");
                    Log.d(TAG,HexStrToByteArray(ed_text.getText().toString()) + "");
                }
            }
        });
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        unregisterReceiver(usbDetachedReceiver);
//        stopUsbConnection();
//    }

    private final BroadcastReceiver usbDetachedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null && device.getVendorId() == USB_VENDOR_ID && device.getProductId() == USB_PRODUCT_ID) {
                    Log.i(TAG, "USB device detached");
                    Toast.makeText(test.this,"USB device detached",Toast.LENGTH_LONG).show();
                    stopUsbConnection();
                }
            }
            Toast.makeText(test.this,"USB device not detached",Toast.LENGTH_LONG).show();
        }
    };


    private UsbSerialInterface.UsbReadCallback callback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] data) {
            try {
                String dataUtf8 = new String(data, "UTF-8");
                buffer += dataUtf8;
                int index;
                while ((index = buffer.indexOf('\n')) != -1) {
                    final String dataStr = buffer.substring(0, index + 1).trim();
                    buffer = buffer.length() == index ? "" : buffer.substring(index + 1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onSerialDataReceived(dataStr);
                        }
                    });
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Error receiving USB data", e);
            }
        }

    };

    private void startUsbConnection() {
        Map<String, UsbDevice> connectedDevices = usbManager.getDeviceList();
        if (!connectedDevices.isEmpty()) {
            for (UsbDevice device : connectedDevices.values()) {
                if ( (device.getVendorId() == USB_VENDOR_ID && device.getProductId() == USB_PRODUCT_ID) || (device.getVendorId() == 0x0bb4 && device.getProductId() == 0x0C03)) {
                    Log.i(TAG, "Device found: " + device.getDeviceName());
                    Toast.makeText(test.this,"Device found: " + device.getDeviceName(),Toast.LENGTH_LONG).show();
                    startSerialConnection(device);
                    return;
                }
            }
            Toast.makeText(test.this,"okokok",Toast.LENGTH_LONG).show();
        }
        Log.w(TAG, "Could not start USB connection - No devices found");
        Toast.makeText(test.this,"Could not start USB connection - No devices found",Toast.LENGTH_LONG).show();
    }

    private void startSerialConnection(UsbDevice device) {
        Log.i(TAG, "Ready to open USB device connection");
        Toast.makeText(test.this,"Ready to open USB device connection",Toast.LENGTH_LONG).show();
        connection = usbManager.openDevice(device);
        serialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection);
        if (serialDevice != null) {
            if (serialDevice.open()) {
                serialDevice.setBaudRate(115200);
                serialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8);
                serialDevice.setStopBits(UsbSerialInterface.STOP_BITS_1);
                serialDevice.setParity(UsbSerialInterface.PARITY_NONE);
                serialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
//                serialDevice.write(HexStrToByteArray("1234"));
//                serialDevice.read(callback);
                Log.i(TAG, "Serial connection opened");
                Toast.makeText(test.this,"Serial connection opened",Toast.LENGTH_LONG).show();
                checkDevice = true;
            } else {
                Log.w(TAG, "Cannot open serial connection");
            }
        } else {
            Log.w(TAG, "Could not create Usb Serial Device");
        }
    }

    private void onSerialDataReceived(String data) {
        // Add whatever you want here
        Log.i(TAG, "Serial data received: " + data);
        Toast.makeText(this,"Serial data received: " + data,Toast.LENGTH_LONG).show();
    }


    private void stopUsbConnection() {
        try {
            if (serialDevice != null) {
                serialDevice.close();
            }

            if (connection != null) {
                connection.close();
            }
        } finally {
            serialDevice = null;
            connection = null;
        }
    }

    public static byte[] HexStrToByteArray(String str){
        int len = str.length();
        byte[] data = new byte[len/2];
        for (int i = 0; i < len; i +=2){
            data[i/2] = (byte) ((Character.digit(str.charAt(i),16) << 4)
                    + Character.digit(str.charAt(i+1), 16));
        }
        return data;
    }

}
