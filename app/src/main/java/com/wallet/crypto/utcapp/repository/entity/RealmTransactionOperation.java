package com.wallet.crypto.utcapp.repository.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.wallet.crypto.utcapp.entity.TransactionContract;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmTransactionOperation  extends RealmObject {
    @PrimaryKey
    private String transactionId;
    private String viewType;
    private String from;
    private String to;
    private String value;
    private String contract_address;
    private String contract_name;
    private String contract_totalSupply;
    private long contract_decimals;
    private String contract_symbol;
    public String getContract_address() {
        return contract_address;
    }

    public void setContract_address(String contract_address) {
        this.contract_address = contract_address;
    }

    public String getContract_name() {
        return contract_name;
    }

    public void setContract_name(String contract_name) {
        this.contract_name = contract_name;
    }

    public String getContract_totalSupply() {
        return contract_totalSupply;
    }

    public void setContract_totalSupply(String contract_totalSupply) {
        this.contract_totalSupply = contract_totalSupply;
    }

    public long getContract_decimals() {
        return contract_decimals;
    }

    public void setContract_decimals(long contract_decimals) {
        this.contract_decimals = contract_decimals;
    }

    public String getContract_symbol() {
        return contract_symbol;
    }

    public void setContract_symbol(String contract_symbol) {
        this.contract_symbol = contract_symbol;
    }


    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
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




}
