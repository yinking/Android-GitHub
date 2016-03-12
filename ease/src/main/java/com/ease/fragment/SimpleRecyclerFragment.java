package com.ease.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ease.R;
import com.ease.adapter.BaseAdapter;
import com.ease.model.BaseModel;

/**
 * simple recycler fragment
 * Created by Spencer on 15/11/26.
 */
public abstract class SimpleRecyclerFragment<T extends BaseModel> extends RecyclerFragment<T> {

    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManger;

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler_layout, container, true);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mLayoutManger = createLayoutManager();
        mRecyclerView.setLayoutManager(mLayoutManger);
        mRecyclerView.setAdapter(getAdapter());
        return view;
    }

    @NonNull
    public abstract BaseAdapter<T> createAdapter();


    @NonNull
    public abstract RecyclerView.LayoutManager createLayoutManager();

}
