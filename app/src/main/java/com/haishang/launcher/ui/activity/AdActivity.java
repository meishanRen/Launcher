package com.haishang.launcher.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.haishang.launcher.Constant;
import com.haishang.launcher.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class AdActivity extends Activity {
    private TextView countdown;
    private ImageView ad;
    private int count = 5;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_ad);
        ad = (ImageView) findViewById(R.id.ad);
        countdown = (TextView) findViewById(R.id.textView);
        countdown.setText(count + "");
        //  animation = AnimationUtils.loadAnimation(this, R.anim.anim_count_down);
        //countdown.startAnimation(animation);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean showAd = sp.getBoolean(Constant.IS_SHOW_AD, false);
        String adImagePath = this.getCacheDir().getAbsolutePath() + "/" + Constant.AD_IMAGE_NAME;


        if (showAd && new File(adImagePath).exists()) {
            Picasso.with(this).load("file://" + adImagePath).into(ad);
            handler.sendEmptyMessageDelayed(0, 1000);
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }


    }

    private int getCount() {
        count--;
        if (count <= 0) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            handler.removeMessages(0);
            finish();
        }
        return count;
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {
                handler.sendEmptyMessageDelayed(0, 1000);
                countdown.setText(getCount() + "");
                //     animation.reset();
                //     countdown.startAnimation(animation);
            }
        }
    };

}