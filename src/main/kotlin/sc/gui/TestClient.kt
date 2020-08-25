package sc.gui

import org.slf4j.LoggerFactory
import sc.framework.plugins.Player
import sc.plugin2021.*
import sc.plugin2021.util.GameRuleLogic
import sc.shared.GameResult

// connects our game handler (ClientListener) to the server
class TestClient(playerType: PlayerType, host: String, port: Int): AbstractClient(host, port) {
    companion object {
        val logger = LoggerFactory.getLogger(TestClient::class.java)
    }

    init {
        val logic = TestGameHandler(playerType, this)
        handler = logic
    }
}

class TestGameHandler(private val playerType: PlayerType, private val client: AbstractClient): IGameHandler {

    companion object {
        val logger = LoggerFactory.getLogger(TestGameHandler::class.java)
    }

    var currentState: GameState? = null;

    override fun gameEnded(data: GameResult, team: Team?, errorMessage: String) {
    }

    override fun onRequestAction() {
        logger.debug(this.playerType.toString(), "got new action request!")
        if (currentState != null) {
            val possibleMoves = GameRuleLogic.getPossibleMoves(currentState!!)
            sendAction(
                    if (possibleMoves.isEmpty()) PassMove(currentState!!.currentColor)
                    else possibleMoves.random())
            /*
            val color = currentState!!.currentColor
            val pieces = currentState!!.undeployedPieceShapes[color]
            val pieceShape = currentState!!.startPiece
            val move = SetMove(Piece(
                    color,
                    pieceShape,
                    Rotation.NONE,
                    false,
                    Coordinates(0, 0)
            ))
            sendAction(move)
             */
        } else {
            logger.error("got move request before gamestate")
        }
    }

    override fun onUpdate(player: Player, otherPlayer: Player) {
    }

    override fun onUpdate(gamestate: GameState) {
        currentState = gamestate
        logger.debug("Got new gamestate, board is now\n" + currentState?.board)
    }

    override fun sendAction(move: Move) {
        client.sendMove(move)
    }

}
