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
    CountrySpinnerAdapter spinnerCountryAdapter;
    ArrayAdapter<String> spinnerMethodsAdapter;


    ActivityMainBinding boundView;
    Double inputValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boundView = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initDependencyInjections();
        initViewsAndLists();
        callAPi();
    }

    private void initViewsAndLists() {
        //initializing the lists
        rateList = new ArrayList<>();

        //adding a default option for choosing country in country spinner
        Rate defaultOption = new Rate();
        defaultOption.setName(getText(R.string.select_an_option).toString());
        rateList.add(defaultOption);

        //setting the adapter for country list spinner
        spinnerCountryAdapter = new CountrySpinnerAdapter(this, rateList,R.layout.spinner_item_view); //selected item will look like a spinner set from XML
        boundView.spinnerCountry.setAdapter(spinnerCountryAdapter);

        //setting the adapter for method list
        spinnerMethodsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>()); //selected item will look like a spinner set from XML
        spinnerMethodsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        boundView.spinnerCalculationMethod.setAdapter(spinnerMethodsAdapter);

        //setting callbacks/listeners when a country is selected to change the contents of methods spinner
        boundView.spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int selectedIndex, long id) {
                clearTheOutput();
                //i.e the default option is not selected
                if(selectedIndex >0) updateMethodsSpinner(selectedIndex);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}

        });

        //setting callbacks/listeners when a method is selected
        boundView.spinnerCalculationMethod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clearTheOutput();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //calculate button listener
        boundView.btnCalculate.setOnClickListener(view -> {
            try {
                if(TextUtils.isEmpty(boundView.editTextInputCurrency.getText())){
                    Toast.makeText(this,getText(R.string.input_value_is_required), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (boundView.spinnerCountry.getSelectedItemPosition() == 0){
                    Toast.makeText(this,getText(R.string.please_select_country_first), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (boundView.spinnerCalculationMethod.getSelectedItemPosition() == 0){
                    Toast.makeText(this,getText(R.string.please_select_a_method), Toast.LENGTH_SHORT).show();
                    return;
                }
                inputValue = Double.parseDouble(boundView.editTextInputCurrency.getText().toString());
                JSONObject validTaxRates = ((Rate)boundView.spinnerCountry.getSelectedItem()).getPeriods().get(0).getRateTypes().getValidTaxRatesAsJSON();
                Double selectedTaxValue = validTaxRates.optDouble(boundView.spinnerCalculationMethod.getSelectedItem().toString());
                Double totalValue = inputValue + selectedTaxValue;
                boundView.tvConvertedValue.setText(totalValue.toString());

            }catch (NumberFormatException e){

            }
        });
    }

    private void updateMethodsSpinner(int selectedIndex) {
        Rate selectedRate = rateList.get(selectedIndex);
        //get associated methods for this country
        ArrayList<String> methodsList = new ArrayList<>(selectedRate.getPeriods().get(0).getRateTypes().getValidTaxtypes());
        methodsList.add(0,getText(R.string.select_an_option).toString());
        //update the methods spinner
        spinnerMethodsAdapter.clear();
        spinnerMethodsAdapter.addAll(methodsList);
    }

    private void enableDisableButtons(boolean willEnable){
            boundView.btnCalculate.setEnabled(willEnable);
            boundView.spinnerCalculationMethod.setEnabled(willEnable);
            boundView.spinnerCountry.setEnabled(willEnable);
    }

    private void callAPi() {
        enableDisableButtons(false);
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
        if ( apiResponse != null && apiResponse.getRates() != null )
            rateList.addAll(apiResponse.getRates());

        else Toast.makeText(this, getText(R.string.cannot_get_data), Toast.LENGTH_LONG).show();
    }


    private void onErrorListener(Throwable t) {
        boundView.layoutProgressBar.setVisibility(View.GONE);
        t.printStackTrace();

        Toast.makeText(this, getText(R.string.internal_error_occurred),
                Toast.LENGTH_LONG).show();
    }

    private void onCompleteListener() {
        boundView.layoutProgressBar.setVisibility(View.GONE);
        updateView();
    }

    private void updateView() {
        enableDisableButtons(true);
        updateCountrySpinner();
    }

    private void updateCountrySpinner() {
        //update the country chooser spinner
//        spinnerCountryAdapter.clear();
//        spinnerCountryAdapter.addAll(rateList);
    }

    private void clearTheOutput() {
        boundView.tvConvertedValue.setText("");
    }

}
