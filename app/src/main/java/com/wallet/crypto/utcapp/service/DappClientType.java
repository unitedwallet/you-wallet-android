package com.wallet.crypto.utcapp.service;

import com.wallet.crypto.utcapp.entity.DappItem;
import com.wallet.crypto.utcapp.entity.DappResponse;
import com.wallet.crypto.utcapp.entity.NoticeResponse;

import io.reactivex.Observable;
import retrofit2.http.Query;

public interface DappClientType {
    Observable<DappResponse> fetchDappItems();
    Observable<DappResponse> fetchAllDappItems( String type,  int page,  int limit);
    Observable<NoticeResponse> fetchNotice();
    Observable<NoticeResponse> fetchNoticeNext(int page,long time);
}
