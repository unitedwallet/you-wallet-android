package com.wallet.crypto.utcapp.repository;

import android.util.Log;

import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.repository.realmentity.RealmAccount;
import com.wallet.crypto.utcapp.repository.realmentity.RealmBalance;
import com.wallet.crypto.utcapp.repository.realmentity.RealmChain;
import com.wallet.crypto.utcapp.repository.realmentity.RealmFund;
import com.wallet.crypto.utcapp.repository.realmentity.RealmWallet;
import com.wallet.crypto.utcapp.util.RealmHelper;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.realm.Realm;

import static com.wallet.crypto.utcapp.util.RealmDateUtil.WALLET_TYPE_0;
import static com.wallet.crypto.utcapp.util.RealmDateUtil.WALLET_TYPE_1;

public class RealmAccountSource {

    public Single<Boolean> checkIshasAccount(String wallet_id, String chain_id) {
        return Single.fromCallable(() -> {
            Realm realm = null;
            final boolean[] ishas = {false};
            try {
                realm = RealmHelper.getInstance().getRealmInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmAccount realmWallet1 = realm.where(RealmAccount.class).equalTo("wallet_id", wallet_id).equalTo("chain_id", chain_id).findFirst();

                        if (realmWallet1 != null) {
                            ishas[0] = true;
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (realm != null) {
                    realm.close();
                }
                return ishas[0];
            }

        });
    }

    public void addAccount(Wallet wallet, Wallet privatewallet, RealmFund realmFund) {
        Realm realm = null;

        try {
            realm = RealmHelper.getInstance().getRealmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmChain realmChain = realm.where(RealmChain.class).equalTo("id", realmFund.getChain_id()).findFirst();
                    if (realmChain != null) {
                        RealmAccount realmAccount = realm.createObject(RealmAccount.class);
                        realmAccount.setId(privatewallet.private_key);
                        realmAccount.setWallet_id(wallet.id);
                        realmAccount.setChain(realmChain.getName());
                        realmAccount.setChain_id(realmChain.getId());
                        realmAccount.setPrivate_key(privatewallet.private_key);
                        realmAccount.setAddress(privatewallet.address);
                        realmAccount.setSon_order(0);
                        realmAccount.setCreate_time(System.currentTimeMillis());
                        realmAccount.setUpdate_time(System.currentTimeMillis());

                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (realm != null) {
                realm.close();
            }

        }


    }


}
