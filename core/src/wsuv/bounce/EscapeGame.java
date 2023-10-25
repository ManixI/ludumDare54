package wsuv.bounce;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

import java.util.Random;

public class EscapeGame extends Game {
    public static final int RSC_EXPLOSION_FRAMES_ROWS = 8;
    public static final int RSC_EXPLOSION_FRAMES_COLS = 8;
    public static final String RSC_GAMEOVER_IMG = "gameover.png";
    public static final String RSC_PRESSAKEY_IMG = "pressakey.png";
    public static final String RSC_MONO_FONT_FILE = "JetBrainsMono-Regular.ttf";
    public static final String RSC_MONO_FONT = "JBM.ttf";
    public static final String RSC_MONO_FONT_BIG = "JBMB.ttf";
    //public static final String PLATFORM_IMG = "platform.png";
    public static final String[] PLATFORM_TILES = {
            "plat-left.png",
            "plat-center.png",
            "plat-right.png",
    };
    public static final String SPEED_PLAT = "speed-pad.png";

    // powerup sprites
    public static final String ONE_UP = "1up.png";
    public static final String POINTS = "points.png";
    public static final String STAR = "star.png";


    // enemies
    public static final String SPIKES = "spikes.png";
    public static final String SPIKES_FLIPPED = "spikes-flipped.png";
    public static final String MISSILE = "missile.png";
    public static final String BEAM = "beam.gif";
    public static final String BEAM_LAUNCHER = "beam-launcher.png";
    public Animation BEAM_ANIMATION;


    public static final String[] CEILING_TILES = {
            "ceiling-1.png",
    };
    public static final String[] FLOOR_TILES = {
            "floor-1.png",
    };
    public static final String PLAYER_SPRITE_2X2 = "avatar-anim.png";
    public static final int PLAYER_SPRITE_COLS = 2;
    public static final int PLAYER_SPRITE_ROWS = 2;

    public static final String BACKGROUD = "background.png";
    public static final String BTN_RESTART = "restart.png";

    // background assets
    public static final String[] CAVE_BACKGROUND = {
            "parallax-cave/0.png",
            "parallax-cave/1.png",
            "parallax-cave/2.png",
            "parallax-cave/3.png",
            "parallax-cave/4.png",
            "parallax-cave/5.png",
            "parallax-cave/6.png",
            "parallax-cave/7.png",
    };
    public static final String[] SURFACE_BACKGROUND = {
            "parallax-scyfi/0.png",
            "parallax-scyfi/1.png",
            "parallax-scyfi/2.png",
            "parallax-scyfi/3.png",
            "parallax-scyfi/4.png",
            "parallax-scyfi/5.png",
    };
    public static final String SKY_BACKGROUND = "sky/GIF_4FPS/space4_4-frames.gif";
    public Animation SKY_BACKGROUND_ANIMATION;


    // sfx
    public static final String SFX_JUMP = "jump.wav";
    public static final String SFX_BONK = "bonk2.wav";
    public static final String SFX_STEP = "step.wav";
    public static final String SFX_HIT = "death1.wav";
    public static final String SFX_POWERUP = "powerup.wav";
    public static final String SFX_MISSILE_LAUNCH = "missileLaunch.wav";
    public static final String SFX_MISSILE_DEATH = "missileDeath.wav";
    public static final String SFX_RESTART = "restart.wav";
    public static final String SFX_BEAM_WARMUP = "beam-warmup.wav";
    public static final String SFX_BEAM_EXPLO = "beam-explo.wav";

    // music
    public static final String BGM = "polo-house.mp3";

    // debug stuff
    public static final String DBG_COLLISION_REC = "debug-collision-image.png";
    public static final String DBG_BOX = "debug-box.png";
    public static final String DBG_LINE = "lane-line.png";

    AssetManager am;  // AssetManager provides a single source for loaded resources
    SpriteBatch batch;

    Random random = new Random();

    Music music;
    @Override
    public void create() {
        am = new AssetManager();

		/* True Type Fonts are a bit of a pain. We need to tell the AssetManager
           a bit more than simply the file name in order to get them into an
           easily usable (BitMap) form...
		 */
        FileHandleResolver resolver = new InternalFileHandleResolver();
        am.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        am.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
        FreetypeFontLoader.FreeTypeFontLoaderParameter myFont = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        myFont.fontFileName = RSC_MONO_FONT_FILE;
        myFont.fontParameters.size = 14;
        am.load(RSC_MONO_FONT, BitmapFont.class, myFont);

        FreetypeFontLoader.FreeTypeFontLoaderParameter playFont = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        playFont.fontFileName = RSC_MONO_FONT_FILE;
        playFont.fontParameters.size = 28;
        am.load(RSC_MONO_FONT_BIG, BitmapFont.class, playFont);

        // Load Textures after the font...
        am.load(RSC_GAMEOVER_IMG, Texture.class);
        am.load(RSC_PRESSAKEY_IMG, Texture.class);

        // platforms
        for (String s : PLATFORM_TILES) {
            am.load(s, Texture.class);
        }
        am.load(SPEED_PLAT, Texture.class);

        // ceiling tiles
        for (String s : CEILING_TILES) {
            am.load(s, Texture.class);
        }

        // floor tiles
        for (String s : FLOOR_TILES) {
            am.load(s, Texture.class);
        }

        // background
        for (String s : SURFACE_BACKGROUND) {
            am.load(s, Texture.class);
        }
        for (String s : CAVE_BACKGROUND) {
            am.load(s, Texture.class);
        }
        SKY_BACKGROUND_ANIMATION = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal(SKY_BACKGROUND).read());


        am.load(BACKGROUD, Texture.class);
        am.load(BTN_RESTART, Texture.class);

        // powerups
        am.load(ONE_UP, Texture.class);
        am.load(POINTS, Texture.class);
        am.load(STAR, Texture.class);

        // enemies
        am.load(SPIKES, Texture.class);
        am.load(SPIKES_FLIPPED, Texture.class);
        am.load(MISSILE, Texture.class);
        am.load(BEAM_LAUNCHER, Texture.class);
        BEAM_ANIMATION = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal(BEAM).read());

        // player
        am.load(PLAYER_SPRITE_2X2, Texture.class);

        // debug stuff
        am.load(DBG_COLLISION_REC, Texture.class);
        am.load(DBG_BOX, Texture.class);
        am.load(DBG_LINE, Texture.class);

        // Load Sounds
        // sfx
        am.load(SFX_BONK, Sound.class);
        am.load(SFX_HIT, Sound.class);
        am.load(SFX_JUMP, Sound.class);
        am.load(SFX_STEP, Sound.class);
        am.load(SFX_POWERUP, Sound.class);
        am.load(SFX_MISSILE_LAUNCH, Sound.class);
        am.load(SFX_MISSILE_DEATH, Sound.class);
        am.load(SFX_RESTART, Sound.class);
        am.load(SFX_BEAM_WARMUP, Sound.class);
        am.load(SFX_BEAM_EXPLO, Sound.class);

        am.load(BGM, Music.class);

        batch = new SpriteBatch();
        setScreen(new LoadScreen(this));

        // start the music right away.
        // this one we'll only reference via the GameInstance, and it's streamed
        // so, no need to add it to the AssetManager...
        /*music = Gdx.audio.newMusic(Gdx.files.internal("sadshark.mp3"));
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();*/
    }

    @Override
    public void dispose() {
        batch.dispose();
        am.dispose();
    }
}