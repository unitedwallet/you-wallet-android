package com.wallet.crypto.utcapp.service;

import com.wallet.crypto.utcapp.entity.BannerItem;

import io.reactivex.Observable;

public interface BannerClientType {
    Observable<BannerItem[]> fetchBannerItems();
}
