import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import net.ltgt.gradle.errorprone.*
import org.openstreetmap.josm.gradle.plugin.task.MarkdownToHtml
import java.net.URL

plugins {
    id("com.github.ben-manes.versions") version "0.51.0"
    id("org.openstreetmap.josm") version "0.8.2"
    id("com.github.spotbugs") version "6.0.23"
    id("net.ltgt.errorprone") version "4.0.1"
    java
    pmd
    `maven-publish`
}

java.sourceCompatibility = JavaVersion.VERSION_11
base.archivesBaseName = "areaselector"

val versions = mapOf(
    "austriaaddresshelper" to "v0.8.2",
    "boofcv" to "1.1.6",
    "ddogleg" to "0.23.3",
    "errorprone" to "2.33.0",
    "junit" to "5.11.1",
    "pmd" to "6.18.0",
    "spotbugs" to "4.8.6",
    "xmlpull" to "1.1.3.4a",
    "xpp" to "1.1.6",
    "xstream" to "1.4.20"
)

repositories {
    mavenCentral()
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
    implementation("com.thoughtworks.xstream:xstream:${versions["xstream"]}")
    implementation("org.ogce:xpp3:${versions["xpp"]}")
    implementation("xmlpull:xmlpull:${versions["xmlpull"]}")

    implementation("org.boofcv:boofcv-core:${versions["boofcv"]}")
    implementation("org.boofcv:boofcv-feature:${versions["boofcv"]}")
    implementation("org.boofcv:boofcv-swing:${versions["boofcv"]}")
    implementation("org.boofcv:boofcv-ip:${versions["boofcv"]}")
    implementation("org.boofcv:boofcv-io:${versions["boofcv"]}")
    implementation("org.ddogleg:ddogleg:${versions["ddogleg"]}")
    implementation(files("lib/marvin-custom.jar"))
    implementation(files("lib/marvinplugins-custom.jar"))

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
    check("ReferenceEquality", CheckSeverity.ERROR)
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
        oldVersionDownloadLink(18173, "v2.6.2", URL("https://github.com/JOSM/areaselector/releases/download/v2.6.2/areaselector.jar"))
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
