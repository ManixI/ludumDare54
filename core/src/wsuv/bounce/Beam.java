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

    public Beam(EscapeGame game, float x, float y, float scale, Animation beamAnimation) {
        this.game = game;
        this.x = x;
        this.y = y;
        this.scale = scale;
        animation = beamAnimation;
    }
    public void update() {
        elapsed += Gdx.graphics.getDeltaTime();
    }

    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }

    public void draw (Batch batch) {
        batch.draw((TextureRegion) animation.getKeyFrame(elapsed),
                x,
                y,
                32*scale,
                32*scale);
    }
}
