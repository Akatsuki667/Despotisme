package com.despotisme.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Settler {

    private Texture frontTexture;
    private Texture backTexture;
    private Texture leftTexture;
    private Texture rightTexture;
    private Texture selectionTexture;

    private Texture currentTexture;
    private boolean selected = false;

    private float x, y;

    public Settler(float x, float y, float tileSize) {
        // Centering on grid
        this.x = Math.round(x / tileSize) * tileSize;
        this.y = Math.round(y / tileSize) * tileSize;

        frontTexture = new Texture(Gdx.files.internal("characters/settler/settler_standing_front.png"));
        backTexture = new Texture(Gdx.files.internal("characters/settler/settler_standing_back.png"));
        leftTexture = new Texture(Gdx.files.internal("characters/settler/settler_standing_left.png"));
        rightTexture = new Texture(Gdx.files.internal("characters/settler/settler_standing_right.png"));

        // ✅ choice selection
        selectionTexture = new Texture(Gdx.files.internal("characters/settler/selection.png"));

        currentTexture = frontTexture;
    }

    public void render(SpriteBatch batch) {
        if (selected) {
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(selectionTexture, x - 8, y - 8, 80, 80);
        }
        batch.draw(currentTexture, x, y, 64, 64);
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

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void dispose() {
        frontTexture.dispose();
        backTexture.dispose();
        leftTexture.dispose();
        rightTexture.dispose();
        selectionTexture.dispose();
    }
}
