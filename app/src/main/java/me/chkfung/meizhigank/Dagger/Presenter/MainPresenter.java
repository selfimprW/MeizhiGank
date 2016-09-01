/*
 * Meizhi & Gank.io
 * Copyright (C) 2016 ChkFung
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package me.chkfung.meizhigank.Dagger.Presenter;

import java.util.List;

import javax.inject.Inject;

import me.chkfung.meizhigank.Constants;
import me.chkfung.meizhigank.Contract.MainContract;
import me.chkfung.meizhigank.Model.DataInfo;
import me.chkfung.meizhigank.Model.Meizhi;
import me.chkfung.meizhigank.NetworkApi;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

/**
 * Main Presenter that perform business logic
 * Created by Fung on 26/07/2016.
 */

public class MainPresenter implements MainContract.Presenter {
    @Inject
    NetworkApi networkApi;
    @Inject
    Scheduler scheduler;
    private MainContract.View mView;
    private Subscription mSubscription;

    @Inject
    MainPresenter(MainContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void loadMeizhi(int page, final List<DataInfo> MeizhiData) {
        if (mSubscription != null) mSubscription.unsubscribe();
        mSubscription = networkApi.getMeizhi(Constants.MEIZHI_AMOUNT, page)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(scheduler)
                .flatMap(new Func1<Meizhi, Observable<DataInfo>>() {
                    @Override
                    public Observable<DataInfo> call(Meizhi meizhi) {
                        return Observable.from(meizhi.getResults());
                    }
                })
                .subscribe(new Subscriber<DataInfo>() {
                    @Override
                    public void onCompleted() {
                        mView.refreshRv(MeizhiData);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.networkError(e);
                    }

                    @Override
                    public void onNext(DataInfo resultsBean) {
                        MeizhiData.add(resultsBean);
                    }
                });
    }

    @Override
    public void detachView() {
        mView = null;
        if (mSubscription != null) mSubscription.unsubscribe();
    }

}
