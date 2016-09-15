package cs351.core;

import cs351.core.Engine;
import cs351.core.Game;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

/**
 * CITATION: http://www.opengl-tutorial.org/beginners-tutorials/tutorial-7-model-loading/
 * The above website is really good for learning stuff about 3D programming.
 *
 * CITATION: http://www.interactivemesh.org/models/jfx3dimporter.html
 * The above website is where I got the code that lets us load 3D assets.
 *
 * CITATION: http://www.avajava.com/tutorials/lessons/how-do-i-recursively-display-all-files-and-directories-in-a-directory.html
 *
 * An renderEntity represents a game object that is not easily
 * represented using the standard JavaFX class of shapes. This
 * includes anything that needs to be loaded in as a 3d model
 * containing vertex, normal, texture and animation data.
 *
 * Currently the only file format that is supported is the .obj
 * format.
 *
 * If you pass in a directory containing a list of .obj files,
 * this class will read them in in order and interpret them as
 * a single animation sequence for the loaded object. If you
 * pass in just one .obj file it will assume you do not want
 * to animate the object.
 *
 * @author Justin Hall
 */
public class RenderEntity
{
  private static final HashMap<String, Model[]> MESH_LOOKUP_TABLE = new HashMap<>(100);
  private ObjModelImporter importer = new ObjModelImporter();
  private TriangleMesh[] meshList;
  private TriangleMesh mesh;
  private Model[] sequence;
  private int currModel = 0;
  private int animFramesPerSecond = 1;
  private double timeElapsed = 0.0;

  /**
   * Model class.
   * @author Justin Hall
   */
  private class Model
  {
    public float[] vertices; // 3 floats = 1 vertex
    public float[] texCoords; // 2 floats = 1 texture coordinate
    public float[] normals; // 3 floats = 1 normal
    public int[] faces;
  }

  /**
   * Loads a model/series of models representing an animated sequence.
   *
   * @param filename non-absolute path to the file
   */
  public RenderEntity(String filename)
  {
    // check if the model sequence already exists
    if (MESH_LOOKUP_TABLE.containsKey(filename)) sequence = MESH_LOOKUP_TABLE.get(filename);
    // if not, load it
    else
    {
      loadModel(filename, getExtension(filename));
      MESH_LOOKUP_TABLE.put(filename, sequence);
    }
    // build the meshList
    meshList = new TriangleMesh[sequence.length];
    for (int i = 0; i < sequence.length; i++)
    {
      Model model = sequence[i];
      meshList[i] = new TriangleMesh();
      meshList[i].getPoints().addAll(model.vertices);
      meshList[i].getTexCoords().addAll(model.texCoords);
      meshList[i].getNormals().addAll(model.normals);
      meshList[i].getFaces().addAll(model.faces);
    }
    // set the starting mesh
    mesh = meshList[currModel];
    animFramesPerSecond = sequence.length;
  }

  /**
   * This updates the TriangleMesh of this renderEntity. This is going to be
   * used as a means of enabling per-vertex animation in the future.
   */
  public void animate(double deltaSeconds)
  {
    timeElapsed += deltaSeconds;
    // 1.0 / animFramesPerSecond would be 1.0 for 1 animation frame per second, 0.5 for
    // 2 animation frames per second, etc.
    if (timeElapsed > 1.0 / animFramesPerSecond)
    {
      timeElapsed = 0;
      currModel++;
      if (currModel > sequence.length - 1) currModel = 0;
      mesh = meshList[currModel];
    }
  }

  /**
   * Gets the current mesh.
   * @return current mesh
   */
  public TriangleMesh getMesh()
  {
    return mesh;
  }

  // used by the renderer
  public TriangleMesh[] getMeshList()
  {
    return meshList;
  }

  /**
   * Frees all now-dead references. Do not use after this.
   */
  public void destroy()
  {
    for (TriangleMesh mesh : meshList)
    {
      mesh.getPoints().clear();
      mesh.getTexCoords().clear();
      mesh.getNormals().clear();
      mesh.getFaces().clear();
    }
  }

  private String getExtension(String filename)
  {
    for (int i = 0; i < filename.length(); i++)
    {
      if (filename.charAt(i) == '.') return filename.substring(i, filename.length());
    }
    return "";
  }

  /**
   * Checks to see whether it needs to load a single .obj file or a .zip file containing
   * many .obj files.
   *
   * @param filename filename
   * @param extension file extension
   */
  private void loadModel(String filename, String extension)
  {
    // check the file type
    if (extension.equals(".obj"))
    {
      sequence = new Model[1];
      sequence[0] = loadObj(filename);
    }
    else if (extension.equals(".txt"))
    {
      sequence = loadFromTxt(filename);
    }
    else throw new RuntimeException(filename + " is not a recognized format : must be either .obj or directory");
  }

  private Model[] loadFromTxt(String filename)
  {
    // this line builds the path to the directory
    //File folder = new File(".\\src\\cs351\\core\\" + directory);
    InputStream stream = RenderEntity.class.getResourceAsStream(filename);
    BufferedReader read = new BufferedReader(new InputStreamReader(stream));
    ArrayList<String> files = new ArrayList<String>(25);
    try
    {
      String line;
      while ((line = read.readLine()) != null) files.add(line);
      stream.close();
      read.close();
    }
    catch (IOException e)
    {
      System.out.println("Could not load " + filename);
    }
    Model[] models = new Model[files.size()];
    int index = 0;
    for (String file : files)
    {
      //System.out.println("RenderEntity: loading " + file);
      models[index] = loadObj(file);
      index++;
    }
    return models;
  }

  private void packData(Model model, TriangleMesh mesh)
  {
    model.vertices = new float[mesh.getPoints().size()];
    model.texCoords = new float[mesh.getTexCoords().size()];
    model.normals = new float[mesh.getNormals().size()];
    model.faces = new int[mesh.getFaces().size()];

    mesh.getPoints().toArray(model.vertices);
    mesh.getTexCoords().toArray(model.texCoords);
    mesh.getNormals().toArray(model.normals);
    mesh.getFaces().toArray(model.faces);

    mesh.getPoints().clear();
    mesh.getTexCoords().clear();
    mesh.getNormals().clear();
    mesh.getFaces().clear();
  }

  /**
   * Uses the ObjModelImporter to load the .obj file and extract its data.
   *
   * @param file file to read from
   * @return valid model object containing the vertex/texture coordinate/normal/face data
   */
  private Model loadObj(File file)
  {
    importer.clear();
    importer.read(file);
    Model model = new Model();
    MeshView meshView = importer.getImport()[0];
    TriangleMesh mesh = (TriangleMesh) meshView.getMesh();

    packData(model, mesh);

    return model;
  }

  /**
   * Uses the ObjModelImporter to load the .obj file and extract its data.
   *
   * @param filename file
   * @return valid model object containing the vertex/texture coordinate/normal/face data
   */
  private Model loadObj(String filename)
  {
    importer.clear();
    URL url = Engine.class.getResource(filename);
    importer.read(url);
    Model model = new Model();
    MeshView meshView = importer.getImport()[0];
    TriangleMesh mesh = (TriangleMesh)meshView.getMesh();

    packData(model, mesh);

    return model;

    /*
    vertexBuffer.clear();
    textureBuffer.clear();
    normalBuffer.clear();

    Model model = new Model();

    InputStream stream = ZombieHouseRenderer.class.getResourceAsStream(filename);
    BufferedReader read = new BufferedReader(new InputStreamReader(stream));
    String line;
    try
    {
      while ((line = read.readLine()) != null)
      {
        String[] elements;
        String[][] faces = new String[3][];
        if (line.length() < 3) continue;
        // vertex
        if (line.charAt(0) == 'v' && line.charAt(1) == ' ')
        {
          elements = (line.substring(2)).split(" ", 3);
          vertexBuffer.add(new Point3D(Float.parseFloat(elements[0]),
                                       Float.parseFloat(elements[1]),
                                       Float.parseFloat(elements[2])));
        }
        // texture
        else if (line.charAt(0) == 'v' && line.charAt(1) == 't')
        {
          elements = (line.substring(3)).split(" ", 2);
          textureBuffer.add(new Point2D(Float.parseFloat(elements[0]),
                                        Float.parseFloat(elements[1])));
        }
        // normal
        else if (line.charAt(0) == 'v' && line.charAt(1) == 'n')
        {
          elements = (line.substring(3)).split(" ", 3);
          normalBuffer.add(new Point3D(Float.parseFloat(elements[0]),
                                       Float.parseFloat(elements[1]),
                                       Float.parseFloat(elements[2])));
        }
        // face
        else if (line.charAt(0) == 'f')
        {
          elements = (line.substring(2)).split(" ", 3);
          for (int i = 0; i < elements.length; i++) faces[i] = elements[i].split("/", 3);
          for (int LOCATION_X = 0; LOCATION_X < faces.length; LOCATION_X++)
          {
            for (int LOCATION_Y = 0; LOCATION_Y < faces[0].length; LOCATION_Y++)
            {
              facesBuffer.add(Short.parseShort(faces[LOCATION_X][LOCATION_Y]));
            }
          }
        }
      }
    }
    catch (IOException e)
    {
      System.out.println("Unable to read " + filename);
      System.exit(-1);
    }

    int numFaces = facesBuffer.size() / 9; // each face = 9 elements where each element is a vertex/texture coordinate/normal
    System.out.println(numFaces);
    model.vertices = new float[numFaces * 9]; // there are 3 vertices per face, and each vertex = 3 floats : 3 * 3
    model.texCoords = new float[numFaces * 6]; // there are 2 texture coordinates per face, and each tex coord = 2 floats : 3 * 2
    model.normals = new float[numFaces * 9]; // there are 3 normals per face, and each normals = 3 floats : 3 * 3
    model.faces = new int[facesBuffer.size() / 3];
    short index;
    // used to store the current index of where we are in the float[] arrays
    // for the model
    int currVertIndex = 0;
    int currTexIndex = 0;
    int currNormIndex = 0;
    int currIndex = 1; // max = 9
    for (int i = 0; i < facesBuffer.size(); i++)
    {
      // .obj files start at index 1 instead of 0 like in C/C++/Java so the - 1 prevents out of bounds problems
      index = (short)(facesBuffer.get(i) - 1);
      if (currIndex == 3 || currIndex == 6 || currIndex == 9)
      {
        model.normals[currNormIndex] = (float)normalBuffer.get(index).getX();
        model.normals[currNormIndex + 1] = (float)normalBuffer.get(index).getY();
        model.normals[currNormIndex + 2] = (float)normalBuffer.get(index).getZ();
        currNormIndex += 3;
      }
      else if (currIndex == 2 || currIndex == 5 || currIndex == 8)
      {
        model.texCoords[currTexIndex] = (float)textureBuffer.get(index).getX();
        model.texCoords[currTexIndex + 1] = (float)textureBuffer.get(index).getY();
        currTexIndex += 2;
      }
      else
      {
        model.vertices[currVertIndex] = (float)vertexBuffer.get(index).getX();
        model.vertices[currVertIndex + 1] = (float)vertexBuffer.get(index).getY();
        model.vertices[currVertIndex + 2] = (float)vertexBuffer.get(index).getZ();
        currVertIndex += 3;
      }

      currIndex++;
      if (currIndex > 9) currIndex = 1;
    }

    for (int i = 0; i < model.faces.length; i++)
    {
      model.faces[i] = i;
      System.out.print(model.vertices[i] + " ");
      if (( i + 1 ) % 3 == 0) System.out.println();
    }

    /*
    float[] points = {
            -1, 1, 0,
            -1, -1, 0,
            1, 1, 0,
            1, -1, 0
    };
    float[] texCoords = {
            1, 1,
            1, 0,
            0, 1,
            0, 0
    };
    int[] faces = {
            2, 2, 1, 1, 0, 0,
            2, 2, 3, 3, 1, 1
    };
    model.vertices = points;
    model.texCoords = texCoords;
    model.faces = faces;
    */
  }
}
