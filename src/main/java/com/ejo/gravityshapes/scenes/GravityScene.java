package com.ejo.gravityshapes.scenes;

import com.ejo.glowlib.math.Angle;
import com.ejo.glowlib.math.VectorMod;
import com.ejo.glowlib.misc.DoOnce;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.ElementUI;
import com.ejo.glowui.scene.elements.shape.LineUI;
import com.ejo.glowui.scene.elements.shape.RectangleUI;
import com.ejo.glowui.scene.elements.shape.RegularPolygonUI;
import com.ejo.glowui.scene.elements.widget.ButtonUI;
import com.ejo.glowui.util.input.Key;
import com.ejo.glowui.util.input.Mouse;
import com.ejo.glowui.util.render.Fonts;
import com.ejo.glowui.util.render.QuickDraw;
import com.ejo.gravityshapes.Grid;
import com.ejo.gravityshapes.Util;
import com.ejo.gravityshapes.objects.PhysicsPolygon;
import com.ejo.glowlib.math.Vector;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.uiphysics.elements.PhysicsObjectUI;
import com.ejo.uiphysics.util.GravityUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class GravityScene extends Scene {

    private final ButtonUI buttonX = new ButtonUI(Vector.NULL,new Vector(15,15),new ColorE(200,0,0,255), ButtonUI.MouseButton.LEFT,() -> getWindow().setScene(new TitleScene()));

    private final boolean doWallBounce;
    private final String collisionType;
    private final boolean drawFieldLines;

    private final double baseSize;

    private boolean shooting;
    private boolean shouldRenderShooting;

    private Vector shootPos;
    private Vector shootVelocity;
    private double shootSize;
    private double shootSpin;
    private int shootVertices;
    private final DoOnce shooter = new DoOnce();
    private final DoOnce shooterInitializer = new DoOnce();

    public GravityScene(int objectCount, double sizeMin, double sizeMax, boolean doWallBounce, String collisionType) {
        super("Orbit Scene");
        DoOnce.DEFAULT1.reset();

        this.doWallBounce = doWallBounce;
        this.collisionType = collisionType;
        this.drawFieldLines = false;

        int verticesMin = 3;
        int verticesMax = 8;

        if (collisionType.equals("PUSH")) {
            sizeMin = sizeMax;
            verticesMin = verticesMax = 30;
        }

        this.baseSize = sizeMin;

        this.shooting = false;
        this.shouldRenderShooting = false;
        this.shootPos = Vector.NULL;
        this.shootVelocity = Vector.NULL;
        this.shootSize = sizeMin;
        this.shootSpin = 0;
        this.shootVertices = 0;
        this.shooter.run(() -> {});

        addStars();
        addPhysicsObjects(objectCount,sizeMin,sizeMax, verticesMin,verticesMax);
        addElements(buttonX);
    }

    @Override
    public void draw() {
        //Set exit button to top right corner
        buttonX.setPos(new Vector(getSize().getX(),0).getAdded(-buttonX.getSize().getX(),0));

        updateStarPositionsOnResize();

        if (this.drawFieldLines || Key.KEY_F.isKeyDown()) drawFieldLines(.05, getPhysicsObjects());

        //Draw all screen objects
        super.draw();

        //Draw Object Count
        if (getWindow().isDebug()) {
            try {
                QuickDraw.drawText("Object Count: " + getPhysicsObjects().size(), Fonts.getDefaultFont(14), new Vector(2, 87), ColorE.WHITE);
            } catch (Exception ignored) {
            }
        }

        //Draw Shooting Object Visual
        if (shooting && shouldRenderShooting) drawShootingObject();

        //Draw X for Exit Button
        QuickDraw.drawText("X",new Font("Arial",Font.PLAIN,14), buttonX.getPos().getAdded(3,-2),ColorE.WHITE);
    }

    @Override
    public void tick() {
        initObjectPositions();

        Grid collisionGrid = null;
        if (collisionType.equals("PUSH")) collisionGrid = new Grid(getSize(),getPhysicsObjects(),(int)Math.round(baseSize*2));

        for (PhysicsPolygon obj : getPhysicsObjects()) {
            if (obj.isPhysicsDisabled()) continue;

            //Debug Vectors
            obj.setDebugVectorForceScale((double) 1 /100); //TODO: Make the force vector scale multiple of max mass
            obj.setDebugVectorCap(100);

            //Updates collision data
            updateCollision(obj,collisionGrid);

            //Do Wall Bounce
            if (doWallBounce) obj.doWallBounce(this,.1);

            //Set Gravity Force
            obj.addForce(GravityUtil.calculateGravityForce(1,obj, getPhysicsObjects(), 10));
        }

        //Run Shoot New Object Computations
        updateShootObject();

        //Calculate the forces/accelerations. Reset's the added forces after acceleration calculation
        super.tick();
    }

    @Override
    public void onKeyPress(int key, int scancode, int action, int mods) {
        super.onKeyPress(key, scancode, action, mods);
        if (key == Key.KEY_ESC.getId() && action == Key.ACTION_PRESS) {
            buttonX.getAction().run();
        }
        if (shooting) {
            if (key == Key.KEY_UP.getId()) shootSize += 1;
            if (key == Key.KEY_DOWN.getId()) shootSize -= 1;
        }
    }

    @Override
    public void onMouseClick(int button, int action, int mods, Vector mousePos) {
        super.onMouseClick(button, action, mods, mousePos);
        if (button == Mouse.BUTTON_LEFT.getId()) {
            if (action == Mouse.ACTION_CLICK) {
                boolean isMouseFree = true;
                for (PhysicsObjectUI obj : getPhysicsObjects()) {
                    if (obj.isMouseOver() && !obj.isPhysicsDisabled()) {
                        isMouseFree = false;
                        break;
                    }
                }
                if (isMouseFree) {
                    shootPos = mousePos;
                    shooting = true;
                }
            }
            if (action == Mouse.ACTION_RELEASE) shooting = false;
        }
    }

    @Override
    public void onMouseScroll(int scroll, Vector mousePos) {
        super.onMouseScroll(scroll, mousePos);
        if (shooting) {
            shootSize += scroll;
            if (shootSize < .1) shootSize = .1;
        }
    }

    private void updateCollision(PhysicsObjectUI obj, Grid collisionGrid) {
        switch (collisionType) {
            case "NONE" -> {
                for (PhysicsPolygon otherObject : getPhysicsObjects()) {
                    if (obj.equals(otherObject) || otherObject.isPhysicsDisabled()) continue;
                    if (Util.areObjectsInCollisionRange(obj, otherObject) && obj.isColliding(otherObject))
                        ((PhysicsPolygon)obj).applyTorqueFromCollision(otherObject,50);
                }
            }
            case "MERGE" -> {
                for (PhysicsPolygon otherObject : getPhysicsObjects()) {
                    if (obj.equals(otherObject) || otherObject.isPhysicsDisabled()) continue;
                    if (Util.areObjectsInCollisionRange(obj, otherObject) && obj.isColliding(otherObject)) {
                        ((PhysicsPolygon)obj).doMergeCollision(otherObject);
                        queueRemoveElements(otherObject);
                    }
                }
            }
            case "PUSH" -> {
                for (PhysicsObjectUI otherObject : collisionGrid.getSurroundingObjects(obj)) {
                    if (obj.equals(otherObject) || otherObject.isPhysicsDisabled()) continue;
                    double objectDistance = obj.getCenter().getSubtracted(otherObject.getCenter()).getMagnitude();
                    boolean isColliding = objectDistance <= ((PhysicsPolygon)obj).getPolygon().getRadius() + ((PhysicsPolygon)otherObject).getPolygon().getRadius();
                    if (isColliding) ((PhysicsPolygon)obj).doPushCollision((PhysicsPolygon) otherObject,.6,.2); //.6m .2f
                }
            }
        }
    }

    private void drawFieldLines(double lineDensity, ArrayList<PhysicsPolygon> physicsPolygons) {
        int inverseDensity = (int) (1/lineDensity);
        int windowWidth = (int)getWindow().getScaledSize().getX();
        int windowHeight = (int)getWindow().getScaledSize().getY();

        for (int x = 0; x < windowWidth / inverseDensity + 1; x++) {
            for (int y = 0; y < windowHeight / inverseDensity + 1; y++) {
                VectorMod gravityForce = Vector.NULL.getMod();
                for (PhysicsPolygon otherObject : physicsPolygons) {
                    if (!otherObject.isPhysicsDisabled()) {
                        Vector gravityFromOtherObject = GravityUtil.calculateGravitationalField(1,otherObject,new Vector(x,y).getMultiplied(inverseDensity),0);
                        if (!(String.valueOf(gravityFromOtherObject.getMagnitude())).equals("NaN")) gravityForce.add(gravityFromOtherObject);
                    }
                }
                LineUI lineUI = new LineUI(new Vector(x,y).getMultiplied(inverseDensity),gravityForce.getTheta(),Math.min(Math.max(gravityForce.getMagnitude(),.1),1)*10,ColorE.WHITE.alpha(100), LineUI.Type.PLAIN,.5);
                lineUI.draw();
            }
        }
    }

    private void drawShootingObject() {
        RegularPolygonUI polygonUI = new RegularPolygonUI(shootPos, QuickDraw.GLOW_BLUE,true,shootSize,shootVertices,new Angle(shootSpin,true));
        GL11.glLineWidth(3);
        polygonUI.draw();
        LineUI line = new LineUI(ColorE.WHITE, LineUI.Type.DOTTED,2,shootPos,shootPos.getAdded(shootPos.getAdded(getWindow().getScaledMousePos().getMultiplied(-1))));
        line.draw();
        shootSpin += 1;
        if (shootSpin > 360) shootSpin = 0;
    }

    private void updateShootObject() {
        if (shooting) {
            shooter.reset();
            shooterInitializer.run(() -> {
                Random random = new Random();
                shootVertices = collisionType.equals("PUSH") ? 30 : random.nextInt(3, 8);
                shouldRenderShooting = true;
            });
        } else {
            shooterInitializer.reset();
            shooter.run(() -> {
                //Shoot the object
                shouldRenderShooting = false;
                Random random = new Random();
                shootVelocity = shootPos.getAdded(getWindow().getScaledMousePos().getMultiplied(-1)).getMultiplied(.75);
                ColorE randomColor = new ColorE(random.nextInt(25,255),random.nextInt(25,255),random.nextInt(25,255),255);
                PhysicsPolygon poly = new PhysicsPolygon(new RegularPolygonUI(shootPos,randomColor,false,shootSize,shootVertices,new Angle(shootSpin,true)),(double) 4 /3*Math.PI*Math.pow(shootSize,3),shootVelocity,Vector.NULL);
                queueAddElements(poly);
            });
        }
    }


    private void initObjectPositions() {
        DoOnce.DEFAULT1.run(this::setRandomObjectPositions);
    }

    private void setRandomObjectPositions() {
        Random random = new Random();
        for (PhysicsPolygon obj : getPhysicsObjects())
            obj.setPos(new Vector(random.nextDouble(0,getSize().getX()),random.nextDouble(0,getSize().getY())));
        for (ElementUI el : getElements()) {
            if (el instanceof RectangleUI rect && rect.shouldRender() && !rect.shouldTick()) {
                rect.setPos(new Vector(random.nextDouble(0,getSize().getX()),random.nextDouble(0,getWindow().getSize().getY())));
            }
        }
    }

    private void updateStarPositionsOnResize() {
        getWindow().doOnResize.run(() -> {
            Random random = new Random();
            for (ElementUI el : getElements()) {
                if (el instanceof RectangleUI rect && rect.shouldRender() && !rect.shouldTick()) {
                    rect.setPos(new Vector(random.nextDouble(0,getSize().getX()),random.nextDouble(0,getWindow().getSize().getY())));
                }
            }
        });
    }


    private void addPhysicsObjects(int objectCount, double sizeMin, double sizeMax, int verticesMin, int verticesMax) {
        Random random = new Random();
        for (int i = 0; i < objectCount; i++) {
            double trueSize = (sizeMin == sizeMax) ? sizeMax : random.nextDouble(sizeMin, sizeMax);
            int trueVertices = (verticesMin == verticesMax) ? verticesMax : random.nextInt(verticesMin, verticesMax);
            double startVelRange = 10;
            ColorE randomColor = new ColorE(random.nextInt(25,255),random.nextInt(25,255),random.nextInt(25,255),255);
            addElements(new PhysicsPolygon(
                    new RegularPolygonUI(Vector.NULL, randomColor, trueSize, trueVertices,new Angle(random.nextDouble(0,2*Math.PI))), (double) 4 /3*Math.PI*Math.pow(trueSize,3),
                    new Vector(random.nextDouble(-startVelRange,startVelRange),random.nextDouble(-startVelRange,startVelRange)), Vector.NULL));
        }
    }

    private void addStars() {
        for (int i = 0; i < 100; i++) {
            ColorE color = new ColorE(255, 255, 255,255);
            RectangleUI obj = new RectangleUI(Vector.NULL,new Vector(1,1), color);
            obj.setTicking(false);
            if (!this.drawFieldLines) addElements(obj);
        }
    }


    public ArrayList<PhysicsPolygon> getPhysicsObjects() {
        ArrayList<PhysicsPolygon> rectangles = new ArrayList<>();
        for (ElementUI elementUI : getElements()) {
            if (elementUI instanceof PhysicsPolygon polygon) rectangles.add(polygon);
        }
        return rectangles;
    }

}