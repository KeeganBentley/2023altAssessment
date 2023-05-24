package yorkClose.game

import com.wbillingsley.amdram.*

/**
  * The messages a player actor can receive in the game
  */
enum Message:
  /** Received every tick */
  case TurnUpdate(you:Player, location:Location, room:Room, visiblePlayers:Set[Player], visibleWeapons:Set[Weapon])
    
  /** Received by all players when a player is murdered */
  case Scream(player:Player)

  /** Received by the victim */
  case YouHaveBeenMurdered

  /** Received by all remaining players if the game is won */
  case Victory

  /** A ghostly message telling you to go to a random room */
  case GoTo(room:Room)

/** Messages to the GameActor from internal code to control the game */
enum GameControl:
  case Initialise(ui:Recipient[GameState])
  case Tick

/**
  * Messages received by the game actor
  */
type GameMessage = GameControl | (Player, Command)


