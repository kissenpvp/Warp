plugins {
    id("java")
    `maven-publish`
}

group = "net.kissenpvp"
version = "2.1.3-SNAPSHOT"

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

    compileOnly("net.kissenpvp.pulvinar:pulvinar-api:1.20.6-R0.1-20240523.164127-1")
    compileOnly("net.kissenpvp:VisualAPI:1.6.1-20240525.145651-1")
}

publishing {
    repositories {
        maven("https://repo.kissenpvp.net/repository/maven-snapshots/") {
            name = "kissenpvp"
            credentials(PasswordCredentials::class)
        }
    }

    publications.create<MavenPublication>(project.name) {
        artifact("build/libs/${project.name}-${project.version}.jar")
    }
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
