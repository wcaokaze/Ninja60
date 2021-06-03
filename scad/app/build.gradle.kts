
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

   val classPath = HashSet<File>()
   classPath += tasks.jar.get().archiveFile.get().asFile
   classPath += configurations.implementationDependenciesMetadata.get().files

   commandLine(
      "java",
      "--class-path", classPath.joinToString(separator = ":"),
      "com.wcaokaze.ninja60.MainKt",
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
