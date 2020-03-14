package com.wallet.crypto.utcapp.service;

import com.wallet.crypto.utcapp.entity.Exchange;
import com.wallet.crypto.utcapp.entity.NewsItem;

import io.reactivex.Observable;

public interface ExchangeServiceType {
    Observable<Exchange[]> fetchExchanges();
}
