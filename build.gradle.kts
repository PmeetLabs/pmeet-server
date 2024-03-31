import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.springframework.boot") version "3.2.3"
  id("io.spring.dependency-management") version "1.1.4"
  kotlin("jvm") version "1.9.22"
  kotlin("plugin.spring") version "1.9.22"
}

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
  testImplementation("org.springframework.boot:spring-security-test")

  // validation
  implementation("org.springframework.boot:spring-boot-starter-validation")

  // security
  implementation("org.springframework.boot:spring-boot-starter-security")

  implementation ("io.jsonwebtoken:jjwt-api:0.12.3")
  runtimeOnly ("io.jsonwebtoken:jjwt-impl:0.12.3")
  runtimeOnly ("io.jsonwebtoken:jjwt-jackson:0.12.3")

  // log
  implementation("io.github.oshai:kotlin-logging-jvm:5.1.1")

  // redis
  implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

  // SMTP
  implementation("org.springframework.boot:spring-boot-starter-mail")
  implementation("io.netty:netty-resolver-dns-native-macos:4.1.106.Final:osx-aarch_64")

  // oauth
  implementation("com.auth0:java-jwt:3.18.1")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs += "-Xjsr305=strict"
    jvmTarget = "21"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
