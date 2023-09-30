package wsuv.bounce;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Powerup extends Sprite {
    final static String ONE_UP = "1up.png";
    final static String MOON_JUMP = "moon_jump.png";
    final static String DOUBLE_JUMP = "double_jump.png";

    public Powerup(BounceGame game, float x, float y, String type) {
        super(game.am.get(type, Texture.class));

        setX(x);
        setX(y);
    }

    public boolean checkCollision(Avatar player) {
        if (getBoundingRectangle().overlaps(player.getBoundingRectangle())) {
            return true;
        }
        return false;
    }
}
