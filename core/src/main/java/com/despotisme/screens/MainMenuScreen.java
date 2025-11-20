package com.despotisme.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.despotisme.Despotisme;
import com.despotisme.screens.MapScreen;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

/**
 * Écran principal du jeu.
 * Affiche le menu d’accueil avec :
 *  - le titre
 *  - un bouton "Play Demo"
 *  - un réglage du volume de la musique
 *  - un bouton pour quitter le jeu
 * Gère également la musique et les transitions.
 */
public class MainMenuScreen implements Screen {

    // === Références principales ===
    private final Despotisme app;   // Référence à l’application principale
    private final Stage stage;      // Scène contenant tous les éléments UI
    private final Skin skin;        // Thème UI

    // === Ressources ===
    private Music music;            // Musique du menu
    private Texture backgroundTexture; // Image de fond du menu

    /**
     * Constructeur du menu principal.
     */
    public MainMenuScreen(Despotisme app) {
        this.app = app;

        // === Configuration du Stage ===
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // === Chargement du Skin ===
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        // === Fond du menu ===
        backgroundTexture = new Texture(Gdx.files.internal("ui/menu_background.jpg"));
        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // === Musique du menu ===
        music = Gdx.audio.newMusic(Gdx.files.internal("music/menu_theme.mp3"));
        music.setLooping(true);
        music.setVolume(0.30f);
        music.play();

        // === Chargement du style de police médiévale ===
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/MedievalSharp.ttf"));

        // Police du titre
        FreeTypeFontGenerator.FreeTypeFontParameter pTitle = new FreeTypeFontGenerator.FreeTypeFontParameter();
        pTitle.size = 52;
        pTitle.color = Color.GOLD;
        BitmapFont titleFont = gen.generateFont(pTitle);

        // Police UI (labels, boutons)
        FreeTypeFontGenerator.FreeTypeFontParameter pUI = new FreeTypeFontGenerator.FreeTypeFontParameter();
        pUI.size = 28;
        pUI.color = Color.WHITE;
        BitmapFont uiFont = gen.generateFont(pUI);

        gen.dispose();

        // Ajout des styles dans le Skin
        skin.add("medieval-title", new Label.LabelStyle(titleFont, Color.GOLD));
        skin.add("medieval-ui", new Label.LabelStyle(uiFont, Color.WHITE));
        skin.add("ui-font", uiFont);

        // === Construction de l’interface ===
        buildUI();
    }

    /**
     * Construit la structure visuelle (UI) du menu.
     */
    private void buildUI() {

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // === Titre du jeu ===
        Label title = new Label("DESPOTISME", skin, "medieval-title");
        title.setAlignment(Align.center);

        // === Style de boutons personnalisé ===
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = skin.getFont("ui-font");
        btnStyle.fontColor = Color.WHITE;
        btnStyle.overFontColor = Color.GOLD;

        // === Bouton PLAY DEMO ===
        TextButton btnPlay = new TextButton("Play Demo", btnStyle);

        btnPlay.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                fadeOutAndStart();
            }
        });

        // === Curseur de volume ===
        Label volumeLabel = new Label("Music Volume", skin, "medieval-ui");

        Slider volumeSlider = new Slider(0f, 1f, 0.01f, false, skin);
        volumeSlider.setValue(0.3f);

        volumeSlider.addListener(new ClickListener() {
            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                music.setVolume(volumeSlider.getValue());
            }
        });

        // === Bouton Quitter ===
        TextButton btnExit = new TextButton("Exit to Desktop", btnStyle);
        btnExit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        // === Mise en page ===
        root.add(title).padBottom(50).row();
        root.add(btnPlay).width(300).height(70).padBottom(20).row();
        root.add(volumeLabel).padBottom(5).row();
        root.add(volumeSlider).width(250).padBottom(30).row();
        root.add(btnExit).width(250).height(50).row();

        // Animation d’apparition douce
        root.getColor().a = 0f;
        root.addAction(fadeIn(1.2f));
    }

    /**
     * Lance une transition fade-out avant d'afficher la carte de démonstration.
     */
    private void fadeOutAndStart() {
        stage.addAction(sequence(
                fadeOut(1f),
                run(() -> music.stop()),
                delay(0.2f),
                run(() -> app.setScreen(new MapScreen(app)))
        ));
    }

    /**
     * Appelée chaque frame pour dessiner le menu.
     */
    @Override
    public void render(float delta) {

        // === Nettoie l'écran ===
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // === Dessine le fond ===
        stage.getBatch().begin();
        stage.getBatch().draw(backgroundTexture, 0, 0,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.getBatch().end();

        // === Dessine l’interface ===
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTexture.dispose();
        music.dispose();
    }
}
