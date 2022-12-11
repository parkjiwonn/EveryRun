package com.example.everyrun.Community.CrewSetting;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.everyrun.Community.Adapter.LeaderCandidateAdapter;
import com.example.everyrun.Community.Adapter.MemberListAdapter;
import com.example.everyrun.Community.Crew_CRUD.ReadCrewActivity;
import com.example.everyrun.R;
import com.example.everyrun.Retrofit.RetrofitClient;
import com.example.everyrun.Retrofit.RetrofitInterface;
import com.example.everyrun.RetrofitData.UserInfoData;
import com.example.everyrun.UserInfo.UserSharedPreference;
import com.example.everyrun.databinding.ActivityChangeLeaderBinding;
import com.example.everyrun.databinding.ActivityCrewSettingBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeLeaderActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName(); // log

    private ActivityChangeLeaderBinding binding;
    Context context;

    private UserSharedPreference preferenceHelper;
    private RetrofitClient retrofitClient;
    private RetrofitInterface retrofitInterface;

    // 인텐트로 받는 변수들
    int crew_id;

    // 현재 로그인한 유저 이메일
    String now_user_email;

    List<UserInfoData> result;
    ArrayList<UserInfoData> searchresult;
    ArrayList<UserInfoData> resetlist;

    //===================멤버 리스트 리사이클러뷰=======================
    RecyclerView rv_candidatelist = null;
    LeaderCandidateAdapter leaderCandidateAdapter = null;
    ArrayList<UserInfoData> userInfoDataArrayList;
    //==============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangeLeaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();

        preferenceHelper = new UserSharedPreference(this);
        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();

        crew_id = getIntent().getIntExtra("crew_id", 0);
        searchresult = new ArrayList<>();
        resetlist = new ArrayList<>();

        //============멤버리스트 리사이클러뷰===========================
        rv_candidatelist = findViewById(R.id.rv_candidatelist);
        userInfoDataArrayList = new ArrayList<>();
        leaderCandidateAdapter = new LeaderCandidateAdapter(userInfoDataArrayList, getApplicationContext());
        rv_candidatelist.setAdapter(leaderCandidateAdapter);
        rv_candidatelist.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        //==========================================================

        setUI();


    }

    private void setUI() {

        now_user_email = preferenceHelper.getEmail();

        // 크루원 리스트를 모두 불러와야 한다.
        retrofitInterface.CandidateList(crew_id).enqueue(new Callback<List<UserInfoData>>() {
            @Override
            public void onResponse(Call<List<UserInfoData>> call, Response<List<UserInfoData>> response) {
                result = response.body();
                Log.d(TAG, "candidatelist onResponse: result =" + result);

                    // 서버에서 불러온 데이터들 모두 담아주기
                    for (int i = 0; i < result.size(); i++) {
                        userInfoDataArrayList.add(result.get(i));
                        resetlist.add(result.get(i));
                    }
                    // 어댑터에 해당 데이터 리스트 세팅해주기
                    leaderCandidateAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(Call<List<UserInfoData>> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });

        leaderCandidateAdapter.setOnItemClickListener(new LeaderCandidateAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                Log.d(TAG, "onItemClick: 아이템 클릭");
                String name = userInfoDataArrayList.get(pos).getUser_name();
                String user_email = userInfoDataArrayList.get(pos).getUser_email();

                // 체크 됐으니까 체크 아이콘 활성화 시켜주기
                userInfoDataArrayList.get(pos).setMessage("true");
                leaderCandidateAdapter.notifyDataSetChanged();

                // 선택한 크루원에게 리더를 위임할 건지 물어보는 다이얼로그 띄워주기
                AlertDialog.Builder askdialog = new AlertDialog.Builder(ChangeLeaderActivity.this);
                askdialog.setTitle("알림");
                askdialog.setMessage("선택한 " +name+" 크루원에게 리더를 위임하시겠습니까?" )
                        .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 아니오 하면 체크 아이콘 true로 바꿔주기
                                userInfoDataArrayList.get(pos).setMessage("false");
                                leaderCandidateAdapter.notifyDataSetChanged();
                            }
                        })
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 리더 위임하겠다는 뜻.
                                ChangeLeader(crew_id, user_email, now_user_email);
                            }
                        })
                        .setCancelable(false); //외부 클릭시 창닫기 금지
                AlertDialog alertDialog = askdialog.create();
                alertDialog.show();

            }
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit: query = " + query);
                Log.d(TAG, "onQueryTextChange: result = " + result);

                for(int i=0; i<result.size() ; i++)
                {
                    // for문을 돌면서 result 내용을 훑는다.
                    if(resetlist.get(i).getUser_name().contains(query)){
                        Log.d(TAG, "onQueryTextSubmit: 찾음");
                        // 찾은 걸 어디에 담자
                        searchresult.add(result.get(i));
                        Log.d(TAG, "onQueryTextSubmit: searchresult = "+searchresult);
                    }else{
                        Log.d(TAG, "onQueryTextSubmit: 못찾음");
                    }

                    Log.d(TAG, "onQueryTextSubmit: i =" + i);
                }

                // 리사이클러뷰 원래 있던 정보들 다 삭제하고 새로 넣어야 하나?
                userInfoDataArrayList.clear();
                leaderCandidateAdapter = new LeaderCandidateAdapter(searchresult, getApplicationContext());
                rv_candidatelist.setAdapter(leaderCandidateAdapter);
                leaderCandidateAdapter.notifyDataSetChanged();


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: newText = " + newText);

                if(newText.length() == 0)
                {
                    Log.d(TAG, "onQueryTextChange: 서치뷰 공백임");
                    searchresult.clear();
                    leaderCandidateAdapter = new LeaderCandidateAdapter(resetlist, getApplicationContext());
                    rv_candidatelist.setAdapter(leaderCandidateAdapter);
                    leaderCandidateAdapter.notifyDataSetChanged();



                }else{
                    Log.d(TAG, "onQueryTextChange: 서치뷰 공백 아님");
                }

                return false;
            }
        });
        

    }

    // 리더 위임 하기 메서드
    private void ChangeLeader(int crew_id, String user_email, String now_user_email) {




        retrofitInterface.ChangeLeader(crew_id, user_email, now_user_email).enqueue(new Callback<UserInfoData>() {
            @Override
            public void onResponse(Call<UserInfoData> call, Response<UserInfoData> response) {
                UserInfoData result = response.body();
                Log.d(TAG, "ChangeLeader onResponse: result = " + result);

                String status = result.getStatus();
                String from = "change";
                if(status.equals("true")){
                    // 리더가 바뀌었음
                    // 리더 위임이 완료되었다는 다이얼로그로 알려준다음
                    // 크루 상세보기 페이지로 이동 시키기
                    startActivity(new Intent(ChangeLeaderActivity.this, ReadCrewActivity.class).putExtra("crew_id", crew_id)
                    .putExtra("from", from));
                    finish();

                }else{
                    Log.d(TAG, "ChangeLeader onResponse: status is false");
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
        switch (view.getId()) {

        }

    }
}