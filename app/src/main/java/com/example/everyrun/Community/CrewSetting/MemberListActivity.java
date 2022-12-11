package com.example.everyrun.Community.CrewSetting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.example.everyrun.Community.Adapter.LeaderCandidateAdapter;
import com.example.everyrun.Community.Adapter.MemberListAdapter;
import com.example.everyrun.R;
import com.example.everyrun.Retrofit.RetrofitClient;
import com.example.everyrun.Retrofit.RetrofitInterface;
import com.example.everyrun.RetrofitData.UserInfoData;
import com.example.everyrun.UserInfo.UserSharedPreference;
import com.example.everyrun.databinding.ActivityMemberListBinding;
import com.example.everyrun.databinding.ActivityReadCrewBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MemberListActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName(); // log

    private ActivityMemberListBinding binding;
    Context context;
    String user_email; // 현재 로그인한 유저 이메일
    int crew_id; // 게시글 고유 id
    private UserSharedPreference preferenceHelper;
    private RetrofitClient retrofitClient;
    private RetrofitInterface retrofitInterface;

    //===================멤버 리스트 리사이클러뷰=======================
    RecyclerView rv_memberlist = null;
    MemberListAdapter memberListAdapter = null;
    ArrayList<UserInfoData> userInfoDataArrayList;
    //==============================================================

    ArrayList<UserInfoData> resetlist;
    ArrayList<UserInfoData> searchresult;
    List<UserInfoData> result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();
        preferenceHelper = new UserSharedPreference(this);
        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();

        // intent로 넘어온 커뮤니티 id로 커뮤니티 정보 세팅해주기
        crew_id = getIntent().getIntExtra("crew_id", 0);
        Log.d(TAG, "onCreate: crew_id=" + crew_id);

        resetlist = new ArrayList<>();
        searchresult = new ArrayList<>();

        //============멤버리스트 리사이클러뷰===========================
        rv_memberlist = findViewById(R.id.rv_memberlist);
        userInfoDataArrayList = new ArrayList<>();
        memberListAdapter = new MemberListAdapter(userInfoDataArrayList, getApplicationContext());
        rv_memberlist.setAdapter(memberListAdapter);
        rv_memberlist.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        //==========================================================

        setUI();

    }

    private void setUI() {
        user_email = preferenceHelper.getEmail();
        // 유저 정보 리스트 불러와야 함.
        retrofitInterface.MemberList(crew_id).enqueue(new Callback<List<UserInfoData>>() {
            @Override
            public void onResponse(Call<List<UserInfoData>> call, Response<List<UserInfoData>> response) {
                result = response.body();
                Log.d(TAG, "onResponse: result=" + result);

                for(int i=0; i<result.size(); i++)
                {
                    // 받아온 데이터를 보다가 message(리더) == user_email 이면 제일 위에 세팅해주기
                    if(result.get(i).getMessage().equals(result.get(i).getUser_email())) {
                        userInfoDataArrayList.add(0, result.get(i));

                    }else{
                        userInfoDataArrayList.add(result.get(i));
                    }
                    resetlist.add(result.get(i));
                }
                memberListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<UserInfoData>> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());

            }
        });

        // 멤버 검색 서치뷰
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
                memberListAdapter = new MemberListAdapter(searchresult, getApplicationContext());
                rv_memberlist.setAdapter(memberListAdapter);
                memberListAdapter.notifyDataSetChanged();


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: newText = " + newText);

                if(newText.length() == 0)
                {
                    Log.d(TAG, "onQueryTextChange: 서치뷰 공백임");
                    searchresult.clear();
                    memberListAdapter = new MemberListAdapter(resetlist, getApplicationContext());
                    rv_memberlist.setAdapter(memberListAdapter);
                    memberListAdapter.notifyDataSetChanged();

                }else{
                    Log.d(TAG, "onQueryTextChange: 서치뷰 공백 아님");
                }

                return false;
            }
        });


    }
}