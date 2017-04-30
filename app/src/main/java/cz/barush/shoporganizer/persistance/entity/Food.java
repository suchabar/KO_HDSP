package cz.barush.shoporganizer.persistance.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
/**
 * Created by Barbora on 21-Apr-17.
 */

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class Food
{
    String name;
    FoodGroup category;
    boolean isSelected = true;
    double mergedPrice;
    int gramsToBuy;
    double energy;
    double carbs;
    double proteins;
    double fats;
    double fibres;

    @Getter
    @AllArgsConstructor
    public enum FoodGroup{
        GRAINS("Whole grains"),
        FRUIT_VEGETABLE("Fruit and vegetable"),
        DIARY_MEAT("Diary products and meat"),
        FATS_SWEETS("Fats, oils and sweets");

        private final String name;
    }
}
