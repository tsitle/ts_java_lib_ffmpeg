package io.github.tsitle.lib_ffmpeg;

import io.github.tsitle.lib_ffmpeg.helpers.FfmpegHelperFfLibVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FfSharedLibInfoTest {

	@Test
	void testLibInfo() {
		FfmpegHelperFfLibVersion.FfmpegSharedLibInfo tmpInfo = FfmpegHelperFfLibVersion.getFfmpegSharedLibInfo();

		//System.out.println("avutilVersion: " + tmpInfo.version());

		/*
		 * --prefix=..
		 * --disable-iconv --disable-opencl --disable-sdl2 --disable-bzlib --disable-lzma --disable-linux-perf --disable-xlib
		 * --enable-shared --enable-version3 --enable-runtime-cpudetect --enable-zlib --enable-libmp3lame --enable-libspeex
		 * --enable-libopencore-amrnb --enable-libopencore-amrwb --enable-libvo-amrwbenc --enable-openssl --enable-libopenh264
		 * --enable-libvpx --enable-libfreetype --enable-libharfbuzz --enable-libopus --enable-libxml2 --enable-libsrt
		 * --enable-libwebp --enable-libaom --enable-libsvtav1 --enable-libzimg --enable-libmfx --enable-vulkan
		 * --enable-hwaccel=h264_vulkan --enable-hwaccel=hevc_vulkan --enable-hwaccel=av1_vulkan
		 * -enable-libdrm --enable-cuda --enable-cuvid --enable-nvenc --enable-pthreads --enable-libxcb --enable-libpulse
		 * --cc='gcc -m64' --cxx='g++ -m64' --extra-cflags='-I../include/ -I../include/libxml2
		 * -I../include/mfx -I../include/svt-av1' --extra-ldflags=-L../lib/
		 * --extra-libs='-lstdc++ -lpthread -ldl -lz -lm -lva-drm -lva-x11 -lva'
		 */
		System.out.println("avutilBuildConfigInfo: " + tmpInfo.buildConfigInfo());

		assertEquals(LibInfo.getFfmpegVersion().orElse("-not_set-"), tmpInfo.version());
	}

}
