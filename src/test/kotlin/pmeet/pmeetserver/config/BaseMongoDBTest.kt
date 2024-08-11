package pmeet.pmeetserver.config

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@DataMongoTest
@Testcontainers
abstract class BaseMongoDBTest(body: DescribeSpec.() -> Unit = {}) : DescribeSpec(body) {
    companion object {
        @Container
        val mongoDBContainer = MongoDBContainer("mongo:latest").apply {
            withExposedPorts(27017)
            start()
        }

        init {
            System.setProperty(
                "spring.data.mongodb.uri",
                "mongodb://localhost:${mongoDBContainer.getMappedPort(27017)}/test"
            )
        }
    }

    init {
        isolationMode = IsolationMode.InstancePerLeaf
    }
}
