package com.wallet.crypto.utcapp.repository;

import com.wallet.crypto.utcapp.entity.Token;
import com.wallet.crypto.utcapp.entity.Wallet;

import java.math.BigDecimal;

import io.reactivex.Completable;
import io.reactivex.Observable;

public interface TokenRepositoryType {

    Observable<Token[]> fetch(String walletAddress);

    Completable addToken(Wallet wallet, String address, String symbol, int decimals, String totalSupply);

    //xieyueshu
    Completable addToken(Wallet wallet, String address, String name, String symbol, int decimals, String totalSupply);
}
