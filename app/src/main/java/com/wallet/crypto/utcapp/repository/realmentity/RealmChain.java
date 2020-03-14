package com.wallet.crypto.utcapp.repository.realmentity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class RealmChain extends RealmObject {
    /*   chain	链
       id
        name
       symbol
       total_supply
       rpc
       url
       description
       rule_private_key
       rule_address
       create_time	timestamp
       update_time	timestamp*/
    @PrimaryKey
    private String id;
    private String name;
    private String symbol;
    private String total_supply;
    private String rpc;
    private int chainID;

    private String icon;
    private String description;
    private String rule_private_key;
    private String rule_address;
    private long create_time;
    private long update_time;

    private String   web_site;
    private String   explorer;
    private String   backend;
    public String getWeb_site() {
        return web_site;
    }

    public void setWeb_site(String web_site) {
        this.web_site = web_site;
    }

    public String getExplorer() {
        return explorer;
    }

    public void setExplorer(String explorer) {
        this.explorer = explorer;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }


    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getTotal_supply() {
        return total_supply;
    }

    public void setTotal_supply(String total_supply) {
        this.total_supply = total_supply;
    }

    public String getRpc() {
        return rpc;
    }

    public void setRpc(String rpc) {
        this.rpc = rpc;
    }

    public int getChainId() {
        return chainID;
    }

    public void setChainId(int chainId) {
        this.chainID = chainId;
    }



    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRule_private_key() {
        return rule_private_key;
    }

    public void setRule_private_key(String rule_private_key) {
        this.rule_private_key = rule_private_key;
    }

    public String getRule_address() {
        return rule_address;
    }

    public void setRule_address(String rule_address) {
        this.rule_address = rule_address;
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
