package com.tamdao.cinestream.core.websocket

import android.util.Log
import com.google.gson.Gson
import kotlinx.io.bytestring.decodeToString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.frame.FrameBody
import org.hildan.krossbow.stomp.headers.StompSendHeaders
import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Singleton
class StompManager @Inject constructor(
    private val sessionManager: com.tamdao.cinestream.core.session.SessionManager,
    val gson: Gson
) {
    private val tag = "StompManager"

    private val wsClient = OkHttpWebSocketClient(
        OkHttpClient.Builder()
            .apply {
                try {
                    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                    })
                    val sslContext = SSLContext.getInstance("SSL")
                    sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                    sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                    hostnameVerifier { _, _ -> true }
                } catch (e: Exception) {
                    Log.e(tag, "Failed to bypass SSL verify: ${e.message}")
                }
            }
            .build()
    )

    private val stompClient = StompClient(wsClient)
    private var stompSession: StompSession? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _connectionState = MutableSharedFlow<ConnectionState>(replay = 1)
    val connectionState: Flow<ConnectionState> = _connectionState

    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    fun connect(wsUrl: String) {
        coroutineScope.launch {
            _connectionState.emit(ConnectionState.Connecting)
            try {
                val token = sessionManager.accessToken.first()
                val authHeaders = if (token != null) mapOf("Authorization" to "Bearer $token") else emptyMap()
                val cleanUrl = wsUrl.replace("http://", "ws://")
                                    .replace("https://", "wss://")

                Log.d(tag, "Connecting STOMP at: $cleanUrl")
                val session = stompClient.connect(cleanUrl, customStompConnectHeaders = authHeaders)
                stompSession = session
                _connectionState.emit(ConnectionState.Connected)
                Log.d(tag, "STOMP connected")
            } catch (e: Exception) {
                Log.e(tag, "STOMP connection failed: ${e.message}", e)
                _connectionState.emit(ConnectionState.Error(e.message ?: "Lỗi kết nối WebSocket"))
            }
        }
    }

    /**
     * Subscribe to a STOMP topic and deserialize frames into the given [clazz].
     * Uses Gson for JSON deserialization.
     */
    fun <T : Any> subscribe(topic: String, clazz: Class<T>): Flow<T> {
        val session = stompSession ?: throw IllegalStateException("STOMP session not initialized.")
        Log.d(tag, "Subscribing to topic: $topic")
        return channelFlow {
            session.subscribe(StompSubscribeHeaders(topic)).collect { frame ->
                val text = when (val body = frame.body) {
                    is FrameBody.Text -> body.text
                    is FrameBody.Binary -> body.bytes.decodeToString()
                    null -> return@collect
                }
                try {
                    trySend(gson.fromJson(text, clazz))
                } catch (e: Exception) {
                    Log.e(tag, "Failed to deserialize frame on $topic: ${e.message}")
                }
            }
        }
    }

    /**
     * Send a payload serialized as JSON to the given STOMP [destination].
     */
    suspend fun sendJson(destination: String, payload: Any) {
        val session = stompSession ?: throw IllegalStateException("STOMP session not initialized.")
        Log.d(tag, "Sending to: $destination")
        val json = gson.toJson(payload)
        session.send(StompSendHeaders(destination), FrameBody.Text(json))
    }

    fun disconnect() {
        coroutineScope.launch {
            try {
                stompSession?.disconnect()
                stompSession = null
                _connectionState.emit(ConnectionState.Disconnected)
                Log.d(tag, "STOMP disconnected")
            } catch (e: Exception) {
                Log.e(tag, "Error during disconnect: ${e.message}")
            }
        }
    }
}
