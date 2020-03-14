package com.wallet.crypto.utcapp.repository.entity;

import java.math.BigDecimal;

public class Contract {

    //contract owner 可能为空
    private String from;
    //transaction id 隶属整个大交易的ID TxID
    //private String transactionId;
    //Primary Key: contract id
    private String address;
    private String name;
    private String symbol;
    private long decimals;
    private long timeStamp;
    private String totalSupply;

    public String balance;//: "12344",

    public String qualityName;//: "Bit Bit Coin",
    public String type;//: "ERC20"
    public String price;//:"451.546",
    public String percent_change_24h;//:"-2.68",
    public String image;//:"https:\/\/files.coinmarketcap.com\/static\/img\/coins\/128x128\/ethereum.png"
    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getQualityName() {
        return qualityName;
    }

    public void setQualityName(String qualityName) {
        this.qualityName = qualityName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPercent_change_24h() {
        return percent_change_24h;
    }

    public void setPercent_change_24h(String percent_change_24h) {
        this.percent_change_24h = percent_change_24h;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public long getDecimals() {
        return decimals;
    }

    public void setDecimals(long decimals) {
        this.decimals = decimals;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(String totalSupply) {
        this.totalSupply = totalSupply;
    }
}
