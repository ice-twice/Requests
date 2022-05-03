package com.requests.domain.job

data class RequestsStatusCounter(
    var successfulRequestsCounter: Int = 0,
    var failureRequestsCounter: Int = 0,
    var failureWithoutSuccessCounter: Int = 0
)