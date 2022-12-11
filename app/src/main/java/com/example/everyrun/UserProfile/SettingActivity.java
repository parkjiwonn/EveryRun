package com.example.everyrun.UserProfile;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.everyrun.UserInfo.LoginActivity;
import com.example.everyrun.UserInfo.ResetPass.FromLoginFindPassActivity;
import com.example.everyrun.UserInfo.UserSharedPreference;
import com.example.everyrun.R;
import com.example.everyrun.databinding.ActivitySettingBinding;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName(); // log
    private UserSharedPreference preferenceHelper;
    private ActivitySettingBinding binding;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();

        binding.txResetpass.setOnClickListener(this);
        binding.btnLogout.setOnClickListener(this);
        // shared 객체 생성
        preferenceHelper = new UserSharedPreference(this);
        binding.btnLogout.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_logout:


                BuildDiaglog();

                break;

            case R.id.tx_resetpass:

                startActivity(new Intent(SettingActivity.this, FromLoginFindPassActivity.class));
                // shared 정보 삭제해야함.
                finish();

                break;
        }
    }

    private void BuildDiaglog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setMessage("로그아웃 하시겠습니까?");
        builder.setNegativeButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                preferenceHelper.clear(); // shared 정보 삭제

                startActivity(new Intent(SettingActivity.this, LoginActivity.class));
                // shared 정보 삭제해야함.
                finish();
            }
        });
        builder.setPositiveButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.show();
    }
}