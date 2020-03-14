package com.wallet.crypto.utcapp.repository;

import com.wallet.crypto.utcapp.entity.Transaction;

public interface ContractRepositoryType {

    public void buildContract(Transaction[] transactions);

    public void buildContract(String address);

}
