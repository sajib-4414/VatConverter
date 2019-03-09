package com.example.arefin.currencyvatconverter.network;

import com.example.arefin.currencyvatconverter.models.ApiResponse;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface APIService {

    //get the vat rates, this requires calling no special path
    @GET("/")
    Observable<ApiResponse> getAllRates();
}
