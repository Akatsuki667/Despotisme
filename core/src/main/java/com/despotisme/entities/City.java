package com.despotisme.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.despotisme.entities.Location;
import com.despotisme.entities.Settler;

public class City extends Location {
    private String cityName;

    public City(String cityName, float x, float y) {
        super(x, y);
        this.cityName = cityName;
        this.texture = new Texture(Gdx.files.internal("buildings/city.png"));
    }
}
