package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.ArrayList;

public class Platform extends Sprite {
    
    // left and right extreams as well as height of top
    float leftmost;
    float rightmost;
    float top;
    EscapeGame game;

    boolean passthough = false;

    // max x distance one platform can spawn from another
    static float maxDistance = 500;
    // max y one platform can spawn from another
    static float maxHeight = 115;

    public boolean isLast = false;
    private Sound bonkSfx;

    public static final float CEILING_HEIGHT = 680;
    public static final float FLOOR_HEIGHT = -150;

    public float upperBounds;
    public float lowerBounds;
    public enum PlatType {NORMAL, SPEED, BOUNCE};
    PlatType type;

    public enum SpawnType {NORMAL, SPARSE, DENSE, NOTMAL_UP, NORMAL_DOWN}
    SpawnType spawnType;


    private Platform(
            EscapeGame g,
            float startX,
            float height,
            String tex,
            OrthographicCamera cam,
            float upper,
            float lower,
            PlatType pType,
            SpawnType sType
    ) {
        super(g.am.get(tex, Texture.class));
        game = g;

        upperBounds = upper;
        lowerBounds = lower;
        type = pType;
        spawnType = sType;

        if (height > upper) {
            height = upper;
        } else if (height < lower) {
            height = lower;
        }


        bonkSfx = game.am.get(game.SFX_BONK);

        //setSize(length, 10);
        scale(3);

        setCenter(startX + getWidth()/2, height - getHeight()/2);

        rightmost = startX+getWidth();
        leftmost = startX;
        top = height;
    }


    public static ArrayList<Platform> makeFirstPlat(
            EscapeGame g,
            float startX,
            float startY,
            int length,
            OrthographicCamera cam,
            float higherst,
            float lowest,
            SpawnType sType
    ) {
        // TODO: there's got to be a better way of doing this then copy paste
        ArrayList<Platform> plats = new ArrayList<Platform>();

        // needs to be at least 2 tiles wide to look right
        // TODO: add single tile platform texture
        if (length < 2) {
            length = 2;
        }

        float lwidth = 64;
        float cwidth = 82;

        if (startY < FLOOR_HEIGHT + 60) {
            startY = FLOOR_HEIGHT + 60;
        } /*else if (startY > CEILING_HEIGHT - 100) {
            startY = CEILING_HEIGHT - 100;
        }*/

        for (int i=0; i<length; i++) {
            // leftmost tile
            if (i == 0) {
                plats.add(new Platform(
                        g,
                        startX,
                        startY,
                        EscapeGame.PLATFORM_TILES[0],
                        cam,
                        higherst,
                        lowest,
                        PlatType.NORMAL,
                        sType
                ));
            } else if (i == length-1) {
                // rightmost tile
                plats.add(new Platform(
                        g,
                        startX+(cwidth*(i-1))+lwidth,
                        startY,
                        EscapeGame.PLATFORM_TILES[2],
                        cam,
                        higherst,
                        lowest,
                        PlatType.NORMAL,
                        sType
                ));
                plats.get(plats.size()-1).isLast = true;
            } else {
                // center tiles
                plats.add(new Platform(
                        g,
                        startX+(cwidth*(i-1))+lwidth,
                        startY,
                        EscapeGame.PLATFORM_TILES[1],
                        cam,
                        higherst,
                        lowest,
                        PlatType.NORMAL,
                        sType
                ));
            }
        }

        return plats;
    }


    public ArrayList<Platform> makePlat(EscapeGame g, float startX, float startY, int length, OrthographicCamera cam, PlatType type) {
        ArrayList<Platform> plats = new ArrayList<Platform>();

        // needs to be at least 2 tiles wide to look right
        // TODO: add single tile platform texture
        if (length < 2) {
            length = 2;
        }

        float lwidth = 64;
        float cwidth = 82;

        if (startY < FLOOR_HEIGHT + 60) {
            startY = FLOOR_HEIGHT + 60;
        } /*else if (startY > CEILING_HEIGHT - 100) {
            startY = CEILING_HEIGHT - 100;
        }*/

        for (int i=0; i<length; i++) {
            if (type == PlatType.NORMAL) {
                // leftmost tile
                if (i == 0) {
                    plats.add(new Platform(
                            g,
                            startX,
                            startY,
                            EscapeGame.PLATFORM_TILES[0],
                            cam,
                            this.upperBounds,
                            this.lowerBounds,
                            PlatType.NORMAL,
                            spawnType
                    ));
                } else if (i == length-1) {
                    // rightmost tile
                    plats.add(new Platform(
                            g,
                            startX+(cwidth*(i-1))+lwidth,
                            startY,
                            EscapeGame.PLATFORM_TILES[2],
                            cam,
                            this.upperBounds,
                            this.lowerBounds,
                            PlatType.NORMAL,
                            spawnType
                    ));
                    plats.get(plats.size()-1).isLast = true;
                } else {
                    // center tiles
                    plats.add(new Platform(
                            g,
                            startX+(cwidth*(i-1))+lwidth,
                            startY,
                            EscapeGame.PLATFORM_TILES[1],
                            cam,
                            this.upperBounds,
                            this.lowerBounds,
                            PlatType.NORMAL,
                            spawnType
                    ));
                }
            } else if (type == PlatType.SPEED) {
                plats.add(new Platform(
                        g,
                        startX+(cwidth*(i-1))+lwidth,
                        startY,
                        EscapeGame.SPEED_PLAT,
                        cam,
                        this.upperBounds,
                        this.lowerBounds,
                        type,
                        spawnType
                ));
            }

        }

        return plats;
    }

    public boolean checkCollision(Avatar player, OrthographicCamera cam, float gameSpeed, float camSpeed) {
        // if ball in line with platform
        //System.out.println(ball.getX()+" "+ball.getY()+" "+ball.yVelocity);
        //System.out.println(leftmost+" "+rightmost+" "+top);
        Avatar futurePlayer;
        try {
            futurePlayer = (Avatar) player.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        float time = Gdx.graphics.getDeltaTime();

        futurePlayer.yVelocity = 0;
        futurePlayer.update(cam, gameSpeed, camSpeed);
        if (getBoundingRectangle().overlaps(futurePlayer.getBoundingRectangle())) {
            //player.xVelocity = 0;
            if (player.getX() < getX()-player.getWidth()) {
                //player.setX(getX()-player.getWidth());
            } else {
                //player.setX(getX()+getWidth());
            }
            return false;
        } else {
            futurePlayer.xVelocity = 0;
            futurePlayer.yVelocity = player.yVelocity;
            futurePlayer.update(cam, gameSpeed, camSpeed);
            if (getBoundingRectangle().overlaps(futurePlayer.getBoundingRectangle())) {
                if (player.yVelocity <= 0 && passthough == false) {
                    // player standing on plat
                    switch (type) {
                        case SPEED:
                            player.isSpeedy = true;

                        case NORMAL:
                            player.setY(getY() + getHeight() * 2.5f + player.scaleFactor * player.getHeight());
                            player.yVelocity = 0;
                            break;
                        default:
                            break;
                    }
                    return true;
                } else {
                    // started below platform, don't warp up if jump falls short
                    /*player.setY((getY() - player.getHeight() - getHeight()*2.5f + 20));
                    player.yVelocity = 0;
                    bonkSfx.play();*/
                    passthough = true;
                    return false;
                }
            } else {
                passthough = false;
            }
        }
        return false;
    }


    public ArrayList<Platform> generateNext(OrthographicCamera cam) {
        // max height is currently 90? unts
        float distX = game.random.nextFloat()*(getX()+getWidth()+maxDistance-(getX()+getWidth()))+getX()+getWidth()+50;
        int direction;
        // if platform is near bottom, weight generation upwards
        if (getY() < cam.position.y - 300) {
            direction = game.random.nextInt(4);
        } else {
            direction = game.random.nextInt(2);
        }
        float distY;
        if (direction != 0) {
            distY = game.random.nextFloat()*maxHeight+getY();
        } else {
            distY = game.random.nextFloat()*getY()-(getY()-maxHeight*1.5f)+getY();
        }
        int t = game.random.nextInt(4);
        PlatType type;
        switch (t) {
            /*case 4:
            case 3:
            case 2:
            case 1:
                type = PlatType.NORMAL;
                break;*/
            case 0:
                type = PlatType.SPEED;
                break;
            default:
                type = PlatType.NORMAL;
                break;
        }
        return makePlat(
            game,
            distX,
            distY,
            game.random.nextInt(3)+2,
            cam,
            type
        );
    }

    public Enemie spawnEnemy() {
        // TODO: make sure spikes are positioned properly
        return new Enemie(
                game,
                game.random.nextFloat()*getWidth()+getX(),
                getY()+getHeight()*4,
                Enemie.SPIKES
        );
    }

    public Powerup spawnPowerup() {
        if (game.random.nextInt(5) ==  0) {
            String type;
            switch (game.random.nextInt( 10)) {
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
                game.random.nextFloat()*(this.getX()+this.getWidth()+powerupMargin-(this.getX()-powerupMargin))+this.getX()+this.getWidth()+powerupMargin,
                game.random.nextFloat()*(heightCealing-heightFloor)+heightCealing,
                type

        );
    }
}
