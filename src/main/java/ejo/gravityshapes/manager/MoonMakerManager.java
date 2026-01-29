package ejo.gravityshapes.manager;

import com.ejo.ui.Scene;
import com.ejo.ui.element.PhysicsObject;
import com.ejo.ui.manager.SceneManager;
import com.ejo.util.math.Angle;
import com.ejo.util.math.Vector;
import ejo.gravityshapes.element.ObjectsPolygon;
import ejo.gravityshapes.element.SpecialPhysicsObject;
import ejo.gravityshapes.gravityscene.GravityScene;
import ejo.gravityshapes.util.VectorUtil;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.NoSuchElementException;
import java.util.Random;

public class MoonMakerManager extends SceneManager {

    private int G;
    private float deltaT;

    public MoonMakerManager(Scene scene, int G, float deltaT) {
        super(scene);
        this.G = G;
        this.deltaT = deltaT;
    }

    @Override
    public void onKeyPress(int key, int scancode, int action, int mods) {
        if (action != GLFW.GLFW_RELEASE && key == GLFW.GLFW_KEY_M) {
            try {
                if (scene.getMouseHoveredHandler().getTop() instanceof SpecialPhysicsObject obj)
                    spawnMoon(obj, obj.getMass() / 200, 1, obj.getPolygon().getRadius() * 2);
            } catch (NoSuchElementException e) {

            }
        }
    }

    public void spawnMoon(PhysicsObject object, double mass, float density, double moonR) {
        Random random = new Random();

        Vector spawnVec = new Vector(1,new Angle(random.nextDouble(0,Math.TAU)));
        Vector referenceV = object.getVelocity();
        double moonV = Math.pow(G * object.getMass() / moonR,0.5f);
        Vector radialOffset = spawnVec.getUnitVector().getMultiplied(moonR);

        Vector pos = object.getPos().getAdded(radialOffset);
        Vector velocity = spawnVec.getCross(Vector.K()).getMultiplied(moonV).getAdded(referenceV);
        SpecialPhysicsObject moonObject = GravityScene.createPhysicsObject(scene,random,pos,velocity,deltaT,mass,density);

        scene.addElement(moonObject,true);
    }
}
