package com.example.everyrun.UserInfo.ResetPass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
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
import com.example.everyrun.databinding.ActivityResetPassBinding;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPassActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = this.getClass().getSimpleName(); // log
    private ActivityResetPassBinding binding;
    Context context;
    String email;
    String pass;
    private RetrofitClient retrofitClient;
    private RetrofitInterface retrofitInterface;

    private UserSharedPreference preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();

        email = getIntent().getStringExtra("email");
        Log.d(TAG, "onCreate: email = "+ email);

        setUI();
        binding.btnCancel.setOnClickListener(this); // 비밀번호 삭제
        binding.btnRecancel.setOnClickListener(this); // 비밀번호 확인 삭제
        binding.btnNext.setOnClickListener(this); // 다음 버튼

        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();
        preferenceHelper = new UserSharedPreference(this);


    }

    private void setUI() {
        /**(1) 비밀번호 틀렸을 경우에 빨간 테두리 그어지고 , 위에 문구 빨간색으로 변함
         (2) 비밀번호 확인 틀렸을 경우에 빨간 테두리 그어지고, 밑에 문구 생김.
         (3) 입력값이 널값일때 원래대로 돌아와야 함.
         **/

        binding.etPass.requestFocus();

        // 키보드 올리기
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        binding.etPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            // 비밀번호가 입력중이라면 삭제, 눈 아이콘 visible 상태가 되어야 한다.
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(binding.etPass.length()>0)
                {
                    binding.btnCancel.setVisibility(View.VISIBLE);
                    binding.btnView.setVisibility(View.VISIBLE);
                    binding.btnNext.setClickable(false);
                    binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));
                }
                else
                {

                    binding.btnCancel.setVisibility(View.GONE);
                    binding.btnView.setVisibility(View.GONE);
                    binding.btnNext.setClickable(false);
                    binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));

                }



            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(binding.etPass.length()>0 && !Pattern.matches("^(?=.*\\d)(?=.*[~`!@#$%\\^&*()-])(?=.*[a-zA-Z]).{6,16}$", binding.etPass.getText().toString())){
                    // 비밀번호 설정방법과 틀릴때
                    // 공백이 들어있거나, 20자 넘기면 입력 제한 걸기
                    binding.etPass.setBackgroundResource(R.drawable.et_red);
                    binding.txWarning.setVisibility(View.VISIBLE);
                    binding.imgCheck.setVisibility(View.GONE);
                    binding.btnNext.setClickable(false);
                    binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));

                }
                else{
                    // 비밀번호 설정방법에 맞게 입력했을 때 -> 체크 이미지 나오게 해야함.
                    if(binding.etPass.length() == 0)
                    {
                        binding.etPass.setBackgroundResource(R.drawable.et_rounded);
                        binding.txWarning.setVisibility(View.GONE);
                        binding.btnNext.setClickable(false);
                        binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));
                    }
                    else
                    {
                        binding.etPass.setBackgroundResource(R.drawable.et_rounded);
                        binding.txWarning.setVisibility(View.GONE);
                        binding.imgCheck.setVisibility(View.VISIBLE);
                        pass = binding.etPass.getText().toString();
                        binding.btnNext.setClickable(false);
                        binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));
                    }

                }

            }
        });

        /**비밀번호 확인란 - 입력한 비밀번호와 맞는지 아닌지 확인
         * (1) 일차한다면 - 다음 버튼 활성화, 체크 이미지 생성
         * (2) 불일치한다면 - 다음 버튼 비활성화, 경고메세지 생성
         * **/

        binding.etRepass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(binding.etRepass.length() > 0)
                {
                    binding.btnRecancel.setVisibility(View.VISIBLE);
                    binding.btnReview.setVisibility(View.VISIBLE);
                    binding.btnNext.setClickable(false);
                    binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));
                }
                else
                {
                    Log.d(TAG, "onTextChanged: 비밀번호 확인 공백임");
                    binding.btnNext.setClickable(false);
                    binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(binding.etRepass.length() > 0 &&!binding.etPass.getText().toString().equals(binding.etRepass.getText().toString()))
                {
                    Log.d(TAG, "afterTextChanged:  불일치" );
                    binding.etRepass.setBackgroundResource(R.drawable.et_red);
                    binding.txRewarning.setVisibility(View.VISIBLE);
                    binding.imgRecheck.setVisibility(View.GONE);
                    binding.btnNext.setClickable(false);
                    binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));
                }
                else
                {
                    if(binding.etRepass.length() == 0){
                        // 공백임.
                        binding.etRepass.setBackgroundResource(R.drawable.et_rounded);
                        binding.txRewarning.setVisibility(View.GONE);
                        binding.btnNext.setClickable(false);
                        binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));
                    }
                    else
                    {
                        Log.d(TAG, "afterTextChanged: 일치");
                        binding.imgRecheck.setVisibility(View.VISIBLE);
                        binding.etRepass.setBackgroundResource(R.drawable.et_rounded);
                        binding.txRewarning.setVisibility(View.GONE);

                        // 비밀번호 일치한다는 것은 모두 입력했다는 뜻. 버튼 활성화
                        binding.btnNext.setClickable(true);
                        binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.primary));
                    }

                }
            }
        });

        // 비밀번호 보이기 버튼
        binding.btnView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if(isChecked)
                {
                    Log.d(TAG, "onCheckedChanged: 비번 눈 뜸");
                    // 버튼 선택했을 경우
                    // 비밀번호가 보여야 한다.
                    binding.etPass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                else
                {
                    Log.d(TAG, "onCheckedChanged: 비번 눈 감음");
                    // 다시 버튼 선택했을 경우
                    // 비밀번호가 안보여야 한다.
                    binding.etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });

        // 비밀번호 확인 보이기 버튼
        binding.btnReview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked)
                {
                    Log.d(TAG, "onCheckedChanged: 비번 눈 뜸");
                    // 버튼 선택했을 경우
                    // 비밀번호가 보여야 한다.
                    binding.etRepass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                else
                {
                    Log.d(TAG, "onCheckedChanged: 비번 눈 감음");
                    // 다시 버튼 선택했을 경우
                    // 비밀번호가 안보여야 한다.
                    binding.etRepass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {


            case R.id.btn_cancel:

                binding.etPass.setText(null);
                binding.btnCancel.setVisibility(View.GONE);
                binding.btnView.setVisibility(View.GONE);
                binding.etPass.setBackgroundResource(R.drawable.et_rounded);
                binding.txWarning.setVisibility(View.GONE);
                binding.imgCheck.setVisibility(View.GONE);
                break;

            case R.id.btn_recancel:
                binding.etRepass.setText(null);
                binding.btnRecancel.setVisibility(View.GONE);
                binding.btnReview.setVisibility(View.GONE);
                binding.etRepass.setBackgroundResource(R.drawable.et_rounded);
                binding.txRewarning.setVisibility(View.GONE);
                binding.imgRecheck.setVisibility(View.GONE);
                break;

            case R.id.btn_next:

                pass = binding.etPass.getText().toString();
                Log.d(TAG, "onClick: 비밀번호 = " + pass);

                // 재설정한 비밀번호 db에 다시 저장해야함.
                ResetPass(email, pass);

                // 비밀번호 재설정 끝났으니까 메인화면으로 이동

                Intent i = new Intent(ResetPassActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                // 그 전에 쌓인 스택들 모두 종료시켜야 한다.

                break;


        }
    }

    // 비밀번호 재설정 서버 통신을 위한 메서드
    private void ResetPass(String email, String pass) {
        retrofitInterface.ResetPass(email, pass).enqueue(new Callback<UserInfoData>() {
            @Override
            public void onResponse(Call<UserInfoData> call, Response<UserInfoData> response) {

                UserInfoData result = response.body();

                Log.d(TAG, "onResponse: result =" + result);
                // 비밀번호 재설정 됐으면 다시 로그인 되어야 함.
                String status = result.getStatus();
                String user_email = result.getUser_email();
                String user_id = result.getUser_name();
                String user_photo = result.getUser_photo();

                if(status.equals("true"))
                {
                    // 재설정 되었으면
                    preferenceHelper.putIsLogin(true);
                    preferenceHelper.putEmail(user_email);
                    preferenceHelper.putNick(user_id);
                    preferenceHelper.putPROFILE(user_photo);

                }
            }

            @Override
            public void onFailure(Call<UserInfoData> call, Throwable t) {

            }
        });
    }
}