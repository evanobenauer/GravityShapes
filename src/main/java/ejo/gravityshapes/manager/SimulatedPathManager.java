package ejo.gravityshapes.manager;

import com.ejo.ui.Scene;
import com.ejo.ui.element.DrawableElement;
import com.ejo.ui.element.Line;
import com.ejo.ui.element.shape.Rectangle;
import com.ejo.ui.element.shape.RegularPolygon;
import com.ejo.ui.element.simulation.PhysicsObject;
import com.ejo.ui.manager.SceneManager;
import com.ejo.util.math.Vector;
import com.ejo.util.misc.ColorUtil;
import ejo.gravityshapes.element.SimulatedParticle;
import ejo.gravityshapes.util.CollisionUtil;
import ejo.gravityshapes.util.PhysicsUtil;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

@Deprecated //This was made as an experiment. Don't implement
public class SimulatedPathManager extends SceneManager {

    private final int steps;
    public SimulatedPathManager(Scene scene, int steps) {
        super(scene);
        this.steps = steps;
    }

    @Override
    public void draw(Vector mousePos) {
        drawPredictedPathForAllParticles(steps);
    }

    private void drawPredictedPathForAllParticles(int steps) {
        for (DrawableElement element : scene.getDrawableElements()) {
            if (!(element instanceof PhysicsObject obj)) continue;
            SimulatedParticle particle = new SimulatedParticle(obj,steps);
            Vector[] vectors = particle.getFuturePositions((obj1, pos,i) -> {
                Vector force = Vector.NULL();
                for (DrawableElement element2 : scene.getDrawableElements()) {
                    if (!(element2 instanceof PhysicsObject p)) continue;
                    if (element2 == element) continue;
                    if (CollisionUtil.isColliding((RegularPolygon) particle.getAtPos(pos).getElement(),(RegularPolygon) p.getElement())) {
                        particle.setSteps(i); //Only call this if on combined mode
                        return force;
                    }
                    force.add(PhysicsUtil.getGravityField(1,p,pos).getMultiplied(obj1.getMass()));
                }
                return force;
            },(objy, pos, vel, force, i) ->
                    CollisionUtil.doWallBounce(scene, pos, vel, .9));

            //Draw Shooter Line
            Line line = new Line(scene, 2, Line.Type.DASHED, Color.WHITE, vectors);
            line.setFaded(true);
            line.draw();
        }
    }
}
