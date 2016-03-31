package com.example.a.testduoxuantupian;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {


    private ArrayList<String> mImgPths;
    private LayoutInflater mInflater;
    private Context mContext;


    /**
     * 构造函数
     *
     * @param context
     * @param mDatas  图片路径
     */
    public ImageAdapter(Context context, ArrayList<String> mDatas) {
        this.mContext = context;
        this.mImgPths = mDatas;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mImgPths.size();
    }

    @Override
    public Object getItem(int position) {
        return mImgPths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_gridview, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mImg = (ImageView) convertView.findViewById(R.id.id_item_image);
            //        首先获取要显示图片的imageview的高、宽,便于picasso加载图片时，设置缩放比例

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

/*//        使用自己写的图片加载类压缩加载图片
        ImageLoader.getInstance(3, ImageLoader.Type.LIFO).
                loadImage(mImgPths.get(position), viewHolder.mImg,2);*/
        ImageSize imageViewSize = ImageUtils.getImageViewSize(viewHolder.mImg);
        //使用Picasso加载图片
        Picasso.with(mContext)
                .load(new File(mImgPths.get(position)))
                .resize(imageViewSize.width, imageViewSize.width).centerInside()
                .into(viewHolder.mImg);


        return convertView;
    }

    private class ViewHolder {
        ImageView mImg;
    }
}