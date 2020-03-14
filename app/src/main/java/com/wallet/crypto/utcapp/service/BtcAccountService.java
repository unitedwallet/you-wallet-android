package com.wallet.crypto.utcapp.service;

import android.util.Log;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.wallet.crypto.utcapp.entity.GoodAddress;

import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.util.BitcoinAddressCreator;
import com.wallet.crypto.utcapp.util.StringUtils;
import com.wallet.crypto.utcapp.widget.MnemonicUtils;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.HDUtils;
import org.bitcoinj.params.MainNetParams;

import org.bitcoinj.wallet.WalletFiles;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletFile;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;


import static com.wallet.crypto.utcapp.ui.findkey.FindPrivateActivity.cancelFind;
import static com.wallet.crypto.utcapp.ui.wallet.CreateGoodAddressActivity.isCancel;
import static org.web3j.crypto.Wallet.create;
import static org.web3j.utils.Numeric.cleanHexPrefix;

public class BtcAccountService implements BtcAccountServiceType {
    private String btc_path = "M/44H/0H/0H/0";
    private static final int P = 1;

    @Override
    public Single<Wallet> createBtcAccount(String mnemonic, int index) {
        Wallet wallet = new Wallet("");
        return Single.fromCallable(() -> {
            wallet.mnemonic = mnemonic;
            //  Log.d("2出错", wallet.mnemonic);
            Log.d("到哪了","4");
            byte[] seed = MnemonicUtils.generateSeed(mnemonic, null);
            // 3.生成 私钥
            DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
            // 4. 由根私钥生成 第一个HD 钱包
            DeterministicHierarchy dh = new DeterministicHierarchy(rootPrivateKey);
            // 5. 定义父路径 H则是加强 imtoken中的eth钱包进过测试发现使用的是此方式生成 bip44
            List<ChildNumber> parentPath = HDUtils.parsePath(btc_path);
            // 6. 由父路径,派生出第一个子私钥 "new ChildNumber(0)" 表示第一个 （m/44'/60'/0'/0/0）
            DeterministicKey child = dh.deriveChild(parentPath, true, true, new ChildNumber(index));

            byte[] privateKeyByte = child.getPrivKeyBytes();
            ECKey key = ECKey.fromPrivate(privateKeyByte);
            Log.d("比特币钱包地址", fromPrivateKey(privateKeyByte));
            Log.d("比特币钱包私钥", key.getPrivateKeyAsWiF(MainNetParams.get()));
            wallet.address = key.toAddress(MainNetParams.get()).toBase58();
            wallet.private_key = key.getPrivateKeyAsWiF(MainNetParams.get());
            return wallet;
        });
    }

    @Override
    public Observable<GoodAddress> createBtcGoodAddress(String mnemonic) {
        return Observable.create(e -> {
            byte[] seed = MnemonicUtils.generateSeed(mnemonic, null);
            // 3.生成 私钥
            DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
            // 4. 由根私钥生成 第一个HD 钱包
            DeterministicHierarchy dh = new DeterministicHierarchy(rootPrivateKey);
            // 5. 定义父路径 H则是加强
            List<ChildNumber> parentPath = HDUtils.parsePath(btc_path);
            int index = 0;
            int count = 0;
            long start = System.currentTimeMillis();

            while (!isCancel && count < 6) {
                //大于60s停止生成地址
                if (System.currentTimeMillis() - start >= 60 * 1000) break;
                DeterministicKey child = dh.deriveChild(parentPath, true, true, new ChildNumber(index));
                byte[] privateKeyByte = child.getPrivKeyBytes();
                //8.通过密码和钥匙对生成WalletFile也就是keystore的bean类
                GoodAddress goodAddress = new GoodAddress();
                goodAddress.address = fromPrivateKey(privateKeyByte);

                goodAddress.index = index;
                goodAddress.is_good = StringUtils.isMatchGoodAddress(cleanHexPrefix(goodAddress.address));
                if (goodAddress.is_good) {
                    count++;
                }
                Log.d("找到几个了", count + "    " + index);
                e.onNext(goodAddress);
                index++;


            }

//            Wallet wallet = new Wallet(Constants.NETWORK_PARAMETERS);
////创建WalletFiles，设置自动保存Wallet
//            WalletFiles walletFiles = wallet.autosaveToFile(walletFile, 3 * 1000, TimeUnit.MILLISECONDS, null);
            e.onComplete();
        });
    }

    @Override
    public Observable<GoodAddress> findBtcPrivate(String address) {
        return Observable.create(e -> {

            boolean isFind = false;
            while (!cancelFind && !isFind) {
                //大于60s停止生成地址
                // FileUtil. systemLog("开始1"+"\n");
                ECKey key = new ECKey();
                GoodAddress goodAddress = new GoodAddress();
                goodAddress.address = key.toAddress(MainNetParams.get()).toBase58();
                goodAddress.privateKey = key.getPrivateKeyAsWiF(MainNetParams.get());
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

    public static BigInteger hexToBigInteger(String hexValue) {
        String cleanValue = cleanHexPrefix(hexValue);
        return new BigInteger(cleanValue, 16);
    }

    @Override
    public Single<Wallet> importBtcPrivateKey(String privateKey) {
        Wallet wallet = new Wallet("");
        return Single.fromCallable(() -> {

            ECKey key = BitcoinAddressCreator.fromPrivateKey(privateKey,MainNetParams.get());
            wallet.private_key = key.getPrivateKeyAsWiF(MainNetParams.get());
            wallet.address = key.toAddress(MainNetParams.get()).toBase58();
            return wallet;
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
            List<ChildNumber> parentPath = HDUtils.parsePath(btc_path);
            // 6. 由父路径,派生出第一个子私钥 "new ChildNumber(0)" 表示第一个 （m/44'/60'/0'/0/0）
            String[] addresss = new String[wallet.son];
            for (int i = 0; i < wallet.son; i++) {
                DeterministicKey child = dh.deriveChild(parentPath, true, true, new ChildNumber(i));
                byte[] privateKeyByte = child.getPrivKeyBytes();
                addresss[i] = fromPrivateKey(privateKeyByte);
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
            List<ChildNumber> parentPath = HDUtils.parsePath(btc_path);
            // 6. 由父路径,派生出第一个子私钥 "new ChildNumber(0)" 表示第一个 （m/44'/60'/0'/0/0）
            String[][] addresss = new String[wallet.son][2];
            for (int i = 0; i < wallet.son; i++) {
                DeterministicKey child = dh.deriveChild(parentPath, true, true, new ChildNumber(i));
                byte[] privateKeyByte = child.getPrivKeyBytes();
                //7.通过私钥生成公私钥对
                ECKey key = ECKey.fromPrivate(privateKeyByte);
                addresss[i][0] = key.getPrivateKeyAsWiF(MainNetParams.get());
                addresss[i][1] = key.toAddress(MainNetParams.get()).toBase58();

            }

            return addresss;
        });
    }


    //私钥转换地址
    public String fromPrivateKey(byte[] prvKeyBytes) {
        ECKey key = ECKey.fromPrivate(prvKeyBytes);
        return key.toAddress(MainNetParams.get()).toBase58();
    }
    private void send(){

    }
}
