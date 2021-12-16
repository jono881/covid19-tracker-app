package au.edu.unsw.infs3634.covid19tracker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CountryDao {

    @Query("SELECT * FROM Country")
    List<Country> getCountries();

    // insert one or more countries as an array
    @Insert
    void insert(Country... country);


    @Query("DELETE FROM Country")
    void deleteAll();

    @Query("SELECT * FROM Country WHERE id = :input")
    Country findByCountryID(String input);

}
