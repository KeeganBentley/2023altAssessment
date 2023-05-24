package yorkClose

import java.awt.BorderLayout
import javax.swing.{JFrame, JPanel}

import java.awt.Color

import com.wbillingsley.amdram.* 
import iteratees.*

import scala.concurrent.* 
import ExecutionContext.Implicits.global

import game.*

/**
  * The actor system is a top-level "given" instance so that it is automatically found where it
  * is needed
  */
given troupe:Troupe = SingleEcTroupe()

/**
  * This is called by the JavaFX UI on start-up
  */
def startGame() = {
  info("Setting up the game")

  


}

