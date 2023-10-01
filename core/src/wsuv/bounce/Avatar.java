package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Avatar extends Sprite implements Cloneable {

    public float xVelocity = MAX_X_VELOCITY/2;
    public float yVelocity = 0;

    public static final float MIN_X_VELOCITY = 0;
    public static final float MAX_X_VELOCITY = 600;
    public static final float MAX_Y_VELOCITY = 1000;

    public static final float GRAVITY = 25;
    public static final float JUMP_VELOCITY = 800;

    public static final float CEILING_HEIGHT = 680;
    public static final float FLOOR_HEIGHT = -150;

    public float scaleFactor = 2f;

    static Animation runAnimation;
    static TextureRegion[] runFrames;
    static TextureRegion currentFrame;
    static float stateTime;



    public Avatar(BounceGame game, float startX, float startY) {
        super(game.am.get(game.PLAYER_SPRITE_2X2, Texture.class));

        //scale(scaleFactor);


        TextureRegion[][] tmp = TextureRegion.split(
                getTexture(),
                (int) (getWidth()/game.PLAYER_SPRITE_COLS),
                (int) (getHeight()/game.PLAYER_SPRITE_ROWS)
        );
        runFrames = new TextureRegion[game.PLAYER_SPRITE_ROWS * game.PLAYER_SPRITE_COLS];
        int index = 0;
        for (int y=0; y < game.PLAYER_SPRITE_ROWS; y++) {
            for (int x=0; x<game.PLAYER_SPRITE_COLS; x++) {
                runFrames[index++] = tmp[y][x];
            }
        }
        runAnimation = new Animation(0.1f, runFrames);
        //runAnimation.getKeyFrame(0, true);
        stateTime = 0;

        setCenter(startX, startY);
    }

    @Override public void draw(Batch batch) {
        stateTime += Gdx.graphics.getDeltaTime();
        currentFrame = (TextureRegion) runAnimation.getKeyFrame(stateTime, true);
        batch.draw(
                currentFrame,
                getX(),
                getY(),
                getHeight(),
                getWidth(),
                getOriginX(),
                getOriginY(),
                getScaleX(),
                getScaleY(),
                getRotation()
        );

    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean update(OrthographicCamera cam) {
        float x = getX();
        float y = getY();
        float time = Gdx.graphics.getDeltaTime();

        boolean collided = false;

        setX(x + time * xVelocity);
        setY(y + time * yVelocity);

        if (getY() > CEILING_HEIGHT) {
            setY(CEILING_HEIGHT);
            yVelocity = 0;
            // TODO: add sfx here
        } else if (getY() < FLOOR_HEIGHT) {
            setY(FLOOR_HEIGHT);
            yVelocity = 0;
            collided = true;
        }


        // set speed caps
        if (xVelocity > MAX_X_VELOCITY) {
            xVelocity = MAX_X_VELOCITY;
        }
        if (xVelocity < MIN_X_VELOCITY) {
            xVelocity = MIN_X_VELOCITY;
        }
        if (yVelocity > MAX_Y_VELOCITY) {
            yVelocity = MAX_Y_VELOCITY;
        }
        if (yVelocity < -MAX_Y_VELOCITY) {
            yVelocity = -MAX_Y_VELOCITY;
        }


        yVelocity -= GRAVITY;

        return collided;
    }

    public void respawn(float x, float y) {
        setCenter(x, y);
    }

    public void jump() {
        yVelocity = JUMP_VELOCITY;
    }
}
