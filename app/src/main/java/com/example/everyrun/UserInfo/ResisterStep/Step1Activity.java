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

import com.example.everyrun.R;
import com.example.everyrun.Retrofit.RetrofitClient;
import com.example.everyrun.Retrofit.RetrofitInterface;
import com.example.everyrun.RetrofitData.UserInfoData;
import com.example.everyrun.databinding.ActivityStep1Binding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Step1Activity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = this.getClass().getSimpleName(); // log
    private ActivityStep1Binding binding;
    Context context;
    String email;
    private RetrofitClient retrofitClient;
    private RetrofitInterface retrofitInterface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityStep1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();

        setUI(); // UI 세팅
        binding.btnNext.setOnClickListener(this);
        binding.btnCancel.setOnClickListener(this);


        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();



    }

    private void setUI() {

        // 액티비티 시작하고 edittext에 포커스 주고 키보드 올리기
        binding.etEmail.requestFocus();

        // 키보드 올리기
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            // edit text가 공백이 아니면 버튼 활성화 - 색 primray
            // edit text가 공백이면 버튼 비활성화 - 색 inactive
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(binding.etEmail.length() > 0)
                {

                    binding.etEmail.setBackgroundResource(R.drawable.et_rounded);
                    binding.txWarning.setVisibility(View.GONE);
                    binding.btnCancel.setVisibility(View.VISIBLE);


                }else{
                    // 공백일 때
                    binding.btnNext.setClickable(false);
                    binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));
                    binding.btnCancel.setVisibility(View.GONE);
                    binding.etEmail.setBackgroundResource(R.drawable.et_rounded);
                    binding.txWarning.setVisibility(View.GONE);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                // 다음 버튼 선택시 이메일 정규식 검사할 것.
                if(binding.etEmail.length()>0 && !android.util.Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.getText().toString()).matches()){

                    // 이메일 정규식에 해당하지 않으면 edittext 빨간 박스 표시해주기
                    binding.etEmail.setBackgroundResource(R.drawable.et_red);
                    binding.txWarning.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onClick: 이메일 =" + binding.etEmail.getText());
                    Log.d(TAG, "onClick: 이메일 정규식 x ");
                    // 버튼 비활성화
                    binding.btnNext.setClickable(false);
                    binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));
                    binding.btnCancel.setVisibility(View.VISIBLE);

                }
                else{

                    if(binding.etEmail.length() == 0)
                    {
                        //공백일떄
                        binding.btnNext.setClickable(false);
                        binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));
                        binding.txWarning.setVisibility(View.GONE);
                        binding.btnCancel.setVisibility(View.GONE);


                    }
                    else{
                        Log.d(TAG, "onClick: 이메일 정규식 o ");
                        // 올바른 이메일 작성시 다음 이메일 인증코드 이메일로 전송하고 인증코드 입력하는 액티비티로 이동.
                        binding.etEmail.setBackgroundResource(R.drawable.et_rounded);
                        binding.txWarning.setVisibility(View.GONE);
                        binding.btnNext.setClickable(true);
                        binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.primary));

                    }


                }
            }
        });




    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_next:

                email = binding.etEmail.getText().toString();
                CheckUserEmail(email);

//                // 다음 버튼 선택시 이메일 정규식 검사할 것.
//                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.getText().toString()).matches()){
//
//                    // 이메일 정규식에 해당하지 않으면 edittext 빨간 박스 표시해주기
//                    binding.etEmail.setBackgroundResource(R.drawable.et_red);
//                    binding.txWarning.setVisibility(View.VISIBLE);
//                    Log.d(TAG, "onClick: 이메일 =" + binding.etEmail.getText());
//                    Log.d(TAG, "onClick: 이메일 정규식 x ");
//                }
//                else{
//                    Log.d(TAG, "onClick: 이메일 정규식 o ");
//                    // 올바른 이메일 작성시 다음 이메일 인증코드 이메일로 전송하고 인증코드 입력하는 액티비티로 이동.
//                    Log.d(TAG, "onClick: 이메일 = " + binding.etEmail.getText().toString());
//
//                    email = binding.etEmail.getText().toString();
//                    // 유저 이메일이 중복인지 아닌지 검사해야 함.
//                    CheckUserEmail(email);
//
//                }

                break;

            case R.id.btn_cancel:

                Log.d(TAG, "onClick: 텍스트 삭제 버튼 선택됨 " );
                binding.etEmail.setText(null);
                binding.btnCancel.setVisibility(View.GONE);
                binding.etEmail.setBackgroundResource(R.drawable.et_rounded);
                binding.txWarning.setVisibility(View.GONE);
                break;

        }
    }

    // 유저 이메일 중복 검사하는 메서드
    private void CheckUserEmail(String email) {

        Log.d(TAG, "CheckUserEmail: email =" + email );
        Log.d(TAG, "CheckUserEmail: retrointer = "+retrofitInterface);

        retrofitInterface.CheckEmail(email).enqueue(new Callback<UserInfoData>() {
            @Override
            public void onResponse(Call<UserInfoData> call, Response<UserInfoData> response) {

                if(response.isSuccessful())
                {
                    UserInfoData result = response.body();

                    String status= result.getStatus();
                    String message = result.getMessage();

                    Log.d(TAG, "onResponse: status = "+ status);
                    Log.d(TAG, "onResponse: message = "+ message);

                    /**
                     * status = true 회원가입 가능
                      status = false 중복된 이메일
                     true -> 다음 액티비티로 이동
                     false -> 이미가입했으니 로그인하거나 비밀번호 찾겠냐는 액티비티로 이동
                     **/

                    // 서버로 이메일을 잘 가고 있음.
                    // db 접근이 안되는 것 같음.

                    if(status.equals("true"))
                    {
                        // 가입 가능
                        // 인증 코드 작성하는 액티비티로 유저 email 보냄
                        startActivity(new Intent(Step1Activity.this,Step2Activity.class).putExtra("email",binding.etEmail.getText().toString()));
                    }
                    else
                    {
                        // 이메일 중복
                        // 이미 가입 했으니 (1) 로그인 할지 (2) 비밀번호 찾을지 선택하게하는 액티비티로 이동시키기
                        startActivity(new Intent(Step1Activity.this,ReloginActivity.class).putExtra("email", binding.etEmail.getText().toString()));
                        // 이메일 intent로 보내야한다.
                    }


                }
                else{
                    Log.d(TAG, "onFailure: 통신 실패");
                }
            }

            @Override
            public void onFailure(Call<UserInfoData> call, Throwable t) {


                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }
}