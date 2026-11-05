package org.example.dmx_tc_mux;

import io.github.tsitle.lib_ffmpeg.FfmpegAudioBitRate;
import io.github.tsitle.lib_ffmpeg.FfmpegAvPktBasics;
import io.github.tsitle.lib_ffmpeg.FfmpegCodec;
import io.github.tsitle.lib_ffmpeg.FfmpegPixelFmt;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDemuxer;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDmxSettingsDemux;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDmxSubStreamInfoAudio;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDmxSubStreamInfoVideo;
import io.github.tsitle.lib_ffmpeg.exceptions.FfmpegDecoderNotFoundException;
import io.github.tsitle.lib_ffmpeg.exceptions.FfmpegEncoderNotFoundException;
import io.github.tsitle.lib_ffmpeg.exceptions.FfmpegGenericException;
import io.github.tsitle.lib_ffmpeg.helpers.FfmpegHelperFfLogLevel;
import io.github.tsitle.lib_ffmpeg.mux.FfmpegMuxer;
import io.github.tsitle.lib_ffmpeg.tc.*;
import io.github.tsitle.lib_xrtxp.common.logmsgs.LogMsgInterface;
import io.github.tsitle.lib_xrtxp.common.logmsgs.RtxpLogLevel;
import io.github.tsitle.lib_xrtxp.common.types.FrameRateEnum;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

class MuxFromSubStreams implements LogMsgInterface  {

	private static final RtxpLogLevel CFG_LOG_LEVEL = RtxpLogLevel.DEBUG;
	private static final int CFG_DEMUX_SELECT_STREAM_NUMBER_VIDEO = -1;
	private static final int CFG_DEMUX_SELECT_STREAM_NUMBER_AUDIO = -1;
	private static final boolean CFG_DEMUX_ALLOW_ONLY_SPEC_CODECS_VIDEO = true;
	private static final boolean CFG_DEMUX_ALLOW_ONLY_SPEC_CODECS_AUDIO = true;
	private static final Set<@NonNull FfmpegCodec> CFG_DEMUX_ALLOWED_CODECS_VIDEO = new HashSet<>(Arrays.asList(  // @CODEC
			FfmpegCodec.V_AV1,
			FfmpegCodec.V_H264,
			FfmpegCodec.V_H265,
			FfmpegCodec.V_MJPEG,
			FfmpegCodec.V_MPEG4,
			FfmpegCodec.V_MPEG2,
			FfmpegCodec.V_THEORA,
			FfmpegCodec.V_VP8,
			FfmpegCodec.V_VP9
		));
	private static final Set<@NonNull FfmpegCodec> CFG_DEMUX_ALLOWED_CODECS_AUDIO = new HashSet<>(Arrays.asList(
			FfmpegCodec.A_AAC,
			FfmpegCodec.A_AC3,
			FfmpegCodec.A_ALAC,
			FfmpegCodec.A_EAC3,
			FfmpegCodec.A_FLAC,
			FfmpegCodec.A_MP3,
			FfmpegCodec.A_OPUS,
			FfmpegCodec.A_VORBIS
		));

	private final FfmpegDmxSubStreamInfoVideo inputSsInfoVid = new FfmpegDmxSubStreamInfoVideo();
	private final FfmpegDmxSubStreamInfoAudio inputSsInfoAud = new FfmpegDmxSubStreamInfoAudio();

	private final FfmpegTcSettingsOutVideo tcSettingsVideo = new FfmpegTcSettingsOutVideo();
	private final FfmpegTcSettingsOutAudio tcSettingsAudio = new FfmpegTcSettingsOutAudio();

	MuxFromSubStreams(
				@NonNull FfmpegDmxSubStreamInfoVideo inputSsInfoVid,
				@NonNull FfmpegDmxSubStreamInfoAudio inputSsInfoAud
			) {
		if (inputSsInfoVid.ffmpegCodec != FfmpegCodec.UNKNOWN &&
				! CFG_DEMUX_ALLOWED_CODECS_VIDEO.contains(inputSsInfoVid.ffmpegCodec)) {
			throw new IllegalArgumentException(getClass().getSimpleName() + ".ctor(): " +
					"Input video stream codec is not allowed (" + inputSsInfoVid.ffmpegCodec + ")");
		}
		if (inputSsInfoAud.ffmpegCodec != FfmpegCodec.UNKNOWN &&
				! CFG_DEMUX_ALLOWED_CODECS_AUDIO.contains(inputSsInfoAud.ffmpegCodec)) {
			throw new IllegalArgumentException(getClass().getSimpleName() + ".ctor(): " +
					"Input audio stream codec is not allowed (" + inputSsInfoAud.ffmpegCodec + ")");
		}
		this.inputSsInfoVid.copyFrom(inputSsInfoVid);
		this.inputSsInfoAud.copyFrom(inputSsInfoAud);

		//
		if (this.inputSsInfoVid.pixelFmt == FfmpegPixelFmt.UNKNOWN) {
			this.inputSsInfoVid.pixelFmt = FfmpegPixelFmt.YUV420P;  // set to default
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	@Override
	public synchronized void addMsgForLogThread(
				@NonNull RtxpLogLevel logLevel,
				@NonNull String threadId,
				@NonNull String msg
			) {
		if (logLevel == RtxpLogLevel.DEBUG && CFG_LOG_LEVEL != RtxpLogLevel.DEBUG) { return; }
		if (logLevel == RtxpLogLevel.INFO && (CFG_LOG_LEVEL == RtxpLogLevel.WARN || CFG_LOG_LEVEL == RtxpLogLevel.ERROR)) { return; }
		if (logLevel == RtxpLogLevel.WARN && CFG_LOG_LEVEL == RtxpLogLevel.ERROR) { return; }
		System.out.println("[" + logLevel + "] " + msg);
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	@SuppressWarnings("SameParameterValue")
	boolean start(@NonNull ConfigDmxTcMux localCfg) {
		final String FNC_NAME = getClass().getSimpleName() + ".start()";

		FfmpegHelperFfLogLevel.setLogLevel(RtxpLogLevel.ERROR);

		//
		localCfg.dmx.inputFilePath = "data/inp/" + localCfg.dmx.inputFilePath;
		localCfg.dmx.selectStreamNumberVideo = CFG_DEMUX_SELECT_STREAM_NUMBER_VIDEO;
		localCfg.dmx.selectStreamNumberAudio = CFG_DEMUX_SELECT_STREAM_NUMBER_AUDIO;
		localCfg.dmx.allowOnlySpecificCodecsVideo = CFG_DEMUX_ALLOW_ONLY_SPEC_CODECS_VIDEO;
		localCfg.dmx.allowedCodecsVideo = CFG_DEMUX_ALLOWED_CODECS_VIDEO;
		localCfg.dmx.allowOnlySpecificCodecsAudio = CFG_DEMUX_ALLOW_ONLY_SPEC_CODECS_AUDIO;
		localCfg.dmx.allowedCodecsAudio = CFG_DEMUX_ALLOWED_CODECS_AUDIO;
		localCfg.dmx.maxSecs = 60L;

		localCfg.tc.vid.videoBitRateKbps = 512 * 4;
		localCfg.tc.vid.videoImgScalingMode = FfmpegTcSettingsOutVideo.ImageScalingMode.LIMITED;
		localCfg.tc.vid.videoImgDimsMax = 1280;
		localCfg.tc.vid.videoImgDimsFixed = 2560;
		localCfg.tc.vid.videoFrameRateCm = FfmpegTcSettingsOutVideo.FrameRateConversionMode.LIMITED;
		localCfg.tc.vid.videoFrameRateMax = FrameRateEnum.FPS_30_0;
		localCfg.tc.vid.videoFrameRateFixed = FrameRateEnum.FPS_25_0;

		localCfg.tc.aud.audioBitRateKbps = FfmpegAudioBitRate.ABR_192;

		localCfg.mux.outputFilePath = "data/" + (new File("data/ramdisk").exists() ? "ramdisk" : "outp") +
				"/" + localCfg.mux.outputFilePath;

		logDebug(FNC_NAME, "Demux file: '" + localCfg.dmx.inputFilePath + "'" +
				(localCfg.dmx.maxSecs > 0 ? " (max secs: " + localCfg.dmx.maxSecs + ")" : ""));

		//
		ValidateInputStreamInfo tmpVisi = new ValidateInputStreamInfo(this);
		if (! tmpVisi.testReadStreamInfo(localCfg, inputSsInfoVid, inputSsInfoAud)) {
			return false;
		}

		//
		FfmpegDmxSettingsDemux dmxSettingsDemux = new FfmpegDmxSettingsDemux();
		localCfg.copyToDemuxerSettings_demux(dmxSettingsDemux);

		//
		localCfg.copyToTcSettings(
				inputSsInfoVid.subStreamIx >= 0,
				tcSettingsVideo,
				inputSsInfoAud.subStreamIx >= 0,
				tcSettingsAudio
			);

		//
		try (FfmpegDemuxer ffmpegDemuxer = FfmpegDemuxer.createForDemuxingOnly(
					this,
					localCfg.dmx.inputFilePath,
					dmxSettingsDemux
				)) {
			FfmpegTcParamsInpVideo sourceParamsVideo = new FfmpegTcParamsInpVideo();
			sourceParamsVideo.ffmpegCodec = inputSsInfoVid.ffmpegCodec;
			sourceParamsVideo.timeBase.copyFrom(inputSsInfoVid.timeBasePts);
			sourceParamsVideo.frameRate.copyFrom(inputSsInfoVid.fps);
			sourceParamsVideo.imgDims = inputSsInfoVid.imgDims;
			sourceParamsVideo.pixelFmt = inputSsInfoVid.pixelFmt;
			sourceParamsVideo.extradataHex.copyFrom(inputSsInfoVid.extradataHex);

			//
			FfmpegTcParamsInpAudio sourceParamsAudio = new FfmpegTcParamsInpAudio();
			sourceParamsAudio.ffmpegCodec = inputSsInfoAud.ffmpegCodec;
			sourceParamsAudio.timeBase.copyFrom(inputSsInfoAud.timeBasePts);
			sourceParamsAudio.sampleRate = inputSsInfoAud.sampleRate;
			sourceParamsAudio.channelCount = inputSsInfoAud.channelCount;
			sourceParamsAudio.extradataHex.copyFrom(inputSsInfoAud.extradataHex);

			//
			logDebug(FNC_NAME, "Input Video Stream Codec: " +
					(sourceParamsVideo.ffmpegCodec == FfmpegCodec.UNKNOWN ? "-none-" : sourceParamsVideo.ffmpegCodec));
			logDebug(FNC_NAME, "Input Audio Stream Codec: " +
					(sourceParamsAudio.ffmpegCodec == FfmpegCodec.UNKNOWN ? "-none-" : sourceParamsAudio.ffmpegCodec));

			//
			try (FfmpegTranscoder ffmpegTc = FfmpegTranscoder.createForAv(
						this,
						sourceParamsVideo,
						tcSettingsVideo,
						sourceParamsAudio,
						tcSettingsAudio
					)) {
				doDemuxingAndTranscodingAndMuxing_fromStream(localCfg, ffmpegDemuxer, ffmpegTc);
			}
		} catch (FfmpegGenericException e) {
			logError(FNC_NAME, "FfmpegGenericException caught: " + e.getMessage());
			return false;
		} catch (FfmpegDecoderNotFoundException e) {
			logError(FNC_NAME, "FfmpegDecoderNotFoundException caught: " + e.getMessage());
			return false;
		} catch (FfmpegEncoderNotFoundException e) {
			logError(FNC_NAME, "FfmpegEncoderNotFoundException caught: " + e.getMessage());
			return false;
		}
		return true;
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private void doDemuxingAndTranscodingAndMuxing_fromStream(
				@NonNull ConfigDmxTcMux localCfg,
				@NonNull FfmpegDemuxer ffmpegDemuxer,
				@NonNull FfmpegTranscoder ffmpegTc
			) throws FfmpegGenericException, FfmpegDecoderNotFoundException, FfmpegEncoderNotFoundException {
		final String FNC_NAME = getClass().getSimpleName() + ".doDemuxingAndTranscodingAndMuxing_fromStream()";

		logDebug(FNC_NAME, "Output Video Stream: " +
				(ffmpegTc.willProduceVideo() ?
						(tcSettingsVideo.cfgCopySubStream ? "copy" : "transcode --> " + tcSettingsVideo.cfgFfmpegCodec)
						: "-none-"
					)
			);
		logDebug(FNC_NAME, "Output Audio Stream: " +
				(ffmpegTc.willProduceAudio() ?
						(tcSettingsAudio.cfgCopySubStream ? "copy" : "transcode --> " + tcSettingsAudio.cfgFfmpegCodec)
						: "-none-"
					)
			);

		//
		Double tmpLastAvSkew = null;
		try (FfmpegMuxer ffmpegMuxer = FfmpegMuxer.createMuxer(
					this,
					tcSettingsVideo.cfgCopySubStream
				)) {
			doDemuxingAndTranscodingAndMuxing_fromStream_sub(
					localCfg,
					ffmpegDemuxer,
					ffmpegTc,
					ffmpegMuxer
				);

			//
			if (ffmpegMuxer.getLastAvSkew().isPresent()) {
				tmpLastAvSkew = ffmpegMuxer.getLastAvSkew().get();
			}
		}

		//
		if (tcSettingsVideo.cfgFfmpegCodec != FfmpegCodec.UNKNOWN) {
			long tcVideoFramesDropped = ffmpegTc.getDroppedFrameCount();
			logInfo(FNC_NAME, "dropped video frames: " + tcVideoFramesDropped);
		}
		if (tmpLastAvSkew != null) {
			logInfo(FNC_NAME, String.format("A/V skew: %.3fs", tmpLastAvSkew).replace(",", "."));
		}
	}

	private void doDemuxingAndTranscodingAndMuxing_fromStream_sub(
				@NonNull ConfigDmxTcMux localCfg,
				@NonNull FfmpegDemuxer ffmpegDemuxer,
				@NonNull FfmpegTranscoder ffmpegTc,
				@NonNull FfmpegMuxer ffmpegMuxer
			) throws FfmpegGenericException, FfmpegDecoderNotFoundException, FfmpegEncoderNotFoundException {
		final FfmpegAvPktBasics demuxedAvPkt = new FfmpegAvPktBasics();
		FfmpegTcAvPacketList tcAvPktListPtr;
		final FfmpegTcAvPacketBacklog tcAvPktBacklog = new FfmpegTcAvPacketBacklog();

		boolean haveOpenedMux = false;

		while (true) {
			FfmpegDemuxer.ReadResult tmpRr = ffmpegDemuxer.readNextAvPacket(demuxedAvPkt);
			if (tmpRr == FfmpegDemuxer.ReadResult.RR_EOF) {
				break;
			}
			tcAvPktListPtr = ffmpegTc.transcodePacketFromBuffer(demuxedAvPkt);

			if (! haveOpenedMux) {
				boolean tmpResB = MuxerHelperFncs.muxerOpen(
						localCfg.mux.outputFilePath,
						localCfg.mux.preferMkvOverMp4,
						localCfg.tc.vid.copySubStreamVideo,
						localCfg.tc.aud.copySubStreamAudio,
						ffmpegTc,
						ffmpegMuxer
					);
				if (! tmpResB) {
					tcAvPktBacklog.addAll(tcAvPktListPtr);
					continue;
				}
				haveOpenedMux = true;
			}

			MuxerHelperFncs.muxerWriteAllTranscodedPackets(tcAvPktBacklog, tcAvPktListPtr, ffmpegMuxer);
		}

		// drain the transcoder
		if (haveOpenedMux) {
			MuxerHelperFncs.muxerDrainTranscoder(ffmpegTc, tcAvPktBacklog, ffmpegMuxer);
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	private void logDebug(@NonNull String fncName, @NonNull String msg) {
		internalLog(RtxpLogLevel.DEBUG, fncName, msg);
	}

	private void logInfo(@NonNull String fncName, @NonNull String msg) {
		internalLog(RtxpLogLevel.INFO, fncName, msg);
	}

	private void logError(@NonNull String fncName, @NonNull String msg) {
		internalLog(RtxpLogLevel.ERROR, fncName, msg);
	}

	private void internalLog(@NonNull RtxpLogLevel logLevel, @NonNull String fncName, @NonNull String msg) {
		addMsgForLogThread(logLevel, Thread.currentThread().getName(), fncName + ": " + msg);
	}

}
