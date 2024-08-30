package com.example.navigationleftexample.ui.bluetooth;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.navigationleftexample.R;

import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {
    public BluetoothLeService() {
    }

    private int connectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";


    public static String SERVICE_UUID = BLEUUID.SERVICE;
    public static UUID DIRECTION_CHARACTERISTIC_UUID;
    public static UUID STEERING_ANGLE_CHARACTERISTIC_UUID;
    public static UUID CAR_SPEED_CHARACTERISTIC_UUID;

//    private Handler mainHandler = new Handler(getMainLooper());

    private Binder binder = new LocalBinder();

    private BluetoothAdapter bluetoothAdapter;

    public static final String TAG = "BluetoothLeService";

    private static BluetoothGatt bluetoothGatt;

    private static BluetoothGattService mService;

    List<BluetoothGattCharacteristic> gattCharacteristics;

    public static BluetoothGattService getmService() {
        return mService;
    }

    public static BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public static class BLEUUID {
        public static final String SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb"; //  GATT CAR DEVICE SERVICE
        public static final String DIRECTION_CHARACTERISTIC = "0000fff5-0000-1000-8000-00805f9b34fb";
        public static final String STEERING_ANGLE_CHARACTERISTIC = "0000fff6-0000-1000-8000-00805f9b34fb";
        public static final String CAR_SPEED_CHARACTERISTIC = "0000fff7-0000-1000-8000-00805f9b34fb";
    }


    // Implement my Broadcast for Eveniments
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

//    // Implement my Broadcast for Characteristics
//    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
//        final Intent intent = new Intent(action);
//
//        if (PIN_CHARACTERISTIC.equals(characteristic.getUuid())) {
//            final String pin = characteristic.getStringValue(0);
//            intent.putExtra(EXTRA_DATA, String.valueOf(pin));
//        } else {
//            // For all other profiles, writes the data formatted in HEX.
//            final byte[] data = characteristic.getValue();
//            if (data != null && data.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for (byte byteChar : data)
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//            }
//        }
//        sendBroadcast(intent);
//    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            // Return this instance of LocalService so clients can call public methods.
            return BluetoothLeService.this;
        }
    }

    public boolean initialize() {

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            //  Notify the user that Mobile cell doesn't have bluetooth  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
        //throw new UnsupportedOperationException("Not yet implemented");
    }


    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                connectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                // Attempts to discover services after successful connection.

                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                // Make a notification !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                connectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (BluetoothGattService service : gatt.getServices()) {
                    if ((service == null) || (service.getUuid() == null)) {
                        continue;
                    }
                    if (BLEUUID.SERVICE.equalsIgnoreCase(service.getUuid().toString())) {
                        mService = service;
                    }
                }

                // get Characteristics
                gattCharacteristics = mService.getCharacteristics();


                for (BluetoothGattCharacteristic gattCharacteristic :
                        gattCharacteristics) {
                    UUID uid = gattCharacteristic.getUuid();

                    if (uid.toString().equalsIgnoreCase(BLEUUID.DIRECTION_CHARACTERISTIC)) {
                        DIRECTION_CHARACTERISTIC_UUID = uid;
                    } else if (uid.toString().equalsIgnoreCase(BLEUUID.STEERING_ANGLE_CHARACTERISTIC)) {
                        STEERING_ANGLE_CHARACTERISTIC_UUID = uid;
                    } else if (uid.toString().equalsIgnoreCase(BLEUUID.CAR_SPEED_CHARACTERISTIC)) {
                        CAR_SPEED_CHARACTERISTIC_UUID = uid;
                    }

                }

//                byte[] bytes = {(byte) 204};
//                sendCharacteristic(bytes, STEERING_ANGLE_CHARACTERISTIC_UUID);


                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }


//        // After Calling readCharateristic() results are hier
//
//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//                broadcastUpdate(EXTRA_DATA, characteristic); // ???????????????????????????????????????
//                Log.i("CARAC", "CARACTERISTICA LEIDA onCharacteristicRead()");
//            }
//        }


//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            readCharacteristic(characteristic);
//            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//        }


    };

    // Return Luist of GATT Services
    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) return null;
        return bluetoothGatt.getServices();
    }


    // Connect to a device
    public boolean connect(final String address) {


        // Check if Bluetooth adapter is not null
        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        try {
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            // connect to the GATT server on the device
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                //return TODO ;
            }
            bluetoothGatt = device.connectGatt(this, true, bluetoothGattCallback);
            return true;
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Device not found with provided address.  Unable to connect.");
            return false;
        }
    }

    //  To  put somewhere !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public void disconnect() {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        bluetoothGatt.disconnect();
    }

    //  To  put somewhere !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public void close() {
        if (bluetoothGatt == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
        Log.e("CIERRE", "CONEXION CERRADA");
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);
        Log.i("READ", "CHARACTERISTIC WAS RED");
    }

//    //  Set notiofication for characteritic
//    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
//        if (bluetoothAdapter == null || bluetoothGatt == null) {
//            return;
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
//        if (PIN_CHARACTERISTIC.equals(characteristic.getUuid())){
//            BluetoothGattDescriptor descriptor =   new BluetoothGattDescriptor(UUID.nameUUIDFromBytes(BLEUUID.CONFIG_UUID.getBytes()),
//                    BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED);
//
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            bluetoothGatt.writeDescriptor(descriptor);
//        }
//    }

//    // Set Characteristic new Value
//    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
//    public void sendCharacteristic(byte[] value, UUID uuid) {
//
//
//        BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) mService.getCharacteristic(uuid);
//
//
//        ch.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            bluetoothGatt.writeCharacteristic(ch, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//        }
//
//
//    }





//    public void sendCharacteristic(String pin, BluetoothDevice device){
//
//        byte[] pinByte = pin.getBytes();
//        int pinInt = Integer.valueOf(pin);
//
//        BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) mService.getCharacteristic(UUID
//                .fromString(BLEUUID.PIN_CHARACTERISTIC_UUID));
//
//        ch.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//
//        ch.setValue(pin);
//
//        Toast.makeText(context, "CARACTERISTICA ASIGNADA", Toast.LENGTH_SHORT).show();
//        connect(device.getAddress());
//        bluetoothGatt = device.connectGatt(this, false, mGattCallback);
//        setCharacteristicNotification(ch, true);
//
//        if (bluetoothGatt.writeCharacteristic(ch)) {
//            Toast.makeText(context, "CARACTERISTICA ESCRITA", Toast.LENGTH_SHORT).show();
//        }
//
//        bluetoothGatt.readCharacteristic(ch);
//        byte[] value = ch.getValue();
//
//        String result = new String(value);
//        Toast.makeText(context, result,  Toast.LENGTH_LONG);
//    }


}