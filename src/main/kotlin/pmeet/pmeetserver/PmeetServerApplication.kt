package pmeet.pmeetserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PmeetServerApplication

fun main(args: Array<String>) {
  runApplication<PmeetServerApplication>(*args)
}
