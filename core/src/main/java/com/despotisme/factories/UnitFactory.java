package com.despotisme.factories;

import com.despotisme.entities.Unit;

public interface UnitFactory {
    Unit createUnit(String unit, float x, float y);
}
