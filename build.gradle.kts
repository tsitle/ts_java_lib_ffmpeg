import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
	id("java-library")
	// this plugin will filter the JavaCPP and FFmpeg libraries such that only libs for the current CPU architecture will be included
	id("org.bytedeco.gradle-javacpp-platform") version "1.5.10"
	id("com.gradleup.shadow") version "9.6.1"  // for Fat JARs
}

group = "org.tsitle"
version = "${project.findProperty("libVersion")}"  // read 'libVersion' from 'gradle.properties'

// FFmpeg version
var tmpArgProvider = providers.gradleProperty("FFMPEG_VERSION")  // returns null if not provided
// Conditionally set [ffmpegVersion] based on the CLI input
val ffmpegVersion = tmpArgProvider.orNull ?: "7.1.1"

println("FFmpeg version: $ffmpegVersion")

// ---------------------------------------------------------------------------------------------------------------------

java {
	/*
	 * Show auto-detectable JDKs:
	 *   $ ./gradlew -q javaToolchains
	 */

	sourceCompatibility = JavaVersion.VERSION_25
	/*toolchain {
		languageVersion.set(JavaLanguageVersion.of(25))
	}*/
}

// ---------------------------------------------------------------------------------------------------------------------

repositories {
	mavenCentral()
	// for local development:
	flatDir { dirs("../lib-rtsp_xrtxp/build/libs") }
}

dependencies {
	// for Unit Tests
	val depsVersionOrgJunitJunitBom = project.findProperty("depsVersionOrgJunitJunitBom")!! as String
	testImplementation(platform("org.junit:junit-bom:${depsVersionOrgJunitJunitBom}"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
	//testRuntimeOnly("org.junit.vintage:junit-vintage-engine")

	//
	implementation("org.jspecify:jspecify:1.0.0")

	implementation("org.zeromq:jeromq:0.6.0")  // for ZeroMQ

	when (ffmpegVersion) {
		"7.1.1" -> {
			implementation("org.bytedeco:ffmpeg-platform:7.1.1-1.5.12")
		}
		"8.0.1" -> {
			implementation("org.bytedeco:ffmpeg-platform:8.0.1-1.5.13")  // requires Linux package 'libva-drm2'
		}
		"8.1.2" -> {
			implementation("org.bytedeco:ffmpeg-platform:8.1.2-1.5.14")  // requires Linux package 'libva-drm2'
		}
		else -> { throw Error("FFmpeg version '${ffmpegVersion}' not supported") }
	}

	// TS lib xrtxp
	val depsVersionTsLibXrtxp = project.findProperty("depsVersionTsLibXrtxp")!! as String
	//implementation("io.github.tsitle:lib_xrtxp:${depsVersionTsLibXrtxp}")  // use official release
	implementation(":ts_lib_xrtxp:${depsVersionTsLibXrtxp}")  // for local development
}

// ---------------------------------------------------------------------------------------------------------------------

tasks.compileJava {
	options.javaModuleVersion = provider { version as String }
}
tasks.compileJava.configure {
	options.encoding = "UTF-8"
	options.compilerArgs.add("-Xlint:deprecation")
}

/*
 * see https://docs.gradle.org/current/userguide/java_testing.html
 */
tasks.test {
	useJUnitPlatform()

	maxHeapSize = "1G"

	jvmArgs("--enable-native-access=ALL-UNNAMED")  // for FFmpeg

	/*
	 * testLogging based on
	 *   https://stackoverflow.com/a/36130467
	 */
	testLogging {
		// set options for log level LIFECYCLE
		events(
			TestLogEvent.FAILED,
			TestLogEvent.PASSED,
			TestLogEvent.SKIPPED,
			TestLogEvent.STANDARD_OUT
		)
		exceptionFormat = TestExceptionFormat.FULL
		showExceptions = true
		showCauses = true
		showStackTraces = true

		// set options for log level DEBUG and INFO
		debug {
			events(
				TestLogEvent.STARTED,
				TestLogEvent.FAILED,
				TestLogEvent.PASSED,
				TestLogEvent.SKIPPED,
				TestLogEvent.STANDARD_ERROR,
				TestLogEvent.STANDARD_OUT
			)
			exceptionFormat = TestExceptionFormat.FULL
		}
		info.events = debug.events
		info.exceptionFormat = debug.exceptionFormat

		addTestListener(object : TestListener {
			override fun beforeSuite(suite: TestDescriptor) = Unit

			override fun afterSuite(suite: TestDescriptor, result: TestResult) {
				if (suite.parent == null) {  // will match the outermost suite
					val output = "Results: ${result.resultType} " +
							"(${result.testCount} tests, ${result.successfulTestCount} passed, " +
							"${result.failedTestCount} failed, ${result.skippedTestCount} skipped)"
					val startItem = "|  "
					val endItem = "  |"
					val repeatLength = startItem.length + output.length + endItem.length
					println("\n" + ("-".repeat(repeatLength)) + "\n" + startItem + output + endItem + "\n" + ("-".repeat(repeatLength)))
				}
			}

			override fun beforeTest(testDescriptor: TestDescriptor) = Unit

			override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) = Unit
		})
	}
}

// ---------------------------------------------------------------------------------------------------------------------

tasks.jar {
	archiveClassifier.set("slim")  // adds the suffix '-slim'
}

tasks.shadowJar {
	dependsOn("jar")
	archiveClassifier.set("full")  // replaces the default suffix '-all' with '-full'
}
