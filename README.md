# Java Library for FFmpeg

This library is mainly being used by the [Java RTSP Server](https://github.com/tsitle/ts_rtsp_server/) project.

## Maven Central Repository

This library is available on the Maven Central Repository.

If you're using Maven as build tool, include it in your `pom.xml` like so:

``` xml
<dependencies>
	<!-- ... -->
	<dependency>
		<groupId>io.github.tsitle</groupId>
		<artifactId>ts_lib_ffmpeg_linux_x64</artifactId>  <!-- or ts_lib_ffmpeg_linux_aarch64 -->
		<version>1.2.2</version>
	</dependency>
</dependencies>
```

Or, if you're using Gradle with the Groovy DSL, add this to your `build.gradle`:

``` groovy
dependencies {
	/* ... */
	implementation 'io.github.tsitle:ts_lib_ffmpeg_linux_x64:1.2.2'  // or ts_lib_ffmpeg_linux_aarch64
}
```

Or, if you're using Gradle with the Kotlin DSL, add this to your `build.gradle.kts`:

``` kotlin
dependencies {
	/* ... */
	implementation("io.github.tsitle:ts_lib_ffmpeg_linux_x64:1.2.2")  // or ts_lib_ffmpeg_linux_aarch64
}
```
