package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Enemie extends Sprite {
    public final static String MISSILE = "missile.png";
    public final static String SPIKES = "spikes.png";
    public final static String SPIKES_FLIPPED = "spikes-flipped.png";
    public final static String BEAM_LAUNCHER = "beam-launcher.png";

    private final String type;
    private Platform spot;
    float scaleFactor = 1.2f;

    private final float missileSpeed = 25;
    private float xVelocity = 0;
    private float yVelocity = 0;

    private final float maxVelocity =  800;
    public enum BeamStates {OFF, WARMUP, ACTIVE, OVER}
    public BeamStates beamState = BeamStates.OFF;

    public Beam beam = null;
    EscapeGame game;

    public Enemie(EscapeGame game, float x, float y, String type) {
        super(game.am.get(type, Texture.class));
        this.game = game;
        this.type = type;

        if (Objects.equals(type, BEAM_LAUNCHER)) {
            beam = new Beam(game, x, y-(getHeight()*2), 3, game.BEAM_ANIMATION);
        }

        scale(scaleFactor);
        setX(x);
        setY(y);
    }
    // TODO: fix collision with ceiling enemies
    public void update(Avatar player, float gameSpeed) {
        switch (type) {
            case MISSILE:
                // ref: https://gamedev.stackexchange.com/questions/88317/sprite-rotation-libgdx
                float x = getX() + getWidth()/2;
                float y = getY() + getHeight()/2;
                float px = player.getX() + player.getWidth()/2;
                float py = player.getY() + player.getHeight()/2;
                float angle = MathUtils.radiansToDegrees * MathUtils.atan2(py-y, px-x);
                angle += 180;
                if (angle < 0) {
                    angle += 360;
                } else if (angle > 360) {
                    angle -= 360;
                }
                setRotation(angle);

                float time = Gdx.graphics.getDeltaTime();
                if (x > px) {
                    xVelocity -= missileSpeed*gameSpeed;
                } else {
                    xVelocity += missileSpeed*gameSpeed;
                }
                if (y < py) {
                    yVelocity += missileSpeed;
                } else {
                    yVelocity -= missileSpeed;
                }

                if (yVelocity > maxVelocity) {
                    yVelocity = maxVelocity;
                } else if (yVelocity < -maxVelocity) {
                    yVelocity = -maxVelocity;
                }
                if (xVelocity > maxVelocity*gameSpeed) {
                    xVelocity = maxVelocity*gameSpeed;
                } else if (xVelocity < -maxVelocity*gameSpeed) {
                    xVelocity = -maxVelocity*gameSpeed;
                }

                setX(getX() + xVelocity * time*gameSpeed);
                setY(getY() + yVelocity * time);
                break;
            case BEAM_LAUNCHER:
                if (beam != null) {
                    if (getX() - player.getX() < Beam.BEAM_START_DISTANCE
                            && beamState == BeamStates.OFF) {
                        beamState = BeamStates.WARMUP;
                        Sound s = game.am.get(EscapeGame.SFX_BEAM_WARMUP);
                        s.play();
                        // TODO: can I move this logic inside the beam class
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                beamState = BeamStates.ACTIVE;
                                Sound s = game.am.get(EscapeGame.SFX_BEAM_EXPLO);
                                s.play();
                            }
                        }, 600);
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                beamState = BeamStates.OVER;
                            }
                        }, 1680);

                        beam.setActive();
                    }
                    beam.update();
                }
                break;
            case SPIKES:
            default:
                break;
        }
    }

    public boolean checkColision(Sprite sprite) {
        return getBoundingRectangle().overlaps(sprite.getBoundingRectangle());
    }

    public String getType() {
        return type;
    }

    private void place(float x, ArrayList<Platform> platformlist) {
        for (Platform p : platformlist) {
            if (x > p.leftmost && x < p.rightmost) {
                setX(x);
                setY(p.top);
                spot = p;
                return;
            }
        }
        // if enemy doesn't overlap with any existing platform, make new platform for it
        /*Platform p = platform.generateNext();
        setX(p.leftmost + p.getWidth()/2);
        setY(p.top);
        spot = p;*/
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);
        if (beam != null) {
            beam.draw(batch);
        }
    }
}
