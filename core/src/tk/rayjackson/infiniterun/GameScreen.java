package tk.rayjackson.infiniterun;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class GameScreen implements Screen {
    final InfiniteRun game;

    OrthographicCamera camera;
    SpriteBatch batch;
    Texture dropImage;
    Texture bucketImage;
    Sound dropSound;
    Music rainMusic;
    Rectangle bucket;

    Vector3 touchPos;

    Array<Rectangle> rainDrops;
    long lastDropTime;

    int speedMultiplier;
    int screenWidth;
    int screenHeight;

    // Time before another drop appears in nanoseconds
    final double dropChargeTime = .5e+9;
    int lives;

    public GameScreen(final InfiniteRun game) {
        this.game = game;
        screenWidth = 800;
        screenHeight = 480;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);
        batch = new SpriteBatch();
        dropImage = new Texture("droplet.png");
        bucketImage = new Texture("bucket.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("waterdrop.wav"));
        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("undertreeinrain.mp3"));

        rainMusic.setLooping(true);
        rainMusic.play();

        bucket = new Rectangle();
        bucket.width = 64;
        bucket.height = 64;
        bucket.x = screenHeight / 2 - bucket.height / 2;
        bucket.y = 20;

        touchPos = new Vector3();

        rainDrops = new Array<Rectangle>();
        spawnRaindrop();

        speedMultiplier = 4;
        lives = 3;
    }

    private void spawnRaindrop() {
        Rectangle raindrop = new Rectangle();
        raindrop.width = 64;
        raindrop.height = 64;
        raindrop.x = MathUtils.random(screenWidth - raindrop.width);
        raindrop.y = screenHeight;
        rainDrops.add(raindrop);
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void show() {
        rainMusic.play();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, .2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.font.draw(game.batch, "Lives: " + lives, 100, screenHeight - 100);
        game.batch.draw(bucketImage, bucket.x, bucket.y);
        for (Rectangle drop : rainDrops) {
            game.batch.draw(dropImage, drop.x, drop.y);
        }
        game.batch.end();

        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = (int) touchPos.x - bucket.width / 2;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            bucket.x -= 100 * speedMultiplier * Gdx.graphics.getDeltaTime();
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            bucket.x += 100 * speedMultiplier * Gdx.graphics.getDeltaTime();
        }

        if (bucket.x < 0) {
            bucket.x = 0;
        }

        if (bucket.x > screenWidth - bucket.width) {
            bucket.x = screenWidth - bucket.width;
        }

        if (TimeUtils.nanoTime() - lastDropTime > dropChargeTime) {
            spawnRaindrop();
        }

        Iterator<Rectangle> iter = rainDrops.iterator();
        while (iter.hasNext()) {
            Rectangle raindrop = iter.next();
            raindrop.y -= 1000 * Gdx.graphics.getDeltaTime();
            if (raindrop.y < -64) {
                iter.remove();
                lives--;
            }

            if (raindrop.overlaps(bucket)) {
                iter.remove();
                dropSound.play();
            }

        }

        if (lives == 0) {
            game.setScreen(new MainMenuScreen(game));
        }

        System.out.println("Drop array length: " + rainDrops.size);
    }

    @Override
    public void resize(int width, int height) {

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

    @Override
    public void dispose() {
        dropImage.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        rainMusic.dispose();
    }
}
