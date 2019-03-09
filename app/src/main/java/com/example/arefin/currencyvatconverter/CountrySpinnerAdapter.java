package com.example.arefin.currencyvatconverter;


import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.arefin.currencyvatconverter.models.Rate;

import java.util.List;

public class CountrySpinnerAdapter extends ArrayAdapter<String> {

    private final LayoutInflater mInflater;
    private final List<Rate> items;
    private final int mResource;

    public CountrySpinnerAdapter(@NonNull Context context, @NonNull List objects,@LayoutRes int resource) {
        super(context, resource, 0, objects);

        mInflater = LayoutInflater.from(context);
        items = objects;
        mResource = resource;
    }
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent){
        final View view = mInflater.inflate(mResource, parent, false);

        TextView text = view.findViewById(R.id.textValue);
        Rate rate = items.get(position);
        text.setText(rate.getName());
        return view;
    }
}