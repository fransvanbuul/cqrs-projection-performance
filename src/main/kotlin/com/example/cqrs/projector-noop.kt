package com.example.cqrs

import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import org.axonframework.config.EventProcessingConfigurer
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration
import org.axonframework.messaging.unitofwork.BatchingUnitOfWork
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Configuration
@Profile("projector-noop")
class NoopConfiguration(
        @Value("\${projector.thread-count}") val threadCount: Int,
        @Value("\${projector.batch-size}") val batchSize: Int
) {

    @Autowired
    fun configure(eventProcessorConfigurer: EventProcessingConfigurer) {
        eventProcessorConfigurer.registerTrackingEventProcessor("projector-noop-group", { it.eventStore() }) {
            TrackingEventProcessorConfiguration
                    .forParallelProcessing(threadCount)
                    .andBatchSize(batchSize)
        }
    }

}

@Component
@Profile("projector-noop")
@ProcessingGroup("projector-noop-group")
class ProjectorNoop(
        metricRegistry: MetricRegistry,
        @Value("\${projector.batch-size}") val batchSize: Long
) {

    private val meter = metricRegistry.meter("projector-noop")

    private fun mark(uow: UnitOfWork<*>) {
        uow.getOrComputeResource<String>("dummy") {
            uow.afterCommit { meter.mark(batchSize) }
            "dummy"
        }
    }

    @EventHandler
    fun on(evt: AccountCreated, uow: UnitOfWork<*>) {
        mark(uow)
    }

    @EventHandler
    fun on(evt: AccountCredited, uow: UnitOfWork<*>) {
        mark(uow)
    }

    @EventHandler
    fun on(evt: AccountDebited, uow: UnitOfWork<*>) {
        mark(uow)
    }

    @EventHandler
    fun on(evt: AccountCancelled, uow: UnitOfWork<*>) {
        mark(uow)
    }

}