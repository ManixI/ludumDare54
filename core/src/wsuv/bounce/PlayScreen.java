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
    private EscapeGame escapeGame;
    private Ball ball;
    private HUD hud;
    private SubState state;
    private int bounces;
    private float timer;
    private int lives;

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

    int numJumps;
    int totalJumps;
    public float points;
    private float staticPoints;
    private float distance;

    private Sound jumpSfx;
    private Sound deathSfx;
    private Sound stepSfx;
    private Sound powerupSfx;
    private Sound missileDeathSfx;
    private Sound missileLaunchSfx;
    private Sound restartSfx;

    private boolean invincible;

    private boolean drawPlayer;

    private Sprite restartButton;

    float spikeTimer = 0;
    float ceilingSpikeTimer = 0;
    float missileTimer = 0;
    float gameSpeed = 1.0f;

    public PlayScreen(EscapeGame game) {
        timer = 0;
        escapeGame = game;
        ball = new Ball(game);
        bounces = 0;
        explosions = new ArrayList<>(10);
        boomSfx = escapeGame.am.get(EscapeGame.RSC_EXPLOSION_SFX);
        cam = new OrthographicCamera(1000, 1000);
        cam.translate(500,300);
        cam.update();
        hud = new HUD(escapeGame.am.get(EscapeGame.RSC_MONO_FONT_BIG), cam);
        lives = 5;
        numJumps = 1;
        totalJumps = 2;
        points = 0;
        distance = 1000;
        invincible = false;
        drawPlayer = true;

        restartButton = new Sprite();
        restartButton.setTexture(escapeGame.am.get(escapeGame.BTN_RESTART));

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

        jumpSfx = escapeGame.am.get(EscapeGame.SFX_JUMP);
        deathSfx = escapeGame.am.get(EscapeGame.SFX_HIT);
        stepSfx = escapeGame.am.get(EscapeGame.SFX_STEP);
        powerupSfx = escapeGame.am.get(EscapeGame.SFX_POWERUP);
        missileDeathSfx = escapeGame.am.get(EscapeGame.SFX_MISSILE_DEATH);
        missileLaunchSfx = escapeGame.am.get(EscapeGame.SFX_MISSILE_LAUNCH);
        restartSfx = escapeGame.am.get(EscapeGame.SFX_RESTART);

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!player.airborn) {
                    stepSfx.play();
                }
            }
        }, 0, 150);

        // increase game speed by 10 base speed every 10s
        Timer b = new Timer();
        b.schedule(new TimerTask() {
            @Override
            public void run() {
                gameSpeed += 0.08f;
            }
        },4500, 4500);


        //player.setScale(2,2);
        //player.setSize(64,64);

        // we've loaded textures, but the explosion texture isn't quite ready to go--
        // we need to carve it up into frames.  All that work really
        // only needs to happen once.  Since we only use explosions in the PlayScreen,
        // we'll do it here, storing the work in a special object we'll use each time
        // a new Bang instance is created...
        baf = new BangAnimationFrames(escapeGame.am.get(EscapeGame.RSC_EXPLOSION_FRAMES));

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
        hud.registerAction("die", new HUDActionCommand() {
            @Override
            public String execute(String[] cmd) {
                //HUDViewCommand.Visibility v = hudData.get("FPS:").nextVisiblityState();
                lives = 0;
                return "killed";
            }

            public String help(String[] cmd) {
                return "kills the player";
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
        cam.position.x += Gdx.graphics.getDeltaTime() * (Avatar.MAX_X_VELOCITY/2)*gameSpeed;
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

        for (Enemie e : enemies) {
            e.update(player, gameSpeed);
        }

        // points are combination of the furthest distance traveled and static pickup
        if (player.getX() > distance) {
            distance = player.getX();
        }
        points = staticPoints + distance;

        // check collision on each platform to re-set jump
        for (Platform p : platformList) {
            if (p.checkCollision(player, cam, gameSpeed)) {
                numJumps = totalJumps;
                player.setAirborn(false);
                break;
            }
        }


        // check collision with enemies
        if (!invincible)
        {
            // TODO: bug where missiles despawn and remove spikes as well
            for (int j=0; j< enemies.size()-1; j++) {
                Enemie e = enemies.get(j);
                if (e.checkColision(player)) {
                    switch (e.getType()) {
                        case Enemie.MISSILE:
                            enemies.remove(j);
                            j--;
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
                if (e.getType() == e.MISSILE) {
                    for (Platform p : platformList) {
                        if (e.checkColision(p)) {
                            // TODO: remove and explode if collides with platform
                            enemies.remove(j);
                            missileDeathSfx.play();
                        }
                    }
                    if (e.getY() > player.CEILING_HEIGHT-10 || e.getY() < player.FLOOR_HEIGHT+10) {
                        enemies.remove(j);
                        missileDeathSfx.play();
                    }
                    // TODO: check for collision with walls, ceiling, and other enemies
                }
            }
        }

        if (player.update(cam, gameSpeed)) {
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
                if (escapeGame.random.nextInt(0, 5) == 0) {
                    enemies.add(platformList.get(platformList.size()-1).spawnEnemy());
                }
            }
        }

        // spawn enemies based on time
        // TODO: fix wierd gaps in spike spawns
        float time = Gdx.graphics.getDeltaTime();
        time *= 1000;

        missileTimer += time;
        spikeTimer += time;
        ceilingSpikeTimer += time;
        if (spikeTimer > 1000) {
            spikeTimer = 0;
            enemies.add(new Enemie(
                    escapeGame,
                    escapeGame.random.nextFloat(cam.position.x + 500, cam.position.x + 600),
                    Avatar.FLOOR_HEIGHT + 14,
                    Enemie.SPIKES
            ));
        }
        if (ceilingSpikeTimer > 250) {
            ceilingSpikeTimer = 0;
            enemies.add(new Enemie(
                    escapeGame,
                    escapeGame.random.nextFloat(cam.position.x + 500, cam.position.x + 600),
                    Avatar.CEILING_HEIGHT + 30,
                    Enemie.SPIKES_FLIPPED
            ));
        }
        if (missileTimer > 4000) {
            missileTimer = 0;
            enemies.add( new Enemie(
                    escapeGame,
                    escapeGame.random.nextFloat(cam.position.x + 500, cam.position.x + 600),
                    escapeGame.random.nextFloat(player.FLOOR_HEIGHT + 50, player.CEILING_HEIGHT - 50),
                    Enemie.MISSILE
            ));
            missileLaunchSfx.play();
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
            escapeGame.music.setVolume(escapeGame.music.getVolume() / 2);
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
                    player.xVelocity = Avatar.MAX_X_VELOCITY/2*gameSpeed;
                } else {
                    player.xVelocity = Avatar.MIN_X_VELOCITY*gameSpeed;
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                player.xVelocity = Avatar.MAX_X_VELOCITY*gameSpeed;
            } else {
                player.xVelocity = Avatar.MAX_X_VELOCITY/2*gameSpeed;
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



        if (state == SubState.GAME_OVER) {
            restartButton.setY(cam.position.y - 230);
            restartButton.setX(cam.position.x - 200);

            // button location hard code but it works
            if (Gdx.input.justTouched()) {
                System.out.println(Gdx.input.getX()+" "+Gdx.input.getY());
                float x = Gdx.input.getX();
                //Texture tex = bounceGame.am.get(BounceGame.BTN_RESTART, Texture.class);
                if (x > 320 && x < 420) {
                    float y = Gdx.input.getY();
                    if (y > 365 && y < 440) {
                        player.airborn = true;
                        restartSfx.play();
                        escapeGame.getScreen().dispose();
                        escapeGame.setScreen(new PlayScreen(escapeGame));
                    }
                }
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
        escapeGame.batch.setProjectionMatrix(cam.combined);
        escapeGame.batch.begin();



        // draw background
        Texture background = escapeGame.am.get(escapeGame.BACKGROUD, Texture.class);
        double start = (cam.position.y - 1300);
        start = Math.floor(start);
        for (int i=1; i<1000; i++) {
            for (int j=0; j<3; j++) {
                escapeGame.batch.draw(
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
            else { b.draw(escapeGame.batch); }
        }
        //ball.draw(bounceGame.batch);
        for (Platform p : platformList) {
            p.draw(escapeGame.batch);
        }

        for (Powerup p : powerupList) {
            p.draw(escapeGame.batch);
        }
        for (Enemie e : enemies) {
            e.draw(escapeGame.batch);
        }

        // draw ceiling
        Texture ceilingTex = escapeGame.am.get(escapeGame.CEILING_TILES[0], Texture.class);
        Texture floorTex = escapeGame.am.get(escapeGame.FLOOR_TILES[0], Texture.class);
        start = (cam.position.y - 600) / ceilingTex.getWidth();
        start = Math.floor(start);
        for (int i=0; i< 3000; i++) {
            escapeGame.batch.draw(
                    ceilingTex,
                    (float) start+(i* ceilingTex.getWidth()*3),
                    Avatar.CEILING_HEIGHT+ ceilingTex.getHeight(),
                    ceilingTex.getWidth()*3,
                    ceilingTex.getHeight()*3
            );
            escapeGame.batch.draw(
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
            player.draw(escapeGame.batch);
        }

        // this logic could also be pushed into a method on SubState enum
        switch (state) {
            case GAME_OVER:
                escapeGame.batch.draw(escapeGame.am.get(EscapeGame.RSC_GAMEOVER_IMG, Texture.class), cam.position.x-200, cam.position.y-200);
                //restartButton.draw(bounceGame.batch);
                escapeGame.batch.draw(
                        escapeGame.am.get(EscapeGame.BTN_RESTART, Texture.class),
                        cam.position.x-100,
                        cam.position.y-230,
                        escapeGame.am.get(EscapeGame.BTN_RESTART, Texture.class).getWidth()*2,
                        escapeGame.am.get(EscapeGame.BTN_RESTART, Texture.class).getHeight()*2
                        );
                break;
            case READY:
                //bounceGame.batch.draw(bounceGame.am.get(BounceGame.RSC_PRESSAKEY_IMG, Texture.class), 200, 200);
                break;
            case PLAYING:
                break;
        }
        hud.draw(escapeGame.batch);
        escapeGame.batch.end();
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
                spritesheet.getWidth() / EscapeGame.RSC_EXPLOSION_FRAMES_COLS,
                spritesheet.getHeight() / EscapeGame.RSC_EXPLOSION_FRAMES_ROWS);

        halfW = (spritesheet.getWidth() / 2f) / EscapeGame.RSC_EXPLOSION_FRAMES_COLS;
        halfH = (spritesheet.getHeight() / 2f) / EscapeGame.RSC_EXPLOSION_FRAMES_ROWS;

        frames = new TextureRegion[EscapeGame.RSC_EXPLOSION_FRAMES_COLS * EscapeGame.RSC_EXPLOSION_FRAMES_ROWS];
        int index = 0;
        for (int i = 0; i < EscapeGame.RSC_EXPLOSION_FRAMES_ROWS; i++) {
            for (int j = 0; j < EscapeGame.RSC_EXPLOSION_FRAMES_COLS; j++) {
                frames[index++] = tmp[i][j];
            }
        }
    }
}