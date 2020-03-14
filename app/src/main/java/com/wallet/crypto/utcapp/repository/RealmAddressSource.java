package com.wallet.crypto.utcapp.repository;

import android.util.Log;

import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.repository.realmentity.RealmAccount;
import com.wallet.crypto.utcapp.repository.realmentity.RealmAddress;
import com.wallet.crypto.utcapp.repository.realmentity.RealmBalance;
import com.wallet.crypto.utcapp.repository.realmentity.RealmChain;
import com.wallet.crypto.utcapp.repository.realmentity.RealmWallet;
import com.wallet.crypto.utcapp.util.RealmHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.realm.Realm;
import io.realm.RealmResults;

import static com.wallet.crypto.utcapp.util.RealmDateUtil.WALLET_TYPE_0;
import static com.wallet.crypto.utcapp.util.RealmDateUtil.WALLET_TYPE_1;

public class RealmAddressSource implements RealmAddressSourceType {
    @Override
    public Single<RealmAddress[]> fetchAddresss() {
        return Single.fromCallable(() -> {
            Realm realm = null;
            try {
                realm = RealmHelper.getInstance().getRealmInstance();
                RealmResults<RealmAddress> realmItems = realm.where(RealmAddress.class)
                        .findAll();
                int len = realmItems.size();

                RealmAddress[] result = new RealmAddress[len];
                for (int i = 0; i < len; i++) {
                    RealmAddress realmItem = realmItems.get(i);
                    if (realmItem != null) {
                        RealmAddress realmAddress = new RealmAddress();
                        realmAddress.setAddress(realmItem.getAddress());
                        realmAddress.setChain(realmItem.getChain());
                        realmAddress.setChain_id(realmItem.getChain_id());
                        realmAddress.setCreate_time(realmItem.getCreate_time());
                        realmAddress.setName(realmItem.getName());
                        realmAddress.setUpdate_time(realmItem.getUpdate_time());
                        result[i] = realmAddress;
                    }
                }
                return result;
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }
        });
    }

    @Override
    public Single<RealmAddress[]> fetchAddresssByChainId(String chain) {
        return Single.fromCallable(() -> {
            Realm realm = null;
            try {
                realm = RealmHelper.getInstance().getRealmInstance();
                RealmResults<RealmAddress> realmItems = realm.where(RealmAddress.class).equalTo("chain_id",chain)
                        .findAll();
                int len = realmItems.size();

                RealmAddress[] result = new RealmAddress[len];
                for (int i = 0; i < len; i++) {
                    RealmAddress realmItem = realmItems.get(i);
                    if (realmItem != null) {
                        RealmAddress realmAddress = new RealmAddress();
                        realmAddress.setAddress(realmItem.getAddress());
                        realmAddress.setChain(realmItem.getChain());
                        realmAddress.setChain_id(realmItem.getChain_id());
                        realmAddress.setCreate_time(realmItem.getCreate_time());
                        realmAddress.setName(realmItem.getName());
                        realmAddress.setUpdate_time(realmItem.getUpdate_time());
                        result[i] = realmAddress;
                    }
                }
                return result;
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }
        });
    }

    @Override
    public Single<Boolean> checkIsAdd(String chain_id, String address) {
        return Single.fromCallable(() -> {
            Realm realm = null;
            try {
                realm = RealmHelper.getInstance().getRealmInstance();
                RealmAddress realmItems = realm.where(RealmAddress.class).equalTo("chain_id", chain_id).equalTo("address", address)
                        .findFirst();
                if (realmItems != null) return true;
                else
                    return false;
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }
        });
    }

    @Override
    public Completable addAddress(RealmChain realmChain, String name, String address) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                Realm realm = null;

                try {

                    realm = RealmHelper.getInstance().getRealmInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmAddress realmAddress = realm.createObject(RealmAddress.class);

                            realmAddress.setCreate_time(System.currentTimeMillis() + "");
                            realmAddress.setUpdate_time(System.currentTimeMillis() + "");
                            realmAddress.setName(name);
                            realmAddress.setAddress(address);
                            realmAddress.setChain_id(realmChain.getId());
                            realmAddress.setChain(realmChain.getSymbol());
                        }
                    });

                } finally {
                    if (realm != null) {
                        realm.close();
                    }

                }
            }
        });

    }
}
