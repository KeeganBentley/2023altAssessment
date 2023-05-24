package yorkClose.game

/**
  * Commands that players can send in the game
  */
enum Command:
    case Move(direction:Direction)
    case Murder(player:Player, weapon:Weapon)

/**
  * Commands that can be invoked on the ghost of Elizabeth Dacre
  */
enum ElizabethDacreCommand:
    case Accuse(player:Player, weapon:Weapon, room:Room)

