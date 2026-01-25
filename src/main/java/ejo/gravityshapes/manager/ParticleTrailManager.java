package ejo.gravityshapes.manager;

import com.ejo.ui.Scene;
import com.ejo.ui.element.DrawableElement;
import com.ejo.ui.element.Line;
import com.ejo.ui.element.PhysicsObject;
import com.ejo.ui.manager.SceneManager;
import com.ejo.util.math.Vector;
import com.ejo.util.misc.ColorUtil;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class ParticleTrailManager extends SceneManager {

    private final int steps;
    private final LinkedHashMap<PhysicsObject, LinkedList<Vector>> previousPositions;

    public ParticleTrailManager(Scene scene, int steps) {
        super(scene);
        this.steps = steps;
        this.previousPositions = new LinkedHashMap<>();
    }

    @Override
    public void draw(Vector mousePos) {
        drawAndUpdatePreviousPositions(steps);
    }

    //Ya know... This kinda works pretty well... I'm happy with it
    private void drawAndUpdatePreviousPositions(int steps) {
        HashSet<PhysicsObject> objSet = new HashSet<>();
        //Add positions from elements
        for (DrawableElement element : scene.getDrawableElements()) {
            if (!(element instanceof PhysicsObject obj)) continue;
            objSet.add(obj);

            //Update Position Lists
            if (!previousPositions.containsKey(obj)) previousPositions.put(obj,new LinkedList<>());
            LinkedList<Vector> list = previousPositions.get(obj);
            list.addFirst(obj.getPos());
        }

        //Remove positions from current elements and draw lines
        for (PhysicsObject obj : previousPositions.keySet()) {
            LinkedList<Vector> list = previousPositions.get(obj);
            if (list.size() > steps) list.removeLast();
            if (!objSet.contains(obj) && !list.isEmpty()) list.removeLast();
            if (list.isEmpty()) continue;

            //Draw Trail Line
            Vector[] vertices = list.toArray(new Vector[0]);
            Line line = new Line(scene,2,Line.Type.DOTTED,new Color(150,150,150,200), vertices);
            line.setFaded(true);
            line.draw();
        }
    }

    @Deprecated //This one is probably a little faster, but the trails are instantly removed when an object collides
    private void drawAndUpdatePreviousPositionsO(int steps) {
        for (DrawableElement element : scene.getDrawableElements()) {
            if (!(element instanceof PhysicsObject obj)) continue;

            //Update Position Lists
            if (!previousPositions.containsKey(obj)) previousPositions.put(obj,new LinkedList<>());
            LinkedList<Vector> list = previousPositions.get(obj);
            list.addFirst(obj.getPos());
            if (list.size() > steps) list.removeLast();

            //Draw Trail Line
            Vector[] vertices = list.toArray(new Vector[0]);
            Line line = new Line(scene,2,Line.Type.DOTTED, Color.WHITE, vertices);
            line.setFaded(true);
            line.draw();
        }
    }
}
