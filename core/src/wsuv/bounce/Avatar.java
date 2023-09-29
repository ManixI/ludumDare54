package wsuv.bounce;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Avatar extends Sprite {

    float xVelocity = 0;
    float yVelocity = 0;

    float gravity = 10;
    float jumpVelocity = 500;


    public Avatar(BounceGame game, float startX, float startY) {
        super(game.am.get("ball.png", Texture.class));

        setX(startX, startY);
        scale(-0.5);
    }

    public float update() {
        
    }

    public jump() {
        yVelocity += jumpVelocity;
    }
}
