import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.compose") version "1.6.2"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
}

group = "com.spreadsheet"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    flatDir {
        val graalVmHome = System.getenv("GRAALVM_HOME") ?: System.getenv("JAVA_HOME")
        dirs(
            "$graalVmHome/lib/polyglot",
            "$graalVmHome/languages/python",
            "$graalVmHome/lib/truffle"
        )
    }
}

// === 依存関係の定義 ===
dependencies {
    // UI
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)

    // Kotlin Core
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

        // Python Integration (GraalVM)
    implementation(group = "org.graalvm.polyglot", name = "polyglot-native-api", version = "local")
    implementation(group = "org.graalvm.python", name = "graalpython", version = "local")
    implementation(group = "org.graalvm.truffle", name = "truffle-api", version = "local")

    // File I/O
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // Database
    val exposedVersion = "0.41.1"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    runtimeOnly("com.h2database:h2:2.2.224")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.mockk:mockk:1.13.10")
}



// === 配布パッケージ生成設定 ===
compose.desktop {
    application {
        mainClass = "com.spreadsheet.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "SpreadsheetNext"
            packageVersion = "1.0.0"
            description = "A modern spreadsheet application"
            vendor = "SpreadsheetNext Corp"

            // --- GraalVM(JRE)を同梱するための設定 ---
            // Gradleが使用するJDKをGraalVMに設定することで、
            // 配布パッケージにGraalVMのJREがバンドルされる
            // (gradle.propertiesまたは環境変数でJAVA_HOMEを設定)

            windows {
                menuGroup = "SpreadsheetNext"
                shortcut = true
                iconFile.set(project.file("src/main/resources/icons/icon.ico"))
            }
            macOS {
                bundleID = "com.spreadsheet.next"
                iconFile.set(project.file("src/main/resources/icons/icon.icns"))
            }
            linux {
                // iconFile.set(project.file("src/main/resources/icons/icon.png"))
            }
        }
    }
}

// === コード品質ツールの設定 ===
ktlint {
    version.set("1.2.1")
    outputToConsole.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
    }
}

detekt {
    config.setFrom(files("detekt-config.yml"))
    buildUponDefaultConfig = true
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "17"
}

// === テストタスクの設定 ===
tasks.test {
    useJUnitPlatform()
}
