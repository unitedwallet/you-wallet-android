package com.wallet.crypto.utcapp.service;

import com.google.gson.Gson;
import com.wallet.crypto.utcapp.C;
import com.wallet.crypto.utcapp.entity.NewsItem;
import com.wallet.crypto.utcapp.entity.httpentity.BaseResponse;
import com.wallet.crypto.utcapp.entity.httpentity.CheckCodeRsponse;
import com.wallet.crypto.utcapp.entity.httpentity.HomeTokenResponse;
import com.wallet.crypto.utcapp.entity.httpentity.LoginRequest;
import com.wallet.crypto.utcapp.entity.httpentity.MarketTickerResponse;
import com.wallet.crypto.utcapp.entity.httpentity.NewsDetailResponse;
import com.wallet.crypto.utcapp.entity.httpentity.NewsResponse;
import com.wallet.crypto.utcapp.entity.httpentity.RegisterRequest;
import com.wallet.crypto.utcapp.entity.httpentity.RegisterResponse;
import com.wallet.crypto.utcapp.entity.httpentity.SmsRequest;
import com.wallet.crypto.utcapp.repository.EthereumNetworkRepositoryType;
import com.wallet.crypto.utcapp.util.LogInterceptor;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOperator;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static com.wallet.crypto.utcapp.C.DISCOVER_URL;
import static com.wallet.crypto.utcapp.C.NEWS_URL;

public class LoginService implements LoginServiceType {
    private OkHttpClient httpClient;
    private final Gson gson;


    private ApiClient newsApiClient;


    public LoginService(

            Gson gson
    ) {

        this.gson = gson;


    }

    private void buildApiClient(String baseUrl, String token) {


        httpClient = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)//设置连接超时时间
                .readTimeout(60, TimeUnit.SECONDS).addInterceptor(new LogInterceptor(token))//设置读取超时时间
                .build();
        newsApiClient = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(ApiClient.class);
    }


    private static @NonNull
    <T> ApiErrorOperator<T> apiError(Gson gson) {
        return new ApiErrorOperator<>(gson);
    }

    @Override
    public Observable<CheckCodeRsponse> getCheckCode() {
        buildApiClient(DISCOVER_URL, "");
        return newsApiClient
                .getCheckCode()
                .lift(apiError(gson))
                .map(r -> r)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<ResponseBody> verify(String checkCode) {
        buildApiClient(DISCOVER_URL, "");
        return newsApiClient
                .verify(checkCode)
                .lift(apiError(gson))
                .map(r -> r)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<BaseResponse> sms(SmsRequest smsRequest) {
        buildApiClient(DISCOVER_URL, "");
        return newsApiClient
                .sendsms(smsRequest.getMap())
                .lift(apiError(gson))
                .map(r -> r)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<RegisterResponse> register(RegisterRequest registerRequest) {
        buildApiClient(DISCOVER_URL, "");
        return newsApiClient
                .register(registerRequest.getMap())
                .lift(apiError(gson))
                .map(r -> r)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<RegisterResponse> login(LoginRequest loginRequest) {
        buildApiClient(DISCOVER_URL, "");
        return newsApiClient
                .login(loginRequest.getMap())
                .lift(apiError(gson))
                .map(r -> r)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<BaseResponse> logout(String token) {
        buildApiClient(DISCOVER_URL, token);

        return newsApiClient
                .logout()
                .lift(apiError(gson))
                .map(r -> r)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<BaseResponse> bindWallet(String token, String address) {
        buildApiClient(DISCOVER_URL, token);
        return newsApiClient
                .bindWallet(address)
                .lift(apiError(gson))
                .map(r -> r)
                .subscribeOn(Schedulers.io());
    }


    private interface ApiClient {
        @POST("/sendsms")
        Observable<Response<BaseResponse>> sendsms(@Body HashMap<String, String> hashMap);

        @POST("/reg")
        Observable<Response<RegisterResponse>> register(@Body HashMap<String, String> hashMap);

        @POST("/login")
        Observable<Response<RegisterResponse>> login(@Body HashMap<String, String> hashMap);

        @POST("/checkCode")
        Observable<Response<CheckCodeRsponse>> getCheckCode();

        @POST("/verify")
        @FormUrlEncoded
        Observable<Response<ResponseBody>> verify(@Field("checkCode") String checkCode);

        @POST("/logout")
        Observable<Response<BaseResponse>> logout();

        @POST("/bindWallet")
        @FormUrlEncoded
        Observable<Response<BaseResponse>> bindWallet(@Field("wallet") String wallet);
    }


    private final static class ApiErrorOperator<T> implements ObservableOperator<T, Response<T>> {

        private final Gson gson;

        public ApiErrorOperator(Gson gson) {
            this.gson = gson;
        }

        @Override
        public Observer<? super Response<T>> apply(Observer<? super T> observer) throws Exception {
            return new DisposableObserver<Response<T>>() {
                @Override
                public void onNext(Response<T> response) {
                    observer.onNext(response.body());
                    observer.onComplete();
                }

                @Override
                public void onError(Throwable e) {
                    observer.onError(e);
                }

                @Override
                public void onComplete() {
                    observer.onComplete();
                }
            };
        }
    }
}
