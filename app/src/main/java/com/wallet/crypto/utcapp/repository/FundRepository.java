package com.wallet.crypto.utcapp.repository;

import android.util.Log;

import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.repository.realmentity.RealmAccount;
import com.wallet.crypto.utcapp.repository.realmentity.RealmBalance;
import com.wallet.crypto.utcapp.repository.realmentity.RealmFund;
import com.wallet.crypto.utcapp.service.AccountKeystoreService;
import com.wallet.crypto.utcapp.service.BtcAccountServiceType;
import com.wallet.crypto.utcapp.util.RealmDateUtil;
import com.wallet.crypto.utcapp.util.RealmHelper;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.realm.Realm;
import okhttp3.OkHttpClient;

public class FundRepository {
    private final AccountKeystoreService accountKeystoreService;
    private final RealmWalletSource realmWalletSource;
    private final RealmAccountSource realmAccountSource;
    private final BtcAccountServiceType btcAccountService;

    public FundRepository(
            AccountKeystoreService accountKeystoreService,
            RealmWalletSource realmWalletSource, RealmAccountSource realmAccountSource, BtcAccountServiceType btcAccountService) {
        this.accountKeystoreService = accountKeystoreService;
        this.realmWalletSource = realmWalletSource;
        this.realmAccountSource = realmAccountSource;
        this.btcAccountService = btcAccountService;
    }

    public Completable addFund(Wallet wallet, RealmFund realmFund) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                try {
                    Wallet walletnew = null;
                    if (realmFund.getRule_address().equals("ETH")) {
                        walletnew = accountKeystoreService.createEthAccount(wallet.id, 0).blockingGet();
                    } else if (realmFund.getRule_address().equals("BTC")) {
                        walletnew = btcAccountService.createBtcAccount(wallet.id, 0).blockingGet();
                    }

                    if (walletnew != null && (wallet.type.equals(RealmDateUtil.WALLET_TYPE_0) || wallet.type.equals(RealmDateUtil.WALLET_TYPE_1))) {

                        realmAccountSource.addAccount(wallet, walletnew, realmFund);
                        realmWalletSource.addBalance(wallet, realmFund);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                }
            }
        });
    }
}
