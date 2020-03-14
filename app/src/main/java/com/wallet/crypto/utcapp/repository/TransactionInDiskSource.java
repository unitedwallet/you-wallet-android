package com.wallet.crypto.utcapp.repository;

import com.wallet.crypto.utcapp.entity.NetworkInfo;
import com.wallet.crypto.utcapp.entity.Transaction;
import com.wallet.crypto.utcapp.entity.Wallet;

import io.reactivex.Single;

public class TransactionInDiskSource implements TransactionLocalSource {
	@Override
	public Single<Transaction[]> fetchTransaction(NetworkInfo networkInfo, Wallet wallet) {
		return null;
	}

	@Override
	public void putTransactions(NetworkInfo networkInfo,Wallet wallet, Transaction[] transactions) {

	}

	@Override
	public void deleteTransactionbs(NetworkInfo networkInfo, Wallet wallet) {

	}

	@Override
    public void clear() {

    }
}
