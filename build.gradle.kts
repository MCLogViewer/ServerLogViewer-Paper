plugins {
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "9.0.2"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
}

group = "fish.crafting"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
    implementation(kotlin("stdlib"))

    compileOnly("net.luckperms:api:5.4")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
tasks {
    shadowJar {
        relocate("kotlin", "fish.crafting.libs.kotlin") // optional, avoids conflicts
    }
}