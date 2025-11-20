package com.despotisme.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.despotisme.entities.Unit;

public class UnitPanel {

    private final Table table;
    private final Label typeLabel;
    private final Label positionLabel;
    private final Image icon;

    private static final float PANEL_WIDTH = 420f;
    private static final float PANEL_HEIGHT = 300f;

    public UnitPanel(Skin skin) {
        table = new Table(skin);
        table.setBackground(new NinePatchDrawable(
                new NinePatch(new Texture("ui/panel_unit.png"), 24, 24, 24, 24)));
        table.align(Align.top);
        table.defaults().pad(10).center();

        Label title = new Label("Unité", skin, "medieval");
        title.setEllipsis(true);
        table.add(title).center().padBottom(10).row();

        icon = new Image(new Texture("characters/settler/settler_standing_front.png"));
        table.add(icon).size(64, 64).padBottom(10).center().row();

        typeLabel = new Label("Colon", skin);
        typeLabel.setColor(Color.WHITE);
        positionLabel = new Label("(0, 0)", skin);
        positionLabel.setColor(Color.LIGHT_GRAY);

        table.add(typeLabel).center().padBottom(6).row();
        table.add(positionLabel).center().padBottom(6).row();

        table.setSize(PANEL_WIDTH, PANEL_HEIGHT);
        table.setWidth(PANEL_WIDTH);
        table.setHeight(PANEL_HEIGHT);

    }

    public void updateInfo(Unit unit) {
        typeLabel.setText(unit.getName());
        positionLabel.setText("Pos: (" + (int) unit.getX() + ", " + (int) unit.getY() + ")");
    }

    public Table getTable() {
        return table;
    }
}
