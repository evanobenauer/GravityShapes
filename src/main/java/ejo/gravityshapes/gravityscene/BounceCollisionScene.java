package ejo.gravityshapes.gravityscene;

import com.ejo.ui.element.Line;
import com.ejo.ui.element.PhysicsObject;
import com.ejo.ui.element.base.Tickable;
import com.ejo.ui.element.shape.ConvexPolygon;
import com.ejo.ui.element.shape.RegularPolygon;
import com.ejo.util.math.Vector;
import com.ejo.util.misc.ColorUtil;
import ejo.gravityshapes.util.CollisionUtil;

import java.awt.*;
import java.util.ArrayList;

//TODO: This was taken from GravityShapesLegacy. The grid is disgusting. Make it less disgusting
//This is very experimental.. Very terrible... Try and make it better...
public class BounceCollisionScene extends GravityScene {

    private final int baseSize;

    private final boolean drawGrid;
    private ObjectGrid collisionGrid;

    public BounceCollisionScene(int G, boolean wallBounce, boolean paths, boolean fieldLines, int objectCount, String spawnMode, int radius) {
        super("Collisions", G, wallBounce, paths, fieldLines, objectCount, spawnMode, (int) (4d/3 * Math.PI * Math.pow(radius,3)), (int) (4d/3 * Math.PI * Math.pow(radius,3)));
        this.baseSize = radius;
        this.drawGrid = false;
    }

    @Override
    public void draw() {
        super.draw();
        if (drawGrid && collisionGrid != null) collisionGrid.drawDebugGridLines();
    }

    @Override
    public void tick() {
        super.tick();

        this.collisionGrid = new ObjectGrid(getWindow().getSize(), tickables, baseSize * 2);
        for (Tickable e1 : tickables) {
            if (!(e1 instanceof PhysicsObject obj1)) continue;
            if (tickables.isRemovalQueued(obj1)) continue;

            //Collisions
            for (Tickable e2 : collisionGrid.getSurroundingObjects(obj1)) {
                if (e1.equals(e2) || !(e2 instanceof PhysicsObject obj2)) continue;
                if (tickables.isRemovalQueued(obj2)) continue;
                doPushCollision(obj1, obj2,.6,false,0f);
            }
        }
    }

    @Deprecated //Old but still cool
    private void doCollisionWeird(PhysicsObject obj1, PhysicsObject obj2) {
        Vector r = obj2.getPos().getSubtracted(obj1.getPos());
        double objectDistance = r.getMagnitude();
        boolean colliding = objectDistance <= ((RegularPolygon) obj1.getElement()).getRadius() + ((RegularPolygon) obj2.getElement()).getRadius();
        if (colliding) {
            Vector avgV = obj1.getVelocity().getAdded(obj2.getVelocity()).getMultiplied(.5);
            obj2.setVelocity(avgV);
            obj1.setVelocity(avgV);
        }
    }

    public void doPushCollision(PhysicsObject obj1, PhysicsObject obj2, double mRestitution, boolean friction, double fCoefficient) {
        if (!CollisionUtil.isColliding((RegularPolygon) obj1.getElement(), (RegularPolygon) obj2.getElement())) return;
        //this is reference frame. Obj2 is observed object
        Vector dirVec = ((ConvexPolygon)obj2.getElement()).getCenter().getSubtracted(((ConvexPolygon)obj1.getElement()).getCenter());

        Vector uParallel = dirVec.getUnitVector(); //From this to Obj2
        Vector uPerpendicular = uParallel.getCross(Vector.K()); //From this to Obj2

        //Calculate Relative Velocity
        Vector relativeVelocity = obj2.getVelocity().getSubtracted(obj1.getVelocity());
        Vector relativeVelocityPerpendicular = uPerpendicular.getMultiplied(relativeVelocity.getDot(uPerpendicular));

        //Calculate Relative Force
        Vector relativeForce = obj2.getNetForce().getSubtracted(obj1.getNetForce());
        Vector relativeForceParallel = uParallel.getMultiplied(relativeForce.getDot(uParallel));

        //Calculate Parallel Velocities
        Vector thisVelParallel = uParallel.getMultiplied(obj1.getVelocity().getDot(uParallel));
        Vector obj2VelParallel = uParallel.getMultiplied(obj2.getVelocity().getDot(uParallel));

        //Calculate Parallel Momentum
        Vector thisMomentumParallel = thisVelParallel.getMultiplied(obj1.getMass());
        Vector obj2MomentumParallel = obj2VelParallel.getMultiplied(obj2.getMass());
        Vector totalMomentumParallel = thisMomentumParallel.getAdded(obj2MomentumParallel);

        //Calculate Post-Collision Obj2 Velocity
        Vector nObj2VelParallel = totalMomentumParallel.getSubtracted(obj2VelParallel.getSubtracted(thisVelParallel).getMultiplied(obj1.getMass() * mRestitution)).getMultiplied(1 / (obj2.getMass() + obj1.getMass()));

        //Calculate Object Parallel Changes in Velocity Post-Collision
        Vector nObj2VelocityDiffParallel = nObj2VelParallel.getSubtracted(obj2VelParallel);
        Vector nThisVelocityDiffParallel = nObj2VelocityDiffParallel.getMultiplied(-obj2.getMass() / obj1.getMass());


        //Set Collision boundary pushing
        double overlap = ((RegularPolygon)obj1.getElement()).getRadius() + ((RegularPolygon)obj2.getElement()).getRadius() - dirVec.getMagnitude();
        obj1.setPos(obj1.getPos().getAdded(uParallel.getMultiplied(-overlap / 2)));
        obj2.setPos(obj2.getPos().getAdded(uParallel.getMultiplied(overlap / 2)));

        if (friction) {
            //Calculate perpendicular friction between objects
            Vector nObj2FrictionForce = relativeVelocityPerpendicular.getMagnitude() > 1 ? relativeVelocityPerpendicular.getUnitVector().getMultiplied(-1).getMultiplied(relativeForceParallel.getMagnitude() * fCoefficient) : Vector.NULL();
            Vector nThisFrictionForce = relativeVelocityPerpendicular.getMagnitude() > 1 ? relativeVelocityPerpendicular.getUnitVector().getMultiplied(relativeForceParallel.getMagnitude() * fCoefficient) : Vector.NULL();

            //Apply Friction Force
            obj1.addForce(nThisFrictionForce);
            obj2.addForce(nObj2FrictionForce);
        }

        //Set both object velocities to their new variants.
        obj1.setVelocity(obj1.getVelocity().getAdded(nThisVelocityDiffParallel));
        obj2.setVelocity(obj2.getVelocity().getAdded(nObj2VelocityDiffParallel));
    }

    //This grid is NOT UPDATABLE. It must be recreated each loop.
    // Make sure it is a local variable object
    private class ObjectGrid {

        private final ArrayList<PhysicsObject>[][] array2D; //A 2D array of arraylists

        private final int cellSize;
        private final Vector gridSize;
        private final Vector screenSize;

        //Currently, the grid encompasses the screen only
        // If an object is off-screen, it is added to the nearest grid pos
        // In the future, potentially resize the grid depending on the farthest object? that may make it slow though
        public <T extends Tickable> ObjectGrid(Vector screenSize, ArrayList<T> objects, int cellSize) {
            int gridCountX = (int) Math.max(Math.round(screenSize.getX() / cellSize),0);
            int gridCountY = (int) Math.max(Math.round(screenSize.getY() / cellSize),0);
            this.gridSize = new Vector(gridCountX, gridCountY);
            this.screenSize = screenSize;
            this.cellSize = cellSize;
            this.array2D = new ArrayList[gridSize.getYi()][gridSize.getXi()];

            initializeCellLists();

            //Assign each object to its grid position
            for (Tickable element : objects) {
                if (!(element instanceof PhysicsObject obj)) continue;
                Vector gridIndex = new Vector(gridSize.getX() * (obj.getPos().getX() / screenSize.getX()), gridSize.getY() * (obj.getPos().getY() / screenSize.getY()));
                this.array2D[Math.clamp(gridIndex.getYi(), 0, gridSize.getYi() - 1)][Math.clamp(gridIndex.getXi(), 0, gridSize.getXi() - 1)].add(obj);
            }
        }

        private void initializeCellLists() {
            for (int i = 0; i < gridSize.getX(); i++)
                for (int j = 0; j < gridSize.getY(); j++)
                    array2D[j][i] = new ArrayList<>();
        }

        public ArrayList<PhysicsObject> getSurroundingObjects(PhysicsObject object) {
            //Finds the grid that the object corresponds to
            Vector gridIndex = new Vector(gridSize.getX() * (object.getPos().getX() / screenSize.getX()), gridSize.getY() * (object.getPos().getY() / screenSize.getY()));

            Vector I = Vector.I();
            Vector J = Vector.J();

            Vector[] directions = { //Surrounding directions
                    I, //x+
                    J.getAdded(I),//++
                    J,//y+
                    J.getAdded(I.getMultiplied(-1)),//y+x-
                    I.getMultiplied(-1),//x-
                    J.getAdded(I).getMultiplied(-1),//--
                    J.getMultiplied(-1),//y-
                    J.getAdded(I.getMultiplied(-1)).getMultiplied(-1),//x+y-
            };

            //Add all objects from surrounding cells and object's cell
            ArrayList<PhysicsObject> surroundingObjects = new ArrayList<>();

            //Add from object's cell
            surroundingObjects.addAll(array2D[Math.clamp(gridIndex.getYi(), 0, gridSize.getYi() - 1)][Math.clamp(gridIndex.getXi(), 0, gridSize.getXi() - 1)]);

            //Add from surrounding cells
            for (Vector dir : directions) {
                int xi = gridIndex.getXi() + dir.getXi();
                int yi = gridIndex.getYi() + dir.getYi();
                if (xi < 0 || yi < 0 || xi > gridSize.getX() - 1 || yi > gridSize.getY() - 1) continue;
                surroundingObjects.addAll(array2D[yi][xi]);
            }
            return surroundingObjects;
        }

        public void drawDebugGridLines() {
            //Draw Debug Grid Lines
            int countX = (int) Math.round(screenSize.getX() / cellSize);
            for (int i = 0; i <= countX; i++) {
                double x = screenSize.getX() / countX * (i);
                Line line = new Line(getWindow().getScene(), new Vector(x, 0), new Vector(x, screenSize.getY()), 1, Line.Type.DOTTED, ColorUtil.getWithAlpha(Color.WHITE, 50));
                line.draw();
            }

            int countY = (int) Math.round(screenSize.getY() / cellSize);
            for (int i = 0; i <= countY; i++) {
                double y = screenSize.getY() / countY * (i);
                Line line = new Line(getWindow().getScene(), new Vector(0, y), new Vector(screenSize.getX(), y), 1, Line.Type.DOTTED, ColorUtil.getWithAlpha(Color.WHITE, 50));
                line.draw();
            }
        }
    }

    @Deprecated
    public void doPushCollisionStupid(PhysicsObject obj1, PhysicsObject obj2, double mRestitution, boolean friction, double fCoefficient) {
        doPushCollisionStupid(obj1,obj2,obj1.getPos(),obj1.getVelocity(),obj1.getNetForce(),obj2.getPos(),obj2.getVelocity(),obj2.getNetForce(),mRestitution,friction,fCoefficient);
    }

    @Deprecated
    public void doPushCollisionStupid(PhysicsObject obj1, PhysicsObject obj2, Vector pos1, Vector vel1, Vector force1, Vector pos2, Vector vel2, Vector force2, double mRestitution, boolean friction, double fCoefficient) {
        if (!CollisionUtil.isColliding((RegularPolygon) obj1.getElement(), (RegularPolygon) obj2.getElement())) return;
        //this is reference frame. Obj2 is observed object
        Vector dirVec = pos2.getSubtracted(pos1);

        Vector uParallel = dirVec.getUnitVector(); //From this to Obj2
        Vector uPerpendicular = uParallel.getCross(Vector.K()); //From this to Obj2

        //Calculate Relative Velocity
        Vector relativeVelocity = vel2.getSubtracted(vel1);
        Vector relativeVelocityPerpendicular = uPerpendicular.getMultiplied(relativeVelocity.getDot(uPerpendicular));

        //Calculate Relative Force
        Vector relativeForce = force2.getSubtracted(force1);
        Vector relativeForceParallel = uParallel.getMultiplied(relativeForce.getDot(uParallel));

        //Calculate Parallel Velocities
        Vector thisVelParallel = uParallel.getMultiplied(vel1.getDot(uParallel));
        Vector obj2VelParallel = uParallel.getMultiplied(vel2.getDot(uParallel));

        //Calculate Parallel Momentum
        Vector thisMomentumParallel = thisVelParallel.getMultiplied(obj1.getMass());
        Vector obj2MomentumParallel = obj2VelParallel.getMultiplied(obj2.getMass());
        Vector totalMomentumParallel = thisMomentumParallel.getAdded(obj2MomentumParallel);

        //Calculate Post-Collision Obj2 Velocity
        Vector nObj2VelParallel = totalMomentumParallel.getSubtracted(obj2VelParallel.getSubtracted(thisVelParallel).getMultiplied(obj1.getMass() * mRestitution)).getMultiplied(1 / (obj2.getMass() + obj1.getMass()));

        //Calculate Object Parallel Changes in Velocity Post-Collision
        Vector nObj2VelocityDiffParallel = nObj2VelParallel.getSubtracted(obj2VelParallel);
        Vector nObj1VelocityDiffParallel = nObj2VelocityDiffParallel.getMultiplied(-obj2.getMass() / obj1.getMass());


        //Set Collision boundary pushing
        double overlap = ((RegularPolygon)obj1.getElement()).getRadius() + ((RegularPolygon)obj2.getElement()).getRadius() - dirVec.getMagnitude();
        obj1.setPos(obj1.getPos().getAdded(uParallel.getMultiplied(-overlap / 2)));
        obj2.setPos(obj2.getPos().getAdded(uParallel.getMultiplied(overlap / 2)));

        if (friction) {
            //Calculate perpendicular friction between objects
            Vector nObj2FrictionForce = relativeVelocityPerpendicular.getMagnitude() > 1 ? relativeVelocityPerpendicular.getUnitVector().getMultiplied(-1).getMultiplied(relativeForceParallel.getMagnitude() * fCoefficient) : Vector.NULL();
            Vector nObj1FrictionForce = relativeVelocityPerpendicular.getMagnitude() > 1 ? relativeVelocityPerpendicular.getUnitVector().getMultiplied(relativeForceParallel.getMagnitude() * fCoefficient) : Vector.NULL();

            //Apply Friction Force
            obj1.getNetForce().add(nObj1FrictionForce);
            obj2.getNetForce().add(nObj2FrictionForce);
        }

        //Set both object velocities to their new variants.
        Vector nVel1 = obj1.getVelocity().getAdded(nObj1VelocityDiffParallel);
        Vector nVel2 = obj2.getVelocity().getAdded(nObj2VelocityDiffParallel);
        vel1.setX(nVel1.getX());
        vel1.setY(nVel1.getY());
        vel2.setX(nVel2.getX());
        vel2.setY(nVel2.getY());
    }



}
