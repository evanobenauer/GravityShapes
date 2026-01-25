# GravityShapes

![title](img/Title.gif.gif)

Gravity Shapes is a mini applet developed ing Java using my AppNexus library for UI creation.
This application created PhysicsObjects that orbit together with respect to Newtonian physics concepts such as:
- Kinematics
- Rotational Kinematics
- Forces/Torques
- Newton's Universal Law of Gravitation
- Conservation of Momentum
  
# Universal Options
Gravity Shapes has 6 main universal options users can toggle on and off

*Object Count*
- Determines the amount of naturally spawning objects on screen
  
*Gravity Strength*
- Determines the strength multiplier of the gravity equation. (The G constant)
  
*Spawn Patterns*
- Spawns objects randomly or radially (circular)
  
*Wall Bounce*
- Objects will bounce off of the walls with 90% restitution to keep all particles on screen
  
*Object Paths*
- Objects draw a path that fades to dark behind them
    
  ![gif](img/trails.gif.gif)
  
*Field Lines*
- Draws all gravitational field lines
    
  ![gif](img/fieldlines.gif.gif)
  
# Object Shooter
Clicking on the screen and pulling back will activate the object shooter
The object shooter will draw a blue outline of the object that is about to be shot
*Simulated Particle Line*
- The shooter uses my SimulatedParticle object to calculate the kinematics equations for 50 steps forward. This will cause curves in the shoot line
  
  ![gif](img/Shooting.gif.gif)

*Rapid Shooting*
- Holding LShift while holding back the shooter will enable rapid fire mode. This will shoot a new object every tick
  
  ![gif](img/RapidShooting.gif.gif)

*Size Changing*
- By default, the shooter size will equal the minimum spawn size for the scene
- The shooter size can be changed by clicking UP or DOWN on the keyboard while pulling back

# Bounce Mode (Left Button)
Objects bounce off of one another using Conservaton of Momentum
  
![gif](img/bounceCollision.gif.gif)
      
# Merge Mode (Middle Button)
Objects combine when collided using Conservation of Momentum and Rotational Motion
  
![gif](img/mergeRadial.gif.gif)
*Rotational Motion*
- Objects in Merge Mode sping their polygons according to Rotational Motion. 
- When objects collide, depending on their angle, they will apply a torque and spin the object
    
![gif](img/rotations.gif.gif)
    
# Electronics Mode (Right Button)
  Unimplemented

