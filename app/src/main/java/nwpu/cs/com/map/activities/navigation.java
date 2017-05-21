package nwpu.cs.com.map.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import nwpu.cs.com.map.classes.MyOrientationListener;
import nwpu.cs.com.map.R;
import nwpu.cs.com.map.overlaytuil.TransitRouteOverlay;

public class navigation extends Activity {
    private String Tag = this.getClass().getName();

    private MapView mapView = null;
    private BaiduMap baiduMap = null;
    private ListView ListInformation = null;
    private ImageView imageView = null;
    private Button exit = null;
    private Button music = null;
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
    private RouteStep entrancestep=null;

    private boolean FirstSearch = true;
    private int positon = 0;
    private int AllpassstationNum = 0;
    private int laststationsNum = 0;
    private double distance = 0;
    private List<LatLng> LineAllwaypoints = new ArrayList<>();
    private List<BusLineResult.BusStation> LineAllBusStation = new ArrayList<>();
    private List<RouteNode> entrances = new ArrayList<>();
    //搜索模块
    private RoutePlanSearch routePlanSearch = null;

    //公交线路信息查询
    private BusLineSearch busLineSearch = null;

    //音频播放(未完成)
//    private MediaPlayer mediaPlayer = MediaPlayer.create(navigation.this,R.raw.music1);

    //线程锁
    private static Object lock = new Object();

    private int count = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_navigation);


        SolveView();

        StationNum = (TextView)findViewById(R.id.station_num);
        Distance = (TextView)findViewById(R.id.distance);
        nextstation = (TextView)findViewById(R.id.nextstation);
        mapView = (MapView) findViewById(R.id.navi_map);
        baiduMap = mapView.getMap();
        baiduMap.clear();

        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15f);
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


    private void SolveView(){
        imageView = (ImageView) findViewById(R.id.imagebutton_imformation);
        exit = (Button)findViewById(R.id.exit_navi);
        ListInformation = (ListView)findViewById(R.id.listview_station);
        music = (Button)findViewById(R.id.music_switch);



        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(music.getText().equals("音乐开"))
                    music.setText("音乐关");
                else
                    music.setText("音乐开");
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ListInformation.getVisibility()==View.INVISIBLE)
                    ListInformation.setVisibility(View.VISIBLE);
                else
                    ListInformation.setVisibility(View.INVISIBLE);
            }
        });
    }
    private List<String> getInstructions(){
        List<String> instructions = new ArrayList<>();
        if(LineAllBusStation.isEmpty()){
            return null;
        }
        List<RouteStep> routeStep = route.getAllStep();
        for(int i=0;i<routeStep.size();i++){
            entrances.add(((TransitRouteLine.TransitStep) routeStep.get(i)).getEntrance());
            if(((TransitRouteLine.TransitStep) routeStep.get(i)).getStepType() ==
                    TransitRouteLine.TransitStep.TransitRouteStepType.WAKLING){

                String str = ((TransitRouteLine.TransitStep)routeStep.get(i)).getInstructions();
                if(instructions.size()==0){
                    if(!instructions.contains(str))
                        instructions.add(str);
                    Log.d(Tag,"str1+"+instructions.get(instructions.size()-1));
                }
                else {
                    int len = instructions.size();
                    String str2 = instructions.get(len-1) + "站下车，"+ str;
                    if(!instructions.contains(str2))
                        instructions.add(str2);
                    Log.d(Tag,"str2+"+instructions.get(instructions.size()-1));
                }
            }
            else {
                boolean flag = false;
                double slatitudeError,elatitudeError;
                double slongitudeError,elongitudeError;
                for(int j=0;j<LineAllBusStation.size();j++){
                    elatitudeError = ((TransitRouteLine.TransitStep) routeStep.get(i)).getExit().getLocation().latitude
                            - LineAllBusStation.get(j).getLocation().latitude;
                    elongitudeError = ((TransitRouteLine.TransitStep) routeStep.get(i)).getExit().getLocation().longitude
                            - LineAllBusStation.get(j).getLocation().longitude;
                    if(Math.abs(elatitudeError)<0.001&&Math.abs(elongitudeError)<0.01&&flag){
                        flag = false;
//                        if(i==routeStep.size()-1){
//                            Log.d(Tag,"最后一条线路不是走路");
//                            String str4 = LineAllBusStation.get(j).getTitle()+"下车，到达目的地";
//                            instructions.add(str4);
//                            Log.d(Tag,"str4+"+instructions.get(instructions.size()-1));
//                        }
                    }
                    if(flag){
                        if(!instructions.contains(LineAllBusStation.get(j).getTitle()))
                            instructions.add(LineAllBusStation.get(j).getTitle());
                        Log.d(Tag,"station+"+instructions.get(instructions.size()-1));
                    }
                    slatitudeError = ((TransitRouteLine.TransitStep) routeStep.get(i)).getEntrance().getLocation().latitude
                            - LineAllBusStation.get(j).getLocation().latitude;
                    slongitudeError = ((TransitRouteLine.TransitStep) routeStep.get(i)).getEntrance().getLocation().longitude
                            - LineAllBusStation.get(j).getLocation().longitude;
                    if (Math.abs(slatitudeError)<0.001&&Math.abs(slongitudeError)<0.01&&!flag){
                        flag = true;
                        String str3 = LineAllBusStation.get(j).getTitle()+"站，"
                                + ((TransitRouteLine.TransitStep) routeStep.get(i)).getInstructions();
                        if(!instructions.contains(str3))
                            instructions.add(str3);
                        Log.d(Tag,"str3+"+instructions.get(instructions.size()-1));
                    }

                }
            }
        }
        if(((TransitRouteLine.TransitStep) routeStep.get(routeStep.size()-1)).getStepType() !=
                TransitRouteLine.TransitStep.TransitRouteStepType.WAKLING){
            String str4 = "到达"+((TransitRouteLine.TransitStep) routeStep.get(routeStep.size()-1)).getExit().getTitle()
                    +"请下车";
            instructions.add(str4);
            Log.d(Tag,"str4+"+instructions.get(instructions.size()-1));
        }
//        if(!instructions.contains(LineAllBusStation.get(LineAllBusStation.size()-1).getTitle())){
//            String str4 = LineAllBusStation.get(LineAllBusStation.size()-1).getTitle()+"下车，到达目的地";
//            instructions.add(str4);
//            Log.d(Tag,"str4+"+instructions.get(instructions.size()-1));
//        }
        return instructions;
    }

    private void setListView(){
        List<String> stringList = getInstructions();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(navigation.this,
                android.R.layout.simple_expandable_list_item_1,stringList);
        ListInformation.setAdapter(adapter);
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

            CalculateRoutelineInformation(route);


        }


    }
    private void CalculateRoutelineInformation(final RouteLine routeLine){
        this.AllSteps = routeLine.getAllStep();
        this.route = routeLine;
        laststationsNum = 0;
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

                        if (((TransitRouteLine.TransitStep) routeStep).getStepType() ==
                                TransitRouteLine.TransitStep.TransitRouteStepType.BUSLINE ||
                                ((TransitRouteLine.TransitStep) routeStep).getStepType() ==
                                        TransitRouteLine.TransitStep.TransitRouteStepType.SUBWAY) {
//                            Log.d(Tag, "RouteLine中获取BUSLINE或SUBWAY成功");

                            if(entrancestep == null) {
                                entrancestep = routeStep;
                            }
                            VehicleInfo vehicleInfo = ((TransitRouteLine.TransitStep) routeStep).getVehicleInfo();
                            //获取所有交通路段的总段数
                            if(FirstSearch) {
                                currentStep = (TransitRouteLine.TransitStep) routeStep;
                                AllpassstationNum += vehicleInfo.getPassStationNum();
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
                            else {
                                laststationsNum += vehicleInfo.getPassStationNum();
                            }
                            //获取该路段所有经过的地理坐标
//                            LineAllwaypoints.addAll(routeStep.getWayPoints());


                        }
                    }
                    distance = route.getDistance();
                    if(FirstSearch)
                        laststationsNum = AllpassstationNum;

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(FirstSearch){
                            setListView();
                            FirstSearch = false;
                        }
                        if(laststationsNum!=0) {
                            String str1 = "剩下"+String.valueOf(laststationsNum)+"站";
                            StationNum.setText(str1.toCharArray(),0,str1.length());
                        }

                        if(distance>1000){
                            double len = (distance)/1000;
                            BigDecimal bd = new BigDecimal(len);
                            String str = "剩"+bd.setScale(2,BigDecimal.ROUND_HALF_UP).toString()+"公里";
                            Distance.setText(str.toCharArray(),0,str.length());

                        }
                        else if(distance>0){
                            int len = (int)(distance);
                            String str = "剩"+String.valueOf(len)+"米";
                            Distance.setText(str.toCharArray(),0,str.length());
                        }

                        LatLng latLng = ((TransitRouteLine.TransitStep)entrancestep).getEntrance().getLocation();
                        for(int i=0;i<LineAllBusStation.size();i++) {
                            double lat = latLng.latitude - LineAllBusStation.get(i).getLocation().latitude;
                            double lon = latLng.longitude - LineAllBusStation.get(i).getLocation().longitude;
                            if(Math.abs(lat)<0.001&&Math.abs(lon)<0.01){
                                positon = i;
                            }
                        }
                        nextstation.setText(LineAllBusStation.get(positon).getTitle());
                    }
                });


            }
        }).start();
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

    private void CalculateNaviInformation (BDLocation bdLocation){
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
                CalculateRoutelineInformation(transitRouteResult.getRouteLines().get(0));
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
            //保存导航定位到的位置
            SharedPreferences.Editor editor = getSharedPreferences("data",MODE_APPEND).edit();
            editor.putFloat("latitude"+count,(float) bdLocation.getLatitude());
            editor.putFloat("longitude"+count,(float)bdLocation.getLongitude());
            editor.commit();
            count++;

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

            //定位到所在地点中间,每10次获取定位移动一次
            if(intervaltime%10==0) {
                //经纬度
                LatLng latlng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latlng);
                baiduMap.animateMapStatus(msu);

            }
            intervaltime++;


            if(!LineAllBusStation.isEmpty()) {
                //计算导航信息并刷新到界面上
                CalculateNaviInformation(bdLocation);
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
//                Log.d(Tag, "该线路经过所有点的坐标数量: " + LineAllwaypoints.size());

                RouteNode startNode = currentStep.getEntrance();
                RouteNode endNode = currentStep.getExit();

                boolean flag = false;
                for (BusLineResult.BusStation stations : busStations) {
//                    Log.d(Tag, "车站坐标： 经度：" + stations.getLocation().latitude + " 纬度： " + stations.getLocation().longitude);

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

                    }
                }

                Log.d(Tag, "对比获取公交车站数量： " + LineAllBusStation.size());


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
