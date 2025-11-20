package com.despotisme.factories;

import com.despotisme.entities.Settler;
import com.despotisme.entities.City;

public class CityFactoryImpl implements CityFactory {
    public CityFactoryImpl() {}

    @Override
    public City createCity(String cityName, float x, float y) {
        return  new City(cityName, x, y);
    }
}
