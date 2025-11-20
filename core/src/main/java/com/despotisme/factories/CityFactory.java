package com.despotisme.factories;

import com.despotisme.entities.City;
import com.despotisme.entities.Settler;

public interface CityFactory {
    City createCity(String cityName, float x, float y);
}
