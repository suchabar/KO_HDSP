package cz.barush.shoporganizer.services;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import static cz.barush.shoporganizer.services.AppConfig.TAG;

/**
 * Created by Barbora on 30-Apr-17.
 */

public class MyErrorListener implements Response.ErrorListener
{
    @Override
    public void onErrorResponse(VolleyError error)
    {
        Log.e(TAG, "onErrorResponse: Error= " + error);
        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
    }
}
