package com.example.cqrs

import com.mongodb.MongoClient
import mu.KotlinLogging
import org.axonframework.eventhandling.tokenstore.TokenStore
import org.axonframework.extensions.mongo.DefaultMongoTemplate
import org.axonframework.extensions.mongo.MongoTemplate
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore
import org.axonframework.serialization.Serializer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableScheduling
class ProjectionNaiveApplication {

    @Bean
    fun axonMongoTemplate(mongoClient: MongoClient): MongoTemplate {
        return DefaultMongoTemplate.builder()
                .mongoDatabase(mongoClient, "test")
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
    val logger = KotlinLogging.logger {}
    val applicationContext = runApplication<ProjectionNaiveApplication>(*args)
    applicationContext.beanDefinitionNames.forEach {
        val beanClassName = applicationContext.getBean(it).javaClass.name
        logger.debug { "bean <$it> of class <$beanClassName>" }
    }
}
