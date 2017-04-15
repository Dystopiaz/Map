package nwpu.cs.com.map;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.search.poi.PoiSearch;

public class Path extends AppCompatActivity {

    private PoiSearch mPosiSearch;
    private Button search_button;
    private EditText startpoint;
    private EditText terminalpoint;

    private String startpointmessage;
    private String terminalpointmessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path);

        startpoint = (EditText)findViewById(R.id.StartPoint);
        terminalpoint = (EditText)findViewById(R.id.TerminalPoint);
        search_button = (Button)findViewById(R.id.search_button);

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startpointmessage = startpoint.getText().toString();
                terminalpointmessage = terminalpoint.getText().toString();
                if(terminalpointmessage.isEmpty()){//判断是否有填写目的地
                    Toast.makeText(Path.this,"请填写目的地",Toast.LENGTH_SHORT).show();
                }
                else{//执行搜索操作

                }
            }
        });
    }
}
