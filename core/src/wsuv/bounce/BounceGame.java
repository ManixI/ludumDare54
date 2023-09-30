package wsuv.bounce;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

import java.util.Random;

public class BounceGame extends Game {
    public static final int RSC_EXPLOSION_FRAMES_ROWS = 8;
    public static final int RSC_EXPLOSION_FRAMES_COLS = 8;
    public static final String RSC_EXPLOSION_FRAMES = "explosion8x8.png";
    public static final String RSC_GAMEOVER_IMG = "gameover.png";
    public static final String RSC_PRESSAKEY_IMG = "pressakey.png";
    public static final String RSC_BALL_IMG = "ball.png";
    public static final String RSC_MONO_FONT_FILE = "JetBrainsMono-Regular.ttf";
    public static final String RSC_MONO_FONT = "JBM.ttf";
    public static final String RSC_MONO_FONT_BIG = "JBMB.ttf";
    public static final String RSC_EXPLOSION_SFX = "explosion7s.wav";
    public static final String PLATFORM_IMG = "platform.png";

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
        am.load(RSC_BALL_IMG, Texture.class);
        am.load(RSC_GAMEOVER_IMG, Texture.class);
        am.load(RSC_PRESSAKEY_IMG, Texture.class);
        am.load(RSC_EXPLOSION_FRAMES, Texture.class);
        am.load(PLATFORM_IMG, Texture.class);

        // Load Sounds
        am.load(RSC_EXPLOSION_SFX, Sound.class);

        batch = new SpriteBatch();
        setScreen(new LoadScreen(this));

        // start the music right away.
        // this one we'll only reference via the GameInstance, and it's streamed
        // so, no need to add it to the AssetManager...
        music = Gdx.audio.newMusic(Gdx.files.internal("sadshark.mp3"));
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
    }

    @Override
    public void dispose() {
        batch.dispose();
        am.dispose();
    }
}