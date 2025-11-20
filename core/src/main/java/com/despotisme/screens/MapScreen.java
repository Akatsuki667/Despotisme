package com.despotisme.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.despotisme.entities.Settler;
import com.despotisme.entities.Unit;
import com.despotisme.entities.City;
import com.despotisme.ui.GameHUD;
import com.despotisme.Despotisme;
import com.despotisme.managers.GameManager;
import com.despotisme.constants.MapConstants;
import com.despotisme.constants.UnitsConstants;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import lombok.Getter;
import lombok.Setter;

/**
 * ============================================================================
 * MapScreen — Écran principal du jeu
 * -----------------------------------
 * Rôle :
 * - Afficher la carte Tiled
 * - Gérer la caméra et les déplacements
 * - Gérer les interactions joueur (clics, déplacement, zoom)
 * - Maintenir et appliquer le Fog of War
 * - Afficher le HUD (mini-carte, ressources, panneau d’unité)
 * ============================================================================
 */
public class MapScreen implements Screen {

    private final Despotisme app; // Référence à l'application principale
    private final GameManager gameManager = new GameManager();

    // === Monde / Carte ===
    private TiledMap map; // Carte Tiled
    private OrthogonalTiledMapRenderer renderer;// Rendu de la carte
    private OrthographicCamera camera; // Caméra principale
    private Viewport worldViewport; // Gestion des dimensions écran
    private SpriteBatch batch; // Batch pour afficher les sprites

    private Unit selectedUnit;

    private float mapWidthPx; // Largeur totale de la carte
    private float mapHeightPx; // Hauteur totale de la carte

    // === Déplacement du colon ===
    private float moveCooldown = 0.15f;
    private float moveTimer = 0f;

    // === Interface (HUD + menus) ===
    private Skin skin;
    private Table actionMenu;
    private BitmapFont font;
    private GameHUD hud;

    // === Pause Menu ===
    private Table pauseMenu; // Menu Pause (Resume + Exit)
    private Image pauseOverlay; // Fond noir semi-transparent
    private boolean paused = false; // État du jeu (en pause ou non)

    // === Fog of War ===
    private boolean[][] explored;
    private Texture fogTexture;

    /**
     * ============================================================================
     * Constructeur
     * Appelle directement show() pour initialiser l'écran.
     * ============================================================================
     */
    public MapScreen(Despotisme app) {
        this.app = app;
    }

    /**
     * ============================================================================
     * show()
     * Initialise entièrement l'écran :
     * - Charge la carte
     * - Configure la caméra
     * - Initialise le HUD
     * - Active les gestionnaires d'entrées
     * ============================================================================
     */
    @Override
    public void show() {

        // --- Chargement de la carte ---
        map = new TmxMapLoader().load("maps/testmap.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1f);
        batch = new SpriteBatch();

        // Dimensions de la carte
        TiledMapTileLayer base = (TiledMapTileLayer) map.getLayers().get(0);
        mapWidthPx = base.getWidth() * MapConstants.TILE_SIZE;
        mapHeightPx = base.getHeight() * MapConstants.TILE_SIZE;

        explored = new boolean[base.getWidth()][base.getHeight()];

        // --- Chargement du colon ---
        Settler settler = loadSettlerFromMap();
        if (settler == null)
            settler = (Settler) this.gameManager.getUnitFactory().createUnit("settler", mapWidthPx / 2f, mapHeightPx / 2f);
        this.selectedUnit = settler;

        // --- Caméra ---
        camera = new OrthographicCamera();
        worldViewport = new ScreenViewport(camera);
        worldViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        // Set initial camera position properly
        camera.position.set(mapWidthPx / 2f, mapHeightPx / 2f, 0);
        camera.update();

        // --- Skin + police médiévale ---
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        generateMedievalFont();

        // --- HUD ---
        hud = new GameHUD(skin, map, settler, mapWidthPx, mapHeightPx);

        // --- Menu d'action (Fonder / Explorer / Attendre) ---
        createActionMenu(settler);
        // --- Menu de pause
        createPauseMenu();

        // --- Multiplexeur d'entrées ---
        InputMultiplexer multiplexer = new InputMultiplexer(
                hud.getStage(),
                new MapInputProcessor());
        Gdx.input.setInputProcessor(multiplexer);
    }

    /**
     * ============================================================================
     * Charge le Settler depuis la couche "Objects" du fichier Tiled
     * ============================================================================
     */
    private Settler loadSettlerFromMap() {
        MapLayer objectLayer = map.getLayers().get("Objects");
        if (objectLayer == null)
            return null;

        for (MapObject obj : objectLayer.getObjects()) {
            if (obj instanceof RectangleMapObject && "Settler".equals(obj.getName())) {
                Rectangle r = ((RectangleMapObject) obj).getRectangle();
                return (Settler) this.gameManager.getUnitFactory().createUnit("settler", r.x, r.y);
            }
        }
        return null;
    }

    /**
     * ============================================================================
     * Génère et ajoute une police médiévale dans le Skin
     * ============================================================================
     */
    private void generateMedievalFont() {
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("fonts/MedievalSharp.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();

        p.size = 22;
        p.color = new Color(1f, 0.84f, 0f, 1f);
        p.borderColor = Color.BLACK;
        p.borderWidth = 2f;
        p.shadowOffsetX = 2;
        p.shadowOffsetY = 2;
        p.shadowColor = new Color(0, 0, 0, 0.75f);

        BitmapFont f = gen.generateFont(p);
        gen.dispose();

        skin.add("medieval-font", f);
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = f;
        style.fontColor = p.color;
        skin.add("medieval", style);
    }

    /**
     * ============================================================================
     * Crée le menu Pause (Resume / Exit)
     * ============================================================================
     */
    private void createPauseMenu() {

        // --- Fond semi-transparent ---
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0.65f);
        pm.fill();
        pauseOverlay = new Image(new Texture(pm));
        pm.dispose();

        pauseOverlay.setVisible(false);
        pauseOverlay.setFillParent(true);

        // --- Menu centré ---
        pauseMenu = new Table();
        pauseMenu.setVisible(false);
        pauseMenu.setFillParent(true);
        pauseMenu.center();

        TextButton.TextButtonStyle st = new TextButton.TextButtonStyle();
        st.font = skin.getFont("medieval-font");
        st.fontColor = Color.WHITE;
        st.overFontColor = Color.GOLD;

        TextButton btnResume = new TextButton("Resume", st);
        TextButton btnExit = new TextButton("Exit to Desktop", st);

        btnResume.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hidePauseMenu();
            }
        });

        btnExit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        pauseMenu.defaults().pad(12).width(280).height(60);
        pauseMenu.add(btnResume).row();
        pauseMenu.add(btnExit).row();

        // --- Ajouter au Stage du HUD (au-dessus de tout) ---
        hud.getStage().addActor(pauseOverlay);
        hud.getStage().addActor(pauseMenu);
    }

    private void showPauseMenu() {
        paused = true;
        pauseOverlay.setVisible(true);
        pauseMenu.setVisible(true);
    }

    private void hidePauseMenu() {
        paused = false;
        pauseOverlay.setVisible(false);
        pauseMenu.setVisible(false);
    }

    /**
     * ============================================================================
     * Vérifie si une tuile est marchable (propriété walkable=true dans Tiled)
     * ============================================================================
     */
    private boolean isWalkable(float x, float y) {
        int tileX = (int) (x / MapConstants.TILE_SIZE);
        int tileY = (int) (y / MapConstants.TILE_SIZE);

        for (MapLayer mapLayer : map.getLayers()) {
            if (!(mapLayer instanceof TiledMapTileLayer))
                continue;

            TiledMapTileLayer layer = (TiledMapTileLayer) mapLayer;

            if (tileX < 0 || tileY < 0 || tileX >= layer.getWidth() || tileY >= layer.getHeight())
                return false;

            TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
            if (cell == null)
                continue;

            Object walk = cell.getTile().getProperties().get("walkable");
            if (walk == null)
                continue;

            boolean ok = (walk instanceof Boolean)
                    ? (Boolean) walk
                    : (walk instanceof String && ((String) walk).equalsIgnoreCase("true"));

            if (!ok)
                return false;
            return true;
        }
        return false;
    }

    /**
     * ============================================================================
     * Met à jour les cases "explored" selon la vision du Settler
     * ============================================================================
     */
    private void updateExploredArea() {
        if (selectedUnit != null) {
            int cx = (int) (selectedUnit.getX() / MapConstants.TILE_SIZE);
            int cy = (int) (selectedUnit.getY() / MapConstants.TILE_SIZE);
            int radius = 5;

            for (int x = cx - radius; x <= cx + radius; x++) {
                for (int y = cy - radius; y <= cy + radius; y++) {
                    if (x < 0 || y < 0 || x >= explored.length || y >= explored[0].length)
                        continue;

                    float dx = x - cx;
                    float dy = y - cy;

                    if (dx * dx + dy * dy <= radius * radius)
                        explored[x][y] = true;
                }
            }
        }
    }

    /**
     * ============================================================================
     * Empêche la caméra de sortir de la carte
     * ============================================================================
     */
    private void clampCameraToWorld() {
        float halfW = camera.viewportWidth * camera.zoom * 0.5f;
        float halfH = camera.viewportHeight * camera.zoom * 0.5f;

        camera.position.x = Math.max(halfW, Math.min(mapWidthPx - halfW, camera.position.x));
        camera.position.y = Math.max(halfH, Math.min(mapHeightPx - halfH, camera.position.y));
    }

    /**
     * ============================================================================
     * Crée le menu d'action (Found City / Explore / Wait)
     * ============================================================================
     */
    private void createActionMenu(Settler settler) {

        actionMenu = new Table();
        actionMenu.align(Align.bottomLeft);
        actionMenu.setSize(250, 180);
        actionMenu.setPosition(30, 30);

        BitmapFont f = new BitmapFont();
        TextButton.TextButtonStyle st = new TextButton.TextButtonStyle();
        st.font = f;
        st.fontColor = Color.WHITE;
        st.overFontColor = Color.GOLD;
        st.downFontColor = Color.YELLOW;

        TextButton btnCity = new TextButton("Found a city", st);
        TextButton btnExplore = new TextButton("Explore", st);
        TextButton btnWait = new TextButton("Wait", st);

        btnCity.addListener(new ClickListener() {
            public void clicked(InputEvent e, float x, float y) {
                gameManager.foundCity("test", settler);
                selectedUnit = null;
                actionMenu.setVisible(false);
            }
        });

        btnExplore.addListener(new ClickListener() {
            public void clicked(InputEvent e, float x, float y) {
                System.out.println("Explore");
                actionMenu.setVisible(false);
            }
        });

        btnWait.addListener(new ClickListener() {
            public void clicked(InputEvent e, float x, float y) {
                System.out.println("Wait");
                actionMenu.setVisible(false);
            }
        });

        actionMenu.defaults().pad(8).width(220).height(40);
        actionMenu.add(btnCity).row();
        actionMenu.add(btnExplore).row();
        actionMenu.add(btnWait).row();

        actionMenu.setVisible(false);

        hud.getStage().addActor(actionMenu);
    }

    /**
     * ============================================================================
     * Render Fog of War sur la carte principale
     * ============================================================================
     */
    private void renderFogOfWar() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (fogTexture == null) {
            Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pm.setColor(Color.WHITE);
            pm.fill();
            fogTexture = new Texture(pm);
            pm.dispose();
        }

        if (selectedUnit != null) {
            int cx = (int) (selectedUnit.getX() / MapConstants.TILE_SIZE);
            int cy = (int) (selectedUnit.getY() / MapConstants.TILE_SIZE);
            int radius = 5;

            TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
            int W = layer.getWidth();
            int H = layer.getHeight();

            for (int x = 0; x < W; x++) {
                for (int y = 0; y < H; y++) {

                    float dx = x - cx;
                    float dy = y - cy;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);

                    boolean inVision = dist <= radius;
                    boolean exp = explored[x][y];

                    if (!exp)
                        batch.setColor(0f, 0f, 0f, 0.95f);
                    else if (!inVision)
                        batch.setColor(0f, 0f, 0f, 0.55f);
                    else
                        batch.setColor(0f, 0f, 0f, (dist / radius) * 0.4f);

                    batch.draw(fogTexture, x * MapConstants.TILE_SIZE, y * MapConstants.TILE_SIZE, MapConstants.TILE_SIZE, MapConstants.TILE_SIZE);
                }
            }
        }

        batch.setColor(1, 1, 1, 1);
        batch.end();
    }

    /**
     * ============================================================================
     * Boucle principale de rendu
     * ============================================================================
     */
    @Override
    public void render(float delta) {
        // --- Gestion de la Pause (ESC) ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (!paused)
                showPauseMenu();
            else
                hidePauseMenu();
        }

        if (paused) {
            // Pas de mouvement, pas d’input de carte
            hud.render(delta, batch, this.selectedUnit);
            return;
        }

        // Efface l'écran
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput(delta);

        // Rendu de la carte
        camera.update();
        renderer.setView(camera);
        renderer.render();

        renderFogOfWar();
        updateExploredArea();

        // Rendu des unités
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (selectedUnit != null) {
            this.selectedUnit.render(batch);
        }

        for (Unit unit : gameManager.getUnits()) {
            unit.render(batch);
        }

        // Rendu des villes
        for (City city : gameManager.getCities()) {
            city.render(batch);
        }

        batch.end();

        // Rendu du HUD
        hud.updateFog(explored, this.selectedUnit);
        hud.render(delta, batch, this.selectedUnit);
    }

    /**
     * ============================================================================
     * Gestion des entrées clavier pour déplacer le Settler
     * ============================================================================
     */
    private void handleInput(float delta) {
        if (this.selectedUnit == null || !this.selectedUnit.isSelected())
            return;

        moveTimer -= delta;
        if (moveTimer > 0)
            return;

        float newX = this.selectedUnit.getX();
        float newY = this.selectedUnit.getY();
        String dir = null;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            newY += MapConstants.TILE_SIZE;
            dir = "up";
        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            newY -= MapConstants.TILE_SIZE;
            dir = "down";
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            newX -= MapConstants.TILE_SIZE;
            dir = "left";
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            newX += MapConstants.TILE_SIZE;
            dir = "right";
        }

        if (dir != null && isWalkable(newX, newY)) {

            this.selectedUnit.setDirection(dir);
            this.selectedUnit.setPosition(newX, newY);

            updateExploredArea();
            moveTimer = moveCooldown;
        }

        camera.position.set(this.selectedUnit.getX() + MapConstants.TILE_SIZE / 2f, this.selectedUnit.getY() + MapConstants.TILE_SIZE / 2f, 0);
        clampCameraToWorld();
        camera.update();
    }

    /**
     * ============================================================================
     * Redimensionnement de l'écran
     * ============================================================================
     */
    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, true);
        if (hud != null)
            hud.resize(width, height);
        clampCameraToWorld();
        camera.update();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    /**
     * ============================================================================
     * Gestion des clics et du zoom via InputProcessor interne
     * ============================================================================
     */
    private class MapInputProcessor extends com.badlogic.gdx.InputAdapter {

        private boolean dragging = false;
        private int lastX, lastY;
        private final float zoomSpeed = 0.1f;
        private final float minZoom = 0.5f;
        private final float maxZoom = 2.0f;

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {

            hud.forceLayout();
            float[] worldPos = new float[2];

            // Clic sur la mini-carte
            if (hud.handleMiniMapClick(screenX, screenY, worldPos)) {
                camera.position.set(worldPos[0], worldPos[1], 0);
                return true;
            }

            // Clic droit = déplacement caméra
            if (button == Input.Buttons.RIGHT) {
                dragging = true;
                lastX = screenX;
                lastY = screenY;
                return true;
            }

            // Clic gauche = sélection d’unité
            if (button == Input.Buttons.LEFT) {

                Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
                if (selectedUnit != null) {
                    Rectangle r = new Rectangle(selectedUnit.getX(), selectedUnit.getY(), MapConstants.TILE_SIZE, MapConstants.TILE_SIZE);

                    boolean selected = r.contains(world.x, world.y);
                    selectedUnit.setSelected(selected);

                    if (actionMenu != null)
                        actionMenu.setVisible(selected);

                    hud.showUnitPanel(selected, selected ? selectedUnit : null);

                    return true;
                }
                actionMenu.setVisible(false);
            }
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (!dragging)
                return false;

            int dx = screenX - lastX;
            int dy = screenY - lastY;

            camera.position.add(-dx * camera.zoom, dy * camera.zoom, 0);
            clampCameraToWorld();

            lastX = screenX;
            lastY = screenY;

            return true;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.RIGHT) {
                dragging = false;
                return true;
            }
            return false;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            camera.zoom += amountY * zoomSpeed;
            camera.zoom = Math.max(minZoom, Math.min(maxZoom, camera.zoom));
            clampCameraToWorld();
            return true;
        }
    }

    public void setSelectedUnit(Unit unit) {
        if (this.selectedUnit != null) {
            this.selectedUnit.setSelected(false);
        }
        unit.setSelected(true);
        this.selectedUnit = unit;
    }

    /**
     * ============================================================================
     * Libération des ressources
     * ============================================================================
     */
    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        batch.dispose();
        if (font != null)
            font.dispose();
    }
}
