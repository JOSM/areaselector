import com.github.spotbugs.SpotBugsTask
import net.ltgt.gradle.errorprone.*
import org.openstreetmap.josm.gradle.plugin.config.I18nConfig
import org.openstreetmap.josm.gradle.plugin.config.JosmManifest
import org.openstreetmap.josm.gradle.plugin.task.MarkdownToHtml
import java.net.URL

plugins {
    id("org.openstreetmap.josm") version "0.6.1"
    id("com.github.spotbugs") version "1.6.4"
    id("net.ltgt.errorprone") version "0.6"
    java
    pmd
    `maven-publish`
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
base.archivesBaseName = "areaselector"

val errorProneVersion = "2.3.2"
val spotbugsVersion = "3.1.5"
val pmdVersion = "6.16.0"
val junitVersion = "5.3.1"

repositories {
    jcenter()
    ivy {
        url = uri("https://github.com/JOSM/austriaaddresshelper/releases/download/")
        val aahVersion = "v0.6.1"
        patternLayout {
            artifact("/$aahVersion/[artifact].[ext]")
            ivy("$aahVersion/ivy.xml")
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
    packIntoJar("com.thoughtworks.xstream:xstream:1.4.11.1")
    packIntoJar("org.ejml:ejml-core:0.38")
    packIntoJar("org.ogce:xpp3:1.1.5")
    packIntoJar("xmlpull:xmlpull:1.1.3.1")
    implementation("org.apache.logging.log4j:log4j-api:2.12.0")
    implementation("org.apache.logging.log4j:log4j-core:2.12.0")

    packIntoJar("org.boofcv:core:0.24.1")
    packIntoJar("org.boofcv:feature:0.24.1")
    packIntoJar("org.boofcv:visualize:0.24.1")
    packIntoJar("org.boofcv:ip:0.24.1")
    packIntoJar("org.boofcv:io:0.24.1")
    packIntoJar("org.ddogleg:ddogleg:0.17")
    packIntoJar(files("lib/marvin-custom.jar"))
    packIntoJar(files("lib/marvinplugins-custom.jar"))
    libsImplementation("org.boofcv:core:0.24.1")
    libsImplementation("org.boofcv:feature:0.24.1")
    libsImplementation("org.boofcv:visualize:0.24.1")
    libsImplementation("org.boofcv:ip:0.24.1")
    libsImplementation("org.boofcv:io:0.24.1")
    libsImplementation("org.ddogleg:ddogleg:0.17")

    testImplementation ("org.openstreetmap.josm:josm-unittest:SNAPSHOT"){ isChanging = true }
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("com.github.spotbugs:spotbugs-annotations:3.1.7")
}

// Set up ErrorProne
dependencies {
  errorprone("com.google.errorprone:error_prone_core:$errorProneVersion")
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
    toolVersion = spotbugsVersion
    setIgnoreFailures(true)
    effort = "max"
    reportLevel = "low"
    sourceSets = listOf(project.sourceSets["main"])
}
pmd {
    toolVersion = pmdVersion
    setIgnoreFailures(true)
    sourceSets = listOf(project.sourceSets["main"])
    ruleSets("category/java/bestpractices.xml", "category/java/codestyle.xml", "category/java/errorprone.xml")
}

josm {
    i18n {
        pathTransformer = getPathTransformer("github.com/JOSM/areaselector/blob")
    }
    manifest {
        pluginDependencies.add("austriaaddresshelper")
        pluginDependencies.add("ejml")
        pluginDependencies.add("log4j")
    }
}

tasks.withType(JavaCompile::class) {
  options.encoding = "UTF-8"
}
tasks.withType(Javadoc::class) {
  isFailOnError = false
}
tasks.withType(SpotBugsTask::class) {
  reports {
    xml.isEnabled = false
    html.isEnabled = true
  }
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
