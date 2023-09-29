package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Avatar extends Sprite {

    public float xVelocity = 0;
    public float yVelocity = 0;

    float maxXVelocity = 300;
    float maxYVelocity = 100;

    float gravity = 5;
    float jumpVelocity = 100;


    public Avatar(BounceGame game, float startX, float startY) {
        super(game.am.get("ball.png", Texture.class));

        setCenter(startX, startY);
        scale(-0.5f);
    }

    public void update() {
        float x = getX();
        float y = getY();
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // set edges of camera as collision bounds for now
        if (x < 0 || (x + getWidth()) > screenWidth) {
            xVelocity = 0;
            if (x < screenWidth / 2) {
                xVelocity = 5;
            } else {
                xVelocity = -5;
            }
        }
        if (y < 0 || (y + getHeight()) > screenHeight) {
            yVelocity = 0;
            if (y < screenHeight / 2) {
                yVelocity = 2;
            } else {
                yVelocity = -2;
            }
        }

        // set speed caps
        if (xVelocity > maxXVelocity) {
            xVelocity = maxXVelocity;
        }
        if (xVelocity < -maxXVelocity) {
            xVelocity = -maxXVelocity;
        }
        if (yVelocity > maxYVelocity) {
            yVelocity = maxYVelocity;
        }
        if (yVelocity < -maxYVelocity) {
            yVelocity = -maxYVelocity;
        }

        float time = Gdx.graphics.getDeltaTime();
        setX(x + time * xVelocity);
        setY(y + time * yVelocity);

        yVelocity -= gravity;
    }

    public void jump() {
        yVelocity += jumpVelocity;
    }
}
