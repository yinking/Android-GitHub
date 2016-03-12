package com.ease.data;


import com.ease.model.BaseModel;

import java.util.List;

import rx.Observable;

/**
 * data action interface
 * Created by Spencer on 15/10/28.
 */
public interface DataActionInterface<M extends BaseModel> {

    Observable<List<M>> doInitialize();

    Observable<List<M>> doRefresh();

    Observable<List<M>> doLoadMore();
}
