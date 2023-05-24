package yorkClose.bots

import com.wbillingsley.amdram.*
import yorkClose.*
import game.*

/**
  * You are the murderer.
  * 
  * This function needs to remain, because this is how the Game spawns the players.
  * However, upon receiving a message (e.g. the first Tick), you can change to whatever handler you define
  *
  * @param player The Player this actor is playing (e.g. Player.Pink)
  * @param location The (x, y) location the player has initially spawned in
  * @return
  */
def murderer(player:Player, location:Location):MessageHandler[Message] = murderer(randomRooms.head)

def murderer(goingTo:Room):MessageHandler[Message] = MessageHandler { (msg, context) =>



}