
/*
 =======================================================================
 This class creates a procedurally generated level map. The following
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
     c.) Else carve out the final room and stop because you have enough
              rooms.
              
              
 Credit for this algorithm: http://www.polygonpi.com/?p=1191
 =====================================================================
 */

package cs351.entities;

import javafx.application.Application;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import java.util.LinkedList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Random;
import java.util.Queue;

/**
 * Rooms class used by RandomLevelGenerator
 *
 * @author Chris Sanchez
 */
public class Rooms extends Application
{
  static final boolean VERTICAL   = true;
  static final boolean HORIZONTAL = false;

  private Group root = new Group();
  private Queue<Rooms> unConnectingRoomsQueue = new LinkedList<>();
  private Scene scene = new Scene(root, 500, 500, Color.BLACK);
  private int numberOfExistingHallways = 0;
  private int splitRotation = 1;
  private int randomNumber2;
  private int randomNumber;
  private int totalHeight;
  private int totalWidth;
  private int xStartPt;
  private int yStartPt;
  private int randInt;
  private int height;
  private int width;
  private int rSize;
  
  public Rooms(){}
  
  public Rooms (int x, int y, int width, int height)
  {
    this.height    = height;
    this.width     = width;
    this.xStartPt  = x;
    this.yStartPt  = y;
    rSize++;
  }
  

  /*
   ===================================================
   This is where the action begins. A board perimeter
   is created, Blocks or "chunks" where the rooms 
   will exist are formed. The halls are connected, 
   and finally the rooms get connected.
   ===================================================
   */
  public void initializeBoard(Stage stage)
  {
    totalWidth   = 300;  
    totalHeight  = 200;
    xStartPt     = 10;
    yStartPt     = 10;
    
    unConnectingRoomsQueue.add( new Rooms( xStartPt, yStartPt, totalWidth, totalHeight) );
    
    Rooms firstInQueue = unConnectingRoomsQueue.remove();
    
    //initial division of a single room
    divideAreaOne(firstInQueue);
    
    //change split rotation
    splitRotation++;
    
    carveBlockArea();

    System.out.println(" size of queue " + rSize);
    
    stage.setTitle("Level Map");
    stage.setScene(scene);
    stage.show();
  }
  
  
  /*
   ======================================================================
   This will carve out large sections, or Blocks of the board, that will 
   eventually become rooms. This is similar to binary space partitioning.
   
   For more information read:
   http://www.roguebasin.com/index.php?title=Basic_BSP_Dungeon_generation
   ======================================================================
   */
  void carveBlockArea()
  {

    while(!unConnectingRoomsQueue.isEmpty())
    {
      //remove the first two "Areas" from the list
      Rooms firstInQueue  = unConnectingRoomsQueue.remove();      
      Rooms secondInQueue = unConnectingRoomsQueue.remove();
      
      divideAreas(firstInQueue, secondInQueue);
      
      //TODO change value below to determine how many rooms get created
      if( unConnectingRoomsQueue.size() > 13 )
      {
        break;
      }
      
      splitRotation++;
    }
    connectRooms();
  }
  
  
  /*
  ===================================================
  Doorways are created on the horizontal and vertical
  edges of each room.
  =================================================
  */
  void connectRooms()
  {
    while( !unConnectingRoomsQueue.isEmpty() )
    {
      Rooms remainingRooms = unConnectingRoomsQueue.remove();
      
      for ( int x = remainingRooms.xStartPt; x <= remainingRooms.width; x++)
      {
        for ( int y = remainingRooms.yStartPt; y <= remainingRooms.height; y++)
        {
          if(   x == remainingRooms.width 
             && y == remainingRooms.height / 2
             && x != totalWidth)
          {
            Rectangle r7 = new Rectangle(x, y, 1, 1);
            r7.setFill(Color.ORANGE);
            root.getChildren().add(r7);
            
            Rectangle r8 = new Rectangle(x , y + 1, 1, 1);
            r8.setFill(Color.ORANGE);
            root.getChildren().add(r8);
          }
          
          if(    x == remainingRooms.width / 2 
              && y == remainingRooms.height
              && y != totalHeight)
           {
             Rectangle r7 = new Rectangle(x, y, 1, 1);
             r7.setFill(Color.ORANGE);
             root.getChildren().add(r7);
             
             Rectangle r8 = new Rectangle(x + 1 , y , 1, 1);
             r8.setFill(Color.ORANGE);
             root.getChildren().add(r8);
           }
        }
      }
    }
  }
  
  /************************ START HELPER METHODS ***************************/
 
  /*
   ===========================================
   Alternate between vertical and horizontal
   division of rooms.
   ===========================================
   */
  public boolean splitDirection_IsVertical()
  {
    if(splitRotation % 2 == 0) return false;
    return true;
  }
  
  
  /*
   =================================================
   Divide the initial room you start with. There is
   only one area to slice, so there is no need to 
   get the first and second objects int the queue. 
   After the two chunks are split in half, the two
   pieces are put in the queue and the whole process
   will begin.
   =================================================
   */
  public void divideAreaOne( Rooms firstInQueue )
  {
      randomNumber = getRandomNumber(firstInQueue, VERTICAL);
      verticalDivide(firstInQueue, randomNumber);
  }
  
  
  /*
  =================================================
  If more hallways are needed, carve more hallways.
  Otherwise split the remaining rooms.
  =================================================
  */
  public void divideAreas( Rooms firstInQueue, Rooms secondInQueue )
  {
    if ( splitDirection_IsVertical() && roomIsLargeEnough(firstInQueue, VERTICAL) 
                                     && roomIsLargeEnough(secondInQueue, VERTICAL) )
    {
      randomNumber  = getRandomNumber(firstInQueue, VERTICAL);
      randomNumber2 = getRandomNumber(secondInQueue, VERTICAL);
      verticalDivide( firstInQueue, randomNumber );
      verticalDivide( secondInQueue, randomNumber2 );
    }
    else if ( roomIsLargeEnough(firstInQueue, HORIZONTAL) && roomIsLargeEnough(secondInQueue, HORIZONTAL) )
    {
      randomNumber  = getRandomNumber(firstInQueue, HORIZONTAL);
      randomNumber2 = getRandomNumber(secondInQueue, HORIZONTAL);
      horizontalDivide( firstInQueue, randomNumber );
      horizontalDivide( secondInQueue, randomNumber2 );
    }
  }
  
  
  /*
   =======================================
   Vertical hallway is created by dividing
   room in half.
   =======================================
   */
  public void verticalDivide(Rooms firstInQueue, int randomNumber)
  {
    for (int x = firstInQueue.xStartPt; x < firstInQueue.width; x++)
    {
      for (int y = firstInQueue.yStartPt; y < firstInQueue.height; y++)
      {
        /* draw area of square */
        if (   ( x == firstInQueue.xStartPt  )        
            || ( y == firstInQueue.yStartPt  )       
            || ( x == firstInQueue.width -1  )    
            || ( y == firstInQueue.height - 1) )
        {
          Rectangle r = new Rectangle(x, y, 1, 1);
          r.setFill(Color.BLUE);
          root.getChildren().add(r);

        }

        if( x == randomNumber && ( y < firstInQueue.height - 1 ) )
        {
          if( numberOfExistingHallways < 6 )
          {
            Rectangle hall = new Rectangle(x , y + 1, 1, 1);
            hall.setFill(Color.RED);
            root.getChildren().add(hall);
            
            Rectangle hall2 = new Rectangle(x + 1, y, 1, 1);
            hall2.setFill(Color.RED);
            root.getChildren().add(hall2);
            
            Rectangle hall6 = new Rectangle(x + 2, y , 1, 1);
            hall6.setFill(Color.RED);
            root.getChildren().add(hall6);
          }
          else 
          {
            Rectangle r3 = new Rectangle(x , y , 1, 1);
            r3.setFill(Color.GREEN);
            root.getChildren().add(r3);
          }
        }
      }
    } /* END TOP LEVEL FOR-LOOP */
    
    /* split Chunks and put them back into queue */
    addToQueue(firstInQueue, randomNumber, VERTICAL);
    
    rSize++;
  }
  
    
  /*
   =========================================
   Horizontal hallway is created by dividing
   Area in half.
   =========================================
   */
  public void horizontalDivide( Rooms firstInQueue, int randomNumber )
  {
    for (int x = firstInQueue.xStartPt; x < firstInQueue.width; x++)
    {
      for (int y = firstInQueue.yStartPt; y < firstInQueue.height; y++)
      {
        if (   ( x == firstInQueue.xStartPt   )       
            || ( y == firstInQueue.yStartPt   )       
            || ( x == firstInQueue.width - 1  )   
            || ( y == firstInQueue.height - 1 ) )
        { 
          Rectangle r4 = new Rectangle(x, y, 1, 1);
          r4.setFill(Color.BLUE);
          root.getChildren().add(r4);
        }
        
        if(y == randomNumber )
        {
          if(numberOfExistingHallways < 6)
          {
            /* there is a weird offset issue that intitializing the first
             * LOCATION_X takes care of. The only way to see it is drawing the
             * pixels by hand
             */
            Rectangle hall3 = new Rectangle(x + 1, y, 1, 1);
            hall3.setFill(Color.RED);
            root.getChildren().add(hall3);
            
            Rectangle hall4 = new Rectangle(x, y + 1, 1, 1);
            hall4.setFill(Color.RED);
            root.getChildren().add(hall4);
            
            Rectangle hall5 = new Rectangle(x, y + 2, 1, 1);
            hall5.setFill(Color.RED);
            root.getChildren().add(hall5);
          }
          else
          {
            Rectangle r6 = new Rectangle(x + 1 , y , 1, 1);
            r6.setFill(Color.GREEN);
            root.getChildren().add(r6);
          }
        }
      }
    } /* END TOP LEVEL FOR-LOOP */
    
    /* splits chunks into two areas and puts them back into queue */
    addToQueue(firstInQueue, randomNumber, HORIZONTAL);

    rSize++;
  }
  
 
  void addToQueue(Rooms firstInQueue, int randomNumber, boolean splitIsVertical)
  {
    int xStartPt = firstInQueue.xStartPt;
    int yStartPt = firstInQueue.yStartPt;
    int width    = firstInQueue.width;
    int height   = firstInQueue.height;
    
    //HALLWAY VERTICAL
    if( ( numberOfExistingHallways < 6 ) && splitIsVertical )
    {
      unConnectingRoomsQueue.add( new Rooms( xStartPt, yStartPt, randomNumber - 1, height ) );
      unConnectingRoomsQueue.add( new Rooms( randomNumber + 3, yStartPt, width, height ) );
    
      numberOfExistingHallways++;
    }
    //NOT HALLWAY - VERTICAL
    else if ( numberOfExistingHallways >= 6 && ( splitIsVertical ) )
    {
      unConnectingRoomsQueue.add( new Rooms( xStartPt, yStartPt, randomNumber, height ) );
      unConnectingRoomsQueue.add( new Rooms( randomNumber , yStartPt, width, height ) );
      
    }
    //HALLWAY HORIZONTAL
    else if ( ( numberOfExistingHallways < 6 ) && (!splitIsVertical) )
    {
      unConnectingRoomsQueue.add( new Rooms( xStartPt, yStartPt, width, randomNumber - 1 ) );
      unConnectingRoomsQueue.add( new Rooms( xStartPt,randomNumber + 3 , width, height ) );
      
      numberOfExistingHallways++;
    }
    //NOT HALLWAY - HORIZONTAL
    else if ( numberOfExistingHallways >= 6 && ( !splitIsVertical ) )
    {
      unConnectingRoomsQueue.add( new Rooms( xStartPt, yStartPt, width, randomNumber ) );
      unConnectingRoomsQueue.add( new Rooms( xStartPt,randomNumber  , width, height ) );
    }
  }
  
  
  /*
   ===================================================
   Returns a random number to divide the LOCATION_X axis or the
   LOCATION_Y axis. The upper and lower bound variables
   restrict the line division between 1/3 and 2/3. the
   reason for this is so the hallways and borders 
   don't end up too close to another wall.
   ===================================================
   */
  public int getRandomNumber(Rooms firstInQueue, boolean splitIsVertical)
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
       randInt = rand.nextInt( ( upperBound_X - lowerBound_X ) ) + lowerBound_X;
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
   divide it horizontally.
   ===========================================================
   */
  public boolean roomIsLargeEnough( Rooms firstInQueue, boolean checkWidth)
  {
    // check the width since we want to make a vertical line
    if( checkWidth )
    {
      if(    ( firstInQueue.width  -  firstInQueue.xStartPt ) < 6 
          && ( firstInQueue.height -  firstInQueue.yStartPt ) < 6 )
        return false;
    }
    //TODO fix this later on
    //TODO MAY NOT WORK 
    return true;
  }
  
  /*********************** END HELPER METHODS **************************/ 
  
 
  @Override
  public void start(Stage stage)
  {
    initializeBoard(stage);
  }
  
  
  public static void main(String[] args)
  {
    launch(args);
  }
}