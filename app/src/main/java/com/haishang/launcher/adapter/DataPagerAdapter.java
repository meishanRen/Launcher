package com.haishang.launcher.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.haishang.launcher.widget.PosterPageLayout;

import java.util.List;

public class DataPagerAdapter<T> extends PagerAdapter {
    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private List<PosterPageLayout> mList = null;

    public DataPagerAdapter(Context context, List<PosterPageLayout> list) {
        mContext = context;
        mList = list;
     //   mList.addAll(mList);
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
        //return mList.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }
/*
    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
         container.removeView((View) mList.get(position));
    }*/

/*

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView((View) mList.get(position));
        return mList.get(position);
    }
*/

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //对ViewPager页号求模取出View列表中要显示的项
        int pos = position;
        Log.d(TAG, "positon=" + position);
        position %= mList.size();
        if (position < 0) {
            position = mList.size() + position;
        }
        Log.d(TAG, "positon=" + position);
        PosterPageLayout view = (PosterPageLayout) mList.get(position);
        //如果View已经在之前添加到了一个父组件，则必须先remove，否则会抛出IllegalStateException。
        ViewParent vp = view.getParent();
        if (vp != null) {
            ViewGroup parent = (ViewGroup) vp;
            parent.removeView(view);
            view.cleanFocusAttr();
            Log.d(TAG, "pos=" + pos + "parent is true" +
                    "");
        }
        container.addView(view);
        //add listeners here if necessary
        return view;
    }
}
