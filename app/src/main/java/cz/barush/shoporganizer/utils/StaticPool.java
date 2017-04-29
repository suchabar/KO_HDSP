package cz.barush.shoporganizer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import cz.barush.shoporganizer.persistance.entity.Food;
import cz.barush.shoporganizer.persistance.entity.Supermarket;
import cz.barush.shoporganizer.persistance.entity.User;
import lombok.NoArgsConstructor;

/**
 * Created by Barbora on 26-Nov-16.
 */
@NoArgsConstructor
public class StaticPool
{
    private static StaticPool mInstance;
    public static User user;
    private Context mContext;
    private SharedPreferences mMyPreferences;
    public static List<Food> grains;
    public static List<Food> fruitVegetable;
    public static List<Food> diaryMeat;
    public static List<Food> oilSweet;


    public static StaticPool getInstance()
    {
        if (mInstance == null) mInstance = new StaticPool();
        return mInstance;
    }

    public static HashMap<Food, Integer> initializePriceList()
    {
        HashMap<Food, Integer> priceList = new HashMap<>();
        if(grains == null)initializeFood();

        int[] grainsPrices = {61, 76, 50, 20, 39};
        int[] fruitVegetablePrices = {38, 19, 86, 18, 43, 27, 26, 29, 48, 115};
        int[] diaryMeatPrices = {431, 120, 27, 618, 138, 108, 134, 182, 81, 300};
        int[] oilSweetPrices = {125, 150, 293, 445, 124, 104, 24, 325};

        for (int i = 0; i < grains.size(); i++)priceList.put(grains.get(i), grainsPrices[i] +  (int)(Math.random() * (0.5*grainsPrices[i])));
        for (int i = 0; i < fruitVegetable.size(); i++)priceList.put(fruitVegetable.get(i), fruitVegetablePrices[i] + (int)(Math.random() * (0.5*fruitVegetablePrices[i])));
        for (int i = 0; i < diaryMeat.size(); i++)priceList.put(diaryMeat.get(i), diaryMeatPrices[i] + (int)(Math.random() * (0.5*diaryMeatPrices[i])));
        for (int i = 0; i < oilSweet.size(); i++)priceList.put(oilSweet.get(i), oilSweetPrices[i] + (int)(Math.random() * (0.5*oilSweetPrices[i])));

        return priceList;
    }

    public static List<List<Food>> initializeFood()
    {
        grains = new ArrayList<>();
        fruitVegetable = new ArrayList<>();
        diaryMeat = new ArrayList<>();
        oilSweet = new ArrayList<>();

        String[] grainsName = {"Protein pasta", "Rice", "Pasta", "Bread", "Potatoes", "Fries"};
        String[] fruitVegetableName = {"Tomato", "Cucumber", "Pepper", "Beans", "Brocoli", "Banana", "Apple", "Orange", "Kiwi", "Strawberry" };
        String[] diaryMeatName = {"Goat cheese", "Cottage cheese", "White yoghurt", "Parmasan", "Eidam 30%", "Chicken", "Salmon", "Scampi", "Pork", "Beef"};
        String[] oilSweetName = {"Olive oil", "Butter", "Nuts", "Dark chocolate", "Olives", "White Wine", "Beer", "Ice cream"};

        int[] grainsCarbs= {61, 76, 50, 20, 39};
        int[] fruitVegetableCarbs = {38, 19, 86, 18, 43, 27, 26, 29, 48, 115};
        int[] diaryMeatCarbs= {431, 120, 27, 618, 138, 108, 134, 182, 81, 300};
        int[] oilSweetCarbs = {125, 150, 293, 445, 124, 104, 24, 325};

        int[] grainsProteins = {61, 76, 50, 20, 39};
        int[] fruitVegetableProteins = {38, 19, 86, 18, 43, 27, 26, 29, 48, 115};
        int[] diaryMeatProteins = {431, 120, 27, 618, 138, 108, 134, 182, 81, 300};
        int[] oilSweetProteins = {125, 150, 293, 445, 124, 104, 24, 325};

        int[] grainsFat = {61, 76, 50, 20, 39};
        int[] fruitVegetableFat= {38, 19, 86, 18, 43, 27, 26, 29, 48, 115};
        int[] diaryMeatFat = {431, 120, 27, 618, 138, 108, 134, 182, 81, 300};
        int[] oilSweetFat= {125, 150, 293, 445, 124, 104, 24, 325};

        int[] grainsFibres = {61, 76, 50, 20, 39};
        int[] fruitVegetableFibres= {38, 19, 86, 18, 43, 27, 26, 29, 48, 115};
        int[] diaryMeatFibres= {431, 120, 27, 618, 138, 108, 134, 182, 81, 300};
        int[] oilSweetFibres = {125, 150, 293, 445, 124, 104, 24, 325};

        for (int i = 0; i < grainsName.length; i++)grains.add(new Food()
                .setName(grainsName[i])
                .setCategory(Food.FoodGroup.GRAINS)
                .setCarbs(grainsCarbs[i])
                .setProteins(grainsProteins[i])
                .setFats(grainsFat[i])
                .setFibres(grainsFibres[i]));
        for (int i = 0; i < fruitVegetableName.length; i++)fruitVegetable.add(new Food()
                .setName(fruitVegetableName[i])
                .setCategory(Food.FoodGroup.FRUIT_VEGETABLE)
                .setCarbs(grainsCarbs[i])
                .setProteins(grainsProteins[i])
                .setFats(grainsFat[i])
                .setFibres(grainsFibres[i]));
        for (int i = 0; i < diaryMeatName.length; i++)diaryMeat.add(new Food()
                .setName(diaryMeatName[i])
                .setCategory(Food.FoodGroup.DIARY_MEAT)
                .setCarbs(grainsCarbs[i])
                .setProteins(grainsProteins[i])
                .setFats(grainsFat[i])
                .setFibres(grainsFibres[i]));
        for (int i = 0; i < oilSweetName.length; i++)oilSweet.add(new Food()
                .setName(oilSweetName[i])
                .setCategory(Food.FoodGroup.FATS_SWEETS)
                .setCarbs(grainsCarbs[i])
                .setProteins(grainsProteins[i])
                .setFats(grainsFat[i])
                .setFibres(grainsFibres[i]));

        List<List<Food>> allFood = new ArrayList<>();
        allFood.add(grains);
        allFood.add(fruitVegetable);
        allFood.add(diaryMeat);
        allFood.add(oilSweet);
        return allFood;
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
