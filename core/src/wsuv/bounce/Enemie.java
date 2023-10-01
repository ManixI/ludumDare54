package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

public class Enemie extends Sprite {
    public final static String MISSILE = "missile.png";
    public final static String SPIKES = "spikes.png";
    public final static String SPIKES_FLIPPED = "spikes-flipped.png";

    private String type;
    private Platform spot;
    float scaleFactor = 1.2f;

    private float missileSpeed = 100;
    private float xVelocity = 0;
    private float yVelocity = 0;

    private float maxVelocity =  1000;

    public Enemie(BounceGame game, float x, float y, String t) {
        super(game.am.get(t, Texture.class));

        scale(scaleFactor);

        setX(x);
        setY(y);
        type = t;
    }
    // TODO: fix collision with ceiling enemies
    public void update(Avatar player) {
        switch (type) {
            case MISSILE:
                // ref: https://gamedev.stackexchange.com/questions/88317/sprite-rotation-libgdx
                float x = getX() + getWidth()/2;
                float y = getY() + getHeight()/2;
                float px = player.getX() + player.getWidth()/2;
                float py = player.getY() + player.getHeight()/2;
                float angle = MathUtils.radiansToDegrees * MathUtils.atan2(py-y, px-x);
                angle += 180;
                if (angle < 0) {
                    angle += 360;
                } else if (angle > 360) {
                    angle -= 360;
                }
                setRotation(angle);

                float time = Gdx.graphics.getDeltaTime();
                if (x > px) {
                    xVelocity -= missileSpeed;
                } else {
                    xVelocity += missileSpeed;
                }
                if (y < py) {
                    yVelocity += missileSpeed;
                } else {
                    yVelocity -= missileSpeed;
                }

                if (yVelocity > maxVelocity) {
                    yVelocity = maxVelocity;
                } else if (yVelocity < -maxVelocity) {
                    yVelocity = -maxVelocity;
                }
                if (xVelocity > maxVelocity) {
                    xVelocity = maxVelocity;
                } else if (xVelocity < -maxVelocity) {
                    xVelocity = -maxVelocity;
                }

                setX(getX() + xVelocity * time);
                setY(getY() + yVelocity * time);

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

    private void place(float x, ArrayList<Platform> platformlist) {
        for (Platform p : platformlist) {
            if (x > p.leftmost && x < p.rightmost) {
                setX(x);
                setY(p.top);
                spot = p;
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
