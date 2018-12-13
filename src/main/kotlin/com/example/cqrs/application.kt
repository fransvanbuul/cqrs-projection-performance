package com.example.cqrs

import org.axonframework.eventhandling.tokenstore.TokenStore
import org.axonframework.extensions.mongo.DefaultMongoTemplate
import org.axonframework.extensions.mongo.MongoTemplate
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore
import org.axonframework.serialization.Serializer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ProjectionNaiveApplication {

    @Bean
    fun axonMongoTemplate(mongoDbFactory: MongoDbFactory): MongoTemplate {
        return DefaultMongoTemplate.builder()
                .mongoDatabase(mongoDbFactory.db)
                .build()
    }

    @Bean
    fun tokenStore(axonMongoTemplate: MongoTemplate, serializer: Serializer): TokenStore {
        return MongoTokenStore.builder()
                .mongoTemplate(axonMongoTemplate)
                .serializer(serializer)
                .build()
    }

}

fun main(args: Array<String>) {
    runApplication<ProjectionNaiveApplication>(*args)
}
