package com.example.everyrun.Community.CrewSetting;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.everyrun.Community.CommunityActivity;
import com.example.everyrun.Community.Crew_CRUD.CreateCrewActivity;
import com.example.everyrun.Community.Crew_CRUD.UpdateCrewActivity;
import com.example.everyrun.R;
import com.example.everyrun.Retrofit.RetrofitClient;
import com.example.everyrun.Retrofit.RetrofitInterface;
import com.example.everyrun.RetrofitData.CrewData;
import com.example.everyrun.RetrofitData.JoinData;
import com.example.everyrun.UserInfo.UserSharedPreference;
import com.example.everyrun.databinding.ActivityCrewSettingBinding;
import com.example.everyrun.databinding.ActivityReadCrewBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrewSettingActivity extends AppCompatActivity implements View.OnClickListener  {

    private final String TAG = this.getClass().getSimpleName(); // log

    private ActivityCrewSettingBinding binding;
    Context context;

    private UserSharedPreference preferenceHelper;
    private RetrofitClient retrofitClient;
    private RetrofitInterface retrofitInterface;

    // 인텐트로 받는 변수들
    int crew_id;
    int reader; // 0 이면 관리자, 1이면 참여자
    int member; // 가입 신청 승인하는지 안하는지
    int current;
    String title; // 크루 제목

    String user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCrewSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();

        preferenceHelper = new UserSharedPreference(this);
        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();


        crew_id = getIntent().getIntExtra("crew_id", 0);
        reader = getIntent().getIntExtra("reader", 0);
        title = getIntent().getStringExtra("title");
        current = getIntent().getIntExtra("current", 0);
        Log.d(TAG, "onCreate: crew_id =" + crew_id);
        Log.d(TAG, "onCreate: reader =" + reader);
        Log.d(TAG, "onCreate: title =" + title);
        Log.d(TAG, "onCreate: current =" + current);


        setUI();

        //===============크루 관리자 기능==============================
        binding.btnAccept.setOnClickListener(this); // 가입 승인 버튼
        binding.btnChange.setOnClickListener(this); // 리더 위임 버튼
        binding.btnDelete.setOnClickListener(this); // 정보 수정 버튼
        binding.btnUpdate.setOnClickListener(this); // 크루 삭제 버튼
        //============================================================

        //===============크루 참여자 기능==================================
        binding.btnOut.setOnClickListener(this); // 크루 탈퇴하기 버튼
        //=============================================================
    }

    private void setUI() {
        /* setting 액티비티로 넘어온 유저에 따라서 설정 레이아웃 달라져야 한다.
        * (1) 관리자가 설정 액티비티에 들어온경우
        * - 모든 설정 요소가 있어야 한다.
        * (2) 참여자가 설정 액티비티에 들어온경우
        * - 크루 탈퇴만 있어야 한다.*/
        user_email = preferenceHelper.getEmail();

        if(reader==0)
        {
            // 현 유저는 현 크루의 관리자이다.
            binding.linearReader.setVisibility(View.VISIBLE);
            // 관리자는 크루탈퇴가 없어야 한다.
            binding.linearOut.setVisibility(View.GONE);
            binding.lastview.setVisibility(View.GONE);
            binding.lastview2.setVisibility(View.GONE);

            // 크루 가입 신청이 있으면 세팅해주기
            member = getIntent().getIntExtra("member", 0);
            Log.d(TAG, "setUI: member = "+member);
            
            if(member == 1) {
                // 바로 가입하는 크루
//                binding.txNum.setVisibility(View.INVISIBLE);
                binding.layoutAccept.setVisibility(View.GONE);

            }else{
                // 가입 승인해야 하는 크루
                SetJoinNum(crew_id);
            }

            if(current > 1)
            {
                // 크루원이 1명 이상이다
            }else{
                // 크루원이 1명이다.
                binding.layoutLeaderchange.setVisibility(View.GONE);
                binding.line3.setVisibility(View.GONE);
            }





        }else{
            // 현 유저는 현 크루의 참여자이다.
        }

    }

    // 현 크루의 가입 신청 갯수 사는 메서드
    private void SetJoinNum(int crew_id) {

        retrofitInterface.SetJoinNum(crew_id).enqueue(new Callback<JoinData>() {
            @Override
            public void onResponse(Call<JoinData> call, Response<JoinData> response) {
                JoinData result = response.body();
                Log.d(TAG, "onResponse: result =" + result);
                int join_num = result.getJoin_num();

                // 가입 신청 갯수가 한개 이상이라면 컴포넌트 visible이어야 한다.
                if(join_num > 0) {
                    binding.txNum.setVisibility(View.VISIBLE);
                    binding.txNum.setText(String.valueOf(join_num));
                }else{
                    // 가입 신청 갯수가 한개 미만 이라면 컴포넌트 invisible해야 한다.
                    binding.txNum.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Call<JoinData> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.btn_accept:
                // 가입 신청 관리
                startActivity(new Intent(CrewSettingActivity.this, JoinManageActivity.class).putExtra("crew_id", crew_id));
                // 가입 신청 관리 액티비티로 이동할 때 해당 크루의 id를 보낸다.
                break;

            case R.id.btn_change:
                startActivity(new Intent(CrewSettingActivity.this, ChangeLeaderActivity.class).putExtra("crew_id", crew_id));
                // 리더 위임하기
                break;

            case R.id.btn_delete:
                // 크루 삭제하는 버튼
                // 크루 삭제시 복구할 수 없다는 다이얼로그로 띄워주기
                //ShowDeleteDialog(crew_id);
                break;

            case R.id.btn_update:
                // 크루 기본 정보 수정하는 버튼 클릭
                // 수정 액티비티로 이동 .
                startActivity(new Intent(CrewSettingActivity.this, UpdateCrewActivity.class).putExtra("crew_id", crew_id));
                break;

            case R.id.btn_out:
                // 크루 탈퇴하기 버튼
                ShowDialog();
                break;

        }

    }

    // 크루 탈퇴할때 알림 다이얼로그
    private void ShowDialog() {
        AlertDialog.Builder outdialog = new AlertDialog.Builder(CrewSettingActivity.this);
        outdialog.setTitle("크루명 : " + title);
        outdialog.setMessage("크루를 탈퇴하시겠습니까?\n탈퇴 후 크루에 게시한 게시글, 댓글, 좋아요등을 수정하거나 삭제 할 수 없습니다. ");
        outdialog.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick: 탈퇴 no");
            }
        });
        outdialog.setNegativeButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick: 탈퇴 yes");
                OutCrew(user_email, crew_id);
            }
        });
        outdialog.show();
    }

    // 크루 탈퇴하기
    private void OutCrew(String user_email, int crew_id) {
        retrofitInterface.OutCrew(user_email, crew_id).enqueue(new Callback<CrewData>() {
            @Override
            public void onResponse(Call<CrewData> call, Response<CrewData> response) {

                CrewData result = response.body();
                Log.d(TAG, "OutCrew onResponse: result = "+ result);
                String status = result.getStatus();
                if(status.equals("true")){
                    // 크루 탈퇴 완
                    // 탈퇴했다는 toast 메세지와 함께 내 크루 리스트를 볼 수 있는 community activity로 이동해야겠다.
                    Toast.makeText(context, title+"\n크루에서 탈퇴했습니다.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CrewSettingActivity.this, CommunityActivity.class));
                    finish();
                }else{
                    // 크루 탈퇴 못함
                    Log.d(TAG, "OutCrew onResponse: status is false");
                }
            }

            @Override
            public void onFailure(Call<CrewData> call, Throwable t) {

            }
        });
    }


}