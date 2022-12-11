package com.example.everyrun.Community.Crew_CRUD;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.everyrun.Community.CommunityActivity;
import com.example.everyrun.Community.CrewSetting.CrewSettingActivity;
import com.example.everyrun.Community.CrewSetting.MemberListActivity;
import com.example.everyrun.R;
import com.example.everyrun.Retrofit.RetrofitClient;
import com.example.everyrun.Retrofit.RetrofitInterface;
import com.example.everyrun.RetrofitData.CrewData;
import com.example.everyrun.UserInfo.UserSharedPreference;
import com.example.everyrun.databinding.ActivityReadCrewBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadCrewActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = this.getClass().getSimpleName(); // log

    private ActivityReadCrewBinding binding;
    Context context;
    String user_email;
    int crew_id; // 게시글 고유 id
    private UserSharedPreference preferenceHelper;
    private RetrofitClient retrofitClient;
    private RetrofitInterface retrofitInterface;

    //========crew info=========
    String title;
    String content;
    String banner;
    int area;
    int member;
    int current;
    int total;
    public String reader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadCrewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();
        preferenceHelper = new UserSharedPreference(this);
        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();

        // intent로 넘어온 커뮤니티 id로 커뮤니티 정보 세팅해주기
        // 리더 위임에서 넘어오는지 아닌지 확인해야함.
        if(!TextUtils.isEmpty(getIntent().getStringExtra("from")))
        {
            //리더 위임에서 넘어온 것.
            Log.d(TAG, "onCreate: 리더 위임에서 넘어옴");
            AlertDialog.Builder confirm_dialog = new AlertDialog.Builder(ReadCrewActivity.this);
            confirm_dialog.setTitle("알림")
                    .setMessage("리더 위임이 완료되었습니다.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
            confirm_dialog.show();
        }else{
            // 그냥 상세페이지로 이동된것.
        }
        crew_id = getIntent().getIntExtra("crew_id", 0);
        Log.d(TAG, "onCreate: crew_id=" + crew_id);

        setUI();
        binding.btnSetting.setOnClickListener(this); // 크루 설정 버튼 클릭 리스너
        binding.btnJoin.setOnClickListener(this); // 크루 가입하기 버튼 클리 리스너
        binding.txMember.setOnClickListener(this); // 크루원 목록 확인할 수 있는 클릭리스너
    }

    private void setUI() {

        user_email = preferenceHelper.getEmail();
        // 크루 기본 정보들 세팅해주는 부분
        retrofitInterface.SetCrewInfo(crew_id).enqueue(new Callback<CrewData>() {
            @Override
            public void onResponse(Call<CrewData> call, Response<CrewData> response) {

                CrewData result = response.body();
                Log.d(TAG, "onResponse: result = " + result);

                // 스피너 - 지역 선택
                final String[] spinnerArea = getResources().getStringArray(R.array.area_list);

                String status = result.getStatus();
                title = result.getTitle();
                content = result.getContent();
                area = result.getArea();
                member = result.getMember();
                total = result.getTotal();
                current = result.getCurrent();
                banner = result.getBanner();
                reader = result.getReader();

                if (status.equals("true")) {
                    binding.txTitle.setText(title);
                    binding.txContent.setText(content);
                    binding.txArea.setText(spinnerArea[area]);
                    //binding.txPeople.setText(String.valueOf(current) + " / " + String.valueOf(total));
                    binding.txPeople.setText(String.valueOf(current));

                    // 기본이미지인 경우
                    if (banner.equals("basic")) {
                        binding.crewImg.setImageResource(R.drawable.img1);
                    } else {
                        // 사용자가 설정한 이미지 UserProfileImg
                        String url = "http://3.36.174.137/CrewImg/" + banner;
                        // glide로 이미지 세팅해주기
                        Glide.with(ReadCrewActivity.this).load(url).into(binding.crewImg);
                    }

                    // 가입하기 버튼 세팅
                    // 현 크루의 관리자 인지 아닌지 구분
                    Log.d(TAG, "setUI: reader = "+ reader);
                    Log.d(TAG, "setUI: user_email = "+user_email);
                    if(reader.equals(user_email))
                    {
                        // 관리자니까 버튼 사라져야 한다.
                        binding.btnJoin.setVisibility(View.GONE);

                    }else{
                        // 관리자가 아니다. 그럼 가입했는지 안했는지 구분해야 한다.
                        CheckJoin(user_email, crew_id);
                    }


                } else {
                    Log.d(TAG, "onResponse: set ui response fail");
                }
            }

            @Override
            public void onFailure(Call<CrewData> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });

        // 주간, 월간, 전체 선택할 수 있는 스피너 세팅해주기
        final String[] spinnerDay = getResources().getStringArray(R.array.day_list);

//        ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(this, R.array.day_list, android.R.layout.simple_spinner_item);
//        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        binding.Dayspinner.setAdapter(arrayAdapter);
//
//        binding.Dayspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                Log.d(TAG, "onItemSelected: 운동 기록 기간 선택 = " + spinnerDay[i]);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_setting:

 /*                크루 설정 액티비티로 이동해야 함. -> 이동할 때 크루 id 같이 보내야 함.
                 setting 액티비티로 갔을 때 현재 로그인한 사람이 관리자인지 아닌지 어떻게 구분?
                 crew 정보 받아올때 리더정보까지 같이 받아와서 현재 로그인한 사람이 리더인지 아닌지 구분한 다음에 intent 구분해서 보내기*/

                Log.d(TAG, "onClick: reader =" + reader);
                Log.d(TAG, "onClick: user = " + user_email);
                Log.d(TAG, "onClick: member = "+ member);


             /*    intent 보낼때 현 크루 id 와 리더인지 아닌지 구분할 수 있는 정수 변수 같이 보낸다.
                 (1) reader 가 0일 때 리더가 맞다는 것
                 (2) reader 가 1일 때 리더가 아니라는 것*/

                if (reader.equals(user_email)) {
                    Log.d(TAG, "onClick: 현 유저 관리자임");
                    startActivity(new Intent(ReadCrewActivity.this, CrewSettingActivity.class).putExtra("crew_id", crew_id).putExtra("reader", 0)
                    .putExtra("member",member).putExtra("current", current));
                } else {
                    Log.d(TAG, "onClick: 현 유저 관리자 아님");
                    startActivity(new Intent(ReadCrewActivity.this, CrewSettingActivity.class).putExtra("crew_id", crew_id).putExtra("reader", 1)
                    .putExtra("title", title));
                }


                break;

                /*가입하기
                    (관리자)
                    가입하기 버튼이 gone 상태여야 한다.
                    (참여자)
                    (내가 가입한 크루일 때)
                    가입하기 버튼이 gone 상태여야 한다.
                    (내가 가입하지 않은 크루일 떄)
                    가입하기 버튼이 보여야 한다.

                    (가입 신청)
                    바로 가입가능 크루일때
                    가입하고 	버튼이 gone 상태로 변화해야 한다.
                    리더 승인후 가입 기능 크루일때
                    가입하고 버튼에 가입 진행 중이라고 변화해야 한다.
                * */
            case R.id.btn_join:

                // 현재 이 버튼이 보인다는 것은 해당 크루의 관리자도 아니고 참여자도 아니라는 뜻.
                // 가입하기 버튼을 눌렀을 때 해당 크루가 바로 가입되는 크루인지 아니면 리더 승인 후 가입되는 크루인지 구분해야 한다.
                // 구분하고 가입진행절차를 밟아야 한다.

                // (1) member = 1 바로 가입
                // (2) member = 2 리더 승인 후 가입

                // 하기 전에 현재 유저가 이 크루에 가입신청을 했는지 먼저 확인해야 한다.
                // 가입을 했는지 안했는지
                // 가입 안했다면 checkmember메서드 실행
                // 버튼 text가 가입 승인 대기중이라면 다이얼로그 나오고
                // 아니라면 checkmember로 가기
                String check = (String) binding.btnJoin.getText();
                Log.d(TAG, "onClick:btn check="+ check);
                if(check.equals("가입 승인 대기중"))
                {
                    Log.d(TAG, "btn onClick: 가입 승인 대기중");
                    // 가입 승인 대기중이니까 다이얼로그 띄워줘야 한다.
                    // (1) 다이얼로그 그냥 닫을 건지
                    // (2) 가입 신청 취소할 건지 선택해야 한다.
                    AlertDialog.Builder builder = new AlertDialog.Builder(ReadCrewActivity.this);
                    builder.setMessage("가입 신청이 완료되었습니다.\n리더가 승인하면 크루에 가입됩니다.");
                    builder.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.setNegativeButton("신청 취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            CancelJoin(user_email, crew_id);
                        }
                    });
                    builder.show();

                }else{
                    CheckMember(crew_id);
                }

                break;

            case R.id.tx_member:
                // 클릭시 멤버 리스트로 이동해야 한다.
                startActivity(new Intent(ReadCrewActivity.this, MemberListActivity.class).putExtra("crew_id", crew_id));
                // 인텐트로 크루 고유 id 같이 보내야 한다.
                break;
        }

    }

    // 가입 신청 취소하는 메서드
    private void CancelJoin(String user_email, int crew_id) {

        Log.d(TAG, "CancelJoin: user_email ="+ user_email);
        Log.d(TAG, "CancelJoin: crew_id ="+ crew_id);
        retrofitInterface.CancelJoin(crew_id, user_email).enqueue(new Callback<CrewData>() {
            @Override
            public void onResponse(Call<CrewData> call, Response<CrewData> response) {
                CrewData result = response.body();
                Log.d(TAG, "CancelJoin onResponse: result="+result);
                String status = result.getStatus();
                if(status.equals("true"))
                {
                 // 크루 가입 신청이 취소되었다는 뜻
                    Toast.makeText(context, "가입 신청이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                    // 취소가 되었으니까 버튼 바껴야 한다.
                    binding.btnJoin.setText("가입 하기");
                }
            }

            @Override
            public void onFailure(Call<CrewData> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }

    // 해당 크루가 바로 가입할 수 있는지 리더 승인 후 가입해야하는 크루인지 확인하는 메서드
    private void CheckMember(int crew_id) {

        retrofitInterface.CheckMember(crew_id).enqueue(new Callback<CrewData>() {
            @Override
            public void onResponse(Call<CrewData> call, Response<CrewData> response) {
                CrewData result= response.body();
                Log.d(TAG, "onResponse: checkmember result = " + result);
                int member = result.getMember();
                String status = result.getStatus();
                
                if(status.equals("true"))
                {
                    if(member == 1)
                    {
                        /* 바로 가입할 수 있는 크루인 경우
                         바로 가입됐다는 다이얼로그와 함께 가입하기 버튼이 사라져야 한다.
                         그리고 크루원이 한명 증가해야 한다. 
                         크루원 목록에서 새로 추가된 멤버 리스트를 볼 수 있어야 한다.*/
                        AddMember(crew_id, user_email);


                    }else{
                        /*리더 승인 후 가입할 수 있는 크루인 경우
                        * 가입 신청이 완료되었다는 다이얼로그가 나와야 한다.
                        * 한번더 버튼 선택했을 때 가입취소할 수 있는 다이얼로그가 나와야 한다.*/
                        AddJoin(crew_id, user_email);
                        AlertDialog.Builder acceptbuilder = new AlertDialog.Builder(ReadCrewActivity.this);
                        acceptbuilder.setMessage("가입 신청이 완료되었습니다.\n리더가 승인하면 크루에 가입됩니다.");
                        acceptbuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                // 가입 신청이 완료되었으니 버튼이 바껴야 한다.
                                binding.btnJoin.setText("가입 승인 대기중");

                            }
                        });

                        acceptbuilder.show();
                        
                    }
                }else{
                    Log.d(TAG, "checkmember onResponse: status is false");
                }
            }

            @Override
            public void onFailure(Call<CrewData> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }

    // 현 크루에 가입 신청하는 메서드
    // 가입 신청하는 날짜를 보내야 한다.
    private void AddJoin(int crew_id, String user_email) {
        Log.d(TAG, "AddJoin: crew_id="+crew_id);
        Log.d(TAG, "AddJoin: user_email="+user_email);
        retrofitInterface.AddJoin(crew_id, user_email).enqueue(new Callback<CrewData>() {
            @Override
            public void onResponse(Call<CrewData> call, Response<CrewData> response) {

                CrewData result= response.body();
                Log.d(TAG, "AddJoin onResponse: result = "+ result);
                // 가입 대기중이니까 관리자가 가입 대기 중 인것을 확인할 수 있어야 한다.
            }

            @Override
            public void onFailure(Call<CrewData> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });

    }

    // 현 크루에 바로 가입할 수 있는 메서드
    private void AddMember(int crew_id, String user_email) {
        Log.d(TAG, "AddMember: crew_id="+crew_id);
        Log.d(TAG, "AddMember: user_email="+user_email);

        retrofitInterface.AddMember(crew_id, user_email, 0).enqueue(new Callback<CrewData>() {
            @Override
            public void onResponse(Call<CrewData> call, Response<CrewData> response) {

                CrewData result = response.body();
                Log.d(TAG, "AddMember onResponse: result =" + result);

                String status = result.getStatus();
                if(status.equals("true"))
                {
                    AlertDialog.Builder joinbuider = new AlertDialog.Builder(ReadCrewActivity.this);
                    joinbuider.setMessage("가입이 완료되었습니다.");
                    joinbuider.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // 가입이 완료되었으니 가입 버튼이 사라져야 한다.
                            // 크루원 한명 증가해야 한다.
                            Log.d(TAG, "onClick: current = " +current);
                            binding.txPeople.setText(String.valueOf(++current));
                            binding.btnJoin.setVisibility(View.GONE);
                            // 가입이 됐으니 바로 crew_member db에 추가 되어야 한다.
                            binding.btnSetting.setVisibility(View.VISIBLE);
                            // 설정 버튼 생기게 하기.

                        }
                    });
                    joinbuider.show();
                }



            }

            @Override
            public void onFailure(Call<CrewData> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }

    // 현 유저는 관리자가 아니라 참여자 입장일때 들어오는 메서드
    // 해당 유저가 현 크루에 가입했는지 안했는지 확인하는 메서드
    private void CheckJoin(String user_email, int crew_id) {

        Log.d(TAG, "CheckJoin: user_email =" + user_email);
        Log.d(TAG, "CheckJoin: crew_id = "+ crew_id);
        retrofitInterface.CheckJoin(crew_id, user_email).enqueue(new Callback<CrewData>() {
            @Override
            public void onResponse(Call<CrewData> call, Response<CrewData> response) {

                CrewData result = response.body();
                Log.d(TAG, "onResponse: checkjoin result =" + result);

                String status = result.getStatus();

                if(status.equals("true"))
                {
                    // 해당 크루의 참여자라는 뜻
                    // 참여자라면 가입하기 버튼이 없어야 한다.
                    binding.btnJoin.setVisibility(View.GONE);
                    // 참여자는 설정 버튼이 있어야 한다.
                }else{

                    // 참여하지 않았는데 가입 신청 중 상태인 지 구분
                    // status = Resister 가입 신청 중이고 승인 대기중이다
                    // status = false 가입 하지 않았다는 뜻.
                    if(status.equals("false"))
                    {
                        // 해당 크루에 참여하지 않는다는 뜻
                        binding.btnJoin.setVisibility(View.VISIBLE);
                        // 설정 버튼이 사라져야 한다.
                        binding.btnSetting.setVisibility(View.GONE);

                    }else{
                        // 현재 가입 승인 대기중이라는 것.
                        binding.btnJoin.setText("가입 승인 대기중");
                        binding.btnSetting.setVisibility(View.GONE);
                    }




                }
           }

            @Override
            public void onFailure(Call<CrewData> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }
}