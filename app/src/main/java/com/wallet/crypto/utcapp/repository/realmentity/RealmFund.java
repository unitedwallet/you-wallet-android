package com.wallet.crypto.utcapp.repository.realmentity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class RealmFund extends RealmObject {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*fund	链_资产
        chain_id			哪条链
        name			资产名字
        type			主币0还是代币1
        create_time	timestamp
        update_time	timestamp*/
    @PrimaryKey
    private String id;
    @Required
    private String chain_id;
    @Required
    private String name;
    //主币0还是代币1
    private String type;
    private String   gas_symbol;
    private String   gas_price;
    private String   gas_price_min;
    private String   gas_price_max;
    private String   gas_limit;
    private String   gas_limit_min;
    private String   gas_limit_max;


    private String icon;
    private String rule_address;
    private String contract_address;
    private boolean is_default;
    private long create_time;
    private long update_time;
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

    public boolean isIs_default() {
        return is_default;
    }

    public String getGas_symbol() {
        return gas_symbol;
    }

    public void setGas_symbol(String gas_symbol) {
        this.gas_symbol = gas_symbol;
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

    public String getChain_id() {
        return chain_id;
    }

    public void setChain_id(String chain_id) {
        this.chain_id = chain_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean getIs_default() {
        return is_default;
    }

    public void setIs_default(boolean is_default) {
        this.is_default = is_default;
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
