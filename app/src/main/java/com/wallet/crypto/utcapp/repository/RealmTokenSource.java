package com.wallet.crypto.utcapp.repository;

import android.util.Log;

import com.wallet.crypto.utcapp.entity.NetworkInfo;
import com.wallet.crypto.utcapp.entity.TokenInfo;
import com.wallet.crypto.utcapp.entity.Transaction;
import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.repository.entity.Contract;
import com.wallet.crypto.utcapp.repository.entity.RealmTokenInfo;
import com.wallet.crypto.utcapp.repository.entity.RealmTransactionOperation;
import com.wallet.crypto.utcapp.util.RealmHelper;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.RealmSchema;
import io.realm.Sort;

public class RealmTokenSource implements TokenLocalSource {

    @Override
    public Completable put(NetworkInfo networkInfo, Wallet wallet, TokenInfo tokenInfo) {
        return Completable.fromAction(() -> putInNeed(networkInfo, wallet, tokenInfo));
    }

    @Override
    public Single<TokenInfo[]> fetch(NetworkInfo networkInfo, Wallet wallet) {
        return Single.fromCallable(() -> {
            Realm realm = null;
            try {
                realm = RealmHelper.getInstance().getRealmInstance(networkInfo, wallet);
                RealmResults<RealmTokenInfo> realmItems = realm.where(RealmTokenInfo.class)
                        .sort("addedTime", Sort.DESCENDING)
                        .findAll();
                int len = realmItems.size();
                TokenInfo[] result = new TokenInfo[len];
                for (int i = 0; i < len; i++) {
                    RealmTokenInfo realmItem = realmItems.get(i);
                    if (realmItem != null) {
                        result[i] = new TokenInfo(
                                realmItem.getAddress(),
                                realmItem.getName(),
                                realmItem.getSymbol(),
                                realmItem.getDecimals(), realmItem.getTotalSupply(), realmItem.balance, realmItem.qualityName, realmItem.type, realmItem.price, realmItem.percent_change_24h, realmItem.image);
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
    public void putInNeedOrUpdate(NetworkInfo networkInfo, Wallet wallet, Contract[] contracts) {
        Realm realm = null;
        try {
            realm = RealmHelper.getInstance().getRealmInstance(networkInfo, wallet);
            Realm finalRealm = realm;
            realm.executeTransaction(r -> {
                if (contracts != null && contracts.length > 0) {
                    for (int i = contracts.length - 1; i >= 0; i--) {
                        Contract contract = contracts[i];
                        RealmTokenInfo realmTokenInfo = finalRealm.where(RealmTokenInfo.class)
                                .equalTo("address", contract.getAddress())
                                .findFirst();
                        RealmTokenInfo realmTokenInfo1 = new RealmTokenInfo();
                        realmTokenInfo1.setAddress(contract.getAddress());
                        realmTokenInfo1.setName(contract.getName());
                        realmTokenInfo1.setSymbol(contract.getSymbol());
                        realmTokenInfo1.setDecimals((int) contract.getDecimals());
                        realmTokenInfo1.setTotalSupply(contract.getTotalSupply());
                        realmTokenInfo1.setBalance(contract.getBalance());
                        realmTokenInfo1.setQualityName(contract.getQualityName());
                        realmTokenInfo1.setType(contract.getType());
                        realmTokenInfo1.setPrice(contract.getPrice());
                        realmTokenInfo1.setPercent_change_24h(contract.getPercent_change_24h());
                        realmTokenInfo1.setImage(contract.getImage());
                        if (realmTokenInfo == null) {
                            realmTokenInfo1.setAddedTime(System.currentTimeMillis());
                        }
                        r.insertOrUpdate(realmTokenInfo1);
                    }
                }

            });
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    private void putInNeed(NetworkInfo networkInfo, Wallet wallet, TokenInfo tokenInfo) {
        Realm realm = null;
        try {
            realm = RealmHelper.getInstance().getRealmInstance(networkInfo, wallet);
            RealmTokenInfo realmTokenInfo = realm.where(RealmTokenInfo.class)
                    .equalTo("address", tokenInfo.address)
                    .findFirst();
            //  if (realmTokenInfo == null) {
            realm.executeTransaction(r -> {
                RealmTokenInfo obj = new RealmTokenInfo();
                //  RealmTokenInfo obj = r.createObject(RealmTokenInfo.class, tokenInfo.address);
                obj.setAddress(tokenInfo.address);
                obj.setName(tokenInfo.name);
                obj.setSymbol(tokenInfo.symbol);
                obj.setDecimals(tokenInfo.decimals);
                obj.setTotalSupply(tokenInfo.totalSupply);
                //if (realmTokenInfo == null) {
                    obj.setAddedTime(System.currentTimeMillis());
               // }
                obj.setBalance(tokenInfo.balance);
                obj.setQualityName(tokenInfo.qualityName);
                obj.setType(tokenInfo.type);
                obj.setPrice(tokenInfo.price);
                obj.setPercent_change_24h(tokenInfo.percent_change_24h);
                obj.setImage(tokenInfo.image);
                r.insertOrUpdate(obj);
            });
            // }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public void putAtOnce(NetworkInfo networkInfo, Wallet wallet, TokenInfo tokenInfo) {
        putInNeed(networkInfo, wallet, tokenInfo);
    }

}
