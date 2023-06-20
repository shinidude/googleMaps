package com.example.compx202as3;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class MapsActivityTest {
    // Confirm method doesn't return an empty string
    @Test
    public void testCamNotEmpty() {
        String URL = MapsActivity.createCamURL(-37.7826, 175.2528);
        assertNotEquals("", URL);
    }

    // Confirm method doesnt return an empty string
    @Test
    public void testWeatherNotEmpty() {
        String URL = MapsActivity.createWeatherURL(-37.7826, 175.2528);
        assertNotEquals("", URL);
    }

    // Confirm method returned correct URL
    @Test
    public void evaluateCamURL() {
        String URL = MapsActivity.createCamURL(-37.7826, 175.2528);
        assertEquals("https://api.windy.com/api/webcams/v2/list/limit=5/nearby=-37.7826,175.2528,250/orderby=distance/?show=webcams:location,image;?&key=G7nva6bysKOxdZfgYI8caTQ5xoAPhRbC", URL);
    }

    // Confirm method returned correct URL
    @Test
    public void evaluateWeatherURL() {
        String URL = MapsActivity.createWeatherURL(-37.7826, 175.2528);
        assertEquals("https://api.openweathermap.org/data/2.5/weather?lat=-37.7826&lon=175.2528&appid=fcc6e74e69cc189958cf9bc68906255a", URL);
    }

    // Confirm URL works regardless of lat or long values being null
    @Test
    public void notNullCamURL() {
        String URL = MapsActivity.createCamURL(null, 175.2528);
        assertNotNull(URL);
        URL = MapsActivity.createCamURL(-37.7826, null);
        assertNotNull(URL);
    }

    // Confirm URL works regardless of lat or long values being null
    @Test
    public void notNullWeatherURL() {
        String URL = MapsActivity.createWeatherURL(null, 175.2528);
        assertNotNull(URL);
        URL = MapsActivity.createWeatherURL(-37.7826, null);
        assertNotNull(URL);
    }

    // Confirming method doesn't return a null value
    @Test
    public void notNullWeatherInfo() {
        try {
            JSONObject testObject =  new JSONObject();
            testObject.put("main", "General");
            testObject.put("description", "Sphinx of black quartz, judge my vow");

            String[] info = MapsActivity.getWeatherInfo(testObject);
            assertNotNull(info);
        } catch (JSONException je) {
        }
    }

    // Confirming correct values are returned
    @Test
    public void validateWeatherInfo() {
        try {
            JSONObject testObject = new JSONObject();
            testObject.put("main", "General");
            testObject.put("description", "sphinx of black quartz, judge my vow");

            String[] expected = new String[] {"general", "Sphinx of black quartz, judge my vow"};

            String[] info = MapsActivity.getWeatherInfo(testObject);
            assertArrayEquals(expected, info);
        } catch (JSONException je) {
        }
    }

    // Perform test evaluating result of having a null value
    @Test
    public void evaluateWeatherInfo() {
        try {
            JSONObject testObject = new JSONObject();
            testObject.put("main", null);
            testObject.put("description", "sphinx of black quartz, judge my vow");

            String[] expected = new String[2];

            String[] info = MapsActivity.getWeatherInfo(testObject);
            assertArrayEquals(expected, info);
        } catch (JSONException je) {
        }
    }
}