package cz.barush.shoporganizer.utils;

import android.location.Location;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.barush.shoporganizer.persistance.entity.Food;
import cz.barush.shoporganizer.persistance.entity.Supermarket;
import cz.barush.shoporganizer.persistance.entity.User;
import cz.barush.shoporganizer.services.AppController;
import gurobi.GRB;
import gurobi.GRBCallback;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import static cz.barush.shoporganizer.services.AppConfig.GOOGLE_BROWSER_API_KEY;
import static cz.barush.shoporganizer.services.AppConfig.TAG;

/**
 * mergedPriceList - 1. row - GRAINS, 2. row - VEGETABLES, FRUIT, 3. row - DIARY, MEAT, 4. row - OILS, SWEET
 */

public class Computation
{
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
    public static GRBModel model;
    public static List<Integer> bestSupermarketCombination = new ArrayList<>();
    static List<Supermarket> supermarketsNearby;
    static GRBVar[][] edges;
    static int[][] distances;

    public Computation(GRBVar[][] newEdges)
    {
        this.edges = newEdges;
    }

    public static List<List<Integer>> solveStiglersProblem(List<List<Food>> nutritionTable, int[] balancedNutrients) throws GRBException
    {
        GRBEnv env = new GRBEnv();
        model = new GRBModel(env);
        List<List<GRBVar>> X = new ArrayList<>();
        //addVar(lowerBound, upperBound, objectiveCoeff, variableType, name)
        for (int i = 0; i < X.size(); i++)
        {
            for (int j = 0; j < X.get(i).size(); j++)
            {
                if (!nutritionTable.get(i).get(j).isSelected())
                    X.get(i).add(j, model.addVar(0, 0, 1, GRB.INTEGER, "x_" + i + "_" + j));
                else X.get(i).add(j, model.addVar(0, 50000, 1, GRB.INTEGER, "x_" + i + "_" + j));
            }
        }

        model.update();

        //OBJECTIVE
        GRBLinExpr obj = new GRBLinExpr();
        for (int i = 0; i < X.size(); i++)
            for (int j = 0; j < X.get(i).size(); j++)
                obj.addTerm(nutritionTable.get(i).get(j).getMergedPrice(), X.get(i).get(j));
        model.setObjective(obj, GRB.MINIMIZE);

        //CONDITIONS
        GRBLinExpr cons1;
        double[] ratioBalancesLowerBound = {0.35, 0.25, 0.15, 0.05};
        double[] ratioBalancesUpperBound = {0.45, 0.35, 0.25, 0.15};

        //GO THROUGH ALL FOOD - Iterate i. set of food
        for (int i = 0; i < nutritionTable.size(); i++)
        {
            //Energy
            cons1 = new GRBLinExpr();
            for (int j = 0; j < nutritionTable.get(i).size(); j++)
            {
                cons1.addTerm(nutritionTable.get(i).get(j).getEnergy(), X.get(i).get(j));
            }
            model.addConstr(cons1, GRB.GREATER_EQUAL, ratioBalancesLowerBound[i] * balancedNutrients[0], "balanceG_Energy_i_" + i);
            model.addConstr(cons1, GRB.LESS_EQUAL, ratioBalancesUpperBound[i] * balancedNutrients[0], "balanceL_Energy_i_" + i);

            //Carbs
            cons1 = new GRBLinExpr();
            for (int j = 0; j < nutritionTable.get(i).size(); j++)
            {
                cons1.addTerm(nutritionTable.get(i).get(j).getCarbs(), X.get(i).get(j));
            }
            model.addConstr(cons1, GRB.GREATER_EQUAL, ratioBalancesLowerBound[i] * balancedNutrients[1], "balanceG_Carbs_i_" + i);
            model.addConstr(cons1, GRB.LESS_EQUAL, ratioBalancesUpperBound[i] * balancedNutrients[1], "balanceL_Carbs_i_" + i);

            //Proteins
            cons1 = new GRBLinExpr();
            for (int j = 0; j < nutritionTable.get(i).size(); j++)
            {
                cons1.addTerm(nutritionTable.get(i).get(j).getProteins(), X.get(i).get(j));
            }
            model.addConstr(cons1, GRB.GREATER_EQUAL, ratioBalancesLowerBound[i] * balancedNutrients[2], "balanceG_Proteins_i_" + i);
            model.addConstr(cons1, GRB.LESS_EQUAL, ratioBalancesUpperBound[i] * balancedNutrients[2], "balanceL_Proteins_i_" + i);

            //Fat
            cons1 = new GRBLinExpr();
            for (int j = 0; j < nutritionTable.get(i).size(); j++)
            {
                cons1.addTerm(nutritionTable.get(i).get(j).getFats(), X.get(i).get(j));
            }
            model.addConstr(cons1, GRB.GREATER_EQUAL, ratioBalancesLowerBound[i] * balancedNutrients[3], "balanceG_Fat_i_" + i);
            model.addConstr(cons1, GRB.LESS_EQUAL, ratioBalancesUpperBound[i] * balancedNutrients[3], "balanceL_Fat_i_" + i);

            //Fibres
            cons1 = new GRBLinExpr();
            for (int j = 0; j < nutritionTable.get(i).size(); j++)
            {
                cons1.addTerm(nutritionTable.get(i).get(j).getFibres(), X.get(i).get(j));
            }
            model.addConstr(cons1, GRB.GREATER_EQUAL, ratioBalancesLowerBound[i] * balancedNutrients[4], "balanceG_Fibres_i_" + i);
            model.addConstr(cons1, GRB.LESS_EQUAL, ratioBalancesUpperBound[i] * balancedNutrients[4], "balanceL_Fibres_i_" + i);
        }

        // Solve the model.
        model.optimize();

        List<List<Integer>> gramsToBuy = new ArrayList<>();
        for (int i = 0; i < X.size(); i++)
            for (int j = 0; j < X.get(i).size(); j++)
                gramsToBuy.get(i).set(j, ((int) X.get(i).get(j).get(GRB.DoubleAttr.X)));

        return gramsToBuy;
    }

    public static List<List<Integer>> generateAllSupermarketSubsets(Supermarket[] supermarkets)
    {
        List<List<Integer>> uniqueSets = new ArrayList<>();
        for (int i = 0; i < (2 * supermarkets.length) - 1; i++)
        {
            int rank = i;
            List<Integer> newSet = new ArrayList<>();
            for (int j = supermarkets.length - 1; j >= 0; j--)
            {
                if (rank % 2 == 1)
                {
                    if(!newSet.contains(rank))newSet.add(rank);
                }
                rank = (int) Math.floor((double) rank / 2);
            }
        }
        return uniqueSets;
    }

    public static List<List<Food>> mergeSupermarketsPriceLists(List<Integer> supermarkets)
    {
        HashMap<String, List<Integer>> preMergedPriceList = new HashMap<>(32);

        List<List<Food>> mergedPriceList = StaticPool.initializeFood();
        for (int i = 0; i < supermarkets.size(); i++)
        {
            HashMap<Food, Integer> prices = supermarketsNearby.get(supermarkets.get(i)).getPriceList();
            for (Food f : prices.keySet()) preMergedPriceList.get(f.getName()).add(prices.get(f));
        }
        for (int i = 0; i < mergedPriceList.size(); i++)
            for (int j = 0; j < mergedPriceList.get(i).size(); j++)
                mergedPriceList.get(i).get(j).setMergedPrice(Collections.min(preMergedPriceList.get(mergedPriceList.get(i).get(j).getName())));

        return mergedPriceList;
    }

    public static List<Integer> getBestCombination(List<Supermarket> supermarkets, Location currentLocation) throws GRBException
    {
        supermarketsNearby = supermarkets;
        User user = StaticPool.getInstance().user;
        setBRMEntities(user);
        int[] balancedNutrients = getBalancedNutrients(user);
        initializeDistances(supermarkets, currentLocation, user.getHomeLocation());

        List<List<Integer>> uniqueSets = generateAllSupermarketSubsets((Supermarket[]) supermarketsNearby.toArray());
        //List<List<Integer>> uniqueFeasibleSets = TSPThroughAllUniqueSubsets(uniqueSets);

        int bestMinValue = Integer.MAX_VALUE;
        for (List<Integer> s : uniqueSets)
        {
            List<List<Food>> nutritionTable = mergeSupermarketsPriceLists(s);
            List<List<Integer>> gramsToBuy = solveStiglersProblem(nutritionTable, balancedNutrients);
            int objectValue = (int) model.get(GRB.DoubleAttr.ObjVal);
            if (objectValue < bestMinValue)
            {
                bestMinValue = objectValue;
                for (int i = 0; i < gramsToBuy.size(); i++)
                    Collections.copy(gramsToBuyBestSolution.get(i), gramsToBuy.get(i));
                bestSupermarketCombination.clear();
                bestSupermarketCombination.addAll(s);
            }
        }
        return bestSupermarketCombination;
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
        user.setBasalCarbs((int) ((user.getBasalFibres() * FIBRES_RATIO) / FIBRES_KJ_G));
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

                Location location1 = supermarkets.get(i).getLocation();
                Location location2 = supermarkets.get(j).getLocation();
                if (i == 0) location1 = currentLocation;
                if (i == 1) location1 = home;
                if (j == 0) location2 = currentLocation;
                if (j == 1) location2 = home;

                computeCommutingTime(location1, location2, i, j);
            }
        }
    }

    private static void computeCommutingTime(Location location1, Location location2, final int i, final int j)
    {
        String url = getDirectionsUrl(new LatLng(location1.getLatitude(), location1.getLongitude()),
                new LatLng(location2.getLatitude(), location2.getLongitude()), new ArrayList<LatLng>());
        JsonObjectRequest request = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>()
                {
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
                            distances[i][j] = Math.round(Integer.valueOf(distOb.getString("text").split(" ")[0])*1000);
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

    //TSP
//    private static List<List<Supermarket>> TSPThroughAllUniqueSubsets(List<List<Supermarket>> uniqueSets)
//    {
//
//    }
//
//    protected static int[] findSubTour(double[][] sol)
//    {
//        int n = sol.length;
//        boolean[] seen = new boolean[n];
//        int[] tour = new int[n];
//        int bestind, bestlen;
//        int i, node, len, start;
//
//        for (i = 0; i < n; i++) seen[i] = false;
//
//        start = 0;
//        bestlen = n + 1;
//        bestind = -1;
//        node = 0;
//        while (start < n)
//        {
//            for (node = 0; node < n; node++) if (!seen[node]) break;
//            if (node == n) break;
//            for (len = 0; len < n; len++)
//            {
//                tour[start + len] = node;
//                seen[node] = true;
//                for (i = 0; i < n; i++)
//                {
//                    if (sol[node][i] > 0.5 && !seen[i])
//                    {
//                        node = i;
//                        break;
//                    }
//                }
//                if (i == n)
//                {
//                    len++;
//                    if (len < bestlen)
//                    {
//                        bestlen = len;
//                        bestind = start;
//                    }
//                    start += len;
//                    break;
//                }
//            }
//        }
//
//        int result[] = new int[bestlen];
//        for (i = 0; i < bestlen; i++) result[i] = tour[bestind + i];
//        return result;
//    }
//
//    protected int[] solveTsp(List<Integer> indecesOfSupermarkets) throws GRBException
//    {
//        int[] optimalTour = new int[indecesOfSupermarkets.size() + 2];
//        GRBEnv env = new GRBEnv();
//        env.set(GRB.IntParam.LazyConstraints, 1);
//        GRBModel model = new GRBModel(env);
//
//        edges = new GRBVar[optimalTour.length + 1][optimalTour.length + 1];
//        for (int i = 0; i < optimalTour.length + 1; i++)
//        {
//            for (int j = 0; j < optimalTour.length + 1; j++)
//            {
//                edges[i][j] = model.addVar(0.0, 1.0, 0, GRB.BINARY, "x" + i + "_" + j);
//            }
//        }
//
//        model.update();
//
//        //OBJECTIVE
//        GRBLinExpr obj = new GRBLinExpr();
//        for (int i = 0; i < optimalTour.length + 1; i++)
//            for (int j = 0; j < optimalTour.length + 1; j++)
//                obj.addTerm(distances[i][j], edges[i][j]);
//        model.setObjective(obj, GRB.MINIMIZE);
//
//
//        // Degree-2 constraints
//        for (int i = 0; i < optimalTour.length + 1; i++)
//        {
//            GRBLinExpr expr = new GRBLinExpr();
//            for (int j = 0; j < optimalTour.length + 1; j++) expr.addTerm(1.0, edges[i][j]);
//            model.addConstr(expr, GRB.EQUAL, 1, "deg1_" + String.valueOf(i));
//        }
//
//        for (int i = 0; i < optimalTour.length + 1; i++)
//        {
//            GRBLinExpr expr = new GRBLinExpr();
//            for (int j = 0; j < optimalTour.length + 1; j++) expr.addTerm(1.0, edges[j][i]);
//            model.addConstr(expr, GRB.EQUAL, 1, "deg2_" + String.valueOf(i));
//        }
//
//        // Forbid edge from node back to itself
//        for (int i = 0; i < optimalTour.length + 1; i++) edges[i][i].set(GRB.DoubleAttr.UB, 0.0);
//
//        model.setCallback(new Computation(edges));
//        model.optimize();
//
//        if (model.get(GRB.IntAttr.SolCount) > 0)
//        {
//            int[] tour = findSubTour(model.get(GRB.DoubleAttr.X, edges));
//            assert tour.length == optimalTour.length + 1;
//
//            //System.out.print("Tour: ");
//            int[] tourWithoutDummyNode = new int[tour.length - 1];
//            ArrayList<Integer> tempList = new ArrayList<>();
//            for (int i = 0; i < tour.length; i++) tempList.add(tour[i]);
//            int startIndex = tempList.indexOf(numberOfStripes);
//            int index = 0;
//            for (int i = startIndex + 1; i < tour.length; i++)
//            {
//                tourWithoutDummyNode[index++] = tour[i];
//                //System.out.print(tour[i] + " ");
//            }
//            for (int i = 0; i < startIndex; i++)
//            {
//                tourWithoutDummyNode[index++] = tour[i];
//                //System.out.print(tour[i] + " ");
//            }
//            //System.out.println();
//            optimalTour = Arrays.copyOf(tourWithoutDummyNode, tourWithoutDummyNode.length);
//        }
//
//        // Dispose of model and environment
//        model.dispose();
//        env.dispose();
//
//        return optimalTour;
//    }
//
//    @Override
//    protected void callback()
//    {
//        try
//        {
//            if (where == GRB.CB_MIPSOL)
//            {
//                // Found an integer feasible solution - does it visit every node?
//                int n = edges.length;
//                int[] tour = findSubTour(getSolution(edges));
//                if (tour.length < n)
//                {
//                    // Add subtour elimination constraint
//                    GRBLinExpr expr = new GRBLinExpr();
//                    for (int i = 0; i < tour.length; i++)
//                        expr.addTerm(1.0, edges[tour[i]][tour[(i + 1) % tour.length]]);
//                    addLazy(expr, GRB.LESS_EQUAL, tour.length - 1);
//                }
//            }
//        } catch (GRBException e)
//        {
//            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
}
