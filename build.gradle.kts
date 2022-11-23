import nu.studer.gradle.jooq.JooqGenerate
import org.jooq.meta.jaxb.Property
import org.jooq.meta.jaxb.Strategy

plugins {
    id("java")
    id("nu.studer.jooq") version "8.0"
    application
}

group = "io.github.marcusdunn"

application {
    mainClass.set("io.github.marcusdunn.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    // jooq
    implementation("org.jooq:jooq:3.17.5")
    jooqGenerator("org.jooq:jooq-meta-extensions-liquibase")
    jooqGenerator("org.liquibase:liquibase-core:4.17.2")
    jooqGenerator("org.postgresql:postgresql:42.5.0")
    jooqGenerator(files("src/main/resources"))

    // dagger
    annotationProcessor("com.google.dagger:dagger-compiler:2.44.2")
    implementation("com.google.dagger:dagger:2.44.2")

    // postgres driver
    implementation("io.r2dbc:r2dbc-postgresql:0.8.13.RELEASE")

    // liquibase
    implementation("org.liquibase:liquibase-core:4.17.2")
    implementation("org.postgresql:postgresql:42.5.0") // for running liquibase transactions only

    // vertx
    implementation("io.vertx:vertx-web:4.3.5")
    implementation("io.vertx:vertx-auth-jwt:4.3.5")
    implementation("io.vertx:vertx-web-openapi:4.3.5")
    implementation("io.vertx:vertx-reactive-streams:4.3.5")
    testImplementation("io.vertx:vertx-web-client:4.3.5")
    testImplementation("io.vertx:vertx-junit5:4.3.5")
    testImplementation("io.vertx:vertx-junit5-web-client:4.0.0-milestone4")

    // reactor
    implementation("io.projectreactor:reactor-core:3.5.0")
    testImplementation("io.projectreactor:reactor-test:3.5.0")

    // testcontainers
    testImplementation("org.testcontainers:testcontainers:1.17.6")
    testImplementation("org.testcontainers:junit-jupiter:1.17.6")
    testImplementation("org.testcontainers:postgresql:1.17.6")
    testImplementation("org.testcontainers:r2dbc:1.17.6")

    // junit
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

jooq {
    configurations {
        create("Main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/realworld"
                    user = "realworld"
                    password = "realworld"
                }
                generator.apply {
                    strategy = Strategy().withName("org.jooq.codegen.example.JPrefixGeneratorStrategy")

                    database.apply {
                        name = "org.jooq.meta.extensions.liquibase.LiquibaseDatabase"
                        properties.add(Property().withKey("scripts").withValue("dbchangelog.xml"))
                    }
                }
            }
        }
    }
}

sourceSets {
    main {
        java {
            srcDir("${buildDir.absolutePath}/generated-src")
        }
    }
}

tasks.withType<JooqGenerate>() {
    allInputsDeclared.set(true)
}