package com.example.everyrun;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.everyrun.Challenge.ChallengeActivity;
import com.example.everyrun.Community.CommunityActivity;
import com.example.everyrun.Record.RecordActivity;
import com.example.everyrun.UserInfo.UserSharedPreference;
import com.example.everyrun.Retrofit.RetrofitClient;
import com.example.everyrun.Retrofit.RetrofitInterface;
import com.example.everyrun.RetrofitData.UserInfoData;
import com.example.everyrun.UserProfile.SettingActivity;
import com.example.everyrun.UserProfile.UserProfileActivity;
import com.example.everyrun.databinding.ActivityMainBinding;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;


import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, MapView.CurrentLocationEventListener, MapView.MapViewEventListener {

    private final String TAG = this.getClass().getSimpleName(); // log
    private ActivityMainBinding binding;
    Context context;
    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    // 첫 번째 뒤로가기 버튼을 누를때 표시
    private Toast toast;
    private RetrofitClient retrofitClient;
    private RetrofitInterface retrofitInterface;
    private UserSharedPreference preferenceHelper;

    String user_email;

    TextView tx_nick;
    ImageView headerprofile;

    private MapView mapView;
    private ViewGroup mapViewContainer;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(binding.getRoot());
        context = getApplicationContext();
        preferenceHelper = new UserSharedPreference(this);
        // 레트로핏 객체
        retrofitClient = RetrofitClient.getInstance();
        retrofitInterface = RetrofitClient.getRetrofitInterface();


        setUI();


    }

    private void setUI() {

        user_email = preferenceHelper.getEmail();


        Log.d(TAG, "setUI: email = " + user_email);

        // inflateheaderview 사용해서 코드상에서 네비게이션 뷰에 헤더 추가하기
        View headerview = binding.navigationView.inflateHeaderView(R.layout.header_layout);
        tx_nick = headerview.findViewById(R.id.tx_nick); // 헤더뷰에서 유저 닉네임 선언해주기
        headerprofile = headerview.findViewById(R.id.img_profile); // 헤더뷰에 프로필 이미지 선언해주기

        // mapView로 지도 띄우기
        Log.d(TAG, "setUI: mapview로 지도 띄우기 코드 시작");
        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        Log.e(TAG, "setUI: setmapvieweventlistener");
        mapView.setMapViewEventListener(this); // this에 MapView.MapViewEventListener 구현하기
        Log.d(TAG, "setUI: setCurrentLocationTrackingMode");
        // 현위치 트래킹 모드 : 지도 화면 중심을 단말의 현재 위치로 이동시켜줌.
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
        Log.d(TAG, "setUI: setCurrentLocationEventListener");
        mapView.setCurrentLocationEventListener(this);



        Log.d(TAG, "setUI: 지도 사용 체크");
        if (!checkLocationServicesStatus()) // 현재 gps를 제공받을 수 있는 환경인지 체크하기
        {
            Log.d(TAG, "setUI: showDialogForLocationServiceSetting");
            // 제공받을 수 없는 환경이면 허용해달라는 다이얼로그 띄워줘야 함.
            showDialogForLocationServiceSetting(); // gps 활성화를 위한 메서드
        } else {
            Log.d(TAG, "setUI: checkRunTimePermission");
            checkRunTimePermission(); // 위치 퍼미션 체크하는 메서드
        }

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
                            Glide.with(MainActivity.this).load(url).into(headerprofile);
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
                startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
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
                        return true;

                    // 운동 기록 화면으로 이동
                    case R.id.nav_record:
                        item.setChecked(true);
                        binding.drawerLayout.closeDrawers();
                        startActivity(new Intent(MainActivity.this, RecordActivity.class));
                        return true;

                    // 커뮤니티로 이동
                    case R.id.nav_community:
                        item.setChecked(true);
                        binding.drawerLayout.closeDrawers();
                        startActivity(new Intent(MainActivity.this, CommunityActivity.class));
                        return true;

                    // 커뮤니티 화면으로 이동
//                    case R.id.nav_callenge:
//                        item.setChecked(true);
//                        binding.drawerLayout.closeDrawers();
//                        startActivity(new Intent(MainActivity.this, ChallengeActivity.class));
//                        return true;

                    // 설정 화면으로 이동
                    case R.id.nav_setting:
                        item.setChecked(true);
                        binding.drawerLayout.closeDrawers();
                        startActivity(new Intent(MainActivity.this, SettingActivity.class));
                        return true;


                }

                return false;
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapViewContainer.removeAllViews();
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
    public void onBackPressed() {
        // 기존 뒤로가기 버튼의 기능을 막기위해 주석처리 또는 삭제
        // super.onBackPressed();

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds

        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);

        } else {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
            // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
            // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
            // 현재 표시된 Toast 취소
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                finish();
                toast.cancel();
            }
        }

    }


    @Override
    public void onClick(View view) {

    }


    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters) {
        Log.d(TAG, "onCurrentLocationUpdate: 입장");
        MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
        Log.d(TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
        Log.d(TAG, "onCurrentLocationUpdate: 위도 = " + mapPointGeo.latitude);
        Log.d(TAG, "onCurrentLocationUpdate: 경도 =" + mapPointGeo.longitude);

        // 현재 위치 마커 찍기
        Log.d(TAG, "setUI: mappoiitem 객체 생성");
        MapPOIItem marker = new MapPOIItem();

        Log.d(TAG, "setUI: 위도 경도 설정");
        //맵 포인트 위도경도 설정
        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude);
        marker.setItemName("현재 위치");
        marker.setTag(0);
        marker.setMapPoint(mapPoint);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        //앱을 실행하면 파란 마커로 생성되고 해당 마커를 선택하면 빨간 마커로 변한다.

        Log.d(TAG, "setUI: 마커 추가 메서드");
        mapView.addPOIItem(marker);

        // poly line 그리기
        MapPolyline polyline = new MapPolyline();
        // MapPolyLine 파라미터 : polyline을 구성하는 점들의 좌표를 저장하는 array의 메모리 할당 크기 (pointListCapacity : polyline의 점 개수를 지정한다.)
        polyline.setTag(1000); // polyline tag번호 지정해주기
        // polyline 객체에 임의의 정수값(tag)를 지정할 수 있다.
        // MapView 객체에 등록된 Polyline둘 증 특정 polyline을 찾기위한 식별자로 사용할 수 있다.
        // tag값을 반드시 지정해야 하는 것은 아니다.
        polyline.setLineColor(Color.argb(128, 255, 51, 0)); // Polyline 컬러 지정.

        // Polyline 좌표 지정.
        polyline.addPoint(MapPoint.mapPointWithGeoCoord(37.537229, 127.005515));
        polyline.addPoint(MapPoint.mapPointWithGeoCoord(37.545024, 127.03923));
        polyline.addPoint(MapPoint.mapPointWithGeoCoord(37.527896, 127.036245));
        polyline.addPoint(MapPoint.mapPointWithGeoCoord(37.541889, 127.095388));

        // addPoint : polyline에 점 하나 추가하는 것.
        // addPoints : polyline에 점 리스트 추가하는 것.

        // Polyline 지도에 올리기.
        mapView.addPolyline(polyline);

        // 지도뷰의 중심좌표와 줌레벨을 Polyline이 모두 나오도록 조정.
        MapPointBounds mapPointBounds = new MapPointBounds(polyline.getMapPoints());
        Log.d(TAG, "onCurrentLocationUpdate: polyline = "+ polyline);
        Log.d(TAG, "onCurrentLocationUpdate: polyline.getMapPoints() = "+ polyline.getMapPoints());

        int padding = 10; // px
        mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));
        Log.d(TAG, "onCurrentLocationUpdate: getPointList =" + polyline.getObjects().toString());
        // moveCamera : cameraupdate에 정의된 명령어에 따라 카메라를 재배치한다.
    }


    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
    }


    private void onFinishReverseGeoCoding(String result) {
//        Toast.makeText(LocationDemoActivity.this, "Reverse Geo-coding : " + result, Toast.LENGTH_SHORT).show();
    }

    // ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                Log.d("@@@", "start");
                //위치 값을 가져올 수 있음

            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있다
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void checkRunTimePermission() {

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            // 3.  위치 값을 가져올 수 있음

        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }
}