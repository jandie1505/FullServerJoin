plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.5"
}

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
    implementation("net.chaossquad:mclib:master-68bff2f911a10022c235778900f31d5a425bd1ca")
}

java {}

tasks {
    shadowJar {
        relocate("net.chaossquad.mclib", "net.jandie1505.fullserverjoin.dependencies.net.chaossquad.mclib")
    }
    build {
        dependsOn(shadowJar)
    }
}
