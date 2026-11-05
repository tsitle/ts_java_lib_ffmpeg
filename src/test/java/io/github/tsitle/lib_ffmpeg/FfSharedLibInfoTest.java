package io.github.tsitle.lib_ffmpeg;

import io.github.tsitle.lib_ffmpeg.helpers.FfmpegHelperFfLibVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FfSharedLibInfoTest {

	@Test
	void testLibInfo() {
		FfmpegHelperFfLibVersion.FfmpegSharedLibInfo tmpInfo = FfmpegHelperFfLibVersion.getFfmpegSharedLibInfo();

		//System.out.println("avutilVersion: " + tmpInfo.version());
		System.out.println("avutilBuildConfigInfo: " + tmpInfo.buildConfigInfo());

		assertEquals(LibInfo.getFfmpegVersion().orElse("-not_set-"), tmpInfo.version());
	}

}
