plugins {
    kotlin("jvm") version "1.6.21"
    java
    `maven-publish`
    signing
}



allprojects {
    group = "org.pdgen"
    version = "2.1.4-SNAPSHOT"

    apply {
        plugin("java")
    }
    repositories {
        mavenCentral()
    }
    dependencies {
        testImplementation("org.testng:testng:7.6.1")
    }
    tasks.getByName<Test>("test") {
        useTestNG()
    }
    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        withJavadocJar()
        withSourcesJar()
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}

dependencies {
    api(project(":pdgenannotations"))
    implementation("net.sf.barcode4j:barcode4j:2.1")
    implementation("org.ow2.asm:asm:9.4")
    implementation("joda-time:joda-time:2.12.2")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            version = project.version.toString()//"2.1.0-SNAPSHOT"
            artifactId = "pdgenruntime"

            from(components["java"])
            pom {
                name.set("Pdgen Runtime")
                description.set("An engine to generate printable documents programmatically")
                url.set("http://pdgen.org/")
                licenses {
                    license {
                        name.set("GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
                        url.set("https://www.gnu.org/licenses/agpl-3.0.html")
                    }
                }
                developers {
                    developer {
                        id.set("hmf")
                        name.set("Hans Marggraff")
                        email.set("hmf@qint.de")
                    }
                }
                scm {
                    connection.set("git@github.com:hmarggraff/pdgenruntime.git")
                    developerConnection.set("git@github.com:hmarggraff/pdgenruntime.git")
                    url.set("https://github.com/hmarggraff/pdgenruntime")
                }

            }
        }
        repositories {
            maven {
                // change URLs to point to your repos, e.g. http://my.org/repo
                val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
                val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                credentials {
                    /* This will load the properties from gradle.properties.
                        These should be defined in users local gradle.properties,
                        by users, that have the right to publish to maven central.
                        For others publishing will fail due to missing credentials
                    */
                    username = rootProject.findProperty("sonatypeUsername") as String?
                    password = rootProject.findProperty("sonatypePassword") as String?
                }

            }
        }

    }
    signing {
        sign(publishing.publications["maven"])
    }

}


