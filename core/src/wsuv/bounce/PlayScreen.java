package wsuv.bounce;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import java.awt.*;
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
    private int lives;

    private ArrayList<Bang> explosions;
    BangAnimationFrames baf;

    ArrayList<Platform>[] platformList;
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
    float camSpeed = 500;

    Texture collisionBox;
    private boolean drawDbugBox = true;
    private boolean debugCam = false;

    public PlayScreen(EscapeGame game) {
        escapeGame = game;
        //ball = new Ball(game);
        explosions = new ArrayList<>(10);
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

        //platformList = new ArrayList<Platform>();
        platformList = new ArrayList[9];
        int tmpPlatSpace = 300;
        for (int i=0; i<platformList.length; i++) {
            platformList[i] = new ArrayList<Platform>();
            platformList[i].addAll(Platform.makeFirstPlat(
                    game,
                    100,
                    200+(tmpPlatSpace*i),
                    10,
                    cam,
                    tmpPlatSpace*i,
                    400 + tmpPlatSpace*i
            ));
        }
        for (ArrayList<Platform> l : platformList) {
            int i = 0;
            ArrayList<Platform> plats;
            while (i < 10) {
                plats = l.get(l.size()-1).generateNext(cam);
                l.addAll(plats);
                i++;
            }
        }
        //platformList.add(new Platform(game, 100, 200, 10, cam));

        powerupList = new ArrayList<Powerup>();
        powerupList.add(new Powerup(game, cam.position.x, cam.position.y, Powerup.ONE_UP));
        powerupList.add(new Powerup(game, cam.position.x + 500, cam.position.y + 100, Powerup.POINTS));

        enemies = new ArrayList<Enemie>();
        //enemies.add(new Enemie(game, 500, 0, Enemie.SPIKES));
        //enemies.add(platformList.get(3).spawnEnemy());

        player = new Avatar(game, 0, 3000);

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
                // TODO: fix bug where player dosn't accelerate with game speed
                //gameSpeed += 0.08f;
            }
        },4500, 4500);


        // debug stuff
        collisionBox = game.am.get(EscapeGame.DBG_COLLISION_REC, Texture.class);


        // we've loaded textures, but the explosion texture isn't quite ready to go--
        // we need to carve it up into frames.  All that work really
        // only needs to happen once.  Since we only use explosions in the PlayScreen,
        // we'll do it here, storing the work in a special object we'll use each time
        // a new Bang instance is created...

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
        hud.registerView("Mouse:", new HUDViewCommand(HUDViewCommand.Visibility.WHEN_OPEN) {
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
                    return true;
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);

    }

    public float getCamSpeed() {
        return camSpeed;
    }

    @Override
    public void show() {
        Gdx.app.log("PlayScreen", "show");
        state = SubState.READY;
    }

    public void update(float delta) {

        if (debugCam) {
            cam.position.x = player.getX();
        } else if (player.isSpeedy) {
            cam.position.x += Avatar.MAX_X_VELOCITY * Gdx.graphics.getDeltaTime() * gameSpeed;
        } else {
            cam.position.x += Gdx.graphics.getDeltaTime() * camSpeed * gameSpeed;

        }

        if (state == SubState.PLAYING) {
            if (player.getY() < 300) {
                // camera floor
                cam.position.y = 300;
            } else if (player.getY() > 6000) {
                // camera ceiling
                cam.position.y = 6000;
            } else {
                // camera track player
                cam.position.y = player.getY();
            }
        }


        cam.update();
        hud.updateCam(cam);
        hud.updatePosCam();

        // lock player in back half of screen
        if (player.getX()+player.getWidth() >= cam.position.x) {
            player.setX(cam.position.x - player.getWidth());
        }

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
        if (state == SubState.PLAYING) {
            distance = player.getX();
            points = staticPoints + distance;
        }

        // check collision on each platform to re-set jump
        for (ArrayList<Platform> l : platformList) {
            for (Platform p : l) {
                if (p.checkCollision(player, cam, gameSpeed, camSpeed)) {
                    numJumps = totalJumps;
                    player.setAirborn(false);
                    if (p.type != Platform.PlatType.SPEED) {
                        player.isSpeedy = false;
                    }
                    break;
                }
            }
        }

        // check collision with enemies
        if (!invincible && state == SubState.PLAYING)
        {
            for (int j=0; j< enemies.size()-1; j++) {
                Enemie e = enemies.get(j);
                // only check enemy collision if close to on screen
                if (e.getX() < cam.position.x + 700
                    && e.getY() > cam.position.y - 700
                    && e.getY() < cam.position.y + 700) {
                    if (e.checkColision(player)) {
                        player.isSpeedy = false;
                        switch (e.getType()) {
                            case Enemie.MISSILE:
                                enemies.remove(j);
                                j--;
                            case Enemie.SPIKES:
                            case Enemie.SPIKES_FLIPPED:
                                lives--;
                                player.respawn(cam.position.x, cam.position.y + 150);
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
                        for (ArrayList<Platform> l : platformList) {
                            for (Platform p : l) {
                                if (e.checkColision(p)) {
                                    missileDeathSfx.play();
                                    enemies.remove(j);
                                    break;
                                }
                            }
                        }
                        // despawm missile if collides with ceiling
                    /*if (e.getY() > player.CEILING_HEIGHT-10 || e.getY() < player.FLOOR_HEIGHT+10) {
                        enemies.remove(j);
                        missileDeathSfx.play();
                        break;
                    }*/
                    }
                }
                }

        }

        if (player.update(cam, gameSpeed, camSpeed)) {
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
        for (ArrayList<Platform> l : platformList) {
            if (l.get(l.size()-1).getX() < cam.position.x + 2500) {
                for (int i=0; i<10; i++) {
                    l.addAll(l.get(l.size()-1).generateNext(cam));
                    Powerup p = l.get(l.size()-1).spawnPowerup();
                    if (p != null) {
                        powerupList.add(p);
                    }
                    if (escapeGame.random.nextInt(3) == 0) {
                        enemies.add(l.get(l.size()-1).spawnEnemy());
                    }
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
        float min = cam.position.x + 500;
        if (spikeTimer > 1000) {
            spikeTimer = 0;
            enemies.add(new Enemie(
                    escapeGame,
                    escapeGame.random.nextFloat()*150+min,
                    Avatar.FLOOR_HEIGHT + 14,
                    Enemie.SPIKES
            ));
        }
        /*if (ceilingSpikeTimer > 250) {
            ceilingSpikeTimer = 0;
            enemies.add(new Enemie(
                    escapeGame,
                    escapeGame.random.nextFloat()*100+min,
                    Avatar.CEILING_HEIGHT + 30,
                    Enemie.SPIKES_FLIPPED
            ));
        }*/
        if (missileTimer > 4000 && state == SubState.PLAYING) {
            missileTimer = 0;
            enemies.add( new Enemie(
                    escapeGame,
                    escapeGame.random.nextFloat()*100+min,
                    escapeGame.random.nextFloat( )*(cam.position.y + 300 -(cam.position.y +300))+cam.position.y + 300,
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
            //escapeGame.music.setVolume(escapeGame.music.getVolume() / 2);
        }
        /*if (state == SubState.GAME_OVER && timer > 3.0f) {
            state = SubState.READY;
        }*/
        // ignore key presses when console is open...
        if (!hud.isOpen() && state == SubState.PLAYING) {
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                player.yVelocity = 200;
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
                player.xVelocity = Avatar.MAX_X_VELOCITY*gameSpeed - 300;
                /*if (player.isSpeedy) {
                    player.xVelocity += 600;
                }*/
            } else {
                player.xVelocity = Avatar.MAX_X_VELOCITY/2*gameSpeed;
                /*if (player.isSpeedy) {
                    player.xVelocity -= 300;
                }*/
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
        for (ArrayList<Platform> l : platformList) {
            for (int i=0; i<l.size(); i++) {
                Platform plat = l.get(i);
                if (plat.getX()+plat.getWidth() < cam.position.x - 500) {
                    l.remove(i);
                    // return early rather than fixing index error
                    return;
                }
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

        Rectangle tmp;

        ScreenUtils.clear(0, 0, 0, 1);
        escapeGame.batch.setProjectionMatrix(cam.combined);
        escapeGame.batch.begin();



        // draw background
        /*Texture background = escapeGame.am.get(escapeGame.BACKGROUD, Texture.class);
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

        }*/

        for(Iterator<Bang> bi = explosions.iterator(); bi.hasNext(); ) {
            Bang b = bi.next();
            if (b.completed()) { bi.remove(); }
            else { b.draw(escapeGame.batch); }
        }
        //ball.draw(bounceGame.batch);
        for (ArrayList<Platform> l : platformList) {
            for (Platform p : l) {
                p.draw(escapeGame.batch);
                if (drawDbugBox) {
                    tmp = p.getBoundingRectangle();
                    escapeGame.batch.draw(
                            collisionBox,
                            tmp.getX(),
                            tmp.getY(),
                            tmp.getWidth(),
                            tmp.getHeight()
                    );
                }
            }
        }
        for (Powerup p : powerupList) {
            p.draw(escapeGame.batch);
            if (drawDbugBox) {
                tmp = p.getBoundingRectangle();
                escapeGame.batch.draw(
                        collisionBox,
                        tmp.getX(),
                        tmp.getY(),
                        tmp.getWidth(),
                        tmp.getHeight()
                );
            }
        }
        for (Enemie e : enemies) {
            e.draw(escapeGame.batch);
            if (drawDbugBox) {
                tmp = e.getBoundingRectangle();
                escapeGame.batch.draw(
                        collisionBox,
                        tmp.getX(),
                        tmp.getY(),
                        tmp.getWidth(),
                        tmp.getHeight()
                );
            }
        }

        // draw ceiling
        /*Texture ceilingTex = escapeGame.am.get(escapeGame.CEILING_TILES[0], Texture.class);
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
        }*/


        //bounceGame.batch.draw(bounceGame.am.get(bounceGame.CEILING_TILES[0], Texture.class), 200, 200);

        if (invincible && !drawPlayer) {

        } else if (state != SubState.PLAYING) {
            // don't draw player if not playing
            // TODO: clean up this
        } else {
            player.draw(escapeGame.batch);
            if (drawDbugBox) {
                tmp = player.getBoundingRectangle();
                escapeGame.batch.draw(
                        collisionBox,
                        tmp.getX(),
                        tmp.getY(),
                        tmp.getWidth(),
                        tmp.getHeight()
                );
            }
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

        // debug stuff goes over every other sprite


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