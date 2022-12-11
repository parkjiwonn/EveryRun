package com.example.everyrun.UserProfile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.everyrun.MainActivity;
import com.example.everyrun.UserInfo.UserSharedPreference;
import com.example.everyrun.R;
import com.example.everyrun.Retrofit.RetrofitClient;
import com.example.everyrun.Retrofit.RetrofitInterface;
import com.example.everyrun.RetrofitData.UserInfoData;
import com.example.everyrun.databinding.ActivityUserprofileBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName(); // log

    private ActivityUserprofileBinding binding;
    Context context;
    String user_email;
    private UserSharedPreference preferenceHelper;
    private RetrofitClient retrofitClient;
    private RetrofitInterface retrofitInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserprofileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();
        preferenceHelper = new UserSharedPreference(this);
        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();

        setUI();

        binding.btnEdit.setOnClickListener(this);



    }

    private void setUI() {
       user_email = preferenceHelper.getEmail();

        retrofitInterface.SetUserInfo(user_email).enqueue(new Callback<UserInfoData>() {
            @Override
            public void onResponse(Call<UserInfoData> call, Response<UserInfoData> response) {

                UserInfoData result = response.body();

                    String status = result.getStatus();
                    Log.d(TAG, "onResponse: result=" + result);

                    if(status.equals("true"))
                    {
                        // header에서 사용자 프로필 사진 세팅하기
                        if(result.getUser_photo().equals("basic"))
                        {
                            // 기본 이미지로 세팅해주기
                            binding.userImg.setImageResource(R.drawable.user_img);

                        }else{
                            // 사용자가 설정한 이미지 UserProfileImg
                            String url = "http://3.36.174.137/UserProfileImg/" + result.getUser_photo();
                            // glide로 이미지 세팅해주기
                            Glide.with(UserProfileActivity.this).load(url).into(binding.userImg);
                        }

                        // header에서 사용자 닉네임 세팅하기
                        binding.txNick.setText(result.getUser_name());
                    }

            }

            @Override
            public void onFailure(Call<UserInfoData> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.btn_edit:

                //프로필 수정 버튼 선택시 프로필 수정 액티비티로 이동해야한다.
                startActivity(new Intent(UserProfileActivity.this, EditUserInfoActivity.class));
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(UserProfileActivity.this, MainActivity.class));
        Log.d(TAG, "onBackPressed: 뒤로가기 누름");
        finish();

    }


}