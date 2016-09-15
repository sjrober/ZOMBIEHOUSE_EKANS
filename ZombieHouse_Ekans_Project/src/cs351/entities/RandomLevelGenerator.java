
package cs351.entities;
import javafx.application.Application;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import java.util.LinkedList;
import javafx.scene.Group;
import java.util.HashMap;
import java.util.Random;


import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Queue;

/**
 * 
 * @author Chris Sanchez
 *
 * This class creates a procedurally generated level map. The following
   algorithm explains the process:
 
   1.) Fill entire map with a rectangular Wall, initialize a queue with 
       this one large area.
   2.) While the queue still has "Areas" waiting in line, take the front
       area, (or head) and divide it
       a.) If (we don't have 6 hallways created yet) AND 
              (The area is large enough)
             Then carve out a straight line (hallway), rotating 90 degrees
                  each time. Add both blocks (either side first) back to
                  the queue.
       b.) Else-If the area is still too big
             Then cut the area in half (pick a random spot) and add both
                  sides back into the queue.
           Else carve out the final room and stop because you have enough
                rooms.
              
   Credit for this algorithm: http://www.polygonpi.com/?p=1191

 @author Chris Sanchez
 */

public class RandomLevelGenerator extends Application
{
  private Group root = new Group();
  private Scene scene = new Scene(root, 200, 200, Color.BLACK);
  private Queue<RandomLevelGenerator> divideRoomsQueue = new LinkedList<>();
  private Queue<RandomLevelGenerator> unReachableRooms = new LinkedList<>();

  private int numberOfExistingHallways = 0;
  static final boolean VERTICAL        = true;
  static final boolean HORIZONTAL      = false;
  private boolean spawnBoolean         = true;
  private boolean graphicDebug         = false;
  private boolean ROTATION             = true;
  private int BOARD_WIDTH              = 50;
  private int BOARD_HEIGHT             = 50;
  private int splitRotation            = 1;
  private int randomNumber2;
  private int randomNumber;
  private int totalHeight;
  private int totalWidth;
  private int xStartPt;
  private int yStartPt;
  private int numRooms;
  private int randInt;
  private int height;
  private int width;
  Stage stage;
  private int n = 1;
  
  
  HashMap<Integer, Integer> areaOneCenter = new HashMap<>();
  private int[][] boardArray = new int [BOARD_WIDTH][BOARD_HEIGHT];
  private static int XSpawnPoint;
  private static int YSpawnPoint;
  

  public RandomLevelGenerator(){}
  
  public RandomLevelGenerator(int x, int y, int width, int height)
  {
    this.height    = height;
    this.width     = width;
    this.xStartPt  = x;
    this.yStartPt  = y;
  }
  

  /**
   * This is where the action begins. A board perimeter
     is created, Blocks or "chunks" where the rooms 
     will exist are formed. The halls are connected, 
     and finally the rooms get connected.
   */
  public void initializeBoard()
  {
    xStartPt     = 0;
    yStartPt     = 0;
    totalWidth   = 50;  
    totalHeight  = 50;
    
    
    //System.out.println("initialize Array");
    for (int x = 0; x < 50; x++)
    {
      for (int y = 0; y < 50; y++)
      {
        boardArray[x][y] = 0;
      }
    }
    
    divideRoomsQueue.add( new RandomLevelGenerator( xStartPt, yStartPt, totalWidth, totalHeight) );
    
    RandomLevelGenerator firstInQueue = divideRoomsQueue.remove();
    
    //initial division of a single room
    divideAreaOne(firstInQueue);
    
    //change split rotation
    splitRotation++;
    
    carveBlockArea();

    Stage stage = new Stage();
    
    stage.setTitle("Level Map");
    stage.setScene(scene);
   // stage.show();
  }
  

  /**
   * This will carve out large sections, or Blocks of the board, that will 
     eventually become rooms. This is similar to binary space partitioning.
   
     For more information read:
     http://www.roguebasin.com/index.php?title=Basic_BSP_Dungeon_generation
   */
  void carveBlockArea()
  {
    
    while(!divideRoomsQueue.isEmpty())
    {
      
      //remove the first two from the list
      RandomLevelGenerator firstInQueue  = divideRoomsQueue.remove();
      RandomLevelGenerator secondInQueue = divideRoomsQueue.remove();

      
      divideAreas(firstInQueue, secondInQueue);
      
      // This is to split up the map into 4 different areas with different tile textures
      if( divideRoomsQueue.size() == 4) printQuadrant(divideRoomsQueue);
      
      if( divideRoomsQueue.size() > 14)
      {
        break;
      }
    }
    
    connectRooms();
    
    createExit();
    
    printArray();
  }
  

  /**
   * This function separates the entire board into 4 quadrants
   * so that the floor tiles can be different in the divided
   * areas
   * @param divideRoomsQueue
   */
  void printQuadrant(Queue<RandomLevelGenerator> divideRoomsQueue)
  {
    int quadRantNumber = 2;

    for (RandomLevelGenerator r : divideRoomsQueue)
    {
      for (int x = r.xStartPt; x <= r.width; x++)
      {
        for (int y = r.yStartPt; y <= r.height; y++)
        {
          if ((x > r.xStartPt) && (y > r.yStartPt ) && (x < r.width - 1) && (y < r.height - 1))
          {
            if(x == r.width-2 && (y == r.height - 2)) quadRantNumber++;
            else boardArray[x][y] = quadRantNumber;
          }
        }
      }
    }
  }
  
  
  /**
   * Makes every Room accessible by making gaps in the 
   * walls where a door should be
   */
  void connectRooms()
  {
    int matchFloorTileTexture = 0;
    
    //check the size of the rooms queue
    while( !unReachableRooms.isEmpty() )
    {
      RandomLevelGenerator remainingRooms = unReachableRooms.remove();
      
      for ( int x = remainingRooms.xStartPt; x <= remainingRooms.width; x++)
      {
        for ( int y = remainingRooms.yStartPt; y <= remainingRooms.height; y++)
        {
          //door on right side of area
          if(   x == remainingRooms.width 
             && y == remainingRooms.height / 2
             && x != totalWidth)
          {
            matchFloorTileTexture = boardArray[x - 2][y];
            boardArray[x - 1][y]  = matchFloorTileTexture;
            boardArray[x-1][y + 1] = matchFloorTileTexture;
            
            if (graphicDebug)
            {
              Rectangle r = new Rectangle(x - 1, y, 3, 3);
              r.setFill(Color.RED);
              root.getChildren().add(r);
            }
          }
          
          // door on left side of area, could be an exit
          if(    x == remainingRooms.xStartPt
              && y == remainingRooms.height / 2
              && x != xStartPt )
           {
             matchFloorTileTexture = boardArray[x + 1][y];
             boardArray[x][y]     = matchFloorTileTexture;
             boardArray[x][y + 1] = matchFloorTileTexture;
             
            if (graphicDebug)
            {
              Rectangle r = new Rectangle(x, y + 1, 3, 3);
              r.setFill(Color.GREEN);
              root.getChildren().add(r);
            }
          }
          
          //door on top of area
          if(    x == remainingRooms.width / 2 
              && y == remainingRooms.yStartPt 
              && y != yStartPt )
           {
             matchFloorTileTexture = boardArray[x][y + 1];
             boardArray[x][y]     = matchFloorTileTexture;
             boardArray[x + 1][y] = matchFloorTileTexture;
             
            if (graphicDebug)
            {
              Rectangle r = new Rectangle(x + 1, y, 3, 3);
              r.setFill(Color.PURPLE);
              root.getChildren().add(r);
            }
          }
          
          //door on bottom of area
          if(   x == remainingRooms.width / 2 
              && y == remainingRooms.height 
              && y != totalHeight )
           {
             matchFloorTileTexture = boardArray[x][y - 1];
             boardArray[x][y - 1]     = matchFloorTileTexture;
             boardArray[x + 1][y - 1] = matchFloorTileTexture;
             
            if (graphicDebug)
            {
              Rectangle r = new Rectangle(x, y - 1, 3, 3);
              r.setFill(Color.ORANGE);
              root.getChildren().add(r);
            }
           }
          
          //door on bottom left of area
           if(   x == remainingRooms.xStartPt
              && y == remainingRooms.height - 5
              && y != totalHeight 
              && x != 0 )
           {
             matchFloorTileTexture = boardArray[x + 1][y];
             boardArray[x ][y]     = matchFloorTileTexture;
             boardArray[x ][y + 1] = matchFloorTileTexture;
             
            if (graphicDebug)
            {
              Rectangle r = new Rectangle(x, y + 1, 3, 3);
              r.setFill(Color.GRAY);
              root.getChildren().add(r);
            }
          }
        }
      }
    }
  }
  
  /************************ START HELPER METHODS ***************************/
  
  /**
   * Alternate between vertical and horizontal
     division of rooms
     
   * @return - true if the split direction needs
   *           to be vertical, false if it needs
   *           to be horizontal
   */
  public boolean splitDirection_IsVertical()
  {
    if(splitRotation % 2 == 0) return false;
    return true;
  }
  
  /**
   * 
   * @return - true if Vertical,
   *           false if Horizontal
   */
  public boolean changeSplitDir()
  {
    if (ROTATION == VERTICAL)
    {
      ROTATION = HORIZONTAL; 
      return HORIZONTAL;
    }
    else 
    {
      ROTATION = VERTICAL;
      return VERTICAL;
    }
  }
  

  /**
   * This divide the initial Area of the level map.
   * Since no other rooms have been placed in the queue,
   * Only one chunk gets divided at first
   * 
   * @param firstInQueue
   */
  public void divideAreaOne( RandomLevelGenerator firstInQueue )
  {
      randomNumber = getRandomNumber(firstInQueue, VERTICAL);
      verticalDivide(firstInQueue, randomNumber);
  }
  
  
  
  /**
   * This will divide the first and second Room object waiting in
   * the queue. The direction of the split alternates with each 
   * pass. The number of rooms that need to be divided grow
   * 2 ^ n times. The first pass is 2 ... then 4 ... then 8 ... etc
   * 
   * @param firstInQueue
   * @param secondInQueue
   */
  public void divideAreas( RandomLevelGenerator firstInQueue, RandomLevelGenerator secondInQueue )
  {

     if (ROTATION && roomIsLargeEnough(firstInQueue, VERTICAL) && roomIsLargeEnough(secondInQueue, VERTICAL))
    {
      randomNumber  = getRandomNumber(firstInQueue, VERTICAL);
      randomNumber2 = getRandomNumber(secondInQueue, VERTICAL);
      verticalDivide( firstInQueue, randomNumber );
      verticalDivide( secondInQueue, randomNumber2 );
    }
    else if ( roomIsLargeEnough(firstInQueue, HORIZONTAL)&& roomIsLargeEnough(secondInQueue, HORIZONTAL))
    {
      randomNumber = getRandomNumber(firstInQueue, HORIZONTAL);
      randomNumber2 = getRandomNumber(secondInQueue, HORIZONTAL);
      horizontalDivide( firstInQueue, randomNumber );
      horizontalDivide( secondInQueue, randomNumber2 );
    }
  }
  

  /**
   * Makes a vertical split down the middle of each chunk of block.
   * First it adds the two haves to the queue. As they are being 
   * added, offset space is created to make hallways
   * 
   * @param firstInQueue
   * @param randomNumber
   */
  public void verticalDivide(RandomLevelGenerator firstInQueue, int randomNumber)
  {

    for (int x = firstInQueue.xStartPt; x < firstInQueue.width; x++)
    {
      for (int y = firstInQueue.yStartPt; y < firstInQueue.height; y++)
      {
   
        if( numberOfExistingHallways < 7)
        {
          /* draw area of square */
          if ((x == firstInQueue.xStartPt) || (y == firstInQueue.yStartPt) || (x == firstInQueue.width - 1)
              || (y == firstInQueue.height - 1))
          {
            Rectangle r = new Rectangle(x, y, 1, 1);
            r.setFill(Color.BLUE);
            root.getChildren().add(r);

            boardArray[x][y] = 1;
          }
        }

        if( x == randomNumber 
            && ( y < firstInQueue.height - 1 ) )
        {
          if( numberOfExistingHallways < 6 )
          {
          }
          else 
          {
            //TURN ON PIXEL
            boardArray[x][y] = 1;
            
            Rectangle r3 = new Rectangle(x , y , 1, 1);
            r3.setFill(Color.GREEN);
            root.getChildren().add(r3);
          }
        }
      }
    } /* END TOP LEVEL FOR-LOOP */
    
    /* split Chunks and put them back into queue */
    addToQueue(firstInQueue, randomNumber, VERTICAL);
  }
  
  
  /**
   * Creates a horizontal split down the middle of each chunk.
   * Offset space is created to make hallways.
   * 
   * @param firstInQueue
   * @param randomNumber
   */
  public void horizontalDivide( RandomLevelGenerator firstInQueue, int randomNumber )
  {
    for (int x = firstInQueue.xStartPt; x < firstInQueue.width; x++)
    {
      for (int y = firstInQueue.yStartPt; y < firstInQueue.height; y++)
      {

        if( numberOfExistingHallways < 7)
        {
          if ((x == firstInQueue.xStartPt) || (y == firstInQueue.yStartPt) || (x == firstInQueue.width - 1)
              || (y == firstInQueue.height - 1))
          {
            Rectangle r4 = new Rectangle(x, y, 1, 1);
            r4.setFill(Color.BLUE);
            root.getChildren().add(r4);
            
            boardArray[x][y] = 1;
          }
        }
        
        if(y == randomNumber )
        {
          if(numberOfExistingHallways < 6)
          {
          }
          else
          {
            //Turn on pixel
            boardArray[x][y] = 1;
            Rectangle r6 = new Rectangle(x + 1 , y , 1, 1);
            r6.setFill(Color.GREEN);
            root.getChildren().add(r6);
          }
        }

      }
    } /* END TOP LEVEL FOR-LOOP */
    
    /* splits chunks into two areas and puts them back into queue */
    addToQueue(firstInQueue, randomNumber, HORIZONTAL);
    
  }
  
  /**
   * One room object is passed as the argument, but this 
   * single chunk gets divided into two pieces, and put
   * back into the end of the queue.
   * 
   * @param firstInQueue -  This is the room object that gets
   *                        poped off the queue
   * @param randomNumber
   * @param splitIsVertical - A boolean that keeps track if the 
   *                          split needs to be horizontal or 
   *                          vertical
   */
  void addToQueue(RandomLevelGenerator firstInQueue, int randomNumber, boolean splitIsVertical)
  {
    int xStartPt = firstInQueue.xStartPt;
    int yStartPt = firstInQueue.yStartPt;
    int width    = firstInQueue.width;
    int height   = firstInQueue.height;
    
    //set spawn location to the Hallway farthest away from the exit
    if ( ( spawnBoolean == true && numberOfExistingHallways == 3) )
    {
      XSpawnPoint = randomNumber;
      YSpawnPoint = yStartPt + 1;
      
      Rectangle sp = new Rectangle(XSpawnPoint, YSpawnPoint, 1, 1);
      sp.setFill(Color.RED);
      root.getChildren().add(sp);
    }
   
    
    //HALLWAY VERTICAL
    if( ( numberOfExistingHallways < 6 ) && splitIsVertical )
    {
      divideRoomsQueue.add( new RandomLevelGenerator( xStartPt, yStartPt, randomNumber - 1, height ) );
      divideRoomsQueue.add( new RandomLevelGenerator( randomNumber + 2, yStartPt, width, height ) );
      

      unReachableRooms.add( new RandomLevelGenerator( xStartPt, yStartPt, randomNumber - 1, height ) );
      unReachableRooms.add( new RandomLevelGenerator( randomNumber + 2, yStartPt, width, height ) );
    
      numberOfExistingHallways++;
    }
    //NOT HALLWAY - VERTICAL
    else if ( numberOfExistingHallways >= 6 && ( splitIsVertical ) )
    {
      divideRoomsQueue.add( new RandomLevelGenerator( xStartPt, yStartPt, randomNumber, height ) );
      divideRoomsQueue.add( new RandomLevelGenerator( randomNumber , yStartPt, width, height ) );
      

      unReachableRooms.add( new RandomLevelGenerator( xStartPt, yStartPt, randomNumber, height ) );
      unReachableRooms.add( new RandomLevelGenerator( randomNumber , yStartPt, width, height ) );
    }
    //HALLWAY HORIZONTAL
    else if ( ( numberOfExistingHallways < 6 ) && (!splitIsVertical) )
    {
      divideRoomsQueue.add( new RandomLevelGenerator( xStartPt, yStartPt, width, randomNumber - 1 ) );
      divideRoomsQueue.add( new RandomLevelGenerator( xStartPt,randomNumber + 2 , width, height ) );
      
      unReachableRooms.add( new RandomLevelGenerator( xStartPt, yStartPt, width, randomNumber - 1 ) );
      unReachableRooms.add( new RandomLevelGenerator( xStartPt,randomNumber + 2 , width, height ) );
      
      numberOfExistingHallways++;
    }
    //NOT HALLWAY - HORIZONTAL
    else if ( numberOfExistingHallways >= 6 && ( !splitIsVertical ) )
    {
      divideRoomsQueue.add( new RandomLevelGenerator( xStartPt, yStartPt, width, randomNumber ) );
      divideRoomsQueue.add( new RandomLevelGenerator( xStartPt,randomNumber  , width, height ) );
      
      unReachableRooms.add( new RandomLevelGenerator( xStartPt, yStartPt, width, randomNumber ) );
      unReachableRooms.add( new RandomLevelGenerator( xStartPt,randomNumber, width, height ) );
    }
    
    
    numRooms = divideRoomsQueue.size();
    if(numRooms == Math.pow(2, n)){
      changeSplitDir();
      n++;
    }
  }
  
  
  /**
   * Finds a random spot either on a horizontal or vertical
   * edge, and uses that as a "pivot" to divide the chunks
   * of Rooms.
   * 
   * The upper and lower bound variables
   * restrict the line division between 1/3 and 2/3. the
   * reason for this is so the hallways and borders 
   * don't end up too close to another wall
   * 
   * @param firstInQueue
   * @param splitIsVertical
   * @return
   */
  public int getRandomNumber(RandomLevelGenerator firstInQueue, boolean splitIsVertical)
  {
    
    int upperBound_X = ( firstInQueue.width * 2 )  / 3;
    int upperBound_Y = ( firstInQueue.height * 2 ) / 3;
    
    int lowerBound_X = ( firstInQueue.width )      / 3;
    int lowerBound_Y = ( firstInQueue.height )     / 3;
    

    Random rand = new Random();
    
    if(splitIsVertical)
    {
      try
      {
       randInt = rand.nextInt(( upperBound_X - lowerBound_X ) ) + lowerBound_X;
      }
      catch(IllegalArgumentException e)
      {
        System.err.println("upperBound_X = "+upperBound_X+" lowerBound_X = "+lowerBound_X+" randInt = "+randInt);
      }
    }
    
    else
    {
      try
      {
        
        randInt = rand.nextInt( upperBound_Y - lowerBound_Y ) + lowerBound_Y;
      }
      catch( IllegalArgumentException e)
      {
        System.err.println("upperBound_Y = "+upperBound_Y+" lowerBound_X = "+lowerBound_X+" randInt = "+randInt);
      }
    }
    
    return randInt;
  }
  
  /*
   ===========================================================
   Checks to see that the minimum size room is not going to be 
   divided. A room that is too narrow, but still has a long 
   length can still be divided. In this case, we could still 
   divide it horizontally
   ===========================================================
   */
  public boolean roomIsLargeEnough( RandomLevelGenerator firstInQueue, boolean checkWidth)
  {
    
    // check the width since we want to make a vertical line
    if( checkWidth )
    {
      if(    ( firstInQueue.width  -  firstInQueue.xStartPt ) < 6 
          && ( firstInQueue.height -  firstInQueue.yStartPt ) < 6 )
        return false;
    }
    return true;
  }
  
 
  /**
   * This will set an exit tile on the farthest lower edge of the 
   * level map
   */
 public void createExit(){

   boardArray[BOARD_WIDTH-8][BOARD_HEIGHT-1]  = 69;
   
   boardArray[BOARD_WIDTH-2][BOARD_HEIGHT-2]  = 6;
   boardArray[BOARD_WIDTH-3][BOARD_HEIGHT-2]  = 6;
   boardArray[BOARD_WIDTH-4][BOARD_HEIGHT-2]  = 6;
   boardArray[BOARD_WIDTH-5][BOARD_HEIGHT-2]  = 6;
   boardArray[BOARD_WIDTH-6][BOARD_HEIGHT-2]  = 6;
   boardArray[BOARD_WIDTH-7][BOARD_HEIGHT-2]  = 6;
 }
 

 /**
  * A utility function that prints the actuall
  * 2d array that holds the wall, tile, exit 
  * values
  */
 public void printArray()
 {
   for (int x = 0; x < BOARD_WIDTH; x++)
   {
     for (int y = 0; y < BOARD_HEIGHT; y++)
     {
       //System.out.print(boardArray[y][x]);
     }
     //System.out.println("\n");
   }//System.out.println("\n");
 }
 
 public int[][] getArray()
 {
   return(boardArray);
 }
 

 
 public static int getXSpawnPoint(){
   return XSpawnPoint;
 }
 public static int getYSpawnPoint(){
   return YSpawnPoint;
 }
  
  /*********************** END HELPER METHODS **************************/ 

 
  @Override
  public void start(Stage stage)
  {
  }
}
