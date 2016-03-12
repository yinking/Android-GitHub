package com.ease.data;


import com.ease.model.BaseModel;

import java.util.List;

/**
 * data subscriber
 * Created by Spencer on 15/10/28.
 */
public class DataSubscriber<M extends BaseModel> {

    public interface DataActionListener<M> {
        void onInitStart();

        void onInitDone(Throwable e, List<M> data);

        void onRefreshStart();

        void onRefreshDone(Throwable e, List<M> data);

        void onLoadMoreStart();

        void onLoadMoreDone(Throwable e, List<M> data);

        void onEnd();
    }

    public interface DataChangeListener<M> {
        void onDataChange();
    }
}
