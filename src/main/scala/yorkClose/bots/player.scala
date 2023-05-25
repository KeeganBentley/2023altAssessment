package yorkClose.bots

import com.wbillingsley.amdram.*
import yorkClose.* 
import game.*

import scala.util.Random

/**
  * Provided as a suggestion for how to manage your state
  *
  * @param headingTo - the room you would like to go to
  * @param suspects - the list of suspects not yet eliminated (either by being proven innocent or by being 'eliminated')
  * @param weaponsSeen - where you have seen weapons, so you can track them
  * @param usedWeapons - weapons that have disappeared
  * @param knownVictims - victims you have heard scream
  */
case class PlayerState(goingTo:Room, suspects:Seq[Player], weaponsSeen:Map[Weapon, Room], usedWeapons:Seq[Weapon], knownVictims:Seq[Player])    

object PlayerState:
    def empty = PlayerState(randomRooms.head, Player.values.toSeq, Map.empty, Seq.empty, Seq.empty)

/**
  * You are not the murderer.
  * 
  * This function needs to remain, because this is how the Game spawns the players.
  * However, upon receiving a message (e.g. the first Tick), you can change to whatever handler you define
  *
  * @param player The Player this actor is playing (e.g. Player.Pink)
  * @param location The (x, y) location the player has initially spawned in
  * @return
  */
def player(p:Player, location:Location):MessageHandler[Message] = 
    player(PlayerState.empty)
    
def player(playerState:PlayerState):MessageHandler[Message] = MessageHandler { (msg, context) => 

    msg match
        case Message.TurnUpdate(me, location, room, visiblePlayers, visibleWeapons) =>            
            // This code makes the player randomly travel from room to room
            if room == playerState.goingTo then
                player(playerState.copy(goingTo = randomRooms.head))
            else 
                gameActor ! (me, Command.Move(location.shortestDirectionTo(playerState.goingTo)))
                player(playerState)

        case _ =>
            player(playerState)

    
    
}