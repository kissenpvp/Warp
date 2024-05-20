plugins {
    id("java")
}

group = "net.kissenpvp"
version = "2.1.2"

configurations {
    create("includeLib")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.kissenpvp.net/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    compileOnly("net.kissenpvp.paper:kissenpaper-api:1.20.6-R0.1-20240520.100459-6")
    implementation("net.kissenpvp:VisualAPI:1.5.3-20240520.105955-1")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"

    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.test {
    useJUnitPlatform()
}
