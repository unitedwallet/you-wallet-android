package com.wallet.crypto.utcapp.repository.realmentity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class RealmWallet extends RealmObject {
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*    name	string	not null unique	钱包名称
            type	int	not null	0:多链， 1 多链主钱包  2靓号钱包  3:ETH，4:BTC 等等
            key	string		多链的时候助记词，单链的时候私钥或地址
            son	int		主钱包时，子钱包的数量，或者为0
            create_time	timestamp
            update_time	timestamp*/
    @PrimaryKey
    private String id;
    @Required
    private String key;
    @Required
    private String type;
    private String name;
    //0:未选中   1：已选中
    private boolean is_select;
    private int son;
    private long create_time;
    private long update_time;
    public boolean getIs_select() {
        return is_select;
    }

    public void setIs_select(boolean is_select) {
        this.is_select = is_select;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public int getSon() {
        return son;
    }

    public void setSon(int son) {
        this.son = son;
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
