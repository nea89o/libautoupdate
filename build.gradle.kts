plugins {
    `maven-publish`
    signing
    java
}

group = "moe.nea"
version = "0.1.0"

allprojects {
    apply(plugin = "java")
    tasks.withType(JavaCompile::class) {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
    repositories {
        mavenCentral()
    }
}

project(":updater") {
    tasks.jar {
        archiveFileName.set("updater.jar")
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to "moe.nea.libautoupdate.postexit.PostExitMain"
                )
            )
        }
    }
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("com.google.code.gson:gson:2.2.4")
}


tasks.processResources {
    val updateJar = tasks.getByPath(":updater:jar")
    from(updateJar.outputs)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                licenses {
                    license {
                        name.set("BSD-2-Clause")
                    }
                }
                developers {
                    developer {
                        name.set("Linnea Gräf")
                    }
                }
                scm {
                    url.set("https://git.nea.moe/nea/libautoupdate")
                }
            }
        }
    }
    repositories {
        maven("https://repo.nea.moe/releases") {
            name = "neamoeReleases"
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}
