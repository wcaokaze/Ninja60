
plugins {
   kotlin("jvm")
}

repositories {
   mavenCentral()
}

dependencies {
   implementation(project(":scadwriter"))
   implementation(project(":linearalgebra"))
   implementation(project(":app:shared"))
}
