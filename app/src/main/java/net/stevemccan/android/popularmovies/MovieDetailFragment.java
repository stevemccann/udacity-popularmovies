package net.stevemccan.android.popularmovies;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Steven on 2015-09-13.
 */
public class MovieDetailFragment extends Fragment {

    private final String LOG_TAG = MovieDetailFragment.class.getSimpleName();

    private MovieResult mMovieResult;
    private CheckBox mStarFavourite;
    private Toast mStartFavouriteToast;
    private String mMovieTrailerJsonResults;
    private LinearLayout mTrailersLinerLayoutContainer;
    private String mReviewsJsonResult;
    private LinearLayout mReviewsLinearLayoutContainer;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mMovieResult = arguments.getParcelable(MovieResult.MOVIE_PARCELABLE_KEY);
        } else {
            // else pull data from Intent (when launched on a phone
            // TODO: Find a way to get intent data from getArguments
            mMovieResult = getActivity().getIntent().getExtras().getParcelable(MovieResult.MOVIE_PARCELABLE_KEY);
        }

        getActivity().setTitle(mMovieResult.getTitle());

        // TODO: check if these vars should be detached when the fragment is disconnected,
        // to prevent memory leaks
        ImageView posterImage = (ImageView) rootView.findViewById(R.id.detail_movie_poster);

        String url = "http://image.tmdb.org/t/p/" + "w500" +  mMovieResult.posterPath;

        Picasso.with(getActivity())
                .load(url)
                .into(posterImage);

        Float movieRating = (Float.parseFloat(mMovieResult.getVoteAvg()) / 2);
        String releaseDateText =
                getResources().getString(R.string.movie_detail_release_label) + ": " +
                        mMovieResult.getReleaseDate();

        ((TextView) rootView.findViewById(R.id.detail_movie_title)).setText(mMovieResult.getTitle());
        ((RatingBar) rootView.findViewById((R.id.detail_movie_ratingBar))).setRating(movieRating);
        ((TextView) rootView.findViewById(R.id.detail_movie_release_date)).setText(releaseDateText);
        ((TextView) rootView.findViewById(R.id.detail_movie_overview)).setText(mMovieResult.getOverview());

        final FavouriteMovieStore favouriteMovieStore = new FavouriteMovieStore();

        mStarFavourite = (CheckBox) rootView.findViewById(R.id.star);
        //mStarFavourite.setChecked(mMovieResult.);
        mStarFavourite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // doing a cancel on the Toast so it can appear immediately if the prevous toast
                // is still on the screen.
                if (mStartFavouriteToast != null) mStartFavouriteToast.cancel();
                Context context = getActivity();
                CharSequence text = null;
                if (isChecked) {
                    text = getResources().getString(R.string.favourite_added).toString();
                    favouriteMovieStore.addFavorite(getActivity(), mMovieResult);
                } else {
                    text = getResources().getString(R.string.favourite_removed).toString();
                    favouriteMovieStore.removeFavorite(getActivity(), mMovieResult);
                }
                int duration = Toast.LENGTH_SHORT;
                mStartFavouriteToast = Toast.makeText(context, text, duration);
                mStartFavouriteToast.show();
            }
        });

        // Start an AsyncTask to fetch and load the trailers for the movie being viewed
        mTrailersLinerLayoutContainer =
                (LinearLayout) rootView.findViewById(R.id.movie_detail_trailers);
        FetchTrailersTask fetchTrailersTask = new FetchTrailersTask();
        fetchTrailersTask.execute(mMovieResult.getMovieId());

        mReviewsLinearLayoutContainer =
                (LinearLayout) rootView.findViewById(R.id.movie_detail_reviews);
        FetchReviewsTask fetchReviewsTask = new FetchReviewsTask();
        fetchReviewsTask.execute(mMovieResult.getMovieId());

        return rootView;
    }

    private class FetchTrailersTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            // TODO: return null if params empty

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                // TODO: Make these variables global (some are also used in main activity fragment)
                final String TMD_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String TRAILERS_PATH = "videos";
                final String API_KEY_PARAM = "api_key";

                ApiStore apiStore = new ApiStore();
                String apiKey = apiStore.getMOVIE_API_KEY();

                // TODO: build this URL dynamically
                final String FETCH_URL = TMD_BASE_URL + params[0] + "/" + TRAILERS_PATH + "?" +
                        API_KEY_PARAM + "=" + apiKey;

                Uri builtUri = Uri.parse(FETCH_URL);
                URL url = new URL(builtUri.toString());

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
                mMovieTrailerJsonResults = buffer.toString();

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
            return mMovieTrailerJsonResults;
        }

        @Override
        protected void onPostExecute(String trailerJsonResults) {
            // load results into view
            super.onPostExecute(trailerJsonResults);
            loadMovieTrailersFromJson(trailerJsonResults);
        }
    }

    private Void loadMovieTrailersFromJson(String jsonResults) {
        try {
            // get the trailers from the list
            JSONObject trailersJsonObj = new JSONObject(jsonResults);
            JSONArray trailersJsonArray = trailersJsonObj.getJSONArray("results");

            for (int i = 0; i < trailersJsonArray.length(); i++) {
                //build a View with the JSON data...
                JSONObject trailerResult = trailersJsonArray.getJSONObject(i);

                TextView trailerNameTv = new TextView(getActivity());
                trailerNameTv.setText(trailerResult.getString("name"));

                final String youTubeVideoId = trailerResult.getString("key");

                mTrailersLinerLayoutContainer.addView(trailerNameTv);

                trailerNameTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // launching intent per: http://stackoverflow.com/questions/574195/android-youtube-app-play-video-intent
                        try{
                            Intent intent = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("vnd.youtube:" + youTubeVideoId));
                            startActivity(intent);
                        }catch (ActivityNotFoundException e){
                            Intent intent=new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://www.youtube.com/watch?v=" + youTubeVideoId));
                            startActivity(intent);
                        }
                    }
                });
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    private class FetchReviewsTask extends  AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            // TODO: return null if params empty

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                // TODO: Make these variables global (some are also used in main activity fragment)
                final String TMD_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String REVIEWS_PATH = "reviews";
                final String API_KEY_PARAM = "api_key";

                ApiStore apiStore = new ApiStore();
                String apiKey = apiStore.getMOVIE_API_KEY();

                // TODO: build this URL dynamically
                final String FETCH_URL = TMD_BASE_URL + params[0] + "/" + REVIEWS_PATH + "?" +
                        API_KEY_PARAM + "=" + apiKey;

                Uri builtUri = Uri.parse(FETCH_URL);
                URL url = new URL(builtUri.toString());

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
                mReviewsJsonResult = buffer.toString();

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
            return mReviewsJsonResult;
        }

        @Override
        protected void onPostExecute(String reviewsJsonResults) {
            // load results into view
            super.onPostExecute(reviewsJsonResults);
            loadReviewsFromJson(reviewsJsonResults);
        }
    }

    private Void loadReviewsFromJson(String jsonResults) {
        try {
            // get the reviews from the list
            JSONObject reviewsJsonObj = new JSONObject(jsonResults);
            JSONArray reviewsJsonArray = reviewsJsonObj.getJSONArray("results");

            for (int i = 0; i < reviewsJsonArray.length(); i++) {
                //build a View with the JSON data...
                JSONObject reviewResult = reviewsJsonArray.getJSONObject(i);

                TextView reviewTv = new TextView(getActivity());
                reviewTv.setText(
                        reviewResult.getString("author") + ": \n" +
                                reviewResult.getString("content") + "\n\n"
                );
                mReviewsLinearLayoutContainer.addView(reviewTv);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }
}
