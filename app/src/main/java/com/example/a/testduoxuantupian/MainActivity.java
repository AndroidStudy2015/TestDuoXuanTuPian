package com.example.a.testduoxuantupian;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 2;
    private static final int REQUEST_IMAGE_CROP = 3;
    private static final int REQUEST_OPEN_PREVIEW = 1;
    ArrayList<String> mSelectPath = new ArrayList<>();
    private Button mBtOpen;
    private TextView mTv;
    private Intent intent;
    private GridView mGridView;
    private ImageView mIvCrop;
    private ImageAdapter adapter;
    private Button mBtSave;

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
        mBtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "mSelectPath.size()" + mSelectPath.size(), Toast.LENGTH_LONG).show();

                for (int i = 0; i < mSelectPath.size(); i++) {

                    final int finalI = i;
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            Log.e("a", finalI + "");
                            Log.e("压缩后的图片的路径：", ImageUtils.saveBitmap(MainActivity.this, mSelectPath.get(finalI)));
//                                    // TODO: 2016/3/31 拿到path后，可以把压缩后的图片上传到服务器
                        }
                    }.start();

                }
            }

        });
        mIvCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initIntent();
                intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);
                startActivityForResult(intent, REQUEST_IMAGE_CROP);
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
        mBtSave = (Button) findViewById(R.id.bt_save);
        mIvCrop = (ImageView) findViewById(R.id.iv_icon);

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
        } else if (requestCode == REQUEST_IMAGE_CROP) {
            if (resultCode == RESULT_OK) {
                mSelectPath = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);

//             由于裁剪图片一般知识得到一张图片，所以get(0)得到所选图片的path，
//              根据Uri.fromFile(file)方法即可将path转为uri
                Uri sourceUri = Uri.fromFile(new File(mSelectPath.get(0)));
//               创建裁剪照片之后保存的路径，也是先用path--->file--->Uri
                String saveDir = Environment.getExternalStorageDirectory()
                        + "/crop";
                File dir = new File(saveDir);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                Uri destinationUri = Uri.fromFile(new File(saveDir, "crop.jpg"));
                UCrop.of(sourceUri, destinationUri)
//                        .withAspectRatio(16, 9)
                        .withMaxResultSize(900, 900)
                        .start(MainActivity.this);
            }
        }else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            Toast.makeText(MainActivity.this,"laile",Toast.LENGTH_SHORT).show();
//            必须首先设为null，否则更新照片之后设置的为就图片
            mIvCrop.setImageURI(null);
            mIvCrop.setImageURI(resultUri);
            mSelectPath.clear();
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }

    }
}
