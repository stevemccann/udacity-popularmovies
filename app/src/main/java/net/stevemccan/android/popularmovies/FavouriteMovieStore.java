package net.stevemccan.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to store favourite movies as an ArrayList in Android SharedPreferences
 * Class structure source from:
 *  http://blog.nkdroidsolutions.com/arraylist-in-sharedpreferences/
 */

public class FavouriteMovieStore {
    public static final String PREFS_NAME = "favourite_movies";
    public static final String FAVORITES = "favourite";

    public FavouriteMovieStore() {
        super();
    }

    // Method to store ArrayList as JSON
    public void storeFavorites(Context context, List favorites) {
        SharedPreferences settings;
        Editor editor;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);
        editor.putString(FAVORITES, jsonFavorites);
        editor.commit();
    }

    // Build ArrayList from JSON string
    public ArrayList loadFavorites(Context context) {
        SharedPreferences settings;
        List favorites;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (settings.contains(FAVORITES)) {
            String jsonFavorites = settings.getString(FAVORITES, null);
            Gson gson = new Gson();
            MovieResult[] favoriteItems = gson.fromJson(jsonFavorites,MovieResult[].class);
            favorites = Arrays.asList(favoriteItems);
            favorites = new ArrayList(favorites);
        } else {
            return null;
        }
        return (ArrayList) favorites;
    }

    public void addFavorite(Context context, MovieResult movieResult) {
        List favorites = loadFavorites(context);
        if (favorites == null) {
            favorites = new ArrayList();
        }
        favorites.add(movieResult);
        storeFavorites(context, favorites);
    }

    public void removeFavorite(Context context, MovieResult movieResult) {
        ArrayList favorites = loadFavorites(context);
        if (favorites != null) {
            favorites.remove(movieResult);
            storeFavorites(context, favorites);
        }
    }
}
