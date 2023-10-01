package wsuv.bounce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

public class LoadScreen extends ScreenAdapter {
    EscapeGame escapeGame;
    int frames;
    BitmapFont font;
    int scrollrate;
    int linesShown;
    String[] credits;

    public LoadScreen(EscapeGame game) {
        escapeGame = game;
        linesShown = 10;
        scrollrate = 30;  // this is currently pixels/frame, not pixels/sec!

        // really our app will load quickly, but let's
        // fake a more complicated system... we'll wait
        // until we show all the credits...
        frames = 0;
        FileHandle file = Gdx.files.internal("credits.txt");
        credits = file.readString().split("\n");
    }

    @Override
    public void show() {
        Gdx.app.log("LoadScreen", "show");
    }

    public void render(float delta) {

        int credits_offset = frames < 120 ? 0 : (frames - 120) / scrollrate;
        ScreenUtils.clear(0, 0, 0, 1);
        // let the AssetManager load for 15 milliseconds (~1 frame)
        // this happens in another thread
        escapeGame.am.update(10);

        if (font == null && escapeGame.am.isLoaded(EscapeGame.RSC_MONO_FONT)) {
            font = escapeGame.am.get(EscapeGame.RSC_MONO_FONT);
        } else if (escapeGame.am.isFinished() && (credits_offset >= credits.length || Gdx.input.isKeyPressed(Input.Keys.SPACE)) ) {
            escapeGame.setScreen(new PlayScreen(escapeGame));
        } else if (font != null) {
            // once the font is loaded, start showing credits.
            // we'll assume a fairly smooth framerate at just start scrolling
            // at a fixed rate per frame until all credits are off screen
            // then we'll switch to the playing state.
            escapeGame.batch.begin();
            float y = 300;
            float lineHeight = font.getLineHeight();
            font.draw(escapeGame.batch, "Thanks to...", 200f, y + 2 * lineHeight);

            for(int i = 0; i < linesShown && (credits_offset + i) < credits.length; i++) {
                font.draw(escapeGame.batch, credits[credits_offset + i], 200f, y);
                y -= lineHeight;
            }
            escapeGame.batch.end();
            frames += 1;
        }
    }
}
