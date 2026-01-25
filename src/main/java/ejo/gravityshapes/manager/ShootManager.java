package ejo.gravityshapes.manager;

import com.ejo.ui.Scene;
import com.ejo.ui.element.DrawableElement;
import com.ejo.ui.element.Line;
import com.ejo.ui.element.PhysicsObject;
import com.ejo.ui.element.shape.RegularPolygon;
import com.ejo.ui.manager.SceneManager;
import com.ejo.util.action.DoOnce;
import com.ejo.util.math.Angle;
import com.ejo.util.math.Vector;
import ejo.gravityshapes.element.ObjectsPolygon;
import ejo.gravityshapes.element.PhysicsObjectDraggable;
import ejo.gravityshapes.gravityscene.BounceCollisionScene;
import ejo.gravityshapes.gravityscene.GravityScene;
import ejo.gravityshapes.util.CollisionUtil;
import ejo.gravityshapes.util.PhysicsUtil;
import ejo.gravityshapes.element.SimulatedParticle;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Random;

public class ShootManager extends SceneManager {

    private boolean shooting;
    private boolean rapid;
    private boolean tripleRapid;

    private final int G;
    private final float deltaT;

    private Vector shootPos;
    private Vector shootVelocity;

    private double shootSize;
    private Angle shootSpin;
    private int shootVertices;
    private int shootLineSteps;

    private final DoOnce shooter;
    private final DoOnce shooterInitializer;

    public ShootManager(Scene scene, int shootLineSteps, int sizeMin, int sizeMax, int G, float deltaT) {
        super(scene);

        this.shootLineSteps = shootLineSteps;

        float p = 1;
        int avgSize = (sizeMin + sizeMax) / 2;
        this.shootSize = Math.pow(3 * avgSize / (4 * Math.PI * p), 1f / 3);

        this.G = G;
        this.deltaT = deltaT;

        this.shooting = false;
        this.rapid = false;
        this.tripleRapid = false;

        this.shootPos = Vector.NULL();
        this.shootVelocity = Vector.NULL();

        this.shootSpin = new Angle(0, true);
        this.shootVertices = 0;

        this.shooter = new DoOnce();
        this.shooterInitializer = new DoOnce();

        this.shooter.run(() -> {
        });
    }


    @Override
    public void draw(Vector mousePos) {
        //Draw Shooting Object Visual
        if (shooting) {
            updateDrawnFeatures(mousePos);
            drawShootingObject(mousePos);
        }
    }

    @Override
    public void tick(Vector mousePos) {
        if (!rapid) {
            updateShootObject(mousePos);
        } else {
            updateShootObjectRapid(mousePos);
        }
    }

    @Override
    public void onKeyPress(int key, int scancode, int action, int mods) {
        if (action == GLFW.GLFW_RELEASE) {
            if (key == GLFW.GLFW_KEY_LEFT_SHIFT) rapid = false;
            if (key == GLFW.GLFW_KEY_LEFT_CONTROL) tripleRapid = false;
        } else {
            if (shooting) {
                if (key == GLFW.GLFW_KEY_LEFT_SHIFT) rapid = true;
                if (key == GLFW.GLFW_KEY_LEFT_CONTROL) tripleRapid = true;

                if (scene instanceof BounceCollisionScene) return;
                if (key == GLFW.GLFW_KEY_UP) shootSize += 1;
                if (key == GLFW.GLFW_KEY_DOWN) shootSize -= 1;
                if (shootSize < .5) shootSize = .5;
            }
        }
    }

    @Override
    public void onMouseClick(int button, int action, int mods, Vector mousePos) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        switch (action) {
            case GLFW.GLFW_PRESS -> shooting = scene.getMouseHoveredHandler().getHoverables().isEmpty();
            case GLFW.GLFW_RELEASE -> shooting = false;
        }
    }


    @Override
    public void onMouseScroll(double scroll, Vector mousePos) {
        if (shooting) {
            shootSize += scroll;
            if (shootSize < .5) shootSize = .5;
        }
    }

    private void updateDrawnFeatures(Vector mousePos) {
        //Update Shoot Velocity & Spin
        this.shootSpin = shootSpin.getAdded(1, true);
        this.shootVelocity = shootPos.getAdded(mousePos.getMultiplied(-1));
    }

    private void drawShootingObject(Vector mousePos) {
        //Draw Blue Outlined Object
        RegularPolygon polygon = new RegularPolygon(scene, shootPos, new Color(0, 125, 200), true, 3, shootSize, shootVertices, shootSpin);
        polygon.draw();

        //Generate Shooter Line Vertices
        SimulatedParticle particle = new SimulatedParticle(new PhysicsObject(scene, shootPos, polygon), shootLineSteps);
        particle.getPhysicsObject().setVelocity(shootVelocity);
        particle.getPhysicsObject().setDeltaT(deltaT);
        Vector[] vectors = particle.getFuturePositions((obj, pos, i) -> {
            Vector force = Vector.NULL();
            for (DrawableElement element : scene.getDrawableElements()) {
                if (!(element instanceof PhysicsObject p)) continue;
                if (CollisionUtil.isColliding((RegularPolygon) particle.getAtPos(pos).getElement(), (RegularPolygon) p.getElement())) {
                    if (!(scene instanceof BounceCollisionScene))particle.setSteps(i); //Only call this if on combined mode
                    return force;
                }
                force.add(PhysicsUtil.getGravityField(G, p, pos).getMultiplied(obj.getMass()));
            }
            return force;
        }, (obj1, pos, vel, force, i) -> {
            if (((GravityScene) scene).wallBounce) CollisionUtil.doWallBounce(scene, pos, vel, .9);
            /*if (scene instanceof BounceCollisionScene s) {
                for (DrawableElement element : scene.getDrawableElements()) {
                    if (!(element instanceof PhysicsObject obj2)) continue;
                    s.doPushCollision(obj1,obj2,obj1.getPos(),obj1.getVelocity(),obj1.getNetForce(),obj2.getPos(),obj2.getVelocity(),obj2.getNetForce(),.6,false,0); //This needs to be modified to accept the futurePos and futureVel
                }
            }*/

        });

        //Draw Shooter Line
        Line line = new Line(scene, 2, Line.Type.DASHED, Color.WHITE, vectors);
        line.setFaded(true);
        line.draw();
    }

    private void updateShootObject(Vector mousePos) {
        if (shooting) {
            shooter.reset();
            shooterInitializer.run(() -> {
                shootPos = mousePos;
                Random random = new Random();
                shootVertices = random.nextInt(3, 9);
            });
        } else {
            shooterInitializer.reset();
            shooter.run(this::addShotObject);
        }
    }

    private void updateShootObjectRapid(Vector mousePos) {
        if (shooting) {
            shooterInitializer.run(() -> shootPos = mousePos);
            Random random = new Random();
            this.shootVertices = random.nextInt(3, 9);
            if (tripleRapid) {
                addTripleShotObject();
            } else {
                addShotObject();
            }
        } else {
            shooterInitializer.reset();
        }
    }

    private void addShotObject() {
        //Shoot the object
        Random random = new Random();
        Color randomColor = new Color(random.nextInt(25, 255), random.nextInt(25, 255), random.nextInt(25, 255), 100);

        ObjectsPolygon poly = new ObjectsPolygon(scene, Vector.NULL(), randomColor, shootSize, shootVertices, shootSpin);
        PhysicsObjectDraggable obj = new PhysicsObjectDraggable(scene, shootPos, poly);

        obj.setMass((double) 4 / 3 * Math.PI * Math.pow(shootSize, 3));
        obj.setVelocity(shootVelocity);
        obj.setDeltaT(deltaT);
        obj.setTheta(shootSpin.getRadians());

        scene.addElement(obj, true);
    }

    private void addTripleShotObject() {
        //Shoot the object
        Random random = new Random();
        Color randomColor = new Color(random.nextInt(25, 255), random.nextInt(25, 255), random.nextInt(25, 255), 100);

        boolean neg = false;
        for (int i = 0; i < 3; i++) {
            ObjectsPolygon poly = new ObjectsPolygon(scene, Vector.NULL(), randomColor, shootSize, shootVertices, shootSpin);
            Vector tempShootPos = shootPos;
            Vector vel = shootVelocity.getUnitVector();
            double space = shootSize * 4;
            if (i % 2 == 0) {
                tempShootPos = vel.getCross(Vector.K()).getMultiplied(space * (neg ? 1 : -1)).getAdded(shootPos);
                neg = !neg;
            }
            PhysicsObjectDraggable obj = new PhysicsObjectDraggable(scene, tempShootPos, poly);

            obj.setMass((double) 4 / 3 * Math.PI * Math.pow(shootSize, 3));
            obj.setVelocity(shootVelocity);
            obj.setDeltaT(deltaT);
            obj.setTheta(shootSpin.getRadians());

            scene.addElement(obj, true);
        }
    }

    public void setShootLineSteps(int shootLineSteps) {
        this.shootLineSteps = shootLineSteps;
    }

}
