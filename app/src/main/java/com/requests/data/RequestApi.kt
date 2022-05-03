package com.requests.data

import com.requests.domain.ResponseStatus
import org.jsoup.Jsoup

class RequestApi {
    fun request(resource: String): ResponseStatus {
        val connection = Jsoup.connect(resource)
        connection.timeout(CONNECTION_TIMEOUT_MS)
        val result = kotlin.runCatching { connection.get() }
        return if (result.isSuccess) ResponseStatus.SUCCESS else ResponseStatus.FAILURE
    }

    private companion object {
        private const val CONNECTION_TIMEOUT_MS = 500
    }
}