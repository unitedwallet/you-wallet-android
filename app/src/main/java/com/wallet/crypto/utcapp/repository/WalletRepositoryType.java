package com.wallet.crypto.utcapp.repository;

import android.content.Context;

import com.wallet.crypto.utcapp.entity.GoodAddress;
import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.repository.realmentity.RealmChain;

import java.math.BigInteger;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface WalletRepositoryType {
	Single<Wallet[]> fetchWallets();




	Single<Wallet> createMnemonicWallet(String password);
	Single<Wallet> createMnemonicWalletByType(Context context, String type,int son,String name);
	Single<Wallet> createMnemonicWalletByType(Context context, String phrase,String type,int son,String name, String select_chain_id, int select_good_address_index);
	Observable<GoodAddress> createEthGoodAddress(String mnemonic);
	Observable<GoodAddress> createBtcGoodAddress(String mnemonic);
	Observable<GoodAddress> findEthPrivateKey(String address);
	Observable<GoodAddress> findBtcPrivateKey(String address);
	Single<Wallet> importPhraseByType(Context context, String phrase,String type,int son,String name);
	Single<Wallet> importKeystoreToWallet(Context context, String keystore,String password,String type,int son,String name, RealmChain realmChain);

	Single<Wallet> importPrivateKeyToWallet(Context context,String privateKey,String type, int son, String name,RealmChain realmChain);
	Single<Wallet> importWatch(Context context,String address,String type, int son, String name,RealmChain realmChain);
	Single<String> exportWallet(Wallet wallet, String newPassword, RealmChain realmChain);
	Single<String> exportPhrase(Wallet var1, String var2);
	Single<String> exportPrivateKey(Wallet var1, RealmChain realmChain);
	Completable deleteWallet(String address, String password);
	 Single<String[]> exportSonAddress(Wallet wallet,RealmChain realmChain);
	 Single<String[][]> exportSonPrivateAddress(Wallet wallet,RealmChain realmChain);
	Completable setDefaultWallet(Wallet wallet);
	Completable setIsSkipBackup(Wallet wallet, boolean isSkip);
	Single<Boolean> getIsSkipBackup(Wallet wallet);
	Single<Wallet> getDefaultWallet();

	Single<byte[]> signMessage(final Wallet wallet, final byte[] array, final String privatekey);
}
