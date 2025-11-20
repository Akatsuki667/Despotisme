package com.despotisme.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;

public class ResourcePanel {
    private final Table table;

    private static final float PANEL_WIDTH = 420f;
    private static final float PANEL_HEIGHT = 300f;

    public ResourcePanel(Skin skin) {
        table = new Table(skin);
        table.setBackground(new NinePatchDrawable(
                new NinePatch(new Texture("ui/panel_resources.png"), 24, 24, 24, 24)));
        table.align(Align.top);
        table.defaults().pad(10).center();

        Label title = new Label("Ressources", skin, "medieval");
        title.setEllipsis(true);
        table.add(title).center().padBottom(10).row();

        addResourceRow("ui/icon_gold.png", "Or : 250", skin);
        addResourceRow("ui/icon_food.png", "Nourriture : 120", skin);
        addResourceRow("ui/icon_population.png", "Population : 35", skin);

        table.setSize(PANEL_WIDTH, PANEL_HEIGHT);
        table.setWidth(PANEL_WIDTH);
        table.setHeight(PANEL_HEIGHT);

    }

    private void addResourceRow(String iconPath, String value, Skin skin) {
        Image icon = new Image(new Texture(iconPath));
        Label label = new Label(value, skin);
        label.setColor(Color.WHITE);

        Table row = new Table();
        row.add(icon).size(26, 26).padRight(8);
        row.add(label).left();

        table.add(row).center().row();
    }

    public Table getTable() {
        return table;
    }
}
