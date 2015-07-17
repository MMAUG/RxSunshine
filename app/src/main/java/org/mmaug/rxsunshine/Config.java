package org.mmaug.rxsunshine;

/**
 * Created by poepoe on 16/7/15.
 * This is the class that store static values which we will use across the projects
 */
public class Config {

  //Base Url
  public static final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
  public static final String IMAGE_URL = "http://openweathermap.org/img/w/";

  //Query Parameters
  public static final String PARAM_MODE = "mode";
  public static final String PARAM_UNITS = "units";
  public static final String PARAM_QUERY = "q";
  public static final String PARAM_DAYS = "cnt";

  //Query Default Values
  public static final String JSON = "json";
  public static final String METRIC = "metric";
}
