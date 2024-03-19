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

	// validation
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// security
	implementation("org.springframework.security:spring-security-core:6.1.7")

	// log
	implementation("io.github.oshai:kotlin-logging-jvm:5.1.1")

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
