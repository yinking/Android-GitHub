package com.ease.holder;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ease.model.BaseModel;

/**
 * item base holder
 * Created by Spencer on 15/11/26.
 */
public abstract class BaseHolder<M extends BaseModel> extends RecyclerView.ViewHolder {

    public BaseHolder(View itemView) {
        super(itemView);
    }

    public abstract void binding(M data);

    @NonNull
    public String getType() {
        return this.getClass().getName();
    }
}
