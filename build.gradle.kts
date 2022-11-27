import nu.studer.gradle.jooq.JooqGenerate
import org.jooq.meta.jaxb.Property
import org.jooq.meta.jaxb.Strategy

plugins {
    id("java")
    id("nu.studer.jooq") version "8.0"
    id("info.solidsoft.pitest") version "1.9.0"
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
    testAnnotationProcessor("com.google.dagger:dagger-compiler:2.44.2")
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
    implementation("io.vertx:vertx-web-client:4.3.5")
    testImplementation("io.vertx:vertx-junit5:4.3.5")

    // testing
    testImplementation("org.hamcrest:java-hamcrest:2.0.0.0")
    testImplementation("com.approvaltests:approvaltests:18.5.0")

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

    // config
    implementation("org.apache.commons:commons-configuration2:2.8.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--enable-preview")
}

tasks.withType<JavaCompile>() {
    dependsOn(tasks.withType(JooqGenerate::class))
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview")
}

jooq {
    configurations {
        create("Main") {
            jooqConfiguration.apply {
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
    inputs.files("dbchangelog.xml")
    allInputsDeclared.set(true)
}

pitest {
    excludedClasses.set(listOf("*_*Factory", "*Module", "*Dagger*_*"))
    threads.set(Runtime.getRuntime().availableProcessors())
    outputFormats.set(listOf("HTML"))
    junit5PluginVersion.set("1.0.0")
}