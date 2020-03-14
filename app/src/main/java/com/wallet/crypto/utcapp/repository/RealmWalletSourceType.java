package com.wallet.crypto.utcapp.repository;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.wallet.crypto.utcapp.entity.HomeToken;
import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.entity.httpentity.HomeTokenResponse;
import com.wallet.crypto.utcapp.repository.realmentity.RealmAccount;
import com.wallet.crypto.utcapp.repository.realmentity.RealmBalance;
import com.wallet.crypto.utcapp.repository.realmentity.RealmChain;
import com.wallet.crypto.utcapp.repository.realmentity.RealmFund;
import com.wallet.crypto.utcapp.repository.realmentity.RealmTransactionNew;
import com.wallet.crypto.utcapp.repository.realmentity.RealmWallet;
import com.wallet.crypto.utcapp.util.PreferenceUtils;
import com.wallet.crypto.utcapp.util.RealmHelper;
import com.wallet.crypto.utcapp.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;

import static com.wallet.crypto.utcapp.util.RealmDateUtil.WALLET_TYPE_0;
import static com.wallet.crypto.utcapp.util.RealmDateUtil.WALLET_TYPE_1;

public interface RealmWalletSourceType {
    public Single<Wallet[]> fetchWallet() ;
    public Single<RealmWallet[]> getBindWallet() ;
    public void createWallet(Context context, Wallet wallet) ;
    public void createAccountBalance(Realm realm, Wallet walletNew, Wallet privatewallet, String chain, String chain_id,int sonOrder) ;

    public Single<Wallet> getDefaultWallet() ;

    public Wallet fetchBalances(Wallet wallet) ;

    public void setDefault(Wallet wallet) ;
    public   HomeToken fetchBalances(String wallet_id, String fund_id);
    public List<RealmChain> getChainNotContainBtc() ;
    public List<RealmChain> getChain() ;
    public RealmFund getMainFundByChain(String chain_id);
    public List<RealmFund> getFunds();
    public RealmFund getFundByFundId(String fund_id);
    public List<RealmFund> getFundsBySearch(String content) ;
    public RealmWallet getWallet(String id) ;
    public Single<String> setWalletName(Wallet wallet);

    public Completable deleteWallet(Wallet wallet);


    public Single<String> getPrivate(Wallet wallet, RealmChain realmChain) ;
    public Observable<List<RealmTransactionNew>> fetchFundTransaction(String wallet_id, String fund_id, int type) ;

    public Single<RealmChain> getChain(String chain_id) ;
    public RealmAccount getAccount(String chain_id, String wallet_id) ;
    public Completable addBalance(Wallet wallet,RealmFund fund);
    public void updateBalance(HomeTokenResponse homeTokenResponse);
}
