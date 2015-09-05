package net.stevemccan.android.popularmovies;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


public class MovieDetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        MovieResult movie = getIntent().getExtras().getParcelable(MainActivity.MOVIE_PARCELABLE_KEY);

        ImageView posterImage = (ImageView) findViewById(R.id.detail_movie_poster);

        String url = "http://image.tmdb.org/t/p/" + "w185" +  movie.posterPath;

        Picasso.with(getApplicationContext())
                .load(url)
                .into(posterImage);

        ((TextView) findViewById(R.id.detail_movie_title)).setText(movie.getTitle());
        ((TextView) findViewById(R.id.detail_movie_rating)).setText(movie.getVoteAvg());
        ((TextView) findViewById(R.id.detail_movie_release_date)).setText(movie.getReleaseDate());
        ((TextView) findViewById(R.id.detail_movie_overview)).setText(movie.getOverview());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_detail, menu);
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
}
