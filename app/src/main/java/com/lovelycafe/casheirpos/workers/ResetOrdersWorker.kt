package com.lovelycafe.casheirpos.workers
/**
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.lovelycafe.casheirpos.api.RetrofitEvent
import com.lovelycafe.casheirpos.api.ResetOrdersResponse
import retrofit2.Response

class ResetOrdersWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        return try {
            // Get the EventApi instance from RetrofitEvent
            val eventApi = RetrofitEvent.instance

            // Make the network request
            val response: Response<ResetOrdersResponse> = eventApi.resetAutoIncrementOrders().execute()

            if (response.isSuccessful && response.body()?.success == true) {
                // Successfully reset auto increment orders
                Result.success()
            } else {
                // Failure case: The request was not successful
                Result.failure()
            }
        } catch (e: Exception) {
            // If there's an error during the network call
            e.printStackTrace()
            Result.failure()
        }
    }
}

 **/