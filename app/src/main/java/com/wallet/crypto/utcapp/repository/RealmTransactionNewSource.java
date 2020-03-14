package com.wallet.crypto.utcapp.repository;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.wallet.crypto.utcapp.entity.NetworkInfo;
import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.repository.entity.RealmTransaction;
import com.wallet.crypto.utcapp.repository.realmentity.RealmTransactionNew;
import com.wallet.crypto.utcapp.repository.realmentity.RealmWallet;
import com.wallet.crypto.utcapp.util.PreferenceUtils;
import com.wallet.crypto.utcapp.util.RealmHelper;

import java.math.BigDecimal;

import io.realm.Realm;
import io.realm.RealmResults;

import static java.math.BigDecimal.ROUND_UP;

public class RealmTransactionNewSource {
    /**
     * 检查该链上最新的交易是否处于未打包状态
     *
     * @param chain_id
     * @param nonce
     * @return
     */
    public boolean isHasSameNoncePending(String wallet_id, String chain_id, int nonce) {
        Realm realm = null;
        boolean isHas = false;
        try {
            realm = RealmHelper.getInstance().getRealmInstance();
            RealmTransactionNew realmItem = realm.where(RealmTransactionNew.class).equalTo("wallet_id", wallet_id).equalTo("nonce", nonce).equalTo("status", 0).equalTo("chain_id", chain_id)
                    .findFirst();
            if (realmItem != null) {
                isHas = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (realm != null) {
                realm.close();
            }
            return isHas;
        }

    }

    //检查新的交易的gasPrice是否超过处于pending状态的交易的gasPrice的10%
    public boolean isGasAddCheckPending(String wallet_id,String chain_id, int nonce, String new_gas_price) {
        Realm realm = null;
        boolean isHas = false;

        try {
            realm = RealmHelper.getInstance().getRealmInstance();
            RealmTransactionNew realmItem = realm.where(RealmTransactionNew.class).equalTo("wallet_id", wallet_id).equalTo("nonce", nonce).equalTo("status", 0).equalTo("chain_id", chain_id)
                    .findFirst();
            if (realmItem != null) {
                String gasLocal = realmItem.getGasPrice();
                BigDecimal gasLocalbigDecimal = new BigDecimal(gasLocal);
                BigDecimal gasbigDecimal = new BigDecimal(new_gas_price);
                if (gasbigDecimal.divide(gasLocalbigDecimal, 2, ROUND_UP).compareTo(new BigDecimal("1.1")) > 0) {
                    isHas = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (realm != null) {
                realm.close();
            }
            return isHas;
        }

    }


    public void putTransaction(RealmTransactionNew realmTransactionNew) {

        Realm realm = null;
        try {
            realm = RealmHelper.getInstance().getRealmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmTransactionNew realmItem = realm.where(RealmTransactionNew.class).equalTo("nonce", realmTransactionNew.getNonce()).equalTo("status", 0).equalTo("chain_id", realmTransactionNew.getChain_id())
                            .findFirst();
                    if (realmItem != null)
                        realmItem.deleteFromRealm();
                    realm.insertOrUpdate(realmTransactionNew);
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

    public void deleteTransaction( ) {

        Realm realm = null;
        try {
            realm = RealmHelper.getInstance().getRealmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<RealmTransactionNew> realmItem = realm.where(RealmTransactionNew.class).notEqualTo("status", 0)
                            .findAll();
                    if (realmItem != null)
                        realmItem.deleteAllFromRealm();
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
