package au.edu.unsw.infs3634.covid19tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    private CountryAdapter countryAdapter;
    private List<Country> countries;
    private RecyclerView recyclerView;

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // don't change above code!


        // store and show item rows of countries
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);


        CountryAdapter.ClickListener listener = new CountryAdapter.ClickListener() {
            // matches countryID to the country in the array list to get its corresponding country code
            @Override
            public void onCountryClick(View view, String countryID) {
//                final Country country = countries.get(countryID);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
//                intent.putExtra("Country Code",country.getCountryCode());
                intent.putExtra("Country ID", countryID);
                startActivity(intent);
            }
        };

        countryAdapter = new CountryAdapter(new ArrayList<Country>(), listener);


        // implement retrofit object to get real-time data from API = Week 8 work
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.covid19api.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        CovidService service = retrofit.create(CovidService.class);


        // Week 9 content
        CountryDatabase database = Room.databaseBuilder(getApplicationContext(), CountryDatabase.class, "country-database")
                .build();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {

                countryAdapter.setCountries(database.countryDao().getCountries());
            }
        });

        // choose either two to record a call + response objects
        Call<Response> responseCall = service.getResponse();
        responseCall.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                Log.d(TAG, "API call successful");
                countries = response.body().getCountries();
                Log.d(TAG, Integer.toString(countries.size()));


                // Week 9 tute
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {

                        database.countryDao().deleteAll();
                        database.countryDao().insert(countries.toArray(new Country[0]));
                    }
                });
                countryAdapter.setCountries(countries);
                recyclerView.setAdapter(countryAdapter);

                FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database1.getReference(FirebaseAuth.getInstance().getUid());
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String value = (String) snapshot.getValue();
                        if(value != null){
                            Log.d(TAG, "Null is checked");
                            for (Country home: countries){
                                if(value.equals(home.getCountryCode())){
                                    Log.d(TAG, "Toast about to launch");
                                    Toast.makeText(MainActivity.this, home.getNewConfirmed() + " new cases in " + home.getCountry(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.d(TAG, "API call fails");
            }
        });

        // new code for json data to be stored as a list in the Response class = Week 7 code
//        Gson gson = new Gson();
//        Response countryList = gson.fromJson(Response.json, Response.class);
//        countries = countryList.getCountries();




    }

    @Override
    // instantiate new menu_main xml --> part 2 after recycler view
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                countryAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                countryAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    // part 2 after recycler view
    // associating numbers with the menu items in order from onCreateOptionsMenu method
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.sort_reset:
                countryAdapter.sort(0);
                return true;
            case R.id.sort_new_cases:
                // sort by new cases
                countryAdapter.sort(1);
                return true;
            case R.id.sort_total_cases:
                // sort by total cases
                countryAdapter.sort(2);
                return true;
            case R.id.sort_new_cases_dec:
                countryAdapter.sort(3);
                return true;
            case R.id.sort_total_cases_dec:
                countryAdapter.sort(4);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}