package com.requests.domain.job

import com.requests.data.RequestApi
import com.requests.domain.RequestsMachine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

class RequestsJobFactory(
    private val requestApi: RequestApi,
    private val dispatcherIO: CoroutineDispatcher
) {

    fun create(
        resource: String,
        coroutineScope: CoroutineScope,
        failureListener: RequestsMachine.FailureListener
    ): RequestsJob =
        RequestsJob(requestApi, dispatcherIO, resource, coroutineScope, failureListener)
}