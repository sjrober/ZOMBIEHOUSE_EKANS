CS351 - Zombie House

Authors: Justin Hall
	 Chris Sanches
	 Scott Cooper

Citations:
	<RenderEntity>
	http://www.opengl-tutorial.org/beginners-tutorials/tutorial-7-model-loading/

	http://www.interactivemesh.org/models/jfx3dimporter.html

	http://www.avajava.com/tutorials/lessons/how-do-i-recursively-display-all-files-and-directories-in-a-directory.html

	<CollisionDetection>
	https://developer.mozilla.org/en-US/docs/Games/Techniques/2D_collision_detection

	<RenderTree>
	http://gamedevelopment.tutsplus.com/tutorials/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space--gamedev-374

	http://www.gamedev.net/page/resources/_/technical/graphics-programming-and-theory/quadtrees-r1303

	https://www.flipcode.com/archives/Frustum_Culling.shtml

	<SpatialHashMap>
	http://www.gamedev.net/page/resources/_/technical/game-programming/spatial-hashing-r2697

	<ZombieHouseRenderer>
	http://stackoverflow.com/questions/1568568/how-to-convert-euler-angles-to-directional-vector

	http://answers.unity3d.com/questions/228203/getting-vector-which-is-pointing-to-the-rightleft.html

	http://docs.oracle.com/javase/8/javafx/graphics-tutorial/javafx-3d-graphics.htm

	<ZombieHouseSoundEngine>
	http://www.vttoth.com/CMS/index.php/technical-notes/68

	http://what-when-how.com/javafx-2/playing-audio-using-the-media-classes-javafx-2-part-1/

	https://www.daniweb.com/programming/game-development/threads/139450/3d-sound-calculating-pan

	<Textures>
	http://images.freeimages.com/images/previews/8ad/brick-texture-1171939.jpg

	http://cdn.designbeep.com/wp-content/uploads/2013/09/11.rock-texture.jpg

	http://image.naldzgraphics.net/2013/03/20-dark-white-ice-texture-free-hi-res.jpg

	https://www.filterforge.com/filters/4747.jpg

	http://40watt.biz/graphics/images/textures/stone_blocks.jpg

	https://www.filterforge.com/filters/9452.jpg

	http://mameara.com/wp-content/uploads/2010/11/metal20.jpg

	<Sounds>
	https://www.audioblocks.com/royalty-free-audio/footsteps-sound-effects

	http://www.freesfx.co.uk/soundeffects/footsteps/?p=2

	http://soundbible.com/1095-Large-Truck-Chains.html

	http://soundbible.com/1458-Monster-Roar.html

	<3D Assets>
	http://tf3dm.com/3d-model/zombie-pack-5-zombies-fully-rigged-animated-5522.html

	http://tf3dm.com/3d-model/heller-zombie-90130.html

	<3D Model Loader>
	http://www.interactivemesh.org/models/jfx3dimporter.html

Build Notes:
	1) Make sure that the file cs351.core.resources is marked as a resources folder (may give errors otherwise).
	
	2) cs351.project1.sound and cs351.project1.textures *might* need to be marked as resource folders, but in testing this was usually not needed.

	3) jimObjModelImporterJFX.jar needs to be set as a dependency (it is inside of the src folder).

Features:
	* Main class: Game
	* Inside of cs351.core.resources is engine.settings - this is where the user can specify different settings for playing the game with
	* WASD keys move forward, left, back and right and the mouse moves the camera
	* There are 10 levels: each level the player's health and stamina increase, but so does the default zombie speed