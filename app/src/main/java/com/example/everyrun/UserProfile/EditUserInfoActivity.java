package com.example.everyrun.UserProfile;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.CompoundButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.everyrun.MainActivity;
import com.example.everyrun.UserInfo.UserSharedPreference;
import com.example.everyrun.R;
import com.example.everyrun.Retrofit.RetrofitClient;
import com.example.everyrun.Retrofit.RetrofitInterface;
import com.example.everyrun.RetrofitData.UserInfoData;
import com.example.everyrun.databinding.ActivityEditUserInfoBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditUserInfoActivity extends AppCompatActivity implements BottomSheetDialog.BottomSheetListener, View.OnClickListener{

    private final String TAG = this.getClass().getSimpleName(); // log
    private ActivityEditUserInfoBinding binding;
    Context context;
    private UserSharedPreference preferenceHelper;
    private RetrofitClient retrofitClient;
    private RetrofitInterface retrofitInterface;

    // 현 액티비티에서 가져오는 정보
    String nick;
    String final_nick;

    //shared에서 가져오는 유저 정보
    String user_nick;
    String user_profile;
    String user_email;

    AlertDialog.Builder builder;
    AlertDialog ad;

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
        binding = ActivityEditUserInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = getApplicationContext();
        preferenceHelper = new UserSharedPreference(this);
        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();

        setUI();

        binding.txEdit.setOnClickListener(this);
        binding.imgProfile.setOnClickListener(this);
    }

    private void setUI() {
        //shared에 있는 유저 정보를 갖고 와서 데이터 세팅해주기
        user_nick = preferenceHelper.getNick();
        user_profile = preferenceHelper.getPROFILE();
        user_email = preferenceHelper.getEmail();


        // 유저 닉네임 세팅해주기
        binding.etNick.setText(user_nick);

        /**유저 프로필 세팅해주기
         * (1) basic - 기본 프로필로 세팅
         * (2) 기본아닐때 - 사용자가 설정한 이미지로 세팅**/
        if(user_profile.equals("basic"))
        {
            // 기본 이미지로 세팅해주기
            binding.imgProfile.setImageResource(R.drawable.user_img);

        }else{
            // 사용자가 설정한 이미지 UserProfileImg
            String url = "http://3.36.174.137/UserProfileImg/" + user_profile;
            // glide로 이미지 세팅해주기
            Glide.with(EditUserInfoActivity.this).load(url).into(binding.imgProfile);
        }

        // 닉네임 수정 text watcher
        binding.etNick.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 입력이 완료된 후에 설정방법에 맞는지 확인
                nick = binding.etNick.getText().toString();

                if (binding.etNick.length() > 0) {
                    // 공백이 아니라면
                    if (binding.etNick.length() > 1) {
                        Log.d(TAG, "afterTextChanged: 3자 이상");

                        // 원래 닉네임이 아닌 경우에 중복검사를 해야한다.
                        if(nick.equals(user_nick))
                        {
                            Log.d(TAG, "onTextChanged: 원래 닉네임과 일치");
                        }else{
                            // 원래 닉네임과 일치하지 않을 때
                            Log.d(TAG, "onTextChanged: 원래 닉네임과 일치X");
                            CheckIdOverlap(); // ID 중복 검사 메서드
                        }


                    } else {
                        Log.d(TAG, "afterTextChanged: 2개 이하");

                      /*  닉네임이 2개 미만이니까 경고 메세지
                          edittext 빨간 테두리
                          text 생기고, 닉네임 2자 이상 입력하세요 */

                        binding.etNick.setBackgroundResource(R.drawable.et_red);
                        binding.txWarn.setVisibility(View.VISIBLE);
                        binding.txWarn.setText("닉네임을 2자 이상 입력하세요.");
                        binding.imgcheck.setVisibility(View.GONE);

                    }
                } else {
                    // 공백이라면
                    // 버튼 비활성화, 색변경
                    // 다시 edittext 원상복구시키기
                    binding.etNick.setBackgroundResource(R.drawable.et_rounded);
                    binding.txWarn.setVisibility(View.GONE);
                    binding.imgcheck.setVisibility(View.GONE);

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {



            }
        });

        // 물음표 (닉네임 설정방법 설명 말풍선)
        binding.imgQuestion.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                // 물음표 선택했을 때
                if (isChecked) {
                    binding.txtBanner.setVisibility(View.VISIBLE);
                    binding.txtBannerTail.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onCheckedChanged: 물음표 클릭 보이기");
                }
                // 물음표 먈풍선 닫을 때
                else {
                    binding.txtBanner.setVisibility(View.GONE);
                    binding.txtBannerTail.setVisibility(View.GONE);
                    Log.d(TAG, "onCheckedChanged: 물음표 안보이기");
                }
            }
        });
        
        


    }

    private void CheckIdOverlap() {



        Log.d(TAG, "afterTextChanged: 잘 설정");
        // id 중복확인해야 함.

        // 특수문자 있는지 확인
        // 한글 영어 숫자 일부 특수문자만 허용
        Boolean test = Pattern.matches("^[0-9a-zA-Zㄱ-ㅎ가-힣]*$", nick);

        // 밑줄과 마침표만 확인하기
        if(test || nick.contains(".") || nick.contains("-") || nick.contains("_"))
        {
            Log.d(TAG, "CheckIdOverlap: o");

            retrofitInterface.CheckId(nick).enqueue(new Callback<UserInfoData>() {
                @Override
                public void onResponse(Call<UserInfoData> call, Response<UserInfoData> response) {

                    UserInfoData result = response.body();

                    String status = result.getStatus();

                    if (status.equals("true")) {
                        Log.d(TAG, "onResponse: 가입 가능");
                        binding.etNick.setBackgroundResource(R.drawable.et_rounded);
                        binding.txWarn.setVisibility(View.GONE);
                        binding.imgcheck.setVisibility(View.VISIBLE);


                    } else {
                        Log.d(TAG, "onResponse: id 중복");
                        binding.etNick.setBackgroundResource(R.drawable.et_red);
                        binding.txWarn.setVisibility(View.VISIBLE);
                        binding.txWarn.setText("중복된 ID입니다.");
                        binding.imgcheck.setVisibility(View.GONE);

                    }
                }

                @Override
                public void onFailure(Call<UserInfoData> call, Throwable t) {
                    Log.e(TAG, "에러 = " + t.getMessage());
                }
            });



        }
        else
        {
            Log.d(TAG, "CheckIdOverlap: X");
            // 닉네임 설정 방법과 맞지 않다.
            binding.etNick.setBackgroundResource(R.drawable.et_red);
            binding.txWarn.setVisibility(View.VISIBLE);
            binding.txWarn.setText("닉네임 설정 방법에 맞지 않습니다.");
            binding.imgcheck.setVisibility(View.GONE);
        }


    }

    // bottom sheet dialog 클릭리스너 인터페이스
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onButtonClicked(int option) {
        switch (option){
            // 카메라 선택
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
            // 갤러리 선택
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
            // 기본이미지 선택
            case 3:
                Log.d(TAG, "onButtonClicked: 기본이미지 선택");

                // 사진 변경했지만 기본 이미지 선택
                img_check = 1;
                check = 2;
                // 기본 이미지 선택했으니 기본 유저 이미지로 프로필 세팅
                binding.imgProfile.setImageResource(R.drawable.user_img);

                break;
            // 취소 선택
            case 4:
                Log.d(TAG, "onButtonClicked: 취소 선택");
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            /*프로필 사진 선택시 어떻게 사진을 변경할 것인지 결정할수 있는 바텀 시트 다이얼로그 올라옴
            * (1) 사진 촬영
            * (2) 갤러리에서 선택
            * (3) 기본 이미지로 변경
            * (4) 취소*/

            case R.id.img_profile:

                // bottom sheet dialog 객체 선언해주고 보여줌.
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog();
                bottomSheetDialog.show(getSupportFragmentManager(), "bottomsheetdialog");
                Log.d(TAG, "onClick: 바텀 시트 보여짐");
                break;
                
                /*프로필 수정 정리
                * check==1 사진을 변경하지 않음
                * check==2 사진을 변경함
                *   img_check == 1 기본이미지로 변경
                *   img_check == 2 카메라 또는 갤러리로 사진 변경 */
            
            case R.id.tx_edit:
                final_nick = binding.etNick.getText().toString();
                
                if(check==1)
                {
                    Log.d(TAG, "onClick: 사진 변경 X");
                    // 닉네임만 변경한 상태
                    Log.d(TAG, "onClick: 수정한 닉네임 =" + final_nick);
                    Log.d(TAG, "onClick: 유저 이메일 =" + user_email);
                    NopicOrJustBasic(final_nick, user_email, 1);

                }else{
                    Log.d(TAG, "onClick: 사진 변경 O");
                    
                    if(img_check ==1)
                    {
                        Log.d(TAG, "onClick: 기본이미지로 변경");
                        Log.d(TAG, "onClick: 수정한 닉네임 =" + final_nick);
                        Log.d(TAG, "onClick: 유저 이메일 =" + user_email);
                        NopicOrJustBasic(final_nick, user_email, 2);


                    }else{
                        Log.d(TAG, "onClick: 카메라, 갤러리로 변경");
                        Log.d(TAG, "onClick: 수정한 닉네임 =" + final_nick);
                        Log.d(TAG, "onClick: 유저 이메일 =" + user_email);
                        Log.d(TAG, "onClick: 이미지 경로 =" + imagePath);
                        FromCameraOrGallery(final_nick, user_email, imagePath);
                    }
                }
                
                break;
        }
    }

    // 카메라, 갤러리에서 선택된 이미지로 수정하는 메서드
    private void FromCameraOrGallery(String nick, String user_email, String imagePath) {

        Log.d(TAG, "FromCameraOrGallery: imagepath = " + imagePath);

        File file = new File(imagePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);

        retrofitInterface.WithPic(body,nick,user_email).enqueue(new Callback<UserInfoData>() {
            @Override
            public void onResponse(Call<UserInfoData> call, Response<UserInfoData> response) {

                UserInfoData result = response.body();
                Log.d(TAG, "onResponse: result =" + result);
                String status = result.getStatus();


                if(status.equals("true"))
                {

                    // shared 정보도 수정하기
                    preferenceHelper.putPROFILE(result.getUser_photo());
                    preferenceHelper.putNick(result.getUser_name());
                    // 프로필로 이동
                    startActivity(new Intent(EditUserInfoActivity.this, UserProfileActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<UserInfoData> call, Throwable t) {
                Log.e(TAG, "에러 = " + t.getMessage());
            }
        });
    }

    // 프로필 수정되면 나오는 다이얼로그 메서드
    private void ShowEditDialog() {
        builder = new AlertDialog.Builder(EditUserInfoActivity.this);
        ad = builder.create();
        builder.setMessage("프로필 수정이 완료되었습니다.");
        builder.setNegativeButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        ad.show();
    }

    // 사진없이 닉네임만 수정 OR 기본이미지로 수정
    private void NopicOrJustBasic(String nick, String user_email, int i) {
        /*i ==1 사진 변경안하고 닉네임만 변경
        * i ==2 기본이미지로 변경하고 닉네임 변경*/
        Log.d(TAG, "NopicOrJustBasic: nick = " + nick);
        Log.d(TAG, "NopicOrJustBasic: email = " + user_email);
        Log.d(TAG, "NopicOrJustBasic: int = " + i);

        if(i==1)
        {
            retrofitInterface.NoPicOrJustBasic(user_email, nick,1).enqueue(new Callback<UserInfoData>() {
                @Override
                public void onResponse(Call<UserInfoData> call, Response<UserInfoData> response) {

                    UserInfoData result= response.body();
                    Log.d(TAG, "onResponse: result="+result);

                    String status = result.getStatus();

                    if(status.equals("true"))
                    {

                        // 닉네임만 수정
                        // shared 정보도 수정하기
                        preferenceHelper.putNick(result.getUser_name());

                        // 프로필 액티비티로 이동
                        startActivity(new Intent(EditUserInfoActivity.this, UserProfileActivity.class));
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<UserInfoData> call, Throwable t) {
                    Log.e(TAG, "에러 = " + t.getMessage());
                }
            });
        }else{
            retrofitInterface.NoPicOrJustBasic(user_email,nick,2).enqueue(new Callback<UserInfoData>() {
                @Override
                public void onResponse(Call<UserInfoData> call, Response<UserInfoData> response) {
                    UserInfoData result= response.body();
                    Log.d(TAG, "onResponse: result="+result);

                    String status = result.getStatus();

                    if(status.equals("true"))
                    {

                        // shared 정보도 수정하기
                        preferenceHelper.putPROFILE(result.getUser_photo());
                        preferenceHelper.putNick(result.getUser_name());

                        // 프로필 액티비티로 이동
                        startActivity(new Intent(EditUserInfoActivity.this, UserProfileActivity.class));
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<UserInfoData> call, Throwable t) {
                    Log.e(TAG, "에러 = " + t.getMessage());
                }
            });


        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(EditUserInfoActivity.this, UserProfileActivity.class));
        Log.d(TAG, "onBackPressed: 뒤로가기 누름");
        finish();

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
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(EditUserInfoActivity.this);

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
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(EditUserInfoActivity.this);

                    builder.setMessage("카메라에 대한 권한 사용을 거부하였습니다.\n기능 사용을 원하실 경우 휴대폰 설정 > 애플리케이션 관리자에서 해당 앱의 권한을 허용해주세요.");

                    android.app.AlertDialog alertDialog = builder.create();

                    alertDialog.show();


                }

                break;



        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) { // 결과가 있을 경우

            switch (requestCode) {
                case GALLERY: // 갤러리에서 이미지로 선택한 경우
                    Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                        imagePath = cursor.getString(index);
                        Log.d(TAG, "onActivityResult: 갤러리 선택 imagepath =" + imagePath);
                        bitmap = BitmapFactory.decodeFile(imagePath);
                        Glide.with(this).load(imagePath).into(binding.imgProfile);
                        cursor.close();
                    }
//                    InputStream 으로 이미지 세팅하기
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(data.getData());
                        bitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                        //ㅡGlide.with(this).load(imagePath).into(banner);
                        //banner.setImageBitmap(bitmap);
                        // 비트맵 객체를 jpg형식으로 변환
                        // 안드로이드에서 이미지 파일은 bitmap을 이용해 다룬다. bitmap객체를 이미지 파일로 저장하고 싶을 대 bitmap의 compress 메서드 이용한다.

//                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream );
//                        // 세번째 파라미터는 bitmap 이미지를 저장하기 위한 outputstream이다.
//                        // + input은 읽을때 output은 저장할때
//                        // 두번째 파라미터인 40은 사진을 40%로 압축한다는 의미이다.
//
//

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case CAMERA: // 카메라로 이미지 가져온 경우
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2; // 이미지 축소 정도. 원 크기에서 1/inSampleSize 로 축소됨
                    bitmap = BitmapFactory.decodeFile(imagePath, options);
                    Log.d(TAG, "onActivityResult: 카메라 촬영 imagepath =" + imagePath);
                    //BitmapFactory.decodeFile 로컬에 존재하는 파일을 그대로 읽어올 때 쓴다.
                    //options를 사용해서 이미지를 줄이고 줄인 이미지 경로 저장한것.
                    //banner.setImageBitmap(bitmap);
                    Glide.with(this).load(imagePath).into(binding.imgProfile);
                    // 비트맵 객체를 jpg형식으로 변환
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream );
                    byte[] byteArray = stream.toByteArray();

                    // setImageBitmap 비트맵을 load하는 것.
                    // "bitmap" 은 비트맵 객체이다.
                    // 이 비트맵 객체를 jpeg 이미지로 저장해야 함.
                    break;
            }

        }
    }


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