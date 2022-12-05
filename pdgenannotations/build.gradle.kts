plugins {
    java
    `maven-publish`
    signing
}

java {
    withJavadocJar()
    withSourcesJar()

}

//version = "2.1"

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.pdgen"
            artifactId = "pdgenannotations"
            version = "2.1.0-SNAPSHOT"

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
                    // this code is untested. Need to check if username/password are properly retrieved from ~/.gradle/gradle.properties
                    val sonatypeUsername: String by project
                    val sonatypePassword: String by project
                    username = sonatypeUsername
                    password = sonatypePassword
                }
            }
        }
    }


}
signing {
    sign(publishing.publications["maven"])
}


