package com.haishang.launcher.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyViewPager extends ViewPager {
  
    private boolean scrollble = true;  
  
    public MyViewPager(Context context) {
        super(context);  
    }  
  
    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);  
    }  
  
  
    @Override  
    public boolean onTouchEvent(MotionEvent ev) {
        if (!scrollble) {  
            return false;
        }  
        return super.onTouchEvent(ev);  
    }
    public void setScanScroll(boolean isCanScroll){
        this.scrollble = isCanScroll;
    }


    @Override
    public void scrollTo(int x, int y){
        if (scrollble){
            super.scrollTo(x, y);
        }
    }


    public boolean isScrollble() {  
        return scrollble;  
    }  
  
    public void setScrollble(boolean scrollble) {  
        this.scrollble = scrollble;  
    }  
}  