package com.example.navigationleftexample.ui.home;

import android.Manifest;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    SeekBar stearAngleSeekBar;
    SeekBar speedcarSeekBar;
    Switch directionSwitch;

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textHome;
//        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);



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


        return root;
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