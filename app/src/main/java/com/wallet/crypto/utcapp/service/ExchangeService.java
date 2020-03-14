package com.wallet.crypto.utcapp.service;

import com.google.gson.Gson;
import com.wallet.crypto.utcapp.entity.Exchange;
import com.wallet.crypto.utcapp.entity.NewsItem;
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

public class ExchangeService implements ExchangeServiceType{
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final EthereumNetworkRepositoryType networkRepository;
    private ExchangeApiClient newsApiClient;


    public ExchangeService(
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
                .create(ExchangeApiClient.class);
    }


    private static @NonNull
    <T> ApiErrorOperator<T> apiError(Gson gson) {
        return new ApiErrorOperator<>(gson);
    }

    @Override
    public Observable<Exchange[]> fetchExchanges() {
       /* Exchange s=new Exchange();
        s.name="变化";
        s.logoUrl="";
        s.url="www.baidu.com";
        Exchange[] exchanges={s,s,s,s,s,s,s,s};*/
        return newsApiClient
                .fetchNewsItems(networkRepository.getDefaultNetwork().symbol)
                .lift(apiError(gson))
                //   .map(r -> r.news)
                .map(r -> r.trades)
               // .map(r->exchanges)
                .subscribeOn(Schedulers.io());
    }



    private interface ExchangeApiClient {
        @GET("/trades?")
            // TODO: startBlock - it's pagination. Not work now
        Observable<Response<ExchangeResponse>> fetchNewsItems(@Query("network_symbol") String symbols);
    }

    private final static class ExchangeResponse {
        Exchange[] trades;
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
