package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.ArrayList;

public class Platform extends Sprite {
    
    // left and right extreams as well as height of top
    float leftmost;
    float rightmost;
    float top;
    BounceGame game;

    boolean passthough = false;

    static float maxDistance = 1000;
    static float maxHeight = 115;

    public boolean isLast = false;


    private Platform(BounceGame g, float startX, float height, String type, OrthographicCamera cam) {
        super(g.am.get(type, Texture.class));
        game = g;




        //setSize(length, 10);
        scale(3);

        setCenter(startX + getWidth()/2, height - getHeight()/2);

        rightmost = startX+getWidth();
        leftmost = startX;
        top = height;
    }

    public static ArrayList<Platform> makePlat(BounceGame g, float startX, float startY, int length, OrthographicCamera cam) {
        ArrayList<Platform> plats = new ArrayList<Platform>();

        // needs to be at least 2 tiles wide to look right
        // TODO: add single tile platform texture
        if (length < 2) {
            length = 2;
        }

        float lwidth = 64;
        float cwidth = 82;

        for (int i=0; i<length; i++) {
            // leftmost tile
            if (i == 0) {
                plats.add(new Platform(
                        g,
                        startX,
                        startY,
                        BounceGame.PLATFORM_TILES[0],
                        cam
                ));
            } else if (i == length-1) {
                // rightmost tile
                plats.add(new Platform(
                        g,
                        startX+(cwidth*(i-1))+lwidth,
                        startY,
                        BounceGame.PLATFORM_TILES[2],
                        cam
                ));
                plats.get(plats.size()-1).isLast = true;
            } else {
                // center tiles
                plats.add(new Platform(
                        g,
                        startX+(cwidth*(i-1))+lwidth,
                        startY,
                        BounceGame.PLATFORM_TILES[1],
                        cam
                ));
            }
        }

        return plats;
    }

    public boolean checkCollision(Avatar ball) {
        // if ball in line with platform
        //System.out.println(ball.getX()+" "+ball.getY()+" "+ball.yVelocity);
        //System.out.println(leftmost+" "+rightmost+" "+top);
        // TODO: player clipping into platform slightly
        if (getBoundingRectangle().overlaps(ball.getBoundingRectangle())) {
            if (ball.yVelocity <= 0 && passthough == false) {
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


    public ArrayList<Platform> generateNext(OrthographicCamera cam) {
        // TODO: can spawn in floor
        // max height is currently 90? unts
        float distX = game.random.nextFloat(getX()+getWidth(), getX()+getWidth()+maxDistance);
        int direction;
        // if platform is near bottom, weight generation upwards
        if (getY() < cam.position.y - 300) {
            direction = game.random.nextInt(0,4);
        } else {
            direction = game.random.nextInt(0,2);
        }
        float distY;
        if (direction != 0) {
            distY = game.random.nextFloat(getY(), getY()+maxHeight);
        } else {
            distY = game.random.nextFloat(getY()-maxHeight*1.5f, getY());
        }
        return makePlat(
            game,
            distX,
            distY,
            game.random.nextInt(2, 5),
            cam
        );
    }

    public Enemie spawnEnemy() {
        // TODO: make sure spikes are positioned properly
        return new Enemie(
                game,
                game.random.nextFloat(getX(), getX()+getWidth()),
                getY()+getHeight()*4,
                Enemie.SPIKES
        );
    }

    public Powerup spawnPowerup() {
        if (game.random.nextInt(0, 5) ==  0) {
            String type;
            switch (game.random.nextInt(0, 10)) {
                case 0:
                    // 1up
                    type = Powerup.ONE_UP;
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                default:
                    // points
                    type = Powerup.POINTS;
                    break;
            }
            return placePowerup(type);
        } else {
            return null;
        }
    }

    private Powerup placePowerup(String type) {
        float powerupMargin = 200;
        float heightFloor = getY() + getHeight()*2 + 50;
        float heightCealing = heightFloor + maxHeight;
        return new Powerup(
                game,
                game.random.nextFloat(this.getX()-powerupMargin, this.getX()+this.getWidth()+powerupMargin),
                game.random.nextFloat(heightFloor, heightCealing),
                type

        );
    }
}
