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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.navigationleftexample.databinding.FragmentBluetoothBinding;
import com.example.navigationleftexample.databinding.FragmentHomeBinding;
import com.example.navigationleftexample.ui.bluetooth.SampleGattAttributes;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.example.navigationleftexample.R;
import com.example.navigationleftexample.ui.home.HomeFragment;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Hex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import kotlin.text.HexFormat;

public class BluetoothLeService extends Service {
    public BluetoothLeService() {
    }

    private int connectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final long READ_MAGNETOMETR_CHARACTERISTIC_ITERATION=400;
    private static final long READ_TEMP_HUMIDITY_CHARACTERISTIC_ITERATION=1000;
    private final double[][] euler = {{1.225434,-0.028676,0.069206},{-0.028676,1.087208,0.045837},{0.069206,0.045837,1.057089}};

    private ReadCharacteristicTempHumidityThread readCharacteristicTempHumidityThread = null;
    private ReadCharacteristicMagnetometerThread readCharacteristicMagnetometerThread = null;
    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";


    public static String SERVICE_UUID = BLEUUID.SERVICE;
    public static UUID CAR_SPEED_FORWARD_CHARACTERISTIC_UUID;
    public static UUID CAR_SPEED_BACKWARD_CHARACTERISTIC_UUID;
    public static UUID STEERING_ANGLE_CHARACTERISTIC_UUID;
    public static UUID CAR_BUZZER_CHARACTERISTIC_UUID;
    public static UUID CAR_MAGNETOMETER_CHARACTERISTIC_UUID;
    public static UUID CAR_TEMP_HUMIDITY_CHARACTERISTIC_UUID;
//    public static UUID CAR_TEMP_HUMIDITY_NOTIFICATION_CHARACTERISTIC_UUID;
//    public static UUID CAR_MAGNETOMETER_NOTIFICATION_CHARACTERISTIC_UUID;

//    private Handler mainHandler = new Handler(getMainLooper());

    // ----------------------------------------Data------------------------------------------------------------------------------------

    public byte[] tempHumidityNotificationData;
    public byte[] magnetometerNotificationData;
    public byte[] buzzerWrittenData;





    private Float tempData;
    private Float humidityData;

    private Binder binder = new LocalBinder();

    private BluetoothAdapter bluetoothAdapter;

    public static Boolean mDeviceBusy = false;
    public static final String TAG = "BluetoothLeService";

    private static BluetoothGatt bluetoothGatt;

    private static BluetoothGattService mService;

    List<BluetoothGattCharacteristic> gattCharacteristics;

    List<BluetoothGattDescriptor> gattDescriptors;

    public static BluetoothGattService getmService() {
        return mService;
    }

    public static BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }



    public static List<BluetoothGattCharacteristic> wtiteCharBuzzer = new ArrayList<>();
    public static List<byte[]> wtiteCharBuzzerValue = new ArrayList<>();
    public static List<BluetoothGattCharacteristic> wtiteStearingAngle = new ArrayList<>();
    //-------------------------------------------------------------------------
    public static List<byte[]> executeCharValueArray = new ArrayList<>();

    public static List<BluetoothGattCharacteristic> executeCharacteristicList = new ArrayList<>();



//        public  BluetoothGattService getmService() {
//            return mService;
//    }
//
//
//    public  BluetoothGatt getBluetoothGatt() {
//        return bluetoothGatt;
//    }




    private BluetoothViewModel bluetoothFragmentViewModel;

    Intent intentService;

    public static final String ACTION_NOTIFICATION_RECEIVED_TRMP_HUMIDITY = "ACTION_NOTIFICATION_RECEIVED_TRMP_HUMIDITY";
    public static final String ACTION_NOTIFICATION_RECEIVED_MAGNETOMETER = "ACTION_NOTIFICATION_RECEIVED_MAGNETOMETER";
    public static final String ACTION_NOTIFICATION_RECEIVED_BUZZER_VALUE = "ACTION_NOTIFICATION_RECEIVED_BUZZER_VALUE";
    List<BluetoothGattCharacteristic> notificationChars = new ArrayList<>();
    public static List<BluetoothGattCharacteristic> notificationDeactivateChars = new ArrayList<>();

    String tempString;
    String humidityString;
    public static String convertByteToHexadecimal(byte[] byteArray)
    {
        String hex = "";

        // Iterating through each byte in the array
        for (byte i : byteArray) {
            hex += String.format("%02X", i);
        }

        return hex;
    }




    // It is deprecated. UUID are always updated
    public static class BLEUUID {
        public static final String SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb"; //  GATT CAR DEVICE SERVICE
        public static final String STEERING_ANGLE_CHARACTERISTIC = "0000ffe1-0000-1000-8000-00805f9b34fb";
        public static final String CAR_SPEED_FORWARD_CHARACTERISTIC = "0000ffe2-0000-1000-8000-00805f9b34fb";
        public static final String CAR_SPEED_BACKWARD_CHARACTERISTIC = "0000ffe3-0000-1000-8000-00805f9b34fb";
        public static final String CAR_BUZZER_CHARACTERISTIC = "0000ffe4-0000-1000-8000-00805f9b34fb";
        public static final String CAR_TEMP_HUMIDITY_CHARACTERISTIC = "0000ffe5-0000-1000-8000-00805f9b34fb";
        public static final String CAR_MAGNETOMETER_CHARACTERISTIC = "0000ffe6-0000-1000-8000-00805f9b34fb";
//        public static final String CAR_TEMP_HUMIDITY_NOTIFICATION_CHARACTERISTIC = "0000ffe5-0000-1000-8000-00805f9b34fb";
//        public static final String CAR_MAGNETOMETER_NOTIFICATION_CHARACTERISTIC = "0000ffe6-0000-1000-8000-00805f9b34fb";
//        public static final String CAR_NOTIFICATION_CCCD_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    }


//    public static void unSubscribeToCharacteristics(BluetoothGatt gatt) {
//
//        if (bluetoothAdapter == null || bluetoothGatt == null) {
//            return;
//        }
//        if(notificationDeactivateChars.size() == 0) return;
//        BluetoothGattCharacteristic characteristic = notificationDeactivateChars.get(0);
////        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
////            // TODO: Consider calling
////            //    ActivityCompat#requestPermissions
////            // here to request the missing permissions, and then overriding
////            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
////            //                                          int[] grantResults)
////            // to handle the case where the user grants the permission. See the documentation
////            // for ActivityCompat#requestPermissions for more details.
////            return;
////        }
//        //gatt.setCharacteristicNotification(characteristic, true);
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BLEUUID.CAR_NOTIFICATION_CCCD_DESCRIPTOR));
////            // get Characteristics
////            gattDescriptors = ch.getDescriptors();
//
//        if(descriptor != null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                gatt.writeDescriptor(descriptor);
//            }
//        }
//    }




//    private void sendDataToActivity()
//    {
//        Intent sendBluetoothData = new Intent();
//        sendBluetoothData.setAction("BLUETOOTH_DATA");
//        sendBluetoothData.putExtra( "TEMP_DATA",tempData);
//        LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(sendBluetoothData);
//
//    }


//    // Implement my Broadcast for Eveniments
//    private void broadcastUpdate(final String action) {
//        final Intent intent = new Intent(action);
//        sendBroadcast(intent);
//    }

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
        public BluetoothLeService getService() {
            // Return this instance of LocalService so clients can call public methods.
            return BluetoothLeService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {

        Log.e(TAG, "Service is unbinded");
        return super.onUnbind(intent);
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
        intentService = intent;

        return binder;
        //throw new UnsupportedOperationException("Not yet implemented");
    }


    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                connectionState = STATE_CONNECTED;
                //broadcastUpdate(ACTION_GATT_CONNECTED);
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
                //broadcastUpdate(ACTION_GATT_DISCONNECTED);
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

                // Find instances for UUID if we know UUID String
                for (BluetoothGattCharacteristic gattCharacteristic :
                        gattCharacteristics) {
                    UUID uid = gattCharacteristic.getUuid();

                    if (uid.toString().equalsIgnoreCase(BLEUUID.STEERING_ANGLE_CHARACTERISTIC)) {
                        STEERING_ANGLE_CHARACTERISTIC_UUID = uid;
                    } else if (uid.toString().equalsIgnoreCase(BLEUUID.CAR_SPEED_FORWARD_CHARACTERISTIC)) {
                        CAR_SPEED_FORWARD_CHARACTERISTIC_UUID = uid;
                    } else if (uid.toString().equalsIgnoreCase(BLEUUID.CAR_SPEED_BACKWARD_CHARACTERISTIC)) {
                        CAR_SPEED_BACKWARD_CHARACTERISTIC_UUID = uid;
                    } else if (uid.toString().equalsIgnoreCase(BLEUUID.CAR_BUZZER_CHARACTERISTIC)) {
                        CAR_BUZZER_CHARACTERISTIC_UUID = uid;
                    } else if (uid.toString().equalsIgnoreCase(BLEUUID.CAR_TEMP_HUMIDITY_CHARACTERISTIC)) {
                        CAR_TEMP_HUMIDITY_CHARACTERISTIC_UUID = uid;
                    } else if (uid.toString().equalsIgnoreCase(BLEUUID.CAR_MAGNETOMETER_CHARACTERISTIC)) {
                        CAR_MAGNETOMETER_CHARACTERISTIC_UUID = uid;
                    }


//                    } else if (uid.toString().equalsIgnoreCase(BLEUUID.CAR_TEMP_HUMIDITY_NOTIFICATION_CHARACTERISTIC)) {
//                        CAR_TEMP_HUMIDITY_NOTIFICATION_CHARACTERISTIC_UUID = uid;
//                        //notificationChars.add(gattCharacteristic);   //  Add notification characteristic to array
//                        //setCharacteristicNotification(CAR_TEMP_HUMIDITY_NOTIFICATION_CHARACTERISTIC_UUID, true);
//                    }  else if (uid.toString().equalsIgnoreCase(BLEUUID.CAR_MAGNETOMETER_NOTIFICATION_CHARACTERISTIC)) {
//                        CAR_MAGNETOMETER_NOTIFICATION_CHARACTERISTIC_UUID = uid;
//                        //notificationChars.add(gattCharacteristic);  //  Add notification characteristic to array
//                        //setCharacteristicNotification(CAR_MAGNETOMETER_NOTIFICATION_CHARACTERISTIC_UUID, true);
//                    }

                }


                readCharacteristicMagnetometerThread = new ReadCharacteristicMagnetometerThread();
                readCharacteristicMagnetometerThread.start();
                readCharacteristicTempHumidityThread = new ReadCharacteristicTempHumidityThread();
                readCharacteristicTempHumidityThread.start();

                //subscribeToCharacteristics(gatt);


            //    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }


        // After Calling readCharateristic() results are hier

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "Characteristic write failed" + characteristic);
                executeCharacteristicList.remove(0);
            } else {
                executeCharacteristicList.remove(0);
            }



            if (CAR_TEMP_HUMIDITY_CHARACTERISTIC_UUID.equals(characteristic.getUuid()) ) {
                tempHumidityNotificationData = characteristic.getValue();

                Intent intent = new Intent(BluetoothLeService.ACTION_NOTIFICATION_RECEIVED_TRMP_HUMIDITY);
                if (tempHumidityNotificationData != null) {
                    intent.putExtra("tempHumidityData", tempHumidityNotificationData);
                }
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

            } else if (CAR_MAGNETOMETER_CHARACTERISTIC_UUID.equals(characteristic.getUuid()) ) {

                magnetometerNotificationData = characteristic.getValue();

                Intent intent = new Intent(BluetoothLeService.ACTION_NOTIFICATION_RECEIVED_MAGNETOMETER);
                intent.putExtra("XYZCompass", magnetometerNotificationData);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else if (CAR_BUZZER_CHARACTERISTIC_UUID.equals(characteristic.getUuid()) ) {
                buzzerWrittenData = characteristic.getValue();

                Intent intent = new Intent(BluetoothLeService.ACTION_NOTIFICATION_RECEIVED_BUZZER_VALUE);
                if (buzzerWrittenData != null) {
                    intent.putExtra("buzzerData", buzzerWrittenData);
                }
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }



            synchronized (mDeviceBusy) {
                mDeviceBusy = false;
            }
        }

        @Override
        public void onDescriptorRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor, int status, @NonNull byte[] value) {
            super.onDescriptorRead(gatt, descriptor, status, value);
        }


        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }


//        @Override
//        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
//            super.onDescriptorWrite(gatt, descriptor, status);
//            if (!notificationChars.isEmpty()) {
//                notificationChars.remove(0);
//                subscribeToCharacteristics(gatt);
//            }
//            if (!notificationDeactivateChars.isEmpty()) {
//                notificationDeactivateChars.remove(0);
//                unSubscribeNoticationCharacteristics(gatt);
//            }
//            if ((notificationDeactivateChars.isEmpty()) && (!wtiteCharBuzzer.isEmpty())){
//
////  PUT A DELAY 300 ms
//
//                try {
//                    Thread.sleep(400);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//
////                final Handler handler = new Handler();
////                handler.postDelayed(new Runnable() {
////                    @Override
////                    public void run() {
////                        // Do something after 5s = 5000ms
////                        if (wtiteCharBuzzer.get(0).getUuid().toString().equals(BLEUUID.CAR_BUZZER_CHARACTERISTIC)) {
////                            int result = gatt.writeCharacteristic(wtiteCharBuzzer.get(0), wtiteCharBuzzerValue.get(0), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
////
////                            if (result != 0) {
////                                Log.w(TAG, "writeCharacteristic() is failed, returns !=0");
////
////                            }
////                        }
////                    }
////                }, 400);
//
//                if (wtiteCharBuzzer.get(0).getUuid().toString().equals(BLEUUID.CAR_BUZZER_CHARACTERISTIC)) {
//                    int result = gatt.writeCharacteristic(wtiteCharBuzzer.get(0), wtiteCharBuzzerValue.get(0), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//
//                    if (result != 0) {
//                        Log.w(TAG, "writeCharacteristic() is failed, returns !=0");
//
//                    }
//                }
//
////                    else if (uuid.toString().equals(BluetoothLeService.BLEUUID.STEERING_ANGLE_CHARACTERISTIC)) {
////                if (BluetoothLeService.wtiteStearingAngle.isEmpty()) {
////                    int result = gatt.writeCharacteristic(ch, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
////
////                    if (result != 0) {
////                        Log.w(TAG, "writeCharacteristic() is failed, returns !=0");
////                    } else {
////                        BluetoothLeService.wtiteStearingAngle.add(ch);
////                    }
////                }
////
////            }
//
//
//            }
//            synchronized (mDeviceBusy) {
//                mDeviceBusy = false;
//            }
//
//
//
//        }
//        private void unSubscribeNoticationCharacteristics(BluetoothGatt bluetoothGatt) {
//
//            if (bluetoothGatt == null) {
//                return;
//            }
//            if(notificationDeactivateChars.size() == 0) return;
//            BluetoothGattCharacteristic characteristic = BluetoothLeService.notificationDeactivateChars.get(0);
//            if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//        bluetoothGatt.setCharacteristicNotification(characteristic, false);
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BluetoothLeService.BLEUUID.CAR_NOTIFICATION_CCCD_DESCRIPTOR));
////            // get Characteristics
////            gattDescriptors = ch.getDescriptors();
//
//            if(descriptor != null) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//                }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                    bluetoothGatt.writeDescriptor(descriptor);
//                }
//            }
//        }
//        private void subscribeToCharacteristics(BluetoothGatt gatt) {
//
//            if (bluetoothAdapter == null || bluetoothGatt == null) {
//            return;
//        }
//            if(notificationChars.size() == 0) return;
//            BluetoothGattCharacteristic characteristic = notificationChars.get(0);
//            if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            gatt.setCharacteristicNotification(characteristic, true);
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BLEUUID.CAR_NOTIFICATION_CCCD_DESCRIPTOR));
////            // get Characteristics
////            gattDescriptors = ch.getDescriptors();
//
//            if(descriptor != null) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                }
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                    gatt.writeDescriptor(descriptor);
//                }
//            }
//        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);


                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.w(TAG, "Characteristic write failed" + characteristic);
                    executeCharacteristicList.remove(0);
                } else {
                    executeCharacteristicList.remove(0);
                }
            synchronized (mDeviceBusy) {
                mDeviceBusy = false;
            }

//            if (CAR_BUZZER_CHARACTERISTIC_UUID.equals(characteristic.getUuid()) ) {
//                readCharacteristic(CAR_BUZZER_CHARACTERISTIC_UUID);
//            }











//            if (characteristic.getUuid().toString().equals(BluetoothLeService.BLEUUID.CAR_BUZZER_CHARACTERISTIC)) {
//                if (status != BluetoothGatt.GATT_SUCCESS) {
//                    Log.w(TAG, "Characteristic write failed" + characteristic);
//                    wtiteCharBuzzer.remove(0);
//                    wtiteCharBuzzerValue.remove(0);
//                } else {
//                    wtiteCharBuzzer.remove(0);
//                    wtiteCharBuzzerValue.remove(0);
//                }
//
//            } else if (characteristic.getUuid().toString().equals(BLEUUID.STEERING_ANGLE_CHARACTERISTIC)) {
//                if (status != BluetoothGatt.GATT_SUCCESS) {
//                    Log.w(TAG, "Characteristic write failed" + characteristic);
//                    wtiteStearingAngle.remove(0);
//                } else {
//                    wtiteStearingAngle.remove(0);
//                }
//
//            }



//            BluetoothGattCharacteristic ch1 = (BluetoothGattCharacteristic) BluetoothLeService.getmService().getCharacteristic(UUID.fromString(BluetoothLeService.BLEUUID.CAR_MAGNETOMETER_NOTIFICATION_CHARACTERISTIC));
//            notificationChars.add(ch1);
//            BluetoothGattCharacteristic ch2 = (BluetoothGattCharacteristic) BluetoothLeService.getmService().getCharacteristic(UUID.fromString(BluetoothLeService.BLEUUID.CAR_TEMP_HUMIDITY_NOTIFICATION_CHARACTERISTIC));
//            notificationChars.add(ch2);
//            subscribeToCharacteristics(gatt);


        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            if (status!= BluetoothGatt.GATT_SUCCESS){
                Log.w(TAG,"Reliable Characteristic write failed");


            }
            super.onReliableWriteCompleted(gatt, status);
        }

//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//
////            synchronized (BluetoothLeService.mDeviceBusy) {
////                if (BluetoothLeService.mDeviceBusy) return;
////                BluetoothLeService.mDeviceBusy = true;
////            }
////
//
//
//            //readCharacteristic(characteristic);
//            if (CAR_TEMP_HUMIDITY_NOTIFICATION_CHARACTERISTIC_UUID.equals(characteristic.getUuid()) ) {
//                tempHumidityNotificationData = characteristic.getValue();
//
//                Intent intent = new Intent(BluetoothLeService.ACTION_NOTIFICATION_RECEIVED_TRMP_HUMIDITY);
//                if (tempHumidityNotificationData != null) {
//                    intent.putExtra("tempHumidityData", tempHumidityNotificationData);
//                }
//                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//
//
//
////                //Data received via Bluetooth is  Little Endian
////
////                ////Calculate humidity, byte1, byte2, 4bits MSB from byte 3
////                long  h =  tempHumidityNotificationData[4]& 0xFF;
////                h <<= 8;
////                h |= tempHumidityNotificationData[3]& 0xFF;
////                h <<= 4;
////                h |= (tempHumidityNotificationData[2] >> 4) & 0x0F;
////                //humidityString = Long.toHexString(h);
////                humidityData = ((float)h * 100) / 0x100000;
////
////
////                //Calculate temp , 4bits LSB from byte 3, byte4, byte5
////                long tdata = (tempHumidityNotificationData[2] & 0x0F);
////                tdata <<= 8;
////                tdata |=  tempHumidityNotificationData[1]& 0xFF;
////                tdata <<= 8;
////                tdata |=  tempHumidityNotificationData[0]& 0xFF;
////                //tempString = Long.toHexString(tdata);
////                tempData = ((float)tdata * 200 / 0x100000) - 50;
////
//////                bluetoothFragmentViewModel.setTempSensor(tempData);
//////                bluetoothFragmentViewModel.setHumiditySensor(humidityData);
////
//////                sendDataToActivity();
////
////                Intent intent = new Intent(BluetoothLeService.ACTION_NOTIFICATION_RECEIVED_TRMP_HUMIDITY);
////                if (tempData != null) {
////                    intent.putExtra("tempData", tempData);
////                }
////                if (tempData != null) {
////                    intent.putExtra("humidityData", humidityData);
////                }
////                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//
//            } else if (CAR_MAGNETOMETER_NOTIFICATION_CHARACTERISTIC_UUID.equals(characteristic.getUuid()) ) {
////                double headingDegrees;
////                double headingDegreesNorm;  // Actual degrees from right to left 360 degree
////                double declinationAngleOffset;  // degrees with offset relatively to vertical axe ( direction of car)
////                double[] pointNotCalibrated = new double[3];
////                double[] pointSubstracted = new double[3];
////                double[] pointCalibrated = new double[3];
////                magnetometerNotificationData = characteristic.getValue();
////
////                //Data received via Bluetooth is  Little Endian
////
////                ////Calculate Magnet, Array is already reversed.
////                long  x =  magnetometerNotificationData[1];
////                x <<= 8;
////                x |= magnetometerNotificationData[0]&0xFF;
////                pointNotCalibrated[0] = Double.valueOf(x);
////
////                long  y =  magnetometerNotificationData[3];
////                y <<= 8;
////                y |= magnetometerNotificationData[2]&0xFF;
////                pointNotCalibrated[1] = Double.valueOf(y);
////
////                long  z =  magnetometerNotificationData[5];
////                z <<= 8;
////                z |= magnetometerNotificationData[4]&0xFF;
////                pointNotCalibrated[2] = Double.valueOf(z);
//
//
////
////                pointSubstracted = substractVectors(pointNotCalibrated, bias);
////
////
////                pointCalibrated = multiplyMatrix(euler,pointSubstracted);
////                //  Axes in the controller are not right. Change them
////                X_Magnetometer_Data =  pointCalibrated[0];
////                Y_Magnetometer_Data =  pointCalibrated[1];
////                Z_Magnetometer_Data =  pointCalibrated[2];
////
////                double headingRadians = Math.atan2(Y_Magnetometer_Data, X_Magnetometer_Data);
////                headingDegrees = (double) (headingRadians * 180 / Math.PI);
////                if (headingDegrees<0) {
////                    headingDegreesNorm = 360 + headingDegrees;
////                } else {
////                    headingDegreesNorm = headingDegrees;
////                }
////                declinationAngleOffset = (offsetAngle + headingDegreesNorm) % 360;
//
//
////                Intent intent = new Intent(BluetoothLeService.ACTION_NOTIFICATION_RECEIVED_MAGNETOMETER);
////                intent.putExtra("angleCompass", declinationAngleOffset);
////                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//                magnetometerNotificationData = characteristic.getValue();
//
//                Intent intent = new Intent(BluetoothLeService.ACTION_NOTIFICATION_RECEIVED_MAGNETOMETER);
//                intent.putExtra("XYZCompass", magnetometerNotificationData);
//                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//
//
//
//
//
////                Intent intent = new Intent(BluetoothLeService.ACTION_NOTIFICATION_RECEIVED);
////                if (tempData != null) {
////                    intent.putExtra("tempData", tempData);
////                }
////                if (tempData != null) {
////                    intent.putExtra("humidityData", humidityData);
////                }
////                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
//
//            }
//
////            synchronized (mDeviceBusy) {
////                mDeviceBusy = false;
////            }
//
//
//
//            // broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
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
        //  Kill thread that is readung Characteristics
        if (readCharacteristicMagnetometerThread!=null) {
            readCharacteristicMagnetometerThread.setRunning(false);
            readCharacteristicMagnetometerThread.interrupt();
        }

        if (readCharacteristicTempHumidityThread!=null) {
            readCharacteristicTempHumidityThread.setRunning(false);
            readCharacteristicTempHumidityThread.interrupt();
        }


        Log.e("CIERRE", "CONEXION CERRADA");
    }

    public boolean readCharacteristic(UUID uuid) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            return false;
        }

        synchronized (BluetoothLeService.mDeviceBusy) {
            if (BluetoothLeService.mDeviceBusy) return false;
            BluetoothLeService.mDeviceBusy = true;
        }

        BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) mService.getCharacteristic(uuid);
        ch.setWriteType(BluetoothGattCharacteristic.PERMISSION_READ);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }


        if (BluetoothLeService.executeCharacteristicList.isEmpty()) {
            boolean result = bluetoothGatt.readCharacteristic(ch);

            if (result == false) {
                Log.w(TAG, "writeCharacteristic() is failed, returns !=0");
                return false;
            } else {
                BluetoothLeService.executeCharacteristicList.add(ch);
            }

            Log.i("READ", "CHARACTERISTIC WAS RED");

            return true;
        } else {
            return false;
        }


//    // Set Characteristic new Value
//    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
//    public void sendCharacteristic(byte[] value, UUID uuid) {
//
//
//        BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) mService.getCharacteristic(uuid);
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
//            int result = bluetoothGatt.writeCharacteristic(ch, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//            if (result!=0){
//                Log.w(TAG, "writeCharacteristic() is failed, returns !=0");
//            }
//
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



    public class ReadCharacteristicTempHumidityThread extends Thread {

        private boolean running = false;

        public void setRunning(boolean running) {
            this.running = running;
        }

        public void toggleThread() {
            this.running = !this.running;
        }

        public void run() {
            running = true;

            try {
                while(!Thread.currentThread().isInterrupted() ) {

                    readCharacteristic(CAR_TEMP_HUMIDITY_CHARACTERISTIC_UUID);
                    Thread.sleep(READ_TEMP_HUMIDITY_CHARACTERISTIC_ITERATION);



                }
                return;
            }  catch
            (InterruptedException e) {
                Log.e(TAG, e.toString());
                //throw new RuntimeException(e);

            }


        }
    }



    public class ReadCharacteristicMagnetometerThread extends Thread {

        private boolean running = false;

        public void setRunning(boolean running) {
            this.running = running;
        }

        public void toggleThread() {
            this.running = !this.running;
        }

        public void run() {
            running = true;

            try {
                while(!Thread.currentThread().isInterrupted() ) {

                    readCharacteristic(CAR_MAGNETOMETER_CHARACTERISTIC_UUID);
                    Thread.sleep(READ_MAGNETOMETR_CHARACTERISTIC_ITERATION);



                }
                return;
            }  catch
            (InterruptedException e) {
                Log.e(TAG, e.toString());
                //throw new RuntimeException(e);

            }


        }
    }

}