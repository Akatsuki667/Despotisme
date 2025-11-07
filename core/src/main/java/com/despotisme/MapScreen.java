package com.despotisme;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.despotisme.entities.Settler;
import com.badlogic.gdx.InputMultiplexer;

public class MapScreen implements Screen {

    private final Despotisme app;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private final Vector3 tmpVector = new Vector3();

    private Settler settler;

    private static final float TILE_SIZE = 32f;

    private float moveCooldown = 0.15f;
    private float moveTimer = 0f;

    // === UI ===
    private Stage stage;
    private Skin skin;
    private Table actionMenu;
    private BitmapFont font;

    public MapScreen(Despotisme app) {
        this.app = app;
        show();
    }

    // === Checking the walkability ===
    private boolean isWalkable(float x, float y) {
        int tileX = (int) (x / TILE_SIZE);
        int tileY = (int) (y / TILE_SIZE);
        boolean foundWalkable = false;

        for (MapLayer mapLayer : map.getLayers()) {
            if (!(mapLayer instanceof TiledMapTileLayer))
                continue;
            TiledMapTileLayer layer = (TiledMapTileLayer) mapLayer;

            if (tileX < 0 || tileY < 0 || tileX >= layer.getWidth() || tileY >= layer.getHeight())
                return false;

            TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
            if (cell == null || cell.getTile() == null)
                continue;

            Object prop = cell.getTile().getProperties().get("walkable");
            if (prop == null)
                continue;

            boolean isTileWalkable = false;
            if (prop instanceof Boolean)
                isTileWalkable = (Boolean) prop;
            else if (prop instanceof String)
                isTileWalkable = ((String) prop).equalsIgnoreCase("true");

            if (!isTileWalkable)
                return false;
            foundWalkable = true;
        }

        return foundWalkable;
    }

    @Override
    public void show() {
        map = new TmxMapLoader().load("maps/testmap.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1f);
        batch = new SpriteBatch();

        int mapWidthTiles = ((TiledMapTileLayer) map.getLayers().get(0)).getWidth();
        int mapHeightTiles = ((TiledMapTileLayer) map.getLayers().get(0)).getHeight();
        float mapWidthPx = mapWidthTiles * TILE_SIZE;
        float mapHeightPx = mapHeightTiles * TILE_SIZE;

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float scaleX = screenWidth / mapWidthPx;
        float scaleY = screenHeight / mapHeightPx;
        float scale = Math.min(scaleX, scaleY);

        camera = new OrthographicCamera(screenWidth / scale, screenHeight / scale);
        camera.position.set(mapWidthPx / 2f, mapHeightPx / 2f, 0);
        camera.update();

        // === Settler ===
        MapLayer objectLayer = map.getLayers().get("Objects");
        if (objectLayer != null) {
            for (MapObject object : objectLayer.getObjects()) {
                if (object instanceof RectangleMapObject && "Settler".equals(object.getName())) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    settler = new Settler(rect.x, rect.y, TILE_SIZE);
                    break;
                }
            }
        }
        if (settler == null) {
            settler = new Settler(mapWidthPx / 2f, mapHeightPx / 2f, TILE_SIZE);
        }

        // === UI ===
        stage = new Stage(new ScreenViewport());

        BitmapFont font = new BitmapFont();
        skin = new Skin();
        skin.add("default-font", font);
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = Color.WHITE;
        skin.add("default", style);

        createActionMenu();

        InputMultiplexer multiplexer = new InputMultiplexer(stage, new MapInputProcessor());
        Gdx.input.setInputProcessor(multiplexer);
    }

    private void createActionMenu() {
        actionMenu = new Table();
        actionMenu.setFillParent(false); // not stretching on fullscreen
        actionMenu.align(Align.bottomLeft);
        actionMenu.setSize(250, 180);
        actionMenu.setPosition(30, 30);

        // === Background ===
        actionMenu.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(
                new com.badlogic.gdx.graphics.g2d.NinePatch(
                        new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("ui/panel-bg.png")),
                        8, 8, 8, 8)));

        // === Button styles ===
        BitmapFont font = new BitmapFont();
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.GOLD;
        buttonStyle.downFontColor = Color.YELLOW;

        buttonStyle.up = new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(
                new com.badlogic.gdx.graphics.g2d.NinePatch(
                        new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("ui/button-up.png")),
                        6, 6, 6, 6));
        buttonStyle.over = new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(
                new com.badlogic.gdx.graphics.g2d.NinePatch(
                        new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("ui/button-over.png")),
                        6, 6, 6, 6));
        buttonStyle.down = new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(
                new com.badlogic.gdx.graphics.g2d.NinePatch(
                        new com.badlogic.gdx.graphics.Texture(Gdx.files.internal("ui/button-down.png")),
                        6, 6, 6, 6));

        // === Buttons ===
        TextButton btnCity = new TextButton("Found a city", buttonStyle);
        TextButton btnExplore = new TextButton("Explore", buttonStyle);
        TextButton btnWait = new TextButton("Wait", buttonStyle);

        btnCity.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("The city has been founded");
            }
        });

        btnExplore.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Explore");
            }
        });

        btnWait.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Wait");
            }
        });

        // === Vertical menu positioning ===
        actionMenu.defaults().pad(8).width(220).height(40);
        actionMenu.add(btnCity).row();
        actionMenu.add(btnExplore).row();
        actionMenu.add(btnWait).row();

        actionMenu.setVisible(false);
        stage.addActor(actionMenu);
    }

    @Override
    public void render(float delta) {
        // Clearing the screen before drawing
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);

        handleInput(delta);

        camera.update();
        renderer.setView(camera);

        // 1️⃣ Rendering the map
        renderer.render();

        // 2️⃣ Then, units and layers
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        settler.render(batch);
        batch.end();

        // 3️⃣ UI in the end
        stage.act(delta);
        stage.draw();
        System.out.println("Stage actors: " + stage.getActors().size);
    }

    private void handleInput(float delta) {
        if (!settler.isSelected())
            return;

        moveTimer -= delta;
        if (moveTimer > 0)
            return;

        float newX = settler.getX();
        float newY = settler.getY();
        String direction = null;

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            newY += TILE_SIZE;
            direction = "up";
        } else if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            newY -= TILE_SIZE;
            direction = "down";
        } else if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            newX -= TILE_SIZE;
            direction = "left";
        } else if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            newX += TILE_SIZE;
            direction = "right";
        }

        if (direction != null) {
            settler.setDirection(direction);
            if (isWalkable(newX, newY)) {
                settler.setPosition(newX, newY);
                moveTimer = moveCooldown;
            }
        }

        camera.position.set(settler.getX(), settler.getY(), 0);
    }

    private void handleSelection() {
        if (Gdx.input.justTouched()) {
            float worldX = Gdx.input.getX();
            float worldY = Gdx.graphics.getHeight() - Gdx.input.getY();
            camera.unproject(tmpVector.set(worldX, worldY, 0));

            Rectangle settlerRect = new Rectangle(settler.getX(), settler.getY(), 64, 64);
            boolean selected = settlerRect.contains(tmpVector.x, tmpVector.y);
            settler.setSelected(selected);
            actionMenu.setVisible(selected);
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        camera.setToOrtho(false, width, height);
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

    private class MapInputProcessor extends com.badlogic.gdx.InputAdapter {
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            System.out.println("touchDown called!"); // console checking if the touchdown is working

            // 1️⃣ Recalculating coordinates of the click in the world
            Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));

            System.out.println("Click at world: " + worldCoords.x + ", " + worldCoords.y);

            // 2️⃣ Check touch on unit
            Rectangle settlerRect = new Rectangle(settler.getX(), settler.getY(), 64, 64);
            boolean selected = settlerRect.contains(worldCoords.x, worldCoords.y);

            // 3️⃣ console checking if the settler is selected
            System.out.println("Clicked settler? " + selected);

            // 4️⃣ Applying results
            settler.setSelected(selected);
            if (actionMenu != null) {
                actionMenu.setVisible(selected);
            }

            return false; // → event still going in stage
        }
    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        batch.dispose();
        font.dispose();
        stage.dispose();
        if (settler != null)
            settler.dispose();
    }
}
