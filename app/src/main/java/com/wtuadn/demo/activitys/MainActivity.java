package com.wtuadn.demo.activitys;

import android.os.Bundle;

import com.wtuadn.demo.containers.MainContainer;
import com.wtuadn.demo.uiframework.BaseActivity;

/**
 * Created by wtuadn on 2017/7/5.
 */

public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            new MainContainer(this, null).setAnim(0, 0, 0, 0).onCreate();
        }
        getWindow().setBackgroundDrawable(null);
    }
}
