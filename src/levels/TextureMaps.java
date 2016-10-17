package levels;

import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;

/**
 * @author Atle Olson 
 * Player objects with position coordinates
 * 
 */
public class TextureMaps 
{

  public  final static String BRICK_DIFFUSE_MAP   = "File:Resources/Images/Textures/brick.png";
  private final static String BRICK_SPECULAR_MAP  = "File:Resources/Images/Textures/brick_s.png";
  private final static String BRICK_NORMAL_MAP    = "File:Resources/Images/Textures/brick_n.png";
  public  final static String RED_DIFFUSE_MAP     = "File:Resources/Images/Textures/redstone_block.png";
  private final static String RED_SPECULAR_MAP    = "File:Resources/Images/Textures/redstone_block_s.png";
  private final static String RED_NORMAL_MAP      = "File:Resources/Images/Textures/redstone_block_n.png";
  public  final static String YELLOW_DIFFUSE_MAP  = "File:Resources/Images/Textures/gold_block.png";
  private final static String YELLOW_SPECULAR_MAP = "File:Resources/Images/Textures/gold_block_s.png";
  private final static String YELLOW_NORMAL_MAP   = "File:Resources/Images/Textures/brick_block_n.png";
  public  final static String BLUE_DIFFUSE_MAP    = "File:Resources/Images/Textures/lapis_block.png";
  private final static String BLUE_SPECULAR_MAP   = "File:Resources/Images/Textures/lapis_block_s.png";
  private final static String BLUE_NORMAL_MAP     = "File:Resources/Images/Textures/lapis_block_n.png";
  public  final static String BLACK_DIFFUSE_MAP   = "File:Resources/Images/Textures/coal_block.png";
  private final static String BLACK_SPECULAR_MAP  = "File:Resources/Images/Textures/coal_block_s.png";
  private final static String BLACK_NORMAL_MAP    = "File:Resources/Images/Textures/coal_block_n.png";
  public  final static String IRON_DIFFUSE_MAP    = "File:Resources/Images/Textures/iron_block.png";
  private final static String IRON_SPECULAR_MAP   = "File:Resources/Images/Textures/iron_block_s.png";
  private final static String IRON_NORMAL_MAP     = "File:Resources/Images/Textures/iron_block_n.png";
  public  final static String GLOW_DIFFUSE_MAP    = "File:Resources/Images/Textures/glowstone.png";
  private final static String GLOW_SPECULAR_MAP   = "File:Resources/Images/Textures/glowstone_s.png";
  private final static String GLOW_NORMAL_MAP     = "File:Resources/Images/Textures/glowstone_n.png";

  private final static String GRAFFITI_DIFFUSE_MAP = "File:Resources/Images/Textures/graffiti.png";
  private final static String EYE_DIFFUSE_MAP = "File:Resources/Images/Textures/eye.png";
  private final static String PAPER_DIFFUSE_MAP = "File:Resources/Images/Textures/paper.png";
  private final static String BONE_DIFFUSE_MAP = "File:Resources/Images/Textures/bone.png";

  public static PhongMaterial brickMaterial = new PhongMaterial();
  public static PhongMaterial redMaterial = new PhongMaterial();
  public static PhongMaterial yellowMaterial = new PhongMaterial();
  public static PhongMaterial blueMaterial = new PhongMaterial();
  public static PhongMaterial blackMaterial = new PhongMaterial();
  public static PhongMaterial ironMaterial = new PhongMaterial();
  public static PhongMaterial glowMaterial = new PhongMaterial();

  public static PhongMaterial graffitiMaterial = new PhongMaterial();
  public static PhongMaterial eyeMaterial = new PhongMaterial();
  public static PhongMaterial paperMaterial = new PhongMaterial();
  public static PhongMaterial boneMaterial = new PhongMaterial();

  static Image brickD  = new Image(BRICK_DIFFUSE_MAP,   128, 128, true, true, false);
  static Image brickS  = new Image(BRICK_SPECULAR_MAP,  128, 128, true, true, false);
  static Image brickN  = new Image(BRICK_NORMAL_MAP,    128, 128, true, true, false);
  static Image redD    = new Image(RED_DIFFUSE_MAP,     128, 128, true, true, false);
  static Image redS    = new Image(RED_SPECULAR_MAP,    128, 128, true, true, false);
  static Image redN    = new Image(RED_NORMAL_MAP,      128, 128, true, true, false);
  static Image yellowD = new Image(YELLOW_DIFFUSE_MAP,  128, 128, true, true, false);
  static Image yellowS = new Image(YELLOW_SPECULAR_MAP, 128, 128, true, true, false);
  static Image yellowN = new Image(YELLOW_NORMAL_MAP,   128, 128, true, true, false);
  static Image blueD   = new Image(BLUE_DIFFUSE_MAP,    128, 128, true, true, false);
  static Image blueS   = new Image(BLUE_SPECULAR_MAP,   128, 128, true, true, false);
  static Image blueN   = new Image(BLUE_NORMAL_MAP,     128, 128, true, true, false);
  static Image blackD  = new Image(BLACK_DIFFUSE_MAP,   128, 128, true, true, false);
  static Image blackS  = new Image(BLACK_SPECULAR_MAP,  128, 128, true, true, false);
  static Image blackN  = new Image(BLACK_NORMAL_MAP,    128, 128, true, true, false);
  static Image ironD   = new Image(IRON_DIFFUSE_MAP,    128, 128, true, true, false);
  static Image ironS   = new Image(IRON_SPECULAR_MAP,   128, 128, true, true, false);
  static Image ironN   = new Image(IRON_NORMAL_MAP,     128, 128, true, true, false);
  static Image glowD   = new Image(GLOW_DIFFUSE_MAP,    128, 128, true, true, false);
  static Image glowS   = new Image(GLOW_SPECULAR_MAP,   128, 128, true, true, false);
  static Image glowN   = new Image(GLOW_NORMAL_MAP,     128, 128, true, true, false);

  static Image graffitiD = new Image(GRAFFITI_DIFFUSE_MAP, 128, 128, true, true, false);
  static Image eyeD = new Image(EYE_DIFFUSE_MAP, 128, 128, true, true, false);
  static Image paperD = new Image(PAPER_DIFFUSE_MAP, 128, 128, true, true, false);
  static Image boneD = new Image(BONE_DIFFUSE_MAP, 128, 128, true, true, false);
 
  /**
   * Initalizes all the texture maps
   * 
   */  
  public static void initializeMaps()
  {
    brickMaterial.setDiffuseMap   (brickD);
    brickMaterial.setSpecularMap  (brickS);
    brickMaterial.setBumpMap      (brickN);
    redMaterial.setDiffuseMap     (redD);
    redMaterial.setSpecularMap    (redS);
    redMaterial.setBumpMap        (redN);
    yellowMaterial.setDiffuseMap  (yellowD);
    yellowMaterial.setSpecularMap (yellowS);
    yellowMaterial.setBumpMap     (yellowN);
    blueMaterial.setDiffuseMap    (blueD);
    blueMaterial.setSpecularMap   (blueS);
    blueMaterial.setBumpMap       (blueN);
    blackMaterial.setDiffuseMap   (blackD);
    blackMaterial.setSpecularMap  (blackS);
    blackMaterial.setBumpMap      (blackN);
    ironMaterial.setDiffuseMap    (ironD);
    ironMaterial.setSpecularMap   (ironS);
    ironMaterial.setBumpMap       (ironN);
    glowMaterial.setDiffuseMap    (glowD);
    glowMaterial.setSpecularMap   (glowS);
    glowMaterial.setBumpMap       (glowN);

    graffitiMaterial.setDiffuseMap (graffitiD);
    eyeMaterial.setDiffuseMap (eyeD);
    paperMaterial.setDiffuseMap (paperD);
    boneMaterial.setDiffuseMap (boneD);
  }
}
