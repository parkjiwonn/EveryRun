package com.example.everyrun.UserInfo.ResisterStep;

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

import com.example.everyrun.R;
import com.example.everyrun.databinding.ActivityStep3Binding;

import java.util.regex.Pattern;

/**
 * 비밀번호 설정할 수 있는 액티비티
 * 해당 액티비티에서 뒤로가기를 하면 이메일 입력 액티비티로 이동하게 된다.
 * 이메일이 intent로 넘어옴 비밀번호와 함께 사용자이름으로 넘겨줘야 함.
 * (1) 비밀번호 설정방법에 충족하는지 검사해야 함.
 * (2) 비밀번호 확인해야 함.
 * **/
public class Step3Activity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName(); // log
    private ActivityStep3Binding binding;
    Context context;
    String email;
    String pass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStep3Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = getApplicationContext();

        // step 2에서 온 유저 email
        email = getIntent().getStringExtra("email");
        Log.d(TAG, "onCreate: email = "+ email);

        setUI();
        binding.btnCancel.setOnClickListener(this); // 비밀번호 삭제
        binding.btnRecancel.setOnClickListener(this); // 비밀번호 확인 삭제
        binding.btnNext.setOnClickListener(this); // 다음 버튼

    }

    private void setUI()
    {

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
                String pass = binding.etPass.getText().toString();
                String repass = binding.etRepass.getText().toString();

                if(isChecked)
                {
                    Log.d(TAG, "onCheckedChanged: 비번 눈 뜸");
                    // 버튼 선택했을 경우
                    // 비밀번호가 보여야 한다.
                    binding.etPass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

                    if(pass.equals(repass))
                    {
                        binding.btnNext.setClickable(true);
                        binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.primary));
                    }


                }
                else
                {
                    Log.d(TAG, "onCheckedChanged: 비번 눈 감음");
                    // 다시 버튼 선택했을 경우
                    // 비밀번호가 안보여야 한다.
                    binding.etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    if(pass.equals(repass))
                    {
                        binding.btnNext.setClickable(true);
                        binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.primary));
                    }
                }
            }
        });

        // 비밀번호 확인 보이기 버튼
        binding.btnReview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                String pass = binding.etPass.getText().toString();
                String repass = binding.etRepass.getText().toString();

                if(isChecked)
                {
                    Log.d(TAG, "onCheckedChanged: 비번 눈 뜸");
                    // 버튼 선택했을 경우
                    // 비밀번호가 보여야 한다.
                    binding.etRepass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);


                    if(pass.equals(repass))
                    {
                        binding.btnNext.setClickable(true);
                        binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.primary));
                    }
                }
                else
                {
                    Log.d(TAG, "onCheckedChanged: 비번 눈 감음");
                    // 다시 버튼 선택했을 경우
                    // 비밀번호가 안보여야 한다.
                    binding.etRepass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);


                    if(pass.equals(repass))
                    {
                        binding.btnNext.setClickable(true);
                        binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.primary));
                    }
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
                Log.d(TAG, "onClick: 이메일 =" + email);
                // 닉네임 설정 액티비티로 유저 email, pass 보냄.
                startActivity(new Intent(Step3Activity.this,Step4Activity.class).putExtra("email",email).putExtra("pass", pass));

                break;


        }
    }
}