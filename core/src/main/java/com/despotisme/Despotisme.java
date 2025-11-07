package com.despotisme;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.Screen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Despotisme extends ApplicationAdapter {
    private SpriteBatch batch;
    private Screen currentScreen;

    @Override
    public void create() {
        batch = new SpriteBatch();
        // Installing MapScreen
        setScreen(new MapScreen(this));
    }

    /** Allows screen changing */
    public void setScreen(Screen screen) {
        if (currentScreen != null) currentScreen.dispose();
        currentScreen = screen;
        if (currentScreen != null) currentScreen.show();
    }

    @Override
    public void render() {
        // Clearing screen
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        // Rendering current screen
        if (currentScreen != null) currentScreen.render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height) {
        if (currentScreen != null) currentScreen.resize(width, height);
    }

    @Override
    public void pause() {
        if (currentScreen != null) currentScreen.pause();
    }

    @Override
    public void resume() {
        if (currentScreen != null) currentScreen.resume();
    }

    @Override
    public void dispose() {
        if (currentScreen != null) currentScreen.hide();
        if (currentScreen != null) currentScreen.dispose();
        if (batch != null) batch.dispose();
    }

    public SpriteBatch getBatch() {
        return batch;
    }
}
