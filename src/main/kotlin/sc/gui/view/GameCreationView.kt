package sc.gui.view

import javafx.beans.Observable
import javafx.beans.binding.Bindings
import javafx.beans.property.Property
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.stage.FileChooser
import sc.gui.controller.AppController
import sc.gui.controller.GameCreationController
import sc.gui.model.*
import sc.plugin2021.Team
import tornadofx.*
import java.io.File

class GameCreationView : View() {
    private val appController: AppController by inject()
    val controller: GameCreationController by inject()

    override val root = borderpane {
        style {
            padding = box(20.px)
        }
        center = form {
            hbox {
                vbox(20) {
                    add(PlayerFragment(Team.ONE, controller.playerOneSettingsModel))
                }
                vbox(20) {
                    add(PlayerFragment(Team.TWO, controller.playerTwoSettingsModel))
                }
            }
        }
        bottom = hbox {
            style {
                alignment = Pos.TOP_RIGHT
            }
            button("Erstellen") {
                action {
                    controller.createGame()
                }

                enableWhen(Bindings.and(controller.playerOneSettingsModel.valid, controller.playerTwoSettingsModel.valid))
            }

            button("Zurück") {
                action {
                    appController.changeViewTo(when (appController.model.previousViewProperty().get()) {
                        ViewTypes.GAME_CREATION -> GameCreationView::class
                        ViewTypes.GAME -> GameView::class
                        ViewTypes.START -> StartView::class
                        else -> throw Exception("Unknown type of view")
                    })
                }
            }
        }
    }
}

class PlayerFragment(private val team: Team, private val settings: TeamSettingsModel) : Fragment() {
    val controller: GameCreationController by inject()

    override var root = vbox(20) {
        fieldset("Spieler Nr. $team") {
            textfield(settings.name).required()
            add(PlayerFileSelectFragment(team, settings))
        }
    }


}

class PlayerFileSelectFragment(private val team: Team, private val settings: TeamSettingsModel) : Fragment() {
    val controller: GameCreationController by inject()

    override var root = borderpane {
        top = hbox {
            combobox(settings.type, PlayerType.values().toList())
        }
    }

    private fun updatePlayerType() {
        // TODO: work with proper binding of property

        when (settings.type.getValue()) {
            PlayerType.COMPUTER -> {
                root.center = hbox(20) {
                    button("Client wählen") {
                        action {
                            val selectedFile = chooseFile(
                                    "Client wählen",
                                    arrayOf(
                                            FileChooser.ExtensionFilter("Alle Dateien", "*.*"),
                                            FileChooser.ExtensionFilter("jar", "*.jar")
                                    )
                            )
                            if (selectedFile != null && selectedFile.isNotEmpty()) {
                                println("Selected file $selectedFile")
                                settings.executable.setValue(selectedFile.first())
                            }
                        }
                    }
                    label("Wähle eine ausführbare Datei aus")
                }
                root.bottom = textflow {
                    label("Ausgewählte Datei: ")
                    label("")
                }
            }
            PlayerType.MANUELL -> {
                root.center = label("Das Programm muss nach Erstellung des Spiels manuell gestartet werden.")
                root.bottom = label()
            }
            PlayerType.INTERNAL -> {
                root.center = label("Ein interner Computerspieler wird hier spielen")
                root.bottom = label()
            }
            PlayerType.HUMAN -> {
                root.center = label("Ein Mensch wird das Spiel hier spielen")
                root.bottom = label()
            }
        }
    }


    init {
        settings.type.onChange {
            updatePlayerType()
            settings.validate()
        }
        settings.executable.onChange {
                root.bottom = textflow {
                    label("Ausgewählte Datei: ")
                    label(settings.executable.getValue().absolutePath)
                }
            }
        updatePlayerType()

        val obs: ObservableValue<File?> = settings.executable
        settings.validationContext.addValidator(root.bottom, obs, ValidationTrigger.OnChange()) {
            if (settings.type.value == PlayerType.COMPUTER && settings.executable.value == null) error("Error") else null
        }
        settings.validate()
    }
}