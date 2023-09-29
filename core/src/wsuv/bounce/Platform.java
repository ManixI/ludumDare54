package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Platform extends Sprite {

    float x;
    float y;

    // left and right extreams as well as height of top
    float leftmost;
    float rightmost;
    float top;


    public Platform(BounceGame game, float startX, float height, float length ) {
        super(game.am.get("platform.png", Texture.class));
        //scale(length);
        setSize(length*10, 10);

        x = startX + getWidth()/2;
        y = height - getHeight()/2;
        setCenter(x, y);

        rightmost = startX;
        leftmost = startX+length;
        top = height;
    }

    public boolean checkCollision(Ball ball) {
        // if ball in line with platform
        //System.out.println(ball.getX()+" "+ball.getY()+" "+ball.yVelocity);
        //System.out.println(leftmost+" "+rightmost+" "+top);
        if (getBoundingRectangle().overlaps(ball.getBoundingRectangle())) {
            System.out.println("overlap");
            if (ball.yVelocity <= 0) {
                ball.setY(top);
                ball.yVelocity = 0;
            }
        }
        if (ball.getX() > leftmost && ball.getX() < rightmost) {
            System.out.println("within bounds");
            // and if ball is above and falling
            if (ball.yVelocity <= 0 && ball.getY() > top) {
                System.out.println("Above");
                // and if next update would put ball into or below top of platform
                if ((ball.getY() + ball.yVelocity * Gdx.graphics.getDeltaTime()) < top) {
                    System.out.println("Will Collide");
                    ball.setY(top);
                    ball.yVelocity = 0;
                    return true;
                }
            }
        }
        return false;
    }

}
