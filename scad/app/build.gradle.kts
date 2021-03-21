
plugins {
   id("org.jetbrains.kotlin.jvm") version "1.4.20"
}

repositories {
   mavenCentral()
}

dependencies {
   implementation(project(":scadwriter"))
}

tasks.register<Exec>("generateAllScads") {
   dependsOn("jar")

   commandLine(
      "kotlin",
      "-classpath", tasks.jar.get().archiveFile.get(),
      "com.wcaokaze.ninja60.scadgenerator.MainKt",
      "--output-file", File(buildDir, "test.scad")
   )
}

tasks.register<Exec>("generateAllStls") {
   dependsOn("generateAllScads")

   commandLine(
      "openscad",
      "-o", File(buildDir, "test.stl"),
      File(buildDir, "test.scad")
   )
}
