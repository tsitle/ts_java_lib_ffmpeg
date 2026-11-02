package io.github.tsitle.lib_ffmpeg.tc;

import org.jspecify.annotations.NonNull;
import io.github.tsitle.lib_ffmpeg.FfmpegCodec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FfmpegTcEncoderLists {

	/** AV1: Rockchip hardware encoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String ENCODER_NAME_AV1_RKMPP = "av1_rkmpp";
	/** AV1: NVidia hardware encoder */
	public static final String ENCODER_NAME_AV1_NVENC = "av1_nvenc";
	public static final String ENCODER_NAME_AV1_LIBSVTAV1 = "libsvtav1";
	public static final String ENCODER_NAME_AV1_LIBAOMAV1 = "libaom-av1";

	/** H264: Rockchip hardware encoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String ENCODER_NAME_H264_RKMPP = "h264_rkmpp";
	/** H264: NVidia hardware encoder */
	public static final String ENCODER_NAME_H264_NVENC = "h264_nvenc";
	public static final String ENCODER_NAME_H264_LIBOPENH264 = "libopenh264";

	/** H265/HEVC: Rockchip hardware encoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String ENCODER_NAME_H265_RKMPP = "hevc_rkmpp";
	/** H265/HEVC: NVidia hardware encoder */
	public static final String ENCODER_NAME_H265_NVENC = "hevc_nvenc";

	/** MJPEG: Rockchip hardware encoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String ENCODER_NAME_MJPEG_RKMPP = "mjpeg_rkmpp";

	/** MPEG2: Rockchip hardware encoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String ENCODER_NAME_MPEG2_RKMPP = "mpeg2_rkmpp";

	/** MPEG4: Rockchip hardware encoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String ENCODER_NAME_MPEG4_RKMPP = "mpeg4_rkmpp";

	/** VP8: Rockchip hardware encoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String ENCODER_NAME_VP8_RKMPP = "vp8_rkmpp";

	/** VP9: Rockchip hardware encoder for RK3588 and others (FFmpeg 8.1+) */
	public static final String ENCODER_NAME_VP9_RKMPP = "vp9_rkmpp";

	private FfmpegTcEncoderLists() { }

	public static final @NonNull List<@NonNull String> ENC_AV1 = new ArrayList<>() {{
			add(ENCODER_NAME_AV1_RKMPP);  // untested
			add(ENCODER_NAME_AV1_NVENC);
			add(ENCODER_NAME_AV1_LIBSVTAV1);
			add("av1_qsv");  // untested
			add("av1_amf");  // untested
			//add("av1_vaapi");  // untested, does not accept pixel format YUV420P and requires hardware frames
		}};

	public static final @NonNull List<@NonNull String> ENC_H264 = new ArrayList<>() {{
			add(ENCODER_NAME_H264_RKMPP);  // untested
			add(ENCODER_NAME_H264_NVENC);
			add("libx264");  // untested
			//add("h264_vulkan");  // untested, does not accept pixel format YUV420P and requires hardware frames
			add(ENCODER_NAME_H264_LIBOPENH264);
			add("h264_qsv");  // untested
			add("h264_amf");  // untested
			//add("h264_vaapi");  // untested, does not accept pixel format YUV420P and requires hardware frames
		}};

	public static final @NonNull List<@NonNull String> ENC_H265 = new ArrayList<>() {{
			add(ENCODER_NAME_H265_RKMPP);  // untested
			add(ENCODER_NAME_H265_NVENC);
			//add("hevc_vulkan");  // untested, does not accept pixel format YUV420P and requires hardware frames
			add("libx265");  // untested
			add("hevc_qsv");  // untested
			add("hevc_amf");  // untested
			//add("hevc_vaapi");  // untested, does not accept pixel format YUV420P and requires hardware frames
		}};

	public static final @NonNull List<@NonNull String> ENC_MJPEG = new ArrayList<>() {{
			add(ENCODER_NAME_MJPEG_RKMPP);  // untested
			add("mjpeg");
		}};

	public static final @NonNull List<@NonNull String> ENC_MPEG2 = new ArrayList<>() {{
			add(ENCODER_NAME_MPEG2_RKMPP);  // untested
			add("mpeg2video");
			add("mpeg2_qsv");  // untested
			//add("mpeg2_vaapi");  // untested, does not accept pixel format YUV420P and requires hardware frames
		}};

	public static final @NonNull List<@NonNull String> ENC_MPEG4 = new ArrayList<>() {{
			add(ENCODER_NAME_MPEG4_RKMPP);  // untested
			add("mpeg4");
			add("libxvid");  // untested
		}};

	public static final @NonNull List<@NonNull String> ENC_THEORA = new ArrayList<>() {{
			add("libtheora");  // untested
		}};

	public static final @NonNull List<@NonNull String> ENC_VP8 = new ArrayList<>() {{
			add(ENCODER_NAME_VP8_RKMPP);  // untested
			add("libvpx");
			//add("vp8_vaapi");  // untested, does not accept pixel format YUV420P and requires hardware frames
		}};

	public static final @NonNull List<@NonNull String> ENC_VP9 = new ArrayList<>() {{
			add(ENCODER_NAME_VP9_RKMPP);  // untested
			add("libvpx-vp9");
			//add("vp9_vaapi");  // untested, does not accept pixel format YUV420P and requires hardware frames
			add("vp9_qsv");  // untested
		}};

	public static final @NonNull Map<@NonNull FfmpegCodec, List<@NonNull String>> ENC_BY_CODEC = new HashMap<>() {{  // @CODEC
			put(FfmpegCodec.V_AV1, ENC_AV1);
			put(FfmpegCodec.V_H264, ENC_H264);
			put(FfmpegCodec.V_H265, ENC_H265);
			put(FfmpegCodec.V_MJPEG, ENC_MJPEG);
			put(FfmpegCodec.V_MPEG2, ENC_MPEG2);
			put(FfmpegCodec.V_MPEG4, ENC_MPEG4);
			put(FfmpegCodec.V_THEORA, ENC_THEORA);
			put(FfmpegCodec.V_VP8, ENC_VP8);
			put(FfmpegCodec.V_VP9, ENC_VP9);
		}};

}
