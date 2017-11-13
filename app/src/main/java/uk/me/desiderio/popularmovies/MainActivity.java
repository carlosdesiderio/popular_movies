package uk.me.desiderio.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = (TextView) findViewById(R.id.text_hello_world);
        textView.setText("Hello ");
        textView.append(BuildConfig.MOVIE_DB_API_TOKEN);
    }
}
