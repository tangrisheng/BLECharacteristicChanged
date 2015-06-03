package com.trs.ble.characteristic.changed;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.List;
import java.util.UUID;


public class MainActivity extends Activity implements View.OnClickListener {

    Button connectButton;

    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    BluetoothGattService service;


    String writeCharacteristicUUID = null;
    String watchCharacteristicUUID = null;

    String serviceUUID = null;

    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // this is the mac address of the bluetooth device you want to connect.
    String mac = "11:22:33:44:55:66";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    void initCtrl() {
        connectButton = (Button) findViewById(R.id.btn_connect);
        connectButton.setOnClickListener(this);
    }

    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_connect:
                connectBTDevice();
                break;
        }
    }

    void connectBTDevice() {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(mac);
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
    }

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // if connect success,start discovering services.
                    gatt.discoverServices();
                }
                if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // disconnect.
                }
            } else if (status == BluetoothGatt.GATT_FAILURE) {
                // connect failure.
            } else if (status == 133) {
                // this is a status 133.some android devices return this code.
            } else {
                // other status,failure.
            }
            super.onConnectionStateChange(gatt, status, newState);
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            // 获取服务成功,读取 Base 和 Sensor 的设置
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // get service.
                service = gatt.getService(UUID.fromString(serviceUUID));
                listenCharacteristicChanged();
            } else {
                // discovery services failure.
            }
            super.onServicesDiscovered(gatt, status);
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            // write uuid success callback.
            if (characteristic.getUuid().equals(UUID.fromString(writeCharacteristicUUID))) {
                // you should judge the uuid.
            }
            super.onCharacteristicWrite(gatt, characteristic, status);
        }


        /**
         * BLE 特征值更新
         *
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // Characteristic change.
            if (characteristic.getUuid().equals(UUID.fromString(watchCharacteristicUUID))) {
                // you should judge the uuid.
            }
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (descriptor.getUuid().equals(CLIENT_CHARACTERISTIC_CONFIG)) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // listen Characteristic change success.
                }
            }
            super.onDescriptorWrite(gatt, descriptor, status);
        }

    };

    /**
     * 监听加速度传感器是否运动
     */
    public void listenCharacteristicChanged() {
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(watchCharacteristicUUID));
        bluetoothGatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor brightDescriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        brightDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(brightDescriptor);
    }
}
