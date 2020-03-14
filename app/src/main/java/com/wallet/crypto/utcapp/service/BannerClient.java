package com.wallet.crypto.utcapp.service;

import com.google.gson.Gson;
import com.wallet.crypto.utcapp.entity.BannerItem;
import com.wallet.crypto.utcapp.entity.DappItem;
import com.wallet.crypto.utcapp.entity.NetworkInfo;
import com.wallet.crypto.utcapp.repository.EthereumNetworkRepositoryType;

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
import retrofit2.http.GET;

import static com.wallet.crypto.utcapp.C.DISCOVER_URL;

public class BannerClient implements BannerClientType {

    private final OkHttpClient httpClient;
    private final Gson gson;


    private BannerApiClient dappApiClient;

    public BannerClient(
            OkHttpClient httpClient,
            Gson gson
    ) {
        this.httpClient = httpClient;
        this.gson = gson;
        buildApiClient(DISCOVER_URL);
    }

    private void buildApiClient(String baseUrl) {
        dappApiClient = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(BannerApiClient.class);
    }

    private static @NonNull
    <T> ApiErrorOperator<T> apiError(Gson gson) {
        return new ApiErrorOperator<>(gson);
    }

    @Override
    public Observable<BannerItem[]> fetchBannerItems() {
        return dappApiClient
                .fetchBannerItems()
                .lift(apiError(gson))
                .map(r -> r.banners)
                .subscribeOn(Schedulers.io());
    }

    private interface BannerApiClient {
        @GET("/banners")
            // TODO: startBlock - it's pagination. Not work now
        Observable<Response<BannerScanResponse>> fetchBannerItems();
    }

    private final static class BannerScanResponse {
        BannerItem[] banners;
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
