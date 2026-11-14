package io.github.tsitle.lib_ffmpeg.tc;

import io.github.tsitle.lib_ffmpeg.FfmpegCodec;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * Helper class to find available decoders.
 */
public final class FfmpegTcDecoderLists extends FfmpegTcEnDeCoderListsBase {

	/** AV1: Rockchip hardware decoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String DECODER_NAME_V_AV1_RKMPP = "av1_rkmpp";
	/** AV1: NVidia hardware decoder */
	public static final String DECODER_NAME_V_AV1_NVDEC = "av1_cuvid";

	/** H264: Rockchip hardware decoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String DECODER_NAME_V_H264_RKMPP = "h264_rkmpp";
	/** H264: NVidia hardware decoder */
	public static final String DECODER_NAME_V_H264_NVDEC = "h264_cuvid";

	/** H265/HEVC: Rockchip hardware decoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String DECODER_NAME_V_H265_RKMPP = "hevc_rkmpp";
	/** H265/HEVC: NVidia hardware decoder */
	public static final String DECODER_NAME_V_H265_NVDEC = "hevc_cuvid";

	/** MJPEG: Rockchip hardware decoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String DECODER_NAME_V_MJPEG_RKMPP = "mjpeg_rkmpp";
	/** MJPEG: NVidia hardware decoder */
	public static final String DECODER_NAME_V_MJPEG_NVDEC = "mjpeg_cuvid";

	/** MPEG2: Rockchip hardware decoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String DECODER_NAME_V_MPEG2_RKMPP = "mpeg2_rkmpp";
	/** MPEG2: NVidia hardware decoder */
	public static final String DECODER_NAME_V_MPEG2_NVDEC = "mpeg2_cuvid";

	/** MPEG4: Rockchip hardware decoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String DECODER_NAME_V_MPEG4_RKMPP = "mpeg4_rkmpp";
	/** MPEG4: NVidia hardware decoder */
	public static final String DECODER_NAME_V_MPEG4_NVDEC = "mpeg4_cuvid";

	/** VP8: Rockchip hardware decoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String DECODER_NAME_V_VP8_RKMPP = "vp8_rkmpp";
	/** VP8: NVidia hardware decoder */
	public static final String DECODER_NAME_V_VP8_NVDEC = "vp8_cuvid";

	/** VP9: Rockchip hardware decoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String DECODER_NAME_V_VP9_RKMPP = "vp9_rkmpp";
	/** VP9: NVidia hardware decoder */
	public static final String DECODER_NAME_V_VP9_NVDEC = "vp9_cuvid";

	// -----------------------------------------------------

	private static final @NonNull List<@NonNull String> DECODERS_V_AV1 = new ArrayList<>() {{
			add(DECODER_NAME_V_AV1_RKMPP);
			add(DECODER_NAME_V_AV1_NVDEC);
			add("av1");
			add("libaom-av1");
			add("av1_qsv");  // untested
		}};

	private static final @NonNull List<@NonNull String> DECODERS_V_H264 = new ArrayList<>() {{
			add(DECODER_NAME_V_H264_RKMPP);
			add(DECODER_NAME_V_H264_NVDEC);
			add("h264");
			add("libopenh264");
			add("h264_v4l2m2m");  // untested
			add("h264_qsv");  // untested
		}};

	private static final @NonNull List<@NonNull String> DECODERS_V_H265 = new ArrayList<>() {{
			add(DECODER_NAME_V_H265_RKMPP);
			add(DECODER_NAME_V_H265_NVDEC);
			add("hevc");
			add("hevc_v4l2m2m");  // untested
			add("hevc_qsv");  // untested
		}};

	private static final @NonNull List<@NonNull String> DECODERS_V_MJPEG = new ArrayList<>() {{
			add(DECODER_NAME_V_MJPEG_RKMPP);
			add(DECODER_NAME_V_MJPEG_NVDEC);
			add("mjpeg");
			add("mjpeg_qsv");  // untested
		}};

	private static final @NonNull List<@NonNull String> DECODERS_V_MPEG2 = new ArrayList<>() {{
			add(DECODER_NAME_V_MPEG2_RKMPP);
			add(DECODER_NAME_V_MPEG2_NVDEC);
			add("mpeg2video");
			add("mpeg2_v4l2m2m");  // untested
			add("mpeg2_qsv");  // untested
		}};

	private static final @NonNull List<@NonNull String> DECODERS_V_MPEG4 = new ArrayList<>() {{
			add(DECODER_NAME_V_MPEG4_RKMPP);
			add(DECODER_NAME_V_MPEG4_NVDEC);
			add("mpeg4");
			add("mpeg4_v4l2m2m");  // untested
		}};

	private static final @NonNull List<@NonNull String> DECODERS_V_THEORA = new ArrayList<>() {{
			add("theora");
		}};

	private static final @NonNull List<@NonNull String> DECODERS_V_VP8 = new ArrayList<>() {{
			add(DECODER_NAME_V_VP8_RKMPP);
			add(DECODER_NAME_V_VP8_NVDEC);
			add("libvpx");
			add("vp8");
			add("vp8_v4l2m2m");  // untested
			add("vp8_qsv");  // untested
		}};

	private static final @NonNull List<@NonNull String> DECODERS_V_VP9 = new ArrayList<>() {{
			add(DECODER_NAME_V_VP9_RKMPP);
			add(DECODER_NAME_V_VP9_NVDEC);
			add("libvpx-vp9");
			add("vp9");
			add("vp9_v4l2m2m");  // untested
			add("vp9_qsv");  // untested
		}};

	private static final @NonNull Map<@NonNull FfmpegCodec, List<@NonNull String>> DECODER_LIST_BY_VIDEO_CODEC =
			new HashMap<>() {{  // @CODEC
					put(FfmpegCodec.V_AV1, DECODERS_V_AV1);
					put(FfmpegCodec.V_H264, DECODERS_V_H264);
					put(FfmpegCodec.V_H265, DECODERS_V_H265);
					put(FfmpegCodec.V_MJPEG, DECODERS_V_MJPEG);
					put(FfmpegCodec.V_MPEG2, DECODERS_V_MPEG2);
					put(FfmpegCodec.V_MPEG4, DECODERS_V_MPEG4);
					put(FfmpegCodec.V_THEORA, DECODERS_V_THEORA);
					put(FfmpegCodec.V_VP8, DECODERS_V_VP8);
					put(FfmpegCodec.V_VP9, DECODERS_V_VP9);
				}};

	// -----------------------------------------------------

	private static final @NonNull Map<@NonNull FfmpegCodec, List<@NonNull String>> DECODER_LIST_BY_AUDIO_CODEC =
			new HashMap<>() {{  // @CODEC
					put(FfmpegCodec.A_AAC, List.of("aac", "aac_fixed", "aac_latm"));
					put(FfmpegCodec.A_AC3, List.of("ac3", "ac3_fixed"));
					put(FfmpegCodec.A_ALAC, List.of("alac"));
					put(FfmpegCodec.A_EAC3, List.of("eac3"));
					put(FfmpegCodec.A_FLAC, List.of("flac"));
					put(FfmpegCodec.A_MP2, List.of("mp2", "mp2float"));
					put(FfmpegCodec.A_MP3, List.of("mp3", "mp3float"));
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

	private FfmpegTcDecoderLists() { }

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * Get a list of video decoders that are available for the given codec
	 * @param codec Codec to get decoders for
	 * @param cfgDisabledDecodersList List of disabled decoders that should be excluded from the result
	 * @return List of decoders sorted by priority
	 */
	public static @NonNull List<@NonNull String> getVideoDecodersForCodec(
				@NonNull FfmpegCodec codec,
				@NonNull Set<@NonNull String> cfgDisabledDecodersList
			) {
		Map<@NonNull FfmpegCodec, @NonNull List<@NonNull String>> tmpMap = getAvailableDecoders(
				true,
				cfgDisabledDecodersList
			);
		return tmpMap.getOrDefault(codec, Collections.emptyList());
	}

	/**
	 * Get all available video decoders per codec
	 * @param cfgDisabledDecodersList List of disabled decoders that should be excluded from the result
	 * @return Decoders per codec, sorted by priority
	 */
	public static @NonNull Map<@NonNull FfmpegCodec, @NonNull List<@NonNull String>> getVideoDecoders(
				@NonNull Set<@NonNull String> cfgDisabledDecodersList
			) {
		return getAvailableDecoders(true, cfgDisabledDecodersList);
	}

	/**
	 * Get a list of audio decoders that are available for the given codec
	 * @param codec Codec to get decoders for
	 * @param cfgDisabledDecodersList List of disabled decoders that should be excluded from the result
	 * @return List of decoders sorted by priority
	 */
	public static @NonNull List<@NonNull String> getAudioDecodersForCodec(
				@NonNull FfmpegCodec codec,
				@NonNull Set<@NonNull String> cfgDisabledDecodersList
			) {
		Map<@NonNull FfmpegCodec, @NonNull List<@NonNull String>> tmpMap = getAvailableDecoders(
				false,
				cfgDisabledDecodersList
			);
		return tmpMap.getOrDefault(codec, Collections.emptyList());
	}

	/**
	 * Get all available audio decoders per codec
	 * @param cfgDisabledDecodersList List of disabled decoders that should be excluded from the result
	 * @return Decoders per codec, sorted by priority
	 */
	public static @NonNull Map<@NonNull FfmpegCodec, @NonNull List<@NonNull String>> getAudioDecoders(
				@NonNull Set<@NonNull String> cfgDisabledDecodersList
			) {
		return getAvailableDecoders(false, cfgDisabledDecodersList);
	}

	/**
	 * Get an unfiltered list of all decoders
	 * @return All available decoders
	 */
	public static @NonNull Set<@NonNull String> getUnfilteredDecoderList() {
		return getUnfilteredEnDeCoders(false);
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private static @NonNull Map<@NonNull FfmpegCodec, @NonNull List<@NonNull String>> getAvailableDecoders(
				boolean forVideo,
				@NonNull Set<@NonNull String> cfgDisabledDecodersList
			) {
		Map<FfmpegCodec, List<String>> searchMapPtr = (forVideo ? DECODER_LIST_BY_VIDEO_CODEC : DECODER_LIST_BY_AUDIO_CODEC);
		return getAvailableEnDeCoders(searchMapPtr, getUnfilteredEnDeCoders(false), cfgDisabledDecodersList);
	}

}
