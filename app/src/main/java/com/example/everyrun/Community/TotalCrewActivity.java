package com.example.everyrun.Community;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.everyrun.Community.Adapter.TotalCrewAdapter;
import com.example.everyrun.Community.Adapter.TotalCrewDetailAdapter;
import com.example.everyrun.R;
import com.example.everyrun.Retrofit.RetrofitClient;
import com.example.everyrun.Retrofit.RetrofitInterface;
import com.example.everyrun.RetrofitData.CrewData;
import com.example.everyrun.UserInfo.UserSharedPreference;
import com.example.everyrun.UserProfile.BottomSheetDialog;
import com.example.everyrun.databinding.ActivityCommunityBinding;
import com.example.everyrun.databinding.ActivityTotalCrewBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TotalCrewActivity extends AppCompatActivity  implements BottomSheetDialog.BottomSheetListener, View.OnClickListener{

    private final String TAG = this.getClass().getSimpleName(); // log
    private ActivityTotalCrewBinding binding;
    Context context;

    private RetrofitClient retrofitClient;
    private RetrofitInterface retrofitInterface;
    private UserSharedPreference preferenceHelper;

    //-------------------------------모든 크루 리사이클러뷰----------------------------

    RecyclerView rv_totalcrew= null;
    // 모든크루 세팅해줄 리사이클러뷰
    TotalCrewDetailAdapter totalCrewDetailAdapter = null;
    // 모든크루 리사이클러뷰 어댑터
    ArrayList<CrewData> totalcrewDataArrayList;
    // 모든크루 데이터 담을 arraylist
    //-------------------------------모든 크루 리사이클러뷰----------------------------

    String user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTotalCrewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = getApplicationContext();
        preferenceHelper = new UserSharedPreference(this);
        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();

        //-------------------------------------------------내크루 리사이클러뷰------------------------------
        rv_totalcrew = findViewById(R.id.rv_totalcrew);
        totalcrewDataArrayList = new ArrayList<>();
        totalCrewDetailAdapter = new TotalCrewDetailAdapter(totalcrewDataArrayList, getApplicationContext());
        rv_totalcrew.setAdapter(totalCrewDetailAdapter);
        rv_totalcrew.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        //-------------------------------------------------내크루 리사이클러뷰------------------------------

        setUI();

        binding.btnFilter.setOnClickListener(this);

    }

    private void setUI() {
        user_email = preferenceHelper.getEmail();


        Log.d(TAG, "setUI: email = " + user_email);

        // 크루 전체 보기 - 리사이클러뷰
        retrofitInterface.TotalCrewList(user_email).enqueue(new Callback<List<CrewData>>() {
            @Override
            public void onResponse(Call<List<CrewData>> call, Response<List<CrewData>> response) {
                List<CrewData> result = response.body();
                Log.d(TAG, "onResponse: result = "+ result);

                // 갯수 제한 두기 6개로 할까 4개로 할까....고민..일단 item 부터 만들기

                for(int i =0; i<result.size(); i++){
                    totalcrewDataArrayList.add(result.get(i));
                }

                totalCrewDetailAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<CrewData>> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }

    // 컴포넌트 클릭 리스너
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_filter:
                // bottom sheet dialog 객체 선언해주고 보여줌.
                FilterBottomSheetDialog bottomSheetDialog = new FilterBottomSheetDialog();
                bottomSheetDialog.show(getSupportFragmentManager(), "bottomsheetdialog");
                Log.d(TAG, "onClick: 바텀 시트 보여짐");
                break;
        }

    }

    // 바텀 시트 클릭 리스너
    @Override
    public void onButtonClicked(int option) {

        switch (option){
            case 1:
                Log.d(TAG, "onButtonClicked: 1 선택");
                binding.txFilter.setText("크루원순");
                break;

            case 2:
                Log.d(TAG, "onButtonClicked: 2 선택");
                binding.txFilter.setText("월간 달린 거리순");
                break;

            case 3:
                Log.d(TAG, "onButtonClicked: 3 선택");
                binding.txFilter.setText("전체 달린 거리순");
                break;

            case 4:
                Log.d(TAG, "onButtonClicked: 4 선택");
                break;
        }

    }
}