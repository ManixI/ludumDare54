package wsuv.bounce;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Iterator;

public class PlayScreen extends ScreenAdapter {
    private enum SubState {READY, GAME_OVER, PLAYING}
    private BounceGame bounceGame;
    private Ball ball;
    private HUD hud;
    private SubState state;
    private int bounces;
    private float timer;

    private Sound boomSfx;
    private ArrayList<Bang> explosions;
    BangAnimationFrames baf;

    Platform plat;
    Avatar player;

    public PlayScreen(BounceGame game) {
        timer = 0;
        bounceGame = game;
        hud = new HUD(bounceGame.am.get(BounceGame.RSC_MONO_FONT));
        ball = new Ball(game);
        bounces = 0;
        explosions = new ArrayList<>(10);
        boomSfx = bounceGame.am.get(BounceGame.RSC_EXPLOSION_SFX);

        plat = new Platform(game, 100, 200, 10);
        player = new Avatar(game, Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);

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
        hud.registerView("Bounces:", new HUDViewCommand(HUDViewCommand.Visibility.ALWAYS) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return Integer.toString(bounces);
            }
        });
        hud.registerView("Ball @:", new HUDViewCommand(HUDViewCommand.Visibility.WHEN_OPEN) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return String.format("%.0f %.0f [%.0f %.0f] (%d)",
                        ball.getX(), ball.getY(), ball.xVelocity, ball.yVelocity, explosions.size());
            }
        });

        hud.registerView("plat @:", new HUDViewCommand(HUDViewCommand.Visibility.WHEN_OPEN) {
            @Override
            public String execute(boolean consoleIsOpen) {
                return String.format("%.0f %.0f %.0f [%.0f %.0f]",
                        plat.leftmost, plat.rightmost, plat.top, plat.getX(), plat.getY());
            }
        });

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

        plat.checkCollision(player);
        player.update();

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
        if (state == SubState.GAME_OVER && timer > 3.0f) {
            state = SubState.READY;
        }
        // ignore key presses when console is open...
        if (!hud.isOpen()) {
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                player.yVelocity += 15;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                player.yVelocity -= 2;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                player.xVelocity -= 2;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                player.xVelocity += 2;
            }
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        ScreenUtils.clear(0, 0, 0, 1);
        bounceGame.batch.begin();
        for(Iterator<Bang> bi = explosions.iterator(); bi.hasNext(); ) {
            Bang b = bi.next();
            if (b.completed()) { bi.remove(); }
            else { b.draw(bounceGame.batch); }
        }
        ball.draw(bounceGame.batch);
        plat.draw(bounceGame.batch);
        player.draw(bounceGame.batch);
        // this logic could also be pushed into a method on SubState enum
        switch (state) {
            case GAME_OVER:
                bounceGame.batch.draw(bounceGame.am.get(BounceGame.RSC_GAMEOVER_IMG, Texture.class), 200, 200);
                break;
            case READY:
                bounceGame.batch.draw(bounceGame.am.get(BounceGame.RSC_PRESSAKEY_IMG, Texture.class), 200, 200);
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