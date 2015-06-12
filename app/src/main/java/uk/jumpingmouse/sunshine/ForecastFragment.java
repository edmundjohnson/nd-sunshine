package uk.jumpingmouse.sunshine;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The fragment which displays the list of daily forecasts.
 * @author Edmund Johnson.
 */
public class ForecastFragment extends Fragment {
    /** The log tag for this class. */
    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();

    /** The URL for retrieving the 7-day forecast for Mountain View. */
    //private static final String FORECAST_URL = ""http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7"";
    /** The URL for retrieving the 7-day forecast for Bristol UK. */
    private static final String FORECAST_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?id=2654675&mode=json&units=metric&cnt=7";

    /**
     * Default constructor.
     */
    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Indicate that the fragment can handle menu events
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        //menu.clear();
        inflater.inflate(R.menu.menu_forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            new FetchWeatherTask().execute(FORECAST_URL);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String[] forecastData = new String[] {
                "Today - Sunny - 88 / 63",
                "Tomorrow - Cloudy - 82 / 58",
                "Wednesday - Foggy - 75 / 54",
                "Thursday - STAY INDOORS - 60 / 41",
                "Friday - Scorcher - 95 / 72",
                "Saturday - Snow and Ice - 64 / 44",
                "Sunday - Tornado - 78 / 56"
        };
        List<String> weekForecastItem = new ArrayList<>(Arrays.asList(forecastData));

        ArrayAdapter<String> forecastAdapter = new ArrayAdapter<>(
                // the current context
                getActivity(),
                // the list item layout
                R.layout.list_item_forecast,
                // the TextView to populate
                R.id.list_item_forecast_textview,
                weekForecastItem);

        new FetchWeatherTask().execute(FORECAST_URL);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach the adapter to it
        ListView listForecast = (ListView) rootView.findViewById(R.id.listview_forecast);
        listForecast.setAdapter(forecastAdapter);

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String[] params) {
            if (params == null || params.length != 1) {
                throw new InvalidParameterException("FetchWeatherTask requires a single URL parameter");
            }
            String strUrl = params[0];

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query.
                // Possible parameters are available at OpenWeatherMap's forecast API page,
                // at http://openweathermap.org/API#forecast
                URL url = new URL(strUrl);

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
                forecastJsonStr = buffer.toString();

                Log.v(LOG_TAG, "forecastJsonStr = \n" + forecastJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException", e);
                // If the code didn't successfully get the weather data, there's no point in
                // attempting to parse it.
                return null;
            } finally{
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
            return null;
        }
    }
}
