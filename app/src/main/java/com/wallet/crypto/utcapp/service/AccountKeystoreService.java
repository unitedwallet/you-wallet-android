package com.wallet.crypto.utcapp.service;

import com.wallet.crypto.utcapp.entity.GoodAddress;
import com.wallet.crypto.utcapp.entity.HomeToken;
import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.repository.realmentity.RealmAccount;
import com.wallet.crypto.utcapp.repository.realmentity.RealmChain;


import java.io.File;
import java.math.BigInteger;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface AccountKeystoreService {

	/**
	 * Create account in mnemonic
	 * @return new {@link Wallet}
	 */
	Single<Wallet> createMnemonicAccount();
	public Single<Wallet> createEthAccount(String mnemonic,int index);
	Observable<GoodAddress> createEthGoodAddress(String mnemonic);
	Observable<GoodAddress> findEthPrivate(String address);
	/**
	 * Include new existing keystore
	 * @param store store to include
	 * @param password store password
	 * @return included {@link Wallet} if success
	 */
	Single<Wallet> importKeystore(String store, String password );
	Single<Wallet> importPrivateKey(String privateKey);
	public Single<Wallet> importPhrase(String phrasde, String newPassword);
	Single<Boolean> isMnemonic(String address);
	/**
	 * Export wallet to keystore
	 * @param wallet wallet to export
	 * @param password password from wallet
	 * @param newPassword new password to store
	 * @return store data
	 */
	Single<String> exportAccount(Wallet wallet,  String newPassword, RealmChain realmChain);
	Single<String> exportPrivateKey(Wallet wallet, String password);


	/**
	 * Export wallet to keystore
	 * @param wallet wallet to export
	 * @param password password from wallet
	 * @return store data
	 */
	Single<String> exportPhrase(Wallet wallet, String password );
	Single<String> exportHomePhrase(Wallet wallet );

	/**
	 * Delete account from keystore
	 * @param address account address
	 * @param password account password
	 */
	Completable deleteAccount(String address, String password);
	 Single<String[]> exportSonAddress(Wallet wallet);
	Single<String[][]> exportSonPrivateAddress(Wallet wallet);
	/**
	 * Sign transaction
	 * @param signer {@link Wallet}
	 * @param signerPassword password from {@link Wallet}
	 * @param toAddress transaction destination address
	 * @param
	 * @param nonce
	 * @return sign data
	 */
	Single<byte[]> signTransaction(
			Wallet signer,
			String signerPassword,
			String toAddress,
			BigInteger amount,
			BigInteger gasPrice,
			BigInteger gasLimit,
			long nonce,
			byte[] data,
			long chainId);
	Single<byte[]> signEthTransaction(
			HomeToken homeToken,
			RealmAccount realmAccount,
			long nonce,
			byte[] data,
			long chainId);
	Single<byte[]> signContract(
			Wallet signer,
			String signerPassword,
			BigInteger amount,
			BigInteger gasPrice,
			BigInteger gasLimit,
			long nonce,
			byte[] data,
			long chainId);

	/**
	 * Check if there is an address in the keystore
	 * @param address {@link Wallet} address
	 */
	boolean hasAccount(String address);

	/**
	 * Return all {@link Wallet} from keystore
	 * @return wallets
	 */
	Single<Wallet[]> fetchAccounts();

	Single<Wallet> findWallet(String address,String password);
	Single<File> getStoreFile( String address);
	Single<byte[]> signMessage(Wallet wallet, byte[] array, String s);
}
