package net.stevemccan.android.popularmovies;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Steven on 2015-08-23.
 */
public class MovieResult {

    //final String TMD_TITLE = "title";
    final String TMD_POSTER = "poster_path";

    public String posterPath = null;
    //public String title = null;

    public MovieResult (JSONObject movie) throws JSONException {
        this.posterPath = movie.getString(TMD_POSTER);

        Log.v("TTTTTTTTTT", this.posterPath);
    }
}
