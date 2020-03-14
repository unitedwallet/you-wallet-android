package com.wallet.crypto.utcapp.service;

import com.wallet.crypto.utcapp.entity.Ticker;

import io.reactivex.Observable;

public interface TickerService {

    Observable<Ticker> fetchTickerPrice(String ticker);
}
