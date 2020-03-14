package com.wallet.crypto.utcapp.service;

import com.wallet.crypto.utcapp.entity.NewsItem;
import com.wallet.crypto.utcapp.entity.httpentity.BaseResponse;
import com.wallet.crypto.utcapp.entity.httpentity.CheckCodeRsponse;
import com.wallet.crypto.utcapp.entity.httpentity.LoginRequest;
import com.wallet.crypto.utcapp.entity.httpentity.RegisterRequest;
import com.wallet.crypto.utcapp.entity.httpentity.RegisterResponse;
import com.wallet.crypto.utcapp.entity.httpentity.SmsRequest;

import io.reactivex.Observable;
import okhttp3.ResponseBody;

public interface LoginServiceType {
    Observable<CheckCodeRsponse> getCheckCode();
    Observable<ResponseBody> verify(String checkCode);
    Observable<BaseResponse> sms(SmsRequest smsRequest);
    Observable<RegisterResponse> register(RegisterRequest registerRequest);
    Observable<RegisterResponse> login(LoginRequest loginRequest);
    Observable<BaseResponse> logout(String token);
    Observable<BaseResponse> bindWallet(String token,String address);
}
