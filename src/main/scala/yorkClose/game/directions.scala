package yorkClose.game


/**
  * The game is tile-based, so a player's position is the coordinates of the square they are standing on
  */
type Location = (Int, Int)

/** 
 * The different directions players can move in the game
 */
enum Direction(val delta:(Int, Int)):
    case North extends Direction(0, -1)
    case East extends Direction(1, 0)
    case South extends Direction(0, 1)
    case West extends Direction(-1, 0)

/** For convenience */
extension (l:Location) {

  /** The location one square in a direction */
  def move(direction:Direction):Location = 
    val (x, y) = l
    val (dx, dy) = direction.delta
    (x + dx, y + dy)

  /** The moves you can make from a given location */
  def availableDirections:Seq[Direction] = 
    for d <- Direction.values if isPassable(l.move(d)) yield d

  /** Used in pathfinding. Produces a map of how far each square is from the current point, emanating outward until some condition is reached */
  def floodFill(until: Location => Boolean):Map[Location, Int] = {
    
    @scala.annotation.tailrec
    def step(distances:Map[Location, Int]):Map[Location, Int] = 
      val d2 = distances ++ (for 
        (l, i) <- distances
        direction <- l.availableDirections if !distances.contains(l.move(direction))
      yield
        l -> (i + 1)
      )

      if d2.exists((l, _) => until(l)) then d2 else step(d2)

    step(Map(l -> 0))
  }

  /** Finds the closest location meeting a given condition */
  def findClosest(condition: Location => Boolean):Location = 
    val ff = floodFill(condition)
    ff.keySet.find(condition).get

  /** Finds which direction to step in, to head towards a location meeting some criterion */
  def shortestDirectionTo(condition: Location => Boolean):Direction = 
    val target = findClosest(condition)
    val reverse = target.floodFill(_ == l)
    availableDirections.minBy((d) => reverse(l.move(d)))

  
}