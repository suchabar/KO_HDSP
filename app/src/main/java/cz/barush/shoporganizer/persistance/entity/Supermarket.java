package cz.barush.shoporganizer.persistance.entity;

import android.location.Location;

import java.util.HashMap;

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
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
@EqualsAndHashCode(exclude={"location", "distance", "priceList"})
public class Supermarket
{
    Location location;
    String name;
    int distance;
    HashMap<Food, Integer> priceList;
}
