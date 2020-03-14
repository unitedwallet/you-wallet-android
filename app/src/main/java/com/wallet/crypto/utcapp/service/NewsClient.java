package com.wallet.crypto.utcapp.service;

import com.google.gson.Gson;
import com.wallet.crypto.utcapp.C;
import com.wallet.crypto.utcapp.entity.NewsItem;
import com.wallet.crypto.utcapp.entity.httpentity.MarketTickerResponse;
import com.wallet.crypto.utcapp.entity.httpentity.NewsDetailResponse;
import com.wallet.crypto.utcapp.entity.httpentity.NewsResponse;
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
import retrofit2.http.Query;

import static com.wallet.crypto.utcapp.C.NEWS_URL;

public class NewsClient implements NewsClientType {

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final EthereumNetworkRepositoryType networkRepository;

    private NewsApiClient newsApiClient;


    public NewsClient(
            OkHttpClient httpClient,
            Gson gson,
            EthereumNetworkRepositoryType networkRepository) {
        this.httpClient = httpClient;
        this.gson = gson;
        this.networkRepository = networkRepository;
        buildApiClient(NEWS_URL);
    }

    private void buildApiClient(String baseUrl) {
        newsApiClient = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(NewsApiClient.class);
    }


    private static @NonNull
    <T> ApiErrorOperator<T> apiError(Gson gson) {
        return new ApiErrorOperator<>(gson);
    }

    @Override
    public Observable<NewsItem[]> fetchNewsItems() {

        return newsApiClient
                .fetchNewsItems()
                .lift(apiError(gson))
                //   .map(r -> r.news)
                .map(r -> r.data)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<NewsResponse> fetchNewsItemsNext(long timestamp, int size, int type){
        buildApiClient(NEWS_URL);
        return newsApiClient
                .fetchNewsItemsNext(timestamp, size,type)
                .lift(apiError(gson))
                //   .map(r -> r.news)
                .map(r -> r)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<NewsDetailResponse> getNewsDetail(int id) {
        buildApiClient(NEWS_URL);
        return newsApiClient
                .getNewsDetail(id)
                .lift(apiError(gson))
                //   .map(r -> r.news)
                .map(r -> {
                    return r;
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<MarketTickerResponse> getTicker(String exchange) {
        buildApiClient(C.TICKER_URL);
        return newsApiClient
                .ticker(exchange)
                .lift(apiError(gson))
                //   .map(r -> r.news)
                .map(r -> {
                    r.exchange=exchange;
                    return r;
                })
                .subscribeOn(Schedulers.io());
    }

    private interface NewsApiClient {
        @GET("/news?page=1&limit=10")
            // TODO: startBlock - it's pagination. Not work now
        Observable<Response<NewsResponse>> fetchNewsItems();

        @GET("/rest/information/list/v1")
            // TODO: startBlock - it's pagination. Not work now
        Observable<Response<NewsResponse>> fetchNewsItemsNext(@Query("timestamp") long timestamp, @Query("limit") int size, @Query("type") int type);
        @GET("/rest/information/detail/v1")
            // TODO: startBlock - it's pagination. Not work now
        Observable<Response<NewsDetailResponse>> getNewsDetail(@Query("id") int id);
        @GET("/rest/ticker/select")
            // TODO: startBlock - it's pagination. Not work now
        Observable<Response<MarketTickerResponse>> ticker(@Query("exchangeName") String exchangeNam);
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
