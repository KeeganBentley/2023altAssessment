package yorkClose

import com.wbillingsley.amdram.* 
import game.* 
import bots.*

import scala.util.Random

/**
  * The game actor is a globally visible actor.
  * 
  * It handles the game state. Every 16ms, it receives a tick message
  * and executes the actions on the tanks (and sends out their new states). In the
  * meantime, it waits for commands from the tanks to apply on the next tick.
  * 
  * Its behaviour is defined by the gameHandler function, starting out empty.
  */
val gameActor = troupe.spawn(unstartedGame)

/**
  * An unstarted game will only respond to an Initialise message
  *
  * @return
  */
def unstartedGame:MessageHandler[GameMessage] = MessageHandler { (msg, context) =>
  msg match 
    case GameControl.Initialise(ui) => 
      // Pick a random murderer
      val killer = Random.shuffle(Player.values).head

      // Randomise the weapon locations
      val weaponLocations = Weapon.values.zip(randomRooms).toMap

      // Set up the players
      val playerLocations = (for p <- Player.values yield p -> randomLocation()).toMap
      val playerActors = (for p <- Player.values yield
        p -> (if p == killer then 
          context.spawn(murderer(p, playerLocations(p))) 
        else 
          context.spawn(player(p, playerLocations(p)))
        )
      ).toMap

      // Create the starting game state
      val state = GameState(playerActors, playerLocations, weaponLocations)

      // Update the user interface
      ui ! state

      // Become a started game
      startedGame(state, ui, Nil)
}

/**
 * A started game has a state, knows the UI to update, and keeps a list of commands players have asked it to perform on the next tick
 */
def startedGame(
  state:GameState, 
  ui:Recipient[GameState],
  queued:List[(Player, Command)]
):MessageHandler[GameMessage] = MessageHandler { (msg, context) =>
  
  msg match

    // Indicates the game is ready to show on the UI. Also sent before the first tick
    case GameControl.Initialise(_) => 
      error("Received an initialise message, but already started")      
      startedGame(state, ui, queued)

    // When we receive a command from a player, we queue it to execute on the tick
    case (player, command) => 
      trace(s"$player $command")
      startedGame(state, ui, (player, command) :: queued)

    // A tick moves the game forward
    case GameControl.Tick =>

      // Rewrite this as a foldLeft
      var s = state
      for (p, command) <- queued.reverse do command match {
        case Command.Move(direction) => 
          s = s.move(p, direction)

        case Command.Murder(victim, weapon) => 
          // Check that the murderer, victim, and weapon are all in the same room
          if s.canMurderHappen(p, victim, weapon) then 
            s = s.murder(victim, weapon)
            s.playerActor(victim) ! Message.YouHaveBeenMurdered
            for remaining <- s.playerLocation.keySet do
              s.playerActor(remaining) ! Message.Scream(victim)

      }
      
      for p <- s.playerLocation.keySet do 
        val room = s.playerRoom(p)
        s.playerActor(p) ! Message.TurnUpdate(p, s.playerLocation(p), room, s.playersInRoom(room) - p, s.weaponsInRoom(room))

      //
      ui ! s
      startedGame(s, ui, Nil)

}

