package wsuv.bounce;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.*;

public class PlayScreen extends ScreenAdapter {
    private enum SubState {READY, GAME_OVER, PLAYING}
    private final EscapeGame escapeGame;
    private final HUD hud;
    private SubState state;
    private int lives;

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

    private final Sound jumpSfx;
    private final Sound deathSfx;
    private final Sound stepSfx;
    private final Sound powerupSfx;
    private final Sound missileDeathSfx;
    private final Sound missileLaunchSfx;
    private final Sound restartSfx;

    private boolean invincible;

    private boolean drawPlayer;

    private final Sprite restartButton;

    // spawn timers for platform independent enemies
    float missileTimer = 0;
    float beamLauncherTimer = 0;
    float gameSpeed = 1.0f;
    float camSpeed = 500;
    static int INVUL_TIME = 1500;

    static int CAM_FLOOR = 300;
    static int CAM_CEILING = 6000;
    static int NUM_LANES = 9;

    // debug stuff
    Texture collisionBox;
    Texture laneLine;
    private boolean drawDbugBox = true;

    float elapsed = 0;
    boolean stopScroll = false;
    boolean drawDbugLanes = true;

    public PlayScreen(EscapeGame game) {
        escapeGame = game;
        //ball = new Ball(game);
        cam = new OrthographicCamera(1000, 1000);
        cam.translate(500,3000);
        cam.update();
        hud = new HUD(escapeGame.am.get(EscapeGame.RSC_MONO_FONT_BIG));
        lives = 5;
        numJumps = 1;
        totalJumps = 2;
        points = 0;
        distance = 1000;
        invincible = false;
        drawPlayer = true;

        restartButton = new Sprite();
        restartButton.setTexture(escapeGame.am.get(EscapeGame.BTN_RESTART));

        platformList = new ArrayList[NUM_LANES];
        int platLaneSpace = CAM_CEILING / NUM_LANES;
        Platform.SpawnType pType;
        for (int i=0; i<platformList.length; i++) {
            if (i%2 == 0) {
                pType = Platform.SpawnType.SPARSE;
            } else {
                pType = Platform.SpawnType.NORMAL;
            }
            platformList[i] = new ArrayList<>();
            platformList[i].addAll(Platform.makeFirstPlat(
                    game,
                    100,
                    200+(platLaneSpace*i),
                    10,
                    cam,
                    platLaneSpace*(i+1),
                    platLaneSpace*i,
                    pType
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

        powerupList = new ArrayList<>();
        powerupList.add(new Powerup(game, cam.position.x, cam.position.y, Powerup.ONE_UP));
        powerupList.add(new Powerup(game, cam.position.x + 500, cam.position.y + 100, Powerup.POINTS));

        enemies = new ArrayList<>();

        player = new Avatar(game, 100, 3000);

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
                if (!player.airborne) {
                    stepSfx.play();
                }
            }
        }, 0, 150);

        // increase game speed by 10 base speed every 10s
        Timer b = new Timer();
        b.schedule(new TimerTask() {
            @Override
            public void run() {
                // TODO: fix bug where player doesn't accelerate with game speed
                //gameSpeed += 0.08f;
            }
        },4500, 4500);


        // debug stuff
        collisionBox = game.am.get(EscapeGame.DBG_COLLISION_REC, Texture.class);
        laneLine = game.am.get(EscapeGame.DBG_LINE, Texture.class);


        // we've loaded textures, but the explosion texture isn't quite ready to go--
        // we need to carve it up into frames.  All that work really
        // only needs to happen once.  Since we only use explosions in the PlayScreen,
        // we'll do it here, storing the work in a special object we'll use each time
        // a new Bang instance is created...

        // the HUD will show FPS always, by default.  Here's how
        // to use the HUD interface to silence it (and other HUD Data)
        hud.setDataVisibility(HUDViewCommand.Visibility.WHEN_OPEN);

        // HUD Console Commands

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
        hud.registerView("Player @:", new HUDViewCommand(HUDViewCommand.Visibility.WHEN_OPEN) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return String.format("%.0f %.0f [%.0f %.0f]",
                        player.getX(), player.getY(), player.xVelocity, player.yVelocity);
            }
        });

        /*hud.registerView("plat @:", new HUDViewCommand(HUDViewCommand.Visibility.WHEN_OPEN) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return String.format("%.0f %.0f %.0f [%.0f %.0f]",
                        platformList.leftmost, platformList.rightmost, platformList.top, platformList.getX(), platformList.getY());
            }
        });*/

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
        hud.registerAction("lives", new HUDActionCommand() {
            @Override
            public String execute(String[] cmd) {
                lives += 100;
                return "100up";
            }

            public String help(String[] cmd) {
                return "gain 100 lives";
            }
        });
        hud.registerAction("godMode", new HUDActionCommand() {
            @Override
            public String execute(String[] cmd) {
                invincible = true;
                return "congrats on the promotion";
            }

            public String help(String[] cmd) {
                return "become invincible";
            }
        });
        hud.registerAction("godModeOff", new HUDActionCommand() {
            @Override
            public String execute(String[] cmd) {
                invincible = false;
                return "no longer god";
            }

            public String help(String[] cmd) {
                return "turns off perma-invincibility";
            }
        });
        hud.registerAction("stopScroll", new HUDActionCommand() {
            @Override
            public String execute(String[] cmd) {
                stopScroll = true;
                return "game speed off";
            }

            public String help(String[] cmd) {
                return "disables forced movement";
            }
        });
        hud.registerAction("startScroll", new HUDActionCommand() {
            @Override
            public String execute(String[] cmd) {
                stopScroll = false;
                return "game speed on";
            }

            public String help(String[] cmd) {
                return "enables forced movement";
            }
        });
        hud.registerAction("showBoxes", new HUDActionCommand() {
            @Override
            public String execute(String[] cmd) {
                drawDbugBox = true;
                return "drawing hitboxes";
            }

            public String help(String[] cmd) {
                return "displays hitboxes";
            }
        });
        hud.registerAction("hideBoxes", new HUDActionCommand() {
            @Override
            public String execute(String[] cmd) {
                drawDbugBox = false;
                return "hiding hitboxes";
            }

            public String help(String[] cmd) {
                return "hiding hitboxes";
            }
        });
        hud.registerAction("showLanes", new HUDActionCommand() {
            @Override
            public String execute(String[] cmd) {
                drawDbugLanes = true;
                return "showing lanes";
            }

            public String help(String[] cmd) {
                return "show platform lanes";
            }
        });
        hud.registerAction("hideLanes", new HUDActionCommand() {
            @Override
            public String execute(String[] cmd) {
                drawDbugLanes = false;
                return "hiding lanes";
            }

            public String help(String[] cmd) {
                return "hide platform lanes";
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
                //Gdx.app.log("Boom!",  "(" + explosions.size() + ")" );
                //explosions.add(new Bang(baf, false, ball.getX() + ball.getOriginX(), ball.getY() + ball.getOriginY()));
                return character == '!';
            }
        });
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void show() {
        Gdx.app.log("PlayScreen", "show");
        state = SubState.READY;
    }

    public void update(float delta) {

        if (hud.isOpen()) {
            return;
        }

        if (state == SubState.PLAYING) {
            if (stopScroll) {
                cam.position.x = player.getX();
            } else if (player.isDashing) {
                cam.position.x += Avatar.DASH_SPEED * delta * gameSpeed;
                player.rotate90(true);
                // TODO: add sfx and graphic here
            } else if (player.isSpeedy) {
                cam.position.x += Avatar.MAX_X_VELOCITY * delta * gameSpeed;
            } else {
                cam.position.x += delta * camSpeed * gameSpeed;
            }
        }

        if (state == SubState.PLAYING) {
            if (player.getY() < CAM_FLOOR) {
                // camera floor
                cam.position.y = CAM_FLOOR;
            } else if (player.getY() > CAM_CEILING) {
                // camera ceiling
                cam.position.y = CAM_CEILING;
            } else {
                // camera track player
                cam.position.y = player.getY();
            }
        }


        cam.update();
        hud.updatePosCam(cam);

        // lock player in back half of screen
        if (!stopScroll) {
            if (player.getX()+player.getWidth() >= cam.position.x) {
                player.setX(cam.position.x - player.getWidth());
            }
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
                    case Powerup.STAR:
                        setInvulTimer(5000);
                        // TODO: add invincible music here
                    default:
                }
                powerupList.remove(i);
                powerupSfx.play();
                i--;
            }
        }

        for (Enemie e : enemies) {
            e.update(player, gameSpeed);
            if (Objects.equals(e.getType(), Enemie.BEAM_LAUNCHER)) {
                // start beam related timers
                /*if (e.getX() <= cam.position.x + 380 && e.beamState == Enemies.BeamStates.OFF) {
                }*/
                if (e.beamState == Enemie.BeamStates.ACTIVE
                        && !invincible
                        && state == SubState.PLAYING) {
                    float beamLeft = e.getX() + e.getWidth()/3;
                    float beamRight = beamLeft + e.getWidth()/3;
                    if (((player.getX() < beamRight
                            && player.getX() > beamLeft)
                            || (player.getX() + player.getWidth() > beamRight
                            && player.getX() + player.getWidth() < beamLeft))
                            && player.getY() < e.getY() - e.getHeight()/2) {
                        takeHit();
                    }
                }
            }
        }

        // points are combination of the furthest distance traveled and static pickup
        if (state == SubState.PLAYING) {
            distance = player.getX();
            points = staticPoints + distance;
        }

        // check collision on each platform to re-set jump
        for (ArrayList<Platform> l : platformList) {
            for (Platform p : l) {
                if (p.getX() < cam.position.x + 100
                    && p.getY() > cam.position.y - 700
                    && p.getY() < cam.position.y + 700) {
                    if (p.checkCollision(player, cam, gameSpeed, camSpeed)) {
                        numJumps = totalJumps;
                        player.setAirborne(false);
                        if (p.type != Platform.PlatType.SPEED) {
                            player.isSpeedy = false;
                        }
                        break;
                    }
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
                            case Enemie.BEAM_LAUNCHER:
                                enemies.remove(j);
                                j--;
                                staticPoints += 2500;
                                // TODO: sfx here
                                break;
                            case Enemie.MISSILE:
                                enemies.remove(j);
                                j--;
                            case Enemie.SPIKES:
                            case Enemie.SPIKES_FLIPPED:
                                takeHit();
                                break;
                            default:
                                break;
                        }
                    }
                    if (Objects.equals(e.getType(), Enemie.MISSILE)) {
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

        if (player.update(cam, gameSpeed)) {
            numJumps = totalJumps;
        }

        // death plane
        if (player.getY() <= cam.position.y - 650) {
            lives = 0;
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
        beamLauncherTimer += time;
        float min = cam.position.x + 500;

        if (missileTimer > 4500 && state == SubState.PLAYING) {
            missileTimer = 0;
            enemies.add( new Enemie(
                    escapeGame,
                    escapeGame.random.nextFloat()*100+min,
                    escapeGame.random.nextFloat()*(0.0f)+cam.position.y + 300,
                    Enemie.MISSILE
            ));
            missileLaunchSfx.play();
        }
        if (beamLauncherTimer > 4000 && state == SubState.PLAYING) {
            beamLauncherTimer = 0;
            enemies.add( new Enemie(
                    escapeGame,
                    escapeGame.random.nextFloat()*100+min,
                    CAM_CEILING - 300,
                    Enemie.BEAM_LAUNCHER
            ));
            // TODO: add beam sfx
        }

        // cleanup stuff that leaves the screen
        cleanupEnemies(enemies);
        cleanupPlatforms();
        cleanupPowerups(powerupList);

        // always update the ball, but ignore bounces unless we're in PLAY state
        if (state == SubState.READY && Gdx.input.isKeyPressed(Input.Keys.ANY_KEY)) {
            state = SubState.PLAYING;
            //escapeGame.music.setVolume(escapeGame.music.getVolume() / 2);
        }
        // ignore key presses when console is open...
        if (hud.isOpen() && state == SubState.PLAYING) {
            if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
                player.yVelocity = 200;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                player.yVelocity -= 10;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                // so player can move off left side of screen
                if (stopScroll) {
                    player.xVelocity = -Avatar.MIN_X_VELOCITY*gameSpeed;
                } else {
                    if (player.getX() <= cam.position.x - 500) {
                        player.xVelocity = Avatar.MAX_X_VELOCITY/2*gameSpeed;
                    } else {
                        player.xVelocity = Avatar.MIN_X_VELOCITY*gameSpeed;
                    }
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                player.xVelocity = Avatar.MAX_X_VELOCITY*gameSpeed - 300;
                /*if (player.isSpeedy) {
                    player.xVelocity += 600;
                }*/
            } else {
                if (stopScroll) {
                    player.xVelocity = 0;
                } else {
                    player.xVelocity = Avatar.MAX_X_VELOCITY/2*gameSpeed;
                }
            }
            if ((Gdx.input.isKeyJustPressed(Input.Keys.W)
                    || Gdx.input.isKeyJustPressed(Input.Keys.UP))
                    && numJumps > 0) {
                // TODO: set timer to end jump if it doesn't get released
                player.jump();
                numJumps--;
                // TODO: second jump sfx should be higher in pitch
                jumpSfx.play();
                player.setAirborne(true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.W)
                    || Gdx.input.isKeyPressed(Input.Keys.UP)) {
                player.gravity = 15;
            } else {
                player.gravity = 30;
            }
        } else if (!hud.isOpen() && state == SubState.PLAYING) {
            if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
                player.yVelocity = 200;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)
                    || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                player.yVelocity -= 10;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)
                    || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                // so player can move off left side of screen
                if (stopScroll) {
                    player.xVelocity = -Avatar.MIN_X_VELOCITY*gameSpeed;
                } else {
                    if (player.getX() <= cam.position.x - 500) {
                        player.xVelocity = Avatar.MAX_X_VELOCITY/2*gameSpeed;
                    } else {
                        player.xVelocity = Avatar.MIN_X_VELOCITY*gameSpeed;
                    }
                }
            } else if (Gdx.input.isKeyPressed(Input.Keys.D)
                    || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                player.xVelocity = Avatar.MAX_X_VELOCITY*gameSpeed - 300;
                /*if (player.isSpeedy) {
                    player.xVelocity += 600;
                }*/
            } else {
                if (stopScroll) {
                    player.xVelocity = 0;
                } else {
                    player.xVelocity = Avatar.MAX_X_VELOCITY/2*gameSpeed;
                }
                /*if (player.isSpeedy) {
                    player.xVelocity -= 300;
                }*/
            }
            if ((Gdx.input.isKeyJustPressed(Input.Keys.UP)
                    || Gdx.input.isKeyJustPressed(Input.Keys.W))
                    && numJumps > 0) {
                // TODO: set timer to end jump if it doesn't get released
                player.jump();
                numJumps--;
                // TODO: second jump sfx should be higher in pitch
                jumpSfx.play();
                player.setAirborne(true);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                player.dash(cam);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.UP)
                    || Gdx.input.isKeyPressed(Input.Keys.W)) {
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
                        player.airborne = true;
                        restartSfx.play();
                        escapeGame.getScreen().dispose();
                        PlayScreen next = new PlayScreen(escapeGame);
                        escapeGame.setScreen(next);
                        next.state = SubState.PLAYING;
                    }
                }
            }
        }
    }

    void takeHit() {
        lives--;
        //player.respawn(cam.position.x, cam.position.y + 150);
        deathSfx.play();
        setInvulTimer(INVUL_TIME);
    }

    private void setInvulTimer(int duration) {
        invincible = true;
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                invincible = false;
            }
        }, duration);

        int invluFrames = duration / 75;
        for (int i=0; i<invluFrames; i++) {
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    drawPlayer = !drawPlayer;
                }
            }, (long) (duration / invluFrames) *i);
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
        if (drawDbugLanes) {
            for (int i=0; i<platformList.length; i++) {
                for (int j=-100; j<1100; j+=laneLine.getWidth()) {
                    escapeGame.batch.draw(
                            laneLine,
                            cam.position.x - 500 + j,
                            i * (CAM_CEILING / NUM_LANES),
                            laneLine.getWidth(),
                            laneLine.getHeight()*3
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

        // needed for beam launcher
        elapsed += Gdx.graphics.getDeltaTime();

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

        if (!(invincible && !drawPlayer)) {
            if (state == SubState.PLAYING) {
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