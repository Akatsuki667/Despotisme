package com.despotisme.managers;

import com.despotisme.factories.CityFactoryImpl;
import com.despotisme.factories.UnitFactoryImpl;
import com.despotisme.entities.Unit;
import com.despotisme.entities.Settler;
import com.despotisme.entities.City;
// import com.despotisme.entities.Ressource;

import java.util.List;
import java.util.ArrayList;
import lombok.Getter;

public class GameManager {
    @Getter private final UnitFactoryImpl unitFactory;
    @Getter private final CityFactoryImpl cityFactory;

    @Getter
    private int turnCount = 1;

    @Getter private final List<Unit> units = new ArrayList<>();
    @Getter private final List<City> cities = new ArrayList<>();
    // private final List<Resource> = new ArrayList<>();

    // Input management ?
    // Barbarians management ?

    public GameManager() {
        this.unitFactory = new UnitFactoryImpl();
        this.cityFactory = new CityFactoryImpl();
    }

    public void endTurn() {
        for (Unit unit : units) {
            unit.restoreMoves();
            // unit.restoreAction();
        }

        // City management ?

        // Barbarians ?

        this.turnCount++;
    }

    // method to spawn a unit without city production (start of game)
    public Unit createUnit(String unit, float x, float y) {
        Unit created = this.unitFactory.createUnit(unit, x, y);
        this.units.add(created);

        return created;
    }

    // method to produce a unit from a city queue
    public Unit produceUnit(String unit, City city) {
        Unit created = this.unitFactory.createUnit(unit, city.getX(), city.getY());
        this.units.add(created);

        return created;
    }

    public City foundCity(String cityName, Settler settler) {
        City city = this.cityFactory.createCity(cityName, settler.getX(), settler.getY());
        cities.add(city);
        settler.setSelected(false);
        units.remove(settler);
        settler.dispose();
        return city;
    }
}
