package com.wallet.crypto.utcapp.repository;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.wallet.crypto.utcapp.C;
import com.wallet.crypto.utcapp.entity.HomeToken;
import com.wallet.crypto.utcapp.entity.NetworkInfo;
import com.wallet.crypto.utcapp.entity.ServiceException;
import com.wallet.crypto.utcapp.entity.Token;
import com.wallet.crypto.utcapp.entity.TokenInfo;
import com.wallet.crypto.utcapp.entity.Transaction;
import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.entity.httpentity.FundTransactionRequest;
import com.wallet.crypto.utcapp.entity.httpentity.FundTransactionResponse;
import com.wallet.crypto.utcapp.repository.entity.Contract;
import com.wallet.crypto.utcapp.repository.realmentity.RealmAccount;
import com.wallet.crypto.utcapp.repository.realmentity.RealmChain;
import com.wallet.crypto.utcapp.repository.realmentity.RealmTransactionNew;
import com.wallet.crypto.utcapp.service.AccountKeystoreService;
import com.wallet.crypto.utcapp.service.BlockExplorerClientType;
import com.wallet.crypto.utcapp.ui.NetWorkActivity;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TransactionRepository implements TransactionRepositoryType {

    private final EthereumNetworkRepositoryType networkRepository;
    private final AccountKeystoreService accountKeystoreService;
    private final TransactionLocalSource transactionLocalSource;
    private final BlockExplorerClientType blockExplorerClient;
    private final TokenLocalSource tokenLocalSource;
    private final RealmWalletSource realmWalletSource;

    public TransactionRepository(
            EthereumNetworkRepositoryType networkRepository,
            AccountKeystoreService accountKeystoreService,
            TransactionLocalSource inMemoryCache,
            TransactionLocalSource inDiskCache,
            BlockExplorerClientType blockExplorerClient, TokenLocalSource tokenLocalSource, RealmWalletSource realmWalletSource) {
        this.networkRepository = networkRepository;
        this.accountKeystoreService = accountKeystoreService;
        this.blockExplorerClient = blockExplorerClient;
        this.transactionLocalSource = new RealmTransactionSource();
        this.tokenLocalSource = tokenLocalSource;
        this.realmWalletSource = realmWalletSource;
        this.networkRepository.addOnChangeDefaultNetwork(this::onNetworkChanged);
    }

    @Override
    public Observable<Transaction[]> fetchTransaction(Wallet wallet) {
        return Observable.create(e -> {
//            Transaction[] transactions = transactionLocalSource.fetchTransaction(networkRepository.getDefaultNetwork(), wallet).blockingGet();
//            if (transactions != null && transactions.length > 0) {
//                e.onNext(transactions);
//            }
//            transactions = blockExplorerClient.fetchTransactions(wallet.address, 1).blockingFirst();
//            transactionLocalSource.clear();
//            transactionLocalSource.putTransactions(networkRepository.getDefaultNetwork(), wallet, transactions);
            //  e.onNext(transactions);
            e.onComplete();
        });
    }

    @Override
    public Observable<Transaction[]> fetchTransactionFromRealm(Wallet wallet, int pagesize) {


        return Observable.create(e -> {
            //try {
//
//            NetworkInfo networkInfo = networkRepository.getDefaultNetwork();
//           // Transaction[] transactions = blockExplorerClient.fetchTransactions(wallet.address, pagesize).blockingFirst();
//            //transactionLocalSource.clear();
//            /*Contract[] contract2Add = ContractRepository.getContractsFromTransaction(transactions);
//            tokenLocalSource.putInNeedOrUpdate(networkInfo, wallet, contract2Add);
//*/
//            if (transactions != null && transactions.length > 0 && pagesize == 1) {
//                transactionLocalSource.deleteTransactionbs(networkRepository.getDefaultNetwork(), wallet);
//            }
//            transactionLocalSource.putTransactions(networkInfo, wallet, transactions);
//            transactions = transactionLocalSource.fetchTransaction(networkRepository.getDefaultNetwork(), wallet).blockingGet();
//            e.onNext(transactions);
            e.onComplete();
            //	throw new Exception("你好");
//			}
//			catch (Exception e1){
//				//e.onError(e1);
//			}
        });
    }

    @Override
    public Observable<List<RealmTransactionNew>> fetchTransactionFromRealmNew(HomeToken homeToken, int pageSize, int type) {
        return Observable.create((ObservableOnSubscribe<List<RealmTransactionNew>>) e -> {
            //先获取服务器的数据
            RealmChain realmChain = realmWalletSource.getChain(homeToken.chain_id).blockingGet();
            FundTransactionRequest fundTransactionRequest = new FundTransactionRequest();
            FundTransactionRequest.DataFundTransaction dataFundTransaction = new FundTransactionRequest.DataFundTransaction();
            dataFundTransaction.address = homeToken.address;
            dataFundTransaction.chain = realmChain.getName();
            dataFundTransaction.contract = homeToken.contract_address;
            dataFundTransaction.fund = homeToken.symbol;
            dataFundTransaction.limit = C.TRANSACTIONS_ONE_PAGE_SIZE;
            dataFundTransaction.page = pageSize;
            dataFundTransaction.type = type;
            fundTransactionRequest.param = new FundTransactionRequest.DataFundTransaction[1];
            fundTransactionRequest.param[0] = dataFundTransaction;

            FundTransactionResponse fundTransactionResponse = blockExplorerClient.fetchTransactions(realmChain.getBackend(), fundTransactionRequest).blockingFirst();
//            if (fundTransactionResponse != null && fundTransactionResponse.code == 0 && fundTransactionResponse.data != null && fundTransactionResponse.data.length > 0 && pageSize == 1) {
//                new RealmTransactionNewSource().deleteTransaction();
//            }
            if (fundTransactionResponse != null && fundTransactionResponse.code == 0 && fundTransactionResponse.data != null) {
                for (int i = 0; i < fundTransactionResponse.data.length; i++) {
                 FundTransactionResponse.DataFundTransaction dataFundTransaction1=fundTransactionResponse.data[i];
                 RealmTransactionNew realmTransactionNew=new RealmTransactionNew();
                    realmTransactionNew.setGasUsed(dataFundTransaction1.gas);
                    realmTransactionNew.setGasPrice(dataFundTransaction1.gasPrice);
                    realmTransactionNew.setChain_id(homeToken.chain_id);
                    realmTransactionNew.setContract_address(homeToken.contract_address);
                    realmTransactionNew.setCreate_time(System.currentTimeMillis());
                    realmTransactionNew.setFee(dataFundTransaction1.gas);
                    realmTransactionNew.setFrom(dataFundTransaction1.from);
                    realmTransactionNew.setFund_id(homeToken.fund_id);
                    realmTransactionNew.setStatus(TextUtils.isEmpty(dataFundTransaction1.error)?2:1);
                    realmTransactionNew.setTo(dataFundTransaction1.to);
                    realmTransactionNew.setType(homeToken.address.equals(dataFundTransaction1.from)?2:1);
                    realmTransactionNew.setUpdate_time(Long.valueOf(dataFundTransaction1.timeStamp)*1000);
                    realmTransactionNew.setValue(dataFundTransaction1.value);
                    realmTransactionNew.setWallet_id(homeToken.wallet_id);
                    realmTransactionNew.setNonce(Integer.valueOf(dataFundTransaction1.nonce));
                    realmTransactionNew.setHash(dataFundTransaction1.hash);
                    new RealmTransactionNewSource().putTransaction(realmTransactionNew);
                }
            }
            List<RealmTransactionNew> list = realmWalletSource
                    .fetchFundTransaction(homeToken.wallet_id, homeToken.fund_id, type).blockingFirst();
            e.onNext(list);
            e.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Maybe<Transaction> findTransaction(Wallet wallet, String transactionHash) {
        return fetchTransaction(wallet)
                .firstElement()
                .flatMap(transactions -> {
                    for (Transaction transaction : transactions) {
                        if (transaction.hash.equals(transactionHash)) {
                            return Maybe.just(transaction);
                        }
                    }
                    return null;
                });
    }

    int nonce;

    @Override
    public Single<Transaction> createTransaction(Wallet from, String toAddress, BigInteger subunitAmount, BigInteger gasPrice, BigInteger gasLimit, byte[] data, String password) {
        final Web3j web3j = Web3jFactory.build(new HttpService(networkRepository.getDefaultNetwork().rpcServerUrl));

        //TODO: 此处关系到为交易签名，然后发送给web3j服务器方面
        return Single.fromCallable(() -> {
            EthGetTransactionCount ethGetTransactionCount = web3j
                    .ethGetTransactionCount(from.address, DefaultBlockParameterName.LATEST)
                    .send();
            nonce = ethGetTransactionCount.getTransactionCount().intValue();
            return ethGetTransactionCount.getTransactionCount();
            //return new BigInteger("1");
        })
                .flatMap(nonce -> accountKeystoreService.signTransaction(from, password, toAddress, subunitAmount, gasPrice, gasLimit, nonce.longValue(), data, networkRepository.getDefaultNetwork().chainId))
                .flatMap(signedMessage -> Single.fromCallable(() -> {
                    EthSendTransaction raw = web3j
                            .ethSendRawTransaction(Numeric.toHexString(signedMessage))
                            .send();
                    if (raw.hasError()) {
                        throw new ServiceException(raw.getError().getMessage());
                    }
                    return new Transaction(raw.getTransactionHash(), "pending", "", System.currentTimeMillis() / 1000, nonce, "", "", "", "", "", "", "", null, "");
                })).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<RealmTransactionNew> createEthTransaction(HomeToken homeToken, byte[] data) {
        final Web3j[] web3j = new Web3j[1];
        final int[] chainId = {-1};
        final int[] nonce = {0};
        return realmWalletSource.getChain(homeToken.chain_id).map(realmChain -> {
            web3j[0] = Web3jFactory.build(new HttpService(realmChain.getRpc()));
            chainId[0] = realmChain.getChainId();
            EthGetTransactionCount ethGetTransactionCount = web3j[0]
                    .ethGetTransactionCount(homeToken.address, DefaultBlockParameterName.LATEST)
                    .send();
            nonce[0] = ethGetTransactionCount.getTransactionCount().intValue();
            return ethGetTransactionCount.getTransactionCount();
        })
                .flatMap(nonceNew -> {
                    RealmAccount realmAccount = realmWalletSource.getAccount(homeToken.chain_id, homeToken.wallet_id);
                    if (TextUtils.isEmpty(realmAccount.getPrivate_key())) {
                        throw new Exception();
                    }
                    //   return accountKeystoreService.signEthTransaction(homeToken, realmAccount, nonceNew.longValue(), data, chainId[0]);

                    return accountKeystoreService.signEthTransaction(homeToken, realmAccount, nonce[0], data, chainId[0]);
                })
                .flatMap(signedMessage -> Single.fromCallable(() -> {

                    EthSendTransaction raw = web3j[0]
                            .ethSendRawTransaction(Numeric.toHexString(signedMessage))
                            .send();
                    if (raw.hasError()) {
                        throw new ServiceException(raw.getError().getMessage());
                    }
                    RealmTransactionNew realmTransactionNew = new RealmTransactionNew();
                    realmTransactionNew.setHash(raw.getTransactionHash());
                    realmTransactionNew.setNonce(nonce[0]);
                    realmTransactionNew.setWallet_id(homeToken.wallet_id);
                    realmTransactionNew.setValue(homeToken.pay_amount.toString());

                    //1转入   2转出
                    realmTransactionNew.setType(C.TURNOUT);
                    realmTransactionNew.setTo(homeToken.to_address);
                    realmTransactionNew.setStatus(0);
                    realmTransactionNew.setFund_id(homeToken.fund_id);
                    realmTransactionNew.setFrom(homeToken.address);
                    realmTransactionNew.setFee(new BigDecimal(homeToken.gas_price).multiply(new BigDecimal(homeToken.gas_limit)).toPlainString());
                    realmTransactionNew.setCreate_time(System.currentTimeMillis());
                    realmTransactionNew.setUpdate_time(System.currentTimeMillis());
                    realmTransactionNew.setContract_address(homeToken.contract_address);
                    realmTransactionNew.setChain_id(homeToken.chain_id);
                    realmTransactionNew.setGasPrice(homeToken.gas_price);
                    realmTransactionNew.setGasUsed(homeToken.gas_limit);
                    return realmTransactionNew;
                })).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<String> createEthTransactionSign(HomeToken homeToken, byte[] data, long nonce) {
        final Web3j[] web3j = new Web3j[1];
        return realmWalletSource.getChain(homeToken.chain_id)
                .map(realmChain -> {
                    RealmAccount realmAccount = realmWalletSource.getAccount(homeToken.chain_id, homeToken.wallet_id);
                    if (TextUtils.isEmpty(realmAccount.getPrivate_key())) {
                        throw new Exception();
                    }
                    return Numeric.toHexString(accountKeystoreService.signEthTransaction(homeToken, realmAccount, nonce, data, realmChain.getChainId()).blockingGet());

                }).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Token> createContract(Wallet from, BigInteger subunitAmount, BigInteger gasPrice, BigInteger gasLimit, byte[] data, String password, Token token) {
        final Web3j web3j = Web3jFactory.build(new HttpService(networkRepository.getDefaultNetwork().rpcServerUrl));
        return Single.fromCallable(() -> {
            EthGetTransactionCount ethGetTransactionCount = web3j
                    .ethGetTransactionCount(from.address, DefaultBlockParameterName.LATEST)
                    .send();
            return ethGetTransactionCount.getTransactionCount();
        })
                .flatMap(nonce -> accountKeystoreService.signContract(from, password, subunitAmount, gasPrice, gasLimit, nonce.longValue(), data, networkRepository.getDefaultNetwork().chainId))
                .flatMap(signedMessage -> Single.fromCallable(() -> {
                    EthSendTransaction raw = web3j
                            .ethSendRawTransaction(Numeric.toHexString(signedMessage))
                            .send();
                    android.util.Log.e("CREATE_CONTRACT", "send raw transaction for contract");
                    if (raw.hasError()) {
                        throw new ServiceException(raw.getError().getMessage());
                    }
                    String transactionHash = raw.getTransactionHash();
                    android.util.Log.e("CREATE_CONTRACT", "got raw transaction hash: " + transactionHash);

                    return transactionHash;
                }))
                .flatMap(transactionHash -> Single.fromCallable(() -> {
//					EthGetTransactionReceipt txReceipt = web3j
//							.ethGetTransactionReceipt(transactionHash)
//							.send();
//                    android.util.Log.e("CREATE_CONTRACT","send request transaction for receipt");
//
//					if (txReceipt.hasError()) {
//						throw new ServiceException(txReceipt.getError().getMessage() + "：Transaction Receipt Error");
//					}
//
//					TransactionReceipt receipt = txReceipt.getTransactionReceipt();
//                    android.util.Log.e("CREATE_CONTRACT","got receipt transaction");
//
//					String contractAddress = receipt.getContractAddress();
//                    android.util.Log.e("CREATE_CONTRACT","got contract address: " + contractAddress);
//
//					TokenInfo newInfo = new TokenInfo(contractAddress,token.tokenInfo.name,token.tokenInfo.symbol,token.tokenInfo.decimals);
//					Token newToken = new Token(newInfo,token.balance);
//
//					return newToken;

                    EthGetTransactionReceipt txReceipt;
                    TransactionReceipt receipt;


                    int maxCount = 60, count = 0;
                    do {
                        android.util.Log.e("CREATE_CONTRACT", "wait 10 sec for receipt request");
                        try {
                            Thread.sleep(15000);
                        } catch (Exception exc) {
                        }

                        txReceipt = web3j
                                .ethGetTransactionReceipt(transactionHash)
                                .send();
                        android.util.Log.e("CREATE_CONTRACT", "send request transaction for receipt");

                        if (txReceipt.hasError()) {
                            throw new ServiceException(txReceipt.getError().getMessage() + "：Transaction Receipt Error");
                        }

                        receipt = txReceipt.getTransactionReceipt();
                        android.util.Log.e("CREATE_CONTRACT_txRecei", new Gson().toJson(txReceipt));
                        if (receipt != null) {
                            android.util.Log.e("CREATE_CONTRACT", "got receipt transaction");
                            android.util.Log.e("CREATE_CONTRACT", new Gson().toJson(receipt));
                            break;
                        } else {
                            android.util.Log.e("CREATE_CONTRACT", "got empty receipt transaction, try again");
                        }

                    } while (++count < maxCount);

                    if (receipt == null) {
                        throw new ServiceException("Transaction Receipt Pending...");
                    }

                    String contractAddress = receipt.getContractAddress();
                    android.util.Log.e("CREATE_CONTRACT", "got contract address: " + contractAddress);

                    TokenInfo newInfo = new TokenInfo(contractAddress, token.tokenInfo.name, token.tokenInfo.symbol, token.tokenInfo.decimals, token.tokenInfo.totalSupply, "", "", "", "", "", "");
                    Token newToken = new Token(newInfo, token.balance);
                    android.util.Log.e("TOKEN_CREATE", "Update Token Name: " + newToken.tokenInfo.name + ", Symbols: " + newToken.tokenInfo.symbol);


                    return newToken;

                }))
                .subscribeOn(Schedulers.io());
    }

    private void onNetworkChanged(NetworkInfo networkInfo) {
        transactionLocalSource.clear();
    }

    public Single<BigInteger> getEthNonce(HomeToken homeToken) {

        return realmWalletSource.getChain(homeToken.chain_id).map(realmChain -> {
            final Web3j web3j = Web3jFactory.build(new HttpService(realmChain.getRpc()));
            EthGetTransactionCount ethGetTransactionCount = web3j
                    .ethGetTransactionCount(homeToken.address, DefaultBlockParameterName.LATEST)
                    .send();
            return ethGetTransactionCount.getTransactionCount();
            //return new BigInteger("1");

        }).subscribeOn(Schedulers.io());
    }
}
