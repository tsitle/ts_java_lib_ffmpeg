package org.example.encoder_info;

import io.github.tsitle.lib_ffmpeg.FfmpegCodec;
import io.github.tsitle.lib_ffmpeg.tc.FfmpegTcEncoderLists;
import org.bytedeco.ffmpeg.avcodec.*;
import org.bytedeco.ffmpeg.avutil.*;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.PointerPointer;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

class EncoderOptionsPrinterTest {

	@Test
	void showGenericOptions() {
		printGenericOptions();
	}

	@Test
	void searchGenericOptions() {
		List<String> searchTerms = List.of("profile", "threads", "rc");
		for (String searchTerm : searchTerms) {
			Optional<String> tmpOptRes = searchGenericOptions(searchTerm);
			if (tmpOptRes.isPresent()) {
				System.out.println("Found generic option: '" + searchTerm + "' = '" + tmpOptRes.get() + "'");
			} else {
				System.out.println("Generic option not found: '" + searchTerm + "'");
			}
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	@Test
	void showAv1Options() {
		for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_AV1)) {
			printEncoderOptions(encoderName);
		}
	}

	@Test
	void searchAv1Options() {
		/*
		 * e.g. '$ ffmpeg -h encoder=av1_nvenc'
		 */
		List<String> searchTerms = List.of("cpu-used", "usage", "preset", "crf", "cq", "rc");
		for (String searchTerm : searchTerms) {
			for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_AV1)) {
				Optional<String> tmpOptRes = searchEncoderOptions(encoderName, searchTerm);
				if (tmpOptRes.isPresent()) {
					System.out.println("Found encoder '" + encoderName + "' option: '" + searchTerm + "' = '" + tmpOptRes.get() + "'");
				} else {
					System.out.println("Encoder '" + encoderName + "' option not found: '" + searchTerm + "'");
				}
			}
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	@Test
	void showH264Options() {
		for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_H264)) {
			printEncoderOptions(encoderName);
		}
	}

	@Test
	void searchH264Options() {
		List<String> searchTerms = List.of("allow_skip_frames", "rc_mode");
		for (String searchTerm : searchTerms) {
			for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_H264)) {
				Optional<String> tmpOptRes = searchEncoderOptions(encoderName, searchTerm);
				if (tmpOptRes.isPresent()) {
					System.out.println("Found encoder '" + encoderName + "' option: '" + searchTerm + "' = '" + tmpOptRes.get() + "'");
				} else {
					System.out.println("Encoder '" + encoderName + "' option not found: '" + searchTerm + "'");
				}
			}
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	@Test
	void showH265Options() {
		for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_H265)) {
			printEncoderOptions(encoderName);
		}
	}

	@Test
	void searchH265Options() {
		List<String> searchTerms = List.of("deadline", "cpu-used", "allow_skip_frames", "rc_mode");
		for (String searchTerm : searchTerms) {
			for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_H265)) {
				Optional<String> tmpOptRes = searchEncoderOptions(encoderName, searchTerm);
				if (tmpOptRes.isPresent()) {
					System.out.println("Found encoder '" + encoderName + "' option: '" + searchTerm + "' = '" + tmpOptRes.get() + "'");
				} else {
					System.out.println("Encoder '" + encoderName + "' option not found: '" + searchTerm + "'");
				}
			}
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	@Test
	void showMjpegOptions() {
		for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_MJPEG)) {
			printEncoderOptions(encoderName);
		}
	}

	@Test
	void searchMjpegOptions() {
		List<String> searchTerms = List.of("huffman");
		for (String searchTerm : searchTerms) {
			for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_MJPEG)) {
				Optional<String> tmpOptRes = searchEncoderOptions(encoderName, searchTerm);
				if (tmpOptRes.isPresent()) {
					System.out.println("Found encoder '" + encoderName + "' option: '" + searchTerm + "' = '" + tmpOptRes.get() + "'");
				} else {
					System.out.println("Encoder '" + encoderName + "' option not found: '" + searchTerm + "'");
				}
			}
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	@Test
	void showMpeg2Options() {
		for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_MPEG2)) {
			printEncoderOptions(encoderName);
		}
	}

	@Test
	void searchMpeg2Options() {
		List<String> searchTerms = List.of("deadline", "cpu-used");
		for (String searchTerm : searchTerms) {
			for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_MPEG2)) {
				Optional<String> tmpOptRes = searchEncoderOptions(encoderName, searchTerm);
				if (tmpOptRes.isPresent()) {
					System.out.println("Found encoder '" + encoderName + "' option: '" + searchTerm + "' = '" + tmpOptRes.get() + "'");
				} else {
					System.out.println("Encoder '" + encoderName + "' option not found: '" + searchTerm + "'");
				}
			}
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	@Test
	void showMpeg4Options() {
		for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_MPEG4)) {
			printEncoderOptions(encoderName);
		}
	}

	@Test
	void searchMpeg4Options() {
		List<String> searchTerms = List.of("deadline", "cpu-used");
		for (String searchTerm : searchTerms) {
			for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_MPEG4)) {
				Optional<String> tmpOptRes = searchEncoderOptions(encoderName, searchTerm);
				if (tmpOptRes.isPresent()) {
					System.out.println("Found encoder '" + encoderName + "' option: '" + searchTerm + "' = '" + tmpOptRes.get() + "'");
				} else {
					System.out.println("Encoder '" + encoderName + "' option not found: '" + searchTerm + "'");
				}
			}
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	@Test
	void showTheoraOptions() {
		for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_THEORA)) {
			printEncoderOptions(encoderName);
		}
	}

	@Test
	void searchTheoraOptions() {
		List<String> searchTerms = List.of("deadline", "cpu-used");
		for (String searchTerm : searchTerms) {
			for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_THEORA)) {
				Optional<String> tmpOptRes = searchEncoderOptions(encoderName, searchTerm);
				if (tmpOptRes.isPresent()) {
					System.out.println("Found encoder '" + encoderName + "' option: '" + searchTerm + "' = '" + tmpOptRes.get() + "'");
				} else {
					System.out.println("Encoder '" + encoderName + "' option not found: '" + searchTerm + "'");
				}
			}
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	@Test
	void showVp8Options() {
		for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_VP8)) {
			printEncoderOptions(encoderName);
		}
	}

	@Test
	void searchVp8Options() {
		List<String> searchTerms = List.of("deadline", "cpu-used");
		for (String searchTerm : searchTerms) {
			for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_VP8)) {
				Optional<String> tmpOptRes = searchEncoderOptions(encoderName, searchTerm);
				if (tmpOptRes.isPresent()) {
					System.out.println("Found encoder '" + encoderName + "' option: '" + searchTerm + "' = '" + tmpOptRes.get() + "'");
				} else {
					System.out.println("Encoder '" + encoderName + "' option not found: '" + searchTerm + "'");
				}
			}
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	@Test
	void showVp9Options() {
		for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_VP9)) {
			printEncoderOptions(encoderName);
		}
	}

	@Test
	void searchVp9Options() {
		List<String> searchTerms = List.of("deadline", "cpu-used");
		for (String searchTerm : searchTerms) {
			for (String encoderName : FfmpegTcEncoderLists.ENC_BY_CODEC.get(FfmpegCodec.V_VP9)) {
				Optional<String> tmpOptRes = searchEncoderOptions(encoderName, searchTerm);
				if (tmpOptRes.isPresent()) {
					System.out.println("Found encoder '" + encoderName + "' option: '" + searchTerm + "' = '" + tmpOptRes.get() + "'");
				} else {
					System.out.println("Encoder '" + encoderName + "' option not found: '" + searchTerm + "'");
				}
			}
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private static void printGenericOptions() {
		AVClass avClass = avcodec.avcodec_get_class();
		if (avClass == null) {
			System.out.println("No generic options dictionary available");
			return;
		}
		printOptionsForAvClass(avClass, "generic");
	}

	private static void printEncoderOptions(String encoderName) {
		try (AVCodec encoder = avcodec.avcodec_find_encoder_by_name(encoderName)) {
			if (encoder == null) {
				System.err.println("Encoder not found: '" + encoderName + "'");
				return;
			}

			AVClass avClass = encoder.priv_class();
			if (avClass == null) {
				System.out.println("No private options dictionary available for: '" + encoderName + "'");
				return;
			}
			printOptionsForAvClass(avClass, encoderName);
		}
	}

	private static void printOptionsForAvClass(@NonNull AVClass avClass, @NonNull String desc) {
		/*
		 * Wrap the AVClass inside a PointerPointer wrapper.
		 * This emulates a dummy object containing the class pointer, matching what FFmpeg expects.
		 */
		PointerPointer<AVClass> dummyObject = new PointerPointer<>(1);
		dummyObject.put(avClass);

		System.out.println("==================================================");
		System.out.println("Available options for dictionary keys (" + desc + "):");
		System.out.println("==================================================");

		//
		AVOption opt = null;
		while ((opt = avutil.av_opt_next(dummyObject, opt)) != null) {
			// filter out internal/dummy headers
			if (opt.type() == avutil.AV_OPT_TYPE_CONST || opt.name() == null) {
				continue;
			}

			String resS = getOptionInfoString(opt);
			System.out.println(resS);
		}
		System.out.println("==================================================");
	}

	private static Optional<String> searchGenericOptions(@NonNull String searchTerm) {
		AVClass avClass = avcodec.avcodec_get_class();
		if (avClass == null) {
			return Optional.empty();
		}
		return searchInOptionsForAvClass(avClass, searchTerm);
	}

	private static Optional<String> searchEncoderOptions(@NonNull String encoderName, @NonNull String searchTerm) {
		try (AVCodec encoder = avcodec.avcodec_find_encoder_by_name(encoderName)) {
			if (encoder == null) {
				return Optional.empty();
			}

			AVClass avClass = encoder.priv_class();
			if (avClass == null) {
				return Optional.empty();
			}
			return searchInOptionsForAvClass(avClass, searchTerm);
		}
	}

	private static Optional<String> searchInOptionsForAvClass(@NonNull AVClass avClass, @NonNull String searchTerm) {
		/*
		 * Wrap the AVClass inside a PointerPointer wrapper.
		 * This emulates a dummy object containing the class pointer, matching what FFmpeg expects.
		 */
		PointerPointer<AVClass> dummyObject = new PointerPointer<>(1);
		dummyObject.put(avClass);

		AVOption opt = null;
		while ((opt = avutil.av_opt_next(dummyObject, opt)) != null) {
			// filter out internal/dummy headers
			if (opt.type() == avutil.AV_OPT_TYPE_CONST || opt.name() == null) {
				continue;
			}

			String name = opt.name().getString(StandardCharsets.UTF_8);
			if (! name.equalsIgnoreCase(searchTerm)) {
				continue;
			}

			String resS = getOptionInfoString(opt);
			return Optional.of(resS);
		}
		return Optional.empty();
	}

	private static @NonNull String getOptionInfoString(@NonNull AVOption opt) {
		String name = opt.name().getString(StandardCharsets.UTF_8);
		String help = (opt.help() != null) ? opt.help().getString(StandardCharsets.UTF_8) : "No description available";
		String typeStr = getOptionTypeString(opt.type());
		String defaultValueStr = getOptionDefaultValueString(opt);

		return String.format("Key: %-18s | Type: %-10s | Min: %.2f | Max: %.2f | Def: %s | %s",
				name, typeStr, opt.min(), opt.max(), defaultValueStr, help);
	}

	private static @NonNull String getOptionDefaultValueString(@NonNull AVOption opt) {
		return switch (opt.type()) {
				case avutil.AV_OPT_TYPE_FLAGS,
					avutil.AV_OPT_TYPE_DICT,
					avutil.AV_OPT_TYPE_BINARY,
					avutil.AV_OPT_TYPE_BOOL -> "-/-";
				case avutil.AV_OPT_TYPE_INT,
					avutil.AV_OPT_TYPE_INT64 -> Long.toString(opt.default_val_i64());
				case avutil.AV_OPT_TYPE_DOUBLE,
					avutil.AV_OPT_TYPE_FLOAT -> Double.toString(opt.default_val_dbl());
				case avutil.AV_OPT_TYPE_STRING -> (opt.default_val_str() == null ?
						"-n/a-" : opt.default_val_str().getString(StandardCharsets.UTF_8));
				case avutil.AV_OPT_TYPE_RATIONAL -> opt.default_val_q().toString();
				default -> "UNKNOWN (" + opt.type() + ")";
			};
	}

	/** Helper method to convert the integer type to a readable string */
	private static @NonNull String getOptionTypeString(int type) {
		return switch (type) {
				case avutil.AV_OPT_TYPE_FLAGS -> "FLAGS";
				case avutil.AV_OPT_TYPE_INT -> "INT";
				case avutil.AV_OPT_TYPE_INT64 -> "INT64";
				case avutil.AV_OPT_TYPE_DOUBLE -> "DOUBLE";
				case avutil.AV_OPT_TYPE_FLOAT -> "FLOAT";
				case avutil.AV_OPT_TYPE_STRING -> "STRING";
				case avutil.AV_OPT_TYPE_RATIONAL -> "RATIONAL";
				case avutil.AV_OPT_TYPE_BINARY -> "BINARY";
				case avutil.AV_OPT_TYPE_DICT -> "DICTIONARY";
				case avutil.AV_OPT_TYPE_BOOL -> "BOOL";
				default -> "UNKNOWN (" + type + ")";
			};
	}

}
