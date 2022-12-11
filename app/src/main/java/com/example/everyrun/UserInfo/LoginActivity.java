package com.example.everyrun.UserInfo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.everyrun.MainActivity;
import com.example.everyrun.UserInfo.ResetPass.FromLoginFindPassActivity;
import com.example.everyrun.UserInfo.ResisterStep.Step1Activity;
import com.example.everyrun.R;
import com.example.everyrun.Retrofit.RetrofitClient;
import com.example.everyrun.Retrofit.RetrofitInterface;
import com.example.everyrun.RetrofitData.UserInfoData;
import com.example.everyrun.databinding.ActivityLoginBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 로그인 액티비티
 * (1) 이메일, 비밀번호를 입력 후 로그인
 * (2) 비밀번호 재설정
 * (3) 회원가입
 * **/
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName(); // log
    private ActivityLoginBinding binding;
    String user_email;
    String user_pwd;
    private RetrofitClient retrofitClient;
    private RetrofitInterface retrofitInterface;
    private UserSharedPreference preferenceHelper;

    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUI(); // UI 세팅

        binding.btnLogin.setOnClickListener(this);
        binding.btnRegister.setOnClickListener(this);
        binding.txFindpassword.setOnClickListener(this);

        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();
        preferenceHelper = new UserSharedPreference(this);

    }

    // UI 세팅해주는 메서드
    private void setUI()
    {


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {


            // case 1 : 로그인 버튼 클릭
            case R.id.btn_login:
                // 유저 이메일과 비밀번호 먼저 변수에 세팅해주기
                user_email = binding.etEmail.getText().toString();
                user_pwd = binding.etPassword.getText().toString();
                Login(user_email,user_pwd);

                break;

            // case 2 : 회원가입 버튼 클릭
            case R.id.btn_register:
                startActivity(new Intent(LoginActivity.this, Step1Activity.class));
                // 회원가입 페이지에서 뒤로가기했을 때 로그인 페이지가 보여야 한다.
                break;

            case R.id.tx_findpassword:
                startActivity(new Intent(LoginActivity.this, FromLoginFindPassActivity.class));
                // 회원가입 페이지에서 뒤로가기했을 때 로그인 페이지가 보여야 한다.
                break;

        }
    }

    // 로그인 하는 메서드
    private void Login(String user_email, String user_pwd) {

        /**서버랑 통신에서 로그인한 유저가 db에 저장되어 있는 유저인지 확인해야 한다.
         * 로그인 하고 shared에 유저 정보 저장해야함.
         * **/

        // 일단 로그인한 정보가 db에 저장되어있는 정보인지 확인하기.
        // 그 전에 서버에서 비밀번호 암호화하고 db에 저장하기.
        // 암호화함.
        retrofitInterface.Login(user_email,user_pwd).enqueue(new Callback<UserInfoData>() {
            @Override
            public void onResponse(Call<UserInfoData> call, Response<UserInfoData> response) {
                
                UserInfoData result = response.body();
                
                if(response.isSuccessful())
                {
                    Log.d(TAG, "onResponse: result =" + result);
                    String status = result.getStatus();
                    String user_id = result.getUser_name();
                    String user_photo = result.getUser_photo();
                    
                    if(status.equals("true"))
                    {
                        Log.d(TAG, "onResponse: 로그인 성공");
                        // 로그인 성공시 shared에 유저 정보 저장하기
                        preferenceHelper.putIsLogin(true);
                        preferenceHelper.putEmail(user_email);
                        preferenceHelper.putNick(user_id);
                        preferenceHelper.putPROFILE(user_photo);

                        // 로그인 성공했으니까 뒤로가기 했을 때 로그인 페이지가 보이면 안된다.
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }else{
                        Log.d(TAG, "onResponse: 로그인 실패");
                        // 로그인 실패시 유저에게 이를 알려줘야 한다.
                        binding.txWarning.setVisibility(View.VISIBLE);
                    }
                    
                }
                else{
                    Log.d(TAG, "onResponse: 응답 실패");
                }
            }

            @Override
            public void onFailure(Call<UserInfoData> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });

    }

    @Override
    public void onBackPressed() {
        // 기존 뒤로가기 버튼의 기능을 막기위해 주석처리 또는 삭제
        // super.onBackPressed();

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            toast.cancel();
        }
    }
}