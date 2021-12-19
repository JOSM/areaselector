import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import net.ltgt.gradle.errorprone.*
import org.openstreetmap.josm.gradle.plugin.task.MarkdownToHtml
import java.net.URL

plugins {
    id("com.github.ben-manes.versions") version "0.39.0"
    id("org.openstreetmap.josm") version "0.7.1"
    id("com.github.spotbugs") version "4.7.9"
    id("net.ltgt.errorprone") version "2.0.2"
    java
    pmd
    `maven-publish`
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
base.archivesBaseName = "areaselector"

val versions = mapOf(
    "austriaaddresshelper" to "v0.8.0",
    // Note: boofcv 0.39.1 is NOT Java 8 compatible
    "boofcv" to "0.39",
    "ddogleg" to "0.20",
    "errorprone" to "2.10.0",
    "junit" to "5.8.2",
    "pmd" to "6.18.0",
    "spotbugs" to "4.5.2",
    "xmlpull" to "1.1.3.1",
    "xpp" to "1.1.6",
    "xstream" to "1.4.18"
)

repositories {
    jcenter()
    mavenCentral()
    // Note: This may become unnecessary in future josm gradle plugin versions.
    // See https://gitlab.com/floscher/gradle-josm-plugin/-/merge_requests/9
    ivy {
        url = uri("https://github.com/JOSM/austriaaddresshelper/releases/download/")
        content {
            includeModule("org.openstreetmap.josm.plugins", "austriaaddresshelper")
        }
        patternLayout {
            artifact("/${versions["austriaaddresshelper"]}/[artifact].[ext]")
            //ivy("${versions["austriaaddresshelper"]}/ivy.xml")
        }
        metadataSources {
            artifact()
        }
    }
    flatDir {
        dirs("lib")
    }
}

sourceSets {
    create("libs") {
        java {
            srcDir("src").include(listOf("boofcv/**", "org/marvinproject/**"))
        }
    }
    main {
        java {
            srcDir("src").include(listOf("org/openstreetmap/**"))
            compileClasspath += sourceSets["libs"].output
            runtimeClasspath += sourceSets["libs"].output
        }
        resources {
            srcDir(project.projectDir).exclude(listOf("resources/**", "src/**")).include(listOf("images/**", "data/*.lang"))
            srcDir("resources")
        }
    }
}

tasks.withType(Jar::class) {
    from(sourceSets["main"].output, sourceSets["libs"].output)
}

val libsImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

dependencies {
    packIntoJar("com.thoughtworks.xstream:xstream:${versions["xstream"]}")
    packIntoJar("org.ogce:xpp3:${versions["xpp"]}")
    packIntoJar("xmlpull:xmlpull:${versions["xmlpull"]}")

    packIntoJar("org.boofcv:boofcv-core:${versions["boofcv"]}")
    packIntoJar("org.boofcv:boofcv-feature:${versions["boofcv"]}")
    packIntoJar("org.boofcv:boofcv-swing:${versions["boofcv"]}")
    packIntoJar("org.boofcv:boofcv-ip:${versions["boofcv"]}")
    packIntoJar("org.boofcv:boofcv-io:${versions["boofcv"]}")
    packIntoJar("org.ddogleg:ddogleg:${versions["ddogleg"]}")
    packIntoJar(files("lib/marvin-custom.jar"))
    packIntoJar(files("lib/marvinplugins-custom.jar"))

    libsImplementation("org.boofcv:boofcv-core:${versions["boofcv"]}")
    libsImplementation("org.boofcv:boofcv-feature:${versions["boofcv"]}")
    libsImplementation("org.boofcv:boofcv-swing:${versions["boofcv"]}")
    libsImplementation("org.boofcv:boofcv-ip:${versions["boofcv"]}")
    libsImplementation("org.boofcv:boofcv-io:${versions["boofcv"]}")
    libsImplementation("org.ddogleg:ddogleg:${versions["ddogleg"]}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${versions["junit"]}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${versions["junit"]}")
    testImplementation("com.github.spotbugs:spotbugs-annotations:${versions["spotbugs"]}")
}

// Set up ErrorProne
dependencies {
  errorprone("com.google.errorprone:error_prone_core:${versions["errorprone"]}")
  if (!JavaVersion.current().isJava9Compatible) {
    errorproneJavac("com.google.errorprone:javac:9+181-r4173-1")
  }
}
tasks.withType(JavaCompile::class).configureEach {
  options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-serial"))
  options.errorprone {
    check("ClassCanBeStatic", CheckSeverity.ERROR)
    check("StringEquality", CheckSeverity.ERROR)
    check("WildcardImport", CheckSeverity.ERROR)
    check("MethodCanBeStatic", CheckSeverity.WARN)
    check("RemoveUnusedImports", CheckSeverity.WARN)
    check("PrivateConstructorForUtilityClass", CheckSeverity.WARN)
    check("LambdaFunctionalInterface", CheckSeverity.WARN)
    check("ConstantField", CheckSeverity.WARN)
  }
}

spotbugs {
    toolVersion.set(versions["spotbugs"])
    ignoreFailures.set(true)
    effort.set(Effort.MAX)
    reportLevel.set(Confidence.LOW)
}
pmd {
    toolVersion = versions["pmd"]!!
    isIgnoreFailures = true
    sourceSets = listOf(project.sourceSets["main"])
    ruleSets("category/java/bestpractices.xml", "category/java/codestyle.xml", "category/java/errorprone.xml")
}

josm {
    pluginName = "areaselector"
    i18n {
        pathTransformer = getPathTransformer(project.projectDir, "github.com/JOSM/areaselector/blob")
    }
    manifest {
        oldVersionDownloadLink(16871, "v2.6.0-beta.1", URL("https://github.com/JOSM/areaselector/releases/download/v2.6.0-beta.1/areaselector.jar"))
        oldVersionDownloadLink(15017, "v2.5.1", URL("https://github.com/JOSM/areaselector/releases/download/v2.5.1/areaselector.jar"))
        oldVersionDownloadLink(12859, "v2.4.9", URL("https://github.com/JOSM/areaselector/releases/download/v2.4.9/areaselector.jar"))
        oldVersionDownloadLink(11226, "v2.4.3", URL("https://github.com/JOSM/areaselector/releases/download/v2.4.3/areaselector.jar"))
  }
}

tasks.withType(JavaCompile::class) {
  options.encoding = "UTF-8"
}
tasks.withType(Javadoc::class) {
  isFailOnError = false
}
tasks.withType(SpotBugsTask::class) {
  reports.create("html")
  reports.create("xml")
}
tasks.create("md2html", MarkdownToHtml::class) {
  destDir = File(buildDir, "md2html")
  source(projectDir)
  include("README.md", "GPL-v3.0.md")
  tasks.withType(ProcessResources::class)["processResources"].from(this)
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allJava)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
}
