package org.mmaug.rxsunshine;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by poepoe on 17/7/15.
 */
public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {

  ArrayList<WeatherItem> weatherItemArrayList;

  //Constructor
  public WeatherAdapter() {

    //initialized arraylist so the adapter size is zero while we wait fetching list from API.
    weatherItemArrayList = new ArrayList<>();

    //This is making view holder item to have stable id. So it will not change
    setHasStableIds(true);
  }

  //call this method when we have successfully got data from API
  public void setData(ArrayList<WeatherItem> weatherItemArrayList) {
    this.weatherItemArrayList = weatherItemArrayList;

    //notify the adapther that array list is changed and need to update the views
    notifyDataSetChanged();
  }

  //creating new view
  @Override public WeatherAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
    View view =
        LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_weather, viewGroup, false);
    return new ViewHolder(view);
  }

  //bind data to view
  @Override public void onBindViewHolder(ViewHolder viewHolder, int i) {
    WeatherItem item = weatherItemArrayList.get(i);
    viewHolder.mWeatherText.setText(item.text);
  }

  //adapter count
  @Override public int getItemCount() {
    return weatherItemArrayList.size();
  }

  @Override public long getItemId(int position) {
    return position;
  }

  static class ViewHolder extends RecyclerView.ViewHolder {

    TextView mWeatherText;
    ImageView mWeatherImage;

    public ViewHolder(View itemView) {
      super(itemView);
      mWeatherText = (TextView) itemView.findViewById(R.id.tv_weather_text);
      mWeatherImage = (ImageView) itemView.findViewById(R.id.iv_weather_icon);
    }
  }
}
