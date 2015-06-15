package uk.jumpingmouse.sunshine;

import android.net.Uri;
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

import org.json.JSONException;

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


    /**
     * The base URL for retrieving a daily weather forecast from OpenWeatherMap.
     * Possible parameters are available at OpenWeatherMap's forecast API page,
     * at http://openweathermap.org/API#forecast
     * Example of a full URL:
     * http://api.openweathermap.org/data/2.5/forecast/daily?id=2654675&mode=json&units=metric&cnt=7
     */
    private static final String URL_DAILY_FORECAST = "api.openweathermap.org/data/2.5/forecast/daily";
    /** The forecast URL parameter for the city id ("q" does not work for Bristol, UK). */
    private static final String FORECAST_PARAM_CITY_ID = "id";
    /** The forecast URL parameter for the format required (JSON, XML, etc.). */
    private static final String FORECAST_PARAM_MODE = "mode";
    /** The forecast URL parameter for the units required (metric, imperial, etc.). */
    private static final String FORECAST_PARAM_UNITS = "units";
    /** The forecast URL parameter for the number of days required. */
    private static final String FORECAST_PARAM_DAY_COUNT = "cnt";

    /** Bristol's city id (Mountain View's id is 94043). */
    private static final String CITY_ID_BRISTOL = "2654675";
    /** The mode for getting JSON returned. */
    private static final String MODE_JSON = "json";
    /** The mode for getting values returned in metric units. */
    private static final String UNITS_METRIC = "metric";
    /** The day count for a 7-day forecast. */
    private static final int FORECAST_DAYS = 7;

    private WeatherDataParser weatherDataParser;


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
        inflater.inflate(R.menu.menu_forecastfragment, menu);
    }

    /**
     * Handle the selection of a menu item.
     * The action bar will automatically handle clicks on the Home/Up button, so long
     * as a parent activity is specified in AndroidManifest.xml.
     * @param item the menu item selected
     * @return whether the event has been consumed
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            new FetchWeatherTask().execute(CITY_ID_BRISTOL);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get the forecast
        new FetchWeatherTask().execute(CITY_ID_BRISTOL);

        String[] forecastData = new String[] {
                "Today - Sunny - 88 / 63",
                "Tomorrow - Cloudy - 82 / 58",
                "Wednesday - Foggy - 75 / 54",
                "Thursday - STAY INDOORS - 60 / 41",
                "Friday - Scorcher - 95 / 72",
                "Saturday - Snow and Ice - 64 / 44",
                "Sunday - Tornado - 78 / 56"
        };

        List<String> weekForecastItems = new ArrayList<>(Arrays.asList(forecastData));

        ArrayAdapter<String> forecastAdapter = new ArrayAdapter<>(
                // the current context
                getActivity(),
                // the list item layout
                R.layout.list_item_forecast,
                // the TextView to populate
                R.id.list_item_forecast_textview,
                weekForecastItems);

        // Inflate the fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach the adapter to it
        ListView listForecast = (ListView) rootView.findViewById(R.id.listview_forecast);
        listForecast.setAdapter(forecastAdapter);

        return rootView;
    }

    private WeatherDataParser getWeatherDataParser() {
        if (weatherDataParser == null) {
            weatherDataParser = new WeatherDataParser();
        }
        return weatherDataParser;
    }

    /**
     * Background task for getting a weather forecast over the internet.
     */
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String[] params) {
            if (params == null || params.length != 1) {
                throw new InvalidParameterException("FetchWeatherTask requires a single parameter, the city id");
            }

            // Get the raw JSON weather data.
            String jsonForecast = getForecastJson(params[0], FORECAST_DAYS);
            Log.v(LOG_TAG, "jsonForecast = \n" + jsonForecast);

            String[] forecastData = null;
            try {
                forecastData = getWeatherDataParser().getWeatherDataFromJson(jsonForecast, FORECAST_DAYS);
                if (forecastData == null) {
                    Log.v(LOG_TAG, "forecastData is null");
                } else {
                    for (String dayForecast : forecastData) {
                        Log.v(LOG_TAG, "dayForecast: " + dayForecast);
                    }
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSONException while parsing raw weather data");
            }

            return forecastData;
        }

        private String getForecastJson(String cityId, int numDays) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJson = null;

            try {
                // Build the forecast URI using the standard values and the parameters
                Uri.Builder uriBuilder = new Uri.Builder();
                uriBuilder.scheme("http");
                uriBuilder.authority(URL_DAILY_FORECAST);
                uriBuilder.appendQueryParameter(FORECAST_PARAM_CITY_ID, cityId);
                uriBuilder.appendQueryParameter(FORECAST_PARAM_MODE, MODE_JSON);
                uriBuilder.appendQueryParameter(FORECAST_PARAM_UNITS, UNITS_METRIC);
                uriBuilder.appendQueryParameter(FORECAST_PARAM_DAY_COUNT, Integer.toString(numDays));

                // Construct the URL for the query.
                String strUrl = uriBuilder.build().toString();
                Log.v(LOG_TAG, "Forecast URL: " + strUrl);
                URL url = new URL(uriBuilder.build().toString());

                // Create the HTTP request, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder builder = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // builder for debugging.
                    builder.append(line);
                    builder.append("\n");
                }

                if (builder.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJson = builder.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException", e);
                // If the code didn't successfully get the weather data, there's no point in
                // attempting to parse it.
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
            return forecastJson;
        }
    }
}
