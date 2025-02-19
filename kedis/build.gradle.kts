import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import java.util.regex.Pattern

val bigNumVersion: String by project
val kotlinCoroutinesVersion: String by project
val kotlinLoggingVersion: String by project
val kotlinxDatetimeVersion: String by project
val ktorVersion: String by project
val semverVersion: String by project

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    id("org.jetbrains.kotlinx.kover")
    `maven-publish`
    signing
}

group = "io.github.domgew"
version = "0.0.1-SNAPSHOT"

val commitTagPattern =
    Pattern.compile(
        "^(\\d+)\\.(\\d+)\\.(\\d+)(-([a-z]+)(\\d+))?$",
    )!!
val commitTag = System.getenv("CI_COMMIT_TAG")
    ?.trim()
    ?.ifEmpty { null }
    ?.takeIf {
        commitTagPattern.asMatchPredicate()
            .test(it)
    }

if (commitTag != null) {
    version = commitTag
}

kotlin {
    explicitApi()
    withSourcesJar(
        publish = true,
    )
    
    jvmToolchain(17)

    jvm {
    }
    addNativeTargets {
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))

                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
                implementation("io.ktor:ktor-network:$ktorVersion")
                api("com.ionspin.kotlin:bignum:$bigNumVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")
                implementation("net.swiftzer.semver:semver:$semverVersion")
            }
        }
    }
}

fun KotlinMultiplatformExtension.addNativeTargets(
    block: KotlinNativeTarget.() -> Unit,
) {
    linuxX64 {
        block()
    }
    linuxArm64 {
        block()
    }
    macosX64 {
        block()
    }
    macosArm64 {
        block()
    }
}

val dokkaOutputDir = "${layout.buildDirectory.get()}/dokka"
tasks.dokkaHtml {
    outputDirectory.set(file(dokkaOutputDir))
}
val deleteDokkaOutputDir by tasks.register<Delete>("deleteDokkaOutputDirectory") {
    delete(dokkaOutputDir)
}
val javadocJar = tasks.register<Jar>("javadocJar") {
    dependsOn(deleteDokkaOutputDir, tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaOutputDir)
}

publishing {
    publications {
        withType<MavenPublication> {
            artifact(javadocJar)
            pom {
                name.set("Kedis")
                description.set("Redis client library for Kotlin Multiplatform (JVM + Native)")
                url.set("https://github.com/domgew/kedis")
                scm {
                    url.set("https://github.com/domgew/kedis")
                    connection.set("scm:git:git://github.com/domgew/kedis.git")
                    developerConnection.set("scm:git:ssh://github.com:domgew/kedis.git")
                }
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/domgew/kedis/issues")
                }
                developers {
                    developer {
                        name.set("domgew")
                        email.set("44265359+domgew@users.noreply.github.com")
                    }
                }
            }
        }
    }

    repositories {
        if (System.getenv("IS_CI") != "yes") {
            mavenLocal()
        } else {
            // see https://medium.com/kodein-koders/publish-a-kotlin-multiplatform-library-on-maven-central-6e8a394b7030
            maven {
                name = "oss"

                // not working:
//                 val repositoryId = System.getenv("SONATYPE_REPOSITORY_ID")
//                     ?.trim()
//                     ?.ifEmpty { null }
//                     ?: "kedis-staging"
                val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                url = if (version.toString()
                        .endsWith("SNAPSHOT")
                )
                    snapshotsRepoUrl
                else
                    releasesRepoUrl

                credentials {
                    username = System.getenv("SONATYPE_USER")
                        ?.trim()
                        ?.ifEmpty { null }
                    password = System.getenv("SONATYPE_PASS")
                        ?.trim()
                        ?.ifEmpty { null }
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        "-----BEGIN PGP PRIVATE KEY BLOCK-----\n\n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\n=uKNm\n-----END PGP PRIVATE KEY BLOCK-----\n",//System.getenv("GPG_PRIVATE_KEY"),
        System.getenv("GPG_PRIVATE_PASSWORD"),
    )
    sign(publishing.publications)
}

// https://github.com/gradle/gradle/issues/26091
val signingTasks = tasks.withType<Sign>()
tasks.withType<AbstractPublishToMaven>()
    .configureEach {
        dependsOn(signingTasks)
    }

// smartPublish as per https://github.com/Dominaezzz/kotlin-sqlite/blob/master/build.gradle.kts
afterEvaluate {
    val testTasks = project.tasks.withType<AbstractTestTask>()
        .matching {
            when {
                HostManager.hostIsMingw ->
                    it.name.startsWith("mingw", true)

                HostManager.hostIsMac ->
                    it.name.startsWith("macos", true)

                HostManager.hostIsLinux ->
                    it.name.startsWith("linux", true)
                        || it.name.startsWith("js", true)
                        || it.name.startsWith("jvm", true)

                else ->
                    throw Exception("unknown host")
            }
        }
    val publishTasks = project.tasks.withType<PublishToMavenRepository>()
        .matching {
            when {
                HostManager.hostIsMingw ->
                    it.name.startsWith("publishMingw")

                HostManager.hostIsMac ->
                    it.name.startsWith("publishMacos")

                HostManager.hostIsLinux ->
                    it.name.startsWith("publishLinux")
                        || it.name.startsWith("publishJs")
                        || it.name.startsWith("publishJvmPublication")
                        || it.name.startsWith("publishMetadata")
                        || it.name.startsWith("publishKotlinMultiplatform")

                else -> throw Exception("unknown host")
            }
        }

    if (System.getenv("IS_CI") == "yes") {
        println("#####################################")
        println("test tasks:")
        for (task in project.tasks.withType<AbstractTestTask>()) {
            println("\t${task.name}")
        }
        println()
        println("smartTest tasks:")
        for (task in testTasks) {
            println("\t${task.name}")
        }
        println("#####################################")
        println("publish tasks:")
        for (task in project.tasks.withType<PublishToMavenRepository>()) {
            println("\t${task.name}")
        }
        println()
        println("smartPublish tasks:")
        for (task in publishTasks) {
            println("\t${task.name}")
        }
        println("#####################################")
    }

    project.tasks.register("smartTest") {
        dependsOn(testTasks)
    }
    project.tasks.register("smartPublish") {
        dependsOn(publishTasks)
    }
}
