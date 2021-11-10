package websockets

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.server.standard.ServerEndpointExporter
import java.util.*
import javax.websocket.*
import javax.websocket.CloseReason.CloseCodes
import javax.websocket.server.ServerEndpoint


@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Configuration
@EnableWebSocket
class WebSocketConfig {
    @Bean
    fun serverEndpoint() = ServerEndpointExporter()
}

@ServerEndpoint("/eliza")
@Component
class ElizaEndpoint {

    private val eliza = Eliza()

    /**
     * Successful connection
     *
     * @param session
     */
    @OnOpen
    fun onOpen(session: Session) {
        LOGGER.info("Server Connected ... Session ${session.id}")
        synchronized(session) {
            with(session.basicRemote) {
                sendText("The doctor is in.")
                sendText("What's on your mind?")
                sendText("---")
            }
        }
    }

    /**
     * Connection closure
     *
     * @param session
     */
    @OnClose
    fun onClose(session: Session, closeReason: CloseReason) {
        LOGGER.info("Session ${session.id} closed because of $closeReason")
    }

    /**
     * Message received
     *
     * @param message
     */
    @OnMessage
    fun onMsg(message: String, session:Session) {
        LOGGER.info("Server Message ... Session ${session.id}")
        val currentLine = Scanner(message.lowercase(Locale.getDefault()))
        if (currentLine.findInLine("bye") == null) {
            LOGGER.info("Server received \"$message\"")
            synchronized(session) {
                with(session.basicRemote) {
                    sendText(eliza.respond(currentLine))
                    sendText("---")
                }
            }
        } else {
            session.close(CloseReason(CloseCodes.NORMAL_CLOSURE, "Alright then, goodbye!"))
        }
    }

    @OnError
    fun onError(session: Session, errorReason: Throwable) {
        LOGGER.error("Session ${session.id} closed because of ${errorReason.javaClass.name}", errorReason)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ElizaEndpoint::class.java)
    }
}