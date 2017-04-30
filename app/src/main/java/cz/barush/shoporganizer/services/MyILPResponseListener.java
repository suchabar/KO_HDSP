package cz.barush.shoporganizer.services;

import android.util.Log;

import com.android.volley.Response;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.barush.shoporganizer.persistance.entity.ILPResponse;
import cz.barush.shoporganizer.utils.Computation;

import static cz.barush.shoporganizer.services.AppConfig.TAG;

/**
 * Created by Barbora on 30-Apr-17.
 */

public class MyILPResponseListener implements Response.Listener<JSONObject>
{
    List<Integer> supermarketCombination;

    public MyILPResponseListener(List<Integer> supermarketCombination)
    {
        this.supermarketCombination = supermarketCombination;
    }

    @Override
    public void onResponse(JSONObject result)
    {
        Log.i(TAG, "onResponse: Result= " + result.toString());
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            ILPResponse ILPresult = mapper.readValue(result.toString(), ILPResponse.class);
            if (ILPresult.getObjectValue() < Computation.bestMinValue)
            {
                Computation.bestMinValue = ILPresult.getObjectValue();
                for (int i = 0; i < ILPresult.getGramsToBuy().size(); i++)
                    Collections.copy(Computation.gramsToBuyBestSolution.get(i), ILPresult.getGramsToBuy().get(i));
                Computation.bestSupermarketCombination.clear();
                Computation.bestSupermarketCombination.addAll(supermarketCombination);
            }
            Computation.pendingRequests--;
            if(Computation.pendingRequests == 0)Computation.getBestCombination4();

        }
        catch (JsonParseException e)
        {
            e.printStackTrace();
        }
        catch (JsonMappingException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
