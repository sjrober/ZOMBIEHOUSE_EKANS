package cs351.core;

import cs351.project1.*;
import cs351.entities.Player;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Main entry point. Does initialization and drives the flow of the game.
 *
 * @author Scott Cooper
 */
public class Game extends Application {


  @FXML private Button playButton;
  @FXML private  AnchorPane ZombieHouseAnchorPane;
  @FXML private StackPane zombieHouseScrollPane;
  @FXML public static Text gameTitle;
  @FXML private Text gameTitle2;
  @FXML public static Text stamina;
  private AnimationTimer timer = new MyTimer();
  private boolean started=true;
  private ZombieHouseRenderer renderer;
  private static Engine engine = new ZombieHouseEngine();



  Stage stage;

  public static Engine getEngine()
  {
    return engine;
  }
  //play button handler - run continually (Until Pause)
  @FXML protected void handlePlay(ActionEvent event)  {
    engine.togglePause(started); // lets the engine know what's going on
    started = !started;
    if (started)
    {
      timer.start();
      playButton.setText("Pause");
    } else
    {
      playButton.setText("Play");
    }

  }


  // clear
  @FXML protected void handleClear(ActionEvent event) {
    // TODO  -- handle this
  }


  // Quit handler
  @FXML protected void handleSubmitButtonAction(ActionEvent event) {
    engine.shutdown();
    System.exit(0);
  }


  private void initEngine(Stage stage)
  {
    World world = new ZombieWorld();
    for (int i = 0; i < 10; i++)
    {
      Level level = new ZombieLevel((i != 0) ? 0.5 : 0, (i != 0) ? 10 : 0, (i != 0) ? 1.0 : 0);
      world.add(level);
    }
    engine.init("resources/engine.settings", stage, world, new ZombieHouseSoundEngine(), renderer);
  }

  private void initGameLoop()
  {
    timer.start();
  }

  @Override
  public void start(Stage stage) throws IOException {
    this.stage = stage;
    Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("ZombieHouse.fxml"));
    stage.setTitle("Zombie House");
    stage.setScene(new Scene(root, 900, 750));
    stage.show();
    boolean done = false;
    stamina = null;
    for (Node node : root.getChildrenUnmodifiable())
    {
      if (node instanceof StackPane)
      {
        StackPane pane = (StackPane)node;
        renderer = new ZombieHouseRenderer(stage, pane, (int)pane.getWidth(), (int)pane.getHeight());
      }      
      if ((node instanceof Text) && (!done))
      {
        stamina = (Text)node;
       }
    }

    // if the above for loop does not find a StackPane, throw an exception
    if (renderer == null) throw new RuntimeException("Could not find a StackPane for the renderer to use");

    initEngine(stage);
    initGameLoop();
  }


  private class MyTimer extends AnimationTimer {

    @Override
    public void handle(long now)
    {
      if (!engine.isEnginePendingShutdown())
      {
        double currStamina = ((Player) (engine.getWorld().getPlayer())).getCurrentStamina();
        currStamina = (int) (1000 * currStamina);
        currStamina /= 1000;

        double currHealth = ((Player) (engine.getWorld().getPlayer())).getCurrentHealth();
        currHealth = (int) (1000 * currHealth);
        currHealth /= 1000;

        if (Game.stamina != null)
        {
          Game.stamina.setText("Stamina: " + currStamina + "  Health:" + currHealth);
        }
      }
      engine.frame();
    }
  }
  

  public static void main(String[] args) {
    launch(args);
  }
}