package com.example.arefin.currencyvatconverter.injections;

import com.example.arefin.currencyvatconverter.network.APIService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.arefin.currencyvatconverter.Constants.BASE_URL;


@Module
public class RetrofitNetworkModule {

    @Singleton
    Gson getGsonInstance(){
        return new GsonBuilder()
                .setLenient()
                .create();
    }

    @Singleton
    Retrofit getRetrofitInstance(){
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(getGsonInstance()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    APIService getApiInstance(){
        return getRetrofitInstance().create(APIService.class);
    }

}
