package com.example.everyrun.Community;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.example.everyrun.Community.Adapter.MyCrewAdapter;
import com.example.everyrun.Community.Adapter.MyCrewDetailAdapter;
import com.example.everyrun.R;
import com.example.everyrun.Retrofit.RetrofitClient;
import com.example.everyrun.Retrofit.RetrofitInterface;
import com.example.everyrun.RetrofitData.CrewData;
import com.example.everyrun.UserInfo.UserSharedPreference;
import com.example.everyrun.databinding.ActivityCommunityBinding;
import com.example.everyrun.databinding.ActivityTotalMyCrewBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TotalMyCrewActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName(); // log
    private ActivityTotalMyCrewBinding binding;
    Context context;

    private RetrofitClient retrofitClient;
    private RetrofitInterface retrofitInterface;
    private UserSharedPreference preferenceHelper;

    //-------------------------------내 크루 리사이클러뷰------------------------------
    RecyclerView rv_mycrew= null;
    // 댓글 세팅해줄 리사이클러뷰
    MyCrewDetailAdapter myCrewAdapter = null;
    // 댓글 리사이클러뷰 어댑터
    ArrayList<CrewData> crewDataArrayList;
    // 댓글 데이터 담을 arraylist
    //-------------------------------내 크루 리사이클러뷰------------------------------

    String user_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTotalMyCrewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = getApplicationContext();
        preferenceHelper = new UserSharedPreference(this);
        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();

        //----------------------------------------------내크루 리사이클러뷰------------------------------
        rv_mycrew = findViewById(R.id.rv_mycrew);
        crewDataArrayList = new ArrayList<>();
        myCrewAdapter = new MyCrewDetailAdapter(crewDataArrayList, getApplicationContext());
        rv_mycrew.setAdapter(myCrewAdapter);
        rv_mycrew.setLayoutManager(new GridLayoutManager(this,2));
        //-------------------------------------------------내크루 리사이클러뷰------------------------------

        setUI();
    }

    private void setUI() {

        user_email = preferenceHelper.getEmail();


        Log.d(TAG, "setUI: email = " + user_email);

        // 내 크루 리스트 - 리사이클러뷰
        retrofitInterface.MyCrewList(user_email).enqueue(new Callback<List<CrewData>>() {
            @Override
            public void onResponse(Call<List<CrewData>> call, Response<List<CrewData>> response) {
                List<CrewData> result = response.body();
                Log.d(TAG, "onResponse: result = "+ result);

                for(int i =0; i<result.size(); i++){
                    crewDataArrayList.add(result.get(i));
                }

                myCrewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<CrewData>> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });

    }
}