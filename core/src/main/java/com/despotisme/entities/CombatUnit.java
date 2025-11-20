package com.despotisme.entities;

public abstract class CombatUnit extends Unit {
    protected int damageInflicted = 10;
    protected int hp = 50;

    public CombatUnit(String name, float x, float y) {
        super(name, x, y);
    }
}
