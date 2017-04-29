package nwpu.cs.com.map.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.baidu.mapapi.map.offline.MKOfflineMap;

import nwpu.cs.com.map.R;

public class SelectCity extends AppCompatActivity {
    private MKOfflineMap mkOfflineMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
    }
}
