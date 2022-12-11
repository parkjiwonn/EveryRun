package com.example.everyrun.UserInfo.ResetPass;

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
import com.example.everyrun.databinding.ActivityWriteEmailBinding;

// 이메일 작성하는 부분
public class WriteEmailActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = this.getClass().getSimpleName(); // log
    private ActivityWriteEmailBinding binding;
    Context context;
    String user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWriteEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();
        user_email = getIntent().getStringExtra("email");
        Log.d(TAG, "onCreate: email = "+ user_email);

        setUI(); // UI 세팅
        binding.btnNext.setOnClickListener(this);
        binding.btnCancel.setOnClickListener(this);
    }

    private void setUI() {

        // 액티비티 시작하고 edittext에 포커스 주고 키보드 올리기
        binding.etEmail.requestFocus();
        binding.etEmail.setText(user_email);

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
                    binding.btnNext.setClickable(true);
                    binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.primary));
                    binding.etEmail.setBackgroundResource(R.drawable.et_rounded);
                    binding.txWarning.setVisibility(View.GONE);
                    binding.btnCancel.setVisibility(View.VISIBLE);


                }else{
                    binding.btnNext.setClickable(false);
                    binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));
                    binding.txWarning.setVisibility(View.GONE);
                    binding.btnCancel.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 다음 버튼 선택시 이메일 정규식 검사할 것.
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.getText().toString()).matches()){

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
                    Log.d(TAG, "onClick: 이메일 정규식 o ");
                    // 올바른 이메일 작성시 다음 이메일 인증코드 이메일로 전송하고 인증코드 입력하는 액티비티로 이동.
                    binding.etEmail.setBackgroundResource(R.drawable.et_rounded);
                    binding.txWarning.setVisibility(View.GONE);


                    binding.btnNext.setClickable(true);
                    binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.primary));


                }
            }
        });


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_next:

                    // 이미 가입한 전적이 있는 이메일이라면

                    // 다음 인증코드 받는 부분으로 이동
                    startActivity(new Intent(WriteEmailActivity.this, WriteCodeActivity.class).putExtra("email",binding.etEmail.getText().toString()));



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
}