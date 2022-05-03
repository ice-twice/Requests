package com.requests.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.requests.data.RequestApi
import com.requests.data.ResourceData
import com.requests.domain.RequestsMachine
import com.requests.domain.job.RequestsJobFactory
import com.requests.domain.job.RequestsStatusCounter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RequestsViewModel : ViewModel() {
    val uiState = MutableStateFlow(UiState())

    // TODO inject
    private val requestMachine by lazy {
        RequestsMachine(
            ResourceData(),
            RequestsJobFactory(RequestApi(), Dispatchers.IO),
            Dispatchers.Default
        )
    }

    init {
        viewModelScope.launch {
            requestMachine.start()
        }
        viewModelScope.launch {
            while (true) {
                delay(1000)
                uiState.value =
                    uiState.value.copy(requestsStatus = requestMachine.getRequestsStatus())
            }
        }
    }

    data class UiState(var requestsStatus: Map<String, RequestsStatusCounter>? = null)
}