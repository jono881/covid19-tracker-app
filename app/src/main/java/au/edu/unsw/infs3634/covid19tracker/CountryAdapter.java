package au.edu.unsw.infs3634.covid19tracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.MyViewHolder> implements Filterable{

    private List<Country> mCountries;
    private List<Country> mCountriesFiltered;
    private ClickListener listener;

    // initialise the dataset of Adapter
    CountryAdapter(List<Country> countries, ClickListener listener){
        this.mCountries = countries;
        this.mCountriesFiltered = countries;
        this.listener = listener;
    }

    public void setCountries(List<Country> data){
        mCountriesFiltered.clear();
        mCountriesFiltered.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    // used to take in string from search
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if(charString.isEmpty()){
                    mCountriesFiltered = mCountries;
                }
                else{
                    ArrayList<Country> filteredList = new ArrayList<>();
                    for(Country country: mCountries){
                        if(country.getCountry().toLowerCase().contains(charString.toLowerCase())){
                            filteredList.add(country);
                        }
                    }
                    mCountriesFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mCountriesFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mCountriesFiltered = (ArrayList<Country>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    // Allows click events to be caught
    public interface ClickListener {
//        void onCountryClick(View view, int countryID);

        void onCountryClick(View view, String countryID);

    }

    // Inflate the row layout from xml when needed (just the view, no data)
    @NonNull
    @Override
    public CountryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // connect to item_row.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
        return new MyViewHolder(view, listener);

    }


    @Override
    public void onBindViewHolder(@NonNull CountryAdapter.MyViewHolder holder, int position) {
        final Country country = mCountriesFiltered.get(position);

        Glide.with(holder.itemView)
                .load("https://flagcdn.com/96x72/" + country.getCountryCode().toLowerCase() + ".png")
                .fitCenter()
                .into(holder.flag);

        holder.countryName.setText(country.getCountry());
        holder.totalCases.setText(String.valueOf(country.getTotalConfirmed()));
        holder.newCases.setText("+" + country.getNewConfirmed().toString());

        // change to country ID
//        holder.itemView.setTag(countryID);
        holder.itemView.setTag(country.getId());

    }

    @Override
    public int getItemCount() {
        return mCountriesFiltered.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView countryName, totalCases, newCases;
        private ImageView flag;
        private ClickListener listener;

        public MyViewHolder(@NonNull View itemView, ClickListener listener){
            super(itemView);
            this.listener = listener;
            itemView.setOnClickListener(MyViewHolder.this);

            // refer to textView elements on item view xml file
            flag = itemView.findViewById(R.id.country_flag);
            countryName = itemView.findViewById(R.id.country_name);
            totalCases = itemView.findViewById(R.id.total_cases_label);
            newCases = itemView.findViewById(R.id.new_cases_label);
        }

        @Override
        public void onClick(View v){
//            listener.onCountryClick(v, (Integer) v.getTag());
            listener.onCountryClick(v, (String) v.getTag());
        }


    }

    // sort method --> part 2 after recycler view
    public void sort(final int sortMethod){
        if(mCountriesFiltered.size() > 0){
            Collections.sort(mCountriesFiltered, new Comparator<Country>() {
                @Override
                public int compare(Country c1, Country c2) {
                    // sorts by new cases
                    if(sortMethod == 0){
                        return c1.getCountry().compareTo(c2.getCountry());
                    }
                    if(sortMethod == 1 | sortMethod == 3){
                        return c1.getNewConfirmed().compareTo(c2.getNewConfirmed());
                    }
                    // sorts by total cases
                    else if(sortMethod == 2 | sortMethod == 4){
                        return c1.getTotalConfirmed().compareTo(c2.getTotalConfirmed());
                    }
                    return 0;
                }
            });

            if(sortMethod > 2){
                Collections.reverse(mCountriesFiltered);
            }
        }
        notifyDataSetChanged();
    }
}
