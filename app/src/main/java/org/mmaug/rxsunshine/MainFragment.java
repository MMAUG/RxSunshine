package org.mmaug.rxsunshine;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

  //Widgets
  private RecyclerView mRecyclerView;
  private TextView mErrorTextView;
  private ProgressBar mProgressBar;

  //Components
  private WeatherAdapter mAdapter;
  private MainActivity mActivity;

  //vales
  private String cityName = "Yangon";
  private int numOfDays = 7;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    mActivity = (MainActivity) getActivity();
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_main, container, false);

    mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_list);
    mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
    mErrorTextView = (TextView) view.findViewById(R.id.tv_error);

    //set Layout Manager for RecyclerView
    mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity));

    // specify an adapter
    mAdapter = new WeatherAdapter();
    mRecyclerView.setAdapter(mAdapter);

    return view;
  }

  @Override public void onStart() {
    super.onStart();
    //fetch weather
    getWeather();
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_main, menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_settings:
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Fetch weather data from API. show and hide widgets according to data set.
   */
  private void getWeather() {
    WeatherApi.getInstance()
        //pass city name and numbers of days to fetch from api
        .getWeather(cityName, numOfDays)
            //subscribe as a new thread apart from main thread, so it will run in background and
            // will not block UI thread
        .subscribeOn(Schedulers.newThread())
            //This is to update UI with what we got from background thread
        .observeOn(AndroidSchedulers.mainThread())
            //Without subscribe, the call will not make. Observable observes only after it has
            // subscribed. Means it will not do if you don't ask it.
        .subscribe(new Observer<JsonObject>() {
                     //Observer has three callback methods. First onNext method is executed, then
                     // onCompleted. onError is called when there is an error occurs in onNext
                     // method. Retrofit also has onError. But why do we use Rx? Retrofit only
                     // throws network related errors while Rx throws any error occurs.
                     @Override public void onCompleted() {
                       //show list and hide other views since it is successfully completed
                       mProgressBar.setVisibility(View.GONE);
                       mErrorTextView.setVisibility(View.GONE);
                       mRecyclerView.setVisibility(View.VISIBLE);
                     }

                     @Override public void onError(Throwable e) {
                       //show Error Message since there is an error.
                       mProgressBar.setVisibility(View.GONE);
                       mRecyclerView.setVisibility(View.GONE);

                       mErrorTextView.setVisibility(View.VISIBLE);
                       mErrorTextView.setText(e.getLocalizedMessage());
                     }

                     @Override public void onNext(JsonObject object) {
                       ArrayList<WeatherItem> arrayList = getWeatherDataFromJson(object, numOfDays);
                       mAdapter.setData(arrayList);
                     }
                   }

        );
  }

  /**
   * Take the String representing the complete forecast in JSON Format and
   * pull out the data we need to construct the Strings needed for the wireframes.
   * <p/>
   * Fortunately parsing is easy:  constructor takes the JSON string and converts it
   * into an Object hierarchy for us.
   */
  private ArrayList<WeatherItem> getWeatherDataFromJson(JsonObject forecastJson, int numDays) {

    // These are the names of the JSON objects that need to be extracted.
    final String OWM_LIST = "list";
    final String OWM_WEATHER = "weather";
    final String OWM_TEMPERATURE = "temp";
    final String OWM_MAX = "max";
    final String OWM_MIN = "min";
    final String OWM_DATETIME = "dt";
    final String OWM_DESCRIPTION = "main";

    /**
     * New line
     */
    final String OWM_ICON = "icon";
    ArrayList<WeatherItem> mItems = new ArrayList<>();

    JsonArray weatherArray = forecastJson.getAsJsonArray(OWM_LIST);

    String[] resultStrs = new String[numDays];
    for (int i = 0; i < weatherArray.size(); i++) {
      // For now, using the format "Day, description, hi/low"
      String day;
      String description;
      String highAndLow;

      String icon;

      // Get the JSON object representing the day
      JsonObject dayForecast = weatherArray.get(i).getAsJsonObject();

      // The date/time is returned as a long.  We need to convert that
      // into something human-readable, since most people won't read "1400356800" as
      // "this saturday".
      long dateTime = dayForecast.get(OWM_DATETIME).getAsLong();
      day = getReadableDateString(dateTime);

      // description is in a child array called "weather", which is 1 element long.
      JsonObject weatherObject = dayForecast.getAsJsonArray(OWM_WEATHER).get(0).getAsJsonObject();
      description = weatherObject.get(OWM_DESCRIPTION).getAsString();

      // Temperatures are in a child object called "temp".  Try not to name variables
      // "temp" when working with temperature.  It confuses everybody.
      JsonObject temperatureObject = dayForecast.getAsJsonObject(OWM_TEMPERATURE);
      double high = temperatureObject.get(OWM_MAX).getAsDouble();
      double low = temperatureObject.get(OWM_MIN).getAsDouble();

      icon = weatherObject.get(OWM_ICON).getAsString();
      Log.i("icon", icon);

      highAndLow = formatHighLows(high, low);
      resultStrs[i] = day + " - " + description + " - " + highAndLow;

      WeatherItem mItem = new WeatherItem();
      mItem.text = resultStrs[i];
      mItem.imageUrl = icon;
      mItems.add(mItem);
    }

    return mItems;
  }

  /**
   * The date/time conversion code is going to be moved outside the asynctask later,
   * so for convenience we're breaking it out into its own method now.
   */
  private String getReadableDateString(long time) {
    // Because the API returns a unix timestamp (measured in seconds),
    // it must be converted to milliseconds in order to be converted to valid date.
    Date date = new Date(time * 1000);
    SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
    return format.format(date).toString();
  }

  /**
   * Prepare the weather high/lows for presentation.
   */
  private String formatHighLows(double high, double low) {
    // For presentation, assume the user doesn't care about tenths of a degree.
    long roundedHigh = Math.round(high);
    long roundedLow = Math.round(low);

    String highLowStr = roundedHigh + "/" + roundedLow;
    return highLowStr;
  }
}
