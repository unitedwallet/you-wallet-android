package com.wallet.crypto.utcapp.repository;

import android.text.format.DateUtils;

import com.wallet.crypto.utcapp.entity.NetworkInfo;
import com.wallet.crypto.utcapp.entity.Transaction;
import com.wallet.crypto.utcapp.entity.Wallet;

import java.util.Map;

import io.reactivex.Single;

// TODO: Add pagination.
public class TransactionInMemorySource implements TransactionLocalSource {

	private static final long MAX_TIME_OUT = DateUtils.MINUTE_IN_MILLIS;
	private final Map<String, CacheUnit> cache = new java.util.concurrent.ConcurrentHashMap<>();

	@Override
	public Single<Transaction[]> fetchTransaction(NetworkInfo networkInfo, Wallet wallet) {
		return Single.fromCallable(() -> {
			CacheUnit unit = cache.get(wallet.address);
			Transaction[] transactions = new Transaction[0];
			if (unit != null) {
				if (System.currentTimeMillis() - unit.create > MAX_TIME_OUT) {
					cache.remove(wallet.address);
				} else {
					transactions = unit.transactions;
				}

			}
			return transactions;
		});
	}

	@Override
	public void putTransactions(NetworkInfo networkInfo,Wallet wallet, Transaction[] transactions) {
		cache.put(wallet.address, new CacheUnit(wallet.address, System.currentTimeMillis(), transactions));
	}

	@Override
	public void deleteTransactionbs(NetworkInfo networkInfo, Wallet wallet) {

	}

	@Override
	public void clear() {
		cache.clear();
	}

	private static class CacheUnit {
		final String accountAddress;
		final long create;
		final Transaction[] transactions;

		private CacheUnit(String accountAddress, long create, Transaction[] transactions) {
			this.accountAddress = accountAddress;
			this.create = create;
			this.transactions = transactions;
		}
	}
}
