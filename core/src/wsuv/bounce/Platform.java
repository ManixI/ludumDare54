package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Platform extends Sprite {
    
    // left and right extreams as well as height of top
    float leftmost;
    float rightmost;
    float top;
    BounceGame game;

    boolean passthough = false;

    Platform next = null;

    static float lengthFloor = 10;


    public Platform(BounceGame g, float startX, float height, float length ) {
        super(g.am.get("platform.png", Texture.class));
        game = g;
        length *= 10;
        if (length < lengthFloor) {
            length = 10;
        }

        if (height < 10) {
            height = 10;
        }
        if (height > Gdx.graphics.getHeight()-100) {
            height = Gdx.graphics.getHeight() - 100;
        }
        setSize(length, 10);

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
            if (ball.yVelocity <= 0 && passthough == false) {
                // TODO: avatar bounces on platform when scaled to any size
                ball.setY(getY()+getHeight());
                ball.yVelocity = 0;
                return true;
            } else {
                // started below platform, don't warp up if jump falls short
                passthough = true;
            }
        } else {
            passthough = false;
        }
        if (next != null) {
            return next.checkCollision(ball);
        }
        return false;
    }

    public void setNext (Platform platform) {
        next = platform;
    }

    public Platform generateNext() {
        if (next == null) {
            next = new Platform(
                    game,
                    rightmost + getWidth() + 50,
                    game.random.nextFloat(top - 100, top + 100),
                    game.random.nextFloat(1, 30));
        } else {
            Platform cursor = next;
            while (cursor.next != null) {
                cursor = cursor.next;
            }
            cursor.next = new Platform(
                    game,
                    rightmost + getWidth() + 50,
                    game.random.nextFloat(top - 100, top + 100),
                    game.random.nextFloat(1, 30));
        }
        return next;
    }
    public void generateNextN(int n) {
        Platform cursor = this;
        for (int i=0; i<n; i++) {
            cursor.generateNext();
            cursor = cursor.next;
        }
    }

    public Platform getNext() {
        return next;
    }

}
