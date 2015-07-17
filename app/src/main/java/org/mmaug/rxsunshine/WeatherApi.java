package org.mmaug.rxsunshine;

import com.google.gson.JsonObject;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

import static org.mmaug.rxsunshine.Config.BASE_URL;
import static org.mmaug.rxsunshine.Config.JSON;
import static org.mmaug.rxsunshine.Config.METRIC;
import static org.mmaug.rxsunshine.Config.PARAM_DAYS;
import static org.mmaug.rxsunshine.Config.PARAM_MODE;
import static org.mmaug.rxsunshine.Config.PARAM_QUERY;
import static org.mmaug.rxsunshine.Config.PARAM_UNITS;

/**
 * @author poepoe
 *         This class adapts Java Interface to REST API. If you don't know about retrofit yet,
 *         please check this awesome explanation in Burmese
 * @link http://swanhtetaung.blogspot.sg/2015/05/retrofit.html
 */
public class WeatherApi {

  private static WeatherApi weatherApi;
  private WeatherService weatherService;

  //instantiate only when it is the first time and prevent creating again and again.
  public WeatherApi() {
    weatherService = restAdapter().create(WeatherService.class);
  }

  /**
   * @return instance of the class
   */
  public static synchronized WeatherApi getInstance() {
    if (weatherApi == null) {
      weatherApi = new WeatherApi();
    }
    return weatherApi;
  }

  /**
   * @return weatherService
   */
  private WeatherService getWeatherService() {
    return weatherService;
  }

  /**
   * @return RestAdapter created with API base url. The purpose of creating this method is
   * you can just make separate param for DEBUG and PRODUCTION. In this example is just for log.
   * But in some cases, different urls, cookies, authen, etc according to debug or production
   * build.
   */
  private RestAdapter restAdapter() {

    RestAdapter restAdapter;

    //show Retrofit Logs only in DEBUG mode. this is a good usage if you have temporary memory loss
    // like me
    if (BuildConfig.DEBUG) {
      restAdapter = new RestAdapter.Builder().setEndpoint(BASE_URL)
          .setLogLevel(RestAdapter.LogLevel.BASIC)
          .build();
    } else {
      restAdapter = new RestAdapter.Builder().setEndpoint(BASE_URL).build();
    }

    return restAdapter;
  }

  /**
   * @return JsonObject from API, you can directly call this method since weatherService is
   * already initialized
   */
  public Observable<JsonObject> getWeather(String cityName, int numOfDays) {
    return getWeatherService().getWeather(cityName, numOfDays);
  }

  interface WeatherService {

    /**
     * @param cityName the location to forecast for weather
     * @param numOfDays the total numbers of days to forecast
     * @return JsonObject from Observable pattern
     */
    @GET("/?" + PARAM_MODE + "=" + JSON + "&" + PARAM_UNITS + "=" + METRIC)
    Observable<JsonObject> getWeather(@Query(PARAM_QUERY) String cityName,
        @Query(PARAM_DAYS) int numOfDays);
  }
}
