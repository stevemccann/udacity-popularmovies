package net.stevemccan.android.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity{

    static public final String MOVIE_PARCELABLE_KEY = "movie_result";
    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private PosterAdapter mPosterAdapter;

    private final String MOVIES_JSON_RESULT_KEY = "movies_json_result";
    String moviesJsonResult = null;
    String lastUsedMovieSortOrder = null;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(MOVIES_JSON_RESULT_KEY, moviesJsonResult);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sortOrderSetting = getSortOrderSetting();

        // if the current settings for sort order do not match the previous settings,
        // re-download movie list with the new setting. This will be called when
        // activity is resuming from the user making changes in the settings.
        if(!sortOrderSetting.equals(lastUsedMovieSortOrder)) {
            Log.v(LOG_TAG, "Sort order change detected, fetching new list for setting: " + sortOrderSetting);
            FetchMoviesTask fetchMoviez = new FetchMoviesTask();
            fetchMoviez.execute(sortOrderSetting);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<MovieResult> movieResults = new ArrayList<>();
        mPosterAdapter = new PosterAdapter(
                this,
                movieResults
        );
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(mPosterAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                MovieResult result = (MovieResult) parent.getAdapter().getItem(position);
                Intent launchIntent = new Intent(getApplicationContext(), MovieDetailActivity.class);
                launchIntent.putExtra(MOVIE_PARCELABLE_KEY, result);
                startActivity(launchIntent);
            }
        });

        String sortOrderSetting = getSortOrderSetting();

        if(savedInstanceState != null) {
            // Load data stored from previous activity and load it to the adapter
            moviesJsonResult = savedInstanceState.getString(MOVIES_JSON_RESULT_KEY, null);
            loadMoviesAdapterFromJson(moviesJsonResult);
        } else {
            Log.v(LOG_TAG, "fetching new list of movies for sort order: " + sortOrderSetting);
            FetchMoviesTask fetchMoviez = new FetchMoviesTask();
            fetchMoviez.execute(sortOrderSetting);
        }


    }

    private void loadMoviesAdapterFromJson(String moviesJsonResult) {

        ArrayList<MovieResult> movieResults = null;
        try {
            movieResults = getMovieObjectsFromJSON(moviesJsonResult);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        if (movieResults != null) {
            mPosterAdapter.clear();
            for(MovieResult movie : movieResults) {
                mPosterAdapter.add(movie);
            }
        }

    }

    private String getSortOrderSetting() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //SharedPreferences prefs = this.getSharedPreferences("general_settings", Context.MODE_PRIVATE);
        return prefs.getString(
                getResources().getString(R.string.pref_movie_sort_order_key),
                getResources().getString(R.string.pref_movie_sort_order_default));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent launchIntent = new Intent(this, SettingsActivity.class);
            startActivity(launchIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private ArrayList getMovieObjectsFromJSON(String moviesJsonStr) throws JSONException{

        final String TMD_RESULT_ARRAY = "results";

        JSONObject moviesJsonObj = new JSONObject(moviesJsonStr);
        JSONArray moviesArray = moviesJsonObj.getJSONArray(TMD_RESULT_ARRAY);
        ArrayList<MovieResult> resultSet = new ArrayList<>();
        for (int i = 0; i < moviesArray.length(); i++) {
            JSONObject movieResult = moviesArray.getJSONObject(i);
            resultSet.add(new MovieResult(movieResult));
        }
        return resultSet;
    }


    private class FetchMoviesTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected String doInBackground(String... params) {

            // TODO: return null if params empty

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String API_KEY_PARAM = "api_key";
                final String SORT_BY_PARAM = "sort_by";

                ApiStore apiStore = new ApiStore();
                // TODO: add exception handling message when API is returned blank
                String apiKey = apiStore.getMOVIE_API_KEY();

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, params[0])
                        .appendQueryParameter(API_KEY_PARAM, apiKey)
                        .build();

                URL url = new URL(builtUri.toString());

                //Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to TheMovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                // update the movie order setting used for reference later (ie during on resume)
                lastUsedMovieSortOrder = params[0];
                moviesJsonResult = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return moviesJsonResult;
        }

        @Override
        protected void onPostExecute(String movieResults) {
            super.onPostExecute(movieResults);
            loadMoviesAdapterFromJson(movieResults);
        }
    }
}
