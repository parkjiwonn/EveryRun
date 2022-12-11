package com.example.everyrun.Community.CrewSetting;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.everyrun.Community.Adapter.JoinListAdapter;
import com.example.everyrun.R;
import com.example.everyrun.Retrofit.RetrofitClient;
import com.example.everyrun.Retrofit.RetrofitInterface;
import com.example.everyrun.RetrofitData.CrewData;
import com.example.everyrun.RetrofitData.JoinData;
import com.example.everyrun.UserInfo.UserSharedPreference;
import com.example.everyrun.databinding.ActivityJoinManageBinding;
import com.example.everyrun.databinding.ActivityReadCrewBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinManageActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName(); // log

    private ActivityJoinManageBinding binding;
    Context context;
    String user_email;
    int crew_id; // 게시글 고유 id
    private UserSharedPreference preferenceHelper;
    private RetrofitClient retrofitClient;
    private RetrofitInterface retrofitInterface;

    //=======크루 가입 수락시 사용되는 변수들========
    String member_email;
    int wait_num; // 가입 신청 수락 대기자들
    //=========================================

    //====================가입한 멤버리스트 리사이클러뷰================
    RecyclerView rv_joinlist = null;
    JoinListAdapter joinListAdapter = null;
    ArrayList<JoinData> joinDataArrayList;
    //============================================================

    int join_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJoinManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();
        preferenceHelper = new UserSharedPreference(this);
        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();

        // intent로 넘어온 커뮤니티 id로 커뮤니티 정보 세팅해주기
        crew_id = getIntent().getIntExtra("crew_id", 0);
        Log.d(TAG, "onCreate: crew_id=" + crew_id);

        //=======================join list 리사이클러뷰====================
        rv_joinlist = findViewById(R.id.rv_joinlist);
        joinDataArrayList = new ArrayList<>();
        joinListAdapter = new JoinListAdapter(joinDataArrayList, getApplicationContext());
        rv_joinlist.setAdapter(joinListAdapter);
        rv_joinlist.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        //==============================================================

        setUI();
    }

    private void setUI() {
        user_email = preferenceHelper.getEmail(); // 현재 로그인한 유저 이메일

        retrofitInterface.JoinList(crew_id).enqueue(new Callback<List<JoinData>>() {
            @Override
            public void onResponse(Call<List<JoinData>> call, Response<List<JoinData>> response) {
                List<JoinData> result = response.body();
                Log.d(TAG, "onResponse: result =" + result);

                // 가입 신청한 리스트가 없다면 가입신청한 회원이 없다는 것을 알려줘야 함.
                if(result.size() > 0)
                {
                    // 가입 신청 대기자가 몇명이고 리사이클러뷰 띄워줘야 함.
                    binding.noneLayout.setVisibility(View.GONE);
                    binding.txInfo.setVisibility(View.VISIBLE);
                    binding.rvJoinlist.setVisibility(View.VISIBLE);

                    wait_num = result.size();
                    Log.d(TAG, "onResponse: 대기자 수 = "+ wait_num);
                    // 가입 신청 대기자 몇명인지 세팅해주기
                    binding.txInfo.setText("가입 신청 대기자 "+ wait_num +"명");

                    for (int i = 0; i < result.size(); i++) {
                        joinDataArrayList.add(result.get(i));
                    }

                    joinListAdapter.notifyDataSetChanged();
                }else{
                    // 가입 신청이 없다면
                    binding.noneLayout.setVisibility(View.VISIBLE);
                    binding.txInfo.setText("가입 신청 대기자 " + wait_num +"명");
                    binding.rvJoinlist.setVisibility(View.GONE);
                }


            }

            @Override
            public void onFailure(Call<List<JoinData>> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });

        // 리사이클러뷰 클릭 리스너
        joinListAdapter.setOnItemClickListener(new JoinListAdapter.OnItemClickListener() {
            @Override
            public void onAcceptClick(View v, int pos) {
                // 가입 신청 수락 클릭 리스너
                Log.d(TAG, "onAcceptClick: 가입 수락");
                // 가입 수락을 했을 때 가입 신청한 유저가 가입 취소를 했다면?
                // 네이버 카페의 경우 수락을 눌렀을때 아무런 데이터가 나오지 않는다.
                // 런데이의 경우 수락을 누렀을때 리스트에서 사라진다.

                // 해당 유저가 가입신청을 취소했는지 안했는지 확인해야 한다.
                Log.d(TAG, "onAcceptClick: join_id=" + joinDataArrayList.get(pos).getJoin_id());
                join_id = joinDataArrayList.get(pos).getJoin_id();
                CheckCancel(join_id, pos, 0);

            }

            @Override
            public void onDenyClick(View v, int pos) {
                // 가입 신청 거절 클릭 리스너
                Log.d(TAG, "onAcceptClick: 가입 거절");
                // 가입 거절을 했을 때 가입 신청한 유저가 가입 취소를 했다면?
                join_id = joinDataArrayList.get(pos).getJoin_id();
                CheckCancel(join_id, pos, 1);
            }
        });

    }

    // 가입 신청 수락했을 때 해당 신청이 취소됐는지 안됐는지 확인
    private void CheckCancel(int join_id, int pos, int option) {
        Log.d(TAG, "CheckCancel: join_id ="+join_id);
        retrofitInterface.CheckCancel(join_id).enqueue(new Callback<JoinData>() {
            @Override
            public void onResponse(Call<JoinData> call, Response<JoinData> response) {
                JoinData result = response.body();
                Log.d(TAG, "checkcancel onResponse: result = " + result);
                String status = result.getStatus();
                if (status.equals("true")) {
                    // 가입 취소했다는 뜻
                    // 가입 취소한 유저라는 것을 알려줘야 한다.
                    ShowDialog(0, pos);

                } else {
                    member_email = joinDataArrayList.get(pos).getUser_email();
                    // 가입 취소하지 않았다는 뜻
                    // 가입 수락인지 거절인지 판단하기
                    if (option == 0) {
                        // 가입 수락하기 선택함
                        // 가입 수락 절차 밟기
                        // 크루 멤버가 되기 위해 필요한 데이터들
                        // (1) 멤버 이메일 (2) 멤버 역할 (3) 크루 id
                        Log.d(TAG, "onResponse: option = 0이고 가입 수락 시작");

                        AcceptJoin(member_email, crew_id, pos);
                    } else {
                        // 가입 거절하기 선택함
                        Log.d(TAG, "onResponse: option = 1이고 가입 거절 시작");
                        DenyJoin(pos);
                    }
                }
            }

            @Override
            public void onFailure(Call<JoinData> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });

    }

    // 가입 거절하기 메서드
    private void DenyJoin(int pos) {
        Log.d(TAG, "DenyJoin: join_id = "+ joinDataArrayList.get(pos).getJoin_id());
        join_id = joinDataArrayList.get(pos).getJoin_id();
        retrofitInterface.DenyJoin(join_id).enqueue(new Callback<JoinData>() {
            @Override
            public void onResponse(Call<JoinData> call, Response<JoinData> response) {
                JoinData result = response.body();
                Log.d(TAG, "onResponse: denyjoin result ="+result);

                String status = result.getStatus();
                if(status.equals("true")){
                    Log.d(TAG, "onResponse: 가입 거절 성공 다이얼로그 띄워줌");
                    ShowDialog(2, pos);
                }else{
                    Log.d(TAG, "DenyJoin onResponse: status is false");
                }
            }

            @Override
            public void onFailure(Call<JoinData> call, Throwable t) {

            }
        });
    }

    // 가입 수락하기 메서드
    private void AcceptJoin(String member_email, int crew_id, int pos) {

        retrofitInterface.AddMember(crew_id, member_email, 1).enqueue(new Callback<CrewData>() {
            @Override
            public void onResponse(Call<CrewData> call, Response<CrewData> response) {
                CrewData result = response.body();
                Log.d(TAG, "acceptjoin onResponse: result =" + result);
                String status = result.getStatus();
                if (status.equals("true")) {
                    // 멤버가 되었다는 뜻
                    ShowDialog(1, pos);
                } else {
                    Log.d(TAG, "acceptjoin onResponse: status is false");
                }
            }

            @Override
            public void onFailure(Call<CrewData> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });

    }

    // 다이얼로그 띄워주기
    private void ShowDialog(int option, int pos) {
        switch (option) {
            case 0:
                // 가입 취소한 유저라는 것을 알려주는 다이얼로그
                AlertDialog.Builder canceldialog = new AlertDialog.Builder(JoinManageActivity.this);
                canceldialog.setMessage("가입 신청이 취소되었습니다.");
                canceldialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 확인 버튼이 눌리면 리스트에서 해당 유저가 사라져야 한다.
                        joinListAdapter.notifyItemRemoved(pos);
                        // 데이터 리스트에도 해당 pos에 해당하는 데이터 삭제해주기
                        joinDataArrayList.remove(pos);

                        binding.noneLayout.setVisibility(View.VISIBLE);
                        binding.rvJoinlist.setVisibility(View.GONE);
                        binding.txInfo.setText("가입 신청 대기자 0명");

                    }
                });
                canceldialog.show();
                break;

            case 1:
                Log.d(TAG, "ShowDialog: 가입 수락");
                // 가입 수락되었다는 것을 알려주는 다이얼로그
                Toast.makeText(getApplicationContext(), "가입 승인이 완료되었습니다.", Toast.LENGTH_LONG).show();
                // 가입 수락이 눌리면 리스트에서 해당 유저가 사라져야 한다.
                joinListAdapter.notifyItemRemoved(pos);
                // 데이터 리스트에도 해당 pos에 해당하는 데이터 삭제해주기
                joinDataArrayList.remove(pos);
                // 가입이 수락되면 대기자에서 한명 줄어들어야 한다.
                --wait_num; // 한명 줄이기
                // 근데 대기자 수가 0명인 경우에는
                if(wait_num == 0)
                {
                    // 아무도 가입 신청을 하지 않았음.
                    binding.noneLayout.setVisibility(View.VISIBLE);
                    binding.rvJoinlist.setVisibility(View.GONE);
                    binding.txInfo.setText("가입 신청 대기자 0명");
                }else{
                    // 대기자 수가 1명이라도 있다면
                    binding.txInfo.setText("가입 신청 대기자 "+ wait_num +"명");
                }


                break;

            case 2:
                Log.d(TAG, "ShowDialog: 가입 거절 ");
                // 가입 거절되었다는 것을 알려주는 다이얼로그
                Toast.makeText(getApplicationContext(), "가입 신청이 거절되었습니다.", Toast.LENGTH_LONG).show();
                // 가입 수락이 눌리면 리스트에서 해당 유저가 사라져야 한다.
                joinListAdapter.notifyItemRemoved(pos);
                // 데이터 리스트에도 해당 pos에 해당하는 데이터 삭제해주기
                joinDataArrayList.remove(pos);
                break;

        }
    }
}