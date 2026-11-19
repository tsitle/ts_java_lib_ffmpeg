package io.github.tsitle.lib_ffmpeg;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LibInfoTest {

	@Test
	void testLibInfo() {
		assertEquals(LibInfo.getLibVersion().orElse("-not_set-"), System.getProperty("libVersion"));
		assertEquals(LibInfo.getGitCommit().orElse("-not_set-"), System.getProperty("gitCommit"));
		if (LibInfo.isCustomJavaCppAndFfmpegBuild()) {
			// the FFmpeg version can also be a commit hash
			assertFalse(LibInfo.getFfmpegVersion().orElse("").isBlank());
		} else {
			// the FFmpeg version must match exactly
			assertEquals(LibInfo.getFfmpegVersion().orElse("-not_set-"), System.getProperty("ffmpegVersion"));
			//
			assertFalse(LibInfo.usesFfmpegRockchipFork());
			assertFalse(LibInfo.hasRkmppSupport());
		}
	}

}
