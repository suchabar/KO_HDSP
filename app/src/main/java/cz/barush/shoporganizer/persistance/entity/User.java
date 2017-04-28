package cz.barush.shoporganizer.persistance.entity;

import android.location.Location;

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
public class User
{
    String name;
    Gender gender;
    int age;
    int height;
    int weight;
    ActivityIntensity activity = ActivityIntensity.LIGHT_INTENSITY;

    int basalEnergy;
    int basalProteins;
    int basalCarbs;
    int basalFats;
    int basalFibres;

    int eatenEnergy;
    int eatenProteins;
    int eatenCarbs;
    int eatenFats;
    int eatenFibres;

    //In minutes
    int maxTimeSpentOnShopping = 60;
    Location homeLocation;
    Location currentLocation;


    public enum Gender{
        MAN,
        WOMAN
    }

    @Getter
    @AllArgsConstructor
    public enum ActivityIntensity{
        NO_ACTIVITY(1.2),
        LIGHT_INTENSITY(1.375),
        AVERAGE_INTENSITY(1.55),
        HIGH_INTENSITY(1.725),
        EXTRA_HIGH_INTENSITY(1.9);

        private final double multiplicativeConstant;
    }

}
