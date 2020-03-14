package com.wallet.crypto.utcapp.repository;

import com.wallet.crypto.utcapp.entity.NetworkInfo;
import com.wallet.crypto.utcapp.entity.TokenInfo;
import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.repository.entity.Contract;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface TokenLocalSource {
    Completable put(NetworkInfo networkInfo, Wallet wallet, TokenInfo tokenInfo);
    Single<TokenInfo[]> fetch(NetworkInfo networkInfo, Wallet wallet);
    public void putAtOnce(NetworkInfo networkInfo, Wallet wallet, TokenInfo tokenInfo);
    void putInNeedOrUpdate(NetworkInfo networkInfo, Wallet wallet, Contract[] contracts);
}
