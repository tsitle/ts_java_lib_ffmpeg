package org.example.dmx_tc_mux;

import io.github.tsitle.lib_ffmpeg.FfmpegAudioBitRate;
import io.github.tsitle.lib_ffmpeg.FfmpegCodec;
import io.github.tsitle.lib_ffmpeg.FfmpegPktConvModeAac;
import io.github.tsitle.lib_ffmpeg.FfmpegPktConvModeH26x;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDmxSettingsBase;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDmxSettingsDemux;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDmxSettingsRsi;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDmxSettingsTc;
import io.github.tsitle.lib_ffmpeg.tc.FfmpegTcSettingsOutAudio;
import io.github.tsitle.lib_ffmpeg.tc.FfmpegTcSettingsOutVideo;
import io.github.tsitle.lib_xrtxp.common.types.FrameRateEnum;
import io.github.tsitle.lib_xrtxp.common.types.SampleRateEnum;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.Set;

final class ConfigDmxTcMux {

	static class Dmx {
		public @NonNull String inputFilePath = "";
		public long maxSecs = -1L;

		/** Select a specific video stream by number (<= 0 means auto-selection) */
		public int selectStreamNumberVideo = -1;
		/** Select a specific audio stream by number (<= 0 means auto-selection) */
		public int selectStreamNumberAudio = -1;
		/** Allow only specific codecs for video (true) or all? */
		public boolean allowOnlySpecificCodecsVideo = false;
		/** Allow only specific codecs for audio (true) or all? */
		public boolean allowOnlySpecificCodecsAudio = false;
		/** Allowed video codecs (only used if [allowOnlySpecificCodecsVideo] is true) */
		public @NonNull Set<@NonNull FfmpegCodec> allowedCodecsVideo = new HashSet<>();
		/** Allowed audio codecs (only used if [allowOnlySpecificCodecsAudio] is true) */
		public @NonNull Set<@NonNull FfmpegCodec> allowedCodecsAudio = new HashSet<>();

		public @NonNull FfmpegPktConvModeH26x demuxOnly_modeH26x = FfmpegPktConvModeH26x.PASSTHROUGH;
		public @NonNull FfmpegPktConvModeAac demuxOnly_modeAac = FfmpegPktConvModeAac.PASSTHROUGH;
	}

	static class TcVid {
		public boolean copySubStreamVideo = false;
		public @NonNull FfmpegCodec videoTargetCodec = FfmpegCodec.UNKNOWN;
		public FfmpegTcSettingsOutVideo.@NonNull ImageScalingMode videoImgScalingMode =
				FfmpegTcSettingsOutVideo.ImageScalingMode.PASSTHROUGH;
		public int videoImgDimsMax = -1;
		public int videoImgDimsFixed = -1;
		public FfmpegTcSettingsOutVideo.@NonNull FrameRateConversionMode videoFrameRateCm =
				FfmpegTcSettingsOutVideo.FrameRateConversionMode.PASSTHROUGH;
		public @NonNull FrameRateEnum videoFrameRateMax = FrameRateEnum.UNKNOWN;
		public @NonNull FrameRateEnum videoFrameRateFixed = FrameRateEnum.UNKNOWN;
		public int videoBitRateKbps = -1;
		public FfmpegTcSettingsOutVideo.@NonNull QualitySpeedRatio videoQualitySpeedRatioAv1 =
				FfmpegTcSettingsOutVideo.QualitySpeedRatio.BALANCED;
		public FfmpegTcSettingsOutVideo.@NonNull QualitySpeedRatio videoQualitySpeedRatioVpX =
				FfmpegTcSettingsOutVideo.QualitySpeedRatio.BALANCED;
		public FfmpegTcSettingsOutVideo.@NonNull GopSize videoGopSize =
				FfmpegTcSettingsOutVideo.GopSize.EVERY_60_FRAMES;
		public @NonNull FfmpegPktConvModeH26x videoModeH26x = FfmpegPktConvModeH26x.PASSTHROUGH;
		public FfmpegTcSettingsOutVideo.@NonNull OutputModeMjpeg videoModeMjpeg =
				FfmpegTcSettingsOutVideo.OutputModeMjpeg.OPTIMIZED;

		@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
		public final @NonNull Set<@NonNull String> disabledEncList = new HashSet<>();
	}

	static class TcAud {
		public boolean copySubStreamAudio = false;
		public @NonNull FfmpegCodec audioTargetCodec = FfmpegCodec.UNKNOWN;
		public FfmpegTcSettingsOutAudio.@NonNull ChannelConversionMode audioChannelCm =
				FfmpegTcSettingsOutAudio.ChannelConversionMode.PASSTHROUGH;
		public FfmpegTcSettingsOutAudio.@NonNull SampleRateConversionMode audioSrCm =
				FfmpegTcSettingsOutAudio.SampleRateConversionMode.AUTO;
		public @NonNull SampleRateEnum audioSampleRateFixed = SampleRateEnum.UNKNOWN;
		public @NonNull SampleRateEnum audioSampleRateMax = SampleRateEnum.SR_048000;
		public @NonNull FfmpegAudioBitRate audioBitRateKbps = FfmpegAudioBitRate.UNKNOWN;
		public @NonNull FfmpegPktConvModeAac audioModeAac = FfmpegPktConvModeAac.PASSTHROUGH;
		public FfmpegTcSettingsOutAudio.@NonNull OutputModeOpus cfgOutputModeOpus =
				FfmpegTcSettingsOutAudio.OutputModeOpus.DEFAULT;
	}

	static class Tc {
		public final @NonNull TcVid vid = new TcVid();
		public final @NonNull TcAud aud = new TcAud();
	}

	static class Mux {
		public @NonNull String outputFilePath = "";
		public boolean preferMkvOverMp4 = false;
	}

	final @NonNull Dmx dmx = new Dmx();
	final @NonNull Tc tc = new Tc();
	final @NonNull Mux mux = new Mux();

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	void copyToDemuxerSettings_rsi(@NonNull FfmpegDmxSettingsRsi dmxSettings) {
		copyToDemuxerSettings_base(dmxSettings);
	}

	void copyToDemuxerSettings_demux(@NonNull FfmpegDmxSettingsDemux dmxSettings) {
		dmxSettings.cfgMaxSecs = dmx.maxSecs;
		dmxSettings.cfgOutputModeH26x = dmx.demuxOnly_modeH26x;
		dmxSettings.cfgOutputModeAac = dmx.demuxOnly_modeAac;
		copyToDemuxerSettings_base(dmxSettings);
	}

	void copyToDemuxerSettings_tc(@NonNull FfmpegDmxSettingsTc dmxSettings) {
		dmxSettings.cfgMaxSecs = dmx.maxSecs;
		copyToDemuxerSettings_base(dmxSettings);
	}

	private void copyToDemuxerSettings_base(@NonNull FfmpegDmxSettingsBase dmxSettings) {
		dmxSettings.cfgSelectStreamNumberVideo = dmx.selectStreamNumberVideo;
		dmxSettings.cfgSelectStreamNumberAudio = dmx.selectStreamNumberAudio;

		dmxSettings.cfgAllowOnlySpecificCodecsVideo = dmx.allowOnlySpecificCodecsVideo;
		dmxSettings.cfgAllowedCodecsVideo.clear();
		if (dmx.allowOnlySpecificCodecsVideo) {
			dmxSettings.cfgAllowedCodecsVideo.addAll(dmx.allowedCodecsVideo);
		}
		dmxSettings.cfgAllowOnlySpecificCodecsAudio = dmx.allowOnlySpecificCodecsAudio;
		dmxSettings.cfgAllowedCodecsAudio.clear();
		if (dmx.allowOnlySpecificCodecsAudio) {
			dmxSettings.cfgAllowedCodecsAudio.addAll(dmx.allowedCodecsAudio);
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	void copyToTcSettings(
				boolean haveVideo,
				@NonNull FfmpegTcSettingsOutVideo tcSettingsVideo,
				boolean haveAudio,
				@NonNull FfmpegTcSettingsOutAudio tcSettingsAudio
			) {
		tcSettingsVideo.clear();
		if (haveVideo) {
			tcSettingsVideo.cfgCopySubStream = tc.vid.copySubStreamVideo;
			if (! tc.vid.copySubStreamVideo) {
				tcSettingsVideo.cfgFfmpegCodec = tc.vid.videoTargetCodec;

				tcSettingsVideo.cfgImgScalingMode = tc.vid.videoImgScalingMode;
				if (tc.vid.videoImgScalingMode == FfmpegTcSettingsOutVideo.ImageScalingMode.FIXED) {
					tcSettingsVideo.cfgImgDimsFixed = tc.vid.videoImgDimsFixed;
				} else if (tc.vid.videoImgScalingMode == FfmpegTcSettingsOutVideo.ImageScalingMode.LIMITED) {
					tcSettingsVideo.cfgImgDimsMax = tc.vid.videoImgDimsMax;
				}

				tcSettingsVideo.cfgFrCm = tc.vid.videoFrameRateCm;
				if (tc.vid.videoFrameRateCm == FfmpegTcSettingsOutVideo.FrameRateConversionMode.FIXED) {
					tcSettingsVideo.cfgFrameRateFixed = tc.vid.videoFrameRateFixed;
				} else if (tc.vid.videoFrameRateCm == FfmpegTcSettingsOutVideo.FrameRateConversionMode.LIMITED) {
					tcSettingsVideo.cfgFrameRateMax = tc.vid.videoFrameRateMax;
				}

				tcSettingsVideo.cfgBitRateKbps = tc.vid.videoBitRateKbps;
				tcSettingsVideo.cfgQualitySpeedRatioAv1 = tc.vid.videoQualitySpeedRatioAv1;
				tcSettingsVideo.cfgQualitySpeedRatioVpX = tc.vid.videoQualitySpeedRatioVpX;

				tcSettingsVideo.cfgGopSize = tc.vid.videoGopSize;

				tcSettingsVideo.cfgOutputModeH26x = tc.vid.videoModeH26x;
				tcSettingsVideo.cfgOutputModeMjpeg = tc.vid.videoModeMjpeg;

				tcSettingsVideo.cfgDisabledEncodersList.clear();
				tcSettingsVideo.cfgDisabledEncodersList.addAll(tc.vid.disabledEncList);
			}
		}

		tcSettingsAudio.clear();
		if (haveAudio) {
			tcSettingsAudio.cfgCopySubStream = tc.aud.copySubStreamAudio;
			if (! tc.aud.copySubStreamAudio) {
				tcSettingsAudio.cfgFfmpegCodec = tc.aud.audioTargetCodec;

				tcSettingsAudio.cfgChannelCm = tc.aud.audioChannelCm;

				tcSettingsAudio.cfgSrCm = tc.aud.audioSrCm;
				if (tc.aud.audioSrCm == FfmpegTcSettingsOutAudio.SampleRateConversionMode.FIXED) {
					tcSettingsAudio.cfgSampleRateFixed = tc.aud.audioSampleRateFixed;
				} else if (tc.aud.audioSrCm == FfmpegTcSettingsOutAudio.SampleRateConversionMode.AUTO) {
					tcSettingsAudio.cfgSampleRateMax = tc.aud.audioSampleRateMax;
				}

				tcSettingsAudio.cfgBitRateKbps = tc.aud.audioBitRateKbps;

				tcSettingsAudio.cfgOutputModeAac = tc.aud.audioModeAac;
				tcSettingsAudio.cfgOutputModeOpus = tc.aud.cfgOutputModeOpus;
			}
		}
	}

}
