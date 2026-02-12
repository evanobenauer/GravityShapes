package ejo.gravityshapes.element;

import com.ejo.ui.Scene;
import com.ejo.ui.element.polygon.RegularPolygon;
import com.ejo.util.math.Angle;
import com.ejo.util.math.Vector;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ObjectsPolygon extends RegularPolygon {

    public ObjectsPolygon(Scene scene, Vector pos, Color color, boolean outlined, float outlineWidth, double radius, int vertexCount, Angle rotation) {
        super(scene, pos, color, outlined, outlineWidth, radius, vertexCount, rotation);
    }

    public ObjectsPolygon(Scene scene, Vector pos, Color color, double radius, int vertexCount, Angle rotation) {
        super(scene, pos, color, radius, vertexCount, rotation);
    }

    public ObjectsPolygon(Scene scene, Vector pos, Color color, double radius, int vertexCount) {
        super(scene, pos, color, radius, vertexCount);
    }

    public void drawBlueOutline() {
        Color c = new Color(0, 125, 200);
        GL11.glColor4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
        GL11.glDisable(GL11.GL_LINE_STIPPLE);
        GL11.glLineWidth(.25f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (Vector vert : vertices) GL11.glVertex2f((float) getPos().getX() + (float) vert.getX(), (float) getPos().getY() + (float) vert.getY());
        GL11.glEnd();
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }
}
