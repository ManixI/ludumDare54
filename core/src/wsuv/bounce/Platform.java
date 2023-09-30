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

    static float lengthFloor = 20;
    static float maxDistance = 400;
    static float maxHeight = 115;


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
            height = game.random.nextFloat(Gdx.graphics.getHeight()-100, getY()+maxHeight);
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
        return false;
    }


    public Platform generateNext() {
        // max height is currently 90? unts
        float distX = game.random.nextFloat(getX()+getWidth(), getX()+getWidth()+maxDistance);
        int direction;
        // if platform is near bottom, weight generation upwards
        if (getY() < Gdx.graphics.getHeight()/2) {
            direction = game.random.nextInt(0,4);
        } else {
            direction = game.random.nextInt(0,2);
        }
        float distY;
        if (direction != 0) {
            distY = game.random.nextFloat(getY(), getY()+maxHeight);
        } else {
            distY = game.random.nextFloat(-(getY()+maxHeight*1.5f), getY());
        }
        return new Platform(
            game,
            distX,
            distY,
            game.random.nextFloat(1, 30));
    }

    public Enemie spawnEnemy() {
        return new Enemie(
                game,
                game.random.nextFloat(getX(), getX()+getWidth()),
                getY()+getHeight()+getHeight(),
                Enemie.SPIKES
        );
    }
}
