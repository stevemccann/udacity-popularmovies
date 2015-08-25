package net.stevemccan.android.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Steven on 2015-08-24.
 */
public class PosterAdapter extends ArrayAdapter<MovieResult> {

    private static final String LOG_TAG = PosterAdapter.class.getSimpleName();
    Context context;

    public PosterAdapter(Activity context, List<MovieResult> movieResults) {
        super(context, R.layout.movie_grid_display, movieResults);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MovieResult movie = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.movie_grid_display, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.poster);


        String url = "http://image.tmdb.org/t/p/" + "w185" +  movie.posterPath;
        //Log.v(LOG_TAG, url);

        Picasso.with(context)
                .load(url)
                .into(imageView);

        return convertView;
    }
}
