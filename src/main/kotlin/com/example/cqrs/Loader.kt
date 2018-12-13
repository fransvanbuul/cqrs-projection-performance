package com.example.cqrs

import com.codahale.metrics.MetricRegistry
import mu.KLogging
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.GenericEventMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.SecureRandom
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.roundToInt

@Component
@Profile("loader")
class Loader(
        val eventBus: EventBus,
        @Value("\${loader.events-per-thread}") val eventsPerThread: Int,
        @Value("\${loader.thread-count}") val threadCount: Int,
        @Value("\${loader.ids-per-thread}") val idsPerThread: Int,
        metricRegistry: MetricRegistry
): CommandLineRunner {

    companion object : KLogging()

    private val meter = metricRegistry.meter("loader")

    override fun run(vararg args: String?) {
        List(threadCount) { thread { singleThread() } }.forEach { it.join() }
    }

    private fun singleThread() {
        val ids: Array<UUID?> = arrayOfNulls(idsPerThread)
        val rng = SecureRandom()
        for(i in 0 until eventsPerThread) {
            val event = randomEvent(ids, rng)
            eventBus.publish(GenericEventMessage.asEventMessage<Any>(event))
            meter.mark()
        }
    }

    private fun randomEvent(ids: Array<UUID?>, rng: Random): Any {
        val index = rng.nextInt(ids.size)
        val id = ids[index]
        if(id == null) {
            val newId = UUID.randomUUID()
            ids[index] = newId
            return AccountCreated(newId )
        } else {
            val dice = rng.nextFloat()
            if(dice < 0.5f) {
                val amount = BigDecimal(rng.nextDouble()*100).setScale(2, RoundingMode.HALF_UP)
                return AccountCredited(id, amount)
            } else if(dice < 0.8f) {
                val amount = BigDecimal(rng.nextDouble()*100).setScale(2, RoundingMode.HALF_UP)
                return AccountDebited(id, amount)
            } else {
                ids[index] = null
                return AccountCancelled(id)
            }
        }
    }

}