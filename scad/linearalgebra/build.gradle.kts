
plugins {
   kotlin("jvm") version "1.5.30"
}

repositories {
   mavenCentral()
}

dependencies {
   implementation(project(":scadwriter"))
}
