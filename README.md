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
		<version>1.2.5</version>
	</dependency>
</dependencies>
```

Or, if you're using Gradle with the Groovy DSL, add this to your `build.gradle`:

``` groovy
dependencies {
	/* ... */
	implementation 'io.github.tsitle:ts_lib_ffmpeg_linux_x64:1.2.5'  // or ts_lib_ffmpeg_linux_aarch64
}
```

Or, if you're using Gradle with the Kotlin DSL, add this to your `build.gradle.kts`:

``` kotlin
dependencies {
	/* ... */
	implementation("io.github.tsitle:ts_lib_ffmpeg_linux_x64:1.2.5")  // or ts_lib_ffmpeg_linux_aarch64
}
```

## Sample Media Files

All sample media files are derived from: [blender.org Big Buck Bunny](https://download.blender.org/demo/movies/BBB/bbb_sunflower_1080p_30fps_normal.mp4.zip)

## Docker Images

For building and publishing the library, the Docker images in `packaging/` are used.  
Publishing is currently only allowed for the version of the library that uses the official JavaCPP+FFmpeg  
artefacts from the Maven Central Repository.

### Default Version

For the default version of the library, building and publishing the library is done like this:

``` bash
$ cd packaging
$ ./build_image-base.sh
$ ./build_and_publish_lib.sh <LIB_VERSION> <COMMIT_OR_BRANCH> pub
```

Note: using the Docker images is not actually required for the default version of the library.  
But using them provides a consistent environment for building the library.

### Custom JavaCPP+FFmpeg Version

``` bash
$ cd packaging
$ ./build_image-base.sh
$ ./build_image-base-cb_javacpp_ffmpeg.sh
$ ./build_and_publish_lib.sh <LIB_VERSION> <COMMIT_OR_BRANCH> cb_javacpp_ffmpeg
```

### Custom JavaCPP+FFmpeg Version with RKMPP Support

``` bash
$ cd packaging
$ ./build_image-base.sh
$ ./build_image-base-cb_javacpp_ffmpeg.sh rkmpp
$ ./build_and_publish_lib.sh <LIB_VERSION> <COMMIT_OR_BRANCH> cb_javacpp_ffmpeg rkmpp
```

### Building with Specific TsLibXrtxp Version

When not wanting to publish a new version of the library, a locally built version of the TsLibXrtxp can be  
used instead of the official version from the Maven Central Repository:

``` bash
$ cd packaging
$ ./build_and_publish_lib.sh <LIB_VERSION> <COMMIT_OR_BRANCH> lib_xrtxp:<COMMIT_OR_BRANCH> [cb_javacpp_ffmpeg [rkmpp]]
```
