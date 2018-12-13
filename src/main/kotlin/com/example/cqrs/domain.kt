package com.example.cqrs

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import java.math.BigDecimal
import java.util.*

/* Events */

data class AccountCreated(val accountId: UUID)
data class AccountCredited(val accountId: UUID, val amount: BigDecimal)
data class AccountDebited(val accountId: UUID, val amount: BigDecimal)
data class AccountCancelled(val accountId: UUID)

/* Projection */

@Document
data class AccountSummary(
        @Id var accountId: UUID,
        var balance: BigDecimal
)

interface AccountSummaryRepository : MongoRepository<AccountSummary, UUID> {}
