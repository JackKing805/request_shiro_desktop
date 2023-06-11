import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    application
    `maven-publish`
}

val vv = "0.0.1"

group = "com.cool"
version = vv

repositories {
    mavenCentral()
    maven{
        setUrl("https://jitpack.io")
    }
}

dependencies {
    testImplementation(kotlin("test"))


    compileOnly("com.github.JackKing805:RtCore:0.7.4")
    compileOnly("com.github.JackKing805:request_core_desktop:0.0.4")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

publishing{
    publications{
        create("maven_public",MavenPublication::class){
            groupId = "com.cool"
            artifactId = "RequestShiroDesktop"
            version = vv
            from(components.getByName("java"))
        }
    }
}
