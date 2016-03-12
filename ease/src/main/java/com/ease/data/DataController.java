package com.ease.data;

import com.ease.model.BaseModel;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Data Controller
 * Created by Spencer on 15/10/28.
 */
public abstract class DataController<M extends BaseModel> implements DataActionInterface<M> {

    enum ACTION {
        INITIALIZE, REFRESH, LOAD_MORE
    }

    public static final int DEFAULT_LIMIT = 20;

    private List<M> mData = new ArrayList<>();

    /**
     * 观察订阅管理器
     */
    private SubscriberManager mSubscriberManager = new SubscriberManager();

    /**
     * 是否忙碌
     */
    private boolean mIsBusy;
    /**
     * 是否允许刷新
     */
    private boolean mAllowRefresh = true;
    /**
     * 是否允许加载
     */
    private boolean mAllowLoadMore = true;

    private int mMaxPage = Integer.MAX_VALUE;
    private int mDataLimit = DEFAULT_LIMIT;

    /**
     * 是否当刷新时清除所有数据
     */
    private boolean mClearAllWhenRefresh = true;

    public void initialize() {
        takeAction(ACTION.INITIALIZE);
    }

    public void refresh() {
        takeAction(ACTION.REFRESH);
    }

    public void loadMore() {
        takeAction(ACTION.LOAD_MORE);
    }

    protected void takeAction(final ACTION action) {
        if (action == ACTION.REFRESH && !mAllowRefresh) {
            return;
        }

        if (action == ACTION.LOAD_MORE && !mAllowLoadMore) {
            return;
        }

        mIsBusy = true;
        mSubscriberManager.dispatchStartMessage(action);

        Observable<List<M>> todo = null;
        switch (action) {
            case INITIALIZE:
                todo = doInitialize();
                mAllowRefresh = false;
                mAllowLoadMore = false;
                break;
            case REFRESH:
                todo = doRefresh();
                mAllowRefresh = false;
                break;
            case LOAD_MORE:
                todo = doLoadMore();
                mAllowLoadMore = false;
                break;
        }

        Action1<List<M>> successAction = new Action1<List<M>>() {
            @Override
            public void call(List<M> ms) {
                onCallSuccess(action, ms);
            }
        };

        Action1<Throwable> failureAction = new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                onCallFailure(action, throwable);
            }
        };

        if (todo != null) {
            todo.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(successAction, failureAction);
        } else {
            failureAction.call(new IllegalArgumentException("The Observable is null."));
        }
    }

    /**
     * 请求成功时的回调
     */
    protected void onCallSuccess(final ACTION action, final List<M> ms) {
        boolean isEnd = (ms == null);
        if (action != ACTION.LOAD_MORE) {
            if (mClearAllWhenRefresh) {
                mData.clear();
            }
            bulkInsertData(0, ms);
        } else {
            bulkAppendData(ms);
        }
        mSubscriberManager.dispatchDoneMessage(action, null, ms);
        if (isEnd) {
            mSubscriberManager.dispatchEndMessage();
        }
        onCallFinished(action);
    }

    /**
     * 请求失败时的回调
     */
    protected void onCallFailure(final ACTION action, final Throwable throwable) {
        mSubscriberManager.dispatchDoneMessage(action, throwable, null);
        onCallFinished(action);
    }

    /**
     * 请求结束时的回调
     */
    protected void onCallFinished(final ACTION action) {
        switch (action) {
            case INITIALIZE:
                mAllowRefresh = true;
                mAllowLoadMore = true;
                break;
            case REFRESH:
                mAllowRefresh = true;
                break;
            case LOAD_MORE:
                mAllowLoadMore = true;
                break;
        }

        mIsBusy = false;
    }

    /**
     * 获取数据
     */
    public List<M> getData() {
        return mData;
    }

    public SubscriberManager getSubscriberManager() {
        return mSubscriberManager;
    }

    public boolean isBusy() {
        return mIsBusy;
    }

    public void setMaxPage(int maxPage) {
        mMaxPage = maxPage;
    }

    public int getOffset() {
        return mData.size();
    }

    public int getLimit() {
        return mDataLimit;
    }

    public void setLimit(int dataLimit) {
        mDataLimit = dataLimit;
    }

    public void setClearAllWhenRefresh(boolean clearAllWhenRefresh) {
        mClearAllWhenRefresh = clearAllWhenRefresh;
    }

    public void appendData(M data) {
        if (data == null) {
            return;
        }
        mData.add(data);
        mSubscriberManager.dispatchDataChangeMessage();
    }

    public void insertData(int position, M data) {
        if (data == null) {
            return;
        }
        mData.add(position, data);
        mSubscriberManager.dispatchDataChangeMessage();
    }

    public void removeData(M data) {
        if (data == null) {
            return;
        }
        mData.remove(data);
        mSubscriberManager.dispatchDataChangeMessage();
    }

    public void removeData(int position) {
        if (position < 0 || position >= getOffset()) {
            return;
        }
        mData.remove(position);
        mSubscriberManager.dispatchDataChangeMessage();
    }

    public void bulkAppendData(List<M> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        mData.addAll(data);
        mSubscriberManager.dispatchDataChangeMessage();
    }

    public void bulkInsertData(int position, List<M> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        mData.addAll(position, data);
        mSubscriberManager.dispatchDataChangeMessage();
    }

    /**
     * 用来管理订阅者的事件分发
     */
    public class SubscriberManager {

        private final List<DataSubscriber.DataActionListener<M>> mDataActionListeners;
        private final List<DataSubscriber.DataChangeListener<M>> mDataChangeListeners;

        public SubscriberManager() {
            mDataActionListeners = new ArrayList<>();
            mDataChangeListeners = new ArrayList<>();
        }

        public void addDataActionListener(DataSubscriber.DataActionListener<M> listener) {
            checkIfNull(listener);
            synchronized (mDataActionListeners) {
                if (!mDataActionListeners.contains(listener)) {
                    mDataActionListeners.add(listener);
                }
            }
        }

        public void removeDataActionListener(DataSubscriber.DataActionListener<M> listener) {
            checkIfNull(listener);
            synchronized (mDataActionListeners) {
                mDataActionListeners.remove(listener);
            }
        }

        public void removeAllDataActionListeners() {
            synchronized (mDataActionListeners) {
                mDataActionListeners.clear();
            }
        }

        public void addDataChangeListener(DataSubscriber.DataChangeListener<M> listener) {
            checkIfNull(listener);
            synchronized (mDataChangeListeners) {
                if (!mDataChangeListeners.contains(listener)) {
                    mDataChangeListeners.add(listener);
                }
            }
        }

        public void removeDataChangeListener(DataSubscriber.DataChangeListener<M> listener) {
            checkIfNull(listener);
            synchronized (mDataChangeListeners) {
                mDataChangeListeners.remove(listener);
            }
        }

        public void removeAllDataChangeListeners() {
            synchronized (mDataActionListeners) {
                mDataActionListeners.clear();
            }
        }

        public void dispatchStartMessage(final DataController.ACTION action) {
            if (mDataActionListeners.isEmpty()) {
                return;
            }

            switch (action) {
                case INITIALIZE:
                    for (DataSubscriber.DataActionListener<M> subscriber : mDataActionListeners) {
                        subscriber.onInitStart();
                    }
                    break;
                case REFRESH:
                    for (DataSubscriber.DataActionListener<M> subscriber : mDataActionListeners) {
                        subscriber.onRefreshStart();
                    }
                    break;
                case LOAD_MORE:
                    for (DataSubscriber.DataActionListener<M> subscriber : mDataActionListeners) {
                        subscriber.onLoadMoreStart();
                    }
                    break;
            }
        }

        public void dispatchDoneMessage(final DataController.ACTION action, final Throwable e, final List<M> data) {
            if (mDataActionListeners.isEmpty()) {
                return;
            }

            switch (action) {
                case INITIALIZE:
                    for (DataSubscriber.DataActionListener<M> subscriber : mDataActionListeners) {
                        subscriber.onInitDone(e, data);
                    }
                    break;
                case REFRESH:
                    for (DataSubscriber.DataActionListener<M> subscriber : mDataActionListeners) {
                        subscriber.onRefreshDone(e, data);
                    }
                    break;
                case LOAD_MORE:
                    for (DataSubscriber.DataActionListener<M> subscriber : mDataActionListeners) {
                        subscriber.onLoadMoreDone(e, data);
                    }
                    break;
            }
        }

        public void dispatchEndMessage() {
            if (mDataActionListeners.isEmpty()) {
                return;
            }

            for (DataSubscriber.DataActionListener<M> subscriber : mDataActionListeners) {
                subscriber.onEnd();
            }
        }

        public void dispatchDataChangeMessage() {
            if (mDataChangeListeners.isEmpty()) {
                return;
            }

            for (DataSubscriber.DataChangeListener<M> listener : mDataChangeListeners) {
                listener.onDataChange();
            }
        }

        private void checkIfNull(Object object) {
            if (object == null) {
                throw new NullPointerException("The Data Subscriber is null.");
            }
        }
    }
}
