package com.example.navigationleftexample.ui.home;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.navigationleftexample.R;
import com.example.navigationleftexample.ui.ViewModels.BluetoothGameDataViewModel;
import com.example.navigationleftexample.ui.bluetooth.BluetoothViewModel;
import com.example.navigationleftexample.ui.circularseekbar.CircularSeekBar;
import com.example.navigationleftexample.databinding.FragmentHomeBinding;
import com.example.navigationleftexample.ui.bluetooth.BluetoothLeService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomeFragment extends Fragment {

//    Intent gattServiceIntent;
//    BluetoothGatt bluetoothGatt;
    private SeekBar speedcarSeekBar;
    private SeekBar speedcarBackSeekBar;

    private CircularSeekBar circularSeekBarStearing;

    private ImageButton leftButton;
    private ImageButton rightButton;
    private ImageButton thrrottleButton;
    private ImageButton brakeButton;


    private ImageButton onButton;
    private ImageButton cruiseButton;
    private ImageButton signalButton;

    private ImageView compassNorthDirection;
    private static final String TAG = "HomeFragment ";

    private Handler handler = new Handler();
    public HomeViewModel homeViewModel;
//    private TextView tempView;
//    private TextView humidityView;

    private final double[] bias = {-544.48,239.4,55.94};
    private final double[][] euler = {{1.225434,-0.028676,0.069206},{-0.028676,1.087208,0.045837},{0.069206,0.045837,1.057089}};
    private static double offsetAngle = 74.745;  // Offset degrees for kompass
    private Double X_Magnetometer_Data;
    private Double Y_Magnetometer_Data;
    private Double Z_Magnetometer_Data;
    // Stearing iteration
    private static final long ITERATION_PERIOD_MOTOR_POWER= 1;
    private static final long ITERATION_PERIOD_STEAR_ANGLE=5;

    private final static int STERAANGLE_MAX = 140;
    private final static int STERAANGLE_MIN = 0;
    private final static int STERAANGLE_MIDDLE = 49;
    private final static int BUZZER_MIDLE = 5;  //200
    private final static int BUZZER_OFF = 0;

    public static int optionsToggle = 0;
    public static int buzzerVolume = BUZZER_MIDLE;
    public static int stearEngle =  STERAANGLE_MIDDLE;
    public static int throttleProgress;
    public static int throttleBackProgress;
    public static int direction = 0;

    private final static int MOTOR_POWER_MAX = 250;
    private final static int MOTOR_POWER_MIN = 0;
    public static int motorPower = MOTOR_POWER_MIN;

    private FragmentHomeBinding binding;
    private  BluetoothLeService bluetoothService;

    UpdateLeftButtonThread myUpdateLeftButtonThread = null;
    UpdateRightButtonThread myUpdateRightButtonThread = null;
//    UpdateThrottleGasThread myUpdateThrottleGasThread = null;
//    UpdateBrakeThread myUpdateBrakeThread = null;

    AutoBrakeThread  autoBrakeThread = null;
    AutoBrakeBackThread  autoBrakeBackThread = null;
    AutoStearThread  autoStearThread = null;

    BluetoothViewModel bluetoothViewModel;

    //BluetoothDataReceiver   bluetoothDataReceiver;
    Vibrator vibe = null;
    int  signal_on_off = 0;   // Buzzer function

    public static double[] multiplyMatrix(double[][] matrix, double[] vector) {
        double[] res = new double[vector.length];

        for (int i = 0; i < matrix[0].length; i++) {
            int sum = 0;
            for (int j = 0; j < vector.length; j++) {
                sum += matrix[j][i] * vector[j];
            }
            res[i] = sum; //this should help you assign the values
        }
        return res;
    }


    public static double[] substractVectors(double[] vector1, double[] vector2) {
        double[] res = new double[vector1.length];
        for (int i = 0; i < vector1.length; i++) {
            res[i] = vector1[i] - vector2[i];
        }
        return res;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothService = ((BluetoothLeService.LocalBinder) service).getService();
            if (bluetoothService == null) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothService = null;
        }
    };


    public class UpdateLeftButtonThread extends Thread {

        private boolean keepRunning = false;
        private boolean releaseButton = false;

        public void toggleThread() {
            this.keepRunning = !this.keepRunning;
        }

        public void run() {

//            while (stearEngle <= STERAANGLE_MAX && !releaseButton) {
//                stearEngle++;
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    byte[] val =  new byte[1];
//                    val[0] = (byte )stearEngle;
//                    sendCharacteristic(val, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
//                }
//
//
//
//            }
//            while (stearEngle >= STERAANGLE_MIDDLE && releaseButton) {
//                stearEngle--;
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    byte[] val =  new byte[1];
//                    val[0] = (byte )stearEngle;
//                    sendCharacteristic(val, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
//                }
//
//            }


            if (!releaseButton) {
                byte[] val = new byte[1];
                val[0] = (byte) STERAANGLE_MAX;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    sendCharacteristic(val, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
                }
            }
        }
    }


    public class UpdateRightButtonThread extends Thread {

        private boolean running = false;


        public void toggleThread() {
            this.running = !this.running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public void run() {


            while (stearEngle >= STERAANGLE_MIN) {
                stearEngle--;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    byte[] val =  new byte[1];
                    val[0] = (byte )stearEngle;
                    sendCharacteristic(val, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
                }
                try {
                    Thread.sleep(ITERATION_PERIOD_MOTOR_POWER);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.toString());
                    throw new RuntimeException(e);

                }


            }

        }
    }




//    public class UpdateThrottleGasThread extends Thread {
//
//        private boolean running = false;
//
//        public void setRunning(boolean running) {
//            this.running = running;
//        }
//
//        public void toggleThread() {
//            this.running = !this.running;
//        }
//
//        public void run() {
//            running = true;
//
//            try {
//                while(!Thread.currentThread().isInterrupted()) {
//
//                    while ((motorPower <= MOTOR_POWER_MAX) && running) {
//                        motorPower++;
//
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                            byte[] val = new byte[1];
//                            val[0] = (byte) motorPower;
//                            sendCharacteristic(val, BluetoothLeService.CAR_SPEED_CHARACTERISTIC_UUID);
//                        }
//
//                        Thread.sleep(ITERATION_PERIOD_MOTOR_POWER);
//                    }
//                    return;
//                }
//                return;
//            }  catch
//              (InterruptedException e) {
//                    Log.e(TAG, e.toString());
//                    //throw new RuntimeException(e);
//
//            }
//
//
//            }
//    }

    public class AutoBrakeThread extends Thread {

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

                    if ((throttleProgress > 0) && running) {
                        while (throttleProgress != 0) {
                            throttleProgress--;
                            speedcarSeekBar.setProgress(throttleProgress);

//                            byte[] val = new byte[1];
//                            val[0] = (byte) throttleProgress;
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                                sendCharacteristic(val, BluetoothLeService.CAR_SPEED_CHARACTERISTIC_UUID);
//                            }
                            Thread.sleep(ITERATION_PERIOD_MOTOR_POWER);
                        }

                    } else if ((throttleProgress < 0) && running){

                        while (throttleProgress != 0) {
                            throttleProgress++;
                            speedcarSeekBar.setProgress(throttleProgress);
//                            int posThrottleProgress;
//
//                            if (throttleProgress < 0) {
//                                posThrottleProgress = (-1) * throttleProgress;
//                            } else
//                                posThrottleProgress = throttleProgress;
//
//                            byte[] val = new byte[1];
//                            val[0] = (byte) posThrottleProgress;
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                                sendCharacteristic(val, BluetoothLeService.CAR_SPEED_CHARACTERISTIC_UUID);
//                            }
                            Thread.sleep(ITERATION_PERIOD_MOTOR_POWER);
                        }

                    }

                    return;
                }
                return;
            }  catch
            (InterruptedException e) {
                Log.e(TAG, e.toString());
                //throw new RuntimeException(e);

            }


        }
    }


    public class AutoBrakeBackThread extends Thread {

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

                    if ((throttleBackProgress > 0) && running) {
                        while (throttleBackProgress != 0) {
                            throttleBackProgress--;
                            speedcarBackSeekBar.setProgress(throttleBackProgress);

//                            byte[] val = new byte[1];
//                            val[0] = (byte) throttleProgress;
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                                sendCharacteristic(val, BluetoothLeService.CAR_SPEED_CHARACTERISTIC_UUID);
//                            }
                            Thread.sleep(ITERATION_PERIOD_MOTOR_POWER);
                        }

                    } else if ((throttleBackProgress < 0) && running){

                        while (throttleBackProgress != 0) {
                            throttleBackProgress++;
                            speedcarBackSeekBar.setProgress(throttleBackProgress);
//                            int posThrottleProgress;
//
//                            if (throttleProgress < 0) {
//                                posThrottleProgress = (-1) * throttleProgress;
//                            } else
//                                posThrottleProgress = throttleProgress;
//
//                            byte[] val = new byte[1];
//                            val[0] = (byte) posThrottleProgress;
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                                sendCharacteristic(val, BluetoothLeService.CAR_SPEED_CHARACTERISTIC_UUID);
//                            }
                            Thread.sleep(ITERATION_PERIOD_MOTOR_POWER);
                        }

                    }

                    return;
                }
                return;
            }  catch
            (InterruptedException e) {
                Log.e(TAG, e.toString());
                //throw new RuntimeException(e);

            }


        }
    }









    public class AutoStearThread extends Thread {

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

                    if ((stearEngle > STERAANGLE_MIDDLE) && running) {
                        while (stearEngle != STERAANGLE_MIDDLE) {
                            stearEngle--;
                            circularSeekBarStearing.setProgress(stearEngle);

//                            byte[] val = new byte[1];
//                            val[0] = (byte) throttleProgress;
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                                sendCharacteristic(val, BluetoothLeService.CAR_SPEED_CHARACTERISTIC_UUID);
//                            }
                            Thread.sleep(ITERATION_PERIOD_STEAR_ANGLE);
                        }

                    } else if ((stearEngle < STERAANGLE_MIDDLE) && running){

                        while (stearEngle != STERAANGLE_MIDDLE) {
                            stearEngle++;
                            circularSeekBarStearing.setProgress(stearEngle);
//                            int posThrottleProgress;
//
//                            if (throttleProgress < 0) {
//                                posThrottleProgress = (-1) * throttleProgress;
//                            } else
//                                posThrottleProgress = throttleProgress;
//
//                            byte[] val = new byte[1];
//                            val[0] = (byte) posThrottleProgress;
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                                sendCharacteristic(val, BluetoothLeService.CAR_SPEED_CHARACTERISTIC_UUID);
//                            }
                            Thread.sleep(ITERATION_PERIOD_STEAR_ANGLE);
                        }

                    }

                    return;
                }
                return;
            }  catch
            (InterruptedException e) {
                Log.e(TAG, e.toString());
                //throw new RuntimeException(e);

            }


        }
    }




//
//    public class UpdateBrakeThread extends Thread {
//
//        private boolean running = false;
//
//        public void setRunning(boolean running) {
//            this.running = running;
//        }
//
//        public void toggleThread() {
//            this.running = !this.running;
//        }
//
//        public void run() {
//            running = true;
//
//            try {
//                while(!Thread.currentThread().isInterrupted()) {
//
//                    while ((motorPower >= MOTOR_POWER_MIN) && running) {
//                        motorPower--;
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                            byte[] val = new byte[1];
//                            val[0] = (byte) motorPower;
//                            sendCharacteristic(val, BluetoothLeService.CAR_SPEED_CHARACTERISTIC_UUID);
//                        }
//
//                        Thread.sleep(ITERATION_PERIOD_MOTOR_POWER);
//                    }
//                    return;
//                }
//                return;
//            }  catch
//            (InterruptedException e) {
//                //throw new RuntimeException(e);
//            }
//
//
//        }
//    }



    // Broadcast Receiver
    // Don't forget to add new notification to filter!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(BluetoothLeService.ACTION_NOTIFICATION_RECEIVED_TRMP_HUMIDITY)) {
                    //str = intent.getFloatExtra("title", 66);
                    //  Show Connected Bluetooth Device
//Data received via Bluetooth is  Little Endian
                    Float tempData;
                    Float humidityData;
                    byte[] tempHumidityNotificationData = intent.getByteArrayExtra("tempHumidityData");

                    ////Calculate humidity, byte1, byte2, 4bits MSB from byte 3
                    long h = tempHumidityNotificationData[4] & 0xFF;
                    h <<= 8;
                    h |= tempHumidityNotificationData[3] & 0xFF;
                    h <<= 4;
                    h |= (tempHumidityNotificationData[2] >> 4) & 0x0F;
                    //humidityString = Long.toHexString(h);
                    humidityData = ((float) h * 100) / 0x100000;


                    //Calculate temp , 4bits LSB from byte 3, byte4, byte5
                    long tdata = (tempHumidityNotificationData[2] & 0x0F);
                    tdata <<= 8;
                    tdata |= tempHumidityNotificationData[1] & 0xFF;
                    tdata <<= 8;
                    tdata |= tempHumidityNotificationData[0] & 0xFF;
                    //tempString = Long.toHexString(tdata);
                    tempData = ((float) tdata * 200 / 0x100000) - 50;

//                    bluetoothViewModel = new ViewModelProvider(requireActivity()).get(BluetoothViewModel.class);
                    binding.setBluetoothViewModelData(bluetoothViewModel);

                    bluetoothViewModel.setTempSensor(tempData);
                    bluetoothViewModel.setHumiditySensor(humidityData);
//        // Show bluetooth device name in view
//        bluetoothViewModel.getTempSensor().observe(getViewLifecycleOwner(), tempSensor -> {
//        tempView.setText(tempSensor.intValue());
//        });
//
//        bluetoothViewModel.getHumiditySensor().observe(getViewLifecycleOwner(), humiditySensor -> {
//        humidityView.setText(humiditySensor.intValue());
//        });


                } else if (intent.getAction().equals(BluetoothLeService.ACTION_NOTIFICATION_RECEIVED_BUZZER_VALUE)) {
                    byte[] buzzerNotificationData = intent.getByteArrayExtra("buzzerData");
                    if (buzzerNotificationData[0] == 0) {
                        signalButton.setImageResource(R.drawable.dashboard_signal_off);
                        signal_on_off = 0;
                    } else if (buzzerNotificationData[0] == BUZZER_MIDLE) {
                        signalButton.setImageResource(R.drawable.dashboard_signal_on);
                        signal_on_off = 1;
                    }
                } else if (intent.getAction().equals(BluetoothLeService.ACTION_NOTIFICATION_RECEIVED_MAGNETOMETER)) {

                    byte[] magnetometerNotificationData = intent.getByteArrayExtra("XYZCompass");

                    double headingDegrees;
                    double headingDegreesNorm;  // Actual degrees from right to left 360 degree
                    double declinationAngleOffset;  // degrees with offset relatively to vertical axe ( direction of car)
                    double[] pointNotCalibrated = new double[3];
                    double[] pointSubstracted = new double[3];
                    double[] pointCalibrated = new double[3];

                    //Data received via Bluetooth is  Little Endian

                    ////Calculate Magnet, Array is already reversed.
                    long x = magnetometerNotificationData[1];
                    x <<= 8;
                    x |= magnetometerNotificationData[0] & 0xFF;
                    pointNotCalibrated[0] = Double.valueOf(x);

                    long y = magnetometerNotificationData[3];
                    y <<= 8;
                    y |= magnetometerNotificationData[2] & 0xFF;
                    pointNotCalibrated[1] = Double.valueOf(y);

                    long z = magnetometerNotificationData[5];
                    z <<= 8;
                    z |= magnetometerNotificationData[4] & 0xFF;
                    pointNotCalibrated[2] = Double.valueOf(z);

                    pointSubstracted = substractVectors(pointNotCalibrated, bias);


                    pointCalibrated = multiplyMatrix(euler, pointSubstracted);
                    //  Axes in the controller are not right. Change them
                    X_Magnetometer_Data = pointCalibrated[0];
                    Y_Magnetometer_Data = pointCalibrated[1];
                    Z_Magnetometer_Data = pointCalibrated[2];

                    double headingRadians = Math.atan2(Y_Magnetometer_Data, X_Magnetometer_Data);
                    headingDegrees = (double) (headingRadians * 180 / Math.PI);
                    if (headingDegrees < 0) {
                        headingDegreesNorm = 360 + headingDegrees;
                    } else {
                        headingDegreesNorm = headingDegrees;
                    }
                    declinationAngleOffset = (offsetAngle + headingDegreesNorm) % 360;
                    compassNorthDirection.setRotation((float) declinationAngleOffset);


//                    compassNorthDirection.setRotation((float)(intent.getDoubleExtra("angleCompass", 0)));


                } else if (intent.getAction().equals(BluetoothLeService.ACTION_NOTIFICATION_RECEIVED_SPEED_SENSOR_VALUE)) {

                    byte[] speedSensorNotificationData = intent.getByteArrayExtra("speedSensorData");

                    float speed;
                    //Data received via Bluetooth is  Little Endian

                    //Calculate period
                    long period = speedSensorNotificationData[0] & 0xFF;
                    period <<= 8;
                    period |= speedSensorNotificationData[1] & 0xFF;
                    period <<= 8;
                    period |= speedSensorNotificationData[2] & 0xFF;

                    if (period == 0){
                        speed = 0;
                    } else {
                        // (( (3,6 grad * pi)/180 grad ) rad /  T ) * 0.032 m   = m/s
                        speed = (float) (((0.0628 * 1000000000) / (Float.valueOf(period) * 12.5)) * 0.032);
                    }
                    binding.setBluetoothViewModelData(bluetoothViewModel);

                    bluetoothViewModel.setSpeedSensor(speed);





                }
            }
        }
    };



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        bluetoothViewModel = new ViewModelProvider(requireActivity()).get(BluetoothViewModel.class);
        binding.setBluetoothViewModelData(bluetoothViewModel);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_NOTIFICATION_RECEIVED_TRMP_HUMIDITY);
        intentFilter.addAction(BluetoothLeService.ACTION_NOTIFICATION_RECEIVED_MAGNETOMETER);
        intentFilter.addAction(BluetoothLeService.ACTION_NOTIFICATION_RECEIVED_BUZZER_VALUE);
        intentFilter.addAction(BluetoothLeService.ACTION_NOTIFICATION_RECEIVED_SPEED_SENSOR_VALUE);

        LocalBroadcastManager.getInstance(this.getContext()).registerReceiver(notificationReceiver, intentFilter);

//        Intent gattServiceIntent = new Intent(getContext(), BluetoothLeService.class);
//        getContext().bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);



        vibe= (Vibrator) getContext().getSystemService(getContext().VIBRATOR_SERVICE) ;

        //myUpdateLeftButtonThread = new UpdateLeftButtonThread();
        //myUpdateRightButtonThread = new UpdateRightButtonThread();

//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        circularSeekBarStearing = (CircularSeekBar) binding.circularSeekbar ;
        leftButton = (ImageButton) binding.left;
        rightButton = (ImageButton) binding.right;
        thrrottleButton = (ImageButton) binding.throttle;
        brakeButton = (ImageButton) binding.brake;

        onButton = (ImageButton) binding.onOffButton;
        cruiseButton = (ImageButton) binding.cruiseButtonOff;
        compassNorthDirection = (ImageView) binding.nordCompass;

        signalButton = (ImageButton) binding.signalOffButton;

        speedcarSeekBar = (SeekBar)binding.speedSeekBar;
        speedcarBackSeekBar = (SeekBar)binding.speedBackSeekBar;

//        tempView = (TextView) binding.textViewTempValue;
//        humidityView = (TextView) binding.textViewHumidityValue;

        speedcarSeekBar.setMin(MOTOR_POWER_MIN);
        speedcarSeekBar.setMax(MOTOR_POWER_MAX);

        speedcarBackSeekBar.setMin(MOTOR_POWER_MIN);
        speedcarBackSeekBar.setMax(MOTOR_POWER_MAX);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Implenmenation of circular seekbar
        circularSeekBarStearing.setOnSeekBarChangeListener( new CircularSeekBar.OnCircularSeekBarChangeListener () {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                // TODO Insert your code here
                stearEngle = progress;

                byte[] val =  new byte[1];
                val[0] = (byte )(progress & 0xFF);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    sendCharacteristic(val, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
                }
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {
                autoStearThread = new AutoStearThread();
                autoStearThread.start();
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {
                if (autoStearThread!=null) {
                    autoStearThread.setRunning(false);
                    autoStearThread.interrupt();
                }

            }
        });




        speedcarSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                throttleProgress = progress&0xFF;
                byte[] val = new byte[1];
                val[0] = (byte) throttleProgress;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    sendCharacteristic(val, BluetoothLeService.CAR_SPEED_FORWARD_CHARACTERISTIC_UUID);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (autoBrakeThread!=null) {
                    autoBrakeThread.setRunning(false);
                    autoBrakeThread.interrupt();
                }
                // Change direction
//
//                if (direction!=0){
//                    direction=0;
//                }
//                byte[] dir =  new byte[1];
//                dir[0] = (byte) 0;
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    sendCharacteristic(dir, BluetoothLeService.DIRECTION_CHARACTERISTIC_UUID);
//                }

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                autoBrakeThread = new AutoBrakeThread();
                autoBrakeThread.start();

            }

        });


        speedcarBackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                throttleBackProgress = progress&0xFF;

                byte[] val = new byte[1];
                val[0] = (byte) throttleBackProgress;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    sendCharacteristic(val, BluetoothLeService.CAR_SPEED_BACKWARD_CHARACTERISTIC_UUID);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (autoBrakeBackThread!=null) {
                    autoBrakeBackThread.setRunning(false);
                    autoBrakeBackThread.interrupt();
                }
                // Change direction

//                if (direction!=1){
//                    direction=1;
//                }
//                byte[] dir =  new byte[1];
//                dir[0] = (byte) 1;
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    sendCharacteristic(dir, BluetoothLeService.DIRECTION_CHARACTERISTIC_UUID);
//                }

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                autoBrakeBackThread = new AutoBrakeBackThread();
                autoBrakeBackThread.start();

            }

        });


//speedcarSeekBar.setOnTouchListener(new View.OnTouchListener() {
//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        if(event.getAction() == MotionEvent.ACTION_DOWN) {
//            return true;
//
//        } else if (event.getAction() == MotionEvent.ACTION_UP) {
//
//            autoBrakeThread = new AutoBrakeThread();
//            autoBrakeThread.start();
//
//        }
//
//
//
//        return true;
//    }
//});




        onButton.setOnClickListener(new View.OnClickListener() {

             int  on_off = 0;

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // Turn Car ON
                vibe.vibrate(50);

                if (on_off==0){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        byte[] val =  new byte[1];
                        val[0] = (byte )STERAANGLE_MIDDLE;
                        sendCharacteristic(val, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
                    }
                    onButton.setImageResource(R.drawable.dashboard_on_on);
                    on_off=1;
                } else
                {
                    // Turn your car OFF
                    onButton.setImageResource(R.drawable.dashboard_off_on);
                    on_off=0;
                }
            }
        });


        cruiseButton.setOnClickListener(new View.OnClickListener() {

            int  on_off = 0;

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // Turn Car ON
                vibe.vibrate(50);

                if (on_off==0){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        byte[] val =  new byte[1];
                        val[0] = (byte) ((byte )optionsToggle|0x01);
                        sendCharacteristic(val, BluetoothLeService.CAR_OPTIONS_CHARACTERISTIC_UUID);
                    }
                    cruiseButton.setImageResource(R.drawable.dashboard_cruise_on);
                    on_off=1;
                } else
                {
                    // Turn your car OFF
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        byte[] val =  new byte[1];
                        val[0] = (byte) ((byte )optionsToggle&(~0x01));
                        sendCharacteristic(val, BluetoothLeService.CAR_OPTIONS_CHARACTERISTIC_UUID);
                    }
                    cruiseButton.setImageResource(R.drawable.dashboard_cruise_off);
                    on_off=0;
                }
            }
        });



        signalButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                    // TODO Auto-generated method stub
                    // Turn Signal ON
                    vibe.vibrate(50);

                    if (signal_on_off == 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            byte[] val = new byte[1];
                            val[0] = (byte) BUZZER_MIDLE;


                            sendCharacteristic(val, BluetoothLeService.CAR_BUZZER_CHARACTERISTIC_UUID);



                        }
//                        signalButton.setImageResource(R.drawable.dashboard_signal_on);
//                        signal_on_off = 1;
                    } else if (signal_on_off == 1) {
                        // Turn your signal OFF
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            byte[] val = new byte[1];
                            val[0] = (byte) BUZZER_OFF;


                            sendCharacteristic(val, BluetoothLeService.CAR_BUZZER_CHARACTERISTIC_UUID);
                        }
//                        signalButton.setImageResource(R.drawable.dashboard_signal_off);
//                        signal_on_off = 0;
                    }

            }


        });



//        leftButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//               if (event.getAction() ==  MotionEvent.ACTION_DOWN){
//                   stearEngle[0]++;
//                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                       sendCharacteristic(stearEngle, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
//                   }
//                    while((event.getAction() ==  MotionEvent.ACTION_DOWN) && (stearEngle[0] < STERAANGLE_MAX))
//                    {
//                        handler.postDelayed(new Runnable() {
//                           @Override
//                           public void run() {
//
//                                   stearEngle[0]++;
//                                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                                       sendCharacteristic(stearEngle, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
//                                   }
//
//
//                           }
//                        }, ITERATION_PERIOD);
//
//                    }
//
//
//                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                   stearEngle[0]--;
//                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                       sendCharacteristic(stearEngle, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
//                   }
//                   while((event.getAction() ==  MotionEvent.ACTION_UP) && (stearEngle[0] > STERAANGLE_MIDDLE))
//                   {
//                       handler.postDelayed(new Runnable() {
//                           @Override
//                           public void run() {
//
//                               stearEngle[0]--;
//                               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                                   sendCharacteristic(stearEngle, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
//                               }
//
//
//                           }
//                       }, ITERATION_PERIOD);
//
//                   }
//
//
//                }
//                return true;
//            }
//
//        });



//        leftButton.setOnTouchListener(new View.OnTouchListener() {
//                                          @Override
//                                          public boolean onTouch(View v, MotionEvent event) {
//
//                                              switch (event.getAction()) {
//                                                  case MotionEvent.ACTION_DOWN:
//
//                                                      do {
//
//                                                          handler.postDelayed(new Runnable() {
//                                                              @Override
//                                                              public void run() {
//
//                                                                  Log.e(TAG, "LongPress");
//
//                                                              }
//                                                          }, ITERATION_PERIOD);
//                                                          Log.e(TAG, "ACTION_DOWN");
//
//                                                      } while (event.getAction()==MotionEvent.ACTION_UP);
//
//                                                      break;
//
//
//                                                  case MotionEvent.ACTION_UP:
//                                                      Log.e(TAG, "ACTION_UP");
//
//                                                      break;
//
//                                              }
//
//                                            return true;
//                                          }
//                                      });








//        leftButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_DOWN) {
////                    myUpdateLeftButtonThread.toggleThread();
////                    myUpdateLeftButtonThread.run();
//
//                    byte[] val = new byte[1];
//                    val[0] = (byte) STERAANGLE_MAX;
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        sendCharacteristic(val, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
//                    }
//
//
//                    Log.e(TAG, "Unable to initialize Bluetooth");
//                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    byte[] val = new byte[1];
//                    val[0] = (byte) STERAANGLE_MIDDLE;
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        sendCharacteristic(val, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
//                    }
//
//                    Log.e(TAG, "Unable to initialize Bluetooth");
//                }
//                return true;
//            }
//        });


//        rightButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_DOWN) {
////                    myUpdateLeftButtonThread.toggleThread();
////                    myUpdateLeftButtonThread.run();
//
//                    byte[] val = new byte[1];
//                    val[0] = (byte) STERAANGLE_MIN;
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        sendCharacteristic(val, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
//                    }
//
//
//                    Log.e(TAG, "Unable to initialize Bluetooth");
//                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    byte[] val = new byte[1];
//                    val[0] = (byte) STERAANGLE_MIDDLE;
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        sendCharacteristic(val, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
//                    }
//
//                    Log.e(TAG, "Unable to initialize Bluetooth");
//                }
//                return true;
//            }
//        });


//        thrrottleButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_DOWN) {
//
//                    myUpdateThrottleGasThread = new UpdateThrottleGasThread();
//                    myUpdateThrottleGasThread.start();
//
//
//                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//
//
//                    myUpdateThrottleGasThread.setRunning(false);
//                    myUpdateThrottleGasThread.interrupt();
//
//
//                }
//                return true;
//            }
//        });


//        brakeButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_DOWN) {
//
//
//                    myUpdateBrakeThread = new UpdateBrakeThread();
//                    myUpdateBrakeThread.start();
//
//
//                } else if (event.getAction() == MotionEvent.ACTION_UP) {
//                    myUpdateBrakeThread.setRunning(false);
//                    myUpdateBrakeThread.interrupt();
//
//                }
//                return true;
//            }
//        });


         return root;
    }


    public ImageButton getLeftButton() {
        return leftButton;
    }

//    private void unSubscribeNoticationCharacteristics(BluetoothGatt bluetoothGatt) {
//
//        if (bluetoothGatt == null) {
//            return;
//        }
//
//        BluetoothGattCharacteristic characteristic = BluetoothLeService.notificationDeactivateChars.get(0);
//        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        bluetoothGatt.setCharacteristicNotification(characteristic, false);
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BluetoothLeService.BLEUUID.CAR_NOTIFICATION_CCCD_DESCRIPTOR));
////            // get Characteristics
////            gattDescriptors = ch.getDescriptors();
//
//        if(descriptor != null) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                bluetoothGatt.writeDescriptor(descriptor);
//            }
//        }
//    }



    // Set Characteristic new Value
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public boolean sendCharacteristic(byte[] value, UUID uuid) {

//        bluetoothGatt = bluetoothService.getBluetoothGatt();
//        BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) bluetoothService.getmService().getCharacteristic(uuid);


        BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) BluetoothLeService.getmService().getCharacteristic(uuid);
        ch.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);


        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            //           BluetoothLeService.getBluetoothGatt().writeCharacteristic(ch, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            BluetoothGatt gatt = BluetoothLeService.getBluetoothGatt();

            synchronized (BluetoothLeService.mDeviceBusy) {
                if (BluetoothLeService.mDeviceBusy) return false;
                BluetoothLeService.mDeviceBusy = true;
            }


            if (BluetoothLeService.executeCharacteristicList.isEmpty()) {
                int result = gatt.writeCharacteristic(ch, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

                if (result != 0) {
                    Log.w(TAG, "writeCharacteristic() is failed, returns !=0");
                    return false;
                } else {
                    BluetoothLeService.executeCharacteristicList.add(ch);
                }


//            BluetoothGattCharacteristic ch1 = (BluetoothGattCharacteristic) BluetoothLeService.getmService().getCharacteristic(UUID.fromString(BluetoothLeService.BLEUUID.CAR_MAGNETOMETER_NOTIFICATION_CHARACTERISTIC));
//            BluetoothLeService.notificationDeactivateChars.add(ch1);
//            BluetoothGattCharacteristic ch2 = (BluetoothGattCharacteristic) BluetoothLeService.getmService().getCharacteristic(UUID.fromString(BluetoothLeService.BLEUUID.CAR_TEMP_HUMIDITY_NOTIFICATION_CHARACTERISTIC));
//            BluetoothLeService.notificationDeactivateChars.add(ch2);
//
//            if (BluetoothLeService.wtiteCharBuzzer.isEmpty()) {
//                BluetoothLeService.wtiteCharBuzzer.add(ch);
//                BluetoothLeService.wtiteCharBuzzerValue.add(value);
//            }
//
//
//            unSubscribeNoticationCharacteristics(gatt);


//            if (uuid.toString().equals(BluetoothLeService.BLEUUID.CAR_BUZZER_CHARACTERISTIC)){
//                if (BluetoothLeService.wtiteCharBuzzer.isEmpty()) {
//                    int result = gatt.writeCharacteristic(ch, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//
//                    if (result != 0) {
//                        Log.w(TAG, "writeCharacteristic() is failed, returns !=0");
//                    } else {
//                        BluetoothLeService.wtiteCharBuzzer.add(ch);
//                    }
//                }
//            } else if (uuid.toString().equals(BluetoothLeService.BLEUUID.STEERING_ANGLE_CHARACTERISTIC)) {
//                if (BluetoothLeService.wtiteStearingAngle.isEmpty()) {
//                    int result = gatt.writeCharacteristic(ch, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//
//                    if (result != 0) {
//                        Log.w(TAG, "writeCharacteristic() is failed, returns !=0");
//                    } else {
//                        BluetoothLeService.wtiteStearingAngle.add(ch);
//                    }
//                }
//
//            }


//            if (bluetoothGatt == null) {
//                Log.w(TAG, "BluetoothGatt not initialized");
//                return;
//            }

                //    bluetoothGatt.beginReliableWrite();
                //gatt.beginReliableWrite().
                //    bluetoothGatt.writeCharacteristic(ch, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//
//            bluetoothGatt.getServ(ch, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            }
        }
        return true;

    }


//    @Override
//    public void onStart() {
//        Intent gattServiceIntent = new Intent(getContext(), BluetoothLeService.class);
//        getContext().bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
//        super.onStart();
//    }


//    @Override
//    public void onStop()
//    {
//        super.onStop();
//        this.getContext().unregisterReceiver(bluetoothDataReceiver);           //<-- Unregister to avoid memoryleak
//    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(this.getContext()).unregisterReceiver(notificationReceiver);
    }
}