package ejo.gravityshapes.manager;

import com.ejo.ui.Scene;
import com.ejo.ui.element.DrawableElement;
import com.ejo.ui.element.Line;
import com.ejo.ui.element.PhysicsObject;
import com.ejo.ui.manager.SceneManager;
import com.ejo.util.math.Vector;
import com.ejo.util.misc.ColorUtil;
import ejo.gravityshapes.util.PhysicsUtil;

import java.awt.*;

public class FieldLineManager extends SceneManager {

    private final int inverseDensity;
    private final int G;

    public FieldLineManager(Scene scene, int G, int inverseDensity) {
        super(scene);
        this.G = G;
        this.inverseDensity = inverseDensity;
    }

    @Override
    public void draw(Vector mousePos) {
        drawFieldLines();
    }

    private void drawFieldLines() {
        int windowWidth = scene.getWindow().getSize().getXi();
        int windowHeight = scene.getWindow().getSize().getYi();

        for (int x = 0; x < windowWidth / inverseDensity + 1; x++) {
            for (int y = 0; y < windowHeight / inverseDensity + 1; y++) {
                Vector gravityForce = Vector.NULL();
                for (DrawableElement otherObject : scene.getDrawableElements()) {
                    if (!(otherObject instanceof PhysicsObject obj)) continue;
                    Vector gravityFromOtherObject = PhysicsUtil.getGravityField(G, obj, new Vector(x, y).getMultiplied(inverseDensity));
                    if (!(String.valueOf(gravityFromOtherObject.getMagnitude())).equals("NaN"))
                        gravityForce.add(gravityFromOtherObject);
                }
                Line lineUI = new Line(scene, new Vector(x, y).getMultiplied(inverseDensity), gravityForce.getTheta(), Math.min(Math.max(gravityForce.getMagnitude(), .2), 1) * 10, .5, Line.Type.PLAIN,ColorUtil.getWithAlpha(Color.WHITE,100));
                lineUI.draw();
            }
        }
    }

}
