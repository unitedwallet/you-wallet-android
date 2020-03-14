package com.wallet.crypto.utcapp.service;

import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.crypto.utcapp.C;
import com.wallet.crypto.utcapp.entity.GoodAddress;
import com.wallet.crypto.utcapp.entity.HomeToken;
import com.wallet.crypto.utcapp.entity.ServiceException;
import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.repository.RealmWalletSource;
import com.wallet.crypto.utcapp.repository.realmentity.RealmAccount;
import com.wallet.crypto.utcapp.repository.realmentity.RealmChain;
import com.wallet.crypto.utcapp.repository.realmentity.RealmWallet;
import com.wallet.crypto.utcapp.ui.wallet.CreateGoodAddressActivity;
import com.wallet.crypto.utcapp.util.CryptoUtils;
import com.wallet.crypto.utcapp.util.FileUtil;

import com.wallet.crypto.utcapp.util.StringUtils;
import com.wallet.crypto.utcapp.widget.MnemonicUtils;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.HDUtils;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.Protos;


import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;

import org.web3j.crypto.Keys;
import org.web3j.crypto.RawTransaction;

import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Numeric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.KeyPairGenerator;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


import static com.wallet.crypto.utcapp.ui.findkey.FindPrivateActivity.cancelFind;
import static com.wallet.crypto.utcapp.ui.wallet.CreateGoodAddressActivity.isCancel;

import static org.web3j.crypto.Hash.sha256;
import static org.web3j.crypto.Wallet.create;
import static org.web3j.crypto.Wallet.decrypt;


public class GethKeystoreAccountService implements AccountKeystoreService {
    private static final int PRIVATE_KEY_RADIX = 16;
    /**
     * CPU/Memory cost parameter. Must be larger than 1, a power of 2 and less than 2^(128 * r / 8).
     */
    private static final int N = 1 << 9;
    /**
     * Parallelization parameter. Must be a positive integer less than or equal to Integer.MAX_VALUE / (128 * r * 8).
     */
    private static final int P = 1;

    // private final KeyStore keyStore;
    private final File storeDir;

    public GethKeystoreAccountService(File keyStoreFile) {
        //   keyStore = new KeyStore(keyStoreFile.getAbsolutePath(), Geth.LightScryptN, Geth.LightScryptP);
        this.storeDir = keyStoreFile;
        if (!storeDir.exists() && !storeDir.mkdirs()) {
            Crashlytics.logException((Throwable) new IOException("Failed to create key store directory."));
        }
    }

  /*  public GethKeystoreAccountService(KeyStore keyStore, File keyStoreFile) {
        this.keyStore = keyStore;
        this.storeDir = keyStoreFile;
    }*/

//    @Override
//    public Single<Wallet> createAccount(String password) {
//       /* return Single.fromCallable(() -> new Wallet(
//                keyStore.newAccount(password).getAddress().getHex().toLowerCase()))
//                .subscribeOn(Schedulers.io());*/
//        return Single.fromCallable(() -> WalletUtils.generateLightNewWalletFile(password, storeDir))
//                .map(string -> WalletUtils.loadCredentials(password, new File(storeDir, string)))
//                .map(credentials -> {
//                    String value = credentials.getAddress().toLowerCase();
//                    if (Numeric.containsHexPrefix(value)) {
//                        return new Wallet(value);
//                    } else {
//                        return new Wallet(Numeric.prependHexPrefix(value));
//                    }
//                });
//
//    }

    //
    @Override
    public Single<Wallet> createMnemonicAccount() {
        Wallet wallet = new Wallet("");
        return Single.fromCallable(() -> {
            byte[] initialEntropy = new byte[16];
            new SecureRandom().nextBytes(initialEntropy);
            String mnemonic = MnemonicUtils.generateMnemonic(initialEntropy);
            wallet.mnemonic = mnemonic;
            return wallet;
        });
//        .map(string -> {
//            wallet.mnemonic = string;
//            //  Log.d("2出错", wallet.mnemonic);
//            byte[] seed = MnemonicUtils.generateSeed(string, null);
//            // 3.生成 私钥
//            DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
//            // 4. 由根私钥生成 第一个HD 钱包
//            DeterministicHierarchy dh = new DeterministicHierarchy(rootPrivateKey);
//            // 5. 定义父路径 H则是加强 imtoken中的eth钱包进过测试发现使用的是此方式生成 bip44
//            List<ChildNumber> parentPath = HDUtils.parsePath("M/44H/60H/0H/0");
//            // 6. 由父路径,派生出第一个子私钥 "new ChildNumber(0)" 表示第一个 （m/44'/60'/0'/0/0）
//            DeterministicKey child = dh.deriveChild(parentPath, true, true, new ChildNumber(0));
//            byte[] privateKeyByte = child.getPrivKeyBytes();
//            //7.通过私钥生成公私钥对
//            ECKeyPair keyPair = ECKeyPair.create(privateKeyByte);
//            //  Log.i("TAG", "generateBip44Wallet: 钥匙对  私钥 = " + Numeric.toHexStringNoPrefix(keyPair.getPrivateKey()));
//            //  Log.i("TAG", "generateBip44Wallet: 钥匙对  公钥 = " + Numeric.toHexStringNoPrefix(keyPair.getPublicKey()));
//            //8.通过密码和钥匙对生成WalletFile也就是keystore的bean类
//            WalletFile walletFile = org.web3j.crypto.Wallet.createLight(password, keyPair);
//            return walletFile.getAddress();
//        }).map(address -> {
//            String value = address;
//            File destination;
//            if (Numeric.containsHexPrefix(value)) {
//                destination = new File(storeDir, getWalletFileName(Numeric.cleanHexPrefix(value)));
//                wallet.address = value;
//            } else {
//                destination = new File(storeDir, getWalletFileName(value));
//                wallet.address = Numeric.prependHexPrefix(value);
//
//            }
//            new ObjectMapper().writeValue(destination, wallet.mnemonic);
//            return wallet;
//        })

    }

    /**
     * @param mnemonic
     * @param index    第几个子账户地址 0是主地址
     * @return
     */
    @Override
    public Single<Wallet> createEthAccount(String mnemonic, int index) {
        Wallet wallet = new Wallet("");
        return Single.fromCallable(() -> {
            wallet.mnemonic = mnemonic;
            //  Log.d("2出错", wallet.mnemonic);
            byte[] seed = MnemonicUtils.generateSeed(mnemonic, null);
            // 3.生成 私钥
            DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
            // 4. 由根私钥生成 第一个HD 钱包
            DeterministicHierarchy dh = new DeterministicHierarchy(rootPrivateKey);
            // 5. 定义父路径 H则是加强 imtoken中的eth钱包进过测试发现使用的是此方式生成 bip44
            List<ChildNumber> parentPath = HDUtils.parsePath("M/44H/60H/0H/0");
            // 6. 由父路径,派生出第一个子私钥 "new ChildNumber(0)" 表示第一个 （m/44'/60'/0'/0/0）
            DeterministicKey child = dh.deriveChild(parentPath, true, true, new ChildNumber(index));
            byte[] privateKeyByte = child.getPrivKeyBytes();
            //7.通过私钥生成公私钥对
            ECKeyPair keyPair = ECKeyPair.create(privateKeyByte);
            //8.通过密码和钥匙对生成WalletFile也就是keystore的bean类
            WalletFile walletFile = org.web3j.crypto.Wallet.createLight("", keyPair);
            if (Numeric.containsHexPrefix(walletFile.getAddress())) {
                wallet.address = walletFile.getAddress();
            } else {
                wallet.address = Numeric.prependHexPrefix(walletFile.getAddress());
            }
            wallet.private_key = Numeric.toHexStringNoPrefix(keyPair.getPrivateKey());
            return wallet;
        });

    }

    @Override
    public Observable<GoodAddress> createEthGoodAddress(String mnemonic) {

        return Observable.create(e -> {
            byte[] seed = MnemonicUtils.generateSeed(mnemonic, null);
            // 3.生成 私钥
            DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
            // 4. 由根私钥生成 第一个HD 钱包
            DeterministicHierarchy dh = new DeterministicHierarchy(rootPrivateKey);
            // 5. 定义父路径 H则是加强 imtoken中的eth钱包进过测试发现使用的是此方式生成 bip44
            List<ChildNumber> parentPath = HDUtils.parsePath("M/44H/60H/0H/0");
            // 6. 由父路径,派生出第一个子私钥 "new ChildNumber(0)" 表示第一个 （m/44'/60'/0'/0/0）
            int index = 0;
            int count = 0;
            long start = System.currentTimeMillis();

            while (!isCancel && count < 6) {
                //大于60s停止生成地址
                if (System.currentTimeMillis() - start >= 60 * 1000) break;
                DeterministicKey child = dh.deriveChild(parentPath, true, true, new ChildNumber(index));
                byte[] privateKeyByte = child.getPrivKeyBytes();
                //7.通过私钥生成公私钥对
                ECKeyPair keyPair = ECKeyPair.create(privateKeyByte);
                //8.通过密码和钥匙对生成WalletFile也就是keystore的bean类
                WalletFile walletFile = org.web3j.crypto.Wallet.createLight("", keyPair);
                GoodAddress goodAddress = new GoodAddress();
                if (Numeric.containsHexPrefix(walletFile.getAddress())) {
                    goodAddress.address = walletFile.getAddress();
                } else {
                    goodAddress.address = Numeric.prependHexPrefix(walletFile.getAddress());
                }
                goodAddress.index = index;
                goodAddress.is_good = StringUtils.isMatchGoodAddress(Numeric.cleanHexPrefix(goodAddress.address));
                if (goodAddress.is_good) {
                    count++;

                }
                Log.d("找到几个了", count + "    " + index);
                e.onNext(goodAddress);
                index++;
            }

            e.onComplete();
        });

    }

    @Override
    public Observable<GoodAddress> findEthPrivate(String address) {


        return Observable.create(e -> {

            boolean isFind = false;
            while (!cancelFind && !isFind) {
                //大于60s停止生成地址
                // FileUtil. systemLog("开始1"+"\n");

                ECKeyPair ecKeyPair = Keys.createEcKeyPair();
                WalletFile walletFile;
                walletFile = org.web3j.crypto.Wallet.create("", ecKeyPair, 2, P);
                GoodAddress goodAddress = new GoodAddress();
                if (Numeric.containsHexPrefix(walletFile.getAddress())) {
                    goodAddress.address = walletFile.getAddress();
                } else {
                    goodAddress.address = Numeric.prependHexPrefix(walletFile.getAddress());
                }
                goodAddress.privateKey = Numeric.toHexStringNoPrefix(ecKeyPair.getPrivateKey());
                if (goodAddress.address.equals(address)) {
                    isFind = true;
                }

                goodAddress.is_good = goodAddress.address.equals(address);
                //   FileUtil. systemLog("开始2"+"\n");
                Thread.sleep(200);
                e.onNext(goodAddress);
            }

            e.onComplete();
        });
    }


    public Single<Wallet> createBitCoinMnemonicAccount(String password) {
        Wallet wallet = new Wallet("");
        return Single.fromCallable(() -> {
            byte[] initialEntropy = new byte[16];
            new SecureRandom().nextBytes(initialEntropy);
            String mnemonic = MnemonicUtils.generateMnemonic(initialEntropy);
            // String mnemonic="abandon adult aerobic agree airport feed festival figure first flush found fruit";
            //       String walletFile = WalletUtils.generateWalletFile(password, privateKey, storeDir, false);
            return mnemonic;
        }).map(string -> {
            wallet.mnemonic = string;
            string = "wave reveal air dinner plunge safe victory flock hammer alley bonus high";
            //  Log.d("2出错", wallet.mnemonic);
            byte[] seed = MnemonicUtils.generateSeed(string, null);
            // 3.生成 私钥
            DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
            // 4. 由根私钥生成 第一个HD 钱包
            DeterministicHierarchy dh = new DeterministicHierarchy(rootPrivateKey);
            // 5. 定义父路径 H则是加强 imtoken中的eth钱包进过测试发现使用的是此方式生成 bip44
            //List<ChildNumber> parentPath = HDUtils.parsePath("M/44H/60H/0H/0");
            List<ChildNumber> parentPath = HDUtils.parsePath("M/44H/0H/0H/0");
            // 6. 由父路径,派生出第一个子私钥 "new ChildNumber(0)" 表示第一个 （m/44'/0'/0'/0/0）
            DeterministicKey child = dh.deriveChild(parentPath, true, true, new ChildNumber(0));
            byte[] privateKeyByte = child.getPrivKeyBytes();
            ECKey key = ECKey.fromPrivate(privateKeyByte);


            Log.d("比特币钱包助记词", string);
            Log.d("比特币钱包地址", fromPrivateKey(privateKeyByte));
            Log.d("比特币钱包私钥", key.getPrivateKeyAsWiF(MainNetParams.get()));

            return fromPrivateKey(privateKeyByte);
        }).map(address -> {
            String value = address;
            wallet.address = value;


            return wallet;
        });
    }

    //私钥转换地址
    public String fromPrivateKey(byte[] prvKeyBytes) {
        ECKey key = ECKey.fromPrivate(prvKeyBytes);
        return key.toAddress(MainNetParams.get()).toBase58();
    }

    private static String getWalletFileName(String address) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("'UTC--'yyyy-MM-dd'T'HH-mm-ss.SSS'--'");
        return dateFormat.format(new Date()) + address + ".json";
    }

    @Override
    public Single<Wallet> importKeystore(String store, String password) {

        return Single.fromCallable(() -> {
            Wallet wallet = new Wallet("");
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            WalletFile string2 = objectMapper.readValue(store, WalletFile.class);
            ECKeyPair ecKeyPair = org.web3j.crypto.Wallet.decrypt(password, string2);
            wallet.private_key = Numeric.toHexStringNoPrefix(ecKeyPair.getPrivateKey());

            if (Numeric.containsHexPrefix(string2.getAddress())) {
                wallet.address = string2.getAddress();
            } else {
                wallet.address = Numeric.prependHexPrefix(string2.getAddress());
            }

            return wallet;
        });
    }

    @Override
    public Single<Wallet> importPrivateKey(String privateKey) {
        Wallet wallet = new Wallet("");
        return Single.fromCallable(() -> {
            BigInteger key = new BigInteger(privateKey, PRIVATE_KEY_RADIX);
            ECKeyPair keypair = ECKeyPair.create(key);
            WalletFile walletFile = create("", keypair, N, P);
            wallet.private_key = Numeric.toHexStringNoPrefix(keypair.getPrivateKey());
            if (Numeric.containsHexPrefix(walletFile.getAddress())) {
                wallet.address = walletFile.getAddress();
            } else {
                wallet.address = Numeric.prependHexPrefix(walletFile.getAddress());
            }

            return wallet;
        });
    }

    @Override
    public Single<Wallet> importPhrase(String phrasde, String newPassword) {
        Wallet wallet = new Wallet("");
        return Single.fromCallable(() -> phrasde)
                .map(string -> {
                    wallet.mnemonic = string;
                    //  Log.d("2出错", wallet.mnemonic);
                    byte[] seed = MnemonicUtils.generateSeed(string, null);
                    DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
                    // 4. 由根私钥生成 第一个HD 钱包
                    DeterministicHierarchy dh = new DeterministicHierarchy(rootPrivateKey);
                    // 5. 定义父路径 H则是加强 imtoken中的eth钱包进过测试发现使用的是此方式生成 bip44
                    List<ChildNumber> parentPath = HDUtils.parsePath("M/44H/60H/0H/0");
                    // 6. 由父路径,派生出第一个子私钥 "new ChildNumber(0)" 表示第一个 （m/44'/60'/0'/0/0）
                    DeterministicKey child = dh.deriveChild(parentPath, true, true, new ChildNumber(0));
                    byte[] privateKeyByte = child.getPrivKeyBytes();
                    //7.通过私钥生成公私钥对
                    ECKeyPair keyPair = ECKeyPair.create(privateKeyByte);
                    //  Log.i("TAG", "generateBip44Wallet: 钥匙对  私钥 = " + Numeric.toHexStringNoPrefix(keyPair.getPrivateKey()));
                    //  Log.i("TAG", "generateBip44Wallet: 钥匙对  公钥 = " + Numeric.toHexStringNoPrefix(keyPair.getPublicKey()));
                    //8.通过密码和钥匙对生成WalletFile也就是keystore的bean类
                    WalletFile walletFile = org.web3j.crypto.Wallet.createLight(newPassword, keyPair);
                    if (walletFile != null && walletFile.getAddress() != null && hasAccount(walletFile.getAddress())) {
                        throw new ServiceException("这个地址的钱包已添加");
                    }
                    return walletFile.getAddress();
                }).map(address -> {
                    String value = address;
                    File destination;
                    if (Numeric.containsHexPrefix(value)) {
                        destination = new File(storeDir, getWalletFileName(Numeric.cleanHexPrefix(value)));
                        wallet.address = value;
                    } else {
                        destination = new File(storeDir, getWalletFileName(value));
                        wallet.address = Numeric.prependHexPrefix(value);

                    }
                    new ObjectMapper().writeValue(destination, wallet.mnemonic);
                    return wallet;
                });
    }

    //    @Override
//    public Single<String> exportAccount(Wallet wallet, String password, String newPassword) {
//        return Single
//                .fromCallable(() -> findAccount(wallet.address))
//                .flatMap(account1 -> Single.fromCallable(()
//                        -> new String(keyStore.exportKey(account1, password, newPassword))))
//                .subscribeOn(Schedulers.io());
//    }
    @Override
    public Single<String> exportAccount(Wallet wallet, String newPassword, RealmChain realmChain) {
      /*  return Single
                .fromCallable(() -> findAccount(wallet.address))
                .flatMap(account1 -> Single.fromCallable(()
                                -> {
                            String str = new String(keyStore.exportKey(account1, password, newPassword));
                            return str;
                        }
                        )
                )
                .subscribeOn(Schedulers.io());*/
        return Single.just(wallet)
                .map(wallet1 -> {
                    if (wallet.type.equals("0") || wallet.type.equals("1")) {
                        byte[] seed = MnemonicUtils.generateSeed((String) wallet1.mnemonic, null);
                        DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
                        // 4. 由根私钥生成 第一个HD 钱包
                        DeterministicHierarchy dh = new DeterministicHierarchy(rootPrivateKey);
                        // 5. 定义父路径 H则是加强 imtoken中的eth钱包进过测试发现使用的是此方式生成 bip44
                        List<ChildNumber> parentPath = HDUtils.parsePath("M/44H/60H/0H/0");
                        // 6. 由父路径,派生出第一个子私钥 "new ChildNumber(0)" 表示第一个 （m/44'/60'/0'/0/0）
                        DeterministicKey child = dh.deriveChild(parentPath, true, true, new ChildNumber(0));
                        byte[] privateKeyByte = child.getPrivKeyBytes();
                        //7.通过私钥生成公私钥对
                        ECKeyPair keyPair = ECKeyPair.create(privateKeyByte);
                        //8.通过密码和钥匙对生成WalletFile也就是keystore的bean类
                        WalletFile walletFile = create(newPassword, keyPair, N, P);
                        ;
                        return walletFile;
                    } else {
                        BigInteger key = new BigInteger(wallet.private_key, PRIVATE_KEY_RADIX);
                        ECKeyPair keypair = ECKeyPair.create(key);
                        return create(newPassword, keypair, N, P);
                    }
                })
                .map(walletFile -> new ObjectMapper().writeValueAsString(walletFile));
    }

    @Override
    public Single<String> exportPrivateKey(Wallet wallet, String password) {
      /*  return Single
                .fromCallable(() -> findAccount(wallet.address))
                .flatMap(account1 -> Single.fromCallable(()
                                -> {
                            String str = new String(keyStore.exportKey(account1, password, newPassword));
                            return str;
                        }
                        )
                )
                .subscribeOn(Schedulers.io());*/

        return this.getStoreFile(wallet.address)
                .map(file -> {
                    Object mimic = new ObjectMapper().readValue(file, Object.class);
                    if (mimic instanceof String && !TextUtils.isEmpty((String) mimic) && ((String) mimic).split(" ").length == 12) {
                        byte[] seed = MnemonicUtils.generateSeed((String) mimic, null);
                        DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
                        // 4. 由根私钥生成 第一个HD 钱包
                        DeterministicHierarchy dh = new DeterministicHierarchy(rootPrivateKey);
                        // 5. 定义父路径 H则是加强 imtoken中的eth钱包进过测试发现使用的是此方式生成 bip44
                        List<ChildNumber> parentPath = HDUtils.parsePath("M/44H/60H/0H/0");
                        // 6. 由父路径,派生出第一个子私钥 "new ChildNumber(0)" 表示第一个 （m/44'/60'/0'/0/0）
                        DeterministicKey child = dh.deriveChild(parentPath, true, true, new ChildNumber(0));
                        byte[] privateKeyByte = child.getPrivKeyBytes();
                        //7.通过私钥生成公私钥对
                        ECKeyPair keyPair = ECKeyPair.create(privateKeyByte);
                        // Log.i("TAG", "generateBip44Wallet: 钥匙对  私钥 = " + Numeric.toHexStringNoPrefix(keyPair.getPrivateKey()));

                        //8.通过密码和钥匙对生成WalletFile也就是keystore的bean类
                        return Numeric.toHexStringNoPrefix(keyPair.getPrivateKey());
                    } else {
                        return Numeric.toHexStringNoPrefix(WalletUtils.loadCredentials(password, file).getEcKeyPair().getPrivateKey());
                    }
                });
    }

    @Override
    public Single<String> exportPhrase(Wallet wallet, String password) {
        return this.getStoreFile(wallet.address)
                .map(file -> new ObjectMapper().readValue(file, String.class));
    }

    public Single<String> exportHomePhrase(Wallet wallet) {
        return this.getStoreFile(wallet.address)
                .map(file -> new ObjectMapper().readValue(file, String.class));
    }

    @Override
    public Completable deleteAccount(String address, String password) {
     /*   return Single.fromCallable(() -> findAccount(address))
                .flatMapCompletable(account -> Completable.fromAction(
                        () -> keyStore.deleteAccount(account, password)))
                .subscribeOn(Schedulers.io());*/
        //TODO 减少体积
        return Completable.fromAction(() -> {
            File[] arrfile = storeDir.listFiles();
            String object = Numeric.cleanHexPrefix(address.toString());
            for (File file : arrfile) {
                if (!file.getName().contains((CharSequence) object) || file.delete()) continue;
                //  Log.d("KEY_STORE_DS_TAG", (String) "Failed to delete key store file.");
            }
        });
    }

    @Override
    public Single<String[]> exportSonAddress(Wallet wallet) {

        return Single.fromCallable(() -> {

            //  Log.d("2出错", wallet.mnemonic);
            byte[] seed = MnemonicUtils.generateSeed(wallet.mnemonic, null);
            // 3.生成 私钥
            DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
            // 4. 由根私钥生成 第一个HD 钱包
            DeterministicHierarchy dh = new DeterministicHierarchy(rootPrivateKey);
            // 5. 定义父路径 H则是加强 imtoken中的eth钱包进过测试发现使用的是此方式生成 bip44
            List<ChildNumber> parentPath = HDUtils.parsePath("M/44H/60H/0H/0");
            // 6. 由父路径,派生出第一个子私钥 "new ChildNumber(0)" 表示第一个 （m/44'/60'/0'/0/0）
            String[] addresss = new String[wallet.son];
            for (int i = 0; i < wallet.son; i++) {
                DeterministicKey child = dh.deriveChild(parentPath, true, true, new ChildNumber(i));
                byte[] privateKeyByte = child.getPrivKeyBytes();
                //7.通过私钥生成公私钥对
                ECKeyPair keyPair = ECKeyPair.create(privateKeyByte);
                //8.通过密码和钥匙对生成WalletFile也就是keystore的bean类

                WalletFile walletFile = create("", keyPair, 2, P);
                if (Numeric.containsHexPrefix(walletFile.getAddress())) {
                    addresss[i] = walletFile.getAddress();
                } else {
                    addresss[i] = Numeric.prependHexPrefix(walletFile.getAddress());
                }

            }

            return addresss;
        });
    }

    @Override
    public Single<String[][]> exportSonPrivateAddress(Wallet wallet) {

        return Single.fromCallable(() -> {

            //  Log.d("2出错", wallet.mnemonic);
            byte[] seed = MnemonicUtils.generateSeed(wallet.mnemonic, null);
            // 3.生成 私钥
            DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
            // 4. 由根私钥生成 第一个HD 钱包
            DeterministicHierarchy dh = new DeterministicHierarchy(rootPrivateKey);
            // 5. 定义父路径 H则是加强 imtoken中的eth钱包进过测试发现使用的是此方式生成 bip44
            List<ChildNumber> parentPath = HDUtils.parsePath("M/44H/60H/0H/0");
            // 6. 由父路径,派生出第一个子私钥 "new ChildNumber(0)" 表示第一个 （m/44'/60'/0'/0/0）
            String[][] addresss = new String[wallet.son][2];
            for (int i = 0; i < wallet.son; i++) {
                DeterministicKey child = dh.deriveChild(parentPath, true, true, new ChildNumber(i));
                byte[] privateKeyByte = child.getPrivKeyBytes();
                //7.通过私钥生成公私钥对
                ECKeyPair keyPair = ECKeyPair.create(privateKeyByte);
                //8.通过密码和钥匙对生成WalletFile也就是keystore的bean类

                WalletFile walletFile = create("", keyPair, 2, P);
                addresss[i][0] = Numeric.toHexStringNoPrefix(keyPair.getPrivateKey());
                if (Numeric.containsHexPrefix(walletFile.getAddress())) {
                    addresss[i][1] = walletFile.getAddress();
                } else {
                    addresss[i][1] = Numeric.prependHexPrefix(walletFile.getAddress());
                }
            }

            return addresss;
        });
    }

    @Override
    public Single<byte[]> signTransaction(Wallet signer, String signerPassword, String toAddress, BigInteger amount, BigInteger gasPrice, BigInteger gasLimit, long nonce, byte[] data, long chainId) {
        return this.getStoreFile(signer.address)
                .map(file -> {
                    Object mimic = new ObjectMapper().readValue(file, Object.class);
                    if (mimic instanceof String && !TextUtils.isEmpty((String) mimic) && ((String) mimic).split(" ").length == 12) {
                        byte[] seed = MnemonicUtils.generateSeed((String) mimic, null);
                        DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
                        // 4. 由根私钥生成 第一个HD 钱包
                        DeterministicHierarchy dh = new DeterministicHierarchy(rootPrivateKey);
                        // 5. 定义父路径 H则是加强 imtoken中的eth钱包进过测试发现使用的是此方式生成 bip44
                        List<ChildNumber> parentPath = HDUtils.parsePath("M/44H/60H/0H/0");
                        // 6. 由父路径,派生出第一个子私钥 "new ChildNumber(0)" 表示第一个 （m/44'/60'/0'/0/0）
                        DeterministicKey child = dh.deriveChild(parentPath, true, true, new ChildNumber(0));
                        byte[] privateKeyByte = child.getPrivKeyBytes();
                        //7.通过私钥生成公私钥对
                        ECKeyPair keyPair = ECKeyPair.create(privateKeyByte);

                        return Credentials.create(keyPair);
                    } else {
                        return WalletUtils.loadCredentials(signerPassword, file);


                    }
                }).map(credentials -> {
                    com.wallet.crypto.utcapp.entity.Address toAddrObj = null;
                    //如果目的地址为空，则为创建新合约
                    if (toAddress == null || toAddress.length() == 0) {
                        //toAddrObj=new Address();
                        return signTransaction2(signer, signerPassword, gasPrice, gasLimit, nonce, data, chainId);
                    } else {
                        toAddrObj = new com.wallet.crypto.utcapp.entity.Address(toAddress);
                    }
                    BigInteger nonceBI = BigInteger.valueOf(nonce);
                    RawTransaction rawTransaction;

                    if (data != null && data.length > 0) {
                        rawTransaction = RawTransaction.createTransaction(nonceBI, gasPrice, gasLimit, toAddress, amount, Numeric.toHexString(data));
                    } else {
                        rawTransaction = RawTransaction.createEtherTransaction(nonceBI, gasPrice, gasLimit, toAddress, amount);
                    }

                    // Credentials credentials = WalletUtils.loadCredentials(signerPassword, findAccountFile(signer.address));
                    byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                    return signedMessage;
                }).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<byte[]> signEthTransaction(HomeToken homeToken, RealmAccount realmAccount, long nonce, byte[] data, long chainId) {
        return Single.just(realmAccount)
                .map(realmAccount1 -> {
                    BigInteger key = new BigInteger(realmAccount1.getPrivate_key(), PRIVATE_KEY_RADIX);
                    ECKeyPair keypair = ECKeyPair.create(key);
                    return Credentials.create(keypair);

                }).map(credentials -> {
                    com.wallet.crypto.utcapp.entity.Address toAddrObj = null;
                    //如果目的地址为空，则为创建新合约

                    toAddrObj = new com.wallet.crypto.utcapp.entity.Address(homeToken.to_address);

                    BigInteger nonceBI = BigInteger.valueOf(nonce);
                    RawTransaction rawTransaction;

                    if (data != null && data.length > 0) {
                        rawTransaction = RawTransaction.createTransaction(nonceBI, new BigDecimal(homeToken.gas_price).toBigInteger(), new BigDecimal(homeToken.gas_limit).toBigInteger(), homeToken.contract_address, new BigDecimal(homeToken.pay_amount).toBigInteger(), Numeric.toHexString(data));
                    } else {
                        rawTransaction = RawTransaction.createEtherTransaction(nonceBI, new BigDecimal(homeToken.gas_price).toBigInteger(), new BigDecimal(homeToken.gas_limit).toBigInteger(), homeToken.to_address, new BigDecimal(homeToken.pay_amount).toBigInteger());
                    }
                    // Credentials credentials = WalletUtils.loadCredentials(signerPassword, findAccountFile(signer.address));
                    byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                    return signedMessage;
                }).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<byte[]> signContract(Wallet signer, String signerPassword, BigInteger amount, BigInteger gasPrice, BigInteger gasLimit, long nonce, byte[] data, long chainId) {
        return Single.fromCallable(() -> {
            return signTransaction2(signer, signerPassword, gasPrice, gasLimit, nonce, data, chainId);
        }).subscribeOn(Schedulers.io());
    }

    private byte[] signTransaction2(Wallet signer, String signerPassword, BigInteger gasPrice, BigInteger gasLimit, long nonce, byte[] data, long chainId) throws Exception {
        //   Credentials credentials = WalletUtils.loadCredentials(signerPassword, findAccountFile(signer.address));
        return this.getStoreFile(signer.address)
                .map(file -> {
                    Object mimic = new ObjectMapper().readValue(file, Object.class);
                    if (mimic instanceof String && !TextUtils.isEmpty((String) mimic) && ((String) mimic).split(" ").length == 12) {
                        byte[] seed = MnemonicUtils.generateSeed((String) mimic, null);
                        DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
                        // 4. 由根私钥生成 第一个HD 钱包
                        DeterministicHierarchy dh = new DeterministicHierarchy(rootPrivateKey);
                        // 5. 定义父路径 H则是加强 imtoken中的eth钱包进过测试发现使用的是此方式生成 bip44
                        List<ChildNumber> parentPath = HDUtils.parsePath("M/44H/60H/0H/0");
                        // 6. 由父路径,派生出第一个子私钥 "new ChildNumber(0)" 表示第一个 （m/44'/60'/0'/0/0）
                        DeterministicKey child = dh.deriveChild(parentPath, true, true, new ChildNumber(0));
                        byte[] privateKeyByte = child.getPrivKeyBytes();
                        //7.通过私钥生成公私钥对
                        ECKeyPair keyPair = ECKeyPair.create(privateKeyByte);
                        return Credentials.create(keyPair);
                    } else {
                        return WalletUtils.loadCredentials(signerPassword, file);
                    }
                }).map(credentials -> {
                    BigInteger nonceBI = BigInteger.valueOf(nonce);
                    RawTransaction rawTransaction = RawTransaction.createContractTransaction(nonceBI, gasPrice, gasLimit, BigInteger.ZERO, Numeric.toHexString(data));
                    byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                    return signedMessage;
                }).subscribeOn(Schedulers.io()).blockingGet();
    }

    @Override
    public Single<Boolean> isMnemonic(String address) {
        //  return keyStore.hasAddress(new Address(address));
        return this.getStoreFile(address)
                .map(file -> {
                    Object mimic = new ObjectMapper().readValue(file, Object.class);
                    if (mimic instanceof String && !TextUtils.isEmpty((String) mimic) && ((String) mimic).split(" ").length == 12) {

                        return true;
                    } else {
                        return false;
                    }
                }).subscribeOn(Schedulers.io());
    }

    @Override
    public boolean hasAccount(String private_key) {
        //  return keyStore.hasAddress(new Address(address));
        RealmWalletSource realmWalletSource = new RealmWalletSource();
        RealmWallet wallet = realmWalletSource.getWallet(private_key);
        if (wallet != null && !TextUtils.isEmpty(wallet.getKey()) && wallet.getKey().equals(private_key)) {
            return true;
        }
        return false;
    }

    @Override
    public Single<Wallet[]> fetchAccounts() {
      /*  return Single.fromCallable(() -> {
            Accounts accounts = keyStore.getAccounts();
            int len = (int) accounts.size();
            Wallet[] result = new Wallet[len];

            for (int i = 0; i < len; i++) {
                org.ethereum.geth.Account gethAccount = accounts.get(i);
                result[i] = new Wallet(gethAccount.getAddress().getHex().toLowerCase());
            }
            return result;
        })
                .subscribeOn(Schedulers.io());*/
        return Single.fromCallable(() -> {
            File[] listFiles;
            if ((listFiles = this.storeDir.listFiles()) == null) {
                listFiles = new File[0];
            }
            int len = listFiles.length;
            Wallet[] result = new Wallet[len];

            for (int i = 0; i < len; i++) {
                //  Log.d("获取钱包0", listFiles[i].getName()+ "");
                String[] arrstring = listFiles[i].getName().split("--");
                String[] arrstringWallet = arrstring[arrstring.length - 1].split("\\.");
                String address = "";
                // Log.d("获取钱包1", arrstringWallet.length + "");
                if (arrstringWallet.length > 0) {
                    //  Log.d("获取钱包2", arrstringWallet[0] + "");
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("0x");
                    stringBuilder.append(arrstringWallet[0]);
                    address = stringBuilder.toString();
                } else {
                    //  Log.d("获取钱包3", arrstring[arrstring.length - 1] + "");
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("0x");
                    stringBuilder.append(arrstring[arrstring.length - 1]);
                    address = stringBuilder.toString();
                }
                if (com.wallet.crypto.utcapp.entity.Address.isAddress(address)) {
                    result[i] = new Wallet(address.toLowerCase());
                }
            }
            return result;
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Wallet> findWallet(String address, String password) {
        return getStoreFile(address).map(new Function<File, Wallet>() {
            @Override
            public Wallet apply(File file) throws Exception {
                Wallet wallet = new Wallet("");
                String mic = new ObjectMapper().readValue(file, String.class);
                byte[] seed = MnemonicUtils.generateSeed(mic, null);

                DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);

                // 4. 由根私钥生成 第一个HD 钱包
                DeterministicHierarchy dh = new DeterministicHierarchy(rootPrivateKey);
                // 5. 定义父路径 H则是加强 imtoken中的eth钱包进过测试发现使用的是此方式生成 bip44
                List<ChildNumber> parentPath = HDUtils.parsePath("M/44H/60H/0H/0");
                // 6. 由父路径,派生出第一个子私钥 "new ChildNumber(0)" 表示第一个 （m/44'/60'/0'/0/0）
                DeterministicKey child = dh.deriveChild(parentPath, true, true, new ChildNumber(0));
                byte[] privateKeyByte = child.getPrivKeyBytes();
                //7.通过私钥生成公私钥对
                ECKeyPair keyPair = ECKeyPair.create(privateKeyByte);
                //  Log.i("TAG", "generateBip44Wallet: 钥匙对  私钥 = " + Numeric.toHexStringNoPrefix(keyPair.getPrivateKey()));
                // Log.i("TAG", "generateBip44Wallet: 钥匙对  公钥 = " + Numeric.toHexStringNoPrefix(keyPair.getPublicKey()));
                //8.通过密码和钥匙对生成WalletFile也就是keystore的bean类
                WalletFile walletFile = org.web3j.crypto.Wallet.createLight(password, keyPair);

                if (Numeric.containsHexPrefix(walletFile.getAddress())) {
                    wallet.address = walletFile.getAddress();
                } else {
                    wallet.address = Numeric.prependHexPrefix(walletFile.getAddress());
                }
                wallet.mnemonic = mic;
                return wallet;
            }
        });
    }

/*    private org.ethereum.geth.Account findAccount(String address) throws ServiceException {
        Accounts accounts = keyStore.getAccounts();
        int len = (int) accounts.size();
        for (int i = 0; i < len; i++) {
            try {
                android.util.Log.d("ACCOUNT_FIND", "Address: " + accounts.get(i).getAddress().getHex());
                if (accounts.get(i).getAddress().getHex().equalsIgnoreCase(address)) {
                    return accounts.get(i);
                }
            } catch (Exception ex) {
                *//* Quietly: interest only result, maybe next is ok. *//*
            }
        }
        throw new ServiceException("Wallet with address: " + address + " not found");
    }*/

    private File findAccountFile(String address) {
        File[] arrfile = storeDir.listFiles();
        String object = Numeric.cleanHexPrefix(address);
        for (File file : arrfile) {
            if (!file.getName().contains(object)) continue;
            return file;
        }
        return null;

    }

    public Single<File> getStoreFile(final String address) {
        //wyb TODO 基本代码确定 需要验证
        //  return Single.fromCallable((Callable<? extends File>)new KeyStoreDataSource$$Lambda$19(this, address));
        return Single.fromCallable(() -> {
            File[] arrfile = storeDir.listFiles();
            String object = Numeric.cleanHexPrefix(address);
            for (File file : arrfile) {
                if (!file.getName().contains(object)) continue;
                return file;
            }
            return null;
        });
    }

    @Override
    public Single<byte[]> signMessage(Wallet wallet, byte[] array, String s) {
        return Single.just(s)
                .map(file -> {
                    BigInteger key = new BigInteger(s, PRIVATE_KEY_RADIX);
                    ECKeyPair keypair = ECKeyPair.create(key);

                    return Sign.signMessage(array, keypair, false);

                })
                .map(signatureData -> {
                    byte[] arrby = new byte[65];
                    System.arraycopy(signatureData.getR(), 0, arrby, 0, 32);
                    System.arraycopy(signatureData.getS(), 0, arrby, 32, 32);
                    arrby[64] = (byte) (signatureData.getV() - 27);
                    return arrby;
                });
    }
}
