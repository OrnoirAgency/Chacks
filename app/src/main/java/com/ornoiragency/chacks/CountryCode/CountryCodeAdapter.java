package com.ornoiragency.chacks.CountryCode;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.ornoiragency.chacks.R;
import com.ornoiragency.chacks.Recycler.FastScrollRecyclerViewInterface;

import java.util.HashMap;
import java.util.List;

public class CountryCodeAdapter  extends RecyclerView.Adapter<CountryCodeAdapter.CountryCodeViewHolder> implements FastScrollRecyclerViewInterface {

    private List<Country> mCountries;
    private Callback mCallback;
    private HashMap<String, Integer> mMapIndex;

    public interface Callback {
        void onItemCountrySelected(Country country);
    }

    public CountryCodeAdapter(List<Country> countries, Callback callback, HashMap<String, Integer> mapIndex) {
        this.mCountries = countries;
        this.mCallback = callback;
        mMapIndex = mapIndex;
    }

    @Override
    public CountryCodeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View rootView = inflater.inflate(R.layout.item_country, viewGroup, false);
        return new CountryCodeViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(CountryCodeViewHolder viewHolder, final int i) {
        final int position = viewHolder.getAdapterPosition();
        viewHolder.setCountry(mCountries.get(position));
        viewHolder.rlyMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onItemCountrySelected(mCountries.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCountries.size();
    }

    class CountryCodeViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout rlyMain;
        TextView tvName, tvCode;
        ImageView imvFlag;
        LinearLayout llyFlagHolder;
        View viewDivider;

        CountryCodeViewHolder(View itemView) {
            super(itemView);
            rlyMain = (RelativeLayout) itemView;
            tvName = (TextView) rlyMain.findViewById(R.id.country_name_tv);
            tvCode = (TextView) rlyMain.findViewById(R.id.code_tv);
            imvFlag = (ImageView) rlyMain.findViewById(R.id.flag_imv);
            llyFlagHolder = (LinearLayout) rlyMain.findViewById(R.id.flag_holder_lly);
            viewDivider = rlyMain.findViewById(R.id.preference_divider_view);
        }

        private void setCountry(Country country) {
            if (country != null) {
                viewDivider.setVisibility(View.GONE);
                tvName.setVisibility(View.VISIBLE);
                tvCode.setVisibility(View.VISIBLE);
                llyFlagHolder.setVisibility(View.VISIBLE);
                String countryNameAndCode = tvName.getContext()
                        .getString(R.string.country_name_and_code, country.getName(),
                                country.getIso().toUpperCase());
                tvName.setText(countryNameAndCode);
                tvCode.setText(
                        tvCode.getContext().getString(R.string.phone_code, country.getPhoneCode()));
                imvFlag.setImageResource(CountryUtils.getFlagDrawableResId(country.getIso()));

            } else {
                viewDivider.setVisibility(View.VISIBLE);
                tvName.setVisibility(View.GONE);
                tvCode.setVisibility(View.GONE);
                llyFlagHolder.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public HashMap<String, Integer> getMapIndex() {
        return this.mMapIndex;
    }

}
