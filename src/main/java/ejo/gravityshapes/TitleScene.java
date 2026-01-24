package ejo.gravityshapes;

import com.ejo.ui.Scene;
import com.ejo.ui.element.Text;
import com.ejo.ui.element.shape.Rectangle;
import com.ejo.ui.element.widget.Button;
import com.ejo.ui.element.widget.settingwidget.Cycle;
import com.ejo.ui.element.widget.settingwidget.Slider;
import com.ejo.ui.element.widget.settingwidget.Toggle;
import com.ejo.ui.manager.TooltipManager;
import com.ejo.util.math.Vector;
import com.ejo.util.setting.Setting;
import com.ejo.util.setting.SettingManager;
import ejo.gravityshapes.element.TitleButton;
import ejo.gravityshapes.gravityscene.BounceCollisionScene;
import ejo.gravityshapes.gravityscene.MergeCollisionScene;
import ejo.gravityshapes.manager.StarManager;

import java.awt.*;

public class TitleScene extends Scene {

    private final StarManager starManager;

    private final Text titleText;
    private double titleAnimationStep = 0;

    public Slider<Integer> sliderObjectCount;
    public Toggle toggleWallBounce;
    public Toggle toggleApplyGravity;
    public Toggle togglePaths;
    public Toggle toggleFieldLines;
    public Cycle<String> cycleSpawnPattern;

    public Button startCollision;
    public Button startMerge;
    public Button startThird; //Idk what third mode to make yet lol

    public TitleScene() {
        super("Title Scene");
        Color widgetColor = new Color(0,125,255);

        this.titleText = new Text(this, Vector.NULL(),"Gravity Shapes",new Font("Arial Black",Font.PLAIN,80),Color.WHITE, Text.Type.STATIC);

        this.sliderObjectCount = new Slider<>(this, new Vector(2,5),new Vector(250,40),widgetColor,new Setting<>("objectCount",1000),0,2500,1,"Object Count","The amount of particles on screen");
        this.cycleSpawnPattern = new Cycle<>(this,sliderObjectCount.getPos().getAdded(0,45),new Vector(250,40),widgetColor,"Spawn","Pattern for spawning the default shapes",new Setting<>("spawnPattern","Random"),"Random","Radial");
        this.toggleApplyGravity = new Toggle(this,cycleSpawnPattern.getPos().getAdded(0,45),new Vector(250,40),widgetColor,new Setting<>("applyGravity",true),"Apply Gravity","Turns gravity between particles on and off");
        this.toggleWallBounce = new Toggle(this,toggleApplyGravity.getPos().getAdded(0,45),new Vector(250,40),widgetColor,new Setting<>("wallBounce",true),"Wall Bounce","Bounces objects off of the walls");
        this.togglePaths = new Toggle(this,toggleWallBounce.getPos().getAdded(0,45),new Vector(250,40),widgetColor,new Setting<>("paths",true),"Paths","Shows a path of a particle's recent positions");
        this.toggleFieldLines = new Toggle(this,togglePaths.getPos().getAdded(0,45),new Vector(250,40),widgetColor,new Setting<>("fieldLines",true),"Field Lines","Shows all gravitational field lines. Very laggy");

        SettingManager.DEFAULT_MANAGER.loadAll();

        this.startCollision = new TitleButton(this, Vector.NULL(),new Vector(200,200), widgetColor,"","Particles collide and bounce off of one another using momentum",getClass().getResource("/collision.png"),() -> {
            int size = 99;
            getWindow().setScene(new BounceCollisionScene(toggleApplyGravity.getContainer().get(),toggleWallBounce.getContainer().get(),togglePaths.getContainer().get(),toggleFieldLines.getContainer().get(),sliderObjectCount.getContainer().get(),cycleSpawnPattern.getContainer().get(),size,size));
            SettingManager.DEFAULT_MANAGER.saveAll();
        });
        this.startMerge = new TitleButton(this, Vector.NULL(),new Vector(200,200), widgetColor,"","Particles collide and combine into larger particles",getClass().getResource("/merge.png"),() -> {
            int min = 99;
            int max = 100;
            getWindow().setScene(new MergeCollisionScene(toggleApplyGravity.getContainer().get(),toggleWallBounce.getContainer().get(),togglePaths.getContainer().get(),toggleFieldLines.getContainer().get(),sliderObjectCount.getContainer().get(),cycleSpawnPattern.getContainer().get(),min,max));
            SettingManager.DEFAULT_MANAGER.saveAll();
        });
        this.startThird = new TitleButton(this, Vector.NULL(),new Vector(200,200), widgetColor,"","Electrons and Positrons flow across the screen. Combining into neutrons",getClass().getResource("/charge.png"),() -> {
            SettingManager.DEFAULT_MANAGER.saveAll();
        });

        addElements(sliderObjectCount,toggleApplyGravity,toggleWallBounce,cycleSpawnPattern,togglePaths,toggleFieldLines);
        addElements(startCollision,startMerge,startThird);
        addElements(titleText);

        this.starManager = new StarManager(this,2,200);
        addSceneManagers(new TooltipManager(this, 20, 1));
    }

    @Override
    public void draw() {
        updateStartButtonPositions();
        updateTitlePosition(-80);

        //Draw Background
        new Rectangle(this,Vector.NULL(),getWindow().getSize(),new Color(25,25,25,255)).draw();

        //Draw Stars
        this.starManager.draw(getMousePos());

        super.draw();
    }

    private void updateStartButtonPositions() {
        int sep = 80;
        int h = 120;
        this.startCollision.setPos(startMerge.getPos().getSubtracted(startMerge.getSize().getX() + sep,0));
        this.startMerge.setPos(getWindow().getSize().getSubtracted(startMerge.getSize()).getMultiplied(.5).getAdded(0,h));
        this.startThird.setPos(startMerge.getPos().getAdded(startMerge.getSize().getX() + sep,0));
    }

    private void updateTitlePosition(int yOffset) {
        titleText.setPos(getWindow().getSize().getSubtracted(titleText.getSize()).getMultiplied(.5).getAdded(0,yOffset));
        titleText.setPos(titleText.getPos().getAdded(new Vector(0, Math.cos(titleAnimationStep) * 8)));
        titleAnimationStep += 0.05;
        if (titleAnimationStep >= Math.PI * 2) titleAnimationStep = 0;
    }

}
