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

    //Managers
    private final StarManager starManager;

    //Title
    private final Text titleText;
    private double titleAnimationStep = 0;

    //Setting Widgets
    public Slider<Integer> sliderObjectCount;
    public Toggle toggleWallBounce;
    public Toggle toggleApplyGravity;
    public Toggle togglePaths;
    public Toggle toggleFieldLines;
    public Cycle<String> cycleSpawnPattern;
    public Slider<Integer> sliderBounceObjectSize;
    public Slider<Integer> sliderMergeObjectSizeMin;
    public Slider<Integer> sliderMergeObjectSizeMax;

    //Start Buttons
    public Button startBounce;
    public Button startMerge;
    public Button startCharge;

    public TitleScene() {
        super("Title Scene");
        Color widgetColor = new Color(0,125,255);

        //Init Title Element
        this.titleText = new Text(this, Vector.NULL(),"Gravity Shapes",new Font("Arial Black",Font.PLAIN,80),Color.WHITE, Text.Type.STATIC);

        //Init Widgets
        initSettingWidgets(widgetColor);
        initStartButtons(widgetColor);

        //Load Settings
        SettingManager.DEFAULT_MANAGER.loadAll();

        //Add all elements
        addElements(sliderObjectCount,toggleApplyGravity,toggleWallBounce,cycleSpawnPattern,togglePaths,toggleFieldLines);
        addElements(startBounce,startMerge, startCharge);
        addElements(sliderBounceObjectSize,sliderMergeObjectSizeMax,sliderMergeObjectSizeMin);
        addElements(titleText);

        //Define managers
        this.starManager = new StarManager(this,2,200);
        addSceneManagers(new TooltipManager(this, 20, 1));
    }

    @Override
    public void draw() {
        updateStartButtonPositionsAndSettings();
        updateTitlePosition(-80);

        //Draw Background
        new Rectangle(this,Vector.NULL(),getWindow().getSize(),new Color(25,25,25,255)).draw();

        //Draw Stars
        this.starManager.draw(getMousePos());

        super.draw();
    }

    // =============================================

    // Init Functions

    // =============================================

    private void initSettingWidgets(Color widgetColor) {
        this.sliderObjectCount = new Slider<>(this, new Vector(2,5),new Vector(250,40),widgetColor,new Setting<>("objectCount",1000),0,2500,1,"Object Count","The amount of particles on screen");
        this.cycleSpawnPattern = new Cycle<>(this,sliderObjectCount.getPos().getAdded(0,45),new Vector(250,40),widgetColor,"Spawn","Pattern for spawning the default shapes",new Setting<>("spawnPattern","Random"),"Random","Radial");
        this.toggleApplyGravity = new Toggle(this,cycleSpawnPattern.getPos().getAdded(0,45),new Vector(250,40),widgetColor,new Setting<>("applyGravity",true),"Apply Gravity","Turns gravity between particles on and off");
        this.toggleWallBounce = new Toggle(this,toggleApplyGravity.getPos().getAdded(0,45),new Vector(250,40),widgetColor,new Setting<>("wallBounce",false),"Wall Bounce","Bounces objects off of the walls");
        this.togglePaths = new Toggle(this,toggleWallBounce.getPos().getAdded(0,45),new Vector(250,40),widgetColor,new Setting<>("paths",false),"Paths","Shows a path of a particle's recent positions");
        this.toggleFieldLines = new Toggle(this,togglePaths.getPos().getAdded(0,45),new Vector(250,40),widgetColor,new Setting<>("fieldLines",false),"Field Lines","Shows all gravitational field lines. Very laggy");

        this.sliderBounceObjectSize = new Slider<>(this,Vector.NULL(),new Vector(200,50),widgetColor,new Setting<>("bounceObjectSize",5),1,30,1,"Radius","Size of all objects on screen");

        this.sliderMergeObjectSizeMin = new Slider<>(this,Vector.NULL(),new Vector(200,50),widgetColor,new Setting<>("mergeObjectsMin",100),1,1000,1,"Min M","Maximum mass size for all spawned objects");
        this.sliderMergeObjectSizeMax = new Slider<>(this,Vector.NULL(),new Vector(200,50),widgetColor,new Setting<>("mergeObjectMax",100),1,1000,1,"Max M","Minimum mass size for all spawned objects");
    }

    private void initStartButtons(Color widgetColor) {
        this.startBounce = new TitleButton(this, Vector.NULL(),new Vector(200,200), widgetColor,"","Particles collide and bounce off of one another using momentum. This is VERY EXPERIMENTAL. My algorithm isnt great...",getClass().getResource("/collision.png"),() -> {
            getWindow().setScene(new BounceCollisionScene(toggleApplyGravity.getContainer().get(),toggleWallBounce.getContainer().get(),togglePaths.getContainer().get(),toggleFieldLines.getContainer().get(),sliderObjectCount.getContainer().get(),cycleSpawnPattern.getContainer().get(),sliderBounceObjectSize.getContainer().get()));
            SettingManager.DEFAULT_MANAGER.saveAll();
        });

        this.startMerge = new TitleButton(this, Vector.NULL(),new Vector(200,200), widgetColor,"","Particles collide and combine into larger particles",getClass().getResource("/merge.png"),() -> {
            int min = sliderMergeObjectSizeMin.getContainer().get();
            int max = sliderMergeObjectSizeMax.getContainer().get();
            getWindow().setScene(new MergeCollisionScene(toggleApplyGravity.getContainer().get(),toggleWallBounce.getContainer().get(),togglePaths.getContainer().get(),toggleFieldLines.getContainer().get(),sliderObjectCount.getContainer().get(),cycleSpawnPattern.getContainer().get(),min,max));
            SettingManager.DEFAULT_MANAGER.saveAll();
        });

        this.startCharge = new TitleButton(this, Vector.NULL(),new Vector(200,200), widgetColor,"","Electrons and Positrons flow across the screen. Combining into neutrons",getClass().getResource("/charge.png"),() -> {
            SettingManager.DEFAULT_MANAGER.saveAll();
        });
    }

    // =============================================

    // Update Functions

    // =============================================

    private void updateStartButtonPositionsAndSettings() {
        int sep = 80;
        int h = 120;
        this.startBounce.setPos(startMerge.getPos().getSubtracted(startMerge.getSize().getX() + sep,0));
        this.startMerge.setPos(getWindow().getSize().getSubtracted(startMerge.getSize()).getMultiplied(.5).getAdded(0,h));
        this.startCharge.setPos(startMerge.getPos().getAdded(startMerge.getSize().getX() + sep,0));

        this.sliderBounceObjectSize.setPos(startBounce.getPos().getAdded(0,startBounce.getSize().getY() + 20));

        this.sliderMergeObjectSizeMin.setPos(startMerge.getPos().getAdded(0,startMerge.getSize().getY() + 20));
        this.sliderMergeObjectSizeMax.setPos(sliderMergeObjectSizeMin.getPos().getAdded(0,sliderMergeObjectSizeMin.getSize().getY()).getAdded(0,5));
    }

    private void updateTitlePosition(int yOffset) {
        titleText.setPos(getWindow().getSize().getSubtracted(titleText.getSize()).getMultiplied(.5).getAdded(0,yOffset));
        titleText.setPos(titleText.getPos().getAdded(new Vector(0, Math.cos(titleAnimationStep) * 8)));
        titleAnimationStep += 0.05;
        if (titleAnimationStep >= Math.PI * 2) titleAnimationStep = 0;
    }

}
