package uk.jumpingmouse.sunshine;

import android.text.format.Time;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Class for parsing weather data.
 * @author Edmund Johnson.
 */
public class WeatherDataParser {
    /** The log tag for this class. */
    //private static final String LOG_TAG = WeatherDataParser.class.getSimpleName();

    private static final String TEMPERATURE_UNITS_FAHRENHEIT = "F";

    /* The date/time conversion code is going to be moved outside the AsyncTask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time, Locale locale) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd", locale);
        return shortenedDateFormat.format(time);
    }

    /**
     * Format the weather high/low temperatures for presentation.
     * @param high the high temperature
     * @param low the low temperature
     * @return the weather high/low temperatures for presentation
     */
    private final String formatHighLow(final double high, final double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        return roundedHigh + "/" + roundedLow;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data needed to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     * @param jsonStringForecast the forecast in JSON format
     * @param numDays the number of days of data to return
     * @return the weather data as an array of Strings
     * @throws JSONException if there is an error while parsing the JSON
     */
    public String[] getWeatherDataFromJson(String jsonStringForecast, int numDays,
                                           String temperatureUnits)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";
        final Locale locale = Locale.getDefault();

        JSONObject jsonObjectForecast = new JSONObject(jsonStringForecast);
        JSONArray weatherArray = jsonObjectForecast.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being asked
        // for, which means that we need to know the GMT offset to translate this data properly.

        // Since this data is sent in order and the first day is always the current day,
        // we're going to take advantage of that to get a nice normalized UTC date
        // for all of our weather.

        // We start at the date returned by local time.
        Time dayTime = new Time();
        dayTime.setToNow();
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] strDayForecasts = new String[numDays];
        for (int i = 0; i < weatherArray.length(); i++) {
            // For now, we are using the format "Date - description - high/low"
            String dateHumanReadable;
            String weatherDescription;
            String temperatureHighAndLow;

            // Get the JSON object representing the day
            JSONObject jsonDayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that into something
            // human-readable, since most people won't read "1400356800" as "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay + i);
            dateHumanReadable = getReadableDateString(dateTime, locale);

            // The weather description is in a child array called "weather",
            // which is 1 element long.
            JSONObject weatherObject = jsonDayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            weatherDescription = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Do not name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = jsonDayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);
            temperatureHighAndLow = formatHighLow(
                    formatTemperature(high, temperatureUnits),
                    formatTemperature(low, temperatureUnits));

            strDayForecasts[i] = dateHumanReadable
                                    + " - " + weatherDescription
                                    + " - " + temperatureHighAndLow;
        }

        return strDayForecasts;
    }

    /**
     * Formats a temperature in specified temperature units.
     * @param temperatureCentigrade a temperature in centigrade
     * @param temperatureUnits the temperature units, "1" for centigrade, "2" for fahrenheit
     * @return the temperature in the required units
     */
    private double formatTemperature(double temperatureCentigrade, String temperatureUnits) {
        if (TEMPERATURE_UNITS_FAHRENHEIT.equals(temperatureUnits)) {
            return temperatureCentigrade * 9 / 5 + 32;
        } else {
            return temperatureCentigrade;
        }
    }

}
