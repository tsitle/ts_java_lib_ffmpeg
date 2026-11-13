package io.github.tsitle.lib_ffmpeg;

import io.github.tsitle.lib_ffmpeg.tc.FfmpegTcDecoderLists;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DecodersAvailableTest {

	@Test
	void showAllAvailableDecoders() {
		Set<String> tmpDecoders = FfmpegTcDecoderLists.getUnfilteredDecoderList();
		assertNotEquals(0, tmpDecoders.size());

		System.out.println("Unfiltered Available Decoders:");
		for (String decName : tmpDecoders) {
			System.out.println(" - '" + decName + "'");
		}
	}

	@Test
	void showVideoDecodersAvailable() {
		Map<FfmpegCodec, List<String>> tmpDecoders = FfmpegTcDecoderLists.getVideoDecoders(new HashSet<>());
		assertNotEquals(0, tmpDecoders.size());

		System.out.println("All Available Video Decoders:");
		for (Map.Entry<FfmpegCodec, List<String>> tmpEntry : tmpDecoders.entrySet()) {
			System.out.println(" - codec: " + tmpEntry.getKey());
			if (tmpEntry.getValue().isEmpty()) {
				System.out.println("   - <NONE> !!!");
				continue;
			}
			for (String decName : tmpEntry.getValue()) {
				System.out.println("   - '" + decName + "'");
			}
		}
	}

	@Test
	void showAudioDecodersAvailable() {
		Map<FfmpegCodec, List<String>> tmpDecoders = FfmpegTcDecoderLists.getAudioDecoders(new HashSet<>());
		assertNotEquals(0, tmpDecoders.size());

		System.out.println("All Available Audio Decoders:");
		for (Map.Entry<FfmpegCodec, List<String>> tmpEntry : tmpDecoders.entrySet()) {
			System.out.println(" - codec: " + tmpEntry.getKey());
			if (tmpEntry.getValue().isEmpty()) {
				System.out.println("   - <NONE> !!!");
				continue;
			}
			for (String decName : tmpEntry.getValue()) {
				System.out.println("   - '" + decName + "'");
			}
		}
	}

	@Test
	void testVideoDecoderH264IsAvailable() {
		List<String> tmpDecoders = FfmpegTcDecoderLists.getVideoDecodersForCodec(FfmpegCodec.V_H264, new HashSet<>());
		assertNotEquals(0, tmpDecoders.size());
	}

	@Test
	void testAudioDecoderAacIsAvailable() {
		List<String> tmpDecoders = FfmpegTcDecoderLists.getAudioDecodersForCodec(FfmpegCodec.A_AAC, new HashSet<>());
		assertNotEquals(0, tmpDecoders.size());
	}

}
