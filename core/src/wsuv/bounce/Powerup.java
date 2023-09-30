package wsuv.bounce;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Powerup extends Sprite {
    final static String ONE_UP = "1up.png";
    final static String MOON_JUMP = "moon_jump.png";
    final static String DOUBLE_JUMP = "double_jump.png";

    String type;
    float scaleFactor = 1.2f;

    public Powerup(BounceGame game, float x, float y, String t) {
        super(game.am.get(t, Texture.class));

        type = t;

        scale(scaleFactor);

        setX(x);
        setY(y);
    }

    public boolean checkCollision(Avatar player) {
        if (getBoundingRectangle().overlaps(player.getBoundingRectangle())) {
            return true;
        }
        return false;
    }

    public String getType() {
        return type;
    }
}
