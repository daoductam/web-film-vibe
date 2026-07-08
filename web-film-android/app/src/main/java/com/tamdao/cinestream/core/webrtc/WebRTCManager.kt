package com.tamdao.cinestream.core.webrtc

import android.content.Context
import android.util.Log
import com.tamdao.cinestream.core.websocket.StompManager
import com.tamdao.cinestream.data.model.SignalingMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.webrtc.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRTCManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stompManager: StompManager
) {
    private val tag = "WebRTCManager"
    private val scope = CoroutineScope(Dispatchers.Default)

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null
    private val peerConnections = mutableMapOf<Long, PeerConnection>()

    private val _remoteVideoStreams = MutableStateFlow<Map<Long, VideoTrack>>(emptyMap())
    val remoteVideoStreams: StateFlow<Map<Long, VideoTrack>> = _remoteVideoStreams

    init {
        initWebRTC()
    }

    private fun initWebRTC() {
        try {
            val options = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(options)

            val factory = PeerConnectionFactory.builder()
                .setVideoDecoderFactory(DefaultVideoDecoderFactory(EglBase.create().eglBaseContext))
                .setVideoEncoderFactory(DefaultVideoEncoderFactory(EglBase.create().eglBaseContext, true, true))
                .createPeerConnectionFactory()
            peerConnectionFactory = factory

            // Create Audio Track
            val audioSource = factory.createAudioSource(MediaConstraints())
            localAudioTrack = factory.createAudioTrack("local_audio_track", audioSource)
            log("WebRTC initialized successfully")
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize WebRTC factory: ${e.message}")
        }
    }

    fun startCamera(videoCapturer: VideoCapturer, surfaceTextureHelper: SurfaceTextureHelper) {
        val factory = peerConnectionFactory ?: return
        val videoSource = factory.createVideoSource(videoCapturer.isScreencast)
        videoCapturer.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
        videoCapturer.startCapture(480, 360, 15) // Cap stream at 480p, 15fps

        localVideoTrack = factory.createVideoTrack("local_video_track", videoSource)
        log("Local camera captured track created")
    }

    fun handleIncomingSignal(roomId: Long, senderUserId: Long, type: String, payload: String) {
        scope.launch {
            val pc = getOrCreatePeerConnection(roomId, senderUserId)
            when (type) {
                "OFFER" -> {
                    pc.setRemoteDescription(SimpleSdpObserver(), SessionDescription(SessionDescription.Type.OFFER, payload))
                    pc.createAnswer(object : SimpleSdpObserver() {
                        override fun onCreateSuccess(desc: SessionDescription?) {
                            desc?.let {
                                pc.setLocalDescription(SimpleSdpObserver(), it)
                                sendSignal(roomId, senderUserId, "ANSWER", it.description)
                            }
                        }
                    }, MediaConstraints())
                }
                "ANSWER" -> {
                    pc.setRemoteDescription(SimpleSdpObserver(), SessionDescription(SessionDescription.Type.ANSWER, payload))
                }
                "ICE_CANDIDATE" -> {
                    val parts = payload.split("|")
                    if (parts.size >= 3) {
                        val sdpMid = parts[0]
                        val sdpMLineIndex = parts[1].toInt()
                        val sdp = parts[2]
                        pc.addIceCandidate(IceCandidate(sdpMid, sdpMLineIndex, sdp))
                    }
                }
            }
        }
    }

    private fun getOrCreatePeerConnection(roomId: Long, targetUserId: Long): PeerConnection {
        return peerConnections[targetUserId] ?: createPeerConnection(roomId, targetUserId).also {
            peerConnections[targetUserId] = it
        }
    }

    private fun createPeerConnection(roomId: Long, targetUserId: Long): PeerConnection {
        val factory = peerConnectionFactory ?: throw IllegalStateException("Factory not initialized")
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        val pcObserver = object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let {
                    val payload = "${it.sdpMid}|${it.sdpMLineIndex}|${it.sdp}"
                    sendSignal(roomId, targetUserId, "ICE_CANDIDATE", payload)
                }
            }

            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                val track = receiver?.track()
                if (track is VideoTrack) {
                    val currentMap = _remoteVideoStreams.value.toMutableMap()
                    currentMap[targetUserId] = track
                    _remoteVideoStreams.value = currentMap
                    log("Remote track added from user $targetUserId")
                }
            }

            override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddStream(stream: MediaStream?) {}
        }

        val pc = factory.createPeerConnection(rtcConfig, pcObserver) ?: throw RuntimeException("Failed to create PeerConnection")

        // Add local tracks
        localAudioTrack?.let { pc.addTrack(it) }
        localVideoTrack?.let { pc.addTrack(it) }

        return pc
    }

    fun initiateCall(roomId: Long, targetUserId: Long) {
        scope.launch {
            val pc = getOrCreatePeerConnection(roomId, targetUserId)
            pc.createOffer(object : SimpleSdpObserver() {
                override fun onCreateSuccess(desc: SessionDescription?) {
                    desc?.let {
                        pc.setLocalDescription(SimpleSdpObserver(), it)
                        sendSignal(roomId, targetUserId, "OFFER", it.description)
                    }
                }
            }, MediaConstraints())
        }
    }

    private fun sendSignal(roomId: Long, targetUserId: Long, type: String, payload: String) {
        scope.launch {
            stompManager.sendJson(
                "/app/room.$roomId.signal",
                SignalingMessage(type = type, targetUserId = targetUserId, payload = payload)
            )
        }
    }

    fun disconnect() {
        peerConnections.values.forEach { it.close() }
        peerConnections.clear()
        localVideoTrack = null
        _remoteVideoStreams.value = emptyMap()
        log("WebRTC PeerConnections disconnected cleanly")
    }

    private fun log(message: String) {
        Log.d(tag, message)
    }

    open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(p0: String?) {}
        override fun onSetFailure(p0: String?) {}
    }
}
