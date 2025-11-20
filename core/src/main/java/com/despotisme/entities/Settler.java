package com.despotisme.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.despotisme.constants.UnitsConstants;

public class Settler extends Unit {
    public Settler(float x, float y) {
        // Centering on grid
        super("Settler", x, y);

        frontTexture = new Texture(Gdx.files.internal("characters/settler/settler_standing_front.png"));
        backTexture = new Texture(Gdx.files.internal("characters/settler/settler_standing_back.png"));
        leftTexture = new Texture(Gdx.files.internal("characters/settler/settler_standing_left.png"));
        rightTexture = new Texture(Gdx.files.internal("characters/settler/settler_standing_right.png"));

        // ✅ choice selection
        selectionTexture = new Texture(Gdx.files.internal("characters/settler/selection.png"));

        currentTexture = frontTexture;
    }
}
