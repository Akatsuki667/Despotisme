package com.despotisme.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.despotisme.entities.Unit;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;

/**
 * ================================================================
 *  GameHUD
 *  Interface utilisateur principale (HUD) du jeu.
 *
 *  - Affiche la mini-carte
 *  - Affiche les ressources
 *  - Affiche les informations de l’unité sélectionnée
 *  - Dessine le cadre décoratif autour de l’écran
 *  - Gère les interactions du HUD (dont clic mini-carte)
 * ================================================================
 */
public class GameHUD {

    // ===============================================================
    // === 1. Éléments principaux du HUD
    // ===============================================================
    private final Stage stage;            // Gère tous les widgets (Scene2D)
    private final Table root;             // Table racine qui organise tout l’UI
    private final MiniMapView miniMapView;
    private final ResourcePanel resourcePanel;
    private final UnitPanel unitPanel;
    private final Skin skin;              // Thème graphique utilisé pour l’UI

    // Taille standard des panneaux latéraux
    private static final float PANEL_WIDTH  = 460f;
    private static final float PANEL_HEIGHT = 400f;

    // ===============================================================
    // === 2. Cadre décoratif (bordure en pierre autour de l'écran)
    // ===============================================================
    private final Image frameImage;
    private final Texture frameTexture;

    /**
     * ===============================================================
     *  Constructeur : crée et configure entièrement le HUD.
     * ===============================================================
     *
     * @param skin Thème UI (boutons, textes, etc.)
     * @param map Carte principale utilisée pour la mini-carte
     * @param settler Unité du joueur
     * @param mapWidth Largeur totale de la carte (pixels)
     * @param mapHeight Hauteur totale de la carte (pixels)
     */
    public GameHUD(Skin skin, TiledMap map, Unit unit,
                    float mapWidth, float mapHeight) {

        this.skin = skin;

        // ---------------------------------------------------------------
        //  Stage : élément central du HUD (gère tous les acteurs UI)
        // ---------------------------------------------------------------
        this.stage = new Stage(new ScreenViewport());

        // ---------------------------------------------------------------
        //  Cadre décoratif autour de l'écran
        // ---------------------------------------------------------------
        frameTexture = new Texture(Gdx.files.internal("ui/roman_stone_frame.png"));
        frameTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        frameImage = new Image(frameTexture);
        frameImage.setTouchable(null); // Le cadre ne bloque pas les clics

        // ---------------------------------------------------------------
        //  Table racine (layout principal)
        // ---------------------------------------------------------------
        root = new Table();
        root.setFillParent(true); // La table occupe tout l'écran

        // ---------------------------------------------------------------
        //  Création des panneaux du HUD
        // ---------------------------------------------------------------
        miniMapView    = new MiniMapView(skin);
        miniMapView.init(map, unit, mapWidth, mapHeight);

        resourcePanel  = new ResourcePanel(skin);
        unitPanel      = new UnitPanel(skin);

        // ---------------------------------------------------------------
        //  Panneau vertical à droite de l'écran
        // ---------------------------------------------------------------
        Table rightPanel = new Table(skin);
        rightPanel.align(Align.topRight);
        rightPanel.padTop(20).padRight(20);

        rightPanel.defaults()
                  .pad(6)
                  .width(PANEL_WIDTH);

        // Ordre des widgets dans le panneau droit
        rightPanel.add(miniMapView.getTable()).row();
        rightPanel.add(resourcePanel.getTable()).row();
        rightPanel.add(unitPanel.getTable()).row();

        unitPanel.getTable().setVisible(false); // Masqué au début

        // ---------------------------------------------------------------
        //  Construction finale du layout
        // ---------------------------------------------------------------
        root.add().expandX(); // colonne vide à gauche (pousser le HUD à droite)
        root.add(rightPanel)
            .align(Align.topRight)
            .padTop(20)
            .padRight(20);

        // Ajout au stage dans le bon ordre (cadre + UI)
        stage.addActor(frameImage);
        stage.addActor(root);

        updateFrameToWindow();
    }

    /**
     * ===============================================================
     *  Gère un clic sur la mini-carte.
     *
     *  Convertit d’abord screen → stage → coordonnées mini-carte → monde.
     * ===============================================================
     *
     * @param screenX coordonnée X écran
     * @param screenY coordonnée Y écran
     * @param worldPos tableau [2] où sera stockée la position monde
     */
    public boolean handleMiniMapClick(float screenX, float screenY, float[] worldPos) {

        // Conversion screen → coordonnées du Stage (Scene2D)
        Vector2 stageCoords = stage.screenToStageCoordinates(new Vector2(screenX, screenY));

        System.out.println("handleMiniMapClick: screen=" + screenX + "," + screenY +
                           " -> stage=" + stageCoords.x + "," + stageCoords.y);

        return miniMapView.screenToWorld(stageCoords.x, stageCoords.y, worldPos);
    }

    /**
     * ===============================================================
     *  Ajuste la taille du cadre décoratif lors d'un resize.
     * ===============================================================
     */
    private void updateFrameToWindow() {

        int width  = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        // Taille et position du cadre
        frameImage.setPosition(0, 0);
        frameImage.setSize(width, height);

        // Mise à jour du viewport du Stage
        stage.getViewport().update(width, height, true);

        stage.getCamera().position.set(width / 2f, height / 2f, 0);
        stage.getCamera().viewportWidth  = width;
        stage.getCamera().viewportHeight = height;
        stage.getCamera().update();

        frameImage.toBack(); // Toujours derrière l’UI
    }

    /**
     * ===============================================================
     *  Met à jour la mini-carte avec le brouillard et le colon.
     * ===============================================================
     */
    public void updateFog(boolean[][] explored, Unit unit) {
        miniMapView.renderMiniMap(explored, unit);
    }

    /**
     * ===============================================================
     *  Dessine l’HUD (appelé à chaque frame).
     * ===============================================================
     */
    public void render(float delta, SpriteBatch batch, Unit selectedUnit) {

        updateFrameToWindow(); // Ajuster si fenêtre changée

        if (selectedUnit == null) {
            unitPanel.getTable().setVisible(false);
        }

        stage.act(delta);      // Mise à jour logique
        stage.draw();          // Rendu de toute l’interface
    }

    /**
     * ===============================================================
     *  Lors d’un redimensionnement de la fenêtre.
     * ===============================================================
     */
    public void resize(int width, int height) {
        updateFrameToWindow();
    }

    /**
     * ===============================================================
     *  Affiche ou masque le panneau d’unité sélectionnée.
     * ===============================================================
     */
    public void showUnitPanel(boolean visible, Unit unit) {
        if (unit != null) {
            unitPanel.updateInfo(unit);
        }

        unitPanel.getTable().setVisible(visible);
    }

    /**
     * ===============================================================
     *  Forcer Scene2D à recalculer immédiatement tout le layout.
     * ===============================================================
     */
    public void forceLayout() {
        stage.act(0);
        stage.draw();
        root.validate();
    }

    public void updateLayout() {
        root.validate();
    }

    public Stage getStage() {
        return stage;
    }

    /**
     * ===============================================================
     *  Libération des ressources.
     * ===============================================================
     */
    public void dispose() {
        stage.dispose();
        frameTexture.dispose();
    }
}
