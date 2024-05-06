import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
  id("org.springframework.boot") version "3.2.3"
  id("io.spring.dependency-management") version "1.1.4"
  kotlin("jvm") version "1.9.22"
  kotlin("plugin.spring") version "1.9.22"
}

val kotestVersion = "5.8.1"
val mockVersion = "1.13.10"
val awsVersion = "2.25.23"
val springMockkVersion = "4.0.2"
val kotestSpringExtensionVersion = "1.1.3"
val testContainerVersion = "1.19.7"
group = "Pmeet"
version = "0.0.1-SNAPSHOT"

java {
  sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
  mavenCentral()
}

dependencies {
  // mongodb
  implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

  // spring webflux
  implementation("org.springframework.boot:spring-boot-starter-webflux")

  // kotlin coroutines
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

  // swagger
  implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.0.4")

  // test
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.projectreactor:reactor-test")
  testImplementation("org.springframework.security:spring-security-test")

  testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")

  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
  testImplementation("io.mockk:mockk:$mockVersion")
  testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
  testImplementation("io.kotest.extensions:kotest-extensions-spring:$kotestSpringExtensionVersion")


  testImplementation("org.testcontainers:testcontainers:$testContainerVersion")
  testImplementation("org.testcontainers:junit-jupiter:$testContainerVersion")
  testImplementation("org.testcontainers:mongodb:$testContainerVersion")

  // validation
  implementation("org.springframework.boot:spring-boot-starter-validation")

  // security
  implementation("org.springframework.boot:spring-boot-starter-security")

  implementation("io.jsonwebtoken:jjwt-api:0.12.3")
  runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
  runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

  // log
  implementation("io.github.oshai:kotlin-logging-jvm:5.1.1")

  // redis
  implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

  // smtp
  implementation("org.springframework.boot:spring-boot-starter-mail")
  implementation("io.netty:netty-resolver-dns-native-macos:4.1.106.Final:osx-aarch_64")

  // oauth
  implementation("com.auth0:java-jwt:3.18.1")

  // aws
  implementation("software.amazon.awssdk:s3:$awsVersion")

}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs += "-Xjsr305=strict"
    jvmTarget = "21"
  }
}

tasks.withType<Test> {
  jvmArgs("-XX:+EnableDynamicAgentLoading")
}

tasks.withType<Test> {
  useJUnitPlatform()
}
