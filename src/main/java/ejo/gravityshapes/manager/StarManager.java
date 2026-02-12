package ejo.gravityshapes.manager;

import com.ejo.ui.Scene;
import com.ejo.ui.element.polygon.Rectangle;
import com.ejo.ui.manager.SceneManager;
import com.ejo.util.action.DoOnce;
import com.ejo.util.action.OnChange;
import com.ejo.util.math.Vector;
import com.ejo.util.time.StopWatch;
import ejo.gravityshapes.App;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class StarManager extends SceneManager {

    private final int starCount;
    private final int starSize;

    private final ArrayList<Star> stars;

    private final OnChange<Vector> doOnResize;
    private final DoOnce initAddStarsOnce;

    public StarManager(Scene scene, int starSize, int starCount) {
        super(scene);
        this.starSize = starSize;
        this.starCount = starCount;

        this.stars = new ArrayList<>();

        this.doOnResize = new OnChange<>();
        this.initAddStarsOnce = new DoOnce();
    }

    @Override
    public void draw(Vector mousePos) {
        initAddStarsOnce.run(this::addStars);
        updateStarPositionsOnResize();

        for (Star star : stars) {
            star.twinkle();
            star.draw();
        }
    }

    private void addStars() {
        Random random = new Random();
        for (int i = 0; i < starCount; i++) {
            Star obj = new Star(scene,new Vector(random.nextDouble(0, App.WINDOW.getSize().getX()), random.nextDouble(0, App.WINDOW.getSize().getY())), new Vector(starSize, starSize));
            stars.add(obj);
        }
    }

    private void updateStarPositionsOnResize() {
        doOnResize.run(App.WINDOW.getSize(),() -> {
            Random random = new Random();
            for (Star star : stars)
                star.setPos(new Vector(random.nextDouble(0, App.WINDOW.getSize().getX()), random.nextDouble(0, App.WINDOW.getSize().getY())));
        });
    }


    private static class Star extends Rectangle {

        private final StopWatch twinkleStarsWatch;

        public Star(Scene scene, Vector pos, Vector size) {
            super(scene, pos, size, Color.BLACK);
            this.twinkleStarsWatch = new StopWatch();
            twinkle();
        }

        private void twinkle() {
            twinkleStarsWatch.start();
            Random random = new Random();
            if (twinkleStarsWatch.hasTimePassedS(random.nextDouble(0,5))) {
                setColor(new Color(255, random.nextInt(125, 255), 100, 255));
                twinkleStarsWatch.restart();
            }
        }
    }
}
