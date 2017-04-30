package cz.barush.shoporganizer.services;

import android.util.Log;

import com.android.volley.Response;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.barush.shoporganizer.utils.Computation;

import static cz.barush.shoporganizer.services.AppConfig.TAG;

/**
 * Created by Barbora on 30-Apr-17.
 */

public class MyResponseListener implements Response.Listener<JSONObject>
{
    int i;
    int j;
    public MyResponseListener(int i, int j)
    {
        this.i = i;
        this.j = j;
    }

    @Override
    public void onResponse(JSONObject result)
    {
        Log.i(TAG, "onResponse: Result= " + result.toString());
        try
        {
            JSONArray routeArray = result.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);

            JSONArray newTempARr = routes.getJSONArray("legs");
            JSONObject newDisTimeOb = newTempARr.getJSONObject(0);

            JSONObject distOb = newDisTimeOb.getJSONObject("distance");
            Computation.distances[i][j] = Integer.valueOf(distOb.getString("value"));
            Computation.pendingRequests--;
            if(Computation.pendingRequests == 0)Computation.getBestCombination2();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (JsonProcessingException e)
        {
            e.printStackTrace();
        }
    }
}
