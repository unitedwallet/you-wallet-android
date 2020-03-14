package com.wallet.crypto.utcapp.service;

import com.wallet.crypto.utcapp.entity.TokenInfo;

import io.reactivex.Observable;

public interface TokenExplorerClientType {
    Observable<TokenInfo[]> fetch(String walletAddress);
}