package uk.jumpingmouse.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class DetailActivity extends AppCompatActivity {
    /** The log tag for this class. */
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.action_settings:
                // Start the settings activity
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

        }


        return super.onOptionsItemSelected(menuItem);
    }

    /**
     * The fragment containing the detail view.
     */
    public static class DetailFragment extends Fragment {

        private String forecast = null;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            Intent intent = getActivity().getIntent();
            forecast = intent.getStringExtra(Intent.EXTRA_TEXT);

            TextView txtForecast = (TextView) rootView.findViewById(R.id.txtForecast);
            txtForecast.setText(forecast);

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_detailfragment, menu);

            MenuItem menuItem = menu.findItem(R.id.menu_item_share);
            ShareActionProvider actionProvider =
                    (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            if (actionProvider != null) {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                //sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT); // API 21 onwards
                sendIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                sendIntent.putExtra(Intent.EXTRA_TEXT, forecast + " #SunshineApp");
                sendIntent.setType("text/plain");
                actionProvider.setShareIntent(sendIntent);
            } else {
                Log.w(LOG_TAG, "Action provider for share intent is null");
            }
        }

    }

}
