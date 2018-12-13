package com.example.cqrs

import mu.KLogging
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.GenericEventMessage
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.roundToInt

@Component
@Profile("loader")
class Loader(
        val eventBus: EventBus,
        @Value("\${cqrs.loader.events-per-thread}") val eventsPerThread: Int,
        @Value("\${cqrs.loader.thread-count}") val threadCount: Int,
        @Value("\${cqrs.loader.ids-per-thread}") val idsPerThread: Int
): CommandLineRunner {

    companion object : KLogging()

    override fun run(vararg args: String?) {
        Thread { runAsync() }.start()
    }

    private fun runAsync() {
        val threads: Array<Thread?> = arrayOfNulls(threadCount)
        for(i in 0 until threads.size) threads[i] = thread { singleThread() }
        val start = System.currentTimeMillis()
        for(i in 0 until threads.size) threads[i]?.join()
        val end = System.currentTimeMillis()
        val events = threadCount * eventsPerThread
        val delta = end - start
        val rate = (1000f * events.toFloat() / delta.toFloat()).roundToInt()
        logger.debug { "$events events in $delta ms = $rate EPS" }
    }


    private fun singleThread() {
        val ids: Array<UUID?> = arrayOfNulls(idsPerThread)
        val rng = SecureRandom()
        for(i in 0 until eventsPerThread) {
            val event = randomEvent(ids, rng)
            eventBus.publish(GenericEventMessage.asEventMessage<Any>(event))
        }
    }

    private fun randomEvent(ids: Array<UUID?>, rng: Random): Any {
        val index = rng.nextInt(ids.size)
        val id = ids[index]
        if(id == null) {
            val id = UUID.randomUUID()
            ids[index] = id
            return CreatedEvent(id)
        } else {
            val dice = rng.nextFloat()
            if(dice < 0.8f) {
                val amount = rng.nextInt(200) - 100;
                return ChangedEvent(id, amount)
            } else {
                ids[index] = null
                return DeletedEvent(id)
            }
        }
    }

}