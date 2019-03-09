package com.example.arefin.currencyvatconverter;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.arefin.currencyvatconverter.databinding.ActivityMainBinding;
import com.example.arefin.currencyvatconverter.injections.DaggerRetrofitNetworkComponent;
import com.example.arefin.currencyvatconverter.injections.RetrofitNetworkComponent;
import com.example.arefin.currencyvatconverter.injections.RetrofitNetworkModule;
import com.example.arefin.currencyvatconverter.models.ApiResponse;
import com.example.arefin.currencyvatconverter.models.Rate;
import com.example.arefin.currencyvatconverter.models.RateTypes;
import com.example.arefin.currencyvatconverter.network.APIService;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Inject
    APIService apiService;

    //the component which will help dagger injection
    private RetrofitNetworkComponent retrofitNetworkComponent;
    List<Rate> rateList;
    Map<String, RateTypes> vatMap;
    String[] countryArr;


    ActivityMainBinding boundView;
    Double inputValue;
    RateTypes rateTypesOfSelectedCountry;
    private String[] methodsArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boundView = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initDependencyInjections();
        rateList = new ArrayList<>();
        callAPi();
    }

    private void callAPi() {
        Observable<ApiResponse> ObservableResponse = apiService.getAllRates();
        ObservableResponse
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onNextListener,this::onErrorListener, this::onCompleteListener);
    }

    private void initDependencyInjections() {
        //the DaggerSharedPrefComponent name is generated after a rebuild
        retrofitNetworkComponent = DaggerRetrofitNetworkComponent
                .builder()
                //the number of modules declared in the pref module, all can be here to use
                .retrofitNetworkModule(new RetrofitNetworkModule())
                .build();
        retrofitNetworkComponent.inject(this);
    }

    private void onNextListener(ApiResponse apiResponse) {
        if (apiResponse != null && apiResponse.getRates() != null ) {
            rateList.addAll(apiResponse.getRates());
            generateDataMap();
        }
        else
            Toast.makeText(this, "NO RESULTS FOUND", Toast.LENGTH_LONG).show();
    }

    private void generateDataMap() {
        if(rateList.size()>0){
            vatMap = new HashMap<>();
            ArrayList<String> countryList = new ArrayList<>();
            for(Rate rate: rateList){
                vatMap.put(rate.getName(),rate.getPeriods().get(0).getRateTypes());
                countryList.add(rate.getName());
            }
            countryArr = new String[countryList.size()];
            countryArr = countryList.toArray(countryArr);
        }
    }

    private void onErrorListener(Throwable t) {
        boundView.layoutProgressBar.setVisibility(View.GONE);
        t.printStackTrace();

        Toast.makeText(this, "ERROR IN FETCHING API RESPONSE. Try again",
                Toast.LENGTH_LONG).show();
    }

    private void onCompleteListener() {
        boundView.layoutProgressBar.setVisibility(View.GONE);
        setView();
    }

    private void setView() {
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item,
                        countryArr); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        boundView.spinnerCountry.setAdapter(spinnerArrayAdapter);
        boundView.spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                clearTheOutput();
                ArrayList<String> methodOptionsList = new ArrayList<>();
                RateTypes rateTypes = vatMap.get(countryArr[position]);
                rateTypesOfSelectedCountry = rateTypes;
                if(rateTypes.getSuperReduced() != null)
                    methodOptionsList.add("super_reduced");
                if(rateTypes.getReduced() != null)
                    methodOptionsList.add("reduced");
                if(rateTypes.getStandard() != null)
                    methodOptionsList.add("standard");
                if(rateTypes.getReduced1() != null)
                    methodOptionsList.add("reduced1");
                if(rateTypes.getReduced2() != null)
                    methodOptionsList.add("reduced2");
                if(rateTypes.getParking() != null)
                    methodOptionsList.add("parking");

                methodsArr = new String[methodOptionsList.size()];
                methodsArr = methodOptionsList.toArray(methodsArr);
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                        (getApplicationContext(), android.R.layout.simple_spinner_item,
                                methodsArr); //selected item will look like a spinner set from XML
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                        .simple_spinner_dropdown_item);
                boundView.spinnerCalculationMethod.setAdapter(spinnerArrayAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
        boundView.spinnerCalculationMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clearTheOutput();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        boundView.btnCalculate.setOnClickListener(view -> {
            try {
                inputValue = Double.parseDouble(boundView.editTextInputCurrency.getText().toString());
                JSONObject validTaxRates = rateTypesOfSelectedCountry.getValidTaxRatesAsJSON();
                Double selectedTaxValue = validTaxRates.optDouble(boundView.spinnerCalculationMethod.getSelectedItem().toString());
                Double totalValue = inputValue + selectedTaxValue;
                boundView.tvConvertedValue.setText(totalValue.toString());

            }catch (NumberFormatException e){

            }

        });
    }

    private void clearTheOutput() {
        boundView.tvConvertedValue.setText("");
    }

}
