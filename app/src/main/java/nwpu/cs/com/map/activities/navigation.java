package nwpu.cs.com.map.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.RouteNode;
import com.baidu.mapapi.search.core.RouteStep;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.core.VehicleInfo;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import nwpu.cs.com.map.MyOrientationListener;
import nwpu.cs.com.map.R;
import nwpu.cs.com.map.overlaytuil.TransitRouteOverlay;

public class navigation extends Activity {
    private String Tag = this.getClass().getName();

    private MapView mapView = null;
    private BaiduMap baiduMap = null;
    //定位相关
    private LocationClient mLocationClient;
    private MyLocationListener mLocationListener;
    private int intervaltime = 0;//定位到中心的间隔时间
    private BDLocation mbdlocation = null;
    private double mLatitude;
    private double mLongitude;
    //自定义模式
    private MyLocationConfiguration.LocationMode mLocationMode;

    boolean useDefaultIcon = false;

    // 自定义图标
    private BitmapDescriptor mIconLocation;
    private MyOrientationListener myOrientationListener;
    private float mCurrentX;
    //导航线路
    private RouteLine route = null;

    //导航信息
    private TextView StationNum = null;
    private TextView Distance = null;
    private TextView nextstation = null;
    private TextView popupText = null;//泡泡消息
    private List<RouteStep> AllSteps = null;
    private TransitRouteLine.TransitStep currentStep = null;

    private int positon = -1;
    private int AllpassstationNum = 0;
    private double distance = 0;
    List<LatLng> LineAllwaypoints = new ArrayList<>();
    List<BusLineResult.BusStation> LineAllBusStation = new ArrayList<>();
    //搜索模块
    private RoutePlanSearch routePlanSearch = null;

    //公交线路信息查询
    BusLineSearch busLineSearch = null;

    //线程锁
    private static Object lock = new Object();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_navigation);

        StationNum = (TextView)findViewById(R.id.station_num);
        Distance = (TextView)findViewById(R.id.distance);
        nextstation = (TextView)findViewById(R.id.nextstation);
        mapView = (MapView) findViewById(R.id.navi_map);
        baiduMap = mapView.getMap();
        baiduMap.clear();

        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        baiduMap.setMapStatus(msu);

        initLocation();
        //公交车线路信息搜索
        busLineSearch = BusLineSearch.newInstance();
        busLineSearch.setOnGetBusLineSearchResultListener(onGetBusLineSearchResultListener);
        //线路计划搜索
        routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch.setOnGetRoutePlanResultListener(onGetRoutePlanResultListener);

        //处理intent传送过来的数据
        SloveIntent();


    }

    private void SloveIntent(){

        Intent intent = getIntent();
        route = intent.getParcelableExtra("path_route");
        mbdlocation = intent.getParcelableExtra("bdlocation");

        if(route!=null){
            TransitRouteOverlay overlay = new MyTransitRouteOverlay(baiduMap);
            baiduMap.setOnMarkerClickListener(overlay);
            overlay.setData((TransitRouteLine) route);
            overlay.addToMap();
            overlay.zoomToSpan();


            AllSteps = route.getAllStep();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock){
                        for(RouteStep routeStep : AllSteps){
                            LatLng nodeLocation = ((TransitRouteLine.TransitStep) routeStep).getEntrance().getLocation();
                            String nodeTitle = ((TransitRouteLine.TransitStep) routeStep).getInstructions();

                            if (nodeLocation == null || nodeTitle == null) {
                             continue;
                            }
                            //在地图上显示位置
//                      baiduMap.addOverlay(new MarkerOptions().position(nodeLocation)
//                               .icon(BitmapDescriptorFactory
//                                     .fromResource(R.mipmap.icon_openmap_mark)));
                            if (((TransitRouteLine.TransitStep) routeStep).getStepType() ==
                                  TransitRouteLine.TransitStep.TransitRouteStepType.BUSLINE ||
                                    ((TransitRouteLine.TransitStep) routeStep).getStepType() ==
                                            TransitRouteLine.TransitStep.TransitRouteStepType.SUBWAY) {
                                Log.d(Tag, "RouteLine中获取BUSLINE或SUBWAY成功");
                                currentStep = (TransitRouteLine.TransitStep) routeStep;
                                VehicleInfo vehicleInfo = ((TransitRouteLine.TransitStep) routeStep).getVehicleInfo();
                                //获取所有交通路段的总段数
                                AllpassstationNum += vehicleInfo.getPassStationNum();
                                //获取该路段所有经过的地理坐标
                                LineAllwaypoints.addAll(routeStep.getWayPoints());
                                //搜索该交通路段所乘坐的公交车线路
                                busLineSearch.searchBusLine(new BusLineSearchOption().
                                        city(mbdlocation.getCity()).
                                       uid(vehicleInfo.getUid()));
                                try{
                                    lock.wait();
                                }catch (Exception e){
                                 e.printStackTrace();
                                }
                            }
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(AllpassstationNum!=0) {
                                String str1 = "剩下"+String.valueOf(AllpassstationNum)+"站";
                                StationNum.setText(str1.toCharArray(),0,str1.length());
                            }
                            distance = route.getDistance();
                            if(distance>0){
                                distance = distance/1000;
                                BigDecimal bd = new BigDecimal(distance);
                                String str2 = "剩"+bd.setScale(2,BigDecimal.ROUND_HALF_UP).toString()+"公里";
                                Distance.setText(str2.toCharArray(),0,str2.length());
                            }
                        }
                    });

                }
            }).start();

        }


    }

    private void initLocation() {
        mLocationMode = MyLocationConfiguration.LocationMode.NORMAL;
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mLocationListener);


        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);
        if(!mLocationClient.isStarted())
            mLocationClient.start();
        //自定义图标初始化
        mIconLocation = BitmapDescriptorFactory.fromResource(R.mipmap.arrow);

        myOrientationListener = new MyOrientationListener(navigation.this);

        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX = x;
            }
        });
    }

    private void CalculateDistance (BDLocation bdLocation){
        PlanNode startnode = PlanNode.withLocation(new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude()));
        PlanNode endnode = PlanNode.withCityNameAndPlaceName(bdLocation.getCity(),route.getTerminal().getTitle());
        routePlanSearch.transitSearch((new TransitRoutePlanOption()).from(startnode).to(endnode).city(bdLocation.getCity()));
    }

    private OnGetRoutePlanResultListener onGetRoutePlanResultListener = new OnGetRoutePlanResultListener() {
        @Override
        public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

        }

        @Override
        public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
            if(transitRouteResult.error == SearchResult.ERRORNO.NO_ERROR){
                double length = transitRouteResult.getRouteLines().get(0).getDistance();
                if(length>1000){
                    double len =length/1000;
                    BigDecimal bd = new BigDecimal(len);
                    String str = "剩"+bd.setScale(2,BigDecimal.ROUND_HALF_UP).toString()+"公里";
                    Distance.setText(str.toCharArray(),0,str.length());

                }
                else {
                    int len = (int)length;
                    String str = "剩"+String.valueOf(len)+"米";
                    Distance.setText(str.toCharArray(),0,str.length());
                }
            }
        }

        @Override
        public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

        }

        @Override
        public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

        }

        @Override
        public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

        }
    };

    private class MyLocationListener implements BDLocationListener
    {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            MyLocationData data = new MyLocationData.Builder()//
                    .direction(mCurrentX)
                    .accuracy(bdLocation.getRadius())//
                    .latitude(bdLocation.getLatitude())//
                    .longitude(bdLocation.getLongitude()).build();

            baiduMap.setMyLocationData(data);
            //自定义图标
            MyLocationConfiguration config = new MyLocationConfiguration(mLocationMode,true,mIconLocation);
            baiduMap.setMyLocationConfigeration(config);

            //获取经纬度 为了显示当前位置
            mbdlocation =bdLocation;
            mLatitude = bdLocation.getLatitude();
            mLongitude = bdLocation.getLongitude();

            //定位到所在地点中间,每100次获取定位移动一次
            if(intervaltime%100==0) {
                //经纬度
                LatLng latlng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latlng);
                baiduMap.animateMapStatus(msu);

            }
            intervaltime++;
            double latitudeErorr;
            double longitudeErorr;
            for(int i=0;i<LineAllBusStation.size();i++) {
                latitudeErorr = bdLocation.getLatitude() - LineAllBusStation.get(i).getLocation().latitude;
                longitudeErorr = bdLocation.getLongitude() - LineAllBusStation.get(i).getLocation().longitude;
                if (Math.abs(latitudeErorr) < 0.001 && Math.abs(longitudeErorr) < 0.01) {
                    positon=i;
                }
            }
            if(!LineAllBusStation.isEmpty()) {
                nextstation.setText(LineAllBusStation.get(positon + 1).getTitle().toCharArray(),
                        0,
                        LineAllBusStation.get(positon).getTitle().length());

                String str1 = "剩下" + String.valueOf(AllpassstationNum - positon - 1) + "站";
                StationNum.setText(str1.toCharArray(), 0, str1.length());

                CalculateDistance(bdLocation);
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
        }
    }

    private OnGetBusLineSearchResultListener onGetBusLineSearchResultListener =new OnGetBusLineSearchResultListener() {
        @Override
        public void onGetBusLineResult(BusLineResult busLineResult) {
            synchronized (lock) {
                Log.d(Tag, "搜索BUSLINE成功:" + busLineResult.toString());
                List<BusLineResult.BusStation> busStations = busLineResult.getStations();
                Log.d(Tag, "车站数量： " + busStations.size());
                Log.d(Tag, "该线路经过所有点的坐标数量: " + LineAllwaypoints.size());

                RouteNode startNode = currentStep.getEntrance();
                RouteNode endNode = currentStep.getExit();

                int cnt = 0;
                boolean flag = false;
                for (BusLineResult.BusStation stations : busStations) {
                    Log.d(Tag, "车站坐标： 经度：" + stations.getLocation().latitude + " 纬度： " + stations.getLocation().longitude);

                    double startlatitudeError = startNode.getLocation().latitude - stations.getLocation().latitude;
                    double startlongitudeError = startNode.getLocation().longitude - stations.getLocation().longitude;
                    if (Math.abs(startlatitudeError) < 0.001 && Math.abs(startlongitudeError) < 0.01) {
                        flag = true;
                    }
                    double endlatitudeError = endNode.getLocation().latitude - stations.getLocation().latitude;
                    double endlongitudeError = endNode.getLocation().longitude - stations.getLocation().longitude;
                    if (Math.abs(endlatitudeError) < 0.001 && Math.abs(endlongitudeError) < 0.01) {
                        flag = false;
                    }
                    if (flag) {
                        //在地图上显示站点
                        baiduMap.addOverlay(new MarkerOptions().position(stations.getLocation())
                                .icon(BitmapDescriptorFactory
                                        .fromResource(R.mipmap.icon_openmap_mark)));
                        //记录站点信息
                        LineAllBusStation.add(stations);
                        cnt++;
                    }
                }

                Log.d(Tag, "对比获取公交车站数量： " + cnt);


                //唤醒对象锁继续下一条线路获取
                lock.notify();
            }
        }
    };


    private class MyTransitRouteOverlay extends TransitRouteOverlay {

        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.mipmap.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.mipmap.icon_en);
            }
            return null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //开始定位
        baiduMap.setMyLocationEnabled(true);
        if(!mLocationClient.isStarted())
            mLocationClient.start();
        //开始方向传感器
        myOrientationListener.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(Tag,"OnDestroy");
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
        mLocationClient.unRegisterLocationListener(mLocationListener);
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(Tag,"OnResume");
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //停止定位
        baiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        //停止方向传感器
        myOrientationListener.stop();

    }
}
