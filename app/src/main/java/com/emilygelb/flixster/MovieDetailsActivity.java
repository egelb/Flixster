package com.emilygelb.flixster;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.emilygelb.flixster.models.Movie;
import com.emilygelb.flixster.models.MovieTrailerActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class MovieDetailsActivity extends AppCompatActivity {

    // the movie to display
    Movie movie;

    AsyncHttpClient client;

    String trailerId;

    // the view objects
    TextView tvTitle;
    TextView tvOverview;
    RatingBar rbVoteAverage;
    ImageView ivBackdrop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        // resolve the view objects
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvOverview = (TextView) findViewById(R.id.tvOverview);
        rbVoteAverage = (RatingBar) findViewById(R.id.rbVoteAverage);
        ivBackdrop = (ImageView) findViewById(R.id.ivBackdrop);

        client = new AsyncHttpClient();

        // unwrap the movie passed in via intent, using its simple name as a key
        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        //Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

        getTrailers();

        // set the title and overview
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());

        String imageUrl = "https://image.tmdb.org/t/p/w780" + movie.getBackdropPath();

        // load image using glide
        Glide.with(ivBackdrop.getContext())
                .load(imageUrl)
                .bitmapTransform(new RoundedCornersTransformation(ivBackdrop.getContext(), 15, 0))
                .placeholder(R.drawable.flicks_backdrop_placeholder)
                .error(R.drawable.flicks_backdrop_placeholder)
                .into(ivBackdrop);

        // vote average is 0..10, convert to 0..5 by dividing by 2
        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage = voteAverage > 0 ? voteAverage / 2.0f : voteAverage);
    }

    // get the trailers movies from the API
    private void getTrailers() {
        // create url
        String url = "https://api.themoviedb.org/3/movie/" + movie.getId() +"/videos?api_key=" + getString(R.string.api_key);
        Log.i("url", url);

        client.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // load the results into movie list
                try {
                    JSONArray results = response.getJSONArray("results");
                    trailerId = results.getJSONObject(0).getString("key");
                } catch (JSONException e) {
                    Log.e("Fail", "Failed to parse now playing movies");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e("Fail", "Failed to get data from now playing endpoint");
            }
        });
    }

    public void onImageClick(View view) {
        if (trailerId != null) {
            Intent intent = new Intent(MovieDetailsActivity.this, MovieTrailerActivity.class);
            // serialize the movie using parceler, use its short name as a key
            intent.putExtra("trailerId", trailerId);
            // show the activity
            this.startActivity(intent);
        }
    }

}
