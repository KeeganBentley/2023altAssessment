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
def murderer(player:Player, location:Location):MessageHandler[Message] = 
    info(s"$player is the muderer")
    val room = house(location)
    murderer(randomRooms.filter(_ != room).head)

def murderer(goingTo:Room):MessageHandler[Message] = MessageHandler { (msg, context) =>
  msg match 

    case Message.TurnUpdate(me, position, room, visiblePlayers, visibleWeapons) =>
        if visiblePlayers.size == 1 && visibleWeapons.nonEmpty then 
            gameActor ! (me, Command.Murder(visiblePlayers.head, visibleWeapons.head))

        if room == goingTo then 
            val next = randomRooms.filter(_ != room).head
            info(s"Heading to $next")
            murderer(next)
        else 
            val shortest = position.shortestDirectionTo(goingTo)
            info(s"Shortest way to $goingTo is $shortest")
            gameActor ! (me, Command.Move(shortest))
            murderer(goingTo)

    case _ => ()


        

}