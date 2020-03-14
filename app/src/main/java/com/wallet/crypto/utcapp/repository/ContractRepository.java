package com.wallet.crypto.utcapp.repository;


import android.text.TextUtils;
import android.widget.TextView;

import com.wallet.crypto.utcapp.R;
import com.wallet.crypto.utcapp.entity.Token;
import com.wallet.crypto.utcapp.entity.Transaction;
import com.wallet.crypto.utcapp.entity.TransactionOperation;
import com.wallet.crypto.utcapp.repository.entity.Contract;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 合约存放及更新类。
 */
public class ContractRepository {

    //static Contract[] contract = null;

    static Set<Contract> contracts =  new HashSet<Contract>();

    static Map<String, Contract> address2Contract = new HashMap<String, Contract>();
    static Map<String, Contract> txid2Contract = new HashMap<String, Contract>();

    public static void clear(){
        contracts.clear();
        address2Contract.clear();
        txid2Contract.clear();
    }

    /**
     * 在给定的交易记录数组中，找出相关的合约信息，并存在公用的地方。
     *
     * @param transactions
     */
    public static void buildContract(Transaction[] transactions){

        for (Transaction transaction : transactions) {

            TransactionOperation operation = transaction.operations == null
                    || transaction.operations.length == 0 ? null : transaction.operations[0];

            if (transaction.to == null || transaction.to.length() == 0) {
                //创建合约
                //后续实现从区块链节点获得合约的信息，name、symbol、decimals等；暂时只在用户创建合约后立刻保存该信息。
            } else if (operation != null && operation.contract != null) {
                //代币转账
                Contract contract = address2Contract.get(operation.contract.address);
                if(contract==null){
                    contract = new Contract();
                    contract.setAddress(operation.contract.address);
                    contract.setName(operation.contract.name);
                    contract.setSymbol(operation.contract.symbol);
                    contract.setDecimals(operation.contract.decimals);
                    contract.setTimeStamp(transaction.timeStamp);
                    contract.setTotalSupply(operation.contract.totalSupply);
                    //只能从创建合约才知道谁是合约的主人
                    //contract.setFrom(operation.from);
                    //交易id也不准确，因为一个合约会对应多个交易
                    //contract.setTransactionId(transaction.hash);
                    address2Contract.put(contract.getAddress(),contract);
                    contracts.add(contract);
                }
                txid2Contract.put(transaction.hash,contract);
            } else {
                //正常交易
            }
        }
    }
    public static Contract[] getContractsFromTransaction(Transaction[] transactions){
        Map<String, Contract> contractNeedInsert = new HashMap<String, Contract>();
        for (Transaction transaction : transactions) {

            TransactionOperation operation = transaction.operations == null
                    || transaction.operations.length == 0 ? null : transaction.operations[0];

            if (transaction.to == null || transaction.to.length() == 0) {
                //创建合约
                //后续实现从区块链节点获得合约的信息，name、symbol、decimals等；暂时只在用户创建合约后立刻保存该信息。
            } else if (operation != null && operation.contract != null) {
                //代币转账
                Contract contract = contractNeedInsert.get(operation.contract.address);
                if(contract==null){
                    contract = new Contract();
                    contract.setAddress(operation.contract.address);
                    contract.setName(operation.contract.name);
                    contract.setSymbol(operation.contract.symbol);
                    contract.setDecimals(operation.contract.decimals);
                    contract.setTimeStamp(transaction.timeStamp);
                    contract.setTotalSupply(operation.contract.totalSupply);
                    //只能从创建合约才知道谁是合约的主人
                    //contract.setFrom(operation.from);
                    //交易id也不准确，因为一个合约会对应多个交易
                    //contract.setTransactionId(transaction.hash);
                    contractNeedInsert.put(contract.getAddress(),contract);
                    //  contracts.add(contract);
                }
                // txid2Contract.put(transaction.hash,contract);
            } else {
                //正常交易
            }
        }
        return contractNeedInsert.values().toArray(new Contract[0]);
    }
    public static Contract[] fetchAddition(Token[] tokens){

        Map<String,Token> address2Token = new HashMap<String,Token>();
        for (int i = 0; i < tokens.length; i++) {
            address2Token.put(tokens[i].tokenInfo.address,tokens[i]);
        }

        List<Contract> contract2Add = new ArrayList<Contract>();

        Contract[] txContracts = contract();
        for(int i = 0; i < txContracts.length; i++){
            Contract contract = txContracts[i];
            if(!address2Token.containsKey(contract.getAddress())){
                contract2Add.add(contract);
            }
        }

        return contract2Add.toArray(new Contract[0]);
    }
    //不管合约有没有在数据库存在，都进行更新动作
    public static Contract[] fetchAddContracts(Token[] tokens){
        List<Contract> contract2Add = new ArrayList<Contract>();
        Contract[] txContracts = contract();
        contract2Add.addAll(Arrays.asList(txContracts));

        return contract2Add.toArray(new Contract[0]);
    }
    public static Contract[] contract(){
        return contracts.toArray(new Contract[0]);
    }
}
