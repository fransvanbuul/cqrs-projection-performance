package com.example.cqrs

import com.codahale.metrics.MetricRegistry
import org.axonframework.eventhandling.EventHandler
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
@Profile("projector-naive")
class ProjectorNaive(
        val accountSummaryRepository: AccountSummaryRepository,
        metricRegistry: MetricRegistry
) {

    private val meter = metricRegistry.meter("projector-naive")

    @EventHandler
    fun on(evt: AccountCreated) {
        val accountSummary = AccountSummary(evt.accountId, BigDecimal.ZERO.setScale(2))
        accountSummaryRepository.insert(accountSummary)
        meter.mark()
    }

    @EventHandler
    fun on(evt: AccountCredited) {
        val accountSummary = accountSummaryRepository.findById(evt.accountId).get()
        accountSummary.balance += evt.amount
        accountSummaryRepository.save(accountSummary)
        meter.mark()
    }

    @EventHandler
    fun on(evt: AccountDebited) {
        val accountSummary = accountSummaryRepository.findById(evt.accountId).get()
        accountSummary.balance -= evt.amount
        accountSummaryRepository.save(accountSummary)
        meter.mark()
    }

    @EventHandler
    fun on(evt: AccountCancelled) {
        accountSummaryRepository.deleteById(evt.accountId)
        meter.mark()
    }

}