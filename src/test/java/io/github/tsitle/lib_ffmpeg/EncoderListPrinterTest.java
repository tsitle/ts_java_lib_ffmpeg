package io.github.tsitle.lib_ffmpeg;

import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

class EncoderListPrinterTest {

	@Test
	void showEncoders() {
		PointerPointer<Pointer> opaque = new PointerPointer<>(1);
		opaque.put(0, (Pointer) null);
		AVCodec codec;

		while ((codec = avcodec.av_codec_iterate(opaque)) != null) {
			if (avcodec.av_codec_is_encoder(codec) != 0) {
				String encName = codec.name().getString(StandardCharsets.UTF_8);
				if (! (encName.contains("av1") ||
						encName.contains("h264") ||
						encName.contains("h265") ||
						encName.contains("hevc") ||
						encName.contains("mjpeg") ||
						encName.contains("mpeg2") ||
						encName.contains("mpeg4") ||
						encName.contains("theora") ||
						encName.contains("vp8") ||
						encName.contains("vp9") ||
						encName.contains("vpx"))) {
					continue;
				}
				System.out.println(encName);
			}
		}
	}

}
