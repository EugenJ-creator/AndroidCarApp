package com.example.navigationleftexample.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.navigationleftexample.MainActivity;
import com.example.navigationleftexample.R;
import com.example.navigationleftexample.databinding.ActivityLoginBinding;
import com.example.navigationleftexample.repository.MainRepository;
import com.permissionx.guolindev.PermissionX;

public class LoginActivity extends AppCompatActivity {



    private ActivityLoginBinding views;

    private MainRepository mainRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views = ActivityLoginBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(views.getRoot());
        init();
    }

    private void init(){
        mainRepository = MainRepository.getInstance();
        views.enterBtn.setOnClickListener(v->{

            PermissionX.init(this)
                    .permissions(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
                    .request((allGranted, grantedList, deniedList) -> {
                        if (allGranted) {
                            //login to firebase here

                            mainRepository.initWebRTCClient(
                                    views.username.getText().toString(), getApplicationContext(), () -> {
                                        //if success then we want to move to call activity
                                        //TODO Change then  to MainActivity.class
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    }
                            );
                        }
                    });
        });
    }





}