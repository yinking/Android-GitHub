package com.ease.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.ease.data.DataController;
import com.ease.data.DataSubscriber;
import com.ease.model.BaseModel;

import java.util.List;

/**
 * base adapter
 * Created by Spencer on 15/10/28.
 */
public abstract class BaseAdapter<M extends BaseModel> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements DataSubscriber.DataChangeListener<M>, DataSubscriber.DataActionListener<M> {

    /**
     * 默认的 common 类型
     */
    public static final int DEFAULT_COMMON_TYPE = 99999;

    /**
     * 默认的 random 类型
     */
    public static final int DEFAULT_RANDOM_TYPE = 88888;

    private enum HOLDER_TYPE {
        HEADER, RANDOM, COMMON, FOOTER
    }

    public interface HeaderDelegate {

        RecyclerView.ViewHolder onCreateHeader(ViewGroup parent, int viewType);

        void onBindHeader(RecyclerView.ViewHolder holder, int position);

        int getHeaderCount();

        int getHeaderType(int position);

        boolean isHeaderType(int viewType);
    }

    public interface RandomDelegate {
        RecyclerView.ViewHolder onCreateRandom(ViewGroup parent, int viewType);

        void onBindRandom(RecyclerView.ViewHolder holder, int position);

        int getRandomType();

        boolean isRandomType(int viewType);

        int getRandomPosition();
    }

    public interface FooterDelegate {

        int getFooterCount();

        RecyclerView.ViewHolder onCreateFooter(ViewGroup parent, int viewType);

        void onBindFooter(RecyclerView.ViewHolder holder, int position);

        boolean isFooterType(int viewType);

        int getFooterType(int position);
    }

    @NonNull
    public abstract DataController<M> createDataController();

    private Context mContext;
    private DataController<M> mDataController;

    private HeaderDelegate mHeaderDelegate;
    private RandomDelegate mRandomDelegate;
    private FooterDelegate mFooterDelegate;

    private int mRandomPosition = -1;

    public BaseAdapter(Context context) {
        mContext = context;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mDataController = createDataController();
        addDataActionListener(this);
        addDataChangeListeners(this);
        mDataController.initialize();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        removeDataActionListener(this);
        removeDataChangeListeners(this);
        mDataController.getData().clear();
    }

    public Context getContext() {
        return mContext;
    }

    public DataController<M> getDataController() {
        return mDataController;
    }

    public void setHeaderDelegate(HeaderDelegate delegate) {
        mHeaderDelegate = delegate;
        notifyDataSetChanged();
    }

    public void setRandomDelegate(RandomDelegate delegate) {
        mRandomDelegate = delegate;
    }

    public void setFooterDelegate(FooterDelegate delegate) {
        mFooterDelegate = delegate;
        notifyDataSetChanged();
    }

    public void refresh() {
        mDataController.refresh();
    }

    public void loadMore() {
        mDataController.loadMore();
    }

    /**
     * 获取总共条目个数,其中包含 Header, Random, Comment, Footer 的条目个数总和
     */
    @Override
    public int getItemCount() {
        return getHeaderCount() + getCommonItemCount() + getFooterCount() + getRandomCount();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getHeaderCount() {
        return mHeaderDelegate == null ? 0 : mHeaderDelegate.getHeaderCount();
    }

    private int getRandomCount() {
        return mRandomDelegate == null ? 0 : (getDataController() != null && getDataController().getData().size() > mRandomDelegate.getRandomPosition() ? 1 : 0);
    }

    public int getFooterCount() {
        return mFooterDelegate == null ? 0 : mFooterDelegate.getFooterCount();
    }

    public int getCommonItemCount() {
        return (mDataController == null || mDataController.getData() == null) ? 0 : mDataController.getData().size();
    }

    public boolean hasHeader() {
        return mHeaderDelegate != null && mHeaderDelegate.getHeaderCount() != 0;
    }

    public boolean hasRandom() {
        return mRandomDelegate != null;
    }

    public boolean hasFooter() {
        return mFooterDelegate != null && mFooterDelegate.getFooterCount() != 0;
    }

    public abstract int getCommonType(int position);

    public abstract RecyclerView.ViewHolder onCreateCommon(ViewGroup parent, int viewType);

    public abstract void onBindCommon(RecyclerView.ViewHolder holder, M item);

    @Override
    public final int getItemViewType(int position) {
        int headerCount = getHeaderCount();
        if (position < headerCount) {
            return mHeaderDelegate.getHeaderType(position);
        }

        if (hasRandom() && position >= headerCount && mRandomDelegate.getRandomPosition() == position - headerCount + 1) {
            mRandomPosition = position;
            return mRandomDelegate.getRandomType();
        }

        if (hasFooter() && position >= headerCount + getCommonItemCount() + getRandomCount()) {
            return mFooterDelegate.getFooterType(position - headerCount - getCommonItemCount() - getRandomCount());
        }

        int p = position - headerCount - ((hasRandom() && mRandomPosition != -1 && position > mRandomPosition) ? 1 : 0);
        return getCommonType(p);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (hasHeader() && mHeaderDelegate.isHeaderType(viewType)) {
            return mHeaderDelegate.onCreateHeader(parent, viewType);
        }

        if (hasRandom() && mRandomDelegate.isRandomType(viewType)) {
            return mRandomDelegate.onCreateRandom(parent, viewType);
        }

        if (hasFooter() && mFooterDelegate.isFooterType(viewType)) {
            return mFooterDelegate.onCreateFooter(parent, viewType);
        }

        return onCreateCommon(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemType(position)) {
            case HEADER:
                mHeaderDelegate.onBindHeader(holder, position);
                break;
            case RANDOM:
                mRandomDelegate.onBindRandom(holder, position);
                break;
            case FOOTER:
                mFooterDelegate.onBindFooter(holder, position - getHeaderCount() - getRandomCount() - getCommonItemCount());
                break;
            case COMMON:
                int p = position - getHeaderCount() - ((hasRandom() && mRandomPosition != -1 && position > mRandomPosition) ? 1 : 0);
                onBindCommon(holder, mDataController.getData().get(p));
                break;
        }

        if (position == getItemCount() - 1) {
            loadMore();
        }
    }

    public HOLDER_TYPE getItemType(int position) {
        int type = getItemViewType(position);

        if (hasHeader() && mHeaderDelegate.isHeaderType(type)) {
            return HOLDER_TYPE.HEADER;
        }
        if (hasRandom() && mRandomDelegate.isRandomType(type)) {
            return HOLDER_TYPE.RANDOM;
        }

        if (hasFooter() && mFooterDelegate.isFooterType(type)) {
            return HOLDER_TYPE.FOOTER;
        }

        return HOLDER_TYPE.COMMON;
    }

    @Override
    public void onInitStart() {

    }

    @Override
    public void onInitDone(Throwable e, List<M> data) {

    }

    @Override
    public void onRefreshStart() {

    }

    @Override
    public void onRefreshDone(Throwable e, List<M> data) {

    }

    @Override
    public void onLoadMoreStart() {

    }

    public void onLoadMoreDone(Throwable e, List<M> data) {

    }

    @Override
    public void onEnd() {

    }

    @Override
    public void onDataChange() {
        notifyDataSetChanged();
    }


    public void addDataActionListener(DataSubscriber.DataActionListener<M> listener) {
        mDataController.getSubscriberManager().addDataActionListener(listener);
    }

    public void removeDataActionListener(DataSubscriber.DataActionListener<M> listener) {
        mDataController.getSubscriberManager().removeDataActionListener(listener);
    }


    public void removeAllDataActionListeners() {
        mDataController.getSubscriberManager().removeAllDataActionListeners();
    }

    public void addDataChangeListeners(DataSubscriber.DataChangeListener<M> listener) {
        mDataController.getSubscriberManager().addDataChangeListener(listener);
    }


    public void removeDataChangeListeners(DataSubscriber.DataChangeListener<M> listener) {
        mDataController.getSubscriberManager().removeDataChangeListener(listener);
    }

    public void removeAllDataChangeListeners() {
        mDataController.getSubscriberManager().removeAllDataChangeListeners();
    }

}
