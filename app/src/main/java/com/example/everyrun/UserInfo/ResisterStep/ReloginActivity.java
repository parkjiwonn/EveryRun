package com.example.everyrun.UserInfo.ResisterStep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.everyrun.MainActivity;
import com.example.everyrun.UserInfo.ResetPass.WriteEmailActivity;
import com.example.everyrun.UserInfo.UserSharedPreference;
import com.example.everyrun.R;
import com.example.everyrun.Retrofit.RetrofitClient;
import com.example.everyrun.Retrofit.RetrofitInterface;
import com.example.everyrun.RetrofitData.UserInfoData;
import com.example.everyrun.databinding.ActivityReloginBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 재로그인하는 액티비티
 * (1) 다시 로그인할 것을 물어봄 - 로그인 (비밀번호만 입력)
 * (2) 비밀번호 잊어버렸는지 물어봄  - 비밀번호 재설정
 * **/
public class ReloginActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = this.getClass().getSimpleName(); // log
    private ActivityReloginBinding binding;
    Context context;
    String user_email;
    String user_pass;
    private RetrofitClient retrofitClient;
    private RetrofitInterface retrofitInterface;
    private UserSharedPreference preferenceHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReloginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();

        user_email = getIntent().getStringExtra("email");
        Log.d(TAG, "onCreate: email = "+ user_email);

        setUI();
        binding.btnLogin.setOnClickListener(this);
        binding.txFindpassword.setOnClickListener(this);

        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();
        preferenceHelper = new UserSharedPreference(this);
    }

    private void setUI() {
        binding.etEmail.setText(user_email);

        binding.etPassword.requestFocus();

        // 키보드 올리기
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            // 로그인 버튼 클릭 - 올바른 비밀번호인지 확인해야 함.
            case R.id.btn_login:
                // 유저 이메일과 비밀번호 먼저 변수에 세팅해주기
                user_email = binding.etEmail.getText().toString();
                user_pass = binding.etPassword.getText().toString();
                Login(user_email,user_pass);

                break;

                // 비밀번호 재설정 버튼 - 비밀번호 재설정 액티비티로 이동해야함.
            case R.id.tx_findpassword:
                /**비밀번호 재설정 과정
                 * (1) 이메일 입력하고
                 * (2) 이메일 인증코드 작성
                 * (3) 비밀먼호 재설정 **/

                startActivity(new Intent(ReloginActivity.this, WriteEmailActivity.class).putExtra("email", user_email));
                finish();
                break;
        }
    }

    // 로그인하는 매서드
    private void Login(String user_email, String user_pass) {
        retrofitInterface.Login(user_email,user_pass).enqueue(new Callback<UserInfoData>() {
            @Override
            public void onResponse(Call<UserInfoData> call, Response<UserInfoData> response) {

                UserInfoData result = response.body();

                if(response.isSuccessful())
                {
                    Log.d(TAG, "onResponse: result =" + result);
                    String status = result.getStatus();
                    String user_email = result.getUser_email();
                    String user_id = result.getUser_name();
                    String user_photo = result.getUser_photo();


                    if(status.equals("true"))
                    {
                        Log.d(TAG, "onResponse: 로그인 성공");
                        // shared에 입력
                        // 재설정 되었으면
                        preferenceHelper.putIsLogin(true);
                        preferenceHelper.putEmail(user_email);
                        preferenceHelper.putNick(user_id);
                        preferenceHelper.putPROFILE(user_photo);

                        Intent i = new Intent(ReloginActivity.this, MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        // 그 전에 쌓인 스택들 모두 종료시켜야 한다.
                    }else{
                        Log.d(TAG, "onResponse: 로그인 실패");
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
}