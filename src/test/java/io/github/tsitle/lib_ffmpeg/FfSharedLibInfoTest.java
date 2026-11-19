package io.github.tsitle.lib_ffmpeg;

import io.github.tsitle.lib_ffmpeg.helpers.FfmpegHelperFfLibVersion;
import org.junit.jupiter.api.Test;

class FfSharedLibInfoTest {

	@Test
	void testLibInfo() {
		FfmpegHelperFfLibVersion.FfmpegSharedLibInfo tmpInfo = FfmpegHelperFfLibVersion.getFfmpegSharedLibInfo();

		System.out.println("FfmpegSharedLibInfo.version        : " + tmpInfo.version());
		System.out.println("FfmpegSharedLibInfo.buildConfigInfo: " + tmpInfo.buildConfigInfo());
	}

}
