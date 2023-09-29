package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;

/**
 * An Explosion!
 */
public class Bang {
    float x, y;
    float time;
    Animation<TextureRegion> animation; // Must declare frame type (TextureRegion)

    /**
     * An Explosion
     * @param bf  - The animation data we'll use
     * @param fast - true iff we should animate quickly
     * @param x  - explosion center x
     * @param y  - explosion center y
     */
    public Bang(BangAnimationFrames bf, boolean fast, float x, float y) {
        this.x = x - bf.halfW;
        this.y = y - bf.halfH;
        time = 0;

        animation = new Animation<>(fast ? 0.06f : .12f, bf.frames);
    }
    public void draw(Batch sb) {
        time += Gdx.graphics.getDeltaTime();
        sb.draw(animation.getKeyFrame(time, false), x, y);
    }
    public boolean completed() {
        return animation.isAnimationFinished(time);
    }
}
