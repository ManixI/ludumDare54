package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Avatar extends Sprite {

    public float xVelocity = 0;
    public float yVelocity = 0;

    float maxXVelocity = 300;
    float maxYVelocity = 1000;

    float gravity = 10;
    float jumpVelocity = 400;

    public float scaleFactor = -0.5f;


    public Avatar(BounceGame game, float startX, float startY) {
        super(game.am.get("ball.png", Texture.class));

        setCenter(startX, startY);
        //scale(scaleFactor);
    }

    public boolean update() {
        float x = getX();
        float y = getY();
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        boolean collided = false;

        // set edges of camera as collision bounds for now
        if (x < 0 || (x + getWidth()) > screenWidth) {
            xVelocity = 0;
            if (x < screenWidth / 2) {
                xVelocity = 5;
                setX(0+getWidth()/2);
            } else {
                xVelocity = -5;
                setX(screenWidth-getWidth()/2);
            }
        }
        if (y < 0 || (y + getHeight()) > screenHeight) {
            yVelocity = 0;
            if (y < screenHeight / 2) {
                setY(0+getHeight()/2);
                yVelocity = 2;
            } else {
                yVelocity = -2;
                setY(screenHeight-getHeight()/2);
            }
        }

        // set speed caps
        if (xVelocity > maxXVelocity) {
            xVelocity = maxXVelocity;
            collided = true;
        }
        if (xVelocity < -maxXVelocity) {
            xVelocity = -maxXVelocity;
            collided = true;
        }
        if (yVelocity > maxYVelocity) {
            yVelocity = maxYVelocity;
            collided = true;
        }
        if (yVelocity < -maxYVelocity) {
            yVelocity = -maxYVelocity;
            collided = true;
        }

        float time = Gdx.graphics.getDeltaTime();
        setX(x + time * xVelocity);
        setY(y + time * yVelocity);

        yVelocity -= gravity;

        return collided;
    }

    public void jump() {
        yVelocity += jumpVelocity;
    }
}
