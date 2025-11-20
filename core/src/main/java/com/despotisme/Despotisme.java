package com.despotisme;

import com.badlogic.gdx.Game;
import com.despotisme.screens.MainMenuScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Despotisme extends Game {

    @Override
    public void create() {
        // Installing Menu Screen
        setScreen(new MainMenuScreen(this));
    }
}
