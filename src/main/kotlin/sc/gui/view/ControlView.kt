package sc.gui.view

import javafx.beans.InvalidationListener
import javafx.beans.binding.Bindings
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import sc.gui.AppStyle
import sc.gui.controller.AppController
import sc.gui.controller.ClientController
import sc.gui.controller.GameController
import sc.gui.controller.GameCreationController
import sc.gui.model.PlayerType
import sc.plugin2021.Color
import tornadofx.*

class ControlView() : View() {
    private val gameController: GameController by inject()
    private val clientController: ClientController by inject()
    private val appController: AppController by inject()
    private val gameCreationController: GameCreationController by inject()
    private val playPauseButton: Button = button {
        text = "Start"
    }
    private val selected = hbox {
        addClass(AppStyle.pieceUnselectable)
        label("Selected: ")
        hbox {
            addClass(AppStyle.undeployedPiece, when (gameController.selectedColor.value) {
                Color.BLUE -> AppStyle.borderBLUE
                Color.GREEN -> AppStyle.borderGREEN
                Color.YELLOW -> AppStyle.borderYELLOW
                else -> AppStyle.borderRED
            })
            gameController.selectedColor.addListener { _, _, newValue ->
                removeClass(AppStyle.borderRED)
                removeClass(AppStyle.borderBLUE)
                removeClass(AppStyle.borderGREEN)
                removeClass(AppStyle.borderYELLOW)
                if (newValue != null) {
                    addClass(when (newValue) {
                        Color.RED -> AppStyle.borderRED
                        Color.BLUE -> AppStyle.borderBLUE
                        Color.GREEN -> AppStyle.borderGREEN
                        Color.YELLOW -> AppStyle.borderYELLOW
                    })
                }
            }
            this += PiecesFragment(gameController.selectedColor, gameController.selectedShape, gameController.selectedRotation, gameController.selectedFlip)
        }
    }

    override val root = borderpane {
        padding = Insets(10.0, 0.0, 10.0, 0.0)

        center {
            hbox {
                alignment = Pos.TOP_CENTER
                hbox {
                    padding = Insets(0.0, 10.0, 0.0, 10.0)
                    this += playPauseButton
                }
                button {
                    disableWhen(gameController.currentTurnProperty().isEqualTo(0))
                    text = "⏮"
                    setOnMouseClicked {
                        clientController.previous()
                    }
                }
                label {
                    padding = Insets(0.0, 10.0, 0.0, 10.0)
                    textProperty().bind(Bindings.concat(gameController.currentTurnProperty(), " / ", gameController.availableTurnsProperty()))
                }
                button {
                    disableWhen(gameController.currentTurnProperty().isEqualTo(gameController.availableTurnsProperty()))
                    text = "⏭"
                    setOnMouseClicked {
                        clientController.next()
                    }
                }
            }
        }
    }

    init {
		val updatePauseState = {
            if(clientController.controllingClient?.game?.isPaused!!) {
                playPauseButton.text = "Play"
            } else {
                playPauseButton.text = "Pause"
            }
        }
        playPauseButton.setOnMouseClicked {
            if (!gameController.gameStartedProperty().get()) {
                gameController.gameStartedProperty().set(true)
            }
            if (gameController.gameEndedProperty().get()) {
                appController.changeViewTo(StartView::class)
                gameController.clearGame()
            } else {
                clientController.togglePause()
				updatePauseState()
            }
        }
    
        gameController.currentTurnProperty().addListener(InvalidationListener {
			// When the game is paused externally e.g. when rewinding
            updatePauseState()
        })
        gameController.gameStartedProperty().addListener { _, _, started ->
            if (!started) {
                playPauseButton.text = "Start"
            } else {
                if (gameCreationController.playerOneSettingsModel.type.value == PlayerType.HUMAN || gameCreationController.playerTwoSettingsModel.type.value == PlayerType.HUMAN) {
                    root.left = selected
                }
            }
        }
        gameController.gameEndedProperty().addListener { _, _, ended ->
            if (ended) {
                playPauseButton.text = "Spiel beenden"
                root.left = hbox()
            }
        }
        gameController.isHumanTurnProperty().addListener { _, _, humanTurn ->
            if (humanTurn) {
                selected.removeClass(AppStyle.pieceUnselectable)
            } else if (!humanTurn && !selected.hasClass(AppStyle.pieceUnselectable)) {
                selected.addClass(AppStyle.pieceUnselectable)
            }
        }
    }
}