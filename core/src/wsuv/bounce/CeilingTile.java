package wsuv.bounce;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CeilingTile extends Sprite {

    float scaleFactor = 1.0f;
    public CeilingTile(BounceGame game, float x, float y, int tile) {
        super(game.am.get(game.CEILING_TILES[tile], Texture.class));

        scale(scaleFactor);

        setX(x);
        setY(y);
    }
}
