package levels;

import java.util.ArrayList;
import java.util.Random;
/**
 * 
 * @author Ben Matthews
 * Contains the code to create a procedurally generated level
 * for the game.
 *
 */
public class ProceduralMap
{
  public static final boolean DEBUG = false;
  
  /**Generates a level represented as a 2d Tile array
   * of approximately the dimensions given<br><br>
   * 
   * <b><u>NOTE:</b></u><br>
   * This method will not retrun a Tile array of the exact
   * specified dimensions, and it should not be expected to. <br><br>
   * 
   * More specifically, the dimensions will be equal to floor(dim/4)*4 + 1.
   * This is a byproduct of the procedural generation and is done to make the
   * calculations fors an art rooms and hallways simpler
   * 
   * @param rows
   * @param cols
   * @param difficulty
   * @return a newly generated level as a 2d array of Tile objects in rows x columns format
   */
  public static Tile[][] generateMap(int rows, int cols, int difficulty){
    rows /= 4;
    cols /= 4;
    
    Tile[][] map = new Tile[rows*4 + 1][cols*4 + 1];
    Rectangle[][] basicMap = new Rectangle[rows][cols];
    
    //initialize
    for (int i = 0; i < rows; i++){
      for (int j = 0; j < cols; j++){
        basicMap[i][j] = null;
      }
    }
    
    ArrayList<ArrayList<Rectangle>> regions = generateRegions(basicMap);
    constructNeighbors(basicMap, regions);
    generatePaths(regions, difficulty);
    if (DEBUG) printCollsionCases(regions);
    resizeRectangles(regions);
    fillMap(map, regions, difficulty);
    
    return map;
  }
  
  
  /**
   * generates an ArrayList of 5 ArrayLists of Rectangle objects where the rectangles in the first
   * list represent the dimensions of the regions, and the rectangles in the next 4 lists represent
   * a collection of rooms and hallways within that region 
   * 
   * @param basicMap - a 2d map representing which rectangle is in which coordinate space
   * @return An Array of Rectangle collections where the first array represents the dimensions
   * of the proceeding regions
   */
  private static ArrayList<ArrayList<Rectangle>> generateRegions(Rectangle[][] basicMap){
    Random random = new Random();
    ArrayList<ArrayList<Rectangle>> regions = new ArrayList<>();
    
    /*
     * Binary split based off of a combination of Vertical and
     * horiontal splits. can be represented in binary where a
     * 0 is a Horizontal split, and a 1 is a vertical split
     * 
     * the farthest left-hand bit represents the "middle"split type
     * the next one represents the "upper" or "left" split (depending
     * on the first split) and the last bit represents the "lower" or
     * "right" split
     */
    int[] orientation = null;
    int regionSplit = random.nextInt(2);
    if (regionSplit == 0) orientation = new int[]{0,1,1};
    if (regionSplit == 1) orientation = new int[]{1,0,0};
    
    
    if (DEBUG) System.out.println(regionSplit);
    
    Rectangle board = new Rectangle(0, 0, basicMap[0].length, basicMap.length);
    
    Rectangle[] holder = new Rectangle[2];
    Rectangle[] holder2 = new Rectangle[2];
    
    Rectangle region1 = new Rectangle();
    Rectangle region2 = new Rectangle();
    Rectangle region3 = new Rectangle();
    Rectangle region4 = new Rectangle();
    
    double[] percentage = new double[3];
    
    //split between .4 and .6
    percentage[0] = random.nextDouble()*.2 + .4;
    percentage[1] = random.nextDouble()*.2 + .4;
    percentage[2] = random.nextDouble()*.2 + .4;
    
    
    //get the region dimensions
    {
      holder = board.getSubRectangles(orientation[0], percentage[0]);

      holder2 = holder[0].getSubRectangles(orientation[1], percentage[1]);
      region1 = holder2[0];
      region2 = holder2[1];

      holder2 = holder[1].getSubRectangles(orientation[2], percentage[2]);
      if (orientation[2] != orientation[0]){ //important to make sure that region 2 and 3 are adjacent
        region3 = holder2[1];
        region4 = holder2[0];
      } else {
        region3 = holder2[0];
        region4 = holder2[1];
      }
    }
    
    //passing in dimensions
    regions.add(new ArrayList<Rectangle>());
    regions.get(0).add(region1);
    regions.get(0).add(region2);
    regions.get(0).add(region3);
    regions.get(0).add(region4);
    
    regions.add(getRectangles(basicMap, region1, 1));
    regions.add(getRectangles(basicMap, region2, 2));
    regions.add(getRectangles(basicMap, region3, 3));
    regions.add(getRectangles(basicMap, region4, 4));
    
    if (DEBUG) printArray(basicMap);
    
    return regions;
  }
  
  
  /**
   * this method gets the rectangles that will fill a given region of the map
   * and adds references to the appropriate rectangle inside of basicMap
   * 
   * @param basicMap - a map of rectangles to coordinates
   * @param bounds - the bounding rectangle of the region
   * @param region - an integer (1-4) representing the region
   * @return an arraylist of rectangles representing rooms in the given region
   */
  private static ArrayList<Rectangle> getRectangles(Rectangle[][] basicMap, Rectangle bounds, int region){
    ArrayList<Rectangle> rectangles = new ArrayList<>();
    ArrayList<Rectangle> splits = new ArrayList<>();
    Random random = new Random();
    splits.add(new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height)); //new rectangle to prevent pointer overlap
    
    while (!splits.isEmpty()){
      Rectangle[] temp;
      Rectangle r = splits.get(0);
      
      if (r.width >= 5 || r.height >= 5){
        double percentage = random.nextDouble()*.2 + .4; //between .4 and .6
        if (r.width < r.height){
          temp = r.getSubRectangles(0, percentage);
        } else {
          temp = r.getSubRectangles(1, percentage);
        }
        splits.add(temp[0]);
        splits.add(temp[1]);
      } else {
        if (r.height > 2 && random.nextBoolean()){
          rectangles.add(r.splitOffHallway(0));
        }
        if (r.width > 2 && random.nextBoolean()){
          rectangles.add(r.splitOffHallway(1));
        }
        rectangles.add(r);
      }
      splits.remove(r);
    }
    
    combineAdjacentHallways(rectangles);
    
    for (Rectangle r: rectangles){
      r.region = region;
//      System.out.println(r);
      for (int x = r.x; x < r.x + r.width; x++){
        for (int y = r.y; y < r.y + r.height; y++){
          basicMap[y][x] = r;
        }
      }
    }
    
  //make position 0,0 is a hallway
    if (region == 1){
      Rectangle r = basicMap[0][0];
      if (r.isRoom){
        int orientation = 0;
        if (random.nextBoolean()){
          orientation = 1;
        }
        Rectangle hallway = r.splitOffHallway(orientation);
        hallway.region = region;
        for (int i = 0; i < hallway.height; i++){
          for (int j = 0; j < hallway.width; j++){
            basicMap[hallway.y + i][hallway.x + j] = hallway;
          }
        }
        rectangles.add(hallway);
        if (r.width == 1 || r.height == 1){
          r.isRoom = false;
        }
      }
    }
    
    return rectangles;
  }
  
  public static void combineAdjacentHallways(ArrayList<Rectangle> region){
    ArrayList<Rectangle> removals = new ArrayList<>();
    
    for (Rectangle r: region){
      if (!r.isRoom && !removals.contains(r)){
        for (Rectangle r2: region){
          if (!r2.isRoom && !r.equals(r2) && !removals.contains(r2)){
            boolean connection = false;
            
            //check for adjacency  and orientation
            if ((r.x == r2.x) && (r.x + r.width == r2.x + r2.width)){
              if ((r.y + r.height == r2.y) || (r2.y + r2.height == r.y)){
                if (r.y > r2.y)
                {
                  r.y = r2.y;
                }
                r.height += r2.height;
                connection = true;
              }
            }
            if ((r.y == r2.y) && (r.y + r.height == r2.y + r2.height)){
              if ((r.x + r.width == r2.x) || (r2.x + r2.width == r.x)){
                if (r.x > r2.x)
                {
                  r.x = r2.x;
                }
                r.width += r2.width;
                connection = true;
              }
            }
            
            //combine hallways
            if (connection){
              removals.add(r2);
              break;
            }
          }
        }
      }
    }
    
    for (Rectangle r: removals){
      region.remove(r);
      r = null;
    }
    
    removals.clear();
  }
  
  /**
   * Constructs the neighbors for all the rectangles given based on their
   * Location on the given 2d array of rectangles<br><br>
   * 
   * Rectangles that are touching each other orthogonally get mapped to each
   * other as neighbors
   * 
   * @param basicMap - a 2d array of rectangles representing a relation between the coordinate
   *                   and which rectangle it represents
   * @param regions - a collection of rectangle objects
   */
  private static void constructNeighbors(Rectangle[][] basicMap, ArrayList<ArrayList<Rectangle>> regions){
    for (int index = 1; index < regions.size(); index++)
    {
      for (Rectangle rect : regions.get(index))
      {
        Rectangle neighbor;
        for (int i = 0; i < rect.width; i++)
        {
          //up
          if (rect.y - 1 >= 0)
          {
            neighbor = basicMap[rect.y - 1][rect.x + i];
            if (!rect.neighbors.contains(neighbor))
            {
              rect.neighbors.add(neighbor);
            }
          }
          //down
          if (rect.y + rect.height < basicMap.length)
          {
            neighbor = basicMap[rect.y + rect.height][rect.x + i];
            if (!rect.neighbors.contains(neighbor))
            {
              rect.neighbors.add(neighbor);
            }
          }
        }
        for (int i = 0; i < rect.height; i++)
        {
          //left
          if (rect.x - 1 >= 0)
          {
            neighbor = basicMap[rect.y + i][rect.x - 1];
            if (!rect.neighbors.contains(neighbor))
            {
              rect.neighbors.add(neighbor);
            }
          }
          //right
          if (rect.x + rect.width < basicMap[0].length)
          {
            neighbor = basicMap[rect.y + i][rect.x + rect.width];
            if (!rect.neighbors.contains(neighbor))
            {
              rect.neighbors.add(neighbor);
            }
          }
        }
      } 
    }
  }
  
  
  /**
   * generates all of the internal region paths
   * 
   * @param regions
   */
  private static void generatePaths(ArrayList<ArrayList<Rectangle>> regions, int difficulty){
    Random random = new Random();
    
    ArrayList<Rectangle> frontier = new ArrayList<>();
    ArrayList<Rectangle> visited = new ArrayList<>();
    ArrayList<Rectangle> border = new ArrayList<>();
    ArrayList<Rectangle> region;
    
    for (int i = 1; i < 5; i++){
      visited.clear();
      border.clear();
      region = regions.get(i);
      
      int index = random.nextInt(region.size());
      Rectangle seed = region.get(index);
      visited.add(seed);
      
      for (Rectangle r: seed.neighbors){
        if (r.region == i){ //i being the current region 1-4
          frontier.add(r);
          seed.paths.add(r);
          r.paths.add(seed);
        }
        if ((r.region == i+1) && !border.contains(seed)){
          border.add(seed);
        }
      }
      
      //connect tree starting from seed
      while (!frontier.isEmpty()){
        Rectangle current = frontier.get(0);
        
        for (Rectangle r : current.neighbors){
          if (!visited.contains(r) && !frontier.contains(r) && (r.region == i)){
            frontier.add(r);
            current.paths.add(r);
            r.paths.add(current);
          }
          if ((r.region == i+1) && !border.contains(current)){
            border.add(current);
          }
        }
        
        visited.add(current);
        frontier.remove(current);
      }
      
      
      //connect dead-end hallways 
      for (Rectangle current: regions.get(i)){
        if (!current.isRoom && current.paths.size() == 1){
          for (Rectangle r: current.neighbors){
            if ((r.region == i) && !current.paths.contains(r)){
              current.paths.add(r);
              r.paths.add(current);
              break;
            }
          }
        }
      }
      
      //add external-region connections
      if (i < 4){
        index = random.nextInt(border.size());
        Rectangle current = border.get(index);
        
        for (Rectangle r: current.neighbors){
          if (r.region == i+1){
            if (DEBUG){
              System.out.println("found connection");
            }
            current.paths.add(r);
            r.paths.add(current);
            break;
          }
        }
      }
      
    }
  }
  
  /**
   * this method resizes the rectangles from the cut-down size used for
   * calculation, back to the original board size<br><br>
   * 
   */
  private static void resizeRectangles(ArrayList<ArrayList<Rectangle>> regions){
    for (int i = 0; i < regions.size(); i++){
      ArrayList<Rectangle> region = regions.get(i);
      for (Rectangle r: region){
        r.x *= 4;
        r.y *= 4;
        r.width *= 4;
        r.height *= 4;
      }
    }
  }
  
  private static void fillMap(Tile[][] map, ArrayList<ArrayList<Rectangle>> regions, int difficulty){
    //left border wall
    for (int i = 0; i < map.length; i++){
      map[i][0] = new Tile(0,i,0, false);
    }
    
    //top border wall
    for (int i = 0; i < map[0].length; i++){
      map[0][i] = new Tile(0,0,1, false);
    }
    
    for (int i = 1; i < regions.size(); i++){
      for (Rectangle r: regions.get(i)){
        for (int x = r.x; x < (r.x + r.width); x++){
          for (int y = r.y; y < (r.y + r.height); y++){
            if ((x == r.x + r.width - 1) || (y == r.y + r.height - 1)){
              map[y+1][x+1] = new Tile(0, y+1,x+1, !r.isRoom); //this actually reflects the map about the x=y line
            } else {
              map[y+1][x+1] = new Tile(r.region,y+1,x+1, !r.isRoom);
              if (x%2 == 1 && y%2 == 1 && r.isRoom){
                double chance = (difficulty + 2)/7d;
                if (Math.random() < chance){
                  map[y+1][x+1] = new Tile(r.region + 5,y+1,x+1, !r.isRoom); //create decorative obstacles
                }
              }
            }
          }
        }
        
        for (Rectangle path : r.paths){
          if (path.x == r.x + r.width){ //on right of r
            int y = 0;
            int end1 = r.y + r.height - 1;
            int end2 = path.y + path.height - 1;
            int height1 = 0;
            int height2 = 0;
            
            if (r.y > path.y){
              height1 = end1 - r.y;
              height2 = end2 - r.y;
              if (height1 < height2){
                y = r.y + height1/2;
              } else {
                y = r.y + height2/2;
              }
            } else {
              height1 = end1 - path.y;
              height2 = end2 - path.y;
              if (height1 < height2){
                y = path.y + height1/2;
              } else {
                y = path.y + height2/2;
              }
            }
            
            map[y][path.x] = new Tile(r.region, y,path.x, !r.isRoom);
            map[y+1][path.x] = new Tile(r.region, y+1,path.x, !r.isRoom);
          }
          if (path.y == r.y + r.height){//on bottom of r
            int x = 0;
            int end1 = r.x + r.width - 1;
            int end2 = path.x + path.width - 1;
            int width1 = 0;
            int width2 = 0;
            if (r.x > path.x){
              width1 = end1 - r.x;
              width2 = end2 - r.x;
              if (width1 < width2){
                x = r.x + width1/2;
              } else {
                x = r.x + width2/2;
              }
            } else {
              width1 = end1 - path.x;
              width2 = end2 - path.x;
              if (width1 < width2){
                x = path.x + width1/2;
              } else {
                x = path.x + width2/2;
              }
            }
            
            map[path.y][x] = new Tile(r.region,path.y,x, !r.isRoom);
            map[path.y][x+1] = new Tile(r.region,path.y,x+1, !r.isRoom);
          }
        }
      }
    }
    
    //exit calculation:
    ArrayList<Rectangle> upExitCandidates = new ArrayList<>();
    ArrayList<Rectangle> leftExitCandidates = new ArrayList<>();
    ArrayList<Rectangle> rightExitCandidates = new ArrayList<>();
    ArrayList<Rectangle> downExitCandidates = new ArrayList<>();
    
    for (Rectangle r: regions.get(4)){
      if (r.y - 1 < 0){
        upExitCandidates.add(r); //up
      }
      if (r.x - 1 < 0){
        leftExitCandidates.add(r); //left
      }
      if (r.y + r.height == map.length - 1){ //down
        downExitCandidates.add(r);
      }
      if (r.x + r.width == map[0].length -1){ //right
        rightExitCandidates.add(r);
      }
    } 
    
    Random random = new Random();
    Rectangle exit;
    boolean first = random.nextBoolean();
    
    if (downExitCandidates.isEmpty() && rightExitCandidates.isEmpty())
    { // up and left
      System.out.println("up left");
      System.out.println(upExitCandidates.size());
      System.out.println(leftExitCandidates.size());
      if (first)
      {
        exit = upExitCandidates.get(random.nextInt(upExitCandidates.size()));
        int row = exit.y;
        int col = exit.x + exit.width / 2;
        map[row][col] = new Tile(5, col, row, false);
        map[row][col + 1] = new Tile(5, col + 1, row, false);
      }
      else
      {
        exit = leftExitCandidates.get(random.nextInt(leftExitCandidates.size()));
        int row = exit.y + exit.height / 2;
        int col = exit.x;
        map[row][col] = new Tile(5, col, row, false);
        map[row + 1][col] = new Tile(5, col, row + 1, false);
      }
    }
    if (upExitCandidates.isEmpty() && rightExitCandidates.isEmpty())
    { // down and left
      System.out.println("down left");
      if (first)
      {
        exit = downExitCandidates.get(random.nextInt(downExitCandidates.size()));
        int row = exit.y + exit.height - 1;
        int col = exit.x + exit.width / 2;
        map[row+1][col] = new Tile(5, col, row+1, false);
        map[row+1][col + 1] = new Tile(5, col + 1, row+1, false);
      }
      else
      {
        exit = leftExitCandidates.get(random.nextInt(leftExitCandidates.size()));
        int row = exit.y + exit.height / 2;
        int col = exit.x;
        map[row][col] = new Tile(5, col, row, false);
        map[row + 1][col] = new Tile(5, col, row+1, false);
      }
    }
    if (downExitCandidates.isEmpty() && leftExitCandidates.isEmpty())
    { // up and right
      System.out.println("up right");
      if (first)
      {
        exit = upExitCandidates.get(random.nextInt(upExitCandidates.size()));
        int row = exit.y;
        int col = exit.x + exit.width / 2;
        map[row][col] = new Tile(5, col, row, false);
        map[row][col + 1] = new Tile(5, col+1, row, false);
      }
      else
      {
        exit = rightExitCandidates.get(random.nextInt(rightExitCandidates.size()));
        int row = exit.y + exit.height / 2;
        int col = exit.x + exit.width - 1;
        map[row][col+1] = new Tile(5, col+1, row, false);
        map[row + 1][col+1] = new Tile(5, col+1, row+1, false);
      }
    }
    if (upExitCandidates.isEmpty() && leftExitCandidates.isEmpty())
    { // down and right
      System.out.println("down right");
      if (first)
      {
        exit = downExitCandidates.get(random.nextInt(downExitCandidates.size()));
        int row = exit.y + exit.height - 1;
        int col = exit.x + exit.width / 2;
        map[row][col] = new Tile(5, col, row, false);
        map[row][col + 1] = new Tile(5, col+1, row, false);
      }
      else
      {
        exit = rightExitCandidates.get(random.nextInt(rightExitCandidates.size()));
        int row = exit.y + exit.height / 2;
        int col = exit.x + exit.width - 1;
        map[row][col+1] = new Tile(5, col+1, row, false);
        map[row + 1][col+1] = new Tile(5, col+1, row+1, false);
      }
    }
  }
  
  /**
   * This method prints an array of rectangles to show the division of regions
   */
  private static void printArray(Rectangle[][] level){
    for (int i = 0; i < level.length; i++){
      for (int j = 0; j < level[0].length; j++){
        System.out.print(level[i][j].region);
      }
      System.out.print("\n");
    }
    System.out.println("--------------------------");
  }
  
  private static void printCollsionCases(ArrayList<ArrayList<Rectangle>> regions){
    ArrayList<Rectangle> rectangles = new ArrayList<>();
    ArrayList<Rectangle> collisions = new ArrayList<>();
    
    for (int i = 1; i < regions.size(); i++){
      for (Rectangle r: regions.get(i)){
        rectangles.add(r);
      }
    }
    
    for (Rectangle r: rectangles){
      for (Rectangle r2: r.neighbors){
        flag:
        
        if (!collisions.contains(r)){
          for (int i = r.x; i < r.x + r.width; i++){
            for (int j = r.y; j < r.y + r.height; j++){
              if (r2.containsPoint(i, j)){
                collisions.add(r);
                collisions.add(r2);
                break flag;
              }
            }
          }
        }
        
      }
    }
    
    for (int i = 0; i < collisions.size(); i++){
      System.out.println(collisions.get(i));
      if (i%2 == 1){
        System.out.println("---------------------");
      }
    }
  }
  
  /**
   * @author Ben
   * 
   * the Recctangle class is used to represent the bounds of a given rectangular area
   * in Cartesian space
   *
   */
  public static class Rectangle{
    public int x;
    public int y;
    public int width;
    public int height;
    public int region = 0;
    public boolean isRoom = true;
    
    public ArrayList<Rectangle> neighbors = new ArrayList<>(); //represents adjacency
    public ArrayList<Rectangle> paths = new ArrayList<>(); //represents openings
    
    /**
     * defualt constructor
     */
    public Rectangle(){
      x = 0;
      y = 0;
      width = 0;
      height = 0;
    }
    
    /**
     * constructor that sets the bounds of the rectangular region
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public Rectangle(int x, int y, int width, int height){
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
    }
    
    /**
     * returns the dimensions of this rectangle as a string
     */
    public String toString(){
      return "region " + region + ": " + x + ", " + y + ", " + width + ", " + height; 
    }
    
    /**
     * compares this rectangle to another based on the dimensions.
     * two rectangles are equal if all of their dimensions and Cartesian
     * positions are equal
     * 
     * @param r - the rectangle to be compared
     * @return true if the rectangle is the same dimensions
     */
    public boolean equals(Rectangle r){
      return ((r.x == x) && (r.y == y) && (r.width == width) && (r.height == height));
    }
    
    /**
     * checks to see if a point is contained within the bounds of this
     * rectangle
     * 
     * @param x
     * @param y
     * @return - true if the point is contained within this rectangle
     */
    public boolean containsPoint(int x, int y){
      return (x >= this.x && x < this.x + width && y >= this.y && y < this.y + height);
    }
    
    /**Returns an array of sub-rectangles that contains the
     * two sub-rectangles given a split of a certain percentage and direction
     * where direction is either horizontal(0) or vertical(1) and percentage is a
     * double value between 0 and 1
     * 
     * @param orientation - orientation of split: 0 for horizontal, 1 for vertical
     * @param percentage = the percentage of the split from 0 to 1
     * @return a rectangle array containing the sub-rectangles
     */
    public Rectangle[] getSubRectangles(int orientation, double percentage){
      Rectangle rectangle1;
      Rectangle rectangle2;
      Rectangle[] subRects= new Rectangle[2];
      
      if (orientation == 0){ //horizontal
        rectangle1 = new Rectangle(x, y, width, (int)Math.round((double)height*percentage));
        rectangle2 = new Rectangle(x, y + rectangle1.height, width, height - rectangle1.height);
        subRects[0] = rectangle1;
        subRects[1] = rectangle2;
        return  subRects;
      }
      
      if (orientation == 1){ //vertical
        rectangle1 = new Rectangle(x, y, (int)Math.round((double)width*percentage), height);
        rectangle2 = new Rectangle(x + rectangle1.width, y, width - rectangle1.width, height);
        subRects[0] = rectangle1;
        subRects[1] = rectangle2;
        return  subRects;
      }
      
      return null;
    }
    
    /**
     * This method takes in a orientation argument (0 for a bottom hallway, 1 for a left side hallway)
     * and return a hallway (rectangle) that has been "split" or "budded" off of the current rectangle<br><br>
     * 
     * the dimensions of the current rectangle are then appended to adjust for splitting off the hallway<br><br>
     * 
     * @param orientation - the orientation of the hallway
     * @return - a rectangle object representing a hallway
     */
    public Rectangle splitOffHallway(int orientation){
      Rectangle hallway;
      
      if (orientation == 0){ //horizontal hallway
        int yPos= 0;
        if (y == 0){
          hallway = new Rectangle(x, yPos, width, 1);
          hallway.isRoom = false;
          height -= 1;
          y += 1;
          return hallway;
        } else{
          yPos = y + height - 1;
          hallway = new Rectangle(x, yPos, width, 1);
          hallway.isRoom = false;
          height -= 1;
          return hallway;
        }
        
      }
      
      if (orientation == 1){//vertical hallway
        int xPos = 0;
        if (x == 0){
          hallway = new Rectangle(xPos, y, 1, height);
          hallway.isRoom = false;
          width -= 1;
          x = 1;
          return hallway;
        } else {
          xPos = x + width - 1;
          hallway = new Rectangle(xPos, y, 1, height);
          hallway.isRoom = false;
          width -= 1;
          return hallway;
        }
        
      }
      
      return null;
    }
  }
}
