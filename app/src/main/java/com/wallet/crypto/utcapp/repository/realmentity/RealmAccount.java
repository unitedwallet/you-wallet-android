package com.wallet.crypto.utcapp.repository.realmentity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class RealmAccount extends RealmObject {
    /*
        account	钱包_地址_链_私钥（主钱包/子钱包分另外一个表，或者每个主钱包的子钱包单独一个表）



        id	auto	primary key     wallet_id+chain_id+private_key
        wallet_id		foreign key
        chain	string	not null
        chain_id	int	not null
        private_key	string	not null
        address	string	not null
        son_order	int		如果是子钱包则表示子钱包的编码，主钱包为0
        create_time	timestamp
        update_time	timestamp
    */
    @Required
    private String id;
    @Required
    private String wallet_id;
    @Required
    private String chain;
    @Required
    private String chain_id;
    @Required
    private String private_key;
    @Required
    private String address;
    private int son_order;
    private long create_time;
    private long update_time;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWallet_id() {
        return wallet_id;
    }

    public void setWallet_id(String wallet_id) {
        this.wallet_id = wallet_id;
    }

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public String getChain_id() {
        return chain_id;
    }

    public void setChain_id(String chain_id) {
        this.chain_id = chain_id;
    }

    public String getPrivate_key() {
        return private_key;
    }

    public void setPrivate_key(String private_key) {
        this.private_key = private_key;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getSon_order() {
        return son_order;
    }

    public void setSon_order(int son_order) {
        this.son_order = son_order;
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
