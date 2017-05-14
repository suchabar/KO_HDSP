package cz.barush.shoporganizer.utils;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import cz.barush.shoporganizer.MapsResultActivity;
import cz.barush.shoporganizer.SelectionOfFood1Activity;
import cz.barush.shoporganizer.UserInfoActivity;
import cz.barush.shoporganizer.persistance.entity.Food;
import cz.barush.shoporganizer.persistance.entity.ILPInput;
import cz.barush.shoporganizer.persistance.entity.Supermarket;
import cz.barush.shoporganizer.persistance.entity.TSPResponse;
import cz.barush.shoporganizer.persistance.entity.User;
import cz.barush.shoporganizer.services.AppController;
import cz.barush.shoporganizer.services.MyErrorListener;
import cz.barush.shoporganizer.services.MyILPResponseListener;
import cz.barush.shoporganizer.services.MyResponseListener;
import cz.barush.shoporganizer.services.MyTSPResponseListener;

import static cz.barush.shoporganizer.services.AppConfig.GOOGLE_BROWSER_API_KEY;
import static cz.barush.shoporganizer.services.AppConfig.TAG;

/**
 * mergedPriceList - 1. row - GRAINS, 2. row - VEGETABLES, FRUIT, 3. row - DIARY, MEAT, 4. row - OILS, SWEET
 */

public class Computation
{
    private static final String BASE_URL = "http://172.16.231.16:8080/rest/api/";
    private static final int CARBS_KJ_G = 17;
    private static final int PROTEINS_KJ_G = 17;
    private static final int FAT_KJ_G = 38;
    private static final int FIBRES_KJ_G = 8;

    private static final double CARBS_RATIO = 0.4;
    private static final double PROTEINS_RATIO = 0.3;
    private static final double FAT_RATIO = 0.25;
    private static final double FIBRES_RATIO = 0.05;

    private static final double KCAL_TO_KJ = 4.2;

    public static List<List<Integer>> gramsToBuyBestSolution = new ArrayList<>();
    public static List<Integer> bestSupermarketCombination = new ArrayList<>();
    public static int[][] distances;
    static List<Supermarket> supermarketsNearby;
    public static Location currentLocationOfUser;
    public static Context currentContext;

    public static void getBestCombination(List<Supermarket> supermarkets, Location currentLocation, Context context)
    {
        currentLocationOfUser = currentLocation;
        currentContext = context;
        supermarketsNearby = supermarkets;
        User user = StaticPool.getInstance().user;
        if (user.getHomeLocation() == null)
        {
            Location home = new Location("Another provider");
            home.setLatitude(50.080542);
            home.setLongitude(14.392588);
            user.setHomeLocation(home);
        }
        initializeDistances(supermarkets, currentLocation, user.getHomeLocation());
    }

    public static void initializeDistances(List<Supermarket> supermarkets, Location currentLocation, Location home)
    {
        //+ currentLocation + locationOfHome
        distances = new int[supermarkets.size() + 3][supermarkets.size() + 3];
        for (int i = 0; i < distances.length; i++)
        {
            for (int j = 0; j < distances.length; j++)
            {
                //Self cycle
                if (i == j) continue;
                //Dummy node TO supermarkets, distances[i][j] = INT MAX
                if ((i == 2 && j > 2) || (j == 2 && i > 2))
                {
                    distances[i][j] = Integer.MAX_VALUE;
                    continue;
                }
                //Dummy node TO Home or CurrentLocation, distances[i][j] = 0
                else if ((i == 2 && j < 2) || (j == 2 && i < 2)) continue;

                Location location1;
                Location location2;
                if (i == 0) location1 = currentLocation;
                else if (i == 1) location1 = home;
                else location1 = supermarkets.get(i - 3).getLocation();

                if (j == 0) location2 = currentLocation;
                else if (j == 1) location2 = home;
                else location2 = supermarkets.get(j - 3).getLocation();

                computeCommutingTime(location1, location2, i, j);
            }
        }
    }
    public static int pendingRequests = 0;
    private static void computeCommutingTime(Location location1, Location location2, final int i, final int j)
    {
        String url = getDirectionsUrl(new LatLng(location1.getLatitude(), location1.getLongitude()),
                new LatLng(location2.getLatitude(), location2.getLongitude()), new ArrayList<LatLng>());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new MyResponseListener(i, j), new MyErrorListener());
        AppController.getInstance().addToRequestQueue(request);
        pendingRequests++;
    }

    public static String getDirectionsUrl(LatLng origin, LatLng dest, List<LatLng> markerPoints)
    {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Waypoints
        String waypoints = "";
        for (int i = 2; i < markerPoints.size(); i++)
        {
            LatLng point = (LatLng) markerPoints.get(i);
            if (i == 2)
                waypoints = "waypoints=";
            waypoints += point.latitude + "," + point.longitude + "|";
        }
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + waypoints;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters
                + "&key=" + GOOGLE_BROWSER_API_KEY;

        return url;
    }

    public static void getBestCombination2() throws JsonProcessingException, JSONException
    {
        List<List<Integer>> uniqueSets = generateAllSupermarketSubsets(supermarketsNearby.size());
        getAllFeasibleSets(uniqueSets);
    }

    private static void getAllFeasibleSets(List<List<Integer>> uniqueSets) throws JsonProcessingException, JSONException
    {
        String url = BASE_URL + "tsp-wtf";

        TSPResponse input = new TSPResponse()
                .setUniqueFeasibleSets(uniqueSets)
                .setDistances(distances)
                .setMaxDistance(StaticPool.user.getMaxDistance());

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(input);
        JSONObject jsonBody = new JSONObject(jsonString);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new MyTSPResponseListener(),new MyErrorListener());
        AppController.getInstance().addToRequestQueue(request);
    }

    public static int[] getBalancedNutrients(User user)
    {
        int[] balancedNutrients = new int[5];
        balancedNutrients[0] = user.getBasalEnergy() - user.getEatenEnergy();
        balancedNutrients[1] = user.getBasalCarbs() - user.getEatenCarbs();
        balancedNutrients[2] = user.getBasalProteins() - user.getEatenProteins();
        balancedNutrients[3] = user.getBasalFats() - user.getEatenFats();
        balancedNutrients[4] = user.getBasalFibres() - user.getEatenFibres();
        return balancedNutrients;
    }

    public static void setBRMEntities(User user)
    {
        if (user.getGender() == User.Gender.MAN)
            user.setBasalEnergy((int) ((10 * user.getWeight()) + (6.25 * user.getHeight()) - (5 * user.getAge()) + 5));
        else
            user.setBasalEnergy((int) ((10 * user.getWeight()) + (6.25 * user.getHeight()) - (5 * user.getAge()) - 161));
        user.setBasalEnergy((int) (user.getBasalEnergy() * user.getActivity().getMultiplicativeConstant() * KCAL_TO_KJ));
        user.setBasalCarbs((int) ((user.getBasalEnergy() * CARBS_RATIO) / CARBS_KJ_G));
        user.setBasalProteins((int) ((user.getBasalEnergy() * PROTEINS_RATIO) / PROTEINS_KJ_G));
        user.setBasalFats((int) ((user.getBasalEnergy() * FAT_RATIO) / FAT_KJ_G));
        user.setBasalFibres((int) ((user.getBasalFibres() * FIBRES_RATIO) / FIBRES_KJ_G));
    }

    public static List<List<Integer>> generateAllSupermarketSubsets(int numberOfNodes)
    {
        List<List<Integer>> uniqueSets = new ArrayList<>();
        for (int i = 0; i < 2 * numberOfNodes; i++)
        {
            int rank = i;
            List<Integer> newSet = new ArrayList<>();
            for (int j = numberOfNodes; j > 0; j--)
            {
                if (rank % 2 == 1)newSet.add(j - 1);
                rank = (int) Math.floor((double) rank / 2);
            }
            if(newSet.size() != 0)uniqueSets.add(newSet);
        }
        return uniqueSets;
    }

    public static void mergeSupermarketsPriceLists(List<Integer> supermarkets)
    {
        HashMap<String, List<Double>> preMergedPriceList = new HashMap<>(32);

        for (int i = 0; i < supermarkets.size(); i++)
        {
            HashMap<Food, Double> prices = supermarketsNearby.get(supermarkets.get(i)).getPriceList();
            for (Food f : prices.keySet())
            {
                if(preMergedPriceList.get(f.getName()) == null)preMergedPriceList.put(f.getName(), new ArrayList<Double>());
                preMergedPriceList.get(f.getName()).add(prices.get(f));
            }
        }
        for (int i = 0; i < StaticPool.allFood.size(); i++)
            for (int j = 0; j < StaticPool.allFood.get(i).size(); j++)
            {
                StaticPool.allFood.get(i).get(j).setMergedPrice(Collections.min(preMergedPriceList.get(
                        StaticPool.allFood.get(i).get(j).getName())));
            }
    }

    public static int bestMinValue;
    public static void getBestCombination3(List<List<Integer>> uniqueFeasibleSets) throws JsonProcessingException, JSONException
    {
        User user = StaticPool.getInstance().user;
        setBRMEntities(user);
        int[] balancedNutrients = getBalancedNutrients(user);

        bestMinValue = Integer.MAX_VALUE;
        pendingRequests = 0;
        for (List<Integer> s : uniqueFeasibleSets)
        {
            mergeSupermarketsPriceLists(s);
            getILPResultFromApi(StaticPool.allFood, balancedNutrients, s);
        }
    }

    public static void getILPResultFromApi(List<List<Food>> food, int[] balancedNutrition, List<Integer> s) throws JsonProcessingException, JSONException
    {
        String url = BASE_URL + "stigler";

        ILPInput input = new ILPInput().setAllFood(food).setBalancedNutrients(balancedNutrition);
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(input);
        JSONObject jsonBody = new JSONObject(jsonString);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new MyILPResponseListener(s),new MyErrorListener());
        AppController.getInstance().addToRequestQueue(request);
        pendingRequests++;;
    }

    public static void getBestCombination4()
    {
        LatLng origin = new LatLng(currentLocationOfUser.getLatitude(), currentLocationOfUser.getLongitude());
        LatLng dest = new LatLng(StaticPool.user.getHomeLocation().getLatitude(), StaticPool.user.getHomeLocation().getLongitude());
        ArrayList<LatLng> markerPoints = new ArrayList<>();
        for (int i = 0; i < bestSupermarketCombination.size(); i++)
        {
            Location loc = supermarketsNearby.get(bestSupermarketCombination.get(i)).getLocation();
            markerPoints.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
        }

        // Getting URL to the Google Directions API
        String url = Computation.getDirectionsUrl(origin, dest, markerPoints);
        JsonObjectRequest request = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject result)
                    {
                        Log.i(TAG, "onResponse: Result= " + result.toString());
                        try
                        {
                            StaticPool.polylines = parseDirectionsResult(result);
                            Intent intent = new Intent(currentContext, MapsResultActivity.class);
                            currentContext.startActivity(intent);
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
                    }
                });
        AppController.getInstance().addToRequestQueue(request);
    }

    private static List<List<HashMap<String, String>>> parseDirectionsResult(JSONObject result) throws JSONException
    {
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;
        try
        {
            DirectionsJSONParser parser = new DirectionsJSONParser();
            routes = parser.parse(result);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return routes;
    }
}
