package ejo.gravityshapes.manager;

import com.ejo.ui.Scene;
import com.ejo.ui.element.DrawableElement;
import com.ejo.ui.element.Line;
import com.ejo.ui.element.simulation.PhysicsObject;
import com.ejo.ui.manager.SceneManager;
import com.ejo.util.math.Vector;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;

public class ParticleTrailManager extends SceneManager {

    private int steps;
    private final HashMap<PhysicsObject, LinkedList<Vector>> previousPositions;

    public ParticleTrailManager(Scene scene, int steps) {
        super(scene);
        this.steps = steps;
        this.previousPositions = new HashMap<>();
    }

    @Override
    public void draw(Vector mousePos) {
        drawAndUpdatePreviousPositions(steps);
    }

    @Override
    public void onKeyPress(int key, int scancode, int action, int mods) {
        if (action != GLFW.GLFW_RELEASE) {
            if (key == GLFW.GLFW_KEY_UP) steps ++;
            if (key == GLFW.GLFW_KEY_DOWN) steps --;
            if (steps < 0) steps = 0;
        }
    }

    //Ya know... This kinda works pretty well... I'm happy with it
    private void drawAndUpdatePreviousPositions(int steps) {
        for (DrawableElement element : scene.getDrawableElements()) {
            if (!(element instanceof PhysicsObject obj)) continue;

            //Update Position Lists
            if (!previousPositions.containsKey(obj)) previousPositions.put(obj,new LinkedList<>());
            LinkedList<Vector> list = previousPositions.get(obj);
            list.addFirst(obj.getPos());
            if (list.size() > steps) list.removeLast();

            //Draw Trail Line
            Vector[] vertices = list.toArray(new Vector[0]);
            Line line = new Line(scene,2,Line.Type.DOTTED,Color.WHITE, vertices);
            line.setFaded(true);
            line.draw();
        }
    }
}
