package com.example.everyrun.Community;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.everyrun.Challenge.ChallengeActivity;
import com.example.everyrun.Community.Adapter.MyCrewAdapter;
import com.example.everyrun.Community.Adapter.TotalCrewAdapter;
import com.example.everyrun.Community.Adapter.TotalCrewListAdapter;
import com.example.everyrun.Community.Crew_CRUD.CreateCrewActivity;
import com.example.everyrun.MainActivity;
import com.example.everyrun.R;
import com.example.everyrun.Record.RecordActivity;
import com.example.everyrun.Retrofit.RetrofitClient;
import com.example.everyrun.Retrofit.RetrofitInterface;
import com.example.everyrun.RetrofitData.CrewData;
import com.example.everyrun.RetrofitData.UserInfoData;
import com.example.everyrun.UserInfo.UserSharedPreference;
import com.example.everyrun.UserProfile.SettingActivity;
import com.example.everyrun.UserProfile.UserProfileActivity;
import com.example.everyrun.databinding.ActivityCommunityBinding;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = this.getClass().getSimpleName(); // log
    private ActivityCommunityBinding binding;
    Context context;
    TextView tx_nick;
    ImageView headerprofile;
    private RetrofitClient retrofitClient;
    private RetrofitInterface retrofitInterface;
    private UserSharedPreference preferenceHelper;

    //-------------------------------내 크루 리사이클러뷰------------------------------
    RecyclerView  rv_mycrew= null;
    // 내크루 세팅해줄 리사이클러뷰
    MyCrewAdapter myCrewAdapter = null;
    // 내크루 리사이클러뷰 어댑터
    ArrayList<CrewData> crewDataArrayList;
    // 내크루 데이터 담을 arraylist
    //-------------------------------내 크루 리사이클러뷰------------------------------

    //-------------------------------모든 크루 리사이클러뷰----------------------------

    RecyclerView  rv_totalcrew= null;
    // 모든크루 세팅해줄 리사이클러뷰
    TotalCrewAdapter totalCrewAdapter = null;
    // 모든크루 리사이클러뷰 어댑터
    ArrayList<CrewData> totalcrewDataArrayList;
    // 모든크루 데이터 담을 arraylist
    //-------------------------------모든 크루 리사이클러뷰----------------------------

    ListView listView = null;
    TotalCrewListAdapter totalCrewListAdapter = null;

    String user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommunityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = getApplicationContext();
        preferenceHelper = new UserSharedPreference(this);
        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();

        //----------------------------------------------내크루 리사이클러뷰------------------------------
        rv_mycrew = findViewById(R.id.rv_mycrew);
        crewDataArrayList = new ArrayList<>();
        myCrewAdapter = new MyCrewAdapter(crewDataArrayList, getApplicationContext());
        rv_mycrew.setAdapter(myCrewAdapter);
        rv_mycrew.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        //-------------------------------------------------내크루 리사이클러뷰------------------------------

        //-------------------------------------------------전체 크루 리사이클러뷰------------------------------
        rv_totalcrew = findViewById(R.id.rv_totalcrew);
        totalcrewDataArrayList = new ArrayList<>();
        totalCrewAdapter = new TotalCrewAdapter(totalcrewDataArrayList, getApplicationContext());
        rv_totalcrew.setAdapter(totalCrewAdapter);
        rv_totalcrew.setLayoutManager(new GridLayoutManager(this,2));
        //-------------------------------------------------전체 크루 리사이클러뷰------------------------------

//        listView = findViewById(R.id.list)


        RecyclerView.ItemAnimator mycrew_animator = rv_mycrew.getItemAnimator();
        if (mycrew_animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) mycrew_animator).setSupportsChangeAnimations(false);
        }

        RecyclerView.ItemAnimator totalcrew_animator = rv_totalcrew.getItemAnimator();
        if (totalcrew_animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) totalcrew_animator).setSupportsChangeAnimations(false);
        }


        setUI();

        binding.btnCreate.setOnClickListener(this); // 크루 만들기 버튼 클릭 리스너
        binding.btnTotalmycrew.setOnClickListener(this); // 내 크루 전체보기 버튼 클릭 리스너
        binding.btnTotalcrew.setOnClickListener(this); // 전체 크루 보기 버튼 클릭 리스너
    }

    private void setUI() {
        user_email = preferenceHelper.getEmail();


        Log.d(TAG, "setUI: email = " + user_email);

        // inflateheaderview 사용해서 코드상에서 네비게이션 뷰에 헤더 추가하기
        View headerview = binding.navigationView.inflateHeaderView(R.layout.header_layout);
        tx_nick = headerview.findViewById(R.id.tx_nick); // 헤더뷰에서 유저 닉네임 선언해주기
        headerprofile = headerview.findViewById(R.id.img_profile); // 헤더뷰에 프로필 이미지 선언해주기

        // 내 크루 리스트 - 리사이클러뷰
        retrofitInterface.MyCrewList(user_email).enqueue(new Callback<List<CrewData>>() {
            @Override
            public void onResponse(Call<List<CrewData>> call, Response<List<CrewData>> response) {
                List<CrewData> result = response.body();
                Log.d(TAG, "onResponse: result = "+ result);

                // 내 크루가 하나라도 있다면 리사이클러뷰에 세팅해야하는데
                // 내 크루가 하나도 없다면 안내문 띄워줘야 한다.
                if(result.size() > 0)
                {
                    for(int i =0; i<result.size(); i++){
                        crewDataArrayList.add(result.get(i));
                    }
                    myCrewAdapter.notifyDataSetChanged();
                }else{
                    binding.txInfo.setVisibility(View.VISIBLE);
                    binding.linearBtn.setVisibility(View.INVISIBLE);
                }


            }

            @Override
            public void onFailure(Call<List<CrewData>> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });

        // 크루 전체 보기 - 리사이클러뷰
        retrofitInterface.RandomTotalCrewList(user_email).enqueue(new Callback<List<CrewData>>() {
            @Override
            public void onResponse(Call<List<CrewData>> call, Response<List<CrewData>> response) {
                List<CrewData> result = response.body();
                Log.d(TAG, "onResponse: result = "+ result);

                for(int i =0; i<result.size(); i++){
                    totalcrewDataArrayList.add(result.get(i));
                }

                totalCrewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<CrewData>> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });


        // 사이드 메뉴 세팅
        retrofitInterface.SetUserInfo(user_email).enqueue(new Callback<UserInfoData>() {
            @Override
            public void onResponse(Call<UserInfoData> call, Response<UserInfoData> response) {
                Log.d(TAG, "setUI: 2");
                UserInfoData result = response.body();
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "setUI: 3");
                    Log.d(TAG, "onResponse: result=" + result);
                    String status = result.getStatus();
                    Log.d(TAG, "setUI: 4");
                    Log.d(TAG, "onResponse: result=" + result);

                    if (status.equals("true")) {
                        // header에서 사용자 프로필 사진 세팅하기
                        if (result.getUser_photo().equals("basic")) {
                            // 기본 이미지로 세팅해주기
                            headerprofile.setImageResource(R.drawable.user_img);

                        } else {
                            // 사용자가 설정한 이미지 UserProfileImg
                            String url = "http://3.36.174.137/UserProfileImg/" + result.getUser_photo();
                            // glide로 이미지 세팅해주기
                            Glide.with(CommunityActivity.this).load(url).into(headerprofile);
                        }

                        // header에서 사용자 닉네임 세팅하기
                        tx_nick.setText(result.getUser_name());
                    }
                } else {
                    Log.e(TAG, "onResponse: 응답 실패");
                }

            }

            @Override
            public void onFailure(Call<UserInfoData> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });


        headerprofile.setOnClickListener(new View.OnClickListener() { // 프로필 이미지 클릭 리스너
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: 이미지 뷰 선택");
                // 헤더에서 유저 이미지 선택하면 유저 프로필로 이동시키기
                startActivity(new Intent(CommunityActivity.this, UserProfileActivity.class));
                finish();

            }
        });


        //==========================drawer layout 설정========================================
        setSupportActionBar(binding.toolbar); // 툴바 세팅 먼저 해야함.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 왼쪽 상단 버튼 만들기
        getSupportActionBar().setDisplayShowTitleEnabled(false); // 툴바 타이틀 비활성화 만들기
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);
        //==========================drawer layout 설정========================================

        // navigation view 헤더 뷰에 접근하기


        // navigation view 버튼 선택 리스너
        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    // 홈 화면으로 이동
                    case R.id.nav_home:
                        item.setChecked(true);
                        binding.drawerLayout.closeDrawers();
                        startActivity(new Intent(CommunityActivity.this, MainActivity.class));
                        return true;

                    // 운동 기록 화면으로 이동
                    case R.id.nav_record:
                        item.setChecked(true);
                        binding.drawerLayout.closeDrawers();
                        startActivity(new Intent(CommunityActivity.this, RecordActivity.class));
                        return true;

                    // 커뮤니티로 이동
                    case R.id.nav_community:
                        item.setChecked(true);
                        binding.drawerLayout.closeDrawers();
                        startActivity(new Intent(CommunityActivity.this, CommunityActivity.class));
                        return true;
//
//                    // 커뮤니티 화면으로 이동
//                    case R.id.nav_callenge:
//                        item.setChecked(true);
//                        binding.drawerLayout.closeDrawers();
//                        startActivity(new Intent(CommunityActivity.this, ChallengeActivity.class));
//                        return true;

                    // 설정 화면으로 이동
                    case R.id.nav_setting:
                        item.setChecked(true);
                        binding.drawerLayout.closeDrawers();
                        startActivity(new Intent(CommunityActivity.this, SettingActivity.class));
                        return true;


                }

                return false;
            }
        });

    }

    // 툴바의 메뉴 버튼 클릭시 - navigation view 나오는 메서드
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { // 왼쪽 상단 버튼 눌렀을 때
                binding.drawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_create:
                // 크루 만들기 액티비티로 이동하기.
                startActivity(new Intent(CommunityActivity.this, CreateCrewActivity.class));

                break;

            case R.id.btn_totalmycrew:
                // 내 크루 전체보기 액티비티로 이동하기.
                startActivity(new Intent(CommunityActivity.this, TotalMyCrewActivity.class));

                break;

            case R.id.btn_totalcrew:
                startActivity(new Intent(CommunityActivity.this, TotalCrewActivity.class));
                break;
        }
    }
}