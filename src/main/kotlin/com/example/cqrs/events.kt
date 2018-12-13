package com.example.cqrs

import java.util.*

data class CreatedEvent(val id: UUID)
data class ChangedEvent(val id: UUID, val change: Int)
data class DeletedEvent(val id: UUID)
