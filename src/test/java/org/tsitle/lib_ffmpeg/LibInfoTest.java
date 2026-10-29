package org.tsitle.lib_ffmpeg;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LibInfoTest {

	@Test
	void testLibInfo() {
		assertEquals(LibInfo.getLibVersion().orElse("-not_set-"), System.getProperty("libVersion"));
		assertEquals(LibInfo.getFfmpegVersion().orElse("-not_set-"), System.getProperty("ffmpegVersion"));
		assertEquals(LibInfo.getGitCommit().orElse("-not_set-"), System.getProperty("gitCommit"));
	}

}
