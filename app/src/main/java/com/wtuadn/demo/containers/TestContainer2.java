package com.wtuadn.demo.containers;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wtuadn.demo.uiframework.BaseActivity;
import com.wtuadn.demo.uiframework.BaseContainer;


/**
 * Created by wtuadn on 2017/6/23.
 */

public class TestContainer2 extends BaseContainer {
    private int c;
    private TextView button;

    public TestContainer2(BaseActivity context, Bundle state) {
        super(context, state);
    }

    @Override
    public View onCreateView() {
        button = new Button(getContext());
        c = MainContainer.count;
        button.setText("" + MainContainer.count++);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MainContainer((BaseActivity) getContext(), null).onCreate();
            }
        });
        button.setBackgroundColor(Color.LTGRAY);
        return button;
    }

    @Override
    public Bundle onSave() {
        Bundle bundle = super.onSave();
        if (bundle.isEmpty()) {
            bundle.putInt("1", c);
        }
        return bundle;
    }

    @Override
    public void onRestore(Bundle state) {
        button.setText("" + state.getInt("1"));
    }
}
