package com.wtuadn.demo.containers;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wtuadn.demo.uiframework.BaseActivity;
import com.wtuadn.demo.uiframework.BaseContainer;

public class MainContainer extends BaseContainer {
    public static int count;
    private int c;
    private TextView button;

    public MainContainer(BaseActivity context, Bundle state) {
        super(context, state);
    }

    @Override
    public View onCreateView() {
        button = new Button(getContext());
        c = count;
        button.setText("" + count++);
        button.setTextColor(Color.WHITE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TestContainer2((BaseActivity) getContext(), null).onCreate();
            }
        });
        button.setBackgroundColor(Color.DKGRAY);
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
