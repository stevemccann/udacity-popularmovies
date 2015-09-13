package net.stevemccan.android.popularmovies;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

/**
 * Created by Steven on 2015-09-13.
 */
public class MovieDetailFragment extends Fragment {

    private MovieResult mMovieResult;
    private CheckBox mStarFavourite;
    private Toast mStartFavouriteToast;

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

        mStarFavourite = (CheckBox) rootView.findViewById(R.id.star);
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
                } else {
                    text = getResources().getString(R.string.favourite_removed).toString();
                }
                int duration = Toast.LENGTH_SHORT;
                mStartFavouriteToast = Toast.makeText(context, text, duration);
                mStartFavouriteToast.show();
            }
        });

        return rootView;
    }
}
