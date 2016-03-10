package com.example.android.popmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

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
 * A placeholder fragment containing a simple view.
 */
public class MovieDBFragment extends Fragment {

    //Create an arrayList adapter to hold movie data
    public ImageAdapter mMovieListAdapter;
    GridView my_gridView_movieList;
    public String[] popMovieSynopsis = new String[20];
    public String[] popMovieTitle = new String[20];
    public String[] popMovieRating = new String[20];
    public String[] popMovieReleaseDate = new String[20];
    public MovieDBFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        Context curContext = getContext();

        my_gridView_movieList = (GridView) rootView.findViewById(R.id.thumbView);


        //Create listener for clicked gridview item
        my_gridView_movieList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Create intent to open DetailActivity
                Intent detailsIntent = new Intent(getActivity(), DetailActivity.class);
                //Create and populate bundle with info required for detail activity
                Bundle movieDataBundle = new Bundle();
                movieDataBundle.putString("title", popMovieTitle[position]);
                movieDataBundle.putString("synopsis", popMovieSynopsis[position]);
                movieDataBundle.putString("releaseDate", popMovieReleaseDate[position]);
                movieDataBundle.putString("rating", popMovieRating[position]);
                movieDataBundle.putString("imgURL", mMovieListAdapter.movieList[position]);
                detailsIntent.putExtra("movieDataBundle", movieDataBundle);

                //Start activity using intent
                startActivity(detailsIntent);
            }
        });



        Log.v("onCreateView", "View Created");
        return rootView;
    }

    //create method to perform update tasks
    private void updatePopMovieList(){
        Log.v("updatePopMovieList", "Update function started");
            FetchMovieTask movieListTask= new FetchMovieTask(getContext(),getView());
            //To use the sort type saved in the preferences you need to access the
            //shared preferences that handles all preferences in a project.
            //Create a SharedPreference object that you will initialise to teh default Shared
            //Preferences file.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            movieListTask.execute();
        Log.v("updatePopMovieList", "Update function completed");

    }

    //Override onStart method to update the movie data

    @Override
    public void onStart(){
       Log.v("onStart", "Fragment function started");
       updatePopMovieList();
       super.onStart();
    }

    public class ImageAdapter extends BaseAdapter
    {
        private Context context;
        private String[] movieList;

        public ImageAdapter(Context c, String[] list)
        {
            super();
            this.context = c;
            this.movieList = list;

        }

        //---returns the number of images---
        public int getCount() {
            return movieList.length;
        }

        //---returns the ID of an item---

        public Object getItem(int position) {
            return movieList[position];
        }

        public long getItemId(int position) {
            return position;
        }

        //---returns an ImageView view---
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                convertView = imageView;
            } else {
                imageView = (ImageView) convertView;
            }
            Picasso.with(context).load(movieList[position]).into(imageView);

            return convertView;
        }

        public void updateMovieList(String[] newList){
            this.movieList = newList;
            this.notifyDataSetChanged();
        }

    }

    public class FetchMovieTask extends AsyncTask<String, Void, String[]> {

        //create string to be used as tag for application
        public final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        private Context passedContext;
        private View passedRootView;

        //create a constructor to take in passed Context and View
        public FetchMovieTask(Context context, View rView){
            this.passedContext = context;
            this.passedRootView = rView;
        }
        //Going to insert networking code snippet from Udacity's GitHub repo
        SharedPreferences sortOrder = PreferenceManager.getDefaultSharedPreferences(getContext());
        String sort_order = sortOrder.getString(getString(R.string.pref_sort_order_key), getString(R.string.pref_sort_order_default));
        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_RESULTS = "results";
            final String TMDB_ID = "id";
            final String TMDB_TITLE = "title";
            final String TMDB_SYNOPSIS = "overview";
            final String TMDB_RATING = "vote_average";
            final String TMDB_RELEASE_DT = "release_date";
            final String TMDB_POSTER_PATH = "poster_path";
            final String TMDB_BASE_IMAGE_URL = "http://image.tmdb.org/t/p/w500/";


            //create a JSON object from the response received by TMDB
            //then create an array to hold the results
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMDB_RESULTS);

            String[] resultStrs = new String[20];
            for(int i = 0; i < movieArray.length(); i++) {
                // declare strings that you will map to the section headers of the JSON response
                String movieTitle;
                String moviePosterPath;
                String movieId;
                String movieSynopsis;
                String movieRating;
                String movieReleaseDate;


                // Get the JSON Object representing the movie
                JSONObject popularMovie = movieArray.getJSONObject(i);

                //Assign values stripped from JSON
                movieId = popularMovie.getString(TMDB_ID);
                movieTitle = popularMovie.getString(TMDB_TITLE);
                movieSynopsis = popularMovie.getString(TMDB_SYNOPSIS);
                movieRating = popularMovie.getString(TMDB_RATING);
                movieReleaseDate = popularMovie.getString(TMDB_RELEASE_DT);
                moviePosterPath = popularMovie.getString(TMDB_POSTER_PATH);


                popMovieTitle[i] = movieTitle;
                popMovieRating[i] = movieRating;
                popMovieReleaseDate[i] = movieReleaseDate;
                popMovieSynopsis[i] = movieSynopsis;
                resultStrs[i] = TMDB_BASE_IMAGE_URL + moviePosterPath;
            }

            //For log verification to be removed in final product.

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Movie List Entry " + s);
            }

            return resultStrs;

        }

        // Will contain the raw JSON response as a string.
        String movieListJsonStr = null;

        protected String[] doInBackground(String... params) {

            /*
            //If there is no parameters then there is nothing to fetch
            if (params.length == 0){
                return null;
            }
            */
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                //The following strings will be used to build the URL
                final String TMDB_BASE_URL = "http://api.themoviedb.org/3/";
                final String QUERY_PARAM = "movie";
                final String FORMAT_PARAM = sort_order;
                final String APPID_PARAM = "api_key";


                //Time to build the URL
                //Declare a Uri object and run the parse command
                Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendPath(QUERY_PARAM)
                        .appendPath(FORMAT_PARAM)
                        .appendQueryParameter(APPID_PARAM,getString(R.string.tmdb_app_id))
                        .build();

                //Place the build URL string into URL variable for future use
                URL url = new URL(builtUri.toString());
                //Create a verbose (annotated by the "v" printing the built URL. To be removved in final code
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
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
                movieListJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Movie Data JSON String: " + movieListJsonStr);



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
            //End of Udacity Code

            try {
                Log.v("doInBackground","Returning movieListJsonStr");
                return getMovieDataFromJson(movieListJsonStr);
            } catch(JSONException e){
                Log.e(LOG_TAG,e.getMessage(),e);
                e.printStackTrace();
            }
            Log.v("doInBackground","Returning movieList:" + movieListJsonStr );
            String[] movieList = {movieListJsonStr};
            return movieList;
        }

        @Override
        protected void onPostExecute(String[] strings) {

            if(mMovieListAdapter == null){
                mMovieListAdapter = new ImageAdapter(passedContext,strings);
                mMovieListAdapter.notifyDataSetChanged();
            }else {
                mMovieListAdapter.updateMovieList(strings);
            }
            my_gridView_movieList.setAdapter(mMovieListAdapter);
            Log.v("onPostExecute", "ImageAdapter Update Complete");
            super.onPostExecute(strings);

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);  // if there is options menu,
    }



}
