package uk.jumpingmouse.sunshine;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The fragment for the forecast list.
 * @author Edmund Johnson.
 */
public class MainActivityFragment extends Fragment {

    /**
     * Default constructor.
     */
    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] forecastArray = new String[] {
                "Today - Sunny - 88 / 63",
                "Tomorrow - Cloudy - 82 / 58",
                "Wednesday - Foggy - 75 / 54",
                "Thursday - STAY INDOORS - 60 / 41",
                "Friday - Scorcher - 95 / 72",
                "Saturday - Snow and Ice - 64 / 44",
                "Sunday - Tornado - 78 / 56"
        };
        List<String> weekForecastItem = new ArrayList<>(Arrays.asList(forecastArray));

        ArrayAdapter<String> forecastAdapter = new ArrayAdapter<>(
                // the current context
                getActivity(),
                // the list item layout
                R.layout.list_item_forecast,
                // the TextView to populate
                R.id.list_item_forecast_textview,
                weekForecastItem);

        ListView listForecast = (ListView) rootView.findViewById(R.id.listview_forecast);
        listForecast.setAdapter(forecastAdapter);

        return rootView;
    }
}
