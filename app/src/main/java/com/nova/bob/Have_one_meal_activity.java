package com.nova.bob;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.widget.*;

import java.util.HashMap;
import java.util.logging.Handler;

import static java.lang.Thread.sleep;

public class Have_one_meal_activity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    GoogleMap hom_googlemap;
    CameraUpdate hom_camera;
    MarkerOptions hom_markeroption;
    Marker hom_marker;

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    String gender;

    Location location;

    BroadcastReceiver broadcastReceiver;

    HashMap<Marker, MarkerInfo> markers;

    Button start_food_button;

    //카메라 setting 시작
    private void hom_googlemap_camera_setting(){
        hom_googlemap.getUiSettings().setZoomGesturesEnabled(false);//줌 제스쳐 - 손가락 두개를 벌려서 지도의 zoom을 당기는 제스쳐
        hom_googlemap.getUiSettings().setScrollGesturesEnabled(false);//스크롤 제스쳐 - 지도 움직이는 제스쳐
        hom_googlemap.getUiSettings().setTiltGesturesEnabled(false);//틸트 제스처 - 화면 눕히는 제스처
        hom_googlemap.getUiSettings().setRotateGesturesEnabled(false);
        hom_googlemap.getUiSettings().setZoomControlsEnabled(false);//Zoom을 컨트롤 할 수 있게끔 setting, + - 버튼이 그것임.그런데 이상하게 안먹히네
        hom_camera = CameraUpdateFactory.zoomTo(17);//zoom level은 1~23자까지 있다.14레벨이 적절.수가 클수록 가까워짐
        hom_googlemap.animateCamera(hom_camera);//카메라 줌 땡기기 끝. 이값때문에 onmapready에 마지막에 넣어야함
    }
    //카메라 setting 끝


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_map_have_one_meal);

        markers = new HashMap<>();

        start_food_button = (Button)findViewById(R.id.hom_imagebutton);
        start_food_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( Build.VERSION.SDK_INT >= 22 &&
                        ContextCompat.checkSelfPermission(Have_one_meal_activity.this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(Have_one_meal_activity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    Log.i("chat", "adsfasd");
                }
                location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                Intent intent = new Intent(SocketService.START_MATCHING);
                Packet packet = new Packet(SocketService.START_MATCHING);
                packet.addData(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));
                intent.putExtra("Packet", packet.toByteArray());
                LocalBroadcastManager.getInstance(Have_one_meal_activity.this).sendBroadcast(intent);

                Log.i("chat", "adsfd");
            }
        });



        //노바톤4. 최초 gps 불러오기 시작

        if(mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();//
        }
        mGoogleApiClient.connect();
        //노바톤4. 최초 gps 불러오기 끝


        //노바톤5. google map fragment 연결하기
        SupportMapFragment mapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //노바톤5. google map fragment 연결하기 끝



        //노바톤4.구글맵 환경설정 시작

        //노바톤4.구글맵 환경설정 끝

        //노바톤5.구글맵 마커 띄우기 시작

        //노바톤5.구글맵 마커 띄우기 끝

        //노바톤6.쿨타임 5분 시작

        //노바톤6.쿨타임 5분 끝

        //노바톤7.

        //노바톤7.
        startDestroyer();
    }

    private void registerReceiver(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(SocketService.GET_MARKER_INFO)){
                    Log.i("chat", "마커인포");
                    intent.getStringExtra("nickName");
                    hom_markeroption = new MarkerOptions();
                    hom_markeroption.position(new LatLng(intent.getDoubleExtra("latitude", 0), intent.getDoubleExtra("longitude", 0)));
                    if(intent.getStringExtra("gender").equals("male")){
                        hom_markeroption.icon(BitmapDescriptorFactory.fromResource(R.drawable.have_one_meal_spoon));
                    }
                    else{
                        hom_markeroption.icon(BitmapDescriptorFactory.fromResource(R.drawable.have_one_meal_fork));
                    }
                    hom_markeroption.title(2017-intent.getIntExtra("age", 0)+"세")
                            .snippet(intent.getStringExtra("nickName"));
                    hom_marker = hom_googlemap.addMarker(hom_markeroption);
                    markers.put(hom_marker, new MarkerInfo(intent.getStringExtra("phoneNumber")));
                }
                else if(intent.getAction().equals(SocketService.MATCHING_SUCCESS)){
                    Show_dialog_success(intent.getStringExtra("gender"), intent.getStringExtra("nickName"), intent.getStringExtra("phoneNumber"),
                            intent.getIntExtra("age", 0));
                }
                else if(intent.getAction().equals(SocketService.MATCHING_FAILED)){
                    Show_dialog_fail();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(SocketService.GET_MARKER_INFO);
        intentFilter.addAction(SocketService.MATCHING_SUCCESS);
        intentFilter.addAction(SocketService.MATCHING_FAILED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        hom_googlemap = googleMap;

        if ( Build.VERSION.SDK_INT >= 22 &&
                ContextCompat.checkSelfPermission(Have_one_meal_activity.this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(Have_one_meal_activity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.i("chat", "adsfasd");
        }
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(location != null){
            hom_googlemap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            hom_googlemap_camera_setting();
        }
        //노바톤6. test 위도,경도 넣어주고 카메라 이동 시작

        //노바톤6. test 위도,경도 넣어주고 카메라 이동 끝


        //노바톤9. 마커 클릭 이벤트 시작
        hom_googlemap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                CameraUpdate center = CameraUpdateFactory.newLatLng(marker.getPosition());//marker위치로 카메라 이동
                hom_googlemap.animateCamera(center);//마커 클릭시 중앙으로 이동
                if(hom_marker.equals(marker)){
                    hom_marker.showInfoWindow();
                }
                return false;
            }
        });
        //노바톤9. 마커 클릭 이벤트 끝

        hom_googlemap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Packet packet = new Packet(SocketService.CLICK_MARKER);
                packet.addData(markers.get(marker).getPhoneNumber());
                Log.i("phone", markers.get(marker).getPhoneNumber());
                Intent intent = new Intent(SocketService.CLICK_MARKER);
                intent.putExtra("Packet", packet.toByteArray());
                LocalBroadcastManager.getInstance(Have_one_meal_activity.this).sendBroadcast(intent);
            }
        });

        //노바톤10. 카메라 기본 설정 시작(맨 마지막에 넣어야함)

        //노바톤10. 카메라 기본 설정 끝(맨 마지막에 넣어야함)

        registerReceiver();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.i("chat", location.getLatitude()+"aaa");
        Log.i("chat", location.getLongitude()+"aaa");
        if(hom_googlemap != null){
            hom_googlemap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            hom_googlemap_camera_setting();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //노바톤 etc.구글맵 스니펫 커스텀 시작 googlemap_snipet custum adapter start
    private class CustomInfoAdapter implements GoogleMap.InfoWindowAdapter {
        private final View mWindow;

        public CustomInfoAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.google_map_have_one_custom_info, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        private void render(Marker marker, View view) {
            TextView hom_age = (TextView) view.findViewById(R.id.hom_textview_age);
            TextView hom_name = (TextView) view.findViewById(R.id.hom_textview_name);
            hom_age.setText(marker.getTitle());
            hom_name.setText(marker.getSnippet());
        }
    }
    //노바톤 etc.구글맵 스니펫 커스텀 끝 googlemap_snipet custum adapter end
    Thread timerThread;

    private void startDestroyer(){
        timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(1000);
                        long time = System.currentTimeMillis();
                        if (markers.size() > 0) {
                            for (final Marker marker : markers.keySet()) {
                                if (time - markers.get(marker).getCreateTime() > 10000) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            marker.remove();
                                        }
                                    });
                                    markers.remove(marker);
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        timerThread.start();
    }

    public void Show_dialog_success(String gender, String nickname, String tel, int age){
        final Dialog dialog1 = new Dialog(Have_one_meal_activity.this);
        dialog1.setContentView(R.layout.dialog_success);
        dialog1.setTitle("매칭 성공");
        dialog1.setCanceledOnTouchOutside(false);

        LinearLayout match_success = (LinearLayout) dialog1.findViewById(R.id.match_success);

        TextView textView_gender = (TextView) dialog1.findViewById(R.id.textView_gender);
        TextView textView_age = (TextView) dialog1.findViewById(R.id.textView_age);
        TextView textView_nickname = (TextView) dialog1.findViewById(R.id.textView_nickname);

        LinearLayout call = (LinearLayout) dialog1.findViewById(R.id.call);
        LinearLayout cancel = (LinearLayout) dialog1.findViewById(R.id.cancel);

        textView_gender.setText(gender);
        textView_nickname.setText(nickname);
        textView_age.setText(String.valueOf(2017-age));

        final String finalTel = tel;
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent1 = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + finalTel));
                startActivity(intent1);

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog1.dismiss();

            }
        });

        dialog1.show();
    }

    public void Show_dialog_fail(){
        final Dialog dialog1 = new Dialog(Have_one_meal_activity.this);
        dialog1.setContentView(R.layout.dialog_fail);
        dialog1.setTitle("매칭 실패");
        dialog1.setCanceledOnTouchOutside(false);

        LinearLayout match_fail = (LinearLayout) dialog1.findViewById(R.id.match_fail);

        Button check_fail = (Button) dialog1.findViewById(R.id.check_fail);

        check_fail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog1.dismiss();

            }
        });

        dialog1.show();
    }

    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(
                mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates states = result.getLocationSettingsStates();
                switch(status.getStatusCode()){
                    case LocationSettingsStatusCodes.SUCCESS :
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED :
                        try{

                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE :
                        break;
                }
            }
        });
    }
}


