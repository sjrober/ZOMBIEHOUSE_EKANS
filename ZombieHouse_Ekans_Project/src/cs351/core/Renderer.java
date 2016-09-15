package cs351.core;

import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;

/**
 * The Renderer is meant to manage the final translation/rotation of all registered
 * models, lights, etc. in order to render a scene each frame.
 *
 * @author Justin Hall
 */
public interface Renderer
{
  /**
   * When this is called this will walk through all of the actors that have
   * been registered with it and render them relative to the player.
   *
   * @param engine reference to the current engine
   * @param mode if this is set to DrawMode.FILL the objects will appear solid, but if
   *             this is set to DrawMode.LINE everything will look like it is a wire-frame model
   */
  void render(Engine engine, DrawMode mode, double deltaSeconds);

  /**
   * Initializes the renderer.
   *
   * @param engine reference to the main engine
   */
  void init(Engine engine);

  /**
   * Resets the renderer to its default starting state.
   */
  void shutdown();

  /**
   * This is how you let the renderer know which actor is the player in the scene. It will
   * automatically attach a camera to this actor.
   *
   * @param player the game's player
   * @param fieldOfView this determines how much the player can see on the screen - normal FOV is 45.0 degrees
   *                    and a large FOV is 90.0 degrees
   */
  void registerPlayer(Actor player, double fieldOfView);

  /**
   * Registers an Actor object with the renderer. What this does is to basically map an Actor object to
   * an object that should be rendered in the world. Shape3D can be things like a sphere, box, etc. (any
   * valid Shape3D from JavaFX).
   *
   * This should be used for all Actor objects, whether they move or don't move.
   *
   * @param actor object to add to the scene
   * @param shape shape of the object
   * @param diffuseColor this is the color the object scatters in all directions when light hits it
   * @param specularColor this is the color the object reflects in one direction when light hits it -
   *                      results in a shiny circles on the surface
   * @param ambientColor base color of the object
   */
  void registerActor(Actor actor, Shape3D shape, Color diffuseColor, Color specularColor, Color ambientColor);

  /**
   * Registers an Actor object with the renderer. This function requires a TriangleMesh which allows
   * you to load in a 3D model that is different from the standard Shape3D objects.
   *
   * @param actor object to add to the scene
   * @param entity object with all the mesh/animation data
   * @param diffuseColor this is the color the object scatters in all directions when light hits it
   * @param specularColor this is the color the object reflects in one direction when light hits it -
   *                      results in a shiny circles on the surface
   * @param ambientColor base color of the object
   */
  void registerActor(Actor actor, RenderEntity entity, Color diffuseColor, Color specularColor, Color ambientColor);

  /**
   * This should be used for all Actor objects that need to be textured.
   *
   * @param textureFile name of the texture file to be loaded
   * @param actor object to associate the texture with when rendering
   */
  void mapTextureToActor(String textureFile, Actor actor);
}
