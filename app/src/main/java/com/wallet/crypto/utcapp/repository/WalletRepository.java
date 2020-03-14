package com.wallet.crypto.utcapp.repository;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.wallet.crypto.utcapp.C;
import com.wallet.crypto.utcapp.entity.GoodAddress;
import com.wallet.crypto.utcapp.entity.HomeToken;
import com.wallet.crypto.utcapp.entity.ServiceException;
import com.wallet.crypto.utcapp.entity.TokenInfo;
import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.repository.entity.RealmTokenInfo;
import com.wallet.crypto.utcapp.repository.realmentity.RealmAccount;
import com.wallet.crypto.utcapp.repository.realmentity.RealmBalance;
import com.wallet.crypto.utcapp.repository.realmentity.RealmChain;
import com.wallet.crypto.utcapp.repository.realmentity.RealmWallet;
import com.wallet.crypto.utcapp.service.AccountKeystoreService;
import com.wallet.crypto.utcapp.service.BtcAccountServiceType;
import com.wallet.crypto.utcapp.util.PreferenceUtils;
import com.wallet.crypto.utcapp.util.RealmDateUtil;
import com.wallet.crypto.utcapp.util.RealmHelper;
import com.wallet.crypto.utcapp.util.StringUtils;

import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import okhttp3.OkHttpClient;

import static com.wallet.crypto.utcapp.util.RealmDateUtil.TYPE_5;
import static com.wallet.crypto.utcapp.util.RealmDateUtil.WALLET_TYPE_0;
import static com.wallet.crypto.utcapp.util.RealmDateUtil.WALLET_TYPE_1;

public class WalletRepository implements WalletRepositoryType {

    private final PreferenceRepositoryType preferenceRepositoryType;
    private final AccountKeystoreService accountKeystoreService;
    private final EthereumNetworkRepositoryType networkRepository;
    private final OkHttpClient httpClient;
    private final RealmWalletSource realmWalletSource;
    private final BtcAccountServiceType btcAccountService;

    public WalletRepository(
            OkHttpClient okHttpClient,
            PreferenceRepositoryType preferenceRepositoryType,
            AccountKeystoreService accountKeystoreService,
            EthereumNetworkRepositoryType networkRepository, RealmWalletSource realmWalletSource, BtcAccountServiceType btcAccountService) {
        this.httpClient = okHttpClient;
        this.preferenceRepositoryType = preferenceRepositoryType;
        this.accountKeystoreService = accountKeystoreService;
        this.networkRepository = networkRepository;
        this.realmWalletSource = realmWalletSource;
        this.btcAccountService = btcAccountService;
    }

    @Override
    public Single<Wallet[]> fetchWallets() {
        return realmWalletSource.fetchWallet();

    }

    @Override
    public Single<Wallet> createMnemonicWallet(String password) {
        return accountKeystoreService
                .createMnemonicAccount();
    }

    /**
     * 创建普通多链或者多链主钱包
     *
     * @param context
     * @param type                      钱包类型
     * @param son                       子账户地址数量
     * @param name                      钱包名字
     * @param select_chain_id           选择的链的id
     * @param select_good_address_index 子账户地址的索引id
     * @return
     */
    @Override
    public Single<Wallet> createMnemonicWalletByType(Context context, String phrase, String type, int son, String name, String select_chain_id, int select_good_address_index) {
        Wallet wallet = new Wallet("");
        wallet.mnemonic = phrase;
        return Single.just(wallet).map(walletMnemonic -> {
            return initWallet(context, walletMnemonic, type, son, name, select_chain_id, select_good_address_index).blockingGet();
        });
    }

    /**
     * 创建普通多链或者多链主钱包
     *
     * @param context
     * @param type    钱包类型
     * @param son     子账户地址数量
     * @param name    钱包名字
     * @return
     */
    public Single<Wallet> createMnemonicWalletByType(Context context, String type, int son, String name) {
        return accountKeystoreService
                .createMnemonicAccount().map(wallet -> {
                    return initWallet(context, wallet, type, son, name, null, 0).blockingGet();
                });
    }

    /**
     * 创建ETH靓号地址
     *
     * @param mnemonic 助记词
     * @return
     */
    @Override
    public Observable<GoodAddress> createEthGoodAddress(String mnemonic) {
        return accountKeystoreService
                .createEthGoodAddress(mnemonic);
    }

    /**
     * 创建Btc靓号地址
     *
     * @param mnemonic 助记词
     * @return
     */
    @Override
    public Observable<GoodAddress> createBtcGoodAddress(String mnemonic) {
        return btcAccountService
                .createBtcGoodAddress(mnemonic);
    }

    /**
     * 找回ETH私钥
     *
     * @param address 需要找回的地址
     * @return
     */
    @Override
    public Observable<GoodAddress> findEthPrivateKey(String address) {
        return accountKeystoreService
                .findEthPrivate(address);
    }

    /**
     * 找回BTC私钥
     *
     * @param address 需要找回的地址
     * @return
     */
    @Override
    public Observable<GoodAddress> findBtcPrivateKey(String address) {
        return btcAccountService
                .findBtcPrivate(address);
    }

    /**
     * 导入助记词
     *
     * @param context
     * @param phrase  助记词
     * @param type    钱包类型
     * @param son     子账户数量
     * @param name    钱报名字
     * @return
     */
    @Override
    public Single<Wallet> importPhraseByType(Context context, String phrase, String type, int son, String name) {
        Wallet walletNews = new Wallet("");
        walletNews.mnemonic = phrase;
        return initWallet(context, walletNews, type, son, name, "", 0);
    }

    public boolean hasAccount(String phrase) {
        //  return keyStore.hasAddress(new Address(address));
        RealmWalletSource realmWalletSource = new RealmWalletSource();
        RealmWallet wallet = realmWalletSource.getWallet(phrase);
        if (wallet != null && !TextUtils.isEmpty(wallet.getKey()) && wallet.getKey().equals(phrase)) {
            return true;
        }
        return false;
    }


    /**
     * 创建钱包 生成表数据  靓号钱包 wallet  account  balance
     *
     * @param context
     * @param wallet
     * @param type
     * @param son
     * @param name
     * @return
     */
    public Single<Wallet> initWallet(Context context, Wallet wallet, String type, int son, String name, String select_chain_id, int select_good_address_index) {

        return Single.just(wallet).map(walletNew -> {

            //把钱包存入wallet表
            walletNew.type = type;
            walletNew.name = name;
            walletNew.son = son;
            if (type.equals(WALLET_TYPE_0) || type.equals(WALLET_TYPE_1)) {
                walletNew.id = walletNew.mnemonic;
            } else {
                walletNew.id = walletNew.private_key;
            }
            if (hasAccount(walletNew.id)) {
                throw new ServiceException(C.REPEAT_IMPORT_WALLET);
            }
            realmWalletSource.createWallet(context, walletNew);
            return walletNew;
        }).map(walletNew -> {
            Realm realm = null;
            try {
                realm = RealmHelper.getInstance().getRealmInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        //生成各个链主账户
                        RealmResults<RealmChain> realmChains = realm.where(RealmChain.class).findAll();
                        int len = realmChains.size();
                        for (int i = 0; i < len; i++) {
                            RealmChain realmItem = realmChains.get(i);
                            if (realmItem == null) continue;
                            if (TextUtils.isEmpty(realmItem.getRule_address())) continue;
                            String chain_id = realmItem.getId();
                            int son_order = 0;
                            //如果公链和生成靓号的公链一致就用select_good_address_index作为账户的son_order
                            if (!TextUtils.isEmpty(select_chain_id) && select_chain_id.equals(chain_id)) {
                                son_order = select_good_address_index;
                            }
                            if (realmItem.getRule_address().equals("ETH")) {
                                Wallet privatewallet = accountKeystoreService.createEthAccount(walletNew.mnemonic, son_order).blockingGet();
                                Log.d("1生成钱包", privatewallet.address);
                                //生成各个链主账户表数据  资产表数据
                                realmWalletSource.createAccountBalance(realm, walletNew, privatewallet, realmItem.getName(), chain_id, son_order);
                            } else if (realmItem.getRule_address().equals("BTC")) {
                                Wallet privatewallet = btcAccountService.createBtcAccount(walletNew.mnemonic, son_order).blockingGet();
                                Log.d("1生成钱包", privatewallet.address);
                                //生成各个链主账户表数据  资产表数据
                                realmWalletSource.createAccountBalance(realm, walletNew, privatewallet, realmItem.getName(), chain_id, son_order);
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
            return walletNew;
        });
    }


    @Override
    public Single<Wallet> importKeystoreToWallet(Context context, String store, String password, String type, int son, String name, RealmChain realmChain) {
        return accountKeystoreService.importKeystore(store, password).map(walletNew -> {
            //把钱包存入wallet表
            walletNew.type = type;
            walletNew.name = name;
            walletNew.son = son;
            if (type.equals(WALLET_TYPE_0) || type.equals(WALLET_TYPE_1)) {
                walletNew.id = walletNew.mnemonic;
            } else {
                walletNew.id = walletNew.private_key;
            }
            if (hasAccount(walletNew.id)) {
                throw new ServiceException(C.REPEAT_IMPORT_WALLET);
            }
            realmWalletSource.createWallet(context, walletNew);
            return walletNew;
        }).map(walletNew -> {
            Realm realm = null;
            try {
                realm = RealmHelper.getInstance().getRealmInstance();
                realm.executeTransaction(realm1 -> {

                    realmWalletSource.createAccountBalance(realm1, walletNew, walletNew, realmChain.getName(), realmChain.getId(), 0);


                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }
            return walletNew;
        });
    }

    @Override
    public Single<Wallet> importPrivateKeyToWallet(Context context, String privateKey, String type, int son, String name, RealmChain realmChain) {

        return Single.fromCallable(() -> {
            if (realmChain.getRule_address().equals("ETH")) {
                return accountKeystoreService.importPrivateKey(privateKey).blockingGet();
            } else if (realmChain.getRule_address().equals("BTC")) {
                return btcAccountService.importBtcPrivateKey(privateKey).blockingGet();
            } else {
                throw new ServiceException(C.COMMON_ERROR);
            }
        }).map(walletNew -> {
            //把钱包存入wallet表
            walletNew.type = type;
            walletNew.name = name;
            walletNew.son = son;
            if (type.equals(WALLET_TYPE_0) || type.equals(WALLET_TYPE_1)) {
                walletNew.id = walletNew.mnemonic;
            } else {
                walletNew.id = walletNew.private_key;
            }
            if (hasAccount(walletNew.id)) {
                throw new ServiceException(C.REPEAT_IMPORT_WALLET);
            }
            realmWalletSource.createWallet(context, walletNew);
            return walletNew;
        }).map(walletNew -> {
            Realm realm = null;
            try {
                realm = RealmHelper.getInstance().getRealmInstance();
                realm.executeTransaction(realm1 -> {
                    //生成各个链主账户表数据  资产表数据
                    realmWalletSource.createAccountBalance(realm1, walletNew, walletNew, realmChain.getName(), realmChain.getId(), 0);
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }
            return walletNew;
        });
    }

    @Override
    public Single<Wallet> importWatch(Context context, String address, String type, int son, String name, RealmChain realmChain) {
        return Single.just(address).map(address1 -> {
            //把钱包存入wallet表
            Wallet walletNew = new Wallet(address);
            walletNew.type = type;
            walletNew.name = name;
            walletNew.son = son;
            walletNew.id = realmChain.getId() + walletNew.address;
            walletNew.private_key = realmChain.getId() + walletNew.address;
            if (hasAccount(walletNew.id)) {
                throw new ServiceException(C.REPEAT_IMPORT_WALLET);
            }
            realmWalletSource.createWallet(context, walletNew);
            return walletNew;
        }).map(walletNew -> {
            Realm realm = null;
            try {
                realm = RealmHelper.getInstance().getRealmInstance();
                realm.executeTransaction(realm1 -> {
                    //生成各个链主账户表数据  资产表数据
                    realmWalletSource.createAccountBalance(realm1, walletNew, walletNew, realmChain.getName(), realmChain.getId(), 0);
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }
            return walletNew;
        });
    }

    @Override
    public Single<String> exportWallet(Wallet wallet, String newPassword, RealmChain realmChain) {
        return accountKeystoreService.exportAccount(wallet, newPassword, realmChain);
    }

    @Override
    public Single<String> exportPhrase(Wallet wallet, String password) {
        return accountKeystoreService.exportPhrase(wallet, password);
    }

    @Override
    public Single<String> exportPrivateKey(Wallet wallet, RealmChain realmChain) {

        return realmWalletSource.getPrivate(wallet, realmChain).map(s -> {
            if (TextUtils.isEmpty(s)) {
                //动态适配添加了新链以后多链和多链主钱包realmAccount表没有产生账户私钥的情况
                if (wallet.type.equals(WALLET_TYPE_0) || wallet.type.equals(WALLET_TYPE_1)) {
                    if (realmChain.getRule_address().equals("ETH")) {
                        s = accountKeystoreService.createEthAccount(wallet.mnemonic, 0).blockingGet().private_key;
                    } else if (realmChain.getRule_address().equals("BTC")) {
                        s = btcAccountService.createBtcAccount(wallet.mnemonic, 0).blockingGet().private_key;
                    }
                }
            }
            return s;
        });
    }

    @Override
    public Completable deleteWallet(String address, String password) {
        return accountKeystoreService.deleteAccount(address, password);
    }

    @Override
    public Single<String[]> exportSonAddress(Wallet wallet, RealmChain realmChain) {
        if (realmChain.getRule_address().equals("ETH")) {
            return accountKeystoreService.exportSonAddress(wallet);
        } else if (realmChain.getRule_address().equals("BTC")) {
            return btcAccountService.exportSonAddress(wallet);
        } else {
            return Single.fromCallable(() -> {
                throw new ServiceException(C.COMMON_ERROR);
            });
        }
    }

    @Override
    public Single<String[][]> exportSonPrivateAddress(Wallet wallet, RealmChain realmChain) {
        if (realmChain.getRule_address().equals("ETH")) {
            return accountKeystoreService.exportSonPrivateAddress(wallet);
        } else if (realmChain.getRule_address().equals("BTC")) {
            return btcAccountService.exportSonPrivateAddress(wallet);
        } else {
            return Single.fromCallable(() -> {
                throw new ServiceException(C.COMMON_ERROR);
            });
        }
    }

    @Override
    public Completable setDefaultWallet(Wallet wallet) {
        return Completable.fromAction(() -> realmWalletSource.setDefault(wallet));
    }

    @Override
    public Completable setIsSkipBackup(Wallet wallet, boolean isSkip) {
        return Completable.fromAction(() -> preferenceRepositoryType.setIsSkipBackUp(wallet, isSkip));
    }

    @Override
    public Single<Boolean> getIsSkipBackup(Wallet wallet) {
        return Single.fromCallable(() -> preferenceRepositoryType.getIsSkipBackUp(wallet));
    }

    @Override
    public Single<Wallet> getDefaultWallet() {
        return realmWalletSource.getDefaultWallet().flatMap(this::fetchBalances);
    }

    //获取资产  从balance表
    public Single<Wallet> fetchBalances(Wallet wallet) {
        return Single.fromCallable(() -> {
            return realmWalletSource.fetchBalances(wallet);
        });

    }


    @Override
    public Single<byte[]> signMessage(Wallet wallet, byte[] array, String s) {
        return accountKeystoreService.signMessage(wallet, array, s);
    }
}