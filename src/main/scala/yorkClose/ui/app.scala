//> using repositories sonatype:snapshots
//> using dep com.wbillingsley::amdram::0.0.0+10-993bfbd8-SNAPSHOT
//> using dep org.scalafx::scalafx::20.0.0-R31

package yorkClose.ui

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

import java.util.{Timer, TimerTask}

import yorkClose.*
import game.*

/**
 * The app runs the game. This generates a main for us
 */
object App extends JFXApp3 { 

    override def start() = {
        val ui = new UI()
        gameActor ! GameControl.Initialise(ui)

        stage = new JFXApp3.PrimaryStage {
            title.value = "Murder at York Close"
            width = 24 * 32
            height = 600
            scene = new Scene {
                content = ui.subscene                    
            }
            onCloseRequest = { (_) => System.exit(0) }
        }
    }

}


