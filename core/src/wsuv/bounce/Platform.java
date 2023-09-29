package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Platform extends Sprite {
    
    // left and right extreams as well as height of top
    float leftmost;
    float rightmost;
    float top;

    boolean passthough = false;


    public Platform(BounceGame game, float startX, float height, float length ) {
        super(game.am.get("platform.png", Texture.class));
        //scale(length);
        setSize(length*10, 10);

        setCenter(startX + getWidth()/2, height - getHeight()/2);

        rightmost = startX;
        leftmost = startX+length;
        top = height;
    }

    public boolean checkCollision(Avatar ball) {
        // if ball in line with platform
        //System.out.println(ball.getX()+" "+ball.getY()+" "+ball.yVelocity);
        //System.out.println(leftmost+" "+rightmost+" "+top);
        if (getBoundingRectangle().overlaps(ball.getBoundingRectangle())) {
            System.out.println("overlap");
            if (ball.yVelocity <= 0 && passthough == false) {
                ball.setY(top);
                ball.yVelocity = 0;
            } else {
                // started below platform, don't warp up if jump falls short
                passthough = true;
            }
        } else {
            passthough = false;
        }
        return false;
    }

}
