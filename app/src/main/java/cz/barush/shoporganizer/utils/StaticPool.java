package cz.barush.shoporganizer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.barush.shoporganizer.persistance.entity.Food;
import cz.barush.shoporganizer.persistance.entity.User;
import lombok.NoArgsConstructor;

/**
 * Created by Barbora on 26-Nov-16.
 */
@NoArgsConstructor
public class StaticPool
{
    public static User user = new User();
    public static List<List<Food>> allFood;
    public static List<Food> grains;
    public static List<Food> fruitVegetable;
    public static List<Food> diaryMeat;
    public static List<Food> oilSweet;
    private static StaticPool mInstance;
    private Context mContext;
    private SharedPreferences mMyPreferences;

    public static StaticPool getInstance()
    {
        if (mInstance == null) mInstance = new StaticPool();
        return mInstance;
    }

    //Price per 1 gram
    public static HashMap<Food, Double> initializePriceList()
    {
        HashMap<Food, Double> priceList = new HashMap<>();
        if (grains == null) initializeFood();

        double[] grainsPrices = {0.068, 0.061, 0.076, 0.050, 0.020, 0.039};
        double[] fruitVegetablePrices = {0.038, 0.019, 0.086, 0.018, 0.043, 0.027, 0.026, 0.029, 0.048, 0.115};
        double[] diaryMeatPrices = {0.431, 0.120, 0.027, 0.618, 0.138, 0.108, 0.134, 0.182, 0.081, 0.300};
        double[] oilSweetPrices = {0.125, 0.150, 0.293, 0.445, 0.124, 0.104, 0.024, 0.325};

        for (int i = 0; i < grains.size(); i++)
            priceList.put(grains.get(i), grainsPrices[i] + (Math.random() * (0.5 * grainsPrices[i])));
        for (int i = 0; i < fruitVegetable.size(); i++)
            priceList.put(fruitVegetable.get(i), fruitVegetablePrices[i] + (Math.random() * (0.5 * fruitVegetablePrices[i])));
        for (int i = 0; i < diaryMeat.size(); i++)
            priceList.put(diaryMeat.get(i), diaryMeatPrices[i] + (Math.random() * (0.5 * diaryMeatPrices[i])));
        for (int i = 0; i < oilSweet.size(); i++)
            priceList.put(oilSweet.get(i), oilSweetPrices[i] + (Math.random() * (0.5 * oilSweetPrices[i])));

        return priceList;
    }

    //Energy, Carbs, Proteins, Fat, Fibres values are PER GRAM
    public static void initializeFood()
    {
        grains = new ArrayList<>();
        fruitVegetable = new ArrayList<>();
        diaryMeat = new ArrayList<>();
        oilSweet = new ArrayList<>();

        String[] grainsName = {"Protein pasta", "Rice", "Pasta", "Bread", "Potatoes", "Fries"};
        String[] fruitVegetableName = {"Tomato", "Cucumber", "Pepper", "Beans", "Brocoli", "Banana", "Apple", "Orange", "Kiwi", "Strawberry"};
        String[] diaryMeatName = {"Goat cheese", "Cottage cheese", "White yoghurt", "Parmasan", "Eidam 30%", "Chicken", "Salmon", "Scampi", "Pork", "Beef"};
        String[] oilSweetName = {"Olive oil", "Butter", "Nuts", "Dark chocolate", "Olives", "White Wine", "Beer", "Ice cream"};

        double[] grainsEnergy = {16.27, 14.61, 14.9, 14.66, 2.8, 10.69};
        double[] fruitVegetableEnergy = {0.93, 0.59, 1.2, 12.98, 1.58, 3.94, 2.78, 2.08, 2.72, 1.47};
        double[] diaryMeatEnergy = {11.47, 4.56, 2.8, 16.29, 11, 4.61, 4.7, 3.91, 9.97, 5.23};
        double[] oilSweetEnergy = {37.5, 31.34, 25.11, 25.93, 5.81, 3.27, 1.55, 7.53};

        double[] grainsCarbs = {0.328, 0.7, 0.682, 0.711, 0.096, 0.336};
        double[] fruitVegetableCarbs = {0.041, 0.023, 0.05, 0.048, 0.057, 0.218, 0.14, 0.11, 0.139, 0.062};
        double[] diaryMeatCarbs = {0.014, 0.027, 0.045, 0.032, 0.018, 0, 0, 0.008, 0.001, 0};
        double[] oilSweetCarbs = {0.002, 0.006, 0.227, 0.253, 0.002, 0.01, 0.02, 0.25};

        double[] grainsProteins = {0.426, 0.08, 0.125, 0.113, 0.013, 0.039};
        double[] fruitVegetableProteins = {0.01, 0.008, 0.01, 0.19, 0.033, 0.012, 0.6, 0.009, 0.01, 0.008};
        double[] diaryMeatProteins = {0.21, 0.11, 0.037, 0.349, 0.27, 0.231, 0.2, 0.2, 0.18, 0.2};
        double[] oilSweetProteins = {0, 0.007, 0.169, 0.085, 0.012, 0, 0.002, 0.03};

        double[] grainsFat = {0.03, 0.022, 0.015, 0.015, 0.001, 0.11};
        double[] fruitVegetableFat = {0.002, 0.002, 0.01, 0.011, 0.002, 0.002, 0.002, 0.002, 0.002, 0.004};
        double[] diaryMeatFat = {0.19, 0.06, 0.038, 0.263, 0.157, 0.012, 0.035, 0.01, 0.177, 0.05};
        double[] oilSweetFat = {0.994, 0.826, 0.539, 0.522, 0.14, 0, 0, 0.05};

        double[] grainsFibres = {0.188, 0.044, 0.03, 0.032, 0.025, 0.026};
        double[] fruitVegetableFibres = {0.016, 0.009, 0.01, 0.01, 0.03, 0.02, 0.35, 0.031, 0.03, 0.018};
        double[] diaryMeatFibres = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        double[] oilSweetFibres = {0, 0, 0.1, 0.109, 0.023, 0, 0, 0.018};

        for (int i = 0; i < grainsName.length; i++)
            grains.add(new Food()
                    .setName(grainsName[i])
                    .setCategory(Food.FoodGroup.GRAINS)
                    .setEnergy(grainsEnergy[i])
                    .setCarbs(grainsCarbs[i])
                    .setProteins(grainsProteins[i])
                    .setFats(grainsFat[i])
                    .setFibres(grainsFibres[i]));
        for (int i = 0; i < fruitVegetableName.length; i++)
            fruitVegetable.add(new Food()
                    .setName(fruitVegetableName[i])
                    .setCategory(Food.FoodGroup.FRUIT_VEGETABLE)
                    .setEnergy(fruitVegetableEnergy[i])
                    .setCarbs(fruitVegetableCarbs[i])
                    .setProteins(fruitVegetableProteins[i])
                    .setFats(fruitVegetableFat[i])
                    .setFibres(fruitVegetableFibres[i]));
        for (int i = 0; i < diaryMeatName.length; i++)
            diaryMeat.add(new Food()
                    .setName(diaryMeatName[i])
                    .setCategory(Food.FoodGroup.DIARY_MEAT)
                    .setEnergy(diaryMeatEnergy[i])
                    .setCarbs(diaryMeatCarbs[i])
                    .setProteins(diaryMeatProteins[i])
                    .setFats(diaryMeatFat[i])
                    .setFibres(diaryMeatFibres[i]));
        for (int i = 0; i < oilSweetName.length; i++)
            oilSweet.add(new Food()
                    .setName(oilSweetName[i])
                    .setCategory(Food.FoodGroup.FATS_SWEETS)
                    .setEnergy(oilSweetEnergy[i])
                    .setCarbs(oilSweetCarbs[i])
                    .setProteins(oilSweetProteins[i])
                    .setFats(oilSweetFat[i])
                    .setFibres(oilSweetFibres[i]));

        allFood = new ArrayList<>();
        allFood.add(grains);
        allFood.add(fruitVegetable);
        allFood.add(diaryMeat);
        allFood.add(oilSweet);
    }

    public void Initialize(Context ctxt)
    {
        mContext = ctxt;
        mMyPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public void writePreference(String key, String value)
    {
        SharedPreferences.Editor e = mMyPreferences.edit();
        e.putString(key, value);
        e.commit();
    }

    public void writePreferenceInt(String key, int value)
    {
        SharedPreferences.Editor e = mMyPreferences.edit();
        e.putInt(key, value);
        e.commit();
    }

    public String readPreference(String key)
    {
        return mMyPreferences.getString(key, null);
    }

    public int readPreferenceInt(String key)
    {
        return mMyPreferences.getInt(key, -1);
    }
}
