package com.wallet.crypto.utcapp.service;

import com.wallet.crypto.utcapp.entity.GoodAddress;
import com.wallet.crypto.utcapp.entity.Wallet;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface BtcAccountServiceType {
    public Single<Wallet> createBtcAccount(String mnemonic, int index);
    Observable<GoodAddress> createBtcGoodAddress(String mnemonic);

    Observable<GoodAddress> findBtcPrivate(String address);
    Single<Wallet> importBtcPrivateKey(String privateKey);

    Single<String[]> exportSonAddress(Wallet wallet);
    Single<String[][]> exportSonPrivateAddress(Wallet wallet);
}
