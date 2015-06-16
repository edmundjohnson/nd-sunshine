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

    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time, Locale locale){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd", locale);
        return shortenedDateFormat.format(time);
    }

    /**
     * Format the weather high/low temperatures for presentation.
     */
    private String formatHighLow(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        return roundedHigh + "/" + roundedLow;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    public String[] getWeatherDataFromJson(String jsonStringForecast, int numDays) throws JSONException {

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
        for(int i = 0; i < weatherArray.length(); i++) {
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

            // The weather description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = jsonDayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            weatherDescription = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Do not name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = jsonDayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            temperatureHighAndLow = formatHighLow(high, low);
            strDayForecasts[i] = dateHumanReadable + " - " + weatherDescription + " - " + temperatureHighAndLow;
        }

        return strDayForecasts;
    }

    /**
     * Given a string of the form returned by the api call:
     * http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7
     * retrieve the maximum temperature for the day indicated by dayIndex
     * (Note: 0-indexed, so 0 would refer to the first day).
     */
    /*
    public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
            throws JSONException {
        if (dayIndex < 0 || dayIndex > 6) {
            Log.e(LOG_TAG, "dayIndex must be between 0 and 6 inclusive");
            return -1;
        } else if (weatherJsonStr == null || weatherJsonStr.isEmpty()) {
            Log.e(LOG_TAG, "weatherJsonStr was null or empty");
            return -1;
        } else {
            JSONObject weeklyForecast = new JSONObject(weatherJsonStr);
            JSONArray dayForecasts = weeklyForecast.getJSONArray("list");
            JSONObject dayForecast = dayForecasts.getJSONObject(dayIndex);
            JSONObject dayTemp = dayForecast.getJSONObject("temp");
            return dayTemp.getDouble("max");
        }
    }
    */

/* TESTS

    import junit.framework.TestCase;
    import org.json.JSONException;

    public class WeatherDataParserTest extends TestCase {
        private final static String WEATHER_DATA_MTV_JUN_4 = "{\"cod\":\"200\",\"message\":4.2116,\"city\":{\"id\":\"5375480\",\"name\":\"Mountain View\",\"coord\":{\"lon\":-122.075,\"lat\":37.4103},\"country\":\"United States of America\",\"population\":0},\"cnt\":7,\"list\":[{\"dt\":1401912000,\"temp\":{\"day\":20.17,\"min\":12.3,\"max\":20.17,\"night\":12.3,\"eve\":17.74,\"morn\":14.05},\"pressure\":1012.43,\"humidity\":77,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"sky is clear\",\"icon\":\"01d\"}],\"speed\":1.67,\"deg\":253,\"clouds\":0},{\"dt\":1401998400,\"temp\":{\"day\":18.9,\"min\":10.74,\"max\":18.9,\"night\":10.74,\"eve\":15.54,\"morn\":14.02},\"pressure\":1009.89,\"humidity\":76,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"sky is clear\",\"icon\":\"01d\"}],\"speed\":1.51,\"deg\":225,\"clouds\":0},{\"dt\":1402084800,\"temp\":{\"day\":13.59,\"min\":13.57,\"max\":14.1,\"night\":14.1,\"eve\":14.04,\"morn\":13.57},\"pressure\":1022.58,\"humidity\":0,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"sky is clear\",\"icon\":\"01d\"}],\"speed\":8.92,\"deg\":325,\"clouds\":0},{\"dt\":1402171200,\"temp\":{\"day\":13.71,\"min\":13.71,\"max\":13.93,\"night\":13.93,\"eve\":13.73,\"morn\":13.93},\"pressure\":1021.29,\"humidity\":0,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"sky is clear\",\"icon\":\"01d\"}],\"speed\":6.41,\"deg\":326,\"clouds\":0},{\"dt\":1402257600,\"temp\":{\"day\":13.55,\"min\":13.52,\"max\":13.72,\"night\":13.52,\"eve\":13.72,\"morn\":13.62},\"pressure\":1022.14,\"humidity\":0,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"sky is clear\",\"icon\":\"01d\"}],\"speed\":9.72,\"deg\":320,\"clouds\":0},{\"dt\":1402344000,\"temp\":{\"day\":12.72,\"min\":12.72,\"max\":13.22,\"night\":13.22,\"eve\":13.19,\"morn\":13.06},\"pressure\":1027.87,\"humidity\":0,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"sky is clear\",\"icon\":\"01d\"}],\"speed\":7.85,\"deg\":322,\"clouds\":7},{\"dt\":1402430400,\"temp\":{\"day\":13.11,\"min\":12.89,\"max\":13.35,\"night\":13.26,\"eve\":13.35,\"morn\":12.89},\"pressure\":1029.35,\"humidity\":0,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"sky is clear\",\"icon\":\"01d\"}],\"speed\":11.01,\"deg\":330,\"clouds\":0}]}";
        private final static String WEATHER_DATA_FREMONT_JUN_4 = "{\"cod\":\"200\",\"message\":2.5405,\"city\":{\"id\":\"5350734\",\"name\":\"Fremont\",\"coord\":{\"lon\":-121.982,\"lat\":37.5509},\"country\":\"United States of America\",\"population\":0},\"cnt\":7,\"list\":[{\"dt\":1401912000,\"temp\":{\"day\":28.12,\"min\":12.74,\"max\":28.12,\"night\":12.74,\"eve\":23.73,\"morn\":28.12},\"pressure\":1004.41,\"humidity\":52,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"sky is clear\",\"icon\":\"01d\"}],\"speed\":2.41,\"deg\":263,\"clouds\":0},{\"dt\":1401998400,\"temp\":{\"day\":28.58,\"min\":10.3,\"max\":28.58,\"night\":10.3,\"eve\":22.76,\"morn\":16.08},\"pressure\":1002.75,\"humidity\":47,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"sky is clear\",\"icon\":\"01d\"}],\"speed\":3.13,\"deg\":261,\"clouds\":0},{\"dt\":1402084800,\"temp\":{\"day\":26.73,\"min\":11.2,\"max\":27.55,\"night\":11.2,\"eve\":22.94,\"morn\":12.53},\"pressure\":1001.79,\"humidity\":50,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"sky is clear\",\"icon\":\"01d\"}],\"speed\":2.01,\"deg\":267,\"clouds\":0},{\"dt\":1402171200,\"temp\":{\"day\":30.67,\"min\":11.81,\"max\":31.25,\"night\":11.81,\"eve\":25.92,\"morn\":15.5},\"pressure\":1001.95,\"humidity\":48,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"sky is clear\",\"icon\":\"01d\"}],\"speed\":1.91,\"deg\":271,\"clouds\":0},{\"dt\":1402257600,\"temp\":{\"day\":16.6,\"min\":10.32,\"max\":17.52,\"night\":12.62,\"eve\":17.52,\"morn\":10.32},\"pressure\":1003.29,\"humidity\":0,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"sky is clear\",\"icon\":\"01d\"}],\"speed\":2.84,\"deg\":292,\"clouds\":0},{\"dt\":1402344000,\"temp\":{\"day\":14.82,\"min\":10.66,\"max\":16.14,\"night\":11.97,\"eve\":16.14,\"morn\":10.66},\"pressure\":1009.02,\"humidity\":0,\"weather\":[{\"id\":500,\"main\":\"Rain\",\"description\":\"light rain\",\"icon\":\"10d\"}],\"speed\":3.72,\"deg\":305,\"clouds\":12},{\"dt\":1402430400,\"temp\":{\"day\":15.26,\"min\":9.84,\"max\":16.75,\"night\":12.76,\"eve\":16.75,\"morn\":9.84},\"pressure\":1009.9,\"humidity\":0,\"weather\":[{\"id\":800,\"main\":\"Clear\",\"description\":\"sky is clear\",\"icon\":\"01d\"}],\"speed\":5.87,\"deg\":325,\"clouds\":0}]}";

        public void testMountainViewThirdDay() throws JSONException {
            assertEquals(14.1, WeatherDataParser.getMaxTemperatureForDay(WEATHER_DATA_MTV_JUN_4, 2));
        }

        public void testFremontLastDay() throws JSONException {
            assertEquals(16.75, WeatherDataParser.getMaxTemperatureForDay(WEATHER_DATA_FREMONT_JUN_4, 6));
        }

        public void testFremonFirstDay() throws JSONException {
            assertEquals(28.12, WeatherDataParser.getMaxTemperatureForDay(WEATHER_DATA_FREMONT_JUN_4, 0));
        }

        public void testDayIndexTooLow() throws JSONException {
            assertEquals(-1.0, WeatherDataParser.getMaxTemperatureForDay(WEATHER_DATA_FREMONT_JUN_4, -1));
        }

        public void testDayIndexTooHigh() throws JSONException {
            assertEquals(-1.0, WeatherDataParser.getMaxTemperatureForDay(WEATHER_DATA_FREMONT_JUN_4, 7));
        }

        public void testJsonNull() throws JSONException {
            assertEquals(-1.0, WeatherDataParser.getMaxTemperatureForDay(null, 6));
        }

        public void testJsonEmpty() throws JSONException {
            assertEquals(-1.0, WeatherDataParser.getMaxTemperatureForDay("", 6));
        }

        public void testJsonInvalidFormat() throws JSONException {
            try {
                assertEquals(-1.0, WeatherDataParser.getMaxTemperatureForDay( "hello", 6));
                assertTrue(false);
            } catch (JSONException e) {
                // OK
            }
        }

    }
*/
}
