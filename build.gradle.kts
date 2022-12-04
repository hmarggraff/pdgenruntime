plugins {
    kotlin("jvm") version "1.6.21"
    java
}


allprojects {
    apply {
        plugin("java")
    }
    repositories {
        mavenCentral()
    }
    dependencies {
        testImplementation("org.testng:testng:7.5")
    }
    tasks.getByName<Test>("test") {
        useTestNG()
    }
    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
    /*
    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint")
    }
    */
}

dependencies {
    implementation(project(":pdgenannotations"))
	implementation("net.sf.barcode4j:barcode4j:2.1")

}
