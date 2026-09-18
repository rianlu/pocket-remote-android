package com.pocketremote.net

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.pocketremote.protocol.Constants
import com.pocketremote.protocol.TvDevice
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer

import java.util.concurrent.ConcurrentHashMap

/** NSD 优先，失败或并行用 UDP PKTREMT1；结果按 host 去重。 */
class Discovery(private val context: Context) {
    interface Listener {
        fun onDevice(device: TvDevice)
        fun onFinished()
    }

    private val tag = "PocketRemoteDisc"
    private var nsd: NsdManager? = null
    private var nsdListener: NsdManager.DiscoveryListener? = null
    private var udpSocket: DatagramSocket? = null
    private val seen = ConcurrentHashMap<String, Boolean>()

    fun start(listener: Listener) {
        stop()
        seen.clear()
        startNsd(listener)
        startUdp(listener)
    }

    fun stop() {
        val n = nsd
        val l = nsdListener
        if (n != null && l != null) {
            try {
                n.stopServiceDiscovery(l)
            } catch (_: Exception) {
            }
        }
        nsdListener = null
        udpSocket?.close()
        udpSocket = null
    }

    private fun startNsd(listener: Listener) {
        val mgr = context.getSystemService(Context.NSD_SERVICE) as? NsdManager ?: return
        nsd = mgr
        val disc = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.w(tag, "nsd start fail $errorCode")
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
            override fun onDiscoveryStarted(serviceType: String) {}
            override fun onDiscoveryStopped(serviceType: String) {}
            override fun onServiceLost(serviceInfo: NsdServiceInfo) {}
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                if (serviceInfo.serviceType != Constants.NSD_TYPE &&
                    !serviceInfo.serviceType.contains("pocketremote")
                ) {
                    return
                }
                mgr.resolveService(
                    serviceInfo,
                    object : NsdManager.ResolveListener {
                        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                            Log.w(tag, "resolve fail $errorCode")
                        }

                        override fun onServiceResolved(resolved: NsdServiceInfo) {
                            val host = resolved.host?.hostAddress ?: return
                            val port = if (resolved.port > 0) resolved.port else Constants.CONTROL_PORT
                            var id = ""
                            var sdk = 0
                            if (Build.VERSION.SDK_INT >= 21) {
                                try {
                                    val attrs = resolved.attributes
                                    id = attrs["id"]?.let { String(it, Charsets.UTF_8) } ?: ""
                                    sdk = attrs["sdk"]?.let { String(it, Charsets.UTF_8) }?.toIntOrNull() ?: 0
                                } catch (_: Exception) {
                                }
                            }
                            emit(
                                listener,
                                TvDevice(
                                    host = host,
                                    port = port,
                                    name = resolved.serviceName ?: host,
                                    id = id,
                                    sdk = sdk,
                                ),
                            )
                        }
                    },
                )
            }
        }
        nsdListener = disc
        try {
            mgr.discoverServices(Constants.NSD_TYPE, NsdManager.PROTOCOL_DNS_SD, disc)
        } catch (e: Exception) {
            Log.w(tag, "nsd", e)
        }
    }

    private fun startUdp(listener: Listener) {
        Thread(
            {
                var multicast: WifiManager.MulticastLock? = null
                try {
                    val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                    multicast = wifi?.createMulticastLock("pocketremote-disc")
                    multicast?.setReferenceCounted(true)
                    multicast?.acquire()
                    val socket = DatagramSocket(Constants.UDP_PORT)
                    socket.broadcast = true
                    socket.soTimeout = 4000
                    udpSocket = socket
                    val magic = Constants.UDP_MAGIC.toByteArray(Charsets.US_ASCII)
                    val probe = ByteBuffer.allocate(10)
                    probe.put(magic)
                    probe.putShort(Constants.CONTROL_PORT.toShort())
                    val payload = probe.array()
                    val packet = DatagramPacket(
                        payload,
                        payload.size,
                        InetAddress.getByName("255.255.255.255"),
                        Constants.UDP_PORT,
                    )
                    socket.send(packet)
                    val buf = ByteArray(1024)
                    val deadline = System.currentTimeMillis() + 4000
                    while (System.currentTimeMillis() < deadline) {
                        try {
                            val incoming = DatagramPacket(buf, buf.size)
                            socket.receive(incoming)
                            if (incoming.length < 10) continue
                            val gotMagic = String(buf, 0, 8, Charsets.US_ASCII)
                            if (gotMagic != Constants.UDP_MAGIC) continue
                            val port = ((buf[8].toInt() and 0xff) shl 8) or (buf[9].toInt() and 0xff)
                            val name = if (incoming.length > 10) {
                                String(buf, 10, incoming.length - 10, Charsets.UTF_8)
                            } else {
                                incoming.address.hostAddress ?: ""
                            }
                            val host = incoming.address.hostAddress ?: continue
                            emit(listener, TvDevice(host = host, port = port, name = name.ifBlank { host }))
                        } catch (_: Exception) {
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.w(tag, "udp", e)
                } finally {
                    try {
                        multicast?.release()
                    } catch (_: Exception) {
                    }
                    listener.onFinished()
                }
            },
            "pocketremote-udp-disc",
        ).start()
    }

    private fun emit(listener: Listener, device: TvDevice) {
        val key = device.host + ":" + device.port
        if (seen.putIfAbsent(key, true) == null) {
            listener.onDevice(device)
        }
    }
}
