package com.ease.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.ease.adapter.BaseAdapter;
import com.ease.data.DataSubscriber;
import com.ease.model.BaseModel;

import java.util.List;


/**
 * recycler fragment
 * Created by Spencer on 15/11/24.
 */
public abstract class RecyclerFragment<T extends BaseModel> extends Fragment implements DataSubscriber.DataActionListener<T> {

    private BaseAdapter<T> mAdapter;

    @NonNull
    public abstract BaseAdapter<T> createAdapter(Context context);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = createAdapter(getActivity());
        mAdapter.addDataActionListener(this);
    }

    public BaseAdapter<T> getAdapter() {
        return mAdapter;
    }

    @Override
    public void onInitStart() {

    }

    @Override
    public void onInitDone(Throwable e, List<T> data) {

    }

    @Override
    public void onLoadMoreStart() {

    }

    @Override
    public void onLoadMoreDone(Throwable e, List<T> data) {

    }

    @Override
    public void onRefreshStart() {

    }

    @Override
    public void onRefreshDone(Throwable e, List<T> data) {

    }

    @Override
    public void onEnd() {

    }
}
