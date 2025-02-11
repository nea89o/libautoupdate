plugins {
	`maven-publish`
	signing
	java
	id("io.freefair.lombok") version "6.5.1"
}

group = "moe.nea"
version = "1.3.1"

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
	@Suppress("VulnerableLibrariesLocal")
	// We use this version of gson, because this is intended to be used for minecraft 1.8.9 (which bundles that gson version)
	implementation("com.google.code.gson:gson:2.2.4")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
	testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.1")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

java {
	withJavadocJar()
	withSourcesJar()
}

tasks.withType(Test::class) {
	useJUnitPlatform()
}

tasks.javadoc {
	isFailOnError = false
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
}
