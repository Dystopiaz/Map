package nwpu.cs.com.map.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewDebug;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nwpu.cs.com.map.R;

public class SelectCity extends AppCompatActivity implements MKOfflineMapListener{

    private BDLocation mbdlocation = null;
    private String cityname = null;
    private Button mbutton = null;
    private ListView allCityListView;

    private MKOfflineMap mkOffline = null;
    private ArrayList<MKOLSearchRecord> allList;
    private ArrayList<MKOLSearchRecord> currentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        //获取上一个活动传递过来的地理信息
        mbdlocation = getIntent().getParcelableExtra("bdlocation_data");
        if(mbdlocation!=null){
            cityname = mbdlocation.getCity();
        }
        mkOffline = new MKOfflineMap();
        mkOffline.init(this);
        initView();

    }

    protected void initView(){
        allList = mkOffline.getOfflineCityList();
        currentList = allList;
        drawView();

        allCityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentList.get(i).cityType == 2){
                    Intent intent = new Intent(SelectCity.this, MainActivity.class);
                    intent.putExtra("SelectCityResult", currentList.get(i).cityName);
                    setResult(2,intent);
                    finish();
                }
                if(currentList.get(i).cityType == 1){
                    currentList = currentList.get(i).childCities;
                    drawView();
                }
            }
        });
        mbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initView();
            }
        });


    }

    protected void drawView(){
        allCityListView = (ListView) findViewById(R.id.allcitylist);
        mbutton = (Button) findViewById(R.id.mButton);
        // 获取所有支持离线地图的城市
        ArrayList<String> allCityStr = new ArrayList<String>();
        if (currentList != null) {
            for (MKOLSearchRecord r : currentList) {
                allCityStr.add(r.cityName + "(" + r.cityType + ")");
            }
        }
        ListAdapter aAdapter = (ListAdapter) new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, allCityStr);
        allCityListView.setAdapter(aAdapter);
    }


    @Override
    public void onGetOfflineMapState(int i, int i1) {

    }
}
