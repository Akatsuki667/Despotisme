package com.despotisme.factories;

import com.despotisme.entities.Unit;
import com.despotisme.entities.Settler;

public class UnitFactoryImpl implements UnitFactory {
    public UnitFactoryImpl() {}

    @Override
    public Unit createUnit(String unit, float x, float y) {
        return switch(unit) {
            case "settler" -> new Settler(x, y);
            default -> throw new IllegalArgumentException("Unknown unit type: " + unit);
        };
    }
}
