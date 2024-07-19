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
case class PlayerState(goingTo:Room, suspects:Seq[Player], weaponsSeen:Map[Weapon, Room], usedWeapons:Seq[Weapon], knownVictims:Seq[Player], heardScream:java.lang.Boolean)    

object PlayerState:
    def empty = PlayerState(randomRooms.head, Player.values.toSeq, Map.empty, Seq.empty, Seq.empty, false)

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
    val initialSuspects = Player.values.toSeq.filterNot(_ == p)
    player(PlayerState.empty.copy(suspects = initialSuspects))
    
    
def player(playerState:PlayerState):MessageHandler[Message] = MessageHandler { (msg, context) => 

    msg match
        case Message.TurnUpdate(me, location, room, visiblePlayers, visibleWeapons) =>  
            val updatedSuspects = playerState.suspects.filterNot(playerState.knownVictims.contains)

            // Check if we have an accusation to make, requires only 1 suspect a victim, weapon and room
            if updatedSuspects.length == 1 && playerState.usedWeapons.nonEmpty then
                    val accused = updatedSuspects.head
                    val victim = playerState.knownVictims.head
                    val usedWeapon = playerState.usedWeapons.head
                    val weaponLocation = playerState.weaponsSeen(usedWeapon)
                    gameActor ! ElizabethDacreCommand.Accuse(accused, victim, usedWeapon, weaponLocation)
            
            // If we have heard a scream we will eliminate all visible players from our suspects ignoring doorways
            val finalSuspects =
                if room == Room.Door then
                    updatedSuspects
                else if playerState.heardScream then
                    updatedSuspects.filterNot(visiblePlayers.contains)

                else
                    updatedSuspects
            // If we see a weapon we will update our weaponsSeen map 
            val (updatedWeapons, updatedUsed) = 
                if visibleWeapons.nonEmpty then
                    (visibleWeapons.foldLeft(playerState.weaponsSeen) { (acc, weapon) => acc + (weapon -> room) }, playerState.usedWeapons)
                // If not we check if we know what weapon was used from this room
                else
                    val missingWeapons = playerState.weaponsSeen.filter { case (weapon, seenRoom) => seenRoom == room && !visibleWeapons.contains(weapon) && !playerState.usedWeapons.contains(weapon) }.keys
                    (playerState.weaponsSeen, playerState.usedWeapons ++ missingWeapons)

            val updatedState = playerState.copy(weaponsSeen = updatedWeapons, usedWeapons = updatedUsed, suspects = finalSuspects, heardScream = false)  
                
            if room == updatedState.goingTo then
                player(updatedState.copy(goingTo = randomRooms.head))
            else 
                gameActor ! (me, Command.Move(location.shortestDirectionTo(updatedState.goingTo)))
                player(updatedState)

        case Message.Scream(screamer) =>
            val updatedPlayerState = playerState.copy(knownVictims = playerState.knownVictims :+ screamer, heardScream = true)
            player(updatedPlayerState)

        case _ =>
            player(playerState)
}