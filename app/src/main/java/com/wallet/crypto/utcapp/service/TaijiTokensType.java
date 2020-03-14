package com.wallet.crypto.utcapp.service;

import com.wallet.crypto.utcapp.entity.TokensResponse;
import com.wallet.crypto.utcapp.entity.httpentity.HomeTokenRequest;
import com.wallet.crypto.utcapp.entity.httpentity.HomeTokenResponse;

import io.reactivex.Observable;

public interface TaijiTokensType {
    Observable<TokensResponse> fetchTokens(String address);
    Observable<HomeTokenResponse> getHomeToken(String url,HomeTokenRequest homeTokenRequest);
}
