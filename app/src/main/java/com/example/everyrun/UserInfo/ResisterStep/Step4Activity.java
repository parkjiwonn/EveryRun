package com.example.everyrun.UserInfo.ResisterStep;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;

import com.example.everyrun.MainActivity;
import com.example.everyrun.UserInfo.UserSharedPreference;
import com.example.everyrun.R;
import com.example.everyrun.Retrofit.RetrofitClient;
import com.example.everyrun.Retrofit.RetrofitInterface;
import com.example.everyrun.RetrofitData.UserInfoData;
import com.example.everyrun.databinding.ActivityStep4Binding;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Step4Activity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName(); // log
    private ActivityStep4Binding binding;
    Context context;
    String user_email;
    String user_pass;
    String user_id;
    private RetrofitClient retrofitClient;
    private RetrofitInterface retrofitInterface;
    private UserSharedPreference preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // binding 객체 생성하기
        binding = ActivityStep4Binding.inflate(getLayoutInflater());
        // 해당 액티비티에 맞는 레이아웃 불러오기
        setContentView(binding.getRoot());
        context = getApplicationContext();

        //step3 액티비티에서 보낸 유저 email, pass.
        user_email = getIntent().getStringExtra("email");
        user_pass = getIntent().getStringExtra("pass");
        Log.d(TAG, "onCreate: email, pass =" + user_email +" , "+ user_pass);
        // email, pass 잘 받아오는 거 확인함.

        setUI(); // UI 세팅 하기.

        binding.btnNext.setOnClickListener(this);

        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();

        // preferenceHelper 객체를 안 만들어 줘서 오류 생겼었음.
        preferenceHelper = new UserSharedPreference(this);


    }

    // UI 세팅하는 메서드
    private void setUI() {

        /**
         * (1) 사용자가 입력한 ID가 설정 방법에 맞는지 먼저 확인
         * (2) ID 중복 검사
         * **/

        // 액티비티 넘어올때 바로 포커싱 잡기
        binding.etId.requestFocus(); // edittext 에 포커싱 주기

        // 키보드 올리기
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        // 닉네임 작성할 때 textwatcher
        binding.etId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                binding.imgcheck.setVisibility(View.GONE);
                // 닉네임 작성중 일땐 체크 이미지 gone
            }

            @Override
            public void afterTextChanged(Editable editable) {

                // 입력이 완료된 후에 설정방법에 맞는지 확인

                if (binding.etId.length() > 0) {
                    // 공백이 아니라면
                    if (binding.etId.length() > 1) {
                        Log.d(TAG, "afterTextChanged: 3자 이상");

                        CheckIdOverlap(); // ID 중복 검사 메서드

                    } else {
                        Log.d(TAG, "afterTextChanged: 2개 이하");
                        // 닉네임이 2개 이하니까 경고 메세지
                        // edittext 빨간 테두리
                        // 버튼 비활성화, 색변경
                        binding.etId.setBackgroundResource(R.drawable.et_red);
                        binding.txWarning.setVisibility(View.VISIBLE);
                        binding.txWarning.setText("ID를 2자 이상 입력하세요.");
                        binding.btnNext.setClickable(false);
                        binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));
                    }
                } else {
                    // 공백이라면
                    // 버튼 비활성화, 색변경
                    // 다시 edittext 원상복구시키기
                    binding.etId.setBackgroundResource(R.drawable.et_rounded);
                    binding.txWarning.setVisibility(View.GONE);
                    binding.btnNext.setClickable(false);
                    binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));
                }


            }
        });

        // 물음표 (닉네임 설정방법 설명 말풍선)
        binding.imgQuestion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                // 물음표 선택했을 때
                if (isChecked) {
                    binding.txtBanner.setVisibility(View.VISIBLE);
                    binding.txtBannerTail.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onCheckedChanged: 물음표 클릭 보이기");
                }
                // 물음표 먈풍선 닫을 때
                else {
                    binding.txtBanner.setVisibility(View.GONE);
                    binding.txtBannerTail.setVisibility(View.GONE);
                    Log.d(TAG, "onCheckedChanged: 물음표 안보이기");
                }
            }
        });


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_next:

                /**
                 * 다음 버튼 클릭
                 * 회원 가입하면 로그인 하지 않고 바로 자동 로그인 되고 앱 사용가능하게 하기**/

                Log.d(TAG, "onClick: userinfo = "+ user_email +"," + user_pass +"," +user_id);
                SaveUserInfo(user_email,user_id,user_pass);

                break;

        }
    }

    // 유저 정보 저장하는 메서드
    private void SaveUserInfo(String user_email, String user_id, String user_pass) {

        retrofitInterface.SaveUserInfo(user_email,user_pass,user_id).enqueue(new Callback<UserInfoData>() {
            @Override
            public void onResponse(Call<UserInfoData> call, Response<UserInfoData> response) {

                // 응답이 성공적인지 아닌지 먼저 구분하기.
                if(response.isSuccessful())
                {
                    // 서버에서 온 응답 DTO로 받기
                    UserInfoData result = response.body();
                    String status = result.getStatus();

                    // 가입 성공인지 아닌지 확인하기
                    if(status.equals("true"))
                    {
                        Log.d(TAG, "onResponse: 가입 성공");
                        /**
                         * 가입 성공이면 메인 액티비티로 이동하게 하기
                         * Shared에 회원가입한 유저의 정보 저장하기
                         * 회원가입하고 이후에 앱 사용할때 자동으로 로그인 되게 하기**/

                        //shared에 유저 이메일, 닉네임 저장하기
                        SaveShared(user_email,user_id);

                        Intent i = new Intent(Step4Activity.this, MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        // 그 전에 쌓인 스택들 모두 종료시켜야 한다.


                    }else{
                        // mysql 쿼리문이 제대로 안돌았다.
                        Log.d(TAG, "onResponse: 가입 실패");
                    }

                }else {
                    Log.d(TAG, "onResponse: 응답 안됨. ");
                }


            }

            // 쉐어드에 유저 이메일, 닉네임 저장하는 메서드
            private void SaveShared(String user_email, String user_id) {
                preferenceHelper.putIsLogin(true);
                preferenceHelper.putEmail(user_email);
                preferenceHelper.putNick(user_id);
            }

            @Override
            public void onFailure(Call<UserInfoData> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }


    private void CheckIdOverlap() {

        user_id = binding.etId.getText().toString();


        Log.d(TAG, "afterTextChanged: 잘 설정");
        // id 중복확인해야 함.

        // 특수문자 있는지 확인
        // 한글 영어 숫자 일부 특수문자만 허용
        Boolean test = Pattern.matches("^[0-9a-zA-Zㄱ-ㅎ가-힣]*$", user_id);

        // 밑줄과 마침표만 확인하기
        if(test || user_id.contains(".") || user_id.contains("-") || user_id.contains("_"))
        {
            Log.d(TAG, "CheckIdOverlap: o");

            retrofitInterface.CheckId(user_id).enqueue(new Callback<UserInfoData>() {
                @Override
                public void onResponse(Call<UserInfoData> call, Response<UserInfoData> response) {

                    UserInfoData result = response.body();

                    String status = result.getStatus();

                    if (status.equals("true")) {
                        Log.d(TAG, "onResponse: 가입 가능");
                        binding.etId.setBackgroundResource(R.drawable.et_rounded);
                        binding.txWarning.setVisibility(View.GONE);
                        binding.imgcheck.setVisibility(View.VISIBLE);

                        binding.btnNext.setClickable(true);
                        binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.primary));

                        // 가입가능한 ID니까 DB에 회원 정보저장해야함.

                    } else {
                        Log.d(TAG, "onResponse: id 중복");
                        binding.etId.setBackgroundResource(R.drawable.et_red);
                        binding.txWarning.setVisibility(View.VISIBLE);
                        binding.txWarning.setText("중복된 ID입니다.");
                        binding.btnNext.setClickable(false);
                        binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));
                    }
                }

                @Override
                public void onFailure(Call<UserInfoData> call, Throwable t) {
                    Log.e(TAG, "에러 = " + t.getMessage());
                }
            });

        }
        else
        {
            Log.d(TAG, "CheckIdOverlap: X");
            // 닉네임 설정 방법과 맞지 않다.
            binding.etId.setBackgroundResource(R.drawable.et_red);
            binding.txWarning.setVisibility(View.VISIBLE);
            binding.txWarning.setText("닉네임 설정 방법에 맞지 않습니다.");
        }



    }
}