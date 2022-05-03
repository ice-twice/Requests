package com.requests.view

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.requests.R
import com.requests.domain.job.RequestsStatusCounter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RequestsActivity : AppCompatActivity() {
    private val requestsViewModel: RequestsViewModel by viewModels()
    private lateinit var resourceList: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resourceList = findViewById(R.id.list)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                requestsViewModel.uiState.collect { uiState ->
                    updateUiState(uiState)
                }
            }
        }
    }

    private fun updateUiState(uiState: RequestsViewModel.UiState) =
        uiState.requestsStatus?.let { requestsStatus ->
            resourceList.removeAllViews()
            for (status in requestsStatus) {
                val view = layoutInflater.inflate(R.layout.item, resourceList, false)
                resourceList.addView(view)
                val viewHolder = ViewHolder(view)
                viewHolder.bind(status)
            }
        }

    private class ViewHolder(view: View) {
        private val linkView = view.findViewById<AppCompatTextView>(R.id.link)!!
        private val successCountView = view.findViewById<AppCompatTextView>(R.id.success_count)!!
        private val failureCountView = view.findViewById<AppCompatTextView>(R.id.failure_count)!!

        fun bind(requestsStatusCounter: Map.Entry<String, RequestsStatusCounter>) {
            linkView.text = requestsStatusCounter.key
            successCountView.text = requestsStatusCounter.value.successfulRequestsCounter.toString()
            failureCountView.text = requestsStatusCounter.value.failureRequestsCounter.toString()
        }
    }
}