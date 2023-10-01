package wsuv.bounce;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class PlayScreen extends ScreenAdapter {
    private enum SubState {READY, GAME_OVER, PLAYING}
    private BounceGame bounceGame;
    private Ball ball;
    private HUD hud;
    private SubState state;
    private int bounces;
    private float timer;
    private int lives = 3;

    private Sound boomSfx;
    private ArrayList<Bang> explosions;
    BangAnimationFrames baf;

    Platform plat;
    //ArrayList<ArrayList<Platform>> platformList;
    ArrayList<Platform> platformList;
    Avatar player;
    OrthographicCamera cam;

    ArrayList<Powerup> powerupList;
    ArrayList<Enemie> enemies;

    int numJumps = 1;
    int totalJumps = 2;
    public float points = 0;
    private float staticPoints = 0;
    private float distance = 100;

    private Sound jumpSfx;
    private Sound deathSfx;
    private Sound stepSfx;
    private Sound powerupSfx;

    private boolean invincible = false;

    private boolean drawPlayer = true;

    public PlayScreen(BounceGame game) {
        timer = 0;
        bounceGame = game;
        ball = new Ball(game);
        bounces = 0;
        explosions = new ArrayList<>(10);
        boomSfx = bounceGame.am.get(BounceGame.RSC_EXPLOSION_SFX);
        cam = new OrthographicCamera(1000, 1000);
        cam.translate(500,300);
        cam.update();
        hud = new HUD(bounceGame.am.get(BounceGame.RSC_MONO_FONT_BIG), cam);

        platformList = new ArrayList<Platform>();
        platformList.addAll(Platform.makePlat(game, 100, 200, 10, cam));
        //platformList.add(new Platform(game, 100, 200, 10, cam));
        int i = 0;
        ArrayList<Platform> plats;
        while (i < 10) {
            plats = platformList.get(platformList.size()-1).generateNext(cam);
            platformList.addAll(plats);
            i++;
        }

        powerupList = new ArrayList<Powerup>();
        powerupList.add(new Powerup(game, cam.position.x, cam.position.y, Powerup.ONE_UP));
        powerupList.add(new Powerup(game, cam.position.x + 500, cam.position.y + 100, Powerup.POINTS));

        enemies = new ArrayList<Enemie>();
        //enemies.add(new Enemie(game, 500, 0, Enemie.SPIKES));
        //enemies.add(platformList.get(3).spawnEnemy());

        player = new Avatar(game, 0, 300);

        jumpSfx = bounceGame.am.get(BounceGame.SFX_JUMP);
        deathSfx = bounceGame.am.get(BounceGame.SFX_HIT);
        stepSfx = bounceGame.am.get(BounceGame.SFX_STEP);
        powerupSfx = bounceGame.am.get(BounceGame.SFX_POWERUP);

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!player.airborn) {
                    stepSfx.play();
                }
            }
        }, 0, 150);


        //player.setScale(2,2);
        //player.setSize(64,64);

        // we've loaded textures, but the explosion texture isn't quite ready to go--
        // we need to carve it up into frames.  All that work really
        // only needs to happen once.  Since we only use explosions in the PlayScreen,
        // we'll do it here, storing the work in a special object we'll use each time
        // a new Bang instance is created...
        baf = new BangAnimationFrames(bounceGame.am.get(BounceGame.RSC_EXPLOSION_FRAMES));

        // the HUD will show FPS always, by default.  Here's how
        // to use the HUD interface to silence it (and other HUD Data)
        hud.setDataVisibility(HUDViewCommand.Visibility.WHEN_OPEN);

        // HUD Console Commands
        hud.registerAction("ball", new HUDActionCommand() {
            static final String help = "Usage: ball <x> <y> <vx> <vy> | ball ";

            @Override
            public String execute(String[] cmd) {
                try {
                    float x = Float.parseFloat(cmd[1]);
                    float y = Float.parseFloat(cmd[2]);
                    float vx = Float.parseFloat(cmd[3]);
                    float vy = Float.parseFloat(cmd[4]);
                    ball.xVelocity = vx;
                    ball.yVelocity = vy;
                    ball.setCenter(x, y);
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        // HUD Data
        hud.registerView("Points:", new HUDViewCommand(HUDViewCommand.Visibility.ALWAYS) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return Integer.toString((int) points);
            }
        });hud.registerView("Lives:", new HUDViewCommand(HUDViewCommand.Visibility.ALWAYS) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return Integer.toString(lives);
            }
        });
        hud.registerView("Mouse:", new HUDViewCommand(HUDViewCommand.Visibility.ALWAYS) {
                    @Override
                    public String execute(boolean consoleIsOpen) {
                        return String.format("%.0f %.0f",
                                (float) Gdx.input.getX(), (float) Gdx.input.getY());
                    }
        });
        hud.registerView("Ball @:", new HUDViewCommand(HUDViewCommand.Visibility.WHEN_OPEN) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return String.format("%.0f %.0f [%.0f %.0f] (%d)",
                        ball.getX(), ball.getY(), ball.xVelocity, ball.yVelocity, explosions.size());
            }
        });

        /*hud.registerView("plat @:", new HUDViewCommand(HUDViewCommand.Visibility.WHEN_OPEN) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return String.format("%.0f %.0f %.0f [%.0f %.0f]",
                        platformList.leftmost, platformList.rightmost, platformList.top, platformList.getX(), platformList.getY());
            }
        });*/

        hud.registerView("player @:", new HUDViewCommand(HUDViewCommand.Visibility.WHEN_OPEN) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return String.format("%.0f %.0f [%.0f %.0f]",
                        player.getX(), player.getY(), player.xVelocity, player.yVelocity);
            }
        });

        // we're adding an input processor AFTER the HUD has been created,
        // so we need to be a bit careful here and make sure not to clobber
        // the HUD's input controls. Do that by using an InputMultiplexer
        InputMultiplexer multiplexer = new InputMultiplexer();
        // let the HUD's input processor handle things first....
        multiplexer.addProcessor(Gdx.input.getInputProcessor());
        // then pass input to our new handler...
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (character == '!') {
                    Gdx.app.log("Boom!",  "(" + explosions.size() + ")" );
                    explosions.add(new Bang(baf, false, ball.getX() + ball.getOriginX(), ball.getY() + ball.getOriginY()));
                    boomSfx.play();
                    return true;
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);

    }

    @Override
    public void show() {
        Gdx.app.log("PlayScreen", "show");
        state = SubState.READY;
        bounces = 0;
    }

    public void update(float delta) {
        timer += delta;


        /*if (player.getX() > (cam.position.x)) {
            cam.position.x = player.getX();
        }*/
        cam.position.x += Gdx.graphics.getDeltaTime() * (Avatar.MAX_X_VELOCITY/2);
        //cam.position.y = player.getY();
        /*if (cam.position.y < 300) {
            cam.position.y = 300;
        }*/
        cam.update();
        hud.updateCam(cam);
        hud.updatePosCam();

        // check powerup collision
        for (int i = 0; i<powerupList.size(); i++) {
            if (powerupList.get(i).checkCollision(player)) {
                switch (powerupList.get(i).getType()) {
                    case Powerup.ONE_UP:
                        lives++;
                        break;
                    case Powerup.POINTS:
                        staticPoints += 1000;
                        break;
                    default:
                }
                powerupList.remove(i);
                powerupSfx.play();
            }
        }

        // points are combination of the furthest distance traveled and static pickup
        if (player.getX() > distance) {
            distance = player.getX();
        }
        points = staticPoints + distance;

        // check collision on each platform to re-set jump
        for (Platform p : platformList) {
            if (p.checkCollision(player, cam)) {
                numJumps = totalJumps;
                player.setAirborn(false);
                break;
            }
        }


        // check collision with enemies
        if (!invincible)
        {
            for (Enemie e : enemies) {
                if (e.checkColision(player)) {
                    switch (e.getType()) {
                        case Enemie.SPIKES:
                            lives--;
                            player.respawn(cam.position.x, cam.position.y + 300);
                            Timer t = new Timer();
                            t.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    invincible = false;
                                }
                            }, 1500);

                            Timer b = new Timer();
                            for (int i=0; i<20; i++) {
                                b.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (drawPlayer) {
                                            drawPlayer = false;
                                        } else {
                                            drawPlayer = true;
                                        }
                                    }
                                }, (1500/20)*i);
                            }
                            invincible = true;
                            deathSfx.play();
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        if (player.update(cam)) {
            numJumps = totalJumps;
        }

        // death plane
        if (player.getY() <= cam.position.y - 650) {
            lives--;
            player.respawn(cam.position.x, cam.position.y);
        }

        // for if player gets pushed off the screen
        if (player.getX() < cam.position.x - 500) {
            lives--;
            player.respawn(cam.position.x, cam.position.y);
        }

        if (lives <= 0) {
            state = SubState.GAME_OVER;
        }


        // generate more platforms if player gets close enough to end
        if (platformList.get(platformList.size()-1).getX() < cam.position.x + 2500) {
            for (int i=0; i<10; i++) {
                platformList.addAll(platformList.get(platformList.size()-1).generateNext(cam));
                Powerup p = platformList.get(platformList.size()-1).spawnPowerup();
                if (p != null) {
                    powerupList.add(p);
                }
                if (bounceGame.random.nextInt(0, 5) == 0) {
                    enemies.add(platformList.get(platformList.size()-1).spawnEnemy());
                }
            }
        }

        // spawn enemies based on time
        // TODO: fix wierd gaps in spike spawns
        float current_time = timer;
        current_time *= 100;
        current_time = (float) Math.floor(current_time);
        //System.out.println(current_time);
        if (current_time % 100 == 0) {
            enemies.add(new Enemie(
                    bounceGame,
                    bounceGame.random.nextFloat(cam.position.x + 500, cam.position.x + 600),
                    Avatar.FLOOR_HEIGHT + 14,
                    Enemie.SPIKES
            ));
        }
        if (current_time % 50 == 0) {
            enemies.add(new Enemie(
                    bounceGame,
                    bounceGame.random.nextFloat(cam.position.x + 500, cam.position.x + 600),
                    Avatar.CEILING_HEIGHT + 30,
                    Enemie.SPIKES_FLIPPED
            ));
        }

        // cleanup stuff that leaves the screen
        cleanupEnemies(enemies);
        cleanupPlatforms();
        cleanupPowerups(powerupList);

        // always update the ball, but ignore bounces unless we're in PLAY state
        /*if (ball.update() && state == SubState.PLAYING) {
            bounces++;
            // fast explosions off walls
            explosions.add(new Bang(baf, true, ball.getX() + ball.getOriginX(), ball.getY() + ball.getOriginY()));
            boomSfx.play();

            if (bounces == 5) {
                bounceGame.music.setVolume(bounceGame.music.getVolume() * 2);
                state = SubState.GAME_OVER;
                timer = 0; // restart the timer.
            }
        }*/
        if (state == SubState.READY && Gdx.input.isKeyPressed(Input.Keys.ANY_KEY)) {
            state = SubState.PLAYING;
            bounceGame.music.setVolume(bounceGame.music.getVolume() / 2);
            bounces = 0;
        }
        /*if (state == SubState.GAME_OVER && timer > 3.0f) {
            state = SubState.READY;
        }*/
        // ignore key presses when console is open...
        if (!hud.isOpen() && state == SubState.PLAYING) {
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                player.yVelocity += 30;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                player.yVelocity -= 10;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                // so player can move off left side of screen
                if (player.getX() <= cam.position.x - 500) {
                    player.xVelocity = Avatar.MAX_X_VELOCITY/2;
                } else {
                    player.xVelocity = Avatar.MIN_X_VELOCITY;
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                player.xVelocity = Avatar.MAX_X_VELOCITY;
            } else {
                player.xVelocity = Avatar.MAX_X_VELOCITY/2;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && numJumps > 0) {
                // TODO: set timer to end jump if it doesn't get released
                player.jump();
                numJumps--;
                // TODO: second jump sfx sould be higher in pitch
                jumpSfx.play();
                player.setAirborn(true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                player.gravity = 15;
            } else {
                player.gravity = 30;
            }
        }
    }


    private void cleanupEnemies(ArrayList<Enemie> sprites) {
        for (int i=0; i<sprites.size(); i++) {
            Sprite sprite = sprites.get(i);
            if (sprite.getX()+sprite.getWidth() < cam.position.x - 500) {
                sprites.remove(i);
                // return early rather than fixing index error
                return;
            }
        }
    }

    private void cleanupPlatforms() {
        for (int i=0; i<platformList.size(); i++) {
            Platform plat = platformList.get(i);
            if (plat.getX()+plat.getWidth() < cam.position.x - 500) {
                platformList.remove(i);
                // return early rather than fixing index error
                return;
            }
        }
    }

    private void cleanupPowerups(ArrayList<Powerup> sprites) {
        for (int i=0; i<sprites.size(); i++) {
            Sprite sprite = sprites.get(i);
            if (sprite.getX()+sprite.getWidth() < cam.position.x - 500) {
                sprites.remove(i);
                // return early rather than fixing index error
                return;
            }
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        ScreenUtils.clear(0, 0, 0, 1);
        bounceGame.batch.setProjectionMatrix(cam.combined);
        bounceGame.batch.begin();



        // draw background
        Texture background = bounceGame.am.get(bounceGame.BACKGROUD, Texture.class);
        double start = (cam.position.y - 1300);
        start = Math.floor(start);
        for (int i=1; i<1000; i++) {
            for (int j=0; j<3; j++) {
                bounceGame.batch.draw(
                        background,
                        (float) start+(i*background.getWidth()*7),
                        Avatar.CEILING_HEIGHT-background.getHeight()*j*7-830,
                        background.getWidth()*7,
                        background.getHeight()*7
                );
            }

        }

        for(Iterator<Bang> bi = explosions.iterator(); bi.hasNext(); ) {
            Bang b = bi.next();
            if (b.completed()) { bi.remove(); }
            else { b.draw(bounceGame.batch); }
        }
        //ball.draw(bounceGame.batch);
        for (Platform p : platformList) {
            p.draw(bounceGame.batch);
        }

        for (Powerup p : powerupList) {
            p.draw(bounceGame.batch);
        }
        for (Enemie e : enemies) {
            e.draw(bounceGame.batch);
        }

        // draw ceiling
        Texture ceilingTex = bounceGame.am.get(bounceGame.CEILING_TILES[0], Texture.class);
        Texture floorTex = bounceGame.am.get(bounceGame.FLOOR_TILES[0], Texture.class);
        start = (cam.position.y - 600) / ceilingTex.getWidth();
        start = Math.floor(start);
        for (int i=0; i< 3000; i++) {
            bounceGame.batch.draw(
                    ceilingTex,
                    (float) start+(i* ceilingTex.getWidth()*3),
                    Avatar.CEILING_HEIGHT+ ceilingTex.getHeight(),
                    ceilingTex.getWidth()*3,
                    ceilingTex.getHeight()*3
            );
            bounceGame.batch.draw(
                    floorTex,
                    (float) start+(i*floorTex.getWidth()*3),
                    Avatar.FLOOR_HEIGHT-floorTex.getHeight()*3,
                    floorTex.getWidth()*3,
                    floorTex.getHeight()*3
            );
        }


        //bounceGame.batch.draw(bounceGame.am.get(bounceGame.CEILING_TILES[0], Texture.class), 200, 200);

        if (invincible && !drawPlayer) {

        } else {
            player.draw(bounceGame.batch);
        }

        // this logic could also be pushed into a method on SubState enum
        switch (state) {
            case GAME_OVER:
                bounceGame.batch.draw(bounceGame.am.get(BounceGame.RSC_GAMEOVER_IMG, Texture.class), cam.position.x-200, cam.position.y-200);
                break;
            case READY:
                //bounceGame.batch.draw(bounceGame.am.get(BounceGame.RSC_PRESSAKEY_IMG, Texture.class), 200, 200);
                break;
            case PLAYING:
                break;
        }
        hud.draw(bounceGame.batch);
        bounceGame.batch.end();
    }
}

/**
 * This class we'll only instantiate once; it will hold data shared
 * by all Bang instances
 */
class BangAnimationFrames {
    float halfW, halfH;
    TextureRegion[] frames;
    BangAnimationFrames(Texture spritesheet) {
        // split the single spritesheet into an array of equally sized TextureRegions
        TextureRegion[][] tmp = TextureRegion.split(spritesheet,
                spritesheet.getWidth() / BounceGame.RSC_EXPLOSION_FRAMES_COLS,
                spritesheet.getHeight() / BounceGame.RSC_EXPLOSION_FRAMES_ROWS);

        halfW = (spritesheet.getWidth() / 2f) / BounceGame.RSC_EXPLOSION_FRAMES_COLS;
        halfH = (spritesheet.getHeight() / 2f) / BounceGame.RSC_EXPLOSION_FRAMES_ROWS;

        frames = new TextureRegion[BounceGame.RSC_EXPLOSION_FRAMES_COLS * BounceGame.RSC_EXPLOSION_FRAMES_ROWS];
        int index = 0;
        for (int i = 0; i < BounceGame.RSC_EXPLOSION_FRAMES_ROWS; i++) {
            for (int j = 0; j < BounceGame.RSC_EXPLOSION_FRAMES_COLS; j++) {
                frames[index++] = tmp[i][j];
            }
        }
    }
}