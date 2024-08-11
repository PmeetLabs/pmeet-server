package pmeet.pmeetserver.user.job.repository

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.DescribeSpec
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Container

@DataMongoTest
class MongoDBTestContainer() : DescribeSpec({

    isolationMode = IsolationMode.InstancePerLeaf

}) {
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

}

