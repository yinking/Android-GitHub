package com.ease.fragment;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * easy fragment
 * Created by Spencer on 15/10/28.
 */
public abstract class EasyFragment extends Fragment {

    @CallSuper
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        onInitialize();
    }

    public abstract void onInitialize();

}
