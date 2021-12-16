package au.edu.unsw.infs3634.covid19tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";

    TextView countryLabel;
    TextView newCasesLabel;
    TextView totalCasesLabel;
    TextView newDeathsLabel;
    TextView totalDeathsLabel;
    TextView newRecoveredLabel;
    TextView totalRecoveredLabel;
    ImageButton searchButton;
    ImageView flag;
    CheckBox countryCheckBox;
//    List<Country> countryList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        // don't change above code!!


        countryLabel = findViewById(R.id.country_label);
        newCasesLabel = findViewById(R.id.new_cases);
        totalCasesLabel = findViewById(R.id.total_cases);
        newDeathsLabel = findViewById(R.id.new_deaths);
        totalDeathsLabel = findViewById(R.id.total_deaths);
        newRecoveredLabel = findViewById(R.id.new_recovered);
        totalRecoveredLabel = findViewById(R.id.total_recovered);
        flag = findViewById(R.id.country_flag_detail);

        // calling on getCountry method to show its covid data
         getCountry();

        // receives intent containing message from Main Activity
        Intent intent = getIntent();

        // search button that allows user to do Google search
        String countryNameSearch = intent.getStringExtra("Country Name");
        searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code here executes on main thread after user presses button
                Log.d(TAG,  "onCreate: Starting Launch");
                Uri webpage = Uri.parse("https://www.google.com/search?q=covid+" + countryNameSearch);
                Intent intent1 = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(intent1);
            }
        });


        // Write a message to database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myCountryCode = database.getReference(FirebaseAuth.getInstance().getUid());
        // Read from the database
        myCountryCode.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    public void getCountry(){

        // receives intent containing message from Main Activity
        Intent intent = getIntent();
        String countryID = intent.getStringExtra("Country ID");

        // implement retrofit object to get real-time data from API = Week 8 work
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://api.covid19api.com/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        CovidService service = retrofit.create(CovidService.class);


        CountryDatabase database = Room.databaseBuilder(getApplicationContext(), CountryDatabase.class, "country-database")
                .build();
        // Week 9 tute
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Country country = database.countryDao().findByCountryID(countryID);


                String countryName = country.getCountry();
                int newConfirmed = country.getNewConfirmed();
                int totalConfirmed = country.getTotalConfirmed();
                int newDeaths = country.getNewDeaths();
                int totalDeaths = country.getTotalDeaths();
                int newRecovered = country.getNewRecovered();
                int totalRecovered = country.getTotalRecovered();


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(DetailActivity.this)
                                .load("https://flagcdn.com/96x72/" + country.getCountryCode().toLowerCase() + ".png")
                                .fitCenter()
                                .into(flag);
                    }
                });

                // set the text for the TextView widgets
                countryLabel.setText(countryName);
                newCasesLabel.setText(String.valueOf(newConfirmed));
                totalCasesLabel.setText(String.valueOf(totalConfirmed));
                newDeathsLabel.setText(String.valueOf(newDeaths));
                totalDeathsLabel.setText(String.valueOf(totalDeaths));
                newRecoveredLabel.setText(String.valueOf(newRecovered));
                totalRecoveredLabel.setText(String.valueOf(totalRecovered));

                // set a new message in intent, can be opened by onCreate method
                intent.putExtra("Country Name", country.getCountry());

                countryCheckBox = findViewById(R.id.default_country_box);

                // Enable users to get their home country
                FirebaseDatabase database1 = FirebaseDatabase.getInstance();

                // check if checkbox is clicked
                DatabaseReference myRef = database1.getReference(FirebaseAuth.getInstance().getUid());
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String value = (String) snapshot.getValue();
                        if(value != null && value.equals(country.getCountryCode())){
                            countryCheckBox.setChecked(true);
                        }
                        else{
                            countryCheckBox.setChecked(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // if checkbox clicked, assign user's country code
                countryCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        DatabaseReference myRef = database1.getReference(FirebaseAuth.getInstance().getUid());
                        if(b){
                            myRef.setValue(country.getCountryCode());
                        }
                        else{
                            myRef.setValue("");
                        }
                    }
                });

            }
        });



        // choose either two to record a call + response objects --> Week 8
//        Call<Response> responseCall = service.getResponse();
//        responseCall.enqueue(new Callback<Response>() {
//            @Override
//            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
//                Log.d(TAG, "API call successful");
//                List<Country> countryList = response.body().getCountries();
//
//
//                // iterate through all the country objects in Array List until its countryID matches with specified one
//                for (Country country: countryList){
//                    if(countryID.equals(country.getId())){
//                        String countryName = country.getCountry();
//                        int newConfirmed = country.getNewConfirmed();
//                        int totalConfirmed = country.getTotalConfirmed();
//                        int newDeaths = country.getNewDeaths();
//                        int totalDeaths = country.getTotalDeaths();
//                        int newRecovered = country.getNewRecovered();
//                        int totalRecovered = country.getTotalRecovered();
//
//                        Glide.with(DetailActivity.this)
//                                .load("https://flagcdn.com/96x72/" + country.getCountryCode().toLowerCase() + ".png")
//                                .fitCenter()
//                                .into(flag);
//
//
//                        // set the text for the TextView widgets
//                        countryLabel.setText(countryName);
//                        newCasesLabel.setText(String.valueOf(newConfirmed));
//                        totalCasesLabel.setText(String.valueOf(totalConfirmed));
//                        newDeathsLabel.setText(String.valueOf(newDeaths));
//                        totalDeathsLabel.setText(String.valueOf(totalDeaths));
//                        newRecoveredLabel.setText(String.valueOf(newRecovered));
//                        totalRecoveredLabel.setText(String.valueOf(totalRecovered));
//
//                        // set a new message in intent, can be opened by onCreate method
//                        intent.putExtra("Country Name",countryName);
//                    }
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<Response> call, Throwable t) {
//                Log.d(TAG, "API call fails");
//            }
//        });


        // accessing list of all the countries stored in Country class from JSON data --> Week 7
//        Gson gson = new Gson();
//        Response countries = gson.fromJson(Response.json, Response.class);
//        List<Country> countryList = countries.getCountries();


    }

}