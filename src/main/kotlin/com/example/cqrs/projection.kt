package com.example.cqrs

import mu.KLogging
import org.axonframework.config.EventProcessingConfiguration
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.TrackingEventProcessor
import org.springframework.context.annotation.Profile
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.util.*

@Document
@Profile("projector")
data class X(
    @Id var id: UUID,
    var amount: BigInteger
)

@Profile("projector")
interface XRepository : MongoRepository<X, UUID> {}

@Component
@Profile("projector")
class TepMonitor(val eventProcessingConfiguration: EventProcessingConfiguration) {

    companion object : KLogging()

    @Scheduled(fixedRate=10000)
    fun report() {
        val tep = eventProcessingConfiguration.eventProcessor<TrackingEventProcessor>("projector").get()
        tep.processingStatus().values.forEach {
            logger.info { "segment ${it.segment}   token ${it.trackingToken}"}
        }
    }
}

@Component
@Profile("projector")
@ProcessingGroup("projector")
class Projector(
        val xRepository: XRepository
) {

    companion object : KLogging()

    @EventHandler
    fun on(evt: CreatedEvent) {
        logger.debug { "$evt" }
        val x = X(evt.id, BigInteger.ZERO)
        xRepository.insert(x)
    }

    @EventHandler
    fun on(evt: ChangedEvent) {
        logger.debug { "$evt" }
        val x = xRepository.findById(evt.id).get()
        x.amount = x.amount + evt.change.toBigInteger()
        xRepository.save(x)
    }

    @EventHandler
    fun on(evt: DeletedEvent) {
        logger.debug { "$evt" }
        xRepository.deleteById(evt.id)
    }

}