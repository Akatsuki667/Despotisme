package com.despotisme.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.despotisme.constants.UnitsConstants;
import com.despotisme.constants.MapConstants;

import lombok.Getter;
import lombok.Setter;

public abstract class Location {
    // sprite
    protected Texture texture;
    protected Texture selectionTexture = new Texture(Gdx.files.internal("characters/settler/selection.png"));

    // state
    @Getter @Setter protected boolean selected = false;

    // location
    @Getter protected final float x, y;

    public Location(float x, float y) {
        // Centering on grid
        this.x = Math.round(x / MapConstants.TILE_SIZE) * MapConstants.TILE_SIZE;
        this.y = Math.round(y / MapConstants.TILE_SIZE) * MapConstants.TILE_SIZE;
    }

    public void render(SpriteBatch batch) {
        if (selected) {
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(selectionTexture, x - 8, y - 8, MapConstants.TILE_SIZE, MapConstants.TILE_SIZE);
        }
        batch.draw(texture, x, y, MapConstants.CITY_HEIGHT, MapConstants.CITY_WIDTH);
    }

    public void dispose() {
        texture.dispose();
    }
}
