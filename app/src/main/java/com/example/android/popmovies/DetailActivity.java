package com.example.android.popmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by ADieu on 1/11/2016.
 */
public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
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
        //if the clicked menu item is the settings button launch the
        //setting activity via an intent
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this,SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static final String FORECAST_SHARE_HASHTAG = " #PopMovieApp";

        private ShareActionProvider mShareActionProvider;

        public PlaceholderFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);
/*
            //Find the MenuItem with the ShareAction provider
            MenuItem mItem = menu.findItem(R.id.action_share);

            //Fetch and store ShareActionProvider
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mItem);

            if(mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
*/
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            //Declare intent variable to copy passed Intent reference into
            Intent passedIntent = getActivity().getIntent();
            //Now create a String to put the passed forecast data into
            //String passedMovieData = passedIntent.getStringExtra(passedIntent.EXTRA_TEXT);
            Bundle passedMovieDataBundle = passedIntent.getBundleExtra("movieDataBundle");
            String passedMovieTitle = passedMovieDataBundle.getString("title");
            String passedMovieSynopsis = passedMovieDataBundle.getString("synopsis");
            String passedMovieReleaseDate = passedMovieDataBundle.getString("releaseDate");
            Float passedMovieRating = Float.parseFloat(passedMovieDataBundle.getString("rating"));
            String passedMovieImgURL = passedMovieDataBundle.getString("imgURL");
            Log.v("DetailActivity", "title: "+passedMovieTitle+ " synopsis:" + passedMovieSynopsis);
            /*
            TextView imageTextView = new TextView(getContext());
            imageTextView.setTextSize(16);
            imageTextView.setText("title: "+passedMovieTitle+ " synopsis:" + passedMovieSynopsis);
            container.addView(imageTextView);
            */
            //Populate detail fragment sections with passed data
            TextView titleTextView = (TextView) rootView.findViewById(R.id.detailTitle);
            titleTextView.setText(passedMovieTitle);
            TextView synopsisTextView = (TextView) rootView.findViewById(R.id.detailSynopsis);
            synopsisTextView.setText(passedMovieSynopsis);
            TextView rDateTextView = (TextView) rootView.findViewById(R.id.detailReleaseDate);
            rDateTextView.setText(passedMovieReleaseDate);
            RatingBar ratingRBar = (RatingBar) rootView.findViewById(R.id.detailRatingBar);
            ratingRBar.setRating(passedMovieRating);

            Context context = getContext();
            Picasso.with(context).load(passedMovieImgURL).into( (ImageView)rootView.findViewById(R.id.detailPoster));
            return rootView;
        }

        //setIntent
        private Intent createShareForecastIntent(){
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, FORECAST_SHARE_HASHTAG);
            return shareIntent;
        }

    }
}