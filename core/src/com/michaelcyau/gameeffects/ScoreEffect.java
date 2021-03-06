package com.michaelcyau.gameeffects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.michaelcyau.gameobjects.Bell;
import com.michaelcyau.gameobjects.Collectible;
import com.michaelcyau.gameworld.GameWorld;
import com.michaelcyau.helpers.AssetLoader;

import java.util.LinkedList;
import java.util.List;

public class ScoreEffect {
    private Vector2 position; // CENTER position
    private Vector2 velocity;
    private String text;
    private GlyphLayout layout;
    private float width;
    private float height;
    private float initStarSpeed = 200;
    private float initStarAccel = -600;

    private float lifetime = -initStarSpeed / initStarAccel;

    private float driftSpeed = -20;
    private float transparency = 1;
    private float fadeOutSpeed = 2;
    private Color color;

    private GameWorld gameWorld;

    private int numStars = 18;
    private List<Star> stars;

    public ScoreEffect (float x, float y, Collectible col, String text, GameWorld gameWorld) {
        position = new Vector2(x + (col.getWidth() / 2), y - (col.getHeight() / 2));
        velocity = new Vector2(0, 0);
        this.text = text;
        this.gameWorld = gameWorld;
        layout = new GlyphLayout();
        layout.setText(AssetLoader.scoreFont, text);
        width = layout.width;
        height = layout.height;
        color = col.getColor().cpy();
        initStars();
    }

    public void update(float delta) {
        position.add(velocity.cpy().scl(delta));
        updateStars(delta);
        validate(delta);
        color.a = transparency;
    }

    public void render(SpriteBatch batcher, SpriteBatch uiBatcher, float camTop) {
        renderScore(uiBatcher, camTop);
        renderStars(batcher, camTop);
    }

    private void renderScore(SpriteBatch uiBatcher, float camTop) {
        float scalingFactor = (float) Gdx.graphics.getWidth() / (float) gameWorld.getWidth();

        float adjustedPositionX = (position.x * scalingFactor) - (width / 2);
        float adjustedPositionY = (position.y * scalingFactor) + (height / 2);

        uiBatcher.begin();
        uiBatcher.enableBlending();
        AssetLoader.scoreFont.setColor(color);
        AssetLoader.scoreFont.draw(uiBatcher, text,
                adjustedPositionX,
                adjustedPositionY + Gdx.graphics.getHeight() - (camTop * scalingFactor));
        AssetLoader.scoreFont.setColor(1, 1, 1, 1);
        uiBatcher.disableBlending();
        uiBatcher.end();
    }

    private void renderStars(SpriteBatch batcher, float camTop) {
        for (Star star: stars) {
            star.render(batcher, camTop);
        }
    }

    private void initStars() {
        stars = new LinkedList<Star>();
        float angle = 0;
        float increment = numStars > 0 ? 360f / numStars : 0;
        for (int i = 0; i < numStars; i++) {
            stars.add(new Star(position.x, position.y, angle, gameWorld, initStarSpeed, initStarAccel, driftSpeed, fadeOutSpeed, color));
            angle += increment;
        }
    }

    private void updateStars(float delta) {
        for (Star star: stars) {
            star.update(delta);
        }
    }

    private void validate(float delta) {
        if (lifetime != 0) {
            if (lifetime - delta < 0) {
                lifetime = 0;
                velocity.y = driftSpeed;
            } else {
                lifetime -= delta;
            }
        } else {
            if (transparency - (fadeOutSpeed * delta) > 0) {
                transparency -= fadeOutSpeed * delta;
            } else {
                gameWorld.removeScoreEffect(this);
            }
        }
    }
}
