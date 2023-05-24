package yorkClose.game

import com.wbillingsley.amdram.*
import scala.util.Random

/**
  * The game state is simply the players and their locations. We also keep the Recipient for each player, so we can
  * send them messages.
  *
  * @param players
  */
case class GameState(
    playerActor:Map[Player, Recipient[Message]],
    playerLocation: Map[Player, Location],
    weaponRoom: Map[Weapon, Room]
) {

  /** A derived map of which players are in which rooms */
  lazy val playerRoom:Map[Player, Room] = (for (p, l) <- playerLocation yield p -> house(l))

  /** The number of players in a room */
  def playersInRoom(r:Room):Set[Player] = playerRoom.keySet.filter(playerRoom(_) == r)

  /** The set of weaponse in a room */
  def weaponsInRoom(r:Room):Set[Weapon] = weaponRoom.keySet.filter(weaponRoom(_) == r) 

  /** Checks whether a murder is valid */
  def canMurderHappen(murderer:Player, victim:Player, weapon:Weapon):Boolean = 
    playerLocation.contains(murderer)
    && playerLocation.contains(victim)
    && weaponRoom.contains(weapon)
    && playerRoom(murderer) == playerRoom(victim)
    && playerRoom(murderer) == weaponRoom(weapon)
    && playersInRoom(playerRoom(murderer)).size == 2

  /** The state after a player is murdered */
  def murder(p:Player, w:Weapon):GameState = GameState(playerActor, playerLocation.removed(p), weaponRoom.removed(w))

  /** The state after a player has moved a step */
  def move(p:Player, d:Direction):GameState = 
    if playerLocation.contains(p) then 
      val actor = playerActor(p)
      val l = playerLocation(p)
      val newLoc = l.move(d)
      GameState(
        playerActor,
        playerLocation + (p -> (if isPassable(newLoc) then newLoc else l)),
        weaponRoom
      )
    else this
}

object GameState {
  def empty = GameState(
    Map.empty,
    Map.empty,
    Weapon.values.zip(randomRooms).toMap
  )
}