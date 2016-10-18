package com.haishang.launcher.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DigitalClockView extends TextView {

    Calendar mCalendar;
    private FormatChangeObserver mFormatChangeObserver;

    private Runnable mTicker;
    private Handler mHandler;

    private boolean mTickerStopped = false;

    String mFormat;

    public DigitalClockView(Context context) {
        super(context);
        initClock();
    }

    public DigitalClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initClock();
    }

    private void initClock() {
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }

        mFormatChangeObserver = new FormatChangeObserver();
        getContext().getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver);

        setFormat();
//        AssetManager mAm=getResources().getAssets();
//        if(mAm!=null){
//        	 Typeface tf=Typeface.createFromAsset(mAm, "fonts/led.ttf");//根据路径得到Typeface
//             setTypeface(tf);
//        }
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onAttachedToWindow() {
        mTickerStopped = false;
        super.onAttachedToWindow();
        mHandler = new Handler();

        /**
         * requests a tick on the next hard-second boundary
         */
        mTicker = new Runnable() {
            public void run() {
                if (mTickerStopped) return;
                mCalendar.setTimeInMillis(System.currentTimeMillis());
                SimpleDateFormat dataFormat = new SimpleDateFormat(mFormat);
                setText(dataFormat.format(mCalendar.getTime()));
                invalidate();
                long now = SystemClock.uptimeMillis();
                long next = now + (1000 - now % 1000);
                mHandler.postAtTime(mTicker, next);
            }
        };
        mTicker.run();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTickerStopped = true;
    }

    private void setFormat() {
        mFormat = "HH:mm";
    }

    public void setFormant(String _format) {
        mFormat = String.valueOf(_format);
    }

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            setFormat();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        //noinspection deprecation
        event.setClassName(DigitalClockView.class.getName());
    }

    @SuppressLint("NewApi")
    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        //noinspection deprecation
        info.setClassName(DigitalClockView.class.getName());
    }
}
