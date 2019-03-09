package com.example.arefin.currencyvatconverter;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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
        enableDisableButtons(false);
        callAPi();
    }

    private void enableDisableButtons(boolean willEnable){
            boundView.btnCalculate.setEnabled(willEnable);
            boundView.spinnerCalculationMethod.setEnabled(willEnable);
            boundView.spinnerCountry.setEnabled(willEnable);
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
            countryList.add(0,getString(R.string.select_an_option));
            countryArr = new String[countryList.size()];
            countryArr = countryList.toArray(countryArr);
        }
    }

    private void onErrorListener(Throwable t) {
        boundView.layoutProgressBar.setVisibility(View.GONE);
        t.printStackTrace();

        Toast.makeText(this, getText(R.string.internal_error_occurred),
                Toast.LENGTH_LONG).show();
    }

    private void onCompleteListener() {
        boundView.layoutProgressBar.setVisibility(View.GONE);
        setView();
    }

    private void setView() {
        enableDisableButtons(true);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, countryArr); //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        boundView.spinnerCountry.setAdapter(spinnerArrayAdapter);
        boundView.spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                if(rateList !=null && vatMap !=null){
                    clearTheOutput();
                    if( ! countryArr[position].equals(getText(R.string.select_an_option))){
                        rateTypesOfSelectedCountry = vatMap.get(countryArr[position]);
                        ArrayList<String> methodOptionsList = vatMap.get(countryArr[position]).getValidTaxtypes();
                        methodOptionsList.add(0,getString(R.string.select_an_option));
                        methodsArr = new String[methodOptionsList.size()];
                        methodsArr = methodOptionsList.toArray(methodsArr);
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, methodsArr);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        boundView.spinnerCalculationMethod.setAdapter(spinnerArrayAdapter);
                    }
                    else{
                        ArrayList<String> options = new ArrayList<>();
                        options.add(getString(R.string.select_an_option));
                        String[] emptyArray = new String[1];
                        emptyArray = options.toArray(emptyArray);
                        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, emptyArray);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        boundView.spinnerCalculationMethod.setAdapter(spinnerArrayAdapter);
                    }
                }
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
                if(TextUtils.isEmpty(boundView.editTextInputCurrency.getText())){
                    Toast.makeText(this,getText(R.string.input_value_is_required), Toast.LENGTH_SHORT).show();
                    return;
                }
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
