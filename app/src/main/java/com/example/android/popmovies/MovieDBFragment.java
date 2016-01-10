package com.example.android.popmovies;

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
import android.widget.ArrayAdapter;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDBFragment extends Fragment {

    //Create an arrayList adatper to hold movie data
    public ArrayAdapter<String> mMovieListAdapter;

    public MovieDBFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //Create a list that will populate the GridView
        List thumbnails = new ArrayList<String> ();

        //declare new array adapter
        mMovieListAdapter = new ArrayAdapter<String>
                (
                        //getContext will return the current context (the parent items activity)
                        getContext(),
                        //this will provide the name of the list item layout
                        R.layout.thumbnail_view,
                        R.id.list_item_movie_textview,
                        //Finally we include the ArrayList that contains the data we want to populate the listviews with
                        thumbnails);

        //Now to bind the adapter to the list view. But first we'll need to create an reference to the list view as we only created
        //it in the fragment xml file

        GridView my_listview_movieList = (GridView) rootView.findViewById(R.id.thumbView);

        //then use the setAdapter method to bind the listview to the adapter
        my_listview_movieList.setAdapter(mMovieListAdapter);

        return rootView;
    }

    //create method to perform update tasks
    private void updatePopMovieList(){
        FetchMovieTask movieListTask= new FetchMovieTask();
        //To use the zip saved in the preferences you need to access the
        //shared preferences that handles all preferences in a project.
        //Create a SharedPreference object that you will initialise to teh default Shared
        //Preferences file.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        //Create a string that will hold the string value that you can find using the
        // getString function of the Shared preferences. Since that function requires 2 strings
        //you have the use the general getString function to get the strings from the resource ID
        movieListTask.execute();
    }

    //Override onStart method to update the weather data
    @Override
    public void onStart(){
        super.onStart();
        updatePopMovieList();
    }

    public class FetchMovieTask extends AsyncTask<String, Void, String[]> {

        //create string to be used as tag for application
        public final String LOG_TAG = FetchMovieTask.class.getSimpleName();
        //Going to insert networking code snippet from Udacity's GitHub repo

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
            final String TMDB_POSTER_PATH = "poster_path";
            //final String TMDB_OVERVIEW = "overview";


            //create a JSON object from the repsonse recieved by TMDB
            //then create an array to hold the results
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMDB_RESULTS);

            String[] resultStrs = new String[20];
            for(int i = 0; i < movieArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String movieTitle;
                String moviePosterPath;
                String movieId;

                // Get the JSON Object representing the movie
                JSONObject popularMovie = movieArray.getJSONObject(i);

                //Assign values stripped from JSON
                movieId = popularMovie.getString(TMDB_ID);
                movieTitle = popularMovie.getString(TMDB_TITLE);
                moviePosterPath = popularMovie.getString(TMDB_POSTER_PATH);

                resultStrs[i] = movieId + "_" + movieTitle + "_" + moviePosterPath;
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
                final String FORMAT_PARAM = "top_rated";
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

                Log.v(LOG_TAG, "Forecast JSON String: " + movieListJsonStr);



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
                return getMovieDataFromJson(movieListJsonStr);
            } catch(JSONException e){
                Log.e(LOG_TAG,e.getMessage(),e);
                e.printStackTrace();
            }
            String[] movieList = {movieListJsonStr};
            return movieList;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            mMovieListAdapter.clear();
            mMovieListAdapter.addAll(strings);
            super.onPostExecute(strings);
        }
    }
}
