package cz.barush.shoporganizer.services;

import android.util.Log;

import com.android.volley.Response;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import cz.barush.shoporganizer.persistance.entity.ILPResponse;
import cz.barush.shoporganizer.persistance.entity.TSPResponse;
import cz.barush.shoporganizer.utils.Computation;

import static cz.barush.shoporganizer.services.AppConfig.TAG;

/**
 * Created by Barbora on 30-Apr-17.
 */

public class MyTSPResponseListener implements Response.Listener<JSONObject>
{
    @Override
    public void onResponse(JSONObject result)
    {
        Log.i(TAG, "onResponse: Result= " + result.toString());
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            TSPResponse TSPresult = mapper.readValue(result.toString(), TSPResponse.class);
            Computation.getBestCombination3(TSPresult.getUniqueFeasibleSets());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
