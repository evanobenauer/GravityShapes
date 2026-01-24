package ejo.gravityshapes.gravityscene;

import com.ejo.ui.Scene;
import com.ejo.ui.element.base.Tickable;
import com.ejo.ui.element.shape.RegularPolygon;
import com.ejo.ui.element.simulation.PhysicsObject;
import com.ejo.ui.element.simulation.PhysicsObjectDraggable;
import com.ejo.ui.manager.DebugManager;
import com.ejo.util.math.Angle;
import com.ejo.util.math.Vector;
import ejo.gravityshapes.App;
import ejo.gravityshapes.TitleScene;
import ejo.gravityshapes.manager.ParticleTrailManager;
import ejo.gravityshapes.manager.ShootManager;
import ejo.gravityshapes.manager.SimulatedPathManager;
import ejo.gravityshapes.manager.StarManager;
import ejo.gravityshapes.util.CollisionUtil;
import ejo.gravityshapes.util.PhysicsUtil;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Random;

public abstract class GravityScene extends Scene {

    protected final StarManager starManager;
    protected final boolean applyGravity;
    protected final boolean wallBounce;

    public GravityScene(String title, boolean applyGravity, boolean wallBounce, int objectCount, String spawnMode, int minM, int maxM) {
        super(title);
        this.applyGravity = applyGravity;
        this.wallBounce = wallBounce;

        this.starManager = new StarManager(this, 1, 250);
        setDebugManager(new GravityDebugManager(this));

        switch (spawnMode) {
            case "Random" ->
                    generateObjectsRandomly(objectCount, minM, maxM, 1, .4f, .1f, .05f);
            case "Radial" ->
                    generateObjectsRadially(objectCount, 10, App.WINDOW.getSize().getYi() / 2, minM, maxM, 1, .04f, .1f, .05f);

        }

        addSceneManagers(new ShootManager(this,256,minM,maxM));

        //These are experimental and just for fun. Remove later
        addSceneManagers(new SimulatedPathManager(this,50));
        addSceneManagers(new ParticleTrailManager(this,50));
    }

    @Override
    public void draw() {
        //Draw all stars. StarManager is not added to the scene managers to specifically draw them underneath
        starManager.draw(getMousePos());

        super.draw();
    }

    @Override
    public void tick() {
        for (Tickable e1 : tickables) {
            if (!(e1 instanceof PhysicsObject obj1)) continue;
            if (tickables.isRemovalQueued(obj1)) continue;

            //Wall Bounce
            if (wallBounce) CollisionUtil.doWallBounce(obj1, .9);

            //Gravity Force
            if (applyGravity) {
                for (Tickable e2 : tickables) {
                    if (e1.equals(e2) || !(e2 instanceof PhysicsObject obj2)) continue;
                    if (tickables.isRemovalQueued(obj2)) continue;
                    obj1.addForce(PhysicsUtil.getGravityForce(obj1, obj2));
                }
            }
        }
        super.tick();
    }

    @Override
    public void onKeyPress(int key, int scancode, int action, int mods) {
        super.onKeyPress(key, scancode, action, mods);
        if (key == GLFW.GLFW_KEY_ESCAPE) getWindow().setScene(new TitleScene());
    }

    private void generateObjectsRandomly(int count, int minM, int maxM, float density, float minV, float maxV, float deltaT) {
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            long m = minM >= maxM ? minM : random.nextInt(minM, maxM);
            double r = Math.pow(3 * m / (4 * Math.PI * density), 1f / 3); //3D

            //Random Positioning
            Vector pos = new Vector(random.nextDouble(App.WINDOW.getSize().getX()), random.nextDouble(App.WINDOW.getSize().getY()));

            //Random Velocity
            float k = minV >= maxV ? minV : random.nextFloat(minV, maxV);
            Vector v = new Vector(k, k);

            Color c = new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255), 100);

            RegularPolygon polygon = new RegularPolygon(this, null, c, r, random.nextInt(3, 9), new Angle(random.nextInt(0, 360), true));
            PhysicsObjectDraggable obj = new PhysicsObjectDraggable(this, pos, polygon);
            obj.setVelocity(v);
            obj.setMass(m);
            obj.setDeltaT(deltaT);
            addElement(obj, false);
        }
    }

    private void generateObjectsRadially(int count, int minR, int maxR, int minM, int maxM, float density, float minV, float maxV, float deltaT) {
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            long m = minM >= maxM ? minM : random.nextInt(minM, maxM);
            double r = Math.pow(3 * m / (4 * Math.PI * density), 1f / 3); //3D

            //Random Circular Positioning
            double rad = minR >= maxR ? minR : random.nextDouble(minR, maxR);
            double randRad = random.nextDouble(0, Math.TAU);
            Vector center = App.WINDOW.getSize().getMultiplied(.5);
            Vector pos = new Vector(Math.cos(randRad), Math.sin(randRad)).getMultiplied(rad).getAdded(center);

            //Random Tangential Velocity
            float k = minV >= maxV ? minV : random.nextFloat(minV, maxV);
            Vector centerDistVec = pos.getSubtracted(center);
            Vector v = centerDistVec.getCross(Vector.K()).getUnitVector().getMultiplied(k * centerDistVec.getMagnitude());

            Color c = new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255), 100);

            RegularPolygon polygon = new RegularPolygon(this, null, c, r, random.nextInt(3, 9), new Angle(random.nextInt(0, 360), true));
            PhysicsObjectDraggable obj = new PhysicsObjectDraggable(this, pos, polygon);
            obj.setVelocity(v);
            obj.setMass(m);
            obj.setDeltaT(deltaT);
            addElement(obj, false);
        }
    }

    public ShootManager getShootManager() {
        return ((ShootManager) getSceneManagers().get(1));
    }

    private class GravityDebugManager extends DebugManager {

        public GravityDebugManager(Scene scene) {
            super(scene);
        }

        @Override
        public void draw(Vector mousePos) {
            super.draw(mousePos);
            Vector pos = new Vector(2, 72);
            pos.add(new Vector(0, 10));

            this.fontRenderer.drawDynamicString("Draw Count: " + drawableElements.size(), pos, Color.WHITE);
            pos.add(new Vector(0, 10));

            this.fontRenderer.drawDynamicString("Tick Count: " + tickables.size(), pos, Color.WHITE);
            pos.add(new Vector(0, 10));

            this.fontRenderer.drawDynamicString("Hove Count: " + hoverables.size(), pos, Color.WHITE);
            pos.add(new Vector(0, 10));

            this.fontRenderer.drawDynamicString("Inte Count: " + interactables.size(), pos, Color.WHITE);
            pos.add(new Vector(0, 10));

            this.fontRenderer.drawDynamicString("Anim Count: " + animatables.size(), pos, Color.WHITE);
            pos.add(new Vector(0, 10));

            PhysicsObject obj = !getMouseHoveredHandler().getHoverables().isEmpty() ? ((PhysicsObject) getMouseHoveredHandler().getTop()) : null;
            if (obj == null) return;

            this.fontRenderer.drawDynamicString("Hov M: " + obj.getMass(), pos, Color.WHITE);
            pos.add(new Vector(0, 10));

            this.fontRenderer.drawDynamicString("Hov V: " + obj.getVelocity().toString2D(), pos, Color.WHITE);
            pos.add(new Vector(0, 10));
        }
    }
}
