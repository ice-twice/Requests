package com.requests.domain.job

import com.requests.data.RequestApi
import com.requests.domain.RequestsMachine
import com.requests.domain.ResponseStatus
import kotlinx.coroutines.*

class RequestsJob(
    private val requestApi: RequestApi,
    private val dispatcherIO: CoroutineDispatcher,
    private val resource: String,
    val coroutineScope: CoroutineScope,
    val failureListener: RequestsMachine.FailureListener
) {
    private lateinit var job: Job
    val requestsStatusCounter = RequestsStatusCounter()

    fun start() {
        job = coroutineScope.launch(dispatcherIO) {
            while (isActive) {
                val result = requestApi.request(resource)
                if (result == ResponseStatus.SUCCESS) {
                    requestsStatusCounter.successfulRequestsCounter++
                    requestsStatusCounter.failureWithoutSuccessCounter = 0
                } else {
                    requestsStatusCounter.failureRequestsCounter++
                    requestsStatusCounter.failureWithoutSuccessCounter++
                    failureListener.onFail(resource)
                }
                delay(REQUEST_DELAY_MS)
            }
        }
    }

    fun cancel() = job.cancel()

    private companion object {
        private const val REQUEST_DELAY_MS = 1000L
    }
}