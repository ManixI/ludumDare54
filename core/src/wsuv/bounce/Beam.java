package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

class Beam {

    EscapeGame game;
    float x;
    float y;
    float scale;
    Animation animation;
    float elapsed = 0;
    static float BEAM_FLOOR = -200;
    // TODO: balance beam start distance
    public static float BEAM_START_DISTANCE = 450;
    boolean active = false;

    public Beam(EscapeGame game, float x, float y, float scale, Animation beamAnimation) {
        this.game = game;
        this.x = x;
        this.y = y;
        this.scale = scale;
        animation = beamAnimation;
    }
    public void update() {
        if (active) {
            elapsed += Gdx.graphics.getDeltaTime();
        }
    }

    public void setActive() {
        active = true;
    }
    public void setInactive() {
        active = false;
    }

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }

    public void draw (Batch batch) {
        if (active) {
            // draw beam from floor to beam-height
            for (float i= BEAM_FLOOR; i<y; i+=(32*scale)) {
                batch.draw((TextureRegion) animation.getKeyFrame(elapsed),
                        x,
                        i,
                        32*scale,
                        32*scale);
            }
        }
    }
}
