package com.wallet.crypto.utcapp.service;

import com.wallet.crypto.utcapp.entity.NewsItem;
import com.wallet.crypto.utcapp.entity.httpentity.MarketTickerResponse;
import com.wallet.crypto.utcapp.entity.httpentity.NewsDetailResponse;
import com.wallet.crypto.utcapp.entity.httpentity.NewsResponse;

import io.reactivex.Observable;

public interface NewsClientType {
    Observable<NewsItem[]> fetchNewsItems();
    Observable<NewsResponse> fetchNewsItemsNext(long timestamp, int size, int type);
    Observable<NewsDetailResponse> getNewsDetail(int id);
    Observable<MarketTickerResponse> getTicker(String exchange);
}
