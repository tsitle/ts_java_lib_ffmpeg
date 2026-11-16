package io.github.tsitle.lib_ffmpeg.tc;

import org.jspecify.annotations.NonNull;
import io.github.tsitle.lib_ffmpeg.FfmpegCodec;

import java.util.*;

/**
 * Helper class to find available encoders.
 */
public final class FfmpegTcEncoderLists extends FfmpegTcEnDeCoderListsBase {

	/** AV1: Rockchip hardware encoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String ENCODER_NAME_V_AV1_RKMPP = "av1_rkmpp";
	/** AV1: NVidia hardware encoder */
	public static final String ENCODER_NAME_V_AV1_NVENC = "av1_nvenc";
	public static final String ENCODER_NAME_V_AV1_LIBSVTAV1 = "libsvtav1";
	public static final String ENCODER_NAME_V_AV1_LIBAOMAV1 = "libaom-av1";

	/** H264: Rockchip hardware encoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String ENCODER_NAME_V_H264_RKMPP = "h264_rkmpp";
	/** H264: NVidia hardware encoder */
	public static final String ENCODER_NAME_V_H264_NVENC = "h264_nvenc";
	public static final String ENCODER_NAME_V_H264_LIBOPENH264 = "libopenh264";

	/** H265/HEVC: Rockchip hardware encoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String ENCODER_NAME_V_H265_RKMPP = "hevc_rkmpp";
	/** H265/HEVC: NVidia hardware encoder */
	public static final String ENCODER_NAME_V_H265_NVENC = "hevc_nvenc";

	/** MJPEG: Rockchip hardware encoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String ENCODER_NAME_V_MJPEG_RKMPP = "mjpeg_rkmpp";

	/** MPEG2: Rockchip hardware encoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String ENCODER_NAME_V_MPEG2_RKMPP = "mpeg2_rkmpp";

	/** MPEG4: Rockchip hardware encoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String ENCODER_NAME_V_MPEG4_RKMPP = "mpeg4_rkmpp";

	/** VP8: Rockchip hardware encoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String ENCODER_NAME_V_VP8_RKMPP = "vp8_rkmpp";

	/** VP9: Rockchip hardware encoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String ENCODER_NAME_V_VP9_RKMPP = "vp9_rkmpp";

	// -----------------------------------------------------

	private static final @NonNull List<@NonNull String> ENCODERS_V_AV1 = new ArrayList<>() {{
			add(ENCODER_NAME_V_AV1_RKMPP);  // note: this is still missing in official FFmpeg 8.1.2 and in 'ffmpeg-rockchip' @TODO untested
			add(ENCODER_NAME_V_AV1_NVENC);
			add(ENCODER_NAME_V_AV1_LIBSVTAV1);  // tested: OK
			add("av1_qsv");  // untested
			add("av1_amf");  // untested
			//add("av1_vaapi");  // untested, does not accept pixel format YUV420P and requires hardware frames
			//add(ENCODER_NAME_V_AV1_LIBAOMAV1);  // libaom-av1 doesn't work all that well as is extremely slow
		}};

	private static final @NonNull List<@NonNull String> ENCODERS_V_H264 = new ArrayList<>() {{
			add(ENCODER_NAME_V_H264_RKMPP);  // tested in 'ffmpeg-rockchip': OK
			add(ENCODER_NAME_V_H264_NVENC);
			add("libx264");  // untested
			//add("h264_vulkan");  // untested, does not accept pixel format YUV420P and requires hardware frames
			add(ENCODER_NAME_V_H264_LIBOPENH264);
			add("h264_v4l2m2m");  // untested
			add("h264_qsv");  // untested
			add("h264_amf");  // untested
			//add("h264_vaapi");  // untested, does not accept pixel format YUV420P and requires hardware frames
		}};

	private static final @NonNull List<@NonNull String> ENCODERS_V_H265 = new ArrayList<>() {{
			add(ENCODER_NAME_V_H265_RKMPP);  // tested in 'ffmpeg-rockchip': OK
			add(ENCODER_NAME_V_H265_NVENC);
			//add("hevc_vulkan");  // untested, does not accept pixel format YUV420P and requires hardware frames
			add("libx265");  // untested
			add("hevc_v4l2m2m");  // untested
			add("hevc_qsv");  // untested
			add("hevc_amf");  // untested
			//add("hevc_vaapi");  // untested, does not accept pixel format YUV420P and requires hardware frames
		}};

	private static final @NonNull List<@NonNull String> ENCODERS_V_MJPEG = new ArrayList<>() {{
			add(ENCODER_NAME_V_MJPEG_RKMPP);  // tested in 'ffmpeg-rockchip': OK
			add("mjpeg");
		}};

	private static final @NonNull List<@NonNull String> ENCODERS_V_MPEG2 = new ArrayList<>() {{
			add(ENCODER_NAME_V_MPEG2_RKMPP);  // note: this is still missing in official FFmpeg 8.1.2 and in 'ffmpeg-rockchip' @TODO untested
			add("mpeg2video");  // tested: OK
			add("mpeg2_qsv");  // untested
			//add("mpeg2_vaapi");  // untested, does not accept pixel format YUV420P and requires hardware frames
		}};

	private static final @NonNull List<@NonNull String> ENCODERS_V_MPEG4 = new ArrayList<>() {{
			add(ENCODER_NAME_V_MPEG4_RKMPP);  // note: this is still missing in official FFmpeg 8.1.2 and in 'ffmpeg-rockchip' @TODO untested
			add("mpeg4");  // tested: OK
			add("libxvid");  // untested
			add("mpeg4_v4l2m2m");  // untested
		}};

	private static final @NonNull List<@NonNull String> ENCODERS_V_THEORA = new ArrayList<>() {{
			add("libtheora");  // tested: OK
		}};

	private static final @NonNull List<@NonNull String> ENCODERS_V_VP8 = new ArrayList<>() {{
			add(ENCODER_NAME_V_VP8_RKMPP);  // note: this exists in official FFmpeg 8.1.2 but is missing in 'ffmpeg-rockchip' @TODO untested
			add("libvpx");  // tested: OK
			add("vp8_v4l2m2m");  // untested
			//add("vp8_vaapi");  // untested, does not accept pixel format YUV420P and requires hardware frames
		}};

	private static final @NonNull List<@NonNull String> ENCODERS_V_VP9 = new ArrayList<>() {{
			add(ENCODER_NAME_V_VP9_RKMPP);  // note: this exists in official FFmpeg 8.1.2 but is missing in 'ffmpeg-rockchip' @TODO untested
			add("libvpx-vp9");  // tested: OK
			//add("vp9_vaapi");  // untested, does not accept pixel format YUV420P and requires hardware frames
			add("vp9_qsv");  // untested
		}};

	private static final @NonNull Map<@NonNull FfmpegCodec, List<@NonNull String>> ENCODER_LIST_BY_VIDEO_CODEC =
			new HashMap<>() {{  // @CODEC
					put(FfmpegCodec.V_AV1, ENCODERS_V_AV1);
					put(FfmpegCodec.V_H264, ENCODERS_V_H264);
					put(FfmpegCodec.V_H265, ENCODERS_V_H265);
					put(FfmpegCodec.V_MJPEG, ENCODERS_V_MJPEG);
					put(FfmpegCodec.V_MPEG2, ENCODERS_V_MPEG2);
					put(FfmpegCodec.V_MPEG4, ENCODERS_V_MPEG4);
					put(FfmpegCodec.V_THEORA, ENCODERS_V_THEORA);
					put(FfmpegCodec.V_VP8, ENCODERS_V_VP8);
					put(FfmpegCodec.V_VP9, ENCODERS_V_VP9);
				}};

	// -----------------------------------------------------

	private static final @NonNull Map<@NonNull FfmpegCodec, List<@NonNull String>> ENCODER_LIST_BY_AUDIO_CODEC =
			new HashMap<>() {{  // @CODEC
					put(FfmpegCodec.A_AAC, List.of("aac"));
					put(FfmpegCodec.A_AC3, List.of("ac3"));
					put(FfmpegCodec.A_ALAC, List.of("alac"));
					put(FfmpegCodec.A_EAC3, List.of("eac3"));
					put(FfmpegCodec.A_FLAC, List.of("flac"));
					put(FfmpegCodec.A_MP2, List.of("mp2"));
					put(FfmpegCodec.A_MP3, List.of("libmp3lame"));
					put(FfmpegCodec.A_OPUS, List.of("opus", "libopus"));
					put(FfmpegCodec.A_PCM_ALAW, List.of("pcm_alaw"));
					put(FfmpegCodec.A_PCM_F32BE, List.of("pcm_f32be"));
					put(FfmpegCodec.A_PCM_F32LE, List.of("pcm_f32le"));
					put(FfmpegCodec.A_PCM_MULAW, List.of("pcm_mulaw"));
					put(FfmpegCodec.A_PCM_S16BE, List.of("pcm_s16be"));
					put(FfmpegCodec.A_PCM_S16LE, List.of("pcm_s16le"));
					put(FfmpegCodec.A_PCM_S24BE, List.of("pcm_s24be"));
					put(FfmpegCodec.A_PCM_S24LE, List.of("pcm_s24le"));
					put(FfmpegCodec.A_PCM_S32BE, List.of("pcm_s32be"));
					put(FfmpegCodec.A_PCM_S32LE, List.of("pcm_s32le"));
					put(FfmpegCodec.A_PCM_U8, List.of("pcm_u8"));
					put(FfmpegCodec.A_VORBIS, List.of("vorbis"));
				}};

	// -----------------------------------------------------

	private FfmpegTcEncoderLists() { }

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Get a list of video encoders that are available for the given codec
	 * @param codec Codec to get encoders for
	 * @param cfgDisabledEncodersList List of disabled encoders that should be excluded from the result
	 * @return List of encoders sorted by priority
	 */
	public static @NonNull List<@NonNull String> getVideoEncodersForCodec(
				@NonNull FfmpegCodec codec,
				@NonNull Set<@NonNull String> cfgDisabledEncodersList
			) {
		Map<@NonNull FfmpegCodec, @NonNull List<@NonNull String>> tmpMap = getAvailableEncoders(
				true,
				cfgDisabledEncodersList
			);
		return tmpMap.getOrDefault(codec, Collections.emptyList());
	}

	/**
	 * Get all available video encoders per codec
	 * @param cfgDisabledEncodersList List of disabled encoders that should be excluded from the result
	 * @return Encoders per codec, sorted by priority
	 */
	public static @NonNull Map<@NonNull FfmpegCodec, @NonNull List<@NonNull String>> getVideoEncoders(
				@NonNull Set<@NonNull String> cfgDisabledEncodersList
			) {
		return getAvailableEncoders(true, cfgDisabledEncodersList);
	}

	/**
	 * Get a list of audio encoders that are available for the given codec
	 * @param codec Codec to get encoders for
	 * @param cfgDisabledEncodersList List of disabled encoders that should be excluded from the result
	 * @return List of encoders sorted by priority
	 */
	public static @NonNull List<@NonNull String> getAudioEncodersForCodec(
				@NonNull FfmpegCodec codec,
				@NonNull Set<@NonNull String> cfgDisabledEncodersList
			) {
		Map<@NonNull FfmpegCodec, @NonNull List<@NonNull String>> tmpMap = getAvailableEncoders(
				false,
				cfgDisabledEncodersList
			);
		return tmpMap.getOrDefault(codec, Collections.emptyList());
	}

	/**
	 * Get all available audio encoders per codec
	 * @param cfgDisabledEncodersList List of disabled encoders that should be excluded from the result
	 * @return Encoders per codec, sorted by priority
	 */
	public static @NonNull Map<@NonNull FfmpegCodec, @NonNull List<@NonNull String>> getAudioEncoders(
				@NonNull Set<@NonNull String> cfgDisabledEncodersList
			) {
		return getAvailableEncoders(false, cfgDisabledEncodersList);
	}

	/**
	 * Get an unfiltered list of all encoders
	 * @return All available encoders
	 */
	public static @NonNull Set<@NonNull String> getUnfilteredEncoderList() {
		return getUnfilteredEnDeCoders(true);
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private static @NonNull Map<@NonNull FfmpegCodec, @NonNull List<@NonNull String>> getAvailableEncoders(
				boolean forVideo,
				@NonNull Set<@NonNull String> cfgDisabledEncodersList
			) {
		Map<FfmpegCodec, List<String>> searchMapPtr = (forVideo ? ENCODER_LIST_BY_VIDEO_CODEC : ENCODER_LIST_BY_AUDIO_CODEC);
		return getAvailableEnDeCoders(searchMapPtr, getUnfilteredEnDeCoders(true), cfgDisabledEncodersList);
	}

}
