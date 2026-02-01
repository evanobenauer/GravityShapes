package ejo.gravityshapes.manager;

import com.ejo.ui.Scene;
import com.ejo.ui.element.DrawableElement;
import com.ejo.ui.element.Line;
import com.ejo.ui.element.PhysicsObject;
import com.ejo.ui.manager.SceneManager;
import com.ejo.util.math.Vector;

import java.awt.*;
import java.util.*;

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
        HashSet<PhysicsObject> objSet = new HashSet<>();

        addPositionsFromElements(objSet);
        modifyAndDrawPositions(objSet);
    }

    private void addPositionsFromElements(HashSet<PhysicsObject> objSet) {
        //Add positions from elements
        for (DrawableElement element : scene.getDrawableElements()) {
            if (!(element instanceof PhysicsObject obj)) continue;
            objSet.add(obj);

            //Update Position Lists
            if (!previousPositions.containsKey(obj)) previousPositions.put(obj,new LinkedList<>());
            LinkedList<Vector> list = previousPositions.get(obj);
            list.addFirst(obj.getPos());
        }
    }

    private void modifyAndDrawPositions(HashSet<PhysicsObject> objSet) {
        //Remove positions from current elements and draw lines
        ArrayList<PhysicsObject> removalList = new ArrayList<>();

        for (PhysicsObject obj : previousPositions.keySet()) {

            //Remove previous positions if the list is too big
            LinkedList<Vector> list = previousPositions.get(obj);
            if (list.size() > steps) list.removeLast();
            if (!objSet.contains(obj) && !list.isEmpty()) list.removeLast();

            //Remove the key reference if the list is empty (particle is gone)
            if (list.isEmpty()) {
                removalList.add(obj);
                continue;
            }

            //Draw Trail Line
            Vector[] vertices = list.toArray(new Vector[0]);
            Line line = new Line(scene,2,Line.Type.DOTTED,new Color(150,150,150,200), vertices);
            line.setFaded(true);
            line.draw();
        }

        //Remove all empty lists
        for (PhysicsObject obj : removalList) previousPositions.remove(obj);
    }

}
