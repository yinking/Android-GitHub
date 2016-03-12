package com.ease.data;


import com.ease.model.BaseModel;

import java.util.List;

/**
 * Simple data subscriber
 * Created by Spencer on 15/10/28.
 */
public class SimpleDataSubscriber<M extends BaseModel>
        implements DataSubscriber.DataActionListener<M>, DataSubscriber.DataChangeListener<M> {

    @Override
    public void onInitStart() {

    }

    @Override
    public void onInitDone(Throwable e, List<M> data) {

    }

    @Override
    public void onLoadMoreStart() {

    }

    @Override
    public void onLoadMoreDone(Throwable e, List<M> data) {

    }

    @Override
    public void onRefreshStart() {

    }

    @Override
    public void onRefreshDone(Throwable e, List<M> data) {

    }

    @Override
    public void onEnd() {

    }

    @Override
    public void onDataChange() {

    }
}
