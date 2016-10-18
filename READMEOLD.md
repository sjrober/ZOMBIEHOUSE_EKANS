# Read Me

---

## How to Use the Program:
Entry Point: Main.java
* Launches a window with buttons:
  * Play Zombie House 3d: Launches the main 3d implementation of the game
  * Play Zombie House 2d: Launches the supplemental  2d implementation of the game
  * (debug feature) Loading Screen: Takes you to a sample loading scene
  * (debug feature) Game Over: Takes you to the game over scene
  * (debug feature) Win Screen: Takes you to the game win scene
  * Settings: Takes you to a settings screen with several modifiable values on sliders with reasonable values (note Map Width and Map Height can go up to 100 to demonstrate A* and procedural generation with the 2d game)
* Controls:
  * W, A, S, D: Player Movement
  * Mouse on screen: rotates camera
  * Left and Right arrow keys: rotates camera
  * F1:     peek above the map
  * Space: Attack
  * F: toggle player lighting
  8 ESC: pause game

---

## Program Architecture:
##### general architecture:
* Main - instantiates a soundManager and Scenes
  * SoundManager - handles game sounds
  * Scenes - creates the different states of the game (menu, game, pause ect..)

##### Game Architecture:
* 3D Renderer - creates and renders game objects
  * EntityManager - controls and instantiates entities
  * MouseHandler - controls mouse input
  * KeyboardHandler - controls the keyboard input

##### Entity Architecture:
* Entity - abstract entity
  * Creature - abstract moving entity
    * Player - the player object
    * Zombie - a zombie object

##### Utilities and Tools:
* MapViewer - used to view a generated Map
* MapLoader - used to read in a map from file
* 2D renderer - used to view zombie pathing

---

## Zombie pathfinding, movement and AI:

```
Author: Jeffrey McCall - zombie movement and attributes, zombie collision detection, zombie pathfinding and master zombie.
Author: Atle Olson - Integration of zombie meshes with zombie movement code
Ben Matthews - Integration of sounds
```

The zombies are created in EntityManager. A list of zombies is created. When zombies are created, a zombie mesh is created for that individual zombie. This is the 3D representation of that zombie on the screen. The zombie also has a bounding circle that is drawn around it. It is used for collision detection. When the animation timer in ZombieHouse3d starts, the zombies are assigned a random direction to travel in. The tick() method in Zombie is called at 60 fps every time the timer is called. The tick() method is the main method that drives everything about the zombies. It does the following in the listed sequence:

1. A collision is checked for with all of the walls in the level. If the zombie's bounding circle intersects with one of the walls, the angle that the zombie is going is subtracted by 180, and the zombie is moved in the opposite direction of the wall. When it is no longer intersecting the walls, the zombie stops moving. This pops the zombie out of the wall. If the zombie does not detect the player, the zombie will pick a random uniformly distributed angle from 0-360 to travel in. The thread used to govern zombie decision rate in EntityManager is set to wait for 2 seconds, and then update the needed values for the zombie. So the next time the decision rate timer moves forward and the zombie makes a decision, the zombie will move in the new direction.

2. If the zombie has detected the player and is going after the player, then after the zombie has collided with the wall,a check is done to see if it has hit a corner. If it has, then the zombie is popped out of that corner and centered on the tile that is diagonal to the corner wall. The zombie will then choose to go in the direction of the player if the player is still in detection range of the zombie.

3. If a collision is not detected, then the zombie will continue to move in the direction of its current angle at a constant speed. 

4. A number of checks are done in tick() to see where the zombie is in relation to the center of tiles. If the zombie is at a position that is smaller than the halfway point of a tile in relation to the direction that the zombie is going, then I set the zombie position for purposes of pathfinding as the previous tile. This is so that the path that is being constructed for the zombie follows along the centers of tiles and not the corners. This helps the zombie to not get stuck in doorways.

5. The current tile that the zombie is standing on is determined by the checks I mentioned previously, and a function is called to do the pathfinding to the player for that zombie. If the zombie is within a Manhattan distance of 20 from the player, then the shortest path to the player is calculated using the A* pathfinding algorithm. The calculation of A* is contained in an inner class in Zombie called CalculatePath. 

6. The shortest path to the player is constantly being calculated for every zombie that is in range of the player. The “angle” field of zombie, which represents the direction that the zombies are going, can only be changed every 2 seconds, the decision rate of the zombies. So while the shortest path is constantly being calculated, the angle value is only reset every 2 seconds. This angle is based on the difference between the x and z values of the first 2 tiles in the shortest path. The zombie can move in 8 directions total to reach the player. 

##### Pathfinding:
The pathfinding for each zombie starts with building a graph of nodes that represent the tiles of the game map. This graph nodes are created in ZombieHouse3d, and are represented by the GraphNode class. The class TileGraph contains the graph itself, which is contained in a synchronized hash map. The nodes of the graph have a list of 8 neighbors as well as various other fields that aid in pathfinding. 

The actual implementation of A* itself is pretty standard, except for some added condition checking in the checkNeighbors method of CalculatePath. This condition checking is done so that A* doesn’t calculate paths that go diagonally through doorways. This was done to solve the problem of zombies getting stuck on doorways. 

The getPathLength method in CalculatePath returns the path length from the zombie to the player. If it is less than or equal to zombieSmell, which is set to 15, than it chases after the player. 

There is some additional functionality with the A* pathfinding specifically for the 2D game board. If the 2D board is being run, then the drawPath method in CalculatePath is called to draw a visual representation of the path from the zombie to the player. This path is only drawn to the screen if the zombie is within shortest path distance of zombieSmell from the player. 
 
##### Master Zombie
The master zombie is a single zombie that has special attributes. It runs on a faster decision rate thread in EntityManager which is separate from the other zombies. It is much faster than the other zombies. It also will immediately find a path to the player and go towards the player if any of the zombies detect the player. This functionality is governed by a boolean value “isMasterZombie” which is set to true if a zombie is designated as the master zombie. In EntityManager, the check is done to see if any zombies are going after the player, and if so, the master zombie is started towards the player. 

---

## Player Movement and Controls
```
Author: Atle Olson - final versions of keyboard and mouse controls plus final version of player collision detection
Author: Jeffrey McCall - initial versions of keyboard and mouse controls, initial version of player collision detection, player stamina
```

The stamina system with the player makes it so that when the player is running, the stamina is decreasing at a constant rate until it hits 0. When it hits 0, the player can no longer run until the stamina regenerates. The decrease and regeneration of stamina at a constant rate is handled within a thread in player called “PlayerStamina.” This is all handled within the player class.

All keyboard and mouse events are handled by the KeyboardEventHandler and the MouseEventHandler respectively. 

The code for handling player collision detection is in Player. When the player hits a wall, the collision detection stops the player from going in that same direction. The collision detection will slide the player along the wall to the left or right when the player is up against the wall and trying to move in the direction of the wall. 

---

## Procedural Map Generation: 
```
Author: Ben Matthews 
```

Procedural map generation is performed based on a series of algorithms that take in width, height and difficulty.

##### setup:
Firstly, a map is created that is 1/4th the size of the given map dimensions. this is done because it allows for simple hallway calculation on resize (a room of width 1 becomes a hallway of width 3). A rectangular region for the map is created and

##### Dividing the Space:
The space is divided using a binary splitting function, which takes a rectangle and splits it into 2 smaller rectangles that fill the space of the previous one. This is done such that the split is perpendicular to the smaller side. 

This function is repeated first 3 times to get the 4 region dimensions in the game, and is then repeated for each region until all rooms are at least less than size 3 (12 in full size game space). after this hallways are split off of the current rooms. the room at coordinate [0, 0] is split into a hallway if it is not already one.

##### Connecting Rooms Hallways and Regions:
Rooms and hallways are connected such that all rooms and hallways are connected and every hallway has at least 2 doors. First, all rooms and hallways for a region are connected in a non-directed graph with no cycles. then hallways with less than 2 connections are connected to a random neighbor. after all the rooms and hallways have been connected for each region, a single connection is inserted from each region to the next region in the path (1 -> 2 -> 3 -> 4)

##### Finishing:
the rooms and hallways are resized to full size (x4) and a 2d Tile array is created. for each room the tile is set to the region type unless it is the lower or right-hand edge, in which case it is a wall.
the exit is added to a random border room in region 4 and obstacles are created based on the difficulty setting on tiles with odd-numbered-coordinates (to prevent unreachable areas)

This tile array is returned as the product of the procedural generation

---

## Sounds Textures and Meshes:

```
Author: Atle Olson - Textures + Meshes 
Author: Ben Matthews  - Sounds
```

##### Textures:
Every Tile has 3 maps:
* Specular - Defines specular reflections
* Diffuse - Defines diffuse reflections
* Normal/Bump - displays a pseudo 3d texture

##### Meshes and 3D rendering:
Each object that is rendered in the game was added to the game scene and then viewed using a 
javaFX camera object

Walls and Floors were rendered in the game using a Box object which was assigned the appropriate Specular diffuse and Normal/Bump texturing as a material

Zombie Meshes were imported using the jfx3dObjimporter by InteractiveMesh.org. the importer would return an array of Mesh Objects which were then assigned to each zombie individually.

##### Sounds:
Sounds were implemented using a SoundManager class which loaded all of the sounds in the game and handled playing those sounds. AudioClips were used for the short game sounds and a MediaPlayer was used for the “long” Mp3 files. 

All sound clips used for this project were either created using FL-studio (music creation software) or taken from Freesound.org. Music for the project was taken from Purple-Planet Music

---

## Sources:

##### Texture sources:
* www.planetminecraft.com/texture_pack/stcms-resourcepack-128x-parallax-amp-normal-mapping/

##### 3D Model sources:
* Master zombie Model - http://tf3dm.com/3d-model/lambent-female-38649.html
* Zombie Model - http://tf3dm.com/3d-model/feral-ghoul-78896.html
* Model Importer - jfx3dObjimporter by InteractiveMesh.org

##### Sound sources:
* http://freesound.org/
* S: male_Thijs_loud_scream.aiff by thanvannispen | License: Attribution
* S: rip_tear FLESH!!!!.wav by aust_paul | License: Creative Commons 0
* S: Tearing Flesh by dereklieu | License: Attribution
* S: Man die by thestigmata | License: Attribution Noncommercial
* S: Zombie Attack by soykevin | License: Creative Commons 0
* S: Click by RADIY | License: Attribution
* S: btn121.wav by junggle | License: Attribution
* S: Footstep Drag Indoors .wav by abbahoot | License: Creative Commons 0
* S: Zombie Growling by gneube | License: Attribution
* S: Zombie 1 by Under7dude | License: Creative Commons 0
* S: Solo Zombie 1 by Slave2theLight | License: Attribution
* S: Zombie 20 by missozzy | License: Attribution
* S: Zombie by nanity05 | License: Creative Commons 0
* S: Footsteps on Tiles.wav by RutgerMuller | License: Creative Commons 0

##### Music sources:
* Harbinger of Death by Purple-Planet Music
* http://www.purple-planet.com/horror/4583971268



