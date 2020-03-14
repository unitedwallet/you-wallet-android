package com.wallet.crypto.utcapp.repository;

import com.wallet.crypto.utcapp.entity.HomeToken;
import com.wallet.crypto.utcapp.entity.ServiceException;
import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.util.LogInterceptor;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

public class OffLineRepository {

    private static final String DATA_PREFIX = "0x70a08231000000000000000000000000";

    public static Single<BigInteger> getEthTokenBalance(String rpc, String address, String contractAddress) {

        return Single.fromCallable(() -> {
            String value = Web3jFactory.build(new HttpService(rpc))
                    .ethCall(Transaction.createEthCallTransaction(address,
                            contractAddress, DATA_PREFIX + Numeric.cleanHexPrefix(address)), DefaultBlockParameterName.PENDING).send().getValue();
            BigInteger s = new BigInteger(Numeric.cleanHexPrefix(value), 16);
            return s;
        })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

    }

    public static Single<BigInteger> getEthNonce(String rpc, String address) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new LogInterceptor(""))
                .build();
        return Single.fromCallable(() -> {
            final Web3j web3j = Web3jFactory.build(new HttpService(rpc, httpClient, false));
            EthGetTransactionCount ethGetTransactionCount = web3j
                    .ethGetTransactionCount(address, DefaultBlockParameterName.LATEST)
                    .send();
            return ethGetTransactionCount.getTransactionCount();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static Single<BigInteger> balanceEthInWei(String rpc, String address) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new LogInterceptor(""))
                .build();
        return Single.fromCallable(() -> Web3jFactory
                .build(new HttpService(rpc, httpClient, false))
                .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                .send()
                .getBalance())
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static Single<String> senSign(String rpc, String sign) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new LogInterceptor(""))
                .build();
        return Single.fromCallable(() -> {
            EthSendTransaction raw = Web3jFactory
                    .build(new HttpService(rpc, httpClient, false))
                    .ethSendRawTransaction(sign)
                    .send();
            if (raw.hasError()) {
                throw new ServiceException(raw.getError().getMessage());
            }
            return raw.getTransactionHash();
        }).subscribeOn(Schedulers.io()).

                observeOn(AndroidSchedulers.mainThread());


    }


}
