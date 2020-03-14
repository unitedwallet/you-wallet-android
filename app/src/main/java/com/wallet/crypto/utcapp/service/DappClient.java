package com.wallet.crypto.utcapp.service;

import com.google.gson.Gson;
import com.wallet.crypto.utcapp.entity.DappItem;
import com.wallet.crypto.utcapp.entity.DappResponse;
import com.wallet.crypto.utcapp.entity.NetworkInfo;
import com.wallet.crypto.utcapp.entity.NoticeResponse;
import com.wallet.crypto.utcapp.entity.Transaction;
import com.wallet.crypto.utcapp.repository.EthereumNetworkRepositoryType;
import com.wallet.crypto.utcapp.util.LogInterceptor;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOperator;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import static com.wallet.crypto.utcapp.C.DISCOVER_URL;


public class DappClient implements DappClientType {

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final EthereumNetworkRepositoryType networkRepository;

    private DappApiClient dappApiClient;

    public DappClient(
            OkHttpClient httpClient,
            Gson gson,
            EthereumNetworkRepositoryType networkRepository) {
           this.httpClient = httpClient;

        this.gson = gson;
        this.networkRepository = networkRepository;
        buildApiClient(DISCOVER_URL);
    }

    private void buildApiClient(String baseUrl) {
        dappApiClient = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(DappApiClient.class);
    }


    private static @NonNull
    <T> ApiErrorOperator<T> apiError(Gson gson) {
        return new ApiErrorOperator<>(gson);
    }

    @Override
    public Observable<DappResponse> fetchDappItems() {
        return dappApiClient
                .fetchDappItems()
                .lift(apiError(gson))
                .map(r -> r)
                .subscribeOn(Schedulers.io());
    }
    @Override
    public Observable<DappResponse> fetchAllDappItems( String type,  int page,  int limit){
        return dappApiClient
                .fetchAllDappItems(type,page,limit)
                .lift(apiError(gson))
                .map(r -> r)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<NoticeResponse> fetchNotice() {
        return dappApiClient
                .fetchNotice()
                .lift(apiError(gson))
                //   .map(r -> r.news)
                .map(r -> r)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<NoticeResponse> fetchNoticeNext(int page, long time) {
        return dappApiClient
                .fetchNoticeNext(page+"",time+"")
                .lift(apiError(gson))
                //   .map(r -> r.news)
                .map(r -> r)
                .subscribeOn(Schedulers.io());}

    private interface DappApiClient {
        @GET("/dapps2?")
            // TODO: startBlock - it's pagination. Not work now
        Observable<Response<DappResponse>> fetchDappItems();

        @GET("/dapps2?")
            // TODO: startBlock - it's pagination. Not work now
        Observable<Response<DappResponse>> fetchAllDappItems(@Query("type") String type, @Query("page") int page, @Query("limit") int limit);


        @GET("/news?page=1&limit=10")
            // TODO: startBlock - it's pagination. Not work now
        Observable<Response<NoticeResponse>> fetchNotice();
        @GET("/news?limit=10")
            // TODO: startBlock - it's pagination. Not work now
        Observable<Response<NoticeResponse>> fetchNoticeNext(@Query("page") String page,@Query("time") String time);
    }

    private final static class DappScanResponse {
        DappItem[] dapps;
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
