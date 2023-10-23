package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.*;

public class Avatar extends Sprite implements Cloneable {

    public float xVelocity = MAX_X_VELOCITY/2;
    public float yVelocity = 0;

    public static final float MIN_X_VELOCITY = 300;
    public static final float MAX_X_VELOCITY = 1000;
    public static final float MAX_Y_VELOCITY = 1000;

    public float gravity = 25;
    public static final float JUMP_VELOCITY = 800;
    public static final float DASH_SPEED = 2000;

    public static final float CEILING_HEIGHT = 680;
    public static final float FLOOR_HEIGHT = -130;

    public float scaleFactor = 2f;

    static Animation runAnimation;
    static TextureRegion[] runFrames;
    static TextureRegion currentFrame;
    static float stateTime;

    public boolean airborne = true;
    public boolean canDash = true;
    public boolean isDashing = false;

    private final Sound bonkSfx;
    private final Sound stepSfx;
    public boolean isSpeedy = false;


    public Avatar(EscapeGame game, float startX, float startY) {
        super(game.am.get(EscapeGame.PLAYER_SPRITE_2X2, Texture.class));

        //ref: https://www.catalinmunteanu.com/libgdx-2d-animations-from-sprites/
        TextureRegion[][] tmp = TextureRegion.split(
                getTexture(),
                (int) (getWidth()/ EscapeGame.PLAYER_SPRITE_COLS),
                (int) (getHeight()/ EscapeGame.PLAYER_SPRITE_ROWS)
        );
        runFrames = new TextureRegion[EscapeGame.PLAYER_SPRITE_ROWS * EscapeGame.PLAYER_SPRITE_COLS];
        int index = 0;
        for (int y = 0; y < EscapeGame.PLAYER_SPRITE_ROWS; y++) {
            for (int x = 0; x< EscapeGame.PLAYER_SPRITE_COLS; x++) {
                runFrames[index++] = tmp[y][x];
            }
        }
        runAnimation = new Animation(0.1f, runFrames);
        stateTime = 0;
        currentFrame = (TextureRegion) runAnimation.getKeyFrame(stateTime, true);

        bonkSfx = game.am.get(EscapeGame.SFX_BONK);
        stepSfx = game.am.get(EscapeGame.SFX_STEP);

        setSize(getWidth()/ EscapeGame.PLAYER_SPRITE_COLS, getHeight()/ EscapeGame.PLAYER_SPRITE_ROWS);
        scale(scaleFactor);

        setX(startX);
        setY(startY);

    }

    @Override public void draw(Batch batch) {
        if (!airborne) {
            stateTime += Gdx.graphics.getDeltaTime();
            currentFrame = (TextureRegion) runAnimation.getKeyFrame(stateTime, true);
        } else {
            // freezes animation on this frame if airborne
            currentFrame = (TextureRegion) runAnimation.getKeyFrame(450, true);
        }

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

    public void setAirborne(boolean b) {
        airborne = b;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean update(OrthographicCamera cam, float gamespeed) {
        float x = getX();
        float y = getY();
        float time = Gdx.graphics.getDeltaTime();

        boolean collided = false;

        if (isSpeedy) {
            xVelocity = Avatar.MAX_X_VELOCITY;
        }


        // set speed caps
        if (isDashing) {
            xVelocity = DASH_SPEED;
        } else {
            if (xVelocity > MAX_X_VELOCITY * gamespeed) {
                xVelocity = MAX_X_VELOCITY * gamespeed;
            }
            if (xVelocity < MIN_X_VELOCITY * gamespeed) {
                xVelocity = MIN_X_VELOCITY * gamespeed;
            }
        }
        if (yVelocity > MAX_Y_VELOCITY) {
            yVelocity = MAX_Y_VELOCITY;
        }
        if (yVelocity < -MAX_Y_VELOCITY) {
            yVelocity = -MAX_Y_VELOCITY;
        }

        setX(x + time * xVelocity*gamespeed);
        setY(y + time * yVelocity);


        if (yVelocity < 0) {
            airborne = true;
        }

        if (getX() < cam.position.x - 500 + 10) {
            setX(cam.position.x + 10 - 500);
        } else if (getX() + getWidth() > cam.position.x + 500) {
            setX(cam.position.x+500 -getWidth());
        }


        yVelocity -= gravity;

        return collided;
    }

    public void respawn(float x, float y) {
        setCenter(x, y);
    }

    public void jump() {
        yVelocity = JUMP_VELOCITY;
    }

    public void dash(OrthographicCamera cam) {
        // TODO: dash cooldown or dash once per jump?
        if (canDash) {
            //canDash = false;
            isDashing = true;
            // TODO: this dosen't actual rotate the sprite
            rotate90(true);
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    isDashing = false;
                    rotate90(false);
                }
            }, 250);
        }
    }
}
