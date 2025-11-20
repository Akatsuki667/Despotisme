package com.despotisme.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.despotisme.constants.UnitsConstants;
import com.despotisme.constants.MapConstants;

import lombok.Getter;
import lombok.Setter;

public abstract class Unit {
    // moving capacity
    @Getter protected int moves = 3;

    // sprite
    protected Texture frontTexture;
    protected Texture backTexture;
    protected Texture leftTexture;
    protected Texture rightTexture;
    protected Texture selectionTexture;

    @Getter
    protected String name;

    // state
    protected Texture currentTexture;
    @Getter @Setter protected boolean selected = false;

    // location
    @Getter protected float x, y;

    public Unit(String name, float x, float y) {
        // Centering on grid
        this.x = Math.round(x / MapConstants.TILE_SIZE) * MapConstants.TILE_SIZE;
        this.y = Math.round(y / MapConstants.TILE_SIZE) * MapConstants.TILE_SIZE;
        this.name = name;
    }

    public void render(SpriteBatch batch) {
        if (selected) {
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(selectionTexture, x - 8, y - 8, UnitsConstants.UNIT_HEIGHT, UnitsConstants.UNIT_WIDTH);
        }
        batch.draw(currentTexture, x, y, UnitsConstants.UNIT_HEIGHT, UnitsConstants.UNIT_WIDTH);
    }

    public void setDirection(String direction) {
        switch (direction) {
            case "up":
                currentTexture = backTexture;
                break;
            case "down":
                currentTexture = frontTexture;
                break;
            case "left":
                currentTexture = leftTexture;
                break;
            case "right":
                currentTexture = rightTexture;
                break;
        }
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void dispose() {
        frontTexture.dispose();
        backTexture.dispose();
        leftTexture.dispose();
        rightTexture.dispose();
        selectionTexture.dispose();
    }

    public void restoreMoves() {
        this.moves = 3;
    }

    public void endTurn() {
        this.restoreMoves();
    }
}
