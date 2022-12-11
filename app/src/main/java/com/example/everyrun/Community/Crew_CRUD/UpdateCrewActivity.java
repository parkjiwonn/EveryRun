package com.example.everyrun.Community.Crew_CRUD;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.everyrun.Community.CrewBottomSheetDialog;
import com.example.everyrun.Community.CrewSetting.CrewSettingActivity;
import com.example.everyrun.R;
import com.example.everyrun.Retrofit.RetrofitClient;
import com.example.everyrun.Retrofit.RetrofitInterface;
import com.example.everyrun.RetrofitData.CrewData;
import com.example.everyrun.UserInfo.UserSharedPreference;
import com.example.everyrun.UserProfile.BottomSheetDialog;
import com.example.everyrun.UserProfile.EditUserInfoActivity;
import com.example.everyrun.databinding.ActivityCreateCrewBinding;
import com.example.everyrun.databinding.ActivityUpdateCrewBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingDeque;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateCrewActivity extends AppCompatActivity  implements BottomSheetDialog.BottomSheetListener, View.OnClickListener{

    private final String TAG = this.getClass().getSimpleName(); // log

    private ActivityUpdateCrewBinding binding;
    Context context;
    String user_email;
    int crew_id;
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
    String reader;

    Uri pictureUri;

    Bitmap bitmap = null; // 카메라
    int nCurrentPermission = 0;
    static final int PERMISSIONS_REQUEST = 1; // 갤러리 권한 요청
    static final int PERMISSIONS_REQUEST2 = 2; // 카메라 권한 요청

    // 카메라 촬영시 사용되는 intent
    Intent intent;
    final int CAMERA = 100; // 카메라 선택시 인텐트로 보내는 값
    final int GALLERY = 101; // 갤러리 선택 시 인텐트로 보내는 값

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat imageDate = new SimpleDateFormat("yyyyMMdd_HHmmss"); // 이미지 중복 피하기 위한 timestamp
    String imagePath; // 카메라, 갤러리 사진 경로

    // 기본이미지 선택 / 카메라, 갤러리에서 사진 선택
    int img_check = 1;
    // 이미지 선택 안하면 기본 값 = 1 / 이미지 선택하면 2로 변경.
    int check = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateCrewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();
        preferenceHelper = new UserSharedPreference(this);
        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();

        crew_id = getIntent().getIntExtra("crew_id", 0);
        Log.d(TAG, "onCreate: crew_id ="+crew_id);

        setUI();

        binding.imgBanner.setOnClickListener(this);
        binding.btnUpdate.setOnClickListener(this);
    }

    private void setUI() {

        // 기존 크루의 정보를 세팅해줘야 한다.
        retrofitInterface.SetCrewInfo(crew_id).enqueue(new Callback<CrewData>() {
            @Override
            public void onResponse(Call<CrewData> call, Response<CrewData> response) {

                CrewData result = response.body();
                Log.d(TAG, "onResponse: result = " + result);

                // 스피너 - 지역 선택
                final String[] spinnerArea = getResources().getStringArray(R.array.area_list);

                ArrayAdapter arrayAdapter = ArrayAdapter.createFromResource(UpdateCrewActivity.this, R.array.area_list, android.R.layout.simple_spinner_item);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.Areaspinner.setAdapter(arrayAdapter);

                String status = result.getStatus();
                title = result.getTitle();
                content = result.getContent();
                area = result.getArea();
                member = result.getMember();
                total = result.getTotal();
                current = result.getCurrent();
                banner = result.getBanner();
                reader = result.getReader();

                if(status.equals("true"))
                {
                    // 크루 이름 세팅 ( 이름 , 글자 수)
                    binding.etName.setText(title);
                    binding.txNamecount.setText(String.valueOf(title.length()));

                    // 크루 소개말 세팅 ( 소개, 글자 수)
                    binding.etProduce.setText(content);
                    binding.txProducecount.setText(String.valueOf(content.length()));

                    // 크루 정원 세팅
                    binding.etPeople.setText(String.valueOf(total));

                    // 크루 사진 세팅
                    // 기본이미지인 경우
                    if (banner.equals("basic")) {
                        binding.imgBanner.setImageResource(R.drawable.img1);
                    } else {
                        // 사용자가 설정한 이미지 UserProfileImg
                        String url = "http://3.36.174.137/CrewImg/" + banner;
                        // glide로 이미지 세팅해주기
                        Glide.with(UpdateCrewActivity.this).load(url).into(binding.imgBanner);
                    }

                    // 스피너 세팅
                    binding.Areaspinner.setSelection(area, false);

                    // 라디오 버튼 세팅
                    switch (member){
                        case 1:
                            // 바로 가입
                            binding.radioRightnow.setChecked(true);
                            break;

                        case 2:
                            // 리더 숭인 후 가입
                            binding.radioAccept.setChecked(true);
                            break;
                    }

                }else
                {
                    Log.d(TAG, "onResponse: 수정 정보 세팅 status is false");
                }



            }

            @Override
            public void onFailure(Call<CrewData> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });

        // 크루 제목 글자 수 제한두기
        binding.etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                binding.txNamecount.setText(String.valueOf(charSequence.length()));
                binding.txNamewarn.setVisibility(View.GONE);
                binding.etName.setBackgroundResource(R.drawable.et_black_rounded);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // 소개 문구 글자 수 제한하기
        binding.etProduce.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.txProducecount.setText(String.valueOf(charSequence.length()));
                binding.txProducewarn.setVisibility(View.GONE);
                binding.etProduce.setBackgroundResource(R.drawable.et_black_rounded);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // 크루 정원 작성하는 부분
        binding.etPeople.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.txPeoplewarn.setVisibility(View.GONE);
                binding.etPeople.setBackgroundResource(R.drawable.et_black_rounded);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.radio_rightnow:
                        Log.d(TAG, "onCheckedChanged: 바로 가입");
                        member = 1;
                        break;
                    case R.id.radio_accept:
                        Log.d(TAG, "onCheckedChanged: 리더 승인 후 가입");
                        member = 2;
                        break;
                }
            }
        });

        binding.Areaspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemSelected: 선택 지역 =" + i);
                area = i; // 선택한 지역으로 변수 세팅하기.
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.img_banner:
                // 이미지 선택 후 바텀 시트 다이얼로그 올라와야 함.
                // bottom sheet dialog 객체 선언해주고 보여줌.
//                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog();
//                bottomSheetDialog.show(getSupportFragmentManager(), "bottomsheetdialog");

                check =2;
                img_check = 2;

                ImagePicker.with(UpdateCrewActivity.this)
                        // .crop(16f, 9f)와 같이 이미지 자르는 사각형의 크기 지정 가능.
                        // ()안에 값이 없으면 유저가 직접 크기 선택
                        .crop(16f, 9f)
                        // 이미지 크기 지정
                        .compress(1024)
                        // 최대 가로세로 크기 지정
                        .maxResultSize(1080, 1080)
                        .start();

                Log.d(TAG, "onClick: 바텀 시트 보여짐");
                break;

            case R.id.btn_update:

                Log.d(TAG, "onClick: ");

                // (1) 개설버튼 누르고 모두 기입했는지 확인하기
                // (2) 기본이미지로 개설하는지 다른 이미지 선택해서 개설하는지 구분하기

                // 크루 이름 공백인지 아닌지 확인
                // 공백이라면 edittext 빨간색으로
                // 경고문 보여주기
                if (binding.etName.length() == 0) {
                    binding.etName.setBackgroundResource(R.drawable.et_red);
                    binding.txNamewarn.setVisibility(View.VISIBLE);
                }
                // 크루 설명 공백인지 아닌지 확인
                // 공백이라면 edittext 빨간색으로
                // 경고문 보여주기

                if (binding.etProduce.length() == 0) {
                    binding.etProduce.setBackgroundResource(R.drawable.et_red);
                    binding.txProducewarn.setVisibility(View.VISIBLE);
                }

                if(binding.etPeople.length() == 0)
                {
                    binding.etPeople.setBackgroundResource(R.drawable.et_red);
                    binding.txPeoplewarn.setVisibility(View.VISIBLE);
                }

                // 필수 입력 항목이 모두 공백이 아닌지 확인
                if(binding.etName.length() != 0 && binding.etProduce.length() !=0 && binding.etPeople.length() != 0)
                {
                    // 공백이 아니라면
                    title = binding.etName.getText().toString(); // 제목
                    content = binding.etProduce.getText().toString(); // 소개 글
                    total = Integer.parseInt(binding.etPeople.getText().toString()); // 정원
                    // 이미지, 지역, 가입 방법
                    Log.e(TAG, "onClick: title =" + title );
                    Log.e(TAG, "onClick: content =" + content );
                    Log.e(TAG, "onClick: total =" + total );
                    Log.e(TAG, "onClick: area =" + area );
                    Log.e(TAG, "onClick: member =" + member );
                    Log.e(TAG, "onClick: check =" + check );
                    Log.e(TAG, "onClick: imgcheck =" + img_check );

                    if(check == 1)
                    {
                        Log.d(TAG, "onClick: 사진 변경X, 텍스트만 수정");
                        NoPicOrJustBasic(crew_id,title,content,total,area,member,1);
                    }else{
                        Log.d(TAG, "onClick: 사진 변경함");
                        
                        if(img_check == 1)
                        {
                            Log.d(TAG, "onClick: 기본 이미지로 변경");
                            NoPicOrJustBasic(crew_id,title,content,total,area,member,2);
                        }else{
                            Log.d(TAG, "onClick: 갤러리/ 카메라로 선택");
                            FromCameraOrGallery(crew_id, title,content,total,area,member,imagePath);
                        }
                    }

                }

                break;
        }
        
    }

    // 갤러리/ 카메라에서 이미지 선택
    private void FromCameraOrGallery(int crew_id, String title, String content, int total, int area, int member, String imagePath) {

        Log.d(TAG, "FromCameraOrGallery: imagepath = " + imagePath);

        File file = new File(pictureUri.getPath());
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);

        Log.d(TAG, "FromCameraOrGallery: crew_id =" +crew_id);
        Log.d(TAG, "FromCameraOrGallery: title =" +title);
        Log.d(TAG, "FromCameraOrGallery: content =" +content);
        Log.d(TAG, "FromCameraOrGallery: total =" +total);
        Log.d(TAG, "FromCameraOrGallery: area =" +area);
        Log.d(TAG, "FromCameraOrGallery: member =" +member);


        retrofitInterface.CameraOrGallery(body, crew_id, title, content, total, area, member).enqueue(new Callback<CrewData>() {
            @Override
            public void onResponse(Call<CrewData> call, Response<CrewData> response) {
                CrewData result = response.body();
                Log.d(TAG, "onResponse: 사진 업데이트 result = "+result);
                String status = result.getStatus();

                if(status.equals("true")){

                    // 수정되었으면 다시 게시글 상세보기로 이동 시키기
                    startActivity(new Intent(UpdateCrewActivity.this, ReadCrewActivity.class).putExtra("crew_id", crew_id));
                    finish();

                }else{
                    Log.d(TAG, "onResponse: 갤러리,사진 선택후 업데이트 status is false");
                }
            }

            @Override
            public void onFailure(Call<CrewData> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }

    // 기본 이미지 선택 OR 이미지 변경X
    private void NoPicOrJustBasic(int crew_id, String title, String content, int total, int area, int member, int i) {

        if( i == 1)
        {
            // 이미지 변경 X
            retrofitInterface.NoPicOrJustBasic(crew_id, title, content, total, area, member, 1).enqueue(new Callback<CrewData>() {
                @Override
                public void onResponse(Call<CrewData> call, Response<CrewData> response) {
                    CrewData result = response.body();
                    Log.d(TAG, "onResponse: 사진 업데이트 이미지 변경X result = "+result);
                    String status = result.getStatus();

                    if(status.equals("true")){

                        // 수정되었으면 다시 게시글 상세보기로 이동 시키기
                        startActivity(new Intent(UpdateCrewActivity.this, ReadCrewActivity.class).putExtra("crew_id", crew_id));
                        finish();


                    }else{
                        Log.d(TAG, "onResponse: 갤러리,사진 선택후 업데이트 status is false");
                    }

                }

                @Override
                public void onFailure(Call<CrewData> call, Throwable t) {
                    Log.e(TAG, "에러 = " + t.getMessage());
                }
            });
        }else{
            // 이미지 변경 - 기본이미지로
            retrofitInterface.NoPicOrJustBasic(crew_id, title,  content, total, area, member, 2).enqueue(new Callback<CrewData>() {
                @Override
                public void onResponse(Call<CrewData> call, Response<CrewData> response) {
                    CrewData result = response.body();
                    Log.d(TAG, "onResponse: 사진 업데이트 기본이미지로 변경 result = "+result);
                    String status = result.getStatus();

                    if(status.equals("true")){

                        // 수정되었으면 다시 게시글 상세보기로 이동 시키기
                        startActivity(new Intent(UpdateCrewActivity.this, ReadCrewActivity.class).putExtra("crew_id", crew_id));
                        finish();

                    }else{
                        Log.d(TAG, "onResponse: 갤러리,사진 선택후 업데이트 status is false");
                    }
                }

                @Override
                public void onFailure(Call<CrewData> call, Throwable t) {
                    Log.e(TAG, "에러 = " + t.getMessage());
                }
            });
        }

    }

    // 바텀 시트 다이얼로그 클릭 리스너
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onButtonClicked(int option) {

        switch (option){
            case 1:
                Log.d(TAG, "onButtonClicked: 카메라 선택");

                // 사진 변경했지만 카메라 선택
                img_check = 2;
                check = 2;

                boolean hasCamPerm = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

                if (!hasCamPerm )  // 권한 없을 시  권한설정 요청
                {
                    OnCameraCheckPermission();
                }
                else
                {
                    intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        File imageFile = null;
                        try {
                            imageFile = createImageFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (imageFile != null) {
                            Uri imageUri = FileProvider.getUriForFile(getApplicationContext(),
                                    "com.example.EveryRun.fileprovider",
                                    imageFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            startActivityForResult(intent, CAMERA); // final int CAMERA = 100;
                        }
                    }
                }

                break;
            case 2:
                Log.d(TAG, "onButtonClicked: 갤러리 선택");

                // 사진 변경했지만 갤러리 선택
                img_check = 2;
                check = 2;

                boolean hasWritePerm = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

                if(!hasWritePerm)
                {
                    OnCheckPermission();
                }
                else
                {
                    intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    intent.setType("image/*");
                    startActivityForResult(intent, GALLERY); // final int GALLERY = 101;

                }

                break;
            case 3:
                Log.d(TAG, "onButtonClicked: 기본 이미지 선택");

                // 사진 변경했지만 기본 이미지 선택
                img_check = 1;
                check = 2;
                // 기본 이미지 선택했으니 기본 유저 이미지로 프로필 세팅
                binding.imgBanner.setImageResource(R.drawable.img1);

                break;
            case 4:
                Log.d(TAG, "onButtonClicked: 취소 선택");
                break;

        }
    }

    // 카메라 권한 요청
    private void OnCameraCheckPermission() {

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ){
                Toast.makeText(this, "해당 기능 사용을 위해서는 권한을 설정해야 합니다.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},PERMISSIONS_REQUEST2);
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST2);

            }

        }
    }

    // 갤러리 권한 요청
    public void OnCheckPermission(){
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ){
                Toast.makeText(this, "해당 기능 사용을 위해서는 권한을 설정해야 합니다.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSIONS_REQUEST);
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);

            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permission, grantResults);
        System.out.println("requestcode"+requestCode);


        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "갤러리에 대한 권한이 설정 되었습니다.", Toast.LENGTH_LONG).show();

                    intent = new Intent(Intent.ACTION_PICK);
                    intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                    intent.setType("image/*");
                    startActivityForResult(intent, GALLERY);
                }
                else
                {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(UpdateCrewActivity.this);

                    builder.setMessage("갤러리에 대한 권한 사용을 거부하였습니다.\n기능 사용을 원하실 경우 휴대폰 설정 > 애플리케이션 관리자에서 해당 앱의 권한을 허용해주세요.");

                    android.app.AlertDialog alertDialog = builder.create();

                    alertDialog.show();

                }

                break;

            case PERMISSIONS_REQUEST2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "카메라에 대한 권한이 설정 되었습니다.", Toast.LENGTH_LONG).show();

                    intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        File imageFile = null;
                        try {
                            imageFile = createImageFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (imageFile != null) {
                            Uri imageUri = FileProvider.getUriForFile(getApplicationContext(),
                                    "com.example.EveryRun.fileprovider",
                                    imageFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            startActivityForResult(intent, CAMERA);
                        }
                    }

                }
                else
                {
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(UpdateCrewActivity.this);

                    builder.setMessage("카메라에 대한 권한 사용을 거부하였습니다.\n기능 사용을 원하실 경우 휴대폰 설정 > 애플리케이션 관리자에서 해당 앱의 권한을 허용해주세요.");

                    android.app.AlertDialog alertDialog = builder.create();

                    alertDialog.show();


                }

                break;



        }
    }

    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {


            // 카메라/갤러리에서 가져온 이미지의 Uri
            pictureUri = data.getData();

            // Uri를 활용하여 ImageView에 가져온 이미지 표시
            binding.imgBanner.setImageURI(pictureUri);
            Log.d(TAG, "onActivityResult: pictureUri = "+ pictureUri);
            Log.d(TAG, "onActivityResult: pictureUri ="+pictureUri.getPath());
            /*
             * (1) getPath - 이미지 상대 경로
             * (2) getAbsolutePath - 이미지 절대 경로*/


        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK) { // 결과가 있을 경우
//
//            switch (requestCode) {
//                case GALLERY: // 갤러리에서 이미지로 선택한 경우
//                    Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
//                    if (cursor != null) {
//                        cursor.moveToFirst();
//                        int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
//                        imagePath = cursor.getString(index);
//                        Log.d(TAG, "onActivityResult: 갤러리 선택 imagepath =" + imagePath);
//                        bitmap = BitmapFactory.decodeFile(imagePath);
//                        Glide.with(this).load(imagePath).into(binding.imgBanner);
//                        cursor.close();
//                    }
////                    InputStream 으로 이미지 세팅하기
//                    try {
//                        InputStream inputStream = getContentResolver().openInputStream(data.getData());
//                        bitmap = BitmapFactory.decodeStream(inputStream);
//                        inputStream.close();
//                        //ㅡGlide.with(this).load(imagePath).into(banner);
//                        //banner.setImageBitmap(bitmap);
//                        // 비트맵 객체를 jpg형식으로 변환
//                        // 안드로이드에서 이미지 파일은 bitmap을 이용해 다룬다. bitmap객체를 이미지 파일로 저장하고 싶을 대 bitmap의 compress 메서드 이용한다.
//
////                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
////                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream );
////                        // 세번째 파라미터는 bitmap 이미지를 저장하기 위한 outputstream이다.
////                        // + input은 읽을때 output은 저장할때
////                        // 두번째 파라미터인 40은 사진을 40%로 압축한다는 의미이다.
////
////
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    break;
//                case CAMERA: // 카메라로 이미지 가져온 경우
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inSampleSize = 2; // 이미지 축소 정도. 원 크기에서 1/inSampleSize 로 축소됨
//                    bitmap = BitmapFactory.decodeFile(imagePath, options);
//                    Log.d(TAG, "onActivityResult: 카메라 촬영 imagepath =" + imagePath);
//                    //BitmapFactory.decodeFile 로컬에 존재하는 파일을 그대로 읽어올 때 쓴다.
//                    //options를 사용해서 이미지를 줄이고 줄인 이미지 경로 저장한것.
//                    //banner.setImageBitmap(bitmap);
//                    Glide.with(this).load(imagePath).into(binding.imgBanner);
//                    // 비트맵 객체를 jpg형식으로 변환
//                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream );
//                    byte[] byteArray = stream.toByteArray();
//
//                    // setImageBitmap 비트맵을 load하는 것.
//                    // "bitmap" 은 비트맵 객체이다.
//                    // 이 비트맵 객체를 jpeg 이미지로 저장해야 함.
//                    break;
//            }
//
//        }
//    }


    @SuppressLint("SimpleDateFormat")
    File createImageFile() throws IOException {
//        이미지 파일 생성
        String timeStamp = imageDate.format(new Date()); // 파일명 중복을 피하기 위한 "yyyyMMdd_HHmmss"꼴의 timeStamp
        String fileName = "IMAGE_" + timeStamp; // 이미지 파일 명
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(fileName, ".jpg", storageDir); // 이미지 파일 생성
        imagePath = file.getAbsolutePath(); // 파일 절대경로 저장하기
        return file;
    }
}