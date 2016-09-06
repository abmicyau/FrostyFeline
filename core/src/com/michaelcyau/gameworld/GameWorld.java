package com.michaelcyau.gameworld;

import com.badlogic.gdx.math.Intersector;
import com.michaelcyau.gameobjects.Bell;
import com.michaelcyau.gameobjects.Bird;
import com.michaelcyau.gameobjects.Bunny;
import com.michaelcyau.gameeffects.ScoreEffect;
import com.michaelcyau.gameobjects.Collectible;
import com.michaelcyau.gameobjects.Snowflake;
import com.badlogic.gdx.math.MathUtils;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class GameWorld {

    private int numSnowflakes = 40;

    private float panUpBoundary = 0.6f; // as a fraction of screen height
    private float panDownBoundary = 0.3f; // as a fraction of screen height
    private float bottomBuffer = 0.3f; // as a fraction of screen height
    private float topBuffer = 0.5f; // as a fraction of screen height
    private float bellInterval = 0.25f; // as a factor of GAME WIDTH (not screen width)
    private float bellMaxInterval = 0.4f;
    private float bellIntervalGrowthAmount = 0.00015f;

    private Bunny bunny;
    private List<Snowflake> snowflakes;
    private List<Snowflake> deadSnowflakes;
    private List<Snowflake> bottomDeadSnowflakes;
    private List<Bell> bells;
    private List<Bell> deadBells;
    private List<Bird> birds;
    private List<Bird> deadBirds;
    private Collectible topCollectible;
    private float newestBellPositionY;
    private float bellSize = 14f;
    private float minBellSize = 7f;
    private float bellShrinkAmount = 0.007f;
    private int newBells = 0;
    private int lastBirdBellNum = 0;
    private int birdInterval = 40;
    private int birdIntervalIncrement = 10;

    private List<ScoreEffect> scoreEffects;
    private List<ScoreEffect> deadScoreEffects;

    private int gameWidth;
    private int gameHeight;

    private float worldTop;
    private float worldTopMax;

    private BigInteger score = new BigInteger("0");
    private BigInteger nextScoreAdded = new BigInteger("10");
    private boolean gameOver = false;

    public GameWorld(int gameWidth, int gameHeight) {
        this.gameHeight = gameHeight;
        this.gameWidth = gameWidth;
        worldTop = gameHeight;
        bunny = new Bunny(68, 0, this);
        initSnowflakes();
        initBells();
        initBirds();
        scoreEffects = new LinkedList<ScoreEffect>();
        deadScoreEffects = new LinkedList<ScoreEffect>();
    }

    public void update(float delta) {
        updateBunny(delta);
        updateSnowflakes(delta);
        updateBells(delta);
        updateBirds(delta);
        updateScoreEffects(delta);
        detectCollisions();
    }

    public Bunny getBunny() {
        return bunny;
    }

    public List<Snowflake> getSnowflakes() {
        return snowflakes;
    }

    public List<Bell> getBells() {
        return bells;
    }

    public List<Bird> getBirds() {
        return birds;
    }

    public List<ScoreEffect> getScoreEffects() {
        return scoreEffects;
    }

    public int getWidth() {
        return gameWidth;
    }

    public int getHeight() {
        return gameHeight;
    }

    public float getWorldTop() {
        return worldTop;
    }

    public float getWorldTopMax() {
        return worldTopMax;
    }

    public float getTopBuffer() {
        return topBuffer;
    }

    public float getBottomBuffer() {
        return bottomBuffer;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getScore() {
        return NumberFormat.getNumberInstance(Locale.US).format(score);
    }

    public void recycleSnowflake(Snowflake snowflake) {
        deadSnowflakes.add(snowflake);
    }

    public void recycleBottomSnowflake(Snowflake snowflake) {
        bottomDeadSnowflakes.add(snowflake);
    }

    public void removeBell(Bell bell) {
        deadBells.add(bell);
    }

    public void removeBird(Bird bird) {
        deadBirds.add(bird);
    }

    public void removeScoreEffect(ScoreEffect scoreEffect) {
        deadScoreEffects.add(scoreEffect);
    }

    public void endGame() {
        gameOver = true;
    }

    private void initBells() {
        bells = new LinkedList<Bell>();
        deadBells = new LinkedList<Bell>();
        newestBellPositionY = gameHeight * 0.1f;
        while (newestBellPositionY < gameHeight * (1 + topBuffer)) {
            newestBellPositionY += (gameWidth * bellInterval);
            Bell bell = new Bell((gameWidth * 0.03f) + MathUtils.random((gameWidth * 0.94f) - bellSize), newestBellPositionY, bellSize, bellSize, this);
            bells.add(bell);
            topCollectible = bell;
            newBells++;
        }
    }

    private void initBirds() {
        birds = new LinkedList<Bird>();
        deadBirds = new LinkedList<Bird>();
    }

    private void initSnowflakes() {
        snowflakes = new LinkedList<Snowflake>();
        for (int i = 0; i < numSnowflakes; i++) {
            snowflakes.add(new Snowflake(MathUtils.random(gameWidth), MathUtils.random(gameHeight), this));
        }
        deadSnowflakes = new LinkedList<Snowflake>();
        bottomDeadSnowflakes = new LinkedList<Snowflake>();
    }

    private void updateBunny(float delta) {
        bunny.update(delta);

        if (worldTop - bunny.getY() < gameHeight - (gameHeight * panUpBoundary)) {
            worldTop += (gameHeight - (gameHeight * panUpBoundary)) - (worldTop - bunny.getY());
        } else if (worldTop - bunny.getY() > gameHeight - (gameHeight * panDownBoundary)) {
            if (worldTop > gameHeight) {
                worldTop += ((gameHeight - (gameHeight * panDownBoundary)) - (worldTop - bunny.getY()));
                if (worldTop < gameHeight) {
                    worldTop = gameHeight;
                }
            }
        }
        worldTopMax = worldTop > worldTopMax ? worldTop : worldTopMax;
    }

    private void updateSnowflakes(float delta) {
        for (Snowflake snowflake: snowflakes) {
            snowflake.update(delta);
        }
        for (Snowflake snowflake: deadSnowflakes) {
            snowflakes.remove(snowflake);
            snowflakes.add(new Snowflake(MathUtils.random(gameWidth), (int) worldTop, this));
        }
        for (Snowflake snowflake: bottomDeadSnowflakes) {
            snowflakes.remove(snowflake);
            snowflakes.add(new Snowflake(MathUtils.random(gameWidth), (int) (worldTop - gameHeight + (MathUtils.random(bunny.getVelocity().y) * delta)), this));
        }

        deadSnowflakes.clear();
        bottomDeadSnowflakes.clear();
    }

    private void updateBells(float delta) {
        for (Bell bell: bells) {
            bell.update(delta);
        }
        for (Bell bell: deadBells) {
            bells.remove(bell);
        }
        if (worldTop > newestBellPositionY + (gameWidth * bellInterval)) {
            newestBellPositionY += gameWidth * bellInterval;
            if (newBells > lastBirdBellNum + birdInterval) {
                Bird bird = new Bird((gameWidth * 0.03f) + MathUtils.random((gameWidth * 0.94f) - Bird.width), newestBellPositionY, this);
                birds.add(bird);
                topCollectible = bird;
                lastBirdBellNum = newBells;
                birdInterval += birdIntervalIncrement;
            } else {
                Bell bell = new Bell((gameWidth * 0.03f) + MathUtils.random((gameWidth * 0.94f) - bellSize), newestBellPositionY, bellSize, bellSize, this);
                bells.add(bell);
                topCollectible = bell;
                bellSize = bellSize - bellShrinkAmount < minBellSize ? minBellSize : bellSize - bellShrinkAmount;
            }
            bellInterval = bellInterval + bellIntervalGrowthAmount > bellMaxInterval ? bellMaxInterval : bellInterval + bellIntervalGrowthAmount;
            newBells++;
        }
        if (topCollectible.getY() < newestBellPositionY - (gameWidth * bellInterval)) {
            Bell bell = new Bell((gameWidth * 0.03f) + MathUtils.random((gameWidth * 0.94f) - bellSize), newestBellPositionY, bellSize, bellSize, this);
            bells.add(bell);
            topCollectible = bell;
            bellSize = bellSize - bellShrinkAmount < minBellSize ? minBellSize : bellSize - bellShrinkAmount;
            bellInterval = bellInterval + bellIntervalGrowthAmount > bellMaxInterval ? bellMaxInterval : bellInterval + bellIntervalGrowthAmount;
        }
    }

    private void updateBirds(float delta) {
        for (Bird bird: birds) {
            bird.update(delta);
        }
        for (Bird bird: deadBirds) {
            birds.remove(bird);
        }
        // bird generation currently in updateBells() method
    }

    private void updateScoreEffects(float delta) {
        for (ScoreEffect scoreEffect: scoreEffects) {
            scoreEffect.update(delta);
        }
        for (ScoreEffect scoreEffect: deadScoreEffects) {
            scoreEffects.remove(scoreEffect);
        }
    }

    private void detectCollisions() {
        for (Bell bell: bells) {
            if (bunny.getY() >= bell.getY() - bellSize - bunny.getHeight() &&
                    bunny.getY() <= bell.getY() + bellSize &&
                    !bell.isDying()) {
                if (Intersector.overlaps(bunny.getBoundingCircle(), bell.getBoundingCircle())) {
                    bunny.jump();
                    bell.playSound();
                    bell.die();
                    score = score.add(nextScoreAdded);
                    scoreEffects.add(new ScoreEffect(bell.getX(), bell.getY() + bellSize, bell, NumberFormat.getNumberInstance(Locale.US).format(nextScoreAdded), this));
                    nextScoreAdded = nextScoreAdded.add(BigInteger.TEN);
                }
            }
        }
        for (Bell bell: deadBells) {
            bells.remove(bell);
        }
        deadBells.clear();
        for (Bird bird: birds) {
            if (bunny.getY() >= bird.getY() - bellSize - bunny.getHeight() &&
                    bunny.getY() <= bird.getY() + bellSize &&
                    !bird.isDying()) {
                if (Intersector.overlaps(bunny.getBoundingCircle(), bird.getBoundingCircle())) {
                    bunny.jump();
                    bird.playSound();
                    bird.die();
                    score = score.multiply(new BigInteger("2"));
                    scoreEffects.add(new ScoreEffect(bird.getX(), bird.getY() + bellSize, bird, "Double Score!", this));
                    nextScoreAdded = nextScoreAdded.add(BigInteger.TEN);
                }
            }
        }
        for (Bird bird: deadBirds) {
            birds.remove(bird);
        }
        deadBirds.clear();
    }
}
