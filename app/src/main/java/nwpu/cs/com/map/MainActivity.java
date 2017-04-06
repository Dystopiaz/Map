package nwpu.cs.com.map;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import static com.baidu.mapapi.map.MyLocationConfiguration.*;


public class MainActivity extends AppCompatActivity {
    //
    //地图
    private MapView mMapView = null;
    private BaiduMap mBaiduMap = null;
    private Context context;
    //定位相关
    private LocationClient mLocationClient;
    private MyLocationListener mLocationListener;
    private boolean isFirstIn = true;//第一次定位设置在中心
    private double mLatitude;
    private double mLongitude;
    // 自定义图标
    private BitmapDescriptor mIconLocation;
    private MyOrientationListener myOrientationListener;
    private float mCurrentX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        //if (getSupportActionBar()!=null) {
        //    getSupportActionBar().hide();
        //}
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        this.context=this;
        //地图设置初始化
        initView();

        //初始化定位
        initLocation();
    }

    private void initLocation() {
        mLocationClient = new LocationClient(context);
        mLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mLocationListener);

        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);
        //自定义图标初始化
        mIconLocation = BitmapDescriptorFactory.fromResource(R.mipmap.arrow);

        myOrientationListener = new MyOrientationListener(context);

        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });
    }

    private void initView() {
        //初始化内容
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        //地图初始放大比例
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //开始定位
        mBaiduMap.setMyLocationEnabled(true);
        if(!mLocationClient.isStarted())
            mLocationClient.start();
        //开始方向传感器
        myOrientationListener.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //停止定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        //停止方向传感器
        myOrientationListener.stop();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //创建菜单栏
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate( R.menu.options_menu , menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //设计菜单栏，实现卫星地图、普通地图和实时交通的切换
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menu_common:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.menu_site:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.menu_traffic:
                if (mBaiduMap.isTrafficEnabled()) {
                    mBaiduMap.setTrafficEnabled(false);
                    item.setTitle("实时交通(off)");
                } else {
                    mBaiduMap.setTrafficEnabled(true);
                    item.setTitle("实时交通(on)");
                }
                break;
            case R.id.menu_location:
                LatLng latlng = new LatLng(mLatitude,mLongitude);
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latlng);
                mBaiduMap.animateMapStatus(msu);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyLocationListener implements BDLocationListener
    {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            MyLocationData data = new MyLocationData.Builder()//
                    .direction(mCurrentX)
                    .accuracy(bdLocation.getRadius())//
                    .latitude(bdLocation.getLatitude())//
                    .longitude(bdLocation.getLongitude()).build();

            mBaiduMap.setMyLocationData(data);
            //自定义图标
            MyLocationConfiguration config = new MyLocationConfiguration(LocationMode.NORMAL,true,mIconLocation);
            mBaiduMap.setMyLocationConfigeration(config);

            //获取经纬度 为了显示当前位置
            mLatitude = bdLocation.getLatitude();
            mLongitude = bdLocation.getLongitude();
            //第一次将地图显示在定位中心
            if (isFirstIn)
            {
                //经纬度
                LatLng latlng = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latlng);
                mBaiduMap.animateMapStatus(msu);
                isFirstIn = false;

                Toast.makeText(context,bdLocation.getAddrStr(),Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
        }
    }


}
