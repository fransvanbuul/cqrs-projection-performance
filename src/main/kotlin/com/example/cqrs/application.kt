package com.example.cqrs

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Slf4jReporter
import org.axonframework.eventhandling.tokenstore.TokenStore
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore
import org.axonframework.extensions.mongo.DefaultMongoTemplate
import org.axonframework.extensions.mongo.MongoTemplate
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore
import org.axonframework.serialization.Serializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.scheduling.annotation.EnableScheduling
import java.util.concurrent.TimeUnit

@SpringBootApplication
@EnableScheduling
class ProjectionBenchmarkApplication {

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

    @Bean
    fun metricRegistry(): MetricRegistry {
        return MetricRegistry()
    }

    @Autowired
    fun configure(metricRegistry: MetricRegistry) {
        Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger("com.example.cqrs.metrics"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
                .start(10, TimeUnit.SECONDS)
    }

}

fun main(args: Array<String>) {
    runApplication<ProjectionBenchmarkApplication>(*args)
}
