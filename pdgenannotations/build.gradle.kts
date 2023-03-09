plugins {
    java
    `maven-publish`
    signing
}

java {
    withJavadocJar()
    withSourcesJar()

}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            version = project.version.toString()
            artifactId = "pdgenannotations"

            from(components["java"])
            pom {
                name.set("Pdgen Runtime")
                description.set("An engine to generate printable (e.g pdf) documents programatically")
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


}
signing {
    sign(publishing.publications["maven"])
}


