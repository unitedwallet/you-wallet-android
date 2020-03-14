package com.wallet.crypto.utcapp.repository.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.wallet.crypto.utcapp.entity.TransactionOperation;

import java.util.Arrays;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmTransaction extends RealmObject {
    @PrimaryKey
    private String hash;
    private String blockNumber;
    private long timeStamp;
    private int nonce;
    private String from;
    private String to;
    private String value;
    private String gas;
    private String gasPrice;
    private String gasUsed;
    private String input;
    private RealmList<RealmTransactionOperation> operations = new RealmList<>();
    private String error;
    //xieyueshu
    private String contract;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getGas() {
        return gas;
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(String gasPrice) {
        this.gasPrice = gasPrice;
    }

    public String getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(String gasUsed) {
        this.gasUsed = gasUsed;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public RealmList<RealmTransactionOperation> getOperations() {
        return operations;
    }

    public void setOperations(TransactionOperation[] transactionOperations) {
        if( this.operations==null) this.operations=new RealmList<>();
        if (transactionOperations != null && transactionOperations.length > 0) {
            for (TransactionOperation transactionOperation : transactionOperations) {
                RealmTransactionOperation realmTransactionOperation = new RealmTransactionOperation();

                realmTransactionOperation.setContract_address(transactionOperation.contract.address);
                realmTransactionOperation.setContract_decimals(transactionOperation.contract.decimals);
                realmTransactionOperation.setContract_name(transactionOperation.contract.name);
                realmTransactionOperation.setContract_symbol(transactionOperation.contract.symbol);
                realmTransactionOperation.setContract_totalSupply(transactionOperation.contract.totalSupply);

                realmTransactionOperation.setFrom(transactionOperation.from);
                realmTransactionOperation.setTo(transactionOperation.to);
                realmTransactionOperation.setTransactionId(transactionOperation.transactionId);
                realmTransactionOperation.setValue(transactionOperation.value);
                realmTransactionOperation.setViewType(transactionOperation.viewType);
                this.operations.add(realmTransactionOperation);
            }
        }
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }


}
