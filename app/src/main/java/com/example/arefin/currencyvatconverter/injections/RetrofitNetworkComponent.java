package com.example.arefin.currencyvatconverter.injections;

import com.example.arefin.currencyvatconverter.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

//Declaring what modules will be using the component
@Component(modules = {RetrofitNetworkModule.class})
@Singleton
public interface RetrofitNetworkComponent {
    void inject(MainActivity mainActivity);
}