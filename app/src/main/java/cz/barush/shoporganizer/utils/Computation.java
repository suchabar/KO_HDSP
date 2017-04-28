package cz.barush.shoporganizer.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.barush.shoporganizer.persistance.entity.Food;
import cz.barush.shoporganizer.persistance.entity.Supermarket;
import cz.barush.shoporganizer.persistance.entity.User;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

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
    public static Set<Supermarket> bestSupermarketCombination = new HashSet<>();
    public static GRBModel model;
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
                if(!nutritionTable.get(i).get(j).isSelected())X.get(i).add(j, model.addVar(0, 0, 1, GRB.INTEGER, "x_" + i + "_" + j));
                else X.get(i).add(j, model.addVar(0, 50000, 1, GRB.INTEGER, "x_" + i + "_" + j));
            }
        }

        model.update();

        //OBJECTIVE
        GRBLinExpr obj = new GRBLinExpr();
        for (int i = 0; i < X.size(); i++)for (int j = 0; j < X.get(i).size(); j++) obj.addTerm(nutritionTable.get(i).get(j).getMergedPrice(), X.get(i).get(j));
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
            model.addConstr(cons1, GRB.GREATER_EQUAL, ratioBalancesLowerBound[i]*balancedNutrients[0], "balanceG_Energy_i_" + i);
            model.addConstr(cons1, GRB.LESS_EQUAL, ratioBalancesUpperBound[i]*balancedNutrients[0], "balanceL_Energy_i_" + i );

            //Carbs
            cons1 = new GRBLinExpr();
            for (int j = 0; j < nutritionTable.get(i).size(); j++)
            {
                cons1.addTerm(nutritionTable.get(i).get(j).getCarbs(), X.get(i).get(j));
            }
            model.addConstr(cons1, GRB.GREATER_EQUAL, ratioBalancesLowerBound[i]*balancedNutrients[1], "balanceG_Carbs_i_" + i);
            model.addConstr(cons1, GRB.LESS_EQUAL, ratioBalancesUpperBound[i]*balancedNutrients[1], "balanceL_Carbs_i_" + i );

            //Proteins
            cons1 = new GRBLinExpr();
            for (int j = 0; j < nutritionTable.get(i).size(); j++)
            {
                cons1.addTerm(nutritionTable.get(i).get(j).getProteins(), X.get(i).get(j));
            }
            model.addConstr(cons1, GRB.GREATER_EQUAL, ratioBalancesLowerBound[i]*balancedNutrients[2], "balanceG_Proteins_i_" + i);
            model.addConstr(cons1, GRB.LESS_EQUAL, ratioBalancesUpperBound[i]*balancedNutrients[2], "balanceL_Proteins_i_" + i );

            //Fat
            cons1 = new GRBLinExpr();
            for (int j = 0; j < nutritionTable.get(i).size(); j++)
            {
                cons1.addTerm(nutritionTable.get(i).get(j).getFats(), X.get(i).get(j));
            }
            model.addConstr(cons1, GRB.GREATER_EQUAL, ratioBalancesLowerBound[i]*balancedNutrients[3], "balanceG_Fat_i_" + i);
            model.addConstr(cons1, GRB.LESS_EQUAL, ratioBalancesUpperBound[i]*balancedNutrients[3], "balanceL_Fat_i_" + i );

            //Fibres
            cons1 = new GRBLinExpr();
            for (int j = 0; j < nutritionTable.get(i).size(); j++)
            {
                cons1.addTerm(nutritionTable.get(i).get(j).getFibres(), X.get(i).get(j));
            }
            model.addConstr(cons1, GRB.GREATER_EQUAL, ratioBalancesLowerBound[i]*balancedNutrients[4], "balanceG_Fibres_i_" + i);
            model.addConstr(cons1, GRB.LESS_EQUAL, ratioBalancesUpperBound[i]*balancedNutrients[4], "balanceL_Fibres_i_" + i );
        }

        // Solve the model.
        model.optimize();

        List<List<Integer>> gramsToBuy = new ArrayList<>();
        for (int i = 0; i < X.size(); i++)for (int j = 0; j < X.get(i).size(); j++)
            gramsToBuy.get(i).set(j, ((int) X.get(i).get(j).get(GRB.DoubleAttr.X)));

        return gramsToBuy;
    }

    public static Set<Set<Supermarket>> generateAllSupermarketSubsets(Supermarket[] supermarkets)
    {
        Set<Set<Supermarket>> uniqueSets = new HashSet<>();
        for (int i = 0; i < (2*supermarkets.length) - 1; i++)
        {
            int rank = i;
            Set<Supermarket> newSet = new HashSet<>();
            for (int j = supermarkets.length-1; j >= 0; j--)
            {
                if(rank % 2 == 1)newSet.add(supermarkets[rank]);
                rank = (int) Math.floor((double) rank / 2);
            }
        }
        return uniqueSets;
    }

    //udelat metodu TSP na zjisteni, jestli set supermarketu je feasible pro user maxLimit
    //experiments - pro ruzny casti prahy(mnoziny)
    //kvantifikatory do pdf pro 'i'...a pro F konzistentni udelat
    //prezentace - opt. podproblemy, pribeh
    public static List<List<Food>> mergeSupermarketsPriceLists(Set<Supermarket> supermarkets)
    {
        HashMap<String,List<Integer>> preMergedPriceList = new HashMap<>(32);

        List<List<Food>>  mergedPriceList = StaticPool.initializeFood();
        for (int i = 0; i < supermarkets.size(); i++)
        {
            HashMap<Food, Integer> prices = supermarkets.iterator().next().getPriceList();
            for (Food f : prices.keySet())preMergedPriceList.get(f.getName()).add(prices.get(f));
        }
        for (int i = 0; i < mergedPriceList.size(); i++)for (int j = 0; j < mergedPriceList.get(i).size(); j++)
           mergedPriceList.get(i).get(j).setMergedPrice(Collections.min(preMergedPriceList.get(mergedPriceList.get(i).get(j).getName())));

        return mergedPriceList;
    }

    public static Set<Supermarket> getBestCombination(List<Supermarket> supermarketsNearby) throws GRBException
    {
        User user = StaticPool.getInstance().user;
        setBRMEntities(user);

        int[] balancedNutrients = new int[5];
        balancedNutrients[0] = user.getBasalEnergy() - user.getEatenEnergy();
        balancedNutrients[1] = user.getBasalCarbs() - user.getEatenCarbs();
        balancedNutrients[2] = user.getBasalProteins() - user.getEatenProteins();
        balancedNutrients[3] = user.getBasalFats() - user.getEatenFats();
        balancedNutrients[4] = user.getBasalFibres() - user.getEatenFibres();

        Set<Set<Supermarket>> uniqueSets = generateAllSupermarketSubsets((Supermarket[]) supermarketsNearby.toArray());
        int bestMinValue = Integer.MAX_VALUE;
        for(Set<Supermarket> s : uniqueSets)
        {
            List<List<Food>> nutritionTable = mergeSupermarketsPriceLists(s);
            List<List<Integer>> gramsToBuy = solveStiglersProblem(nutritionTable, balancedNutrients);
            int objectValue = (int) model.get(GRB.DoubleAttr.ObjVal);
            if(objectValue < bestMinValue)
            {
                bestMinValue = objectValue;
                for (int i = 0; i < gramsToBuy.size(); i++)Collections.copy(gramsToBuyBestSolution.get(i), gramsToBuy.get(i));
                bestSupermarketCombination.clear();
                bestSupermarketCombination.addAll(s);
            }
        }
        return bestSupermarketCombination;
    }

    public static void setBRMEntities(User user)
    {
        if(user.getGender() == User.Gender.MAN)user.setBasalEnergy((int)((10 * user.getWeight()) + (6.25 * user.getHeight()) - (5 * user.getAge()) + 5));
        else user.setBasalEnergy((int)((10 * user.getWeight()) + (6.25 * user.getHeight()) - (5 * user.getAge()) - 161));
        user.setBasalEnergy((int)(user.getBasalEnergy() * user.getActivity().getMultiplicativeConstant() * KCAL_TO_KJ));
        user.setBasalCarbs((int)((user.getBasalEnergy() * CARBS_RATIO) / CARBS_KJ_G));
        user.setBasalProteins((int)((user.getBasalEnergy() * PROTEINS_RATIO) / PROTEINS_KJ_G));
        user.setBasalFats((int)((user.getBasalEnergy() * FAT_RATIO) / FAT_KJ_G));
        user.setBasalCarbs((int)((user.getBasalFibres() * FIBRES_RATIO) / FIBRES_KJ_G));
    }
}
