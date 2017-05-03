package nwpu.cs.com.map.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import java.util.ArrayList;
import java.util.List;

import nwpu.cs.com.map.R;

public class Search extends AppCompatActivity implements OnGetPoiSearchResultListener,OnGetSuggestionResultListener{

//    private PoiSearch mPosiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;

    private AutoCompleteTextView searchkey = null;
    private Button search_button = null;

    private ArrayAdapter<String> sugAdapter = null;

    private String cityname = null;
    private List<String> suggest = null;
    private BDLocation mbdlocation = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Search.java","OnCreate "+this.toString());
        //去除标题栏
        if (getSupportActionBar()!=null) {
            getSupportActionBar().hide();
        }
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_search);
        //获取上一个活动传递过来的地理信息
        mbdlocation = (BDLocation)getIntent().getParcelableExtra("bdlocation_data");
        if(mbdlocation!=null){
            Log.d("Search.java","AcceptIntentSuccess");
            cityname = mbdlocation.getCity();
        }
        cityname = getIntent().getStringExtra("currentCity");
        System.out.print("city"+cityname);
        //初始化搜索模块，注册搜索监听事件
//        mPosiSearch = PoiSearch.newInstance();
//        mPosiSearch.setOnGetPoiSearchResultListener(this);
        //初始化建议搜索模块，注册建议搜索监听事件
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);

        search_button = (Button)findViewById(R.id.search_button);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(searchkey!=null) {
                    Intent intent = new Intent(Search.this, MainActivity.class);
                    intent.putExtra("search_target", searchkey.getText().toString());
                    Log.d("Search.java","sendIntentData: "+searchkey.getText().toString());
//                    startActivity(intent);
                    setResult(RESULT_OK,intent);
                    finish();
                }
            }
        });

        searchkey = (AutoCompleteTextView)findViewById(R.id.searchkey);
        sugAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line);
        searchkey.setAdapter(sugAdapter);
        searchkey.setThreshold(1);

        searchkey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() <= 0){
                    return;
                }

                mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                        .keyword(charSequence.toString())
                        .city(cityname));

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });




    }

    /*
     *获取POI搜索结果
     * 2017／4／15 by standardchar
     */
    @Override
    public void onGetPoiResult(PoiResult result) {

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }
        suggest = new ArrayList<String>();
        for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
            if (info.key != null) {
                suggest.add(info.key);
            }
        }
        sugAdapter = new ArrayAdapter<String>(Search.this, android.R.layout.simple_dropdown_item_1line, suggest);
        searchkey.setAdapter(sugAdapter);
        sugAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
//        mPosiSearch.destroy();
        Log.d("Search.java","OnDestroy");
        mSuggestionSearch.destroy();
        super.onDestroy();
    }
}
