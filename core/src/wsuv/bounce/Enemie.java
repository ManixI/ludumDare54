package wsuv.bounce;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Enemie extends Sprite {
    public final static String MISSILE = "missile.png";
    public final static String SPIKES = "spikes.png";

    private String type;
    private Platform spot;
    float scaleFactor = 1.2f;

    public Enemie(BounceGame game, float x, float y, String t, Platform platform) {
        super(game.am.get(t, Texture.class));

        scale(scaleFactor);

        place(x, platform);
        type = t;
    }

    public void update(Avatar player) {
        switch (type) {
            case MISSILE:
                // TODO: do movement here
            case SPIKES:
            default:
                break;
        }
    }

    public boolean checkColision(Sprite sprite) {
        if (getBoundingRectangle().overlaps(sprite.getBoundingRectangle())) {
            return true;
        }
        return false;
    }

    public String getType() {
        return type;
    }

    private void place(float x, Platform platform) {
        Platform cursor = platform;
        while (cursor != null) {
            if (x > cursor.leftmost && x < cursor.rightmost) {
                setX(x);
                setY(cursor.top);
                spot = cursor;
                return;
            }
        }
        // if enemy doesn't overlap with any existing platform, make new platform for it
        /*Platform p = platform.generateNext();
        setX(p.leftmost + p.getWidth()/2);
        setY(p.top);
        spot = p;*/
    }
}
