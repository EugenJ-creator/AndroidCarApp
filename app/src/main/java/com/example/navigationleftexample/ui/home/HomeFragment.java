package com.example.navigationleftexample.ui.home;

import android.Manifest;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.navigationleftexample.R;
import com.example.navigationleftexample.databinding.FragmentHomeBinding;
import com.example.navigationleftexample.ui.bluetooth.BluetoothFragment;
import com.example.navigationleftexample.ui.bluetooth.BluetoothLeService;

import java.util.UUID;

public class HomeFragment extends Fragment {

    private SeekBar stearAngleSeekBar;
    private SeekBar speedcarSeekBar;
    private Switch directionSwitch;

    private ImageButton leftButton;
    private ImageButton rightButton;
    private ImageButton thrrottleButton;
    private ImageButton brakeButton;

    private Button startButton;

    private static final String TAG = "HomeFragment ";

    private Handler handler = new Handler();




    // Stearing iteration
    private static final long ITERATION_PERIOD_MOTOR_POWER= 100;

    private final static int STERAANGLE_MAX = 140;
    private final static int STERAANGLE_MIN = 0;
    private final static int STERAANGLE_MIDDLE = 49;
    public static int stearEngle =  STERAANGLE_MIDDLE;


    private final static int MOTOR_POWER_MAX = 250;
    private final static int MOTOR_POWER_MIN = 0;
    public static int motorPower = MOTOR_POWER_MIN;

    private FragmentHomeBinding binding;


    UpdateLeftButtonThread myUpdateLeftButtonThread = null;
    UpdateRightButtonThread myUpdateRightButtonThread = null;
    UpdateThrottleGasThread myUpdateThrottleGasThread = null;
    UpdateBrakeThread myUpdateBrakeThread = null;

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




    public class UpdateThrottleGasThread extends Thread {

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
                while(!Thread.currentThread().isInterrupted()) {

                    while ((motorPower <= MOTOR_POWER_MAX) && running) {
                        motorPower++;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            byte[] val = new byte[1];
                            val[0] = (byte) motorPower;
                            sendCharacteristic(val, BluetoothLeService.CAR_SPEED_CHARACTERISTIC_UUID);
                        }

                        Thread.sleep(ITERATION_PERIOD_MOTOR_POWER);
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




    public class UpdateBrakeThread extends Thread {

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
                while(!Thread.currentThread().isInterrupted()) {

                    while ((motorPower >= MOTOR_POWER_MIN) && running) {
                        motorPower--;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            byte[] val = new byte[1];
                            val[0] = (byte) motorPower;
                            sendCharacteristic(val, BluetoothLeService.CAR_SPEED_CHARACTERISTIC_UUID);
                        }

                        Thread.sleep(ITERATION_PERIOD_MOTOR_POWER);
                    }
                    return;
                }
                return;
            }  catch
            (InterruptedException e) {
                //throw new RuntimeException(e);
            }


        }
    }









    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



        //myUpdateLeftButtonThread = new UpdateLeftButtonThread();
        //myUpdateRightButtonThread = new UpdateRightButtonThread();

//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        leftButton = (ImageButton) binding.left;
        rightButton = (ImageButton) binding.right;
        thrrottleButton = (ImageButton) binding.throttle;
        brakeButton = (ImageButton) binding.brake;

        startButton = (Button) binding.startButton;

        stearAngleSeekBar = (SeekBar)binding.stearingSeekBar;
        speedcarSeekBar = (SeekBar)binding.speedSeekBar;
        directionSwitch = (Switch) binding.directionSwitch;

        speedcarSeekBar.setMin(0);
        speedcarSeekBar.setMax(250);


        stearAngleSeekBar.setMin(0);
        stearAngleSeekBar.setMax(160);

        // Change Direction if switch was chavked
        directionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                Toast.makeText(root.getContext(), "Bluetooth was changed", Toast.LENGTH_SHORT).show();
                if (isChecked == true) {
                    byte[] val =  new byte[1];
                    val[0] = (byte) 1;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        sendCharacteristic(val, BluetoothLeService.DIRECTION_CHARACTERISTIC_UUID);
                    }

                } else {
                        byte[] val =  new byte[1];
                        val[0] = (byte) 0;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        sendCharacteristic(val, BluetoothLeService.DIRECTION_CHARACTERISTIC_UUID);
                    }
                }
            }
        });

        stearAngleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                byte[] val =  new byte[1];
                val[0] = (byte )progress;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    sendCharacteristic(val, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        speedcarSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                byte[] val =  new byte[1];
                val[0] = (byte )progress;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    sendCharacteristic(val, BluetoothLeService.CAR_SPEED_CHARACTERISTIC_UUID);
                }

            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    byte[] val =  new byte[1];
                    val[0] = (byte )stearEngle;
                       sendCharacteristic(val, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
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








        leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
//                    myUpdateLeftButtonThread.toggleThread();
//                    myUpdateLeftButtonThread.run();

                    byte[] val = new byte[1];
                    val[0] = (byte) STERAANGLE_MAX;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        sendCharacteristic(val, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
                    }


                    Log.e(TAG, "Unable to initialize Bluetooth");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    byte[] val = new byte[1];
                    val[0] = (byte) STERAANGLE_MIDDLE;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        sendCharacteristic(val, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
                    }

                    Log.e(TAG, "Unable to initialize Bluetooth");
                }
                return true;
            }
        });


        rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
//                    myUpdateLeftButtonThread.toggleThread();
//                    myUpdateLeftButtonThread.run();

                    byte[] val = new byte[1];
                    val[0] = (byte) STERAANGLE_MIN;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        sendCharacteristic(val, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
                    }


                    Log.e(TAG, "Unable to initialize Bluetooth");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    byte[] val = new byte[1];
                    val[0] = (byte) STERAANGLE_MIDDLE;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        sendCharacteristic(val, BluetoothLeService.STEERING_ANGLE_CHARACTERISTIC_UUID);
                    }

                    Log.e(TAG, "Unable to initialize Bluetooth");
                }
                return true;
            }
        });


        thrrottleButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {

                    myUpdateThrottleGasThread = new UpdateThrottleGasThread();
                    myUpdateThrottleGasThread.start();


                } else if (event.getAction() == MotionEvent.ACTION_UP) {


                    myUpdateThrottleGasThread.setRunning(false);
                    myUpdateThrottleGasThread.interrupt();


                }
                return true;
            }
        });


        brakeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {


                    myUpdateBrakeThread = new UpdateBrakeThread();
                    myUpdateBrakeThread.start();


                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    myUpdateBrakeThread.setRunning(false);
                    myUpdateBrakeThread.interrupt();

                }
                return true;
            }
        });






        return root;
    }


    public ImageButton getLeftButton() {
        return leftButton;
    }

    // Set Characteristic new Value
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void sendCharacteristic(byte[] value, UUID uuid) {


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
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            BluetoothLeService.getBluetoothGatt().writeCharacteristic(ch, value, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        }

    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}