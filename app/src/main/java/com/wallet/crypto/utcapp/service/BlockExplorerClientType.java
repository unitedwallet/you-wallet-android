package com.wallet.crypto.utcapp.service;

import com.wallet.crypto.utcapp.entity.Transaction;
import com.wallet.crypto.utcapp.entity.httpentity.FundTransactionRequest;
import com.wallet.crypto.utcapp.entity.httpentity.FundTransactionResponse;

import io.reactivex.Observable;

public interface BlockExplorerClientType {
	public Observable<FundTransactionResponse> fetchTransactions(String url, FundTransactionRequest transactionRequest);
}
