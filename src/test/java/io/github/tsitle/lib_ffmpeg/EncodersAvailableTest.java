package io.github.tsitle.lib_ffmpeg;

import io.github.tsitle.lib_ffmpeg.tc.FfmpegTcEncoderLists;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class EncodersAvailableTest {

	@Test
	void showAllAvailableEncoders() {
		Set<String> tmpEncoders = FfmpegTcEncoderLists.getUnfilteredEncoderList();
		assertNotEquals(0, tmpEncoders.size());

		System.out.println("Unfiltered Available Encoders:");
		for (String encName : tmpEncoders) {
			System.out.println(" - '" + encName + "'");
		}
	}

	@Test
	void showVideoEncodersAvailable() {
		Map<FfmpegCodec, List<String>> tmpEncoders = FfmpegTcEncoderLists.getVideoEncoders(new HashSet<>());
		assertNotEquals(0, tmpEncoders.size());

		System.out.println("All Available Video Encoders:");
		for (Map.Entry<FfmpegCodec, List<String>> tmpEntry : tmpEncoders.entrySet()) {
			System.out.println(" - codec: " + tmpEntry.getKey());
			if (tmpEntry.getValue().isEmpty()) {
				System.out.println("   - <NONE> !!!");
				continue;
			}
			for (String encName : tmpEntry.getValue()) {
				System.out.println("   - '" + encName + "'");
			}
		}
	}

	@Test
	void showAudioEncodersAvailable() {
		Map<FfmpegCodec, List<String>> tmpEncoders = FfmpegTcEncoderLists.getAudioEncoders(new HashSet<>());
		assertNotEquals(0, tmpEncoders.size());

		System.out.println("All Available Audio Encoders:");
		for (Map.Entry<FfmpegCodec, List<String>> tmpEntry : tmpEncoders.entrySet()) {
			System.out.println(" - codec: " + tmpEntry.getKey());
			if (tmpEntry.getValue().isEmpty()) {
				System.out.println("   - <NONE> !!!");
				continue;
			}
			for (String encName : tmpEntry.getValue()) {
				System.out.println("   - '" + encName + "'");
			}
		}
	}

	@Test
	void testVideoEncoderH264IsAvailable() {
		List<String> tmpEncoders = FfmpegTcEncoderLists.getVideoEncodersForCodec(FfmpegCodec.V_H264, new HashSet<>());
		assertNotEquals(0, tmpEncoders.size());
	}

	@Test
	void testAudioEncoderAacIsAvailable() {
		List<String> tmpEncoders = FfmpegTcEncoderLists.getAudioEncodersForCodec(FfmpegCodec.A_AAC, new HashSet<>());
		assertNotEquals(0, tmpEncoders.size());
	}

}
