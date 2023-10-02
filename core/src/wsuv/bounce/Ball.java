package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;


public class Ball extends Sprite {

    float xVelocity;
    float yVelocity;

    public Ball(EscapeGame game) {
        super(game.am.get("ball.png", Texture.class));

        xVelocity = game.random.nextFloat()*70+80;
        yVelocity = game.random.nextFloat()*70+80;
        if (game.random.nextBoolean()) xVelocity *= -1;
        if (game.random.nextBoolean()) yVelocity *= -1;
        setCenter(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
    }

    /**
     * Update the ball's location based on time since last update and velocity.
     * update() should generally be called every frame...
     *
     * @return true iff the ball bounced in this last update.
     */
    public boolean update() {
        float x = getX();
        float y = getY();
        boolean bounced = false;

        if (x < 0 || (x + getWidth()) > Gdx.graphics.getWidth()) {
            xVelocity *= -1;
            bounced = true;
        }
        if (y < 0 || (y + getHeight()) > Gdx.graphics.getHeight()) {
            yVelocity *= -1;
            bounced = true;
        }
        float time = Gdx.graphics.getDeltaTime();
        setX(x + time * xVelocity);
        setY(y + time * yVelocity);

        return bounced;
    }
}
