package com.wallet.crypto.utcapp.repository;

import com.wallet.crypto.utcapp.entity.HomeToken;
import com.wallet.crypto.utcapp.entity.Token;
import com.wallet.crypto.utcapp.entity.TokenInfo;
import com.wallet.crypto.utcapp.entity.Transaction;
import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.repository.realmentity.RealmTransactionNew;

import java.math.BigInteger;
import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

public interface TransactionRepositoryType {
	Observable<Transaction[]> fetchTransaction(Wallet wallet);
	Observable<Transaction[]> fetchTransactionFromRealm(Wallet wallet,int pagesize);

	Observable<List<RealmTransactionNew>> fetchTransactionFromRealmNew(HomeToken homeToken,int pageSize, int type);

	Maybe<Transaction> findTransaction(Wallet wallet, String transactionHash);
	Single<Transaction> createTransaction(Wallet from, String toAddress, BigInteger subunitAmount, BigInteger gasPrice, BigInteger gasLimit, byte[] data, String password);
	Single<RealmTransactionNew> createEthTransaction(HomeToken homeToken,byte[] data);
	Single<String> createEthTransactionSign(HomeToken homeToken,byte[] data,long nonce);
	Single<Token> createContract(Wallet from, BigInteger subunitAmount, BigInteger gasPrice, BigInteger gasLimit, byte[] data, String password, Token token);
	Single<BigInteger> getEthNonce(HomeToken homeToken);
}
