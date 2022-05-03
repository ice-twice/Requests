package com.requests.data

class ResourceData {

    fun getResources() = generateSequence({ 0 }, { it + 1 })
        .take(20)
        .map {
            "https://test$it.com/"
        }
        .toList()
}