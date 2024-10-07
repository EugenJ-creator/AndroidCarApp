package com.example.navigationleftexample.ui.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.widget.Switch;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

public class BluetoothViewModel extends ViewModel {

   // private SavedStateHandle savedStateHandle;




    private  MutableLiveData<BluetoothDevice> selectedDevice;

    private MutableLiveData<Switch> switchButton;



//    public BluetoothViewModel() {
//
//        selectedDevice = new MutableLiveData<BluetoothDevice>();
//
//    }


//
//    public BluetoothViewModel(SavedStateHandle savedStateHandle) {
//        this.savedStateHandle = savedStateHandle;
//
//            selectedDevice = savedStateHandle.getLiveData("pairedDevice");
//                if (selectedDevice == null) {
//                    selectedDevice = new MutableLiveData<BluetoothDevice>();
//                }
//            switchButton  = savedStateHandle.getLiveData("pairedDevice");
//
//       }

    public BluetoothViewModel() {


        selectedDevice = new MutableLiveData<BluetoothDevice>();
        switchButton = new MutableLiveData<Switch>();
    }






//    public void setPairedDeviceToSavedStateHandle(){
//
//        savedStateHandle.set("pairedDevice", selectedDevice);
//    }


    public int setSwitch(Switch switchB) {
        switchButton.setValue(switchB);
        return 0;
    }


    public LiveData<Switch> getSwitch() {
        return switchButton;
    }


    public LiveData<BluetoothDevice> getDevice() {
        return selectedDevice;
    }


    public int setDevice(BluetoothDevice dev) {
        selectedDevice.setValue(dev);
        return 0;
    }




}