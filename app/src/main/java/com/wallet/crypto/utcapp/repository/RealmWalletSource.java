package com.wallet.crypto.utcapp.repository;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.wallet.crypto.utcapp.C;
import com.wallet.crypto.utcapp.entity.HomeToken;
import com.wallet.crypto.utcapp.entity.NetworkInfo;
import com.wallet.crypto.utcapp.entity.TokenInfo;
import com.wallet.crypto.utcapp.entity.Transaction;
import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.entity.httpentity.HomeTokenResponse;
import com.wallet.crypto.utcapp.repository.entity.RealmTokenInfo;
import com.wallet.crypto.utcapp.repository.realmentity.RealmAccount;
import com.wallet.crypto.utcapp.repository.realmentity.RealmBalance;
import com.wallet.crypto.utcapp.repository.realmentity.RealmChain;
import com.wallet.crypto.utcapp.repository.realmentity.RealmFund;
import com.wallet.crypto.utcapp.repository.realmentity.RealmTransactionNew;
import com.wallet.crypto.utcapp.repository.realmentity.RealmWallet;
import com.wallet.crypto.utcapp.util.BalanceUtils;
import com.wallet.crypto.utcapp.util.PreferenceUtils;
import com.wallet.crypto.utcapp.util.RealmDateUtil;
import com.wallet.crypto.utcapp.util.RealmHelper;
import com.wallet.crypto.utcapp.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.functions.Action0;

import static com.wallet.crypto.utcapp.util.RealmDateUtil.WALLET_TYPE_0;
import static com.wallet.crypto.utcapp.util.RealmDateUtil.WALLET_TYPE_1;


public class RealmWalletSource implements RealmWalletSourceType {


    public Single<Wallet[]> fetchWallet() {
        return Single.fromCallable(() -> {
            Realm realm = null;
            try {
                realm = RealmHelper.getInstance().getRealmInstance();
                RealmResults<RealmWallet> realmItems = realm.where(RealmWallet.class)
                        .findAll();
                int len = realmItems.size();
                Log.d("获取的钱包", len + "");
                Wallet[] result = new Wallet[len];
                for (int i = 0; i < len; i++) {
                    RealmWallet realmItem = realmItems.get(i);
                    if (realmItem != null) {
                        Wallet wallet = new Wallet();
                        wallet.is_select = realmItem.getIs_select();
                        wallet.type = realmItem.getType();
                        if (realmItem.getType().equals(WALLET_TYPE_0) || realmItem.getType().equals(WALLET_TYPE_1)) {

                            wallet.mnemonic = realmItem.getId();
                        } else {
                            wallet.private_key = realmItem.getId();
                        }
                        wallet.name = realmItem.getName();
                        wallet.son = realmItem.getSon();
                        wallet.id = realmItem.getId();
                        result[i] = wallet;
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
    public Single<RealmWallet[]> getBindWallet() {
        return Single.fromCallable(() -> {
            Realm realm = null;
            try {
                realm = RealmHelper.getInstance().getRealmInstance();
                RealmResults<RealmWallet> realmItems = realm.where(RealmWallet.class).equalTo("type", RealmDateUtil.WALLET_TYPE_0)
                        .findAll();
                int len = realmItems.size();
                Log.d("获取的钱包", len + "");
                RealmWallet[] result = new RealmWallet[len];
                for (int i = 0; i < len; i++) {
                    RealmWallet realmItem = realmItems.get(i);
                    if (realmItem != null) {
                        RealmWallet wallet = new RealmWallet();
                        wallet.setSon(realmItem.getSon());
                        wallet.setType(realmItem.getType());
                        wallet.setId(realmItem.getId());
                        wallet.setName(realmItem.getName());
                        wallet.setKey(realmItem.getKey());
                        wallet.setIs_select(realmItem.getIs_select());
                        result[i] = wallet;
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

    public void createWallet(Context context, Wallet wallet) {

        Realm realm = null;
        try {
            realm = RealmHelper.getInstance().getRealmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmWallet realmWallet1 = realm.where(RealmWallet.class).equalTo("is_select", true).findFirst();
                    if (realmWallet1 != null)
                        realmWallet1.setIs_select(false);
                    RealmWallet realmWallet = realm.createObject(RealmWallet.class, wallet.id);
                    if (TextUtils.isEmpty(wallet.name)) {
                        realmWallet.setName(PreferenceUtils.getNewWalletName(context));
                    } else {

                        realmWallet.setName(wallet.name);
                    }
                    if (realmWallet.getName().equals(PreferenceUtils.getNewWalletName(context))) {
                        PreferenceUtils.saveWalletSequenceNo(context);
                    }
                    realmWallet.setKey(wallet.id);
                    realmWallet.setType(wallet.type);
                    realmWallet.setSon(wallet.son);
                    realmWallet.setIs_select(true);
                    realmWallet.setCreate_time(System.currentTimeMillis());
                    realmWallet.setUpdate_time(System.currentTimeMillis());
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

    public void createAccountBalance(Realm realm, Wallet walletNew, Wallet privatewallet, String chain, String chain_id, int sonOrder) {
        realm = RealmHelper.getInstance().getRealmInstance();

        //生成各个链主账户
        RealmAccount realmAccount = realm.createObject(RealmAccount.class);
        realmAccount.setId(privatewallet.private_key);
        realmAccount.setWallet_id(walletNew.id);
        realmAccount.setChain(chain);
        realmAccount.setChain_id(chain_id);
        realmAccount.setPrivate_key(privatewallet.private_key);
        realmAccount.setAddress(privatewallet.address);
        realmAccount.setSon_order(sonOrder);
        realmAccount.setCreate_time(System.currentTimeMillis());
        realmAccount.setUpdate_time(System.currentTimeMillis());

        //生成资产表数据 先查找资产表
        RealmResults<RealmFund> funds = realm.where(RealmFund.class).equalTo("chain_id", chain_id).equalTo("is_default", true).findAll();
        int len = funds.size();
        for (int i = 0; i < len; i++) {
            RealmFund fund = funds.get(i);
            if (fund != null) {
                RealmBalance realmBalance = realm.createObject(RealmBalance.class);
                realmBalance.setAccount_id(privatewallet.private_key);
                realmBalance.setAddress(privatewallet.address);
                realmBalance.setWallet_id(walletNew.id);
                realmBalance.setChainId(chain_id);
                realmBalance.setFund_name(fund.getName());
                realmBalance.setFund_id(fund.getId());
                realmBalance.setBalance("0");
                realmBalance.setCreate_time(System.currentTimeMillis());
                realmBalance.setUpdate_time(System.currentTimeMillis());
                realmBalance.setRule_address(fund.getRule_address());
                realmBalance.setDecimals(fund.getDecimals());

                realmBalance.setGas_symbol(fund.getGas_symbol());
                realmBalance.setGas_limit(fund.getGas_limit());
                realmBalance.setGas_limit_max(fund.getGas_limit_max());
                realmBalance.setGas_limit_min(fund.getGas_limit_min());
                realmBalance.setGas_price(fund.getGas_price());
                realmBalance.setGas_price_max(fund.getGas_price_max());
                realmBalance.setGas_price_min(fund.getGas_price_min());
                realmBalance.setContract_address(fund.getContract_address());
                realmBalance.setIcon(fund.getIcon());

            }
        }
        ;


    }


    public Single<Wallet> getDefaultWallet() {
        return Single.fromCallable(() -> {
            Realm realm = null;
            Wallet wallet = new Wallet();
            try {
                realm = RealmHelper.getInstance().getRealmInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        RealmWallet realmWallet1 = realm.where(RealmWallet.class).equalTo("is_select", true).findFirst();

                        if (realmWallet1 != null) {
                            wallet.type = realmWallet1.getType();
                            if (realmWallet1.getType().equals(WALLET_TYPE_0) || realmWallet1.getType().equals(WALLET_TYPE_1)) {
                                wallet.mnemonic = realmWallet1.getId();
                            } else {
                                wallet.private_key = realmWallet1.getId();
                            }
                            wallet.name = realmWallet1.getName();
                            wallet.id = realmWallet1.getId();
                            wallet.son = realmWallet1.getSon();


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
            return wallet;
        });
    }

    public Wallet fetchBalances(Wallet wallet) {

        Realm realm = null;
        try {
            realm = RealmHelper.getInstance().getRealmInstance();
            RealmResults<RealmBalance> realmItems = realm.where(RealmBalance.class).equalTo("wallet_id", wallet.id)
                    .findAll();
            realmItems = realmItems.sort("fund_name", Sort.ASCENDING);
            int len = realmItems.size();
            Log.d("获取的资产", len + "");
            List<HomeToken> result = new ArrayList<>();
            BigDecimal totalBalance = BigDecimal.ZERO;
            for (int i = 0; i < len; i++) {
                RealmBalance realmItem = realmItems.get(i);
                if (realmItem != null) {
                    HomeToken homeToken = new HomeToken();
                    homeToken.balance = realmItem.getBalance();
                    homeToken.icon = realmItem.getIcon();
                    homeToken.price = realmItem.getCny_price();
                    homeToken.symbol = realmItem.getFund_name();
                    homeToken.chain_id = realmItem.getChainId();
                    homeToken.address = realmItem.getAddress();
                    homeToken.decimals = realmItem.getDecimals();
                    homeToken.contract_address = realmItem.getContract_address();
                    homeToken.fund_id = realmItem.getFund_id();
                    homeToken.wallet_id = realmItem.getWallet_id();
                    homeToken.total_balance = StringUtils.clearZero(realmItem.getCny_balance());
                    homeToken.rule_address = realmItem.getRule_address();
                    homeToken.gas_limit = realmItem.getGas_limit();
                    homeToken.gas_limit_max = realmItem.getGas_limit_max();
                    homeToken.gas_limit_min = realmItem.getGas_limit_min();
                    homeToken.gas_price = realmItem.getGas_price();
                    homeToken.gas_price_max = realmItem.getGas_price_max();
                    homeToken.gas_price_min = realmItem.getGas_price_min();
                    homeToken.gas_symbol = realmItem.getGas_symbol();

                    totalBalance = totalBalance.add(BalanceUtils.subunitToBase(homeToken.total_balance, homeToken.decimals));
                    result.add(homeToken);
                }

            }
            wallet.balance = StringUtils.clearZero(totalBalance.stripTrailingZeros().toPlainString());
            wallet.token = result;
            return wallet;


        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public HomeToken fetchBalances(String wallet_id, String fund_id) {

        Realm realm = null;
        try {
            realm = RealmHelper.getInstance().getRealmInstance();
            RealmBalance realmItem = realm.where(RealmBalance.class).equalTo("wallet_id", wallet_id).equalTo("fund_id", fund_id)
                    .findFirst();

            HomeToken homeToken = new HomeToken();
            if (realmItem != null) {
                homeToken.balance = realmItem.getBalance();
                homeToken.icon = realmItem.getIcon();
                homeToken.price = realmItem.getCny_price();
                homeToken.symbol = realmItem.getFund_name();
                homeToken.chain_id = realmItem.getChainId();
                homeToken.address = realmItem.getAddress();
                homeToken.decimals = realmItem.getDecimals();
                homeToken.contract_address = realmItem.getContract_address();
                homeToken.fund_id = realmItem.getFund_id();
                homeToken.wallet_id = realmItem.getWallet_id();
                homeToken.total_balance = StringUtils.clearZero(realmItem.getCny_balance());
                homeToken.rule_address = realmItem.getRule_address();
                homeToken.gas_limit = realmItem.getGas_limit();
                homeToken.gas_limit_max = realmItem.getGas_limit_max();
                homeToken.gas_limit_min = realmItem.getGas_limit_min();
                homeToken.gas_price = realmItem.getGas_price();
                homeToken.gas_price_max = realmItem.getGas_price_max();
                homeToken.gas_price_min = realmItem.getGas_price_min();
                homeToken.gas_symbol = realmItem.getGas_symbol();

            }
            return homeToken;


        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public void setDefault(Wallet wallet) {

        Realm realm = null;
        try {
            realm = RealmHelper.getInstance().getRealmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmWallet realmWallet1 = realm.where(RealmWallet.class).equalTo("is_select", true).findFirst();
                    if (realmWallet1 != null) {
                        realmWallet1.setIs_select(false);
                        realmWallet1.setUpdate_time(System.currentTimeMillis());
                    }
                    RealmWallet realmWallet2 = realm.where(RealmWallet.class).equalTo("id", wallet.id).findFirst();
                    if (realmWallet2 != null) {
                        realmWallet2.setIs_select(true);
                        realmWallet2.setUpdate_time(System.currentTimeMillis());
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

    @Override
    public List<RealmChain> getChain() {
        Realm realm = null;
        List<RealmChain> result = new ArrayList<>();
        try {

            realm = RealmHelper.getInstance().getRealmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    //生成各个链主账户
                    //TODO 此处需要扩展 目前写死ETH  后续从数据库chain表拿取 依次为各个链根据链的规则生成主地址和主私钥
                    RealmResults<RealmChain> realmChains = realm.where(RealmChain.class).findAll();
                    int len = realmChains.size();
                    for (int i = 0; i < len; i++) {
                        RealmChain realmItem = realmChains.get(i);
                        if (realmItem != null) {
                            RealmChain homeToken = new RealmChain();
                            homeToken.setChainId(realmItem.getChainId());
                            homeToken.setDescription(realmItem.getDescription());
                            homeToken.setIcon(realmItem.getIcon());
                            homeToken.setId(realmItem.getId());
                            homeToken.setName(realmItem.getName());
                            homeToken.setRpc(realmItem.getRpc());

                            homeToken.setRule_address(realmItem.getRule_address());
                            homeToken.setRule_private_key(realmItem.getRule_private_key());
                            homeToken.setSymbol(realmItem.getSymbol());
                            homeToken.setTotal_supply(realmItem.getTotal_supply());
                            homeToken.setBackend(realmItem.getBackend());
                            homeToken.setExplorer(realmItem.getExplorer());
                            homeToken.setWeb_site(realmItem.getWeb_site());
                            result.add(homeToken);
                        }
                    }

                }
            });
            return result;
        } finally {
            if (realm != null) {
                realm.close();
            }

        }
    }

    @Override
    public List<RealmChain> getChainNotContainBtc() {
        Realm realm = null;
        List<RealmChain> result = new ArrayList<>();
        try {

            realm = RealmHelper.getInstance().getRealmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    //生成各个链主账户
                    //TODO keystor导入和导出不包含BTC
                    RealmResults<RealmChain> realmChains = realm.where(RealmChain.class).notEqualTo("id","BTC_0").findAll();
                    int len = realmChains.size();
                    for (int i = 0; i < len; i++) {
                        RealmChain realmItem = realmChains.get(i);
                        if (realmItem != null) {
                            RealmChain homeToken = new RealmChain();
                            homeToken.setChainId(realmItem.getChainId());
                            homeToken.setDescription(realmItem.getDescription());
                            homeToken.setIcon(realmItem.getIcon());
                            homeToken.setId(realmItem.getId());
                            homeToken.setName(realmItem.getName());
                            homeToken.setRpc(realmItem.getRpc());

                            homeToken.setRule_address(realmItem.getRule_address());
                            homeToken.setRule_private_key(realmItem.getRule_private_key());
                            homeToken.setSymbol(realmItem.getSymbol());
                            homeToken.setTotal_supply(realmItem.getTotal_supply());
                            homeToken.setBackend(realmItem.getBackend());
                            homeToken.setExplorer(realmItem.getExplorer());
                            homeToken.setWeb_site(realmItem.getWeb_site());
                            result.add(homeToken);
                        }
                    }

                }
            });
            return result;
        } finally {
            if (realm != null) {
                realm.close();
            }

        }
    }

    public List<RealmFund> getFunds() {
        Realm realm = null;
        List<RealmFund> result = new ArrayList<>();
        try {

            realm = RealmHelper.getInstance().getRealmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    //生成各个链主账户
                    //TODO 此处需要扩展 目前写死ETH  后续从数据库chain表拿取 依次为各个链根据链的规则生成主地址和主私钥
                    RealmResults<RealmFund> realmChains = realm.where(RealmFund.class).findAll();
                    int len = realmChains.size();
                    for (int i = 0; i < len; i++) {
                        RealmFund realmItem = realmChains.get(i);
                        if (realmItem != null) {
                            RealmFund homeToken = new RealmFund();
                            homeToken.setChain_id(realmItem.getChain_id());
                            homeToken.setIcon(realmItem.getIcon());
                            homeToken.setName(realmItem.getName());
                            homeToken.setContract_address(realmItem.getContract_address());
                            homeToken.setCreate_time(realmItem.getCreate_time());
                            homeToken.setGas_limit(realmItem.getGas_limit());
                            homeToken.setGas_limit_max(realmItem.getGas_limit_max());
                            homeToken.setGas_limit_min(realmItem.getGas_limit_min());
                            homeToken.setGas_price_max(realmItem.getGas_price_max());
                            homeToken.setGas_price_min(realmItem.getGas_price_min());
                            homeToken.setGas_price(realmItem.getGas_price());
                            homeToken.setGas_symbol(realmItem.getGas_symbol());
                            homeToken.setId(realmItem.getId());
                            homeToken.setIs_default(realmItem.getIs_default());
                            homeToken.setRule_address(realmItem.getRule_address());
                            homeToken.setType(realmItem.getType());
                            homeToken.setUpdate_time(realmItem.getUpdate_time());
                            result.add(homeToken);
                        }
                    }

                }
            });
            return result;
        } finally {
            if (realm != null) {
                realm.close();
            }

        }
    }

    @Override
    public RealmFund getFundByFundId(String fund_id) {
        Realm realm = null;
        RealmFund homeToken = new RealmFund();
        try {

            realm = RealmHelper.getInstance().getRealmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    //生成各个链主账户
                    //TODO 此处需要扩展 目前写死ETH  后续从数据库chain表拿取 依次为各个链根据链的规则生成主地址和主私钥
                    RealmFund realmItem = realm.where(RealmFund.class).equalTo("id", fund_id).findFirst();

                    if (realmItem != null) {

                        homeToken.setChain_id(realmItem.getChain_id());
                        homeToken.setIcon(realmItem.getIcon());
                        homeToken.setName(realmItem.getName());
                        homeToken.setContract_address(realmItem.getContract_address());
                        homeToken.setCreate_time(realmItem.getCreate_time());
                        homeToken.setGas_limit(realmItem.getGas_limit());
                        homeToken.setGas_limit_max(realmItem.getGas_limit_max());
                        homeToken.setGas_limit_min(realmItem.getGas_limit_min());
                        homeToken.setGas_price_max(realmItem.getGas_price_max());
                        homeToken.setGas_price_min(realmItem.getGas_price_min());
                        homeToken.setGas_price(realmItem.getGas_price());
                        homeToken.setGas_symbol(realmItem.getGas_symbol());
                        homeToken.setId(realmItem.getId());
                        homeToken.setIs_default(realmItem.getIs_default());
                        homeToken.setRule_address(realmItem.getRule_address());
                        homeToken.setType(realmItem.getType());
                        homeToken.setUpdate_time(realmItem.getUpdate_time());
                    }
                }
            });
            return homeToken;
        } finally {
            if (realm != null) {
                realm.close();
            }

        }
    }

    public List<RealmFund> getFunds(String chain_id) {
        Realm realm = null;
        List<RealmFund> result = new ArrayList<>();
        try {

            realm = RealmHelper.getInstance().getRealmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    //生成各个链主账户
                    //TODO 此处需要扩展 目前写死ETH  后续从数据库chain表拿取 依次为各个链根据链的规则生成主地址和主私钥
                    RealmResults<RealmFund> realmChains = realm.where(RealmFund.class).contains("chain_id", chain_id).findAll();
                    int len = realmChains.size();
                    for (int i = 0; i < len; i++) {
                        RealmFund realmItem = realmChains.get(i);
                        if (realmItem != null) {
                            RealmFund homeToken = new RealmFund();
                            homeToken.setChain_id(realmItem.getChain_id());
                            homeToken.setIcon(realmItem.getIcon());
                            homeToken.setName(realmItem.getName());
                            homeToken.setContract_address(realmItem.getContract_address());
                            homeToken.setCreate_time(realmItem.getCreate_time());
                            homeToken.setGas_limit(realmItem.getGas_limit());
                            homeToken.setGas_limit_max(realmItem.getGas_limit_max());
                            homeToken.setGas_limit_min(realmItem.getGas_limit_min());
                            homeToken.setGas_price_max(realmItem.getGas_price_max());
                            homeToken.setGas_price_min(realmItem.getGas_price_min());
                            homeToken.setGas_price(realmItem.getGas_price());
                            homeToken.setGas_symbol(realmItem.getGas_symbol());
                            homeToken.setId(realmItem.getId());
                            homeToken.setIs_default(realmItem.getIs_default());
                            homeToken.setRule_address(realmItem.getRule_address());
                            homeToken.setType(realmItem.getType());
                            homeToken.setUpdate_time(realmItem.getUpdate_time());
                            result.add(homeToken);
                        }
                    }

                }
            });
            return result;
        } finally {
            if (realm != null) {
                realm.close();
            }

        }
    }

    public RealmFund getMainFundByChain(String chain_id) {
        Realm realm = null;
        RealmFund homeToken = new RealmFund();
        try {

            realm = RealmHelper.getInstance().getRealmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    //生成各个链主账户
                    //TODO 此处需要扩展 目前写死ETH  后续从数据库chain表拿取 依次为各个链根据链的规则生成主地址和主私钥
                    RealmFund realmItem = realm.where(RealmFund.class).contains("chain_id", chain_id).equalTo("is_default", true).findFirst();

                    if (realmItem != null) {

                        homeToken.setChain_id(realmItem.getChain_id());
                        homeToken.setIcon(realmItem.getIcon());
                        homeToken.setName(realmItem.getName());
                        homeToken.setContract_address(realmItem.getContract_address());
                        homeToken.setCreate_time(realmItem.getCreate_time());
                        homeToken.setGas_limit(realmItem.getGas_limit());
                        homeToken.setGas_limit_max(realmItem.getGas_limit_max());
                        homeToken.setGas_limit_min(realmItem.getGas_limit_min());
                        homeToken.setGas_price_max(realmItem.getGas_price_max());
                        homeToken.setGas_price_min(realmItem.getGas_price_min());
                        homeToken.setGas_price(realmItem.getGas_price());
                        homeToken.setGas_symbol(realmItem.getGas_symbol());
                        homeToken.setId(realmItem.getId());
                        homeToken.setIs_default(realmItem.getIs_default());
                        homeToken.setRule_address(realmItem.getRule_address());
                        homeToken.setType(realmItem.getType());
                        homeToken.setUpdate_time(realmItem.getUpdate_time());
                    }
                }
            });
            return homeToken;
        } finally {
            if (realm != null) {
                realm.close();
            }

        }
    }

    public List<RealmFund> getFundsBySearch(String content) {
        Realm realm = null;
        List<RealmFund> result = new ArrayList<>();
        try {

            realm = RealmHelper.getInstance().getRealmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    //生成各个链主账户
                    //TODO 此处需要扩展 目前写死ETH  后续从数据库chain表拿取 依次为各个链根据链的规则生成主地址和主私钥
                    RealmResults<RealmFund> realmChains = realm.where(RealmFund.class).contains("name", content.toLowerCase())
                            .or().contains("name", content.toUpperCase())
                            .or().contains("contract_address", content.toLowerCase())
                            .or().contains("contract_address", content.toUpperCase()).findAll();
                    int len = realmChains.size();
                    for (int i = 0; i < len; i++) {
                        RealmFund realmItem = realmChains.get(i);
                        if (realmItem != null) {
                            RealmFund homeToken = new RealmFund();
                            homeToken.setChain_id(realmItem.getChain_id());
                            homeToken.setIcon(realmItem.getIcon());
                            homeToken.setName(realmItem.getName());
                            homeToken.setContract_address(realmItem.getContract_address());
                            homeToken.setCreate_time(realmItem.getCreate_time());
                            homeToken.setGas_limit(realmItem.getGas_limit());
                            homeToken.setGas_limit_max(realmItem.getGas_limit_max());
                            homeToken.setGas_limit_min(realmItem.getGas_limit_min());
                            homeToken.setGas_price_max(realmItem.getGas_price_max());
                            homeToken.setGas_price_min(realmItem.getGas_price_min());
                            homeToken.setGas_price(realmItem.getGas_price());
                            homeToken.setGas_symbol(realmItem.getGas_symbol());
                            homeToken.setId(realmItem.getId());
                            homeToken.setIs_default(realmItem.getIs_default());
                            homeToken.setRule_address(realmItem.getRule_address());
                            homeToken.setType(realmItem.getType());
                            homeToken.setUpdate_time(realmItem.getUpdate_time());
                            result.add(homeToken);
                        }
                    }

                }
            });
            return result;
        } finally {
            if (realm != null) {
                realm.close();
            }

        }
    }

    public List<RealmFund> getFundsBySearch(String content, String chain_id) {
        Realm realm = null;
        List<RealmFund> result = new ArrayList<>();
        try {
            Log.d("到这里了", "1");
            realm = RealmHelper.getInstance().getRealmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    //生成各个链主账户
                    //TODO 此处需要扩展 目前写死ETH  后续从数据库chain表拿取 依次为各个链根据链的规则生成主地址和主私钥
                    RealmQuery<RealmFund> realmQuery = realm.where(RealmFund.class).contains("chain_id", chain_id);
                    RealmResults<RealmFund> realmChains = realmQuery.contains("name", content.toLowerCase())
                            .or().contains("name", content.toUpperCase())
                            .or().contains("contract_address", content.toLowerCase())
                            .or().contains("contract_address", content.toUpperCase()).findAll();
                    int len = realmChains.size();
                    for (int i = 0; i < len; i++) {
                        RealmFund realmItem = realmChains.get(i);
                        if (realmItem != null) {
                            if (!realmItem.getChain_id().contains(chain_id)) continue;
                            RealmFund homeToken = new RealmFund();
                            homeToken.setChain_id(realmItem.getChain_id());
                            homeToken.setIcon(realmItem.getIcon());
                            homeToken.setName(realmItem.getName());
                            homeToken.setContract_address(realmItem.getContract_address());
                            homeToken.setCreate_time(realmItem.getCreate_time());
                            homeToken.setGas_limit(realmItem.getGas_limit());
                            homeToken.setGas_limit_max(realmItem.getGas_limit_max());
                            homeToken.setGas_limit_min(realmItem.getGas_limit_min());
                            homeToken.setGas_price_max(realmItem.getGas_price_max());
                            homeToken.setGas_price_min(realmItem.getGas_price_min());
                            homeToken.setGas_price(realmItem.getGas_price());
                            homeToken.setGas_symbol(realmItem.getGas_symbol());
                            homeToken.setId(realmItem.getId());
                            homeToken.setIs_default(realmItem.getIs_default());
                            homeToken.setRule_address(realmItem.getRule_address());
                            homeToken.setType(realmItem.getType());
                            homeToken.setUpdate_time(realmItem.getUpdate_time());
                            result.add(homeToken);
                        }
                    }

                }
            });
            return result;
        } finally {
            if (realm != null) {
                realm.close();
            }

        }
    }

    public RealmWallet getWallet(String id) {
        RealmWallet wallet = new RealmWallet();
        Realm realm = null;
        try {
            realm = RealmHelper.getInstance().getRealmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmWallet realmWallet1 = realm.where(RealmWallet.class).equalTo("id", id).findFirst();
                    if (realmWallet1 != null) {
                        wallet.setKey(realmWallet1.getId());
                        wallet.setName(realmWallet1.getName());
                        wallet.setId(realmWallet1.getId());
                        wallet.setType(realmWallet1.getType());
                        wallet.setIs_select(realmWallet1.getIs_select());
                        wallet.setSon(realmWallet1.getSon());
                    }

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (realm != null) {
                realm.close();
            }
            return wallet;
        }
    }

    public Single<String> setWalletName(Wallet wallet) {
        return Single.fromCallable(() -> {
            Realm realm = null;
            List<RealmChain> result = new ArrayList<>();
            try {

                realm = RealmHelper.getInstance().getRealmInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        //生成各个链主账户

                        RealmWallet realmChains = realm.where(RealmWallet.class).equalTo("id", wallet.id).findFirst();
                        if (realmChains != null)
                            realmChains.setName(wallet.name);

                    }
                });
                return wallet.name;
            } finally {
                if (realm != null) {
                    realm.close();
                }

            }
        }).subscribeOn(Schedulers.io());

    }

    public Completable deleteWallet(Wallet wallet) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                Realm realm = null;
                List<RealmChain> result = new ArrayList<>();
                try {

                    realm = RealmHelper.getInstance().getRealmInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            RealmWallet realmChains = realm.where(RealmWallet.class).equalTo("id", wallet.id).findFirst();
                            if (realmChains != null) {
                                RealmResults<RealmAccount> realmAccounts = realm.where(RealmAccount.class).equalTo("wallet_id", wallet.id).findAll();
                                RealmResults<RealmBalance> realmBalances = realm.where(RealmBalance.class).equalTo("wallet_id", wallet.id).findAll();
                                realmBalances.deleteAllFromRealm();
                                realmAccounts.deleteAllFromRealm();
                                realmChains.deleteFromRealm();
                            }

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


    public Single<String> getPrivate(Wallet wallet, RealmChain realmChain) {
        return Single.fromCallable(() -> {
            Realm realm = null;
            final String[] privateKey = {""};
            try {

                realm = RealmHelper.getInstance().getRealmInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        //生成各个链主账户

                        RealmAccount realmAccount = realm.where(RealmAccount.class).equalTo("wallet_id", wallet.id).and().equalTo("chain_id", realmChain.getId()).findFirst();
                        if (realmAccount != null) {
                            privateKey[0] = realmAccount.getPrivate_key();
                        }

                    }
                });
                return privateKey[0];
            } finally {
                if (realm != null) {
                    realm.close();
                }

            }
        }).subscribeOn(Schedulers.io());

    }

    public Observable<List<RealmTransactionNew>> fetchFundTransaction(String wallet_id, String fund_id, int type) {


        return Single.fromCallable(() -> {
            Realm realm = null;
            final String[] privateKey = {""};
            try {
                List<RealmTransactionNew> list = new ArrayList<>();
                realm = RealmHelper.getInstance().getRealmInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        //生成各个链主账户
                        RealmResults<RealmTransactionNew> realmTransactionNews;
                        if (type == C.TURNIN || type == C.TURNOUT) {
                            realmTransactionNews = realm.where(RealmTransactionNew.class).equalTo("wallet_id", wallet_id).and().equalTo("fund_id", fund_id).and().equalTo("type", type).findAll();
                        } else {
                            realmTransactionNews = realm.where(RealmTransactionNew.class).equalTo("wallet_id", wallet_id).and().equalTo("fund_id", fund_id).findAll();
                        }

                        int len = realmTransactionNews.size();
                        realmTransactionNews = realmTransactionNews.sort("update_time", Sort.DESCENDING);
                        for (int i = 0; i < len; i++) {
                            RealmTransactionNew realmTransactionNew = realmTransactionNews.get(i);
                            RealmTransactionNew resultItem = new RealmTransactionNew();
                            resultItem.setContract_address(realmTransactionNew.getContract_address());
                            resultItem.setCreate_time(realmTransactionNew.getCreate_time());
                            resultItem.setFee(realmTransactionNew.getFee());
                            resultItem.setFrom(realmTransactionNew.getFrom());
                            resultItem.setFund_id(realmTransactionNew.getFund_id());
                            resultItem.setHash(realmTransactionNew.getHash());
                            resultItem.setNonce(realmTransactionNew.getNonce());
                            resultItem.setStatus(realmTransactionNew.getStatus());
                            resultItem.setTo(realmTransactionNew.getTo());
                            resultItem.setType(realmTransactionNew.getType());
                            resultItem.setUpdate_time(realmTransactionNew.getUpdate_time());
                            resultItem.setValue(realmTransactionNew.getValue());
                            resultItem.setWallet_id(realmTransactionNew.getWallet_id());
                            list.add(resultItem);
                        }

                    }
                });
                return list;
            } finally {
                if (realm != null) {
                    realm.close();
                }

            }
        }).toObservable().subscribeOn(Schedulers.io());
    }


    public Single<RealmChain> getChain(String chain_id) {
        return Single.fromCallable(() -> {
            Realm realm = null;
            final RealmChain realmChain = new RealmChain();
            try {

                realm = RealmHelper.getInstance().getRealmInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        //生成各个链主账户

                        RealmChain result = realm.where(RealmChain.class).equalTo("id", chain_id).findFirst();

                        if (result != null) {
                            realmChain.setRpc(result.getRpc());
                            realmChain.setChainId(result.getChainId());
                            realmChain.setBackend(result.getBackend());
                            realmChain.setExplorer(result.getExplorer());
                            realmChain.setWeb_site(result.getWeb_site());
                            realmChain.setName(result.getName());
                            realmChain.setRule_address(result.getRule_address());
                            realmChain.setSymbol(result.getSymbol());
                            realmChain.setId(result.getId());
                            realmChain.setIcon(result.getIcon());
                        }

                    }
                });
                return realmChain;
            } finally {
                if (realm != null) {
                    realm.close();
                }

            }
        }).subscribeOn(Schedulers.io());

    }

    public Single<RealmChain> getChain(String name, String chain) {
        return Single.fromCallable(() -> {
            Realm realm = null;
            final RealmChain realmChain = new RealmChain();
            try {

                realm = RealmHelper.getInstance().getRealmInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        //生成各个链主账户

                        RealmChain result = realm.where(RealmChain.class).equalTo(name, chain).findFirst();

                        if (result != null) {
                            realmChain.setRpc(result.getRpc());
                            realmChain.setChainId(result.getChainId());
                            realmChain.setBackend(result.getBackend());
                            realmChain.setExplorer(result.getExplorer());
                            realmChain.setWeb_site(result.getWeb_site());
                            realmChain.setName(result.getName());
                            realmChain.setRule_address(result.getRule_address());
                            realmChain.setSymbol(result.getSymbol());
                            realmChain.setId(result.getId());
                            realmChain.setIcon(result.getIcon());
                        }

                    }
                });
                return realmChain;
            } finally {
                if (realm != null) {
                    realm.close();
                }

            }
        }).subscribeOn(Schedulers.io());

    }

    public RealmAccount getAccount(String chain_id, String wallet_id) {

        Realm realm = null;
        final RealmAccount realmAccount = new RealmAccount();
        try {

            realm = RealmHelper.getInstance().getRealmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    //生成各个链主账户

                    RealmAccount result = realm.where(RealmAccount.class).equalTo("chain_id", chain_id).and().equalTo("wallet_id", wallet_id).findFirst();
                    if (result != null) {
                        realmAccount.setPrivate_key(result.getPrivate_key());
                        realmAccount.setId(result.getId());
                        realmAccount.setSon_order(result.getSon_order());
                        realmAccount.setAddress(result.getAddress());
                        realmAccount.setChain_id(result.getChain_id());
                        realmAccount.setWallet_id(result.getWallet_id());
                        realmAccount.setChain(result.getChain());


                    }

                }
            });
            return realmAccount;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (realm != null) {
                realm.close();
            }

        }
        return realmAccount;
    }

    public Completable addBalance(Wallet wallet, RealmFund fund) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                Realm realm = null;

                try {

                    realm = RealmHelper.getInstance().getRealmInstance();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Log.d("到这了", "1");
                            if (fund != null) {
                                Log.d("到这了", "2");
                                RealmBalance realmBalance = realm.createObject(RealmBalance.class);
                                RealmAccount realmAccount = realm.where(RealmAccount.class).equalTo("wallet_id", wallet.id).equalTo("chain_id", fund.getChain_id()).findFirst();
                                realmBalance.setAccount_id(realmAccount.getId());
                                realmBalance.setAddress(realmAccount.getAddress());
                                realmBalance.setWallet_id(wallet.id);
                                realmBalance.setChainId(fund.getChain_id());
                                realmBalance.setFund_name(fund.getName());
                                realmBalance.setFund_id(fund.getId());
                                realmBalance.setBalance("0");
                                realmBalance.setCreate_time(System.currentTimeMillis());
                                realmBalance.setUpdate_time(System.currentTimeMillis());
                                realmBalance.setRule_address(fund.getRule_address());
                                realmBalance.setDecimals(fund.getDecimals());
                                realmBalance.setGas_symbol(fund.getGas_symbol());
                                realmBalance.setGas_limit(fund.getGas_limit());
                                realmBalance.setGas_limit_max(fund.getGas_limit_max());
                                realmBalance.setGas_limit_min(fund.getGas_limit_min());
                                realmBalance.setGas_price(fund.getGas_price());
                                realmBalance.setGas_price_max(fund.getGas_price_max());
                                realmBalance.setGas_price_min(fund.getGas_price_min());
                                realmBalance.setContract_address(fund.getContract_address());
                                realmBalance.setIcon(fund.getIcon());
                                Log.d("到这了", "3");
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
        });

    }

    @Override
    public void updateBalance(HomeTokenResponse homeTokenResponse) {

        Realm realm = null;
        final RealmAccount realmAccount = new RealmAccount();
        try {

            realm = RealmHelper.getInstance().getRealmInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    //生成各个链主账户
                    if (homeTokenResponse == null || homeTokenResponse.data == null) return;
                    for (int i = 0; i < homeTokenResponse.data.length; i++) {
                        RealmBalance result = realm.where(RealmBalance.class).equalTo("wallet_id", homeTokenResponse.wallet_id).and().equalTo("fund_name", homeTokenResponse.data[i].fund).findFirst();
                        if (result != null) {
                            result.setBalance(homeTokenResponse.data[i].balance);
                            result.setCny_price(homeTokenResponse.data[i].price);
                            result.setCny_balance(homeTokenResponse.data[i].value);
                        }
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
