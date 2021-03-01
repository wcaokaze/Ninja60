
plugins {
   id("org.jetbrains.kotlin.jvm") version "1.4.20"
   application
}

repositories {
   mavenCentral()
}

dependencies {
}

application {
   mainClass.set("com.wcaokaze.ninja60.scadgenerator.MainKt")
}

