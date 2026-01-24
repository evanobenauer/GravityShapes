package ejo.gravityshapes.element;

import com.ejo.ui.Scene;
import com.ejo.ui.element.base.Descriptable;
import com.ejo.ui.element.widget.Button;
import com.ejo.ui.render.GLUtil;
import com.ejo.util.math.Vector;
import com.ejo.util.misc.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class TitleButton extends Button implements Descriptable {

    private static final int BORDER = 10;

    private final String description;
    private final BufferedImage image;

    public TitleButton(Scene scene, Vector pos, Vector size, Color color, String title, String description, URL imageURL, Runnable action) {
        super(scene, pos, size, color, title, action);
        this.description = description;
        if (imageURL == null) this.image = null;
        else this.image = ImageUtil.blackToTransparent(ImageUtil.getBufferedImage(size.getXi() - BORDER * 2,size.getYi() - BORDER * 2,imageURL));
    }

    @Override
    protected void drawWidget(Vector mousePos) {
        super.drawWidget(mousePos);
        if (image != null) GLUtil.drawTexture(image,getPos().getAdded(BORDER, BORDER));
    }

    @Override
    public String getDescription() {
        return description;
    }
}