package nwpu.cs.com.map.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.baidu.location.BDLocation;

import nwpu.cs.com.map.R;

public class SelectCity extends AppCompatActivity {

    private BDLocation mbdlocation = null;
    private String cityname = null;
    private Button select_city_ok_button = null;
    private EditText search_city_text = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);
        //获取上一个活动传递过来的地理信息
        mbdlocation = getIntent().getParcelableExtra("bdlocation_data");
        if(mbdlocation!=null){
            cityname = mbdlocation.getCity();
        }
        search_city_text = (EditText)findViewById(R.id.search_city_text);
        select_city_ok_button = (Button)findViewById(R.id.select_city_ok_button);
        select_city_ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(search_city_text!=null) {
                    Intent intent = new Intent(SelectCity.this, MainActivity.class);
                    intent.putExtra("SelectCityResult",search_city_text.getText().toString());
                    setResult(2,intent);
                    finish();
                }
            }
        });

    }
}
