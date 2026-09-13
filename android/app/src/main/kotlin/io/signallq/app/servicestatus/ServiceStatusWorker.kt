package io.signallq.app.servicestatus

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.signallq.app.notificacao.SignallQNotificationHelper
import java.util.concurrent.TimeUnit

@HiltWorker
class ServiceStatusWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted params: WorkerParameters,
        private val repository: ServiceStatusRepository,
    ) : CoroutineWorker(appContext, params) {
        override suspend fun doWork(): Result {
            repository.incidentesParaNotificar().forEach { (service, incident) ->
                SignallQNotificationHelper.notificarServicoIndisponivel(
                    applicationContext,
                    service.id,
                    service.nome,
                    incident.summary,
                )
            }
            return Result.success()
        }
    }

internal object ServiceStatusScheduler {
    private const val TAG = "service_status_alerts"

    fun atualizarAgendamento(
        context: Context,
        ativo: Boolean,
    ) {
        if (!ativo) {
            WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
            return
        }
        val request =
            PeriodicWorkRequestBuilder<ServiceStatusWorker>(30, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
                .addTag(TAG)
                .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.UPDATE, request)
    }
}
