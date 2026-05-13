package com.deuna.maven.fraud

import android.content.Context
import com.deuna.maven.GenerateFraudId
import com.deuna.maven.shared.DeunaLogs
import com.deuna.maven.shared.Json
import com.lexisnexisrisk.threatmetrix.rl.TMXConfig
import com.lexisnexisrisk.threatmetrix.rl.TMXEndNotifier
import com.lexisnexisrisk.threatmetrix.rl.TMXProfiling
import com.lexisnexisrisk.threatmetrix.rl.TMXProfilingHandle
import com.lexisnexisrisk.threatmetrix.rl.TMXProfilingOptions
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal fun GenerateFraudId.runCybersource(config: Json, providerId: String) {
    val orgId = config["orgId"] as? String
    val merchantId = config["merchantId"] as? String
    val fpServer = config["fpServer"] as? String ?: "h.online-metrix.net"
    if (orgId.isNullOrBlank() || merchantId.isNullOrBlank()) {
        DeunaLogs.warning("[fraud] Missing CYBERSOURCE.orgId or merchantId. Skipping native init.")
        return
    }
    val sessionId = merchantId + providerId
    try {
        val profiled = CybersourceNativeBridge.profile(context, orgId, fpServer, sessionId)
        if (!profiled) {
            DeunaLogs.warning("[fraud] CYBERSOURCE profile did not return TMX_OK.")
        }
    } catch (error: Throwable) {
        DeunaLogs.warning("[fraud] CYBERSOURCE native profiling failed: ${error.message}")
    }
}

private object CybersourceNativeBridge {
    private val configurationLock = Any()
    private var configuredOrgId: String? = null
    private var configuredFpServer: String? = null

    fun profile(context: Context, orgId: String, fpServer: String, sessionId: String): Boolean {
        val profiling = TMXProfiling.getInstance()
        configureIfNeeded(context, profiling, orgId, fpServer)

        val options = TMXProfilingOptions()
            .setSessionID(sessionId)

        val latch = CountDownLatch(1)
        var profiled = false

        profiling.profile(options, object : TMXEndNotifier {
            override fun complete(result: TMXProfilingHandle.Result) {
                val statusName = result.status.name
                val returnedSessionId = result.sessionID
                DeunaLogs.info(
                    "[fraud] CYBERSOURCE profile status: $statusName, sessionId: $returnedSessionId"
                )
                profiled = statusName == "TMX_OK"
                latch.countDown()
            }
        })

        latch.await(8, TimeUnit.SECONDS)
        return profiled
    }

    private fun configureIfNeeded(
        context: Context,
        profiling: TMXProfiling,
        orgId: String,
        fpServer: String
    ) {
        synchronized(configurationLock) {
            val alreadyConfigured = configuredOrgId
            if (alreadyConfigured != null) {
                if (alreadyConfigured != orgId || configuredFpServer != fpServer) {
                    DeunaLogs.warning(
                        "[fraud] CYBERSOURCE already configured with orgId=$alreadyConfigured and fpServer=$configuredFpServer. Ignoring new orgId=$orgId fpServer=$fpServer."
                    )
                }
                return
            }

            val config = TMXConfig()
                .setContext(context)
                .setOrgId(orgId)
                .setFPServer(fpServer)
            profiling.init(config)
            configuredOrgId = orgId
            configuredFpServer = fpServer
        }
    }
}
