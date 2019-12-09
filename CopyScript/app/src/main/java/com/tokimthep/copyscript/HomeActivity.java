package com.tokimthep.copyscript;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tokimthep.copyscript.service.FloatingViewService;

import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends AppCompatActivity {
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private RecyclerView rvCopy;
    private CopyAdapter copyAdapter;
    private Button btAdd,btRun,btClear,btSetTime;
    private EditText etCopy,etTime;
    private List<String> copyList = new ArrayList<>();
    private SharedPref sharedPref;
    private Gson gson = new Gson();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {


            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
            initializeView();
        }
    }

    private void initializeView() {


//        findViewById(R.id.notify_me).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startService(new Intent(HomeActivity.this, FloatingViewService.class));
//                finish();
//            }
//        });
        sharedPref.init(this);

        rvCopy= findViewById(R.id.rvCopy);
        btRun= findViewById(R.id.btRun);
        btAdd= findViewById(R.id.btAdd);
        btClear= findViewById(R.id.btClear);
        btSetTime= findViewById(R.id.btSetTime);
        etCopy= findViewById(R.id.etCopy);
        etTime= findViewById(R.id.etTime);


        int time = sharedPref.read(SharedPref.TIME_COPY,5);
        etTime.setText(String.valueOf(time));

        copyList = gson.fromJson( sharedPref.read(SharedPref.DATA_COPY,"[]"), ArrayList.class);
//        copyList.add("helo");
//        copyList.add("helo");
//        copyList.add("helo");

        rvCopy.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvCopy.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)

//        posts.add(new Post(0, "1 con chó biết nói dị hợm tên Micheal Quái Lọ.", "https://firebasestorage.googleapis.com/v0/b/x-partner-bd11d.appspot.com/o/demo_1.mp4?alt=media&token=4046dba1-03b8-412e-820f-aed098701bd6"));
//        posts.add(new Post(1, "Vào những năm 2018, giữa lòng thủ đô có 1 vương quốc bị lãng quên nằm ven bên hồ Văn Chương. Nơi đó toàn là nghiện và nhiều tệ nạn phát tởm.\n" +
//                "Trớ trêu thay, ở khu dân cư ven hồ ấy đã sinh ra 1 chàng hoàng tử Trần Bơm tài trí hơn người. Chàng có 1 cô em gái là Công chúa lợn Nguyễn Vá. 2 anh em sống trong 1 túp lều của mụ phù thủy gớm ghiếc cùng với 1 con chó biết nói dị hợm tên Micheal Quái Lọ.", null));
//        posts.add(new Post(2, "Anh có vóc dáng cao to khỏe khoắn, ngồi xổm trông như con sư tử đá, đứng lên 1 cái là nhìn to bằng cái tủ lạnh 2 mét!\n" +
//                "Anh hay để kiểu đầu cua khỏe khoắn, cánh tay có xăm con rồng uốn lượn, khuôn mặt bụi bặm, giọng nói nhiều bass đặc sệt sự nam tính, nhìn trông rất chơi.", "https://znews-photo.zadn.vn/w660/Uploaded/mdf_uswreo/2019_01_30/13.jpg"));
        copyAdapter = new CopyAdapter(copyList);
        rvCopy.setAdapter(copyAdapter);


        btSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newText = etCopy.getText().toString();
                if(!newText.isEmpty())
                {
                    int timeCopy = 0;
                    try {
                        timeCopy = Integer.parseInt(newText);
                        if(timeCopy>0) {
                            copyAdapter.addNewValue(newText);
                            sharedPref.write(SharedPref.TIME_COPY, timeCopy);
                        }else{
                            Toast.makeText(HomeActivity.this,
                                    "Thời gian không hợp lệ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        Toast.makeText(HomeActivity.this,
                                "Thời gian không hợp lệ",
                                Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newText = etCopy.getText().toString();
                if(!newText.isEmpty())
                {copyAdapter.addNewValue(newText);
                    etCopy.setText("");
                }
            }
        });

        btRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> dataset = copyAdapter.getDataset();
                if(dataset.size()>0)
                {sharedPref.write(SharedPref.DATA_COPY,gson.toJson(dataset));
                startService();}
                else{
                    Toast.makeText(HomeActivity.this,
                            "Nội dung không thể để trống",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(HomeActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Xoá toàn bộ")
                        .setMessage("Sau khi đồng ý toàn bộ dữ liệu sẽ bị xoá khỏi DB lưu trữ")
                        .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sharedPref.write(SharedPref.DATA_COPY,"[]");
                                copyAdapter.clear();
                            }

                        })
                        .setNegativeButton("Huỷ bỏ", null)
                        .show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) {
                initializeView();
            } else { //Permission is not available
                Toast.makeText(this,
                        "Draw over other app permission not available. Closing the application",
                        Toast.LENGTH_SHORT).show();

                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startService(){
        startService(new Intent(HomeActivity.this, FloatingViewService.class));
        Toast.makeText(this,
                "Bắt đầu chạy tự đông copy",
                Toast.LENGTH_SHORT).show();
        finish();
    }
}
