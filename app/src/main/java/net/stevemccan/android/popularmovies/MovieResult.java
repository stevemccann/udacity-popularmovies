package net.stevemccan.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Steven on 2015-08-23.
 */
public class MovieResult implements Parcelable{

    // key used to parcel and unparcel movie result object through intents
    static public final String MOVIE_PARCELABLE_KEY = "movie_result";

    // Set this number to size of variables so parcelable array is proper size
    private final int PARCELABLE_ARRAY_SIZE = 7;

    final String TMD_POSTER = "poster_path";
    final String TMD_TITLE = "title";
    final String TMD_OVERVIEW = "overview";
    final String TMD_BACKDROP = "backdrop_path";
    final String TMD_VOTE_AVG = "vote_average";
    final String TMD_RELEASE_DATE = "release_date";
    final String TMD_MOVIE_ID = "id";

    public String posterPath;
    private String title;
    private String overview;
    private String backdrop;
    private String voteAvg;
    private String releaseDate;
    private String movieId;

    public MovieResult (JSONObject movie) throws JSONException {
        this.posterPath = movie.getString(TMD_POSTER);
        this.title = movie.getString(TMD_TITLE);
        this.overview = movie.getString(TMD_OVERVIEW);
        this.backdrop = movie.getString(TMD_BACKDROP);
        this.voteAvg = movie.getString(TMD_VOTE_AVG);
        this.releaseDate = movie.getString(TMD_RELEASE_DATE);
        this.movieId = movie.getString(TMD_MOVIE_ID);
    }

    // Getters
    public String getPosterPath() {
        return posterPath;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getBackdrop() {
        return backdrop;
    }

    public String getVoteAvg() {
        return voteAvg;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getMovieId() {
        return movieId;
    }

    // Create object from incoming Parcel
    public MovieResult(Parcel in) {
        String[] data = new String[PARCELABLE_ARRAY_SIZE];

        in.readStringArray(data);

        // pull data from parcel
        this.posterPath = data[0];
        this.title = data[1];
        this.overview = data[2];
        this.backdrop = data[3];
        this.voteAvg = data[4];
        this.releaseDate = data[5];
        this.movieId = data[6];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {
                this.posterPath,
                this.title,
                this.overview,
                this.backdrop,
                this.voteAvg,
                this.releaseDate,
                this.movieId
        });
    }

    public static final Parcelable.Creator<MovieResult> CREATOR =
            new Parcelable.Creator<MovieResult>() {
                public MovieResult createFromParcel(Parcel in) {
                    return new MovieResult(in);
                }

                public MovieResult[] newArray(int size) {
                    return new MovieResult[size];
                }
            };
}
