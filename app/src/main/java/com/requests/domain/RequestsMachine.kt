package com.requests.domain

import com.requests.data.ResourceData
import com.requests.domain.job.RequestsJob
import com.requests.domain.job.RequestsJobFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.math.min

class RequestsMachine(
    private val resourceData: ResourceData,
    private val requestsJobFactory: RequestsJobFactory,
    private val defaultDispatcher: CoroutineDispatcher
) {
    private val resourceToJob = linkedMapOf<String, RequestsJob>()
    private val mutex = Mutex()
    private lateinit var resources: List<String>
    private var endIndex: Int = 0

    suspend fun start() = withContext(defaultDispatcher) {
        startInternal()
    }

    private suspend fun startInternal() {
        resources = resourceData.getResources().distinct()
        endIndex = min(resources.size, ACTIVE_RESOURCES_POOL_SIZE) - 1
        coroutineScope {
            mutex.withLock {
                val failureListener = FailureListener()
                for (i in 0..endIndex) {
                    val resource = resources[i]
                    createJobAndStart(resource, this, failureListener)
                }
            }
        }
    }

    private fun createJobAndStart(
        resource: String,
        coroutineScope: CoroutineScope,
        failureListener: FailureListener
    ) {
        val requestsJob = requestsJobFactory.create(resource, coroutineScope, failureListener)
        resourceToJob[resource] = requestsJob
        requestsJob.start()
    }

    suspend fun getRequestsStatus() = withContext(defaultDispatcher) {
        mutex.withLock {
            resourceToJob.mapValues { it.value.requestsStatusCounter.copy() }
        }
    }

    inner class FailureListener {

        suspend fun onFail(resource: String) {
            mutex.withLock {
                resourceToJob[resource]?.let { requestsJob ->
                    if (requestsJob.requestsStatusCounter.failureWithoutSuccessCounter == FAILED_REQUEST_THRESHOLD) {
                        requestsJob.cancel()
                        resourceToJob.remove(resource)

                        while (true) {
                            endIndex = if (endIndex + 1 >= resources.size) 0 else endIndex + 1
                            val nextResource = resources[endIndex]
                            if (!resourceToJob.contains(nextResource)) {
                                createJobAndStart(
                                    nextResource,
                                    requestsJob.coroutineScope,
                                    requestsJob.failureListener
                                )
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    private companion object {
        private const val ACTIVE_RESOURCES_POOL_SIZE = 10
        private const val FAILED_REQUEST_THRESHOLD = 100
    }
}