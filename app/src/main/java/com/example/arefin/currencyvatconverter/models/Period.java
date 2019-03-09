package com.example.arefin.currencyvatconverter.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Period {

    @SerializedName("effective_from")
    @Expose
    private String effectiveFrom;

    @SerializedName("rates")
    @Expose
    private RateTypes rateTypes;

    public String getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(String effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public RateTypes getRateTypes() {
        return rateTypes;
    }

    public void setRateTypes(RateTypes rateTypes) {
        this.rateTypes = rateTypes;
    }

}
