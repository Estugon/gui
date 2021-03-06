package sc.gui.view

import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.image.ImageView
import sc.gui.AppStyle
import sc.gui.controller.AppController
import sc.gui.controller.GameController
import sc.gui.controller.ServerController
import sc.gui.model.ViewTypes
import sc.plugin2021.Rotation
import tornadofx.*
import java.awt.Desktop
import java.net.URI

class AppView : View("Software-Challenge Germany") {
    val controller: AppController by inject()
    private val gameController: GameController by inject()
    private val serverController: ServerController by inject()
    private val sochaIcon = ImageView(AppView::class.java.getResource("/icon.png").toExternalForm())

    override val root = borderpane {
        addClass(AppStyle.lightColorSchema)
        top = menubar {
            menu(graphic = sochaIcon) {
                item("Beenden", "Shortcut+Q").action {
                    println("Quitting!")
                    Platform.exit()
                }
                item("Neues Spiel", "Shortcut+N").action {
                    enableWhen(controller.model.currentViewProperty().isNotEqualTo(ViewTypes.GAME_CREATION))
                    println("New Game!")
                    if (controller.model.currentViewProperty().get() == ViewTypes.GAME) {
                        alert(
                                type = Alert.AlertType.CONFIRMATION,
                                header = "Neues Spiel anfangen",
                                content = "Willst du wirklich dein aktuelles Spiel verwerfen und ein neues anfangen?",
                                actionFn = { btnType ->
                                    if (btnType.buttonData == ButtonBar.ButtonData.OK_DONE) {
                                        controller.changeViewTo(GameCreationView::class)
                                    }
                                }
                        )
                    } else if (controller.model.currentViewProperty().get() != ViewTypes.GAME_CREATION) {
                        controller.changeViewTo(GameCreationView::class)
                    }
                }
                item("Toggle Darkmode").action {
                    controller.toggleDarkmode()
                }
                separator()
                item("Replay laden").action {
                    // TODO
                    println("Replay wird geladen")
                }
                item("Logs öffnen", "Shortcut+L").action {
                    // TODO
                    println("Logs werden geöffnet")
                }
            }
            menu("Steuerung") {
                enableWhen(controller.model.currentViewProperty().isEqualTo(ViewTypes.GAME))
                menu("Rotieren") {
                    item("Scrollen", "Mausrad")
                    item("Uhrzeigersinn", "D").action {
                        gameController.rotatePiece(Rotation.RIGHT)
                    }
                    item("Gegen Uhrzeigersinn", "A").action {
                        gameController.rotatePiece(Rotation.LEFT)
                    }
                    item("180", "W oder S").action {
                        gameController.rotatePiece(Rotation.MIRROR)
                    }
                }
                item("Flippen", "R-Click oder CTRL").action {
                    gameController.flipPiece()
                }
            }
            menu("Hilfe") {
                item("Spielregeln", "Shortcut+S").action {
                    // TODO: github.io Link der Doku einfügen
                    Desktop.getDesktop().browse(URI("https://cau-kiel-tech-inf.github.io/socha-enduser-docs/spiele/blokus/regeln.html"))
                }
                item("Dokumentation", "Shortcut+D").action {
                    Desktop.getDesktop().browse(URI("https://cau-kiel-tech-inf.github.io/socha-enduser-docs/"))
                }
                item("Webseite", "Shortcut+I").action {
                    Desktop.getDesktop().browse(URI("https://www.software-challenge.de"))
                }
                item("Wettbewerb", "Shortcut+W").action {
                    Desktop.getDesktop().browse(URI("https://contest.software-challenge.de/saison/28"))
                }
            }
        }
    }

    init {
        sochaIcon.fitHeight = 32.0
        sochaIcon.fitWidth = 32.0
        with(root) {
            prefWidth = 1100.0
            prefHeight = 700.0
            center(StartView::class)
        }

        // responsive scaling
        val resizer = ChangeListener<Number> { _, _, _ ->
            if (controller.model.currentViewProperty().get() == ViewTypes.GAME) {
                find(GameView::class).resize()
            }
        }
        root.widthProperty().addListener(resizer)
        root.heightProperty().addListener(resizer)
    
        titleProperty.bind(controller.model.currentViewProperty().stringBinding {
            when(it) {
                ViewTypes.GAME_CREATION -> "Neues Spiel - Software-Challenge Germany"
                ViewTypes.GAME -> "Spiele Blokus - Software-Challenge Germany"
                ViewTypes.START -> "Software-Challenge Germany"
                null -> throw NoWhenBranchMatchedException("Current view can't be null!")
            }
        })
    
        controller.model.isDarkModeProperty().listenImmediately { value ->
            if (value) {
                root.removeClass(AppStyle.lightColorSchema)
                root.addClass(AppStyle.darkColorSchema)
            } else {
                root.removeClass(AppStyle.darkColorSchema)
                root.addClass(AppStyle.lightColorSchema)
            }
        }
    }
}

fun ObservableValue<Boolean>.listenImmediately(listener: (newValue: Boolean) -> Unit) {
    listener(this.value)
    addListener { _, _, new -> listener(new) }
}