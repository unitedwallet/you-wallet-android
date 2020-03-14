package com.wallet.crypto.utcapp.repository.realmentity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class RealmBalance extends RealmObject {
    /*  balance	地址_资产_余额
      id		primary key     account_id+fund
      account_id		foreign key 使用address的值
      address	string
      chain	string     哪个链上的资产
      fund	string      链上的那种资产  代币还是主币
      balance	bigint   余额
      create_time	timestamp
      update_time	timestamp*/

    @Required
    private String account_id;
    @Required
    private String address;
    @Required
    private String chain_id;
    @Required
    private String fund_id;
    @Required
    private String wallet_id;
    private String contract_address;
    private String rule_address;
    @Required
    private String fund_name;
    private String balance;
    private String cny_price;
    private String cny_balance;
    private String icon;
    private long create_time;
    private long update_time;
    private String   gas_symbol;
    private String   gas_price;
    private String   gas_price_min;
    private String   gas_price_max;
    private String   gas_limit;
    private String   gas_limit_min;
    private String   gas_limit_max;

    private int decimals;





    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public String getRule_address() {
        return rule_address;
    }

    public void setRule_address(String rule_address) {
        this.rule_address = rule_address;
    }


    public String getGas_price() {
        return gas_price;
    }

    public void setGas_price(String gas_price) {
        this.gas_price = gas_price;
    }

    public String getGas_price_min() {
        return gas_price_min;
    }

    public void setGas_price_min(String gas_price_min) {
        this.gas_price_min = gas_price_min;
    }

    public String getGas_price_max() {
        return gas_price_max;
    }

    public void setGas_price_max(String gas_price_max) {
        this.gas_price_max = gas_price_max;
    }

    public String getGas_limit() {
        return gas_limit;
    }

    public void setGas_limit(String gas_limit) {
        this.gas_limit = gas_limit;
    }

    public String getGas_limit_min() {
        return gas_limit_min;
    }

    public void setGas_limit_min(String gas_limit_min) {
        this.gas_limit_min = gas_limit_min;
    }

    public String getGas_limit_max() {
        return gas_limit_max;
    }

    public void setGas_limit_max(String gas_limit_max) {
        this.gas_limit_max = gas_limit_max;
    }



    public String getGas_symbol() {
        return gas_symbol;
    }

    public void setGas_symbol(String gas_symbol) {
        this.gas_symbol = gas_symbol;
    }



    public String getWallet_id() {
        return wallet_id;
    }

    public void setWallet_id(String wallet_id) {
        this.wallet_id = wallet_id;
    }



    public String getCny_price() {
        return cny_price;
    }

    public void setCny_price(String cny_price) {
        this.cny_price = cny_price;
    }



    public String getCny_balance() {
        return cny_balance;
    }

    public void setCny_balance(String cny_balance) {
        this.cny_balance = cny_balance;
    }




    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }


    public String getContract_address() {
        return contract_address;
    }

    public void setContract_address(String contract_address) {
        this.contract_address = contract_address;
    }

    public String getFund_name() {
        return fund_name;
    }

    public void setFund_name(String fund_name) {
        this.fund_name = fund_name;
    }


    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getChainId() {
        return chain_id;
    }

    public void setChainId(String chain_id) {
        this.chain_id = chain_id;
    }

    public String getFund_id() {
        return fund_id;
    }

    public void setFund_id(String fund_id) {
        this.fund_id = fund_id;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public long getCreate_time() {
        return create_time;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public long getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(long update_time) {
        this.update_time = update_time;
    }


}
