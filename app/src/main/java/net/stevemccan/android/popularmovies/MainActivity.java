package net.stevemccan.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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


public class MainActivity extends ActionBarActivity {

    private PosterAdapter mPosterAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView gridview = (GridView) findViewById(R.id.gridview);

        ArrayList<MovieResult> movieResults = new ArrayList<>();
        //movieResults.clear();
        //movieResults.add(new MovieResult("/tbhdm8UJAb4ViCTsulYFL3lxMCd.jpg"));

        mPosterAdapter = new PosterAdapter(
                this,
                movieResults
        );

        gridview.setAdapter(mPosterAdapter);

        FetchMoviesTask fetchMoviez = new FetchMoviesTask();
        fetchMoviez.execute();

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                MovieResult result = (MovieResult) parent.getAdapter().getItem(position);

                Intent launchIntent = new Intent(getApplicationContext(), MovieDetailActivity.class);

                launchIntent.putExtra("movie_result", result);

                startActivity(launchIntent);

//                Toast.makeText(getApplicationContext(), result.posterPath,
//                        Toast.LENGTH_SHORT).show();
//
//                Log.v("CLICK DATA: ", result.posterPath);
            }
        });
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<MovieResult>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

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

        @Override
        protected ArrayList<MovieResult> doInBackground(String... params) {

            // TODO: return null if params empty


            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String moviesJsonResult = null;


            try {

                final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String API_KEY_PARAM = "api_key";
                final String SORT_BY_PARAM = "sort_by";

                // TODO: pass these values dynamically
                ApiStore apiStore = new ApiStore();
                String apiKey = apiStore.getMOVIE_API_KEY(); // Udacity: Add API key in ApiStore.java
                String sortBy = "popularity.desc";

                Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, sortBy)
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
                moviesJsonResult = buffer.toString();

                //Log.v(LOG_TAG, "MoviesJSON string: " + moviesJsonResult);


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

            try {
                return getMovieObjectsFromJSON(moviesJsonResult);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<MovieResult> movieResults) {
            super.onPostExecute(movieResults);
            if (movieResults != null) {
                mPosterAdapter.clear();
                for(MovieResult movie : movieResults) {
                    mPosterAdapter.add(movie);
                }
                // New data is back from the server.  Hooray!
            }

        }
    }
}
