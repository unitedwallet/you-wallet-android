package com.wallet.crypto.utcapp.repository;

import com.wallet.crypto.utcapp.entity.NetworkInfo;
import com.wallet.crypto.utcapp.entity.Transaction;
import com.wallet.crypto.utcapp.entity.Wallet;

import io.reactivex.Single;

public interface TransactionLocalSource {
	Single<Transaction[]> fetchTransaction(NetworkInfo networkInfo, Wallet wallet);

	void putTransactions(NetworkInfo networkInfo,Wallet wallet, Transaction[] transactions);
	 void deleteTransactionbs(NetworkInfo networkInfo, Wallet wallet);
	void clear();
}
