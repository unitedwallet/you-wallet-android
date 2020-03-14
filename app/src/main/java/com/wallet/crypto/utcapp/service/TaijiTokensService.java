package com.wallet.crypto.utcapp.service;

import com.google.gson.Gson;
import com.wallet.crypto.utcapp.entity.NetworkInfo;
import com.wallet.crypto.utcapp.entity.TokensResponse;
import com.wallet.crypto.utcapp.entity.httpentity.HomeTokenRequest;
import com.wallet.crypto.utcapp.entity.httpentity.HomeTokenResponse;
import com.wallet.crypto.utcapp.repository.EthereumNetworkRepositoryType;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableOperator;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;


public class TaijiTokensService implements TaijiTokensType {

    //private static final String TRUST_API_URL = "https://api.trustwalletapp.com";

    private final OkHttpClient httpClient;
    private final Gson gson;
    private ApiClient apiClient;
    private final EthereumNetworkRepositoryType networkRepository;

    public TaijiTokensService(
            OkHttpClient httpClient,
            Gson gson, EthereumNetworkRepositoryType networkRepository) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.networkRepository = networkRepository;
        this.networkRepository.addOnChangeDefaultNetwork(this::onNetworkChanged);
        NetworkInfo networkInfo = networkRepository.getDefaultNetwork();
        onNetworkChanged(networkInfo);
    }

    private void onNetworkChanged(NetworkInfo networkInfo) {
        buildApiClient(networkInfo.backendUrl);
        //buildApiClient(TOKENS_URL);//测试
    }

    private void buildApiClient(String baseUrl) {
        apiClient = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(ApiClient.class);
    }

    @Override
    public Observable<TokensResponse> fetchTokens(String address) {
        return apiClient
                .fetchTokensPrice(address)
                .lift(apiError())
                .map(r -> r)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<HomeTokenResponse> getHomeToken(String url, HomeTokenRequest homeTokenRequest) {
        buildApiClient(url);
        return apiClient
                .getHomeToken(homeTokenRequest.getMap())
                .lift(apiError())
                .map(r -> {
                r.wallet_id=homeTokenRequest.wallet_id;
                return r;
                })
                .subscribeOn(Schedulers.io());

    }

    private static @NonNull
    <T> ApiErrorOperator<T> apiError() {
        return new ApiErrorOperator<>();
    }

    public interface ApiClient {
        @GET("wallet/{address}")
        Observable<Response<TokensResponse>> fetchTokensPrice(@Path("address") String address);

        @POST("/wallet2")
        Observable<Response<HomeTokenResponse>> getHomeToken(@Body HashMap<String, String> hashMap);
    }


    private final static class ApiErrorOperator<T> implements ObservableOperator<T, Response<T>> {

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
