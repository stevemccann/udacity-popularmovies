package net.stevemccan.android.popularmovies;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by Steven on 2015-09-13.
 */
public class MovieDetailFragment extends Fragment {

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

        MovieResult movie = getActivity().getIntent().getExtras().getParcelable(MovieResult.MOVIE_PARCELABLE_KEY);

        getActivity().setTitle(movie.getTitle());

        ImageView posterImage = (ImageView) rootView.findViewById(R.id.detail_movie_poster);

        String url = "http://image.tmdb.org/t/p/" + "w500" +  movie.posterPath;

        Picasso.with(getActivity())
                .load(url)
                .into(posterImage);

        Float movieRating = (Float.parseFloat(movie.getVoteAvg()) / 2);
        String releaseDateText =
                getResources().getString(R.string.movie_detail_release_label) + ": " +
                        movie.getReleaseDate();

        ((TextView) rootView.findViewById(R.id.detail_movie_title)).setText(movie.getTitle());
        ((RatingBar) rootView.findViewById((R.id.detail_movie_ratingBar))).setRating(movieRating);
        ((TextView) rootView.findViewById(R.id.detail_movie_release_date)).setText(releaseDateText);
        ((TextView) rootView.findViewById(R.id.detail_movie_overview)).setText(movie.getOverview());

        return rootView;
    }
}
