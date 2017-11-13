package uk.me.desiderio.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import uk.me.desiderio.popularmovies.data.Movie;

public class DetailsActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE = "extra_movie";

    private TextView titleTextView;
    private TextView dateTextView;
    private TextView durationTextView;
    private TextView voteTextView;
    private TextView synopsisTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        Movie movie = (Movie) intent.getSerializableExtra(EXTRA_MOVIE);

        titleTextView = findViewById(R.id.titleTextView);
        dateTextView = findViewById(R.id.dateTextView);
        durationTextView = findViewById(R.id.durationTextView);
        voteTextView = findViewById(R.id.voteTextView);
        synopsisTextView = findViewById(R.id.synopsisTextView);

        titleTextView.setText(movie.getTitle());
        dateTextView.setText(movie.getDate());
        durationTextView.setText(String.valueOf(movie.getDuration()));
        voteTextView.setText(getVoteAverageString(movie.getVoteAverage()));
        synopsisTextView.setText(movie.getSynopsis());

    }

    private String getVoteAverageString(int vote) {
        return String.valueOf(vote);
    }

}
