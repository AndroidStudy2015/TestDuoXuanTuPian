package com.example.a.testduoxuantupian;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

public class PreviewPicActivity extends AppCompatActivity {

    private HackyViewPager viewPager;
    /**
     * 外部传递进来的position，即点击外面的gridview的那个position
     */
    private int mOutTransitPosition;
    /**
     * 当前ViewPager显示的图片的position
     */
    private int mCurrentPosition;
    private ArrayList<String> mImgPths;
    MyPagerAdapter adapter;
    View currentItem;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_pic);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        mImgPths = intent.getStringArrayListExtra("mImgPths");
        mOutTransitPosition = intent.getIntExtra("position", -1);


        viewPager = (HackyViewPager) findViewById(R.id.viewpager);
        adapter = new MyPagerAdapter();
        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(mOutTransitPosition, true);
        mCurrentPosition=mOutTransitPosition;
        toolbar.setTitle(String.format("%d/%d",
                mOutTransitPosition + 1, mImgPths.size()));

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                toolbar.setTitle(String.format("%d/%d",
                        position + 1, mImgPths.size()));
                mCurrentPosition = position;

                Toast.makeText(PreviewPicActivity.this, "删除" + position+1, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_delete) {
            mImgPths.remove(mCurrentPosition);
                    toolbar.setTitle(String.format("%d/%d",
                            mCurrentPosition + 1, mImgPths.size()));
                    adapter.notifyDataSetChanged();
                    if (mImgPths.size() == 0) {
                        setImgPathResult();
                    }

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
//            当点击回退键时，把现在经过删除操作后的数组传递给Mainactivity,让其去改变gridview
            setImgPathResult();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setImgPathResult() {
        Intent intent = new Intent();
        intent.putExtra("mImgPths", mImgPths);
        setResult(RESULT_OK, intent);
        finish();
    }


    class MyPagerAdapter extends PagerAdapter {



        @Override
        public int getCount() {
            return mImgPths.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View view = View
                    .inflate(PreviewPicActivity.this, R.layout.item_preview_pic_viewpager, null);

            final PhotoView photoView = (PhotoView) view.findViewById(R.id.photoView);


            ImageLoader.getInstance().loadImage(mImgPths.get(position), photoView);

            photoView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(PreviewPicActivity.this, "删除" + position, Toast.LENGTH_SHORT).show();
                    mImgPths.remove(position);
                    toolbar.setTitle(String.format("%d/%d",
                            position + 1, mImgPths.size()));
                    notifyDataSetChanged();
                    if (mImgPths.size() == 0) {
                        setImgPathResult();
                    }
                    return true;
                }
            });
            // ★★★这句话很重要！！！别忘了写！！！
            ((ViewPager) container).addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position,
                                Object object) {
            // ★★★这句话很重要！！！别忘了写！！！
            ((ViewPager) container).removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
//                不返回POSITION_NONE，删除了页面也不会更新UI
            return POSITION_NONE;
        }


    }
}
