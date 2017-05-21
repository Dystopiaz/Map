package nwpu.cs.com.map.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import nwpu.cs.com.map.R;

public class HeatMap extends Activity {

    private MapView mapView;
    private BaiduMap baiduMap;
    private com.baidu.mapapi.map.HeatMap heatmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_heat_map);
        mapView = (MapView) findViewById(R.id.mapview);
        baiduMap = mapView.getMap();
        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(12));

        SharedPreferences preferences = getSharedPreferences("data",MODE_APPEND);
        List<LatLng> latLngList = new ArrayList<>();
        for(int i=0;;i++){
            double latitude = preferences.getFloat("latitude"+i,0);
            double longitude = preferences.getFloat("longitude"+i,0);
            if(latitude!=0&&longitude!=0){
                latLngList.add(new LatLng(latitude,longitude));
            }
            else
                break;
        }
        heatmap = new com.baidu.mapapi.map.HeatMap.Builder().data(latLngList).build();
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLngList.get(0));
        baiduMap.animateMapStatus(msu);
    }
    @Override
    protected void onPause() {
        super.onPause();
        // activity 暂停时同时暂停地图控件
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // activity 恢复时同时恢复地图控件
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // activity 销毁时同时销毁地图控件
        mapView.onDestroy();
    }
}
