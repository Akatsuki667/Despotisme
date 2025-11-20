package com.despotisme.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.despotisme.entities.Unit;

/**
 * ============================================================================
 * MiniMapView
 * ----------------------------------------------------------------------------
 * Gère :
 *   - Le rendu de la mini-carte dans un FrameBuffer
 *   - Le tuilage tuiles -> pixels -> minimap
 *   - Le brouillard de guerre en 3 couches (non exploré / exploré / visible)
 *   - Le marqueur du colon
 *   - La détection de clic dans la mini-carte (conversion stage -> world)
 *
 * La mini-carte est entièrement autonome et appelée via GameHUD.
 * ============================================================================
 */
public class MiniMapView {

    // ============================================================================
    // === 1. Widgets UI de la mini-carte
    // ============================================================================
    private final Table table;         // Conteneur visuel
    private final Texture panelTexture;
    private final Image panelImage;
    private Image miniImage;           // Zone affichant la texture finale

    // ============================================================================
    // === 2. Objets LibGDX nécessaires au rendu
    // ============================================================================
    private OrthogonalTiledMapRenderer mapRenderer;   // Rendu de la carte
    private OrthographicCamera miniCamera;            // Caméra orthographique réduite
    private FrameBuffer fbo;                          // Texture rendue dans un FBO
    private final SpriteBatch miniBatch;              // Batch interne pour brouillard + colon

    // ============================================================================
    // === 3. Ressources minimap
    // ============================================================================
    private Texture fogPixel;         // Texture 1×1 blanche pour dessiner le fog
    private Texture settlerPixel;     // Texture du marqueur rouge du colon

    // ============================================================================
    // === 4. Informations sur la carte principale
    // ============================================================================
    private float mapWidth;
    private float mapHeight;

    // Taille affichée du panneau minimap
    private static final float PANEL_WIDTH  = 460f;
    private static final float PANEL_HEIGHT = 400f;

    // Taille fixe du rendu interne (256x256)
    private static final int MINIMAP_SIZE = 256;

    /**
     * ============================================================================
     * Constructeur : crée l'interface visuelle du panneau mini-carte.
     * ============================================================================
     */
    public MiniMapView(Skin skin) {

        table = new Table(skin);

        // ---------------------------------------------------------
        // Fond du panneau
        // ---------------------------------------------------------
        panelTexture = new Texture(Gdx.files.internal("ui/panel_minimap.png"));
        panelImage = new Image(panelTexture);
        table.setBackground(panelImage.getDrawable());

        panelImage.setTouchable(Touchable.disabled);
        table.setTouchable(Touchable.enabled);

        table.align(Align.top);
        table.pad(12);
        table.defaults().space(6).center();

        // ---------------------------------------------------------
        // Titre "Mini-carte"
        // ---------------------------------------------------------
        Label title = new Label("Mini-carte", skin, "medieval");
        title.setEllipsis(true);
        table.add(title).center().padBottom(8).row();

        // ---------------------------------------------------------
        // Zone d'affichage de la mini-carte (texture FBO)
        // ---------------------------------------------------------
        miniImage = new Image();
        miniImage.setTouchable(Touchable.enabled);

        // Listener temporaire (debug)
        miniImage.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("### miniImage.touchDown: local=" + x + "," + y +
                        "  stage=" + event.getStageX() + "," + event.getStageY());
                return false;
            }
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("### miniImage.clicked: local=" + x + "," + y);
            }
        });

        miniImage.setScaling(Scaling.stretch);

        table.add(miniImage)
             .size(MINIMAP_SIZE, MINIMAP_SIZE)
             .center()
             .row();

        table.setSize(PANEL_WIDTH, PANEL_HEIGHT);

        // ---------------------------------------------------------
        // Textures utilitaires
        // ---------------------------------------------------------
        // Fog pixel (blanc 1x1)
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(1, 1, 1, 1);
        pm.fill();
        fogPixel = new Texture(pm);
        pm.dispose();

        // Pixel rouge (marqueur du colon)
        Pixmap pm2 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm2.setColor(1, 0, 0, 1);
        pm2.fill();
        settlerPixel = new Texture(pm2);
        pm2.dispose();

        miniBatch = new SpriteBatch();
    }

    /**
     * ============================================================================
     * Initialise les informations de la mini-carte.
     * - renderer
     * - caméra indépendante
     * - FrameBuffer (texture finale)
     * ============================================================================
     */
    public void init(TiledMap map, Unit unit, float mapWidth, float mapHeight) {
        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1f);
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;

        // Caméra centrée sur la carte
        miniCamera = new OrthographicCamera();
        miniCamera.position.set(mapWidth / 2f, mapHeight / 2f, 0);
        miniCamera.update();

        // Création du FBO 256x256
        if (fbo != null)
            fbo.dispose();

        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, MINIMAP_SIZE, MINIMAP_SIZE, false);
    }

    /**
     * ============================================================================
     * Convertit un clic STAGE → coordonnées monde.
     * ============================================================================
     */
    public boolean screenToWorld(float stageX, float stageY, float[] outPos) {

        System.out.println("screenToWorld() STAGE=" + stageX + "," + stageY);

        // Stage -> miniImage (local)
        Vector2 local = new Vector2(stageX, stageY);
        miniImage.stageToLocalCoordinates(local);

        System.out.println("  -> local=" + local.x + "," + local.y +
                "  miniImage size=" + miniImage.getWidth() + "x" + miniImage.getHeight());

        // Hors de la mini-carte ?
        if (local.x < 0 || local.y < 0 ||
            local.x > miniImage.getWidth() ||
            local.y > miniImage.getHeight()) {
            System.out.println(" --> MISS minimap");
            return false;
        }

        // Normalisation 0..1
        float nx = local.x / miniImage.getWidth();
        float ny = local.y / miniImage.getHeight();

        // Conversion en coordonnées monde (pixels)
        outPos[0] = nx * mapWidth;
        outPos[1] = ny * mapHeight;

        System.out.println(" --> HIT minimap! nx=" + nx + " ny=" + ny +
                "   world=" + outPos[0] + "," + outPos[1]);

        return true;
    }

    /**
     * ============================================================================
     * Rendu complet de la mini-carte :
     *   1. Rendu de la carte dans le FBO
     *   2. Rendu du brouillard de guerre
     *   3. Rendu du marqueur du colon
     *   4. Affectation du FBO → miniImage
     * ============================================================================
     */
    public void renderMiniMap(boolean[][] explored, Unit unit) {

        if (mapRenderer == null || fbo == null || explored == null || unit == null)
            return;

        // -------------------------------
        // Étape 1 — rendre dans le FBO
        // -------------------------------
        fbo.begin();
        Gdx.gl.glViewport(0, 0, MINIMAP_SIZE, MINIMAP_SIZE);
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Calcul du viewport de la mini-caméra selon ratio carte→FBO
        float mapAspect = mapWidth / mapHeight;
        float fboAspect = 1f; // carré

        if (fboAspect > mapAspect) {
            miniCamera.viewportHeight = mapHeight;
            miniCamera.viewportWidth = mapHeight * fboAspect;
        } else {
            miniCamera.viewportWidth = mapWidth;
            miniCamera.viewportHeight = mapWidth / fboAspect;
        }

        miniCamera.position.set(mapWidth / 2f, mapHeight / 2f, 0);
        miniCamera.update();

        mapRenderer.setView(miniCamera);
        mapRenderer.render();

        // -------------------------------
        // Étape 2 — brouillard de guerre
        // -------------------------------
        miniBatch.setProjectionMatrix(
                miniBatch.getProjectionMatrix().idt().setToOrtho2D(0, 0, MINIMAP_SIZE, MINIMAP_SIZE));
        miniBatch.begin();

        int mapTilesX = explored.length;
        int mapTilesY = explored[0].length;

        float tileW = (float) MINIMAP_SIZE / mapTilesX;
        float tileH = (float) MINIMAP_SIZE / mapTilesY;

        int unitTileX = (int) (unit.getX() / 32f);
        int unitTileY = (int) (unit.getY() / 32f);

        int radius = 5;

        for (int x = 0; x < mapTilesX; x++) {
            for (int y = 0; y < mapTilesY; y++) {

                boolean visible =
                        (x - unitTileX) * (x - unitTileX) +
                        (y - unitTileY) * (y - unitTileY)
                        <= radius * radius;

                if (!explored[x][y])
                    miniBatch.setColor(0f, 0f, 0f, 0.95f); // Jamais vu
                else if (!visible)
                    miniBatch.setColor(0f, 0f, 0f, 0.55f); // Vu mais pas visible
                else
                    miniBatch.setColor(0f, 0f, 0f, 0.20f); // Actuellement visible

                miniBatch.draw(fogPixel, x * tileW, y * tileH, tileW, tileH);
            }
        }

        // -------------------------------
        // Étape 3 — marqueur du colon
        // -------------------------------
        float mx = unitTileX * tileW;
        float my = unitTileY * tileH;

        miniBatch.setColor(1f, 0.2f, 0.2f, 1f);
        miniBatch.draw(settlerPixel,
                mx - tileW * 0.5f,
                my - tileH * 0.5f,
                tileW * 1.5f,
                tileH * 1.5f);

        miniBatch.setColor(1, 1, 1, 1);
        miniBatch.end();

        fbo.end();

        // -------------------------------
        // Étape 4 — appliquer texture FBO à miniImage
        // -------------------------------
        Texture tex = fbo.getColorBufferTexture();
        TextureRegion region = new TextureRegion(tex, 0, 0, MINIMAP_SIZE, MINIMAP_SIZE);
        region.flip(false, true);

        miniImage.setDrawable(new TextureRegionDrawable(region));
    }

    /**
     * Retourne la Table contenant la mini-carte.
     */
    public Table getTable() {
        return table;
    }

    /**
     * Libération des ressources internes.
     */
    public void dispose() {
        if (panelTexture != null) panelTexture.dispose();
        if (fogPixel != null) fogPixel.dispose();
        if (fbo != null) fbo.dispose();
        miniBatch.dispose();
    }
}
