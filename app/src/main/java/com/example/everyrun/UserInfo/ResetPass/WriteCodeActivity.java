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
import android.widget.Toast;

import com.example.everyrun.UserInfo.ResisterStep.GMailSender;
import com.example.everyrun.R;
import com.example.everyrun.databinding.ActivityWriteCodeBinding;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;

public class WriteCodeActivity extends AppCompatActivity  implements View.OnClickListener{

    private final String TAG = this.getClass().getSimpleName(); // log
    private ActivityWriteCodeBinding binding;
    Context context;
    String email;
    String emailcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWriteCodeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();

        // step1 에서 인텐트로 전달한 유저 이메일. -> tx_email에 세팅해줘야 함.
        email = getIntent().getStringExtra("email");
        Log.d(TAG, "onCreate: email = " + email );



        // 이메일로 인증코드 전송
        SendEmailCode();
        setUI(); //UI 세팅

        binding.btnNext.setOnClickListener(this);
        binding.txResend.setOnClickListener(this);
    }

    private void setUI() {
        // 전달받은 이메일 tx_email에 세팅해주기
        Log.d(TAG, "setUI: email = " + email);
        binding.txEmail.setText(email);

        binding.etEmail.requestFocus();

        // 키보드 올리기
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        // 인증 코드 입력하면 다음 버튼 활성화되도록 하기.
        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            // edit text 가 공백이 아니라면 버튼 활성화
            // 공백이라면 버튼 비활성화
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(binding.etEmail.length() > 0)
                {
                    binding.btnNext.setClickable(true);
                    binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.primary));
                    binding.etEmail.setBackgroundResource(R.drawable.et_rounded);
                    binding.txWarning.setVisibility(View.GONE);
                }
                else
                {
                    binding.btnNext.setClickable(false);
                    binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(binding.etEmail.length()>0)
                {
                    if(binding.etEmail.getText().toString().equals(emailcode))
                    {
                        Log.d(TAG, "onClick: 인증코드 맞음");
                        // 맞으면 비밀번호 설정 액티비티로 이동
                        Log.d(TAG, "onClick: 비밀번호 설정 액티비티로 넘어가기 전 email ="+ email);
                        // 비밀번호 설정에도 유저 email 보냄.
                        binding.etEmail.setBackgroundResource(R.drawable.et_rounded);
                        binding.txWarning.setVisibility(View.GONE);
                        binding.btnNext.setClickable(true);
                        binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.primary));

                    }
                    else{
                        Log.d(TAG, "onClick: 인증코드 안맞음");
                        // 안맞으면 인증코드 안맞다고 유저에게 알려주기
                        binding.etEmail.setBackgroundResource(R.drawable.et_red);
                        binding.txWarning.setVisibility(View.VISIBLE);
                        binding.btnNext.setClickable(false);
                        binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));

                    }
                }else{
                    // 공백
                    binding.etEmail.setBackgroundResource(R.drawable.et_rounded);
                    binding.txWarning.setVisibility(View.GONE);
                    binding.btnNext.setClickable(false);
                    binding.btnNext.setBackgroundColor(context.getResources().getColor(R.color.inactive));
                }
            }
        });

    }

    // 유저 이메일로 인증 코드 보내는 메서드
    private void SendEmailCode() {


        MailTread mailTread = new MailTread();
        //메일을 보내주는 쓰레드
        mailTread.start();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            // 다음 버튼 클릭 - 이메일 인증 코드 맞는 건지 확인
            case R.id.btn_next:

                Log.d(TAG, "onClick: emailcode ="+emailcode);

                if(binding.etEmail.getText().toString().equals(emailcode))
                {
                    Log.d(TAG, "onClick: 인증코드 맞음");
                    // 맞으면 비밀번호 설정 액티비티로 이동
                    Log.d(TAG, "onClick: 비밀번호 설정 액티비티로 넘어가기 전 email ="+ email);
                    // 비밀번호 설정에도 유저 email 보냄.
                    startActivity(new Intent(WriteCodeActivity.this, ResetPassActivity.class).putExtra("email", email));
                    finish();
                }
                else{
                    Log.d(TAG, "onClick: 인증코드 안맞음");
                    // 안맞으면 인증코드 안맞다고 유저에게 알려주기
                    binding.etEmail.setBackgroundResource(R.drawable.et_red);
                    binding.txWarning.setVisibility(View.VISIBLE);
                }


                break;

            case R.id.tx_resend:

                Log.d(TAG, "onClick: 코드 재전송 클릭");

                // 코드 재전송할 때 toast 메세지 띄워주기
                Toast.makeText(getApplicationContext(), "코드가 재전송되었습니다.", Toast.LENGTH_LONG).show();
                SendEmailCode();
                break;


        }
    }

    class MailTread extends Thread{
        public void run()
        { GMailSender gMailSender = new GMailSender("monspirit47@gmail.com", "wujazmnpbolztnnb");
            emailcode=gMailSender.getEmailCode();
            //인증코드
            Log.d(TAG, "run: emailcode =" + emailcode);
            String content = "안녕하세요. EveryRun 입니다. \r\n회원가입시 필요한 인증코드 메일입니다. \r\n아래 인증코드를 인증 코드 기입란에 입력하시기 바랍니다. \r\n 인증코드: " + emailcode;
            try {
                gMailSender.sendMail(emailcode +" 이(가) 인증코드입니다.", content , email);
            }
            catch (SendFailedException e) {
                System.out.println("SendFailedException 문제"+e);
            }
            catch (MessagingException e) {
                System.out.println("인터넷 문제"+e);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}