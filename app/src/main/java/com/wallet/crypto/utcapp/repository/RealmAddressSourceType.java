package com.wallet.crypto.utcapp.repository;

import android.content.Context;

import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.repository.realmentity.RealmAddress;
import com.wallet.crypto.utcapp.repository.realmentity.RealmChain;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface RealmAddressSourceType {
    public Single<RealmAddress[]> fetchAddresss() ;
    public Single<RealmAddress[]> fetchAddresssByChainId(String chain) ;
    public Single<Boolean> checkIsAdd(String chain_id,String address) ;
    public Completable addAddress(RealmChain realmChain, String  name, String address) ;

}
