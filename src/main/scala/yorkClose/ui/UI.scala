package yorkClose.ui

import yorkClose.* 
import game.*


import scalafx.Includes._
import scalafx.application.JFXApp3
import scalafx.scene.{Scene, Group}
import scalafx.scene.paint.Color
import scalafx.scene.shape.*
import scalafx.scene.layout.*
import scalafx.scene.control.*
import scalafx.geometry.*
import scalafx.collections.*
import scalafx.animation.*
import scalafx.beans.property.*
import scalafx.scene.text.Text

import com.wbillingsley.amdram.*
import scalafx.application.Platform

/**
  * The user interface for the game. Also houses the animation timer.
  */
class UI() extends Recipient[GameState] {

    val squareSize = 32;

    private var _gameState = GameState.empty

    val roomColours = Map(
        Room.DiningRoom -> Color.TAN,
        Room.Conservatory -> Color.OLDLACE,
        Room.Pantry -> Color.PAPAYAWHIP,
        Room.Hall -> Color.LAVENDERBLUSH,
        Room.Lounge -> Color.LAVENDER,
        Room.Study -> Color.GHOSTWHITE,
        Room.Kitchen -> Color.LIGHTGOLDENRODYELLOW,
        Room.Garden -> Color.LIGHTGREEN,
        Room.Terrace -> Color.LINEN,
        Room.Workshop -> Color.LIGHTBLUE,
        Room.Boatshed -> Color.GOLDENROD,
        Room.Lake -> Color.CORNFLOWERBLUE,
        Room.Door -> Color.LIGHTGRAY,
        Room.Wall -> Color.DARKGREY,
    )

    class Square(location:Location, tile:Room) {
        import Room.*

        private val square = new Rectangle {
            width = squareSize
            height = squareSize
            fill = roomColours(tile)
        }

        val ui = new Group {
            translateX = location._1 * squareSize
            translateY = location._2 * squareSize
            children = Seq(square)
        }
    }

    /** The map of the house itself */
    val houseTiles = for (loc, tile) <- house yield Square(loc, tile)

    /** The centre pixel of a room, for the label */
    val roomLabelLocs = {
        import Room.*
        Map(
            DiningRoom -> (1, 2),
            Lounge -> (7, 2),
            Conservatory -> (11, 2),
            Terrace -> (15, 2),
            Garden -> (18, 2),
            Kitchen -> (1, 6),
            Hall -> (5, 6),
            Pantry -> (5, 8),
            Study -> (8, 8),
            Workshop -> (11, 8),
            Boatshed -> (21, 8),
        )
    }

    /** A fixed set of labels for each room */
    val roomLabels = for (label, (x, y)) <- roomLabelLocs yield new Group {
        translateX = ((x + .1) * squareSize).toInt
        translateY = ((y - .2) * squareSize).toInt
        children = Seq(new Text {
            text = label.toString
            fill = Color.BLACK
        })
    }

    /** The location where to show text if a weapon is present */
    val weaponLabelLocs = {
        import Room.*
        Map(
            DiningRoom -> (1, 3),
            Lounge -> (7, 3),
            Conservatory -> (11, 3),
            Terrace -> (15, 3),
            Garden -> (18, 3),
            Kitchen -> (1, 7),
            Hall -> (8, 6),
            Pantry -> (5, 9),
            Study -> (8, 9),
            Workshop -> (11, 9),
            Boatshed -> (21, 9),
        )
    }

    /** Creates a set of labels for the weapons */
    def weaponLabels() = for 
        (loc, (x, y)) <- weaponLabelLocs
        weapon <- _gameState.weaponRoom.find((_, t) => t == loc).map((w, _) => w)
    yield new Group {
        translateX = ((x + .1) * squareSize).toInt
        translateY = ((y - .2) * squareSize).toInt
        children = Seq(new Text { 
            text = weapon.toString
            fill = Color.RED
        })
    }

    /** Needed so the setter will be available as a setter */
    def gameState = _gameState


    /** The fixed part of the UI, as the rooms don't move! */
    val fixedBackground = new Group(houseTiles.toSeq.map(_.ui) ++ roomLabels*)
    
    /** A group that contains the current weapon labels */
    val weaponGroup = new Group(weaponLabels().toSeq*)

    val playerIcons = (for p <- Player.values yield 
        val color = Color.web(p.colour)
        p -> new Circle {
                radius = squareSize / 4 
                fill = color
                stroke = Color.Black
            }
    ).toMap

    /** Creates a set of markers for each player on the map */
    def alivePlayerIcons() = for (p, (x, y)) <- gameState.playerLocation yield        
        playerIcons(p)
    

    /** A group that contains the current player icons */
    val playerGroup = new Group(alivePlayerIcons().toSeq*)

    /** Receives a new gamestate as a message */
    override def send(g:GameState):Unit = 
        _gameState = g
        Platform.runLater {
            weaponGroup.children = weaponLabels()
            playerGroup.children = alivePlayerIcons()
            playerTable.refresh()

            for (p, (x, y)) <- g.playerLocation do 
                val icon = playerIcons(p)
                val timeline = new Timeline {
                    cycleCount = 1
                    keyFrames = Seq(
                        at (250.ms) { Set(
                            icon.centerX -> (x * squareSize + squareSize / 2),
                            icon.centerY -> (y * squareSize + squareSize / 2)
                        )}
                    )
                }
                timeline.play()
        }



    val players = ObservableBuffer.from(Player.values)
    val playerTable = new TableView[Player](players) {
        columns ++= Seq(
            new TableColumn[Player, Player] {
                text = "Player"
                
                cellValueFactory = x => ObjectProperty(x.value)

                cellFactory = (cell, p) => {
                    cell.text = p.name
                    cell.textFill = Color.web(p.colour)
                    cell.style = "-fx-background-color: #999; -fx-border-style: solid; -fx-border-width: 1px;"
                }
            },
            new TableColumn[Player, Option[Room]] {
                text = "Location"
                
                cellValueFactory = x => ObjectProperty(gameState.playerRoom.get(x.value))

                cellFactory = (cell, r) => {
                    cell.text = r.map(_.toString).getOrElse("Deceased")
                    cell.textFill = r.map(rr => roomColours(rr)).getOrElse(Color.DarkRed)
                    cell.style = "-fx-background-color: #999; -fx-border-style: solid; -fx-border-width: 1px;"
                }
            }
        )
    }

        
    val playing = BooleanProperty(false)
    var tickInterval = 5e8

    // Time of last tick
    private var lastTime = 0L

    /** Controls when game ticks are sent */
    AnimationTimer({ now =>
        if playing.value && (now > lastTime + tickInterval) then 
            lastTime = now
            gameActor ! GameControl.Tick
    }).start()

    // Starts and stops the game from ticking    
    private val startStop = new Button {
        text <== (for p <- playing yield if p then "Stop" else "Start")
        onAction = { _ => playing.value = !playing.value }
    }


    val subscene = new HBox(
        new VBox(
            new Group(fixedBackground, weaponGroup, playerGroup),
            new HBox(5, startStop),
            playerTable
        ),
    )

}