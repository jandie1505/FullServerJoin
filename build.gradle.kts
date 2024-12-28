plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.5"
}

group = "net.jandie1505"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven {
        name = "chaossquad"
        url = uri("https://maven.chaossquad.net/snapshots")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("net.chaossquad:mclib:master-71951f62e2e9e0a4c73b4e6e67e7c1fcf87deea8")
}

java {}

tasks {
    shadowJar {
        relocate("net.chaossquad.mclib", "net.jandie1505.joinmanager.dependencies.net.chaossquad.mclib")
    }
    build {
        dependsOn(shadowJar)
    }
}
