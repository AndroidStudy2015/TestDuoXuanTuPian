package com.example.a.testduoxuantupian;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 2;
    private static final int REQUEST_OPEN_PREVIEW = 1;
    ArrayList<String> mSelectPath = new ArrayList<>();
    private Button mBtOpen;
    private TextView mTv;
    private Intent intent;
    private GridView mGridView;
    private ImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("照片多选Demo");

        initView();

        setListener();


    }

    private void setListener() {
        mBtOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initIntent();
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent openPreviewIntent = new Intent(MainActivity.this, PreviewPicActivity.class);
                openPreviewIntent.putExtra("mImgPths", mSelectPath);
                openPreviewIntent.putExtra("position", position);
                startActivityForResult(openPreviewIntent, REQUEST_OPEN_PREVIEW);
            }
        });
    }

    private void initIntent() {
        intent = new Intent(MainActivity.this, MultiImageSelectorActivity.class);
        // 是否显示调用相机拍照
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        // 最大图片选择数量
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 9);
        // 设置模式 (支持 单选/MultiImageSelectorActivity.MODE_SINGLE 或者 多选/MultiImageSelectorActivity.MODE_MULTI)
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
//        // 默认选择图片,回填选项(支持String ArrayList)
//        intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mSelectPath);
        // 默认选择
        if (mSelectPath != null && mSelectPath.size() > 0) {
            intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mSelectPath);
        }
    }

    private void initView() {
        mBtOpen = (Button) findViewById(R.id.bt_open);
        mTv = (TextView) findViewById(R.id.tv);
        mGridView = (GridView) findViewById(R.id.grid);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                mSelectPath = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                StringBuilder sb = new StringBuilder();
                for (String p : mSelectPath) {
                    sb.append(p);
                    sb.append("\n");
                }
                mTv.setText(sb.toString());
                adapter = new ImageAdapter(MainActivity.this, mSelectPath);
                mGridView.setAdapter(adapter);

            }
        } else if (requestCode == REQUEST_OPEN_PREVIEW) {
            if (resultCode == RESULT_OK) {
                mSelectPath = data.getStringArrayListExtra("mImgPths");
                Toast.makeText(MainActivity.this, "shou", Toast.LENGTH_SHORT).show();
                adapter = new ImageAdapter(MainActivity.this, mSelectPath);
                mGridView.setAdapter(adapter);
            }
        }
    }

}
