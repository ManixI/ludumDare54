package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Avatar extends Sprite {

    public float xVelocity = MAX_X_VELOCITY/2;
    public float yVelocity = 0;

    public static final float MIN_X_VELOCITY = 50;
    public static final float MAX_X_VELOCITY = 600;
    public static final float MAX_Y_VELOCITY = 1000;

    public static final float GRAVITY = 25;
    public static final float JUMP_VELOCITY = 800;

    public static final float CEILING_HEIGHT = 680;

    public float scaleFactor = -0.5f;


    public Avatar(BounceGame game, float startX, float startY) {
        super(game.am.get("ball.png", Texture.class));

        setCenter(startX, startY);
        //scale(scaleFactor);
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
        yVelocity += JUMP_VELOCITY;
    }
}
