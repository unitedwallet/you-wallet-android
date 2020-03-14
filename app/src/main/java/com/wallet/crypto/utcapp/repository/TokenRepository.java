package com.wallet.crypto.utcapp.repository;

import android.text.TextUtils;
import android.util.Log;

import com.wallet.crypto.utcapp.C;
import com.wallet.crypto.utcapp.entity.NetworkInfo;
import com.wallet.crypto.utcapp.entity.Token;
import com.wallet.crypto.utcapp.entity.TokenInfo;
import com.wallet.crypto.utcapp.entity.Wallet;
import com.wallet.crypto.utcapp.repository.entity.Contract;
import com.wallet.crypto.utcapp.service.TokenExplorerClientType;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameterName;

import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import okhttp3.OkHttpClient;

public class TokenRepository implements TokenRepositoryType {

    private final TokenExplorerClientType tokenNetworkService;
    private final TokenLocalSource tokenLocalSource;
    private final OkHttpClient httpClient;
    private final EthereumNetworkRepositoryType ethereumNetworkRepository;
    private Web3j web3j;
    private final TransactionLocalSource transactionLocalSource;

    public TokenRepository(
            OkHttpClient okHttpClient,
            EthereumNetworkRepositoryType ethereumNetworkRepository,
            TokenExplorerClientType tokenNetworkService,
            TokenLocalSource tokenLocalSource) {
        this.httpClient = okHttpClient;
        this.ethereumNetworkRepository = ethereumNetworkRepository;
        this.tokenNetworkService = tokenNetworkService;
        this.tokenLocalSource = tokenLocalSource;
        this.ethereumNetworkRepository.addOnChangeDefaultNetwork(this::buildWeb3jClient);
        this.transactionLocalSource = new RealmTransactionSource();

        buildWeb3jClient(ethereumNetworkRepository.getDefaultNetwork());
    }

    private void buildWeb3jClient(NetworkInfo defaultNetwork) {
        web3j = Web3jFactory.build(new HttpService(defaultNetwork.rpcServerUrl, httpClient, false));
    }

    @Override
    public Observable<Token[]> fetch(String walletAddress) {
        return Observable.create(e -> {
            NetworkInfo defaultNetwork = ethereumNetworkRepository.getDefaultNetwork();
            Wallet wallet = new Wallet(walletAddress);
            Token[] tokens;
        /*    tokenLocalSource.fetch(defaultNetwork, wallet)
                    .map(items -> {
                        int len = items.length;
                        Token[] result = new Token[len];
                        for (int i = 0; i < len; i++) {
                            result[i] = new Token(items[i], null);
                        }

                        //增加来自交易记录中的新的合约，第一次先插入数据库中，第二次查询将会显示
                        Contract[] contract2Add = ContractRepository.buildContract(result);
                        for(int i=0;i<contract2Add.length;i++){
                            //addToken(wallet,contract2Add[i].getAddress(),contract2Add[i].getName(),contract2Add[i].getSymbol(),(int)contract2Add[i].getDecimals());
                            tokenLocalSource.putAtOnce(
                                    defaultNetwork,
                                    wallet,
                                    new TokenInfo(contract2Add[i].getAddress(),contract2Add[i].getName(),contract2Add[i].getSymbol(),(int)contract2Add[i].getDecimals(),contract2Add[i].getTotalSupply()));
                        }

                        return result;
                    })
                    .blockingGet();
        //    e.onNext(tokens);*/

            // updateTokenInfoCache(defaultNetwork, wallet);
            //transactionLocalSource.clear();
            if (defaultNetwork.backendVersion <= 1) {
                com.wallet.crypto.utcapp.entity.Transaction[] transactions = transactionLocalSource.fetchTransaction(defaultNetwork, wallet).blockingGet();
                Contract[] contract2Add = ContractRepository.getContractsFromTransaction(transactions);
                tokenLocalSource.putInNeedOrUpdate(defaultNetwork, wallet, contract2Add);
            }
            tokens = tokenLocalSource.fetch(defaultNetwork, wallet)
                    .map(items -> {
                        int len = items.length;
                        Token[] result = new Token[len];

                        for (int i = 0; i < len; i++) {
                            BigDecimal balance = null;
                            try {
                                if (ethereumNetworkRepository.getDefaultNetwork().backendVersion <= 1) {
                                    balance = getBalance(wallet, items[i]);
                                } else {
                                    balance = TextUtils.isEmpty(items[i].balance) ? BigDecimal.ZERO : new BigDecimal(items[i].balance);
                                }
                            } catch (Exception e1) {
                                Log.d("TOKEN", "Err", e1);
                                /* Quietly */
                            }
                            result[i] = new Token(items[i], balance);
                        }

                        return result;
                    }).blockingGet();
            e.onNext(tokens);
        });
    }

    //xieyueshu
    @Override
    public Completable addToken(Wallet wallet, String address, String name, String symbol, int decimals, String totalSupply) {
        return tokenLocalSource.put(
                ethereumNetworkRepository.getDefaultNetwork(),
                wallet,
                new TokenInfo(address, name, symbol, decimals, totalSupply, "", "", "", "", "", ""));
    }

    @Override
    public Completable addToken(Wallet wallet, String address, String symbol, int decimals, String totalSupply) {
        return tokenLocalSource.put(
                ethereumNetworkRepository.getDefaultNetwork(),
                wallet,
                new TokenInfo(address, "", symbol, decimals, totalSupply, "", "", "", "", "", ""));
    }

    private void updateTokenInfoCache(NetworkInfo defaultNetwork, Wallet wallet) {
        if (!defaultNetwork.isMainNetwork) {
            return;
        }
        tokenNetworkService
                .fetch(wallet.address)
                .flatMapCompletable(items -> Completable.fromAction(() -> {
                    for (TokenInfo tokenInfo : items) {
                        try {
                            tokenLocalSource.put(
                                    ethereumNetworkRepository.getDefaultNetwork(), wallet, tokenInfo)
                                    .blockingAwait();
                        } catch (Throwable t) {
                            Log.d("TOKEN_REM", "Err", t);
                        }
                    }
                }));
        //xys
        //.blockingAwait();
    }

    private BigDecimal getBalance(Wallet wallet, TokenInfo tokenInfo) throws Exception {
        org.web3j.abi.datatypes.Function function = balanceOf(wallet.address);
        String responseValue = callSmartContractFunction(function, tokenInfo.address, wallet);

        List<Type> response = FunctionReturnDecoder.decode(
                responseValue, function.getOutputParameters());
        if (response.size() == 1) {
            return new BigDecimal(((Uint256) response.get(0)).getValue());
        } else {
            return null;
        }
    }

    private static org.web3j.abi.datatypes.Function balanceOf(String owner) {
        return new org.web3j.abi.datatypes.Function(
                "balanceOf",
                Collections.singletonList(new Address(owner)),
                Collections.singletonList(new TypeReference<Uint256>() {
                }));
    }

    private String callSmartContractFunction(
            org.web3j.abi.datatypes.Function function, String contractAddress, Wallet wallet) throws Exception {
        String encodedFunction = FunctionEncoder.encode(function);

        org.web3j.protocol.core.methods.response.EthCall response = web3j.ethCall(
                Transaction.createEthCallTransaction(wallet.address, contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST)
                .sendAsync().get();

        return response.getValue();
    }

    public static byte[] createTokenTransferData(String to, BigInteger tokenAmount) {
        List<Type> params = Arrays.<Type>asList(new Address(to), new Uint256(tokenAmount));

        List<TypeReference<?>> returnTypes = Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
        });

        Function function = new Function("transfer", params, returnTypes);
        String encodedFunction = FunctionEncoder.encode(function);
        return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(encodedFunction));
    }


    private static final String CONTRACT_CODE = "0x60606040526002805460ff19166012179055341561001c57600080fd5b604051610a1c380380610a1c833981016040528080519190602001805182019190602001805160025460ff16600a0a85026003819055600160a060020a03331660009081526004602052604081209190915592019190508280516100849291602001906100a1565b5060018180516100989291602001906100a1565b5050505061013c565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106100e257805160ff191683800117855561010f565b8280016001018555821561010f579182015b8281111561010f5782518255916020019190600101906100f4565b5061011b92915061011f565b5090565b61013991905b8082111561011b5760008155600101610125565b90565b6108d18061014b6000396000f3006060604052600436106100b95763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166306fdde0381146100be578063095ea7b31461014857806318160ddd1461017e57806323b872dd146101a3578063313ce567146101cb57806342966c68146101f457806370a082311461020a57806379cc67901461022957806395d89b411461024b578063a9059cbb1461025e578063cae9ca5114610282578063dd62ed3e146102e7575b600080fd5b34156100c957600080fd5b6100d161030c565b60405160208082528190810183818151815260200191508051906020019080838360005b8381101561010d5780820151838201526020016100f5565b50505050905090810190601f16801561013a5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b341561015357600080fd5b61016a600160a060020a03600435166024356103aa565b604051901515815260200160405180910390f35b341561018957600080fd5b6101916103da565b60405190815260200160405180910390f35b34156101ae57600080fd5b61016a600160a060020a03600435811690602435166044356103e0565b34156101d657600080fd5b6101de610457565b60405160ff909116815260200160405180910390f35b34156101ff57600080fd5b61016a600435610460565b341561021557600080fd5b610191600160a060020a03600435166104eb565b341561023457600080fd5b61016a600160a060020a03600435166024356104fd565b341561025657600080fd5b6100d16105d9565b341561026957600080fd5b610280600160a060020a0360043516602435610644565b005b341561028d57600080fd5b61016a60048035600160a060020a03169060248035919060649060443590810190830135806020601f8201819004810201604051908101604052818152929190602084018383808284375094965061065395505050505050565b34156102f257600080fd5b610191600160a060020a0360043581169060243516610781565b60008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156103a25780601f10610377576101008083540402835291602001916103a2565b820191906000526020600020905b81548152906001019060200180831161038557829003601f168201915b505050505081565b600160a060020a033381166000908152600560209081526040808320938616835292905220819055600192915050565b60035481565b600160a060020a0380841660009081526005602090815260408083203390941683529290529081205482111561041557600080fd5b600160a060020a038085166000908152600560209081526040808320339094168352929052208054839003905561044d84848461079e565b5060019392505050565b60025460ff1681565b600160a060020a0333166000908152600460205260408120548290101561048657600080fd5b600160a060020a03331660008181526004602052604090819020805485900390556003805485900390557fcc16f5dbb4873280815c1ee09dbd06736cffcc184412cf7a71a0fdb75d397ca59084905190815260200160405180910390a2506001919050565b60046020526000908152604090205481565b600160a060020a0382166000908152600460205260408120548290101561052357600080fd5b600160a060020a038084166000908152600560209081526040808320339094168352929052205482111561055657600080fd5b600160a060020a038084166000818152600460209081526040808320805488900390556005825280832033909516835293905282902080548590039055600380548590039055907fcc16f5dbb4873280815c1ee09dbd06736cffcc184412cf7a71a0fdb75d397ca59084905190815260200160405180910390a250600192915050565b60018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156103a25780601f10610377576101008083540402835291602001916103a2565b61064f33838361079e565b5050565b60008361066081856103aa565b156107795780600160a060020a0316638f4ffcb1338630876040518563ffffffff167c01000000000000000000000000000000000000000000000000000000000281526004018085600160a060020a0316600160a060020a0316815260200184815260200183600160a060020a0316600160a060020a0316815260200180602001828103825283818151815260200191508051906020019080838360005b838110156107165780820151838201526020016106fe565b50505050905090810190601f1680156107435780820380516001836020036101000a031916815260200191505b5095505050505050600060405180830381600087803b151561076457600080fd5b5af1151561077157600080fd5b505050600191505b509392505050565b600560209081526000928352604080842090915290825290205481565b6000600160a060020a03831615156107b557600080fd5b600160a060020a038416600090815260046020526040902054829010156107db57600080fd5b600160a060020a038316600090815260046020526040902054828101101561080257600080fd5b50600160a060020a0380831660008181526004602052604080822080549488168084528284208054888103909155938590528154870190915591909301927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9085905190815260200160405180910390a3600160a060020a0380841660009081526004602052604080822054928716825290205401811461089f57fe5b505050505600a165627a7a72305820716da1fe3419571a3c3fa845a628d9f1ec18c456f298161c22d2fbe6b6ea70260029";

    /**
     * 创建合约方法。
     *
     * @param name
     * @param symbol
     * @param supply
     * @return
     */
    public static byte[] createContractByte(String name, String symbol, BigInteger supply, int decimals) {

        String data = createContractString(name, symbol, supply, decimals);

        return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(data));
    }

    public static String createContractString(String name, String symbol, BigInteger supply, int decimals) {

//        000000000000000000000000000000000000000000000000000000020c855800		//发行总量, 0x20c855800 = 8800000000
//        0000000000000000000000000000000000000000000000000000000000000060		//代币名称偏移，固定值即可, 0x60 = 96
//        00000000000000000000000000000000000000000000000000000000000000a0		//代币符号偏移，固定值即可, 0xa0 = 160
//        0000000000000000000000000000000000000000000000000000000000000003		//代币名称长度, strlen("WEN") = 3
//        57454e0000000000000000000000000000000000000000000000000000000000		//代币字符串， 57454e = WEN， 'W' = 0x57, 'E' = 0x45, 'N' = 0x4e
//        0000000000000000000000000000000000000000000000000000000000000003		//代币符号长度, strlen("WEN") = 3
//        57454e0000000000000000000000000000000000000000000000000000000000		//代币符号字符串， 57454e = WEN， 'W' = 0x57, 'E' = 0x45, 'N' = 0x4e

        String supplyHex = supply.toString(16);
        String nameOffsetHex = "0000000000000000000000000000000000000000000000000000000000000060";
        String symbolOffsetHex = "00000000000000000000000000000000000000000000000000000000000000a0";
        String nameLenHex = (BigInteger.valueOf(name.length())).toString(16);
        String nameHex = stringToHexString(name);
        String symbolLenHex = (BigInteger.valueOf(symbol.length())).toString(16);
        String symbolHex = stringToHexString(symbol);

        supplyHex = paddingLefString(supplyHex);
        nameLenHex = paddingLefString(nameLenHex);
        nameHex = paddingRightString(nameHex);
        symbolLenHex = paddingLefString(symbolLenHex);
        symbolHex = paddingRightString(symbolHex);


        String data = CONTRACT_CODE + supplyHex + nameOffsetHex + symbolOffsetHex + nameLenHex + nameHex + symbolLenHex + symbolHex;

        return data;
    }


    public static String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    public static String paddingLefString(String s) {
        String padStr = padding(s.length(), 64, '0');
        return padStr + s;
    }

    public static String paddingRightString(String s) {
        String padStr = padding(s.length(), 64, '0');
        return s + padStr;
    }

    public static String padding(int strLen, int maxLen, char padChar) {
        int mode = strLen % maxLen;
        if (mode > 0) {
            int pad = maxLen - mode;
            char[] padStr = new char[pad];
            for (int i = 0; i < padStr.length; i++) {
                padStr[i] = padChar;
            }
            return new String(padStr);
        }
        return "";
    }
}
