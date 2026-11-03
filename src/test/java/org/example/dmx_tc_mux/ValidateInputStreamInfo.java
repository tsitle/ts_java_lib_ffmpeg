package org.example.dmx_tc_mux;

import io.github.tsitle.lib_ffmpeg.demux.FfmpegDemuxer;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDmxSettingsRsi;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDmxSubStreamInfoAudio;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDmxSubStreamInfoVideo;
import io.github.tsitle.lib_ffmpeg.exceptions.FfmpegGenericException;
import io.github.tsitle.lib_xrtxp.avdata.extradata.ExtradataContainerHex;
import io.github.tsitle.lib_xrtxp.avdata.extradata.ExtradataConvHexFmtHelper;
import io.github.tsitle.lib_xrtxp.common.logmsgs.LogMsgInterface;
import io.github.tsitle.lib_xrtxp.common.logmsgs.RtxpLogLevel;
import org.jspecify.annotations.NonNull;

final class ValidateInputStreamInfo {

	private final @NonNull LogMsgInterface logMsgInterface;

	ValidateInputStreamInfo(@NonNull LogMsgInterface logMsgInterface) {
		this.logMsgInterface = logMsgInterface;
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	boolean testReadStreamInfo(
				@NonNull ConfigDmxTcMux localCfg,
				@NonNull FfmpegDmxSubStreamInfoVideo expectedSsInfoVid,
				@NonNull FfmpegDmxSubStreamInfoAudio expectedSsInfoAud
			) {
		final String FNC_NAME = getClass().getSimpleName() + ".testReadStreamInfo()";

		FfmpegDmxSubStreamInfoVideo localSsInfoVid = new FfmpegDmxSubStreamInfoVideo();
		FfmpegDmxSubStreamInfoAudio localSsInfoAud = new FfmpegDmxSubStreamInfoAudio();

		try {
			FfmpegDmxSettingsRsi dmxSettingsRsi = new FfmpegDmxSettingsRsi();
			localCfg.copyToDemuxerSettings_rsi(dmxSettingsRsi);

			FfmpegDemuxer.readStreamInfos(
					null,
					localCfg.dmx.inputFilePath,
					dmxSettingsRsi,
					localSsInfoVid,
					localSsInfoAud
				);

			// check video params
			if (! checkVideo(FNC_NAME, expectedSsInfoVid, localSsInfoVid)) {
				return false;
			}

			// check audio params
			if (! checkAudio(FNC_NAME, expectedSsInfoAud, localSsInfoAud)) {
				return false;
			}

			// output stream info
			/*if (localSsInfoVid.subStreamIx != -1) {
				logDebug(FNC_NAME, "Stream Info Vid: ED='" + localSsInfoVid.extradataHex.getEd() + "'");
			}
			if (localSsInfoAud.subStreamIx != -1) {
				logDebug(FNC_NAME, "Stream Info Aud: ED='" + localSsInfoAud.extradataHex.getEd() + "'");
			}*/
		} catch (FfmpegGenericException e) {
			logError(FNC_NAME, "FfmpegGenericException caught: " + e.getMessage());
			return false;
		}
		return true;
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private boolean checkVideo(
				@NonNull String fncName,
				@NonNull FfmpegDmxSubStreamInfoVideo expectedSsInfoVid,
				@NonNull FfmpegDmxSubStreamInfoVideo localSsInfoVid
			) {
		if (localSsInfoVid.subStreamIx != expectedSsInfoVid.subStreamIx) {
			logError(fncName, "Stream Info Vid: mismatch subStreamIx: " +
					"is=" + expectedSsInfoVid.subStreamIx + ", exp=" + localSsInfoVid.subStreamIx);
			return false;
		}
		if (localSsInfoVid.ffmpegCodec != expectedSsInfoVid.ffmpegCodec) {
			logError(fncName, "Stream Info Vid: mismatch ffmpegCodec: " +
					"is=" + expectedSsInfoVid.ffmpegCodec + ", exp=" + localSsInfoVid.ffmpegCodec);
			return false;
		}
		if (! localSsInfoVid.timeBasePts.equals(expectedSsInfoVid.timeBasePts)) {
			logError(fncName, "Stream Info Vid: mismatch timeBasePts: " +
					"is=" + expectedSsInfoVid.timeBasePts + ", exp=" + localSsInfoVid.timeBasePts);
			return false;
		}
		if (! localSsInfoVid.fps.equals(expectedSsInfoVid.fps)) {
			logError(fncName, "Stream Info Vid: mismatch fps: " +
					"is=" + expectedSsInfoVid.fps + ", exp=" + localSsInfoVid.fps);
			return false;
		}
		if (! localSsInfoVid.imgDims.equals(expectedSsInfoVid.imgDims)) {
			logError(fncName, "Stream Info Vid: mismatch imgDims: " +
					"is=" + expectedSsInfoVid.imgDims + ", exp=" + localSsInfoVid.imgDims);
			return false;
		}
		if (localSsInfoVid.pixelFmt != expectedSsInfoVid.pixelFmt) {
			logError(fncName, "Stream Info Vid: mismatch pixelFmt: " +
					"is=" + expectedSsInfoVid.pixelFmt + ", exp=" + localSsInfoVid.pixelFmt);
			return false;
		}
		if (localSsInfoVid.extradataHex.isEmpty() != expectedSsInfoVid.extradataHex.isEmpty()) {
			logError(fncName, "Stream Info Vid: mismatch extradataHex: " +
					"is=" + expectedSsInfoVid.extradataHex + ", exp=" + localSsInfoVid.extradataHex);
			return false;
		}
		if (! localSsInfoVid.extradataHex.isEmpty() && localSsInfoVid.extradataHex.isCodecH264()) {
			ExtradataContainerHex tmpEchLocal =
					ExtradataConvHexFmtHelper.convertH264EncoderExtradata(true, localSsInfoVid.extradataHex);
			ExtradataContainerHex tmpEchIs =
					ExtradataConvHexFmtHelper.convertH264EncoderExtradata(true, expectedSsInfoVid.extradataHex);
			if (! tmpEchLocal.equals(tmpEchIs)) {
				logError(fncName, "Stream Info Vid: mismatch H264 extradataHex: " +
						"is=" + tmpEchIs + ", exp=" + tmpEchLocal);
				return false;
			}
		} else if (! localSsInfoVid.extradataHex.isEmpty() && localSsInfoVid.extradataHex.isCodecH265()) {
			ExtradataContainerHex tmpEchLocal =
					ExtradataConvHexFmtHelper.convertH265EncoderExtradata(true, localSsInfoVid.extradataHex);
			ExtradataContainerHex tmpEchIs =
					ExtradataConvHexFmtHelper.convertH265EncoderExtradata(true, expectedSsInfoVid.extradataHex);
			if (! tmpEchLocal.equals(tmpEchIs)) {
				logError(fncName, "Stream Info Vid: mismatch H265 extradataHex: " +
						"is=" + tmpEchIs + ", exp=" + tmpEchLocal);
				return false;
			}
		} else if (! localSsInfoVid.extradataHex.equals(expectedSsInfoVid.extradataHex)) {
			logError(fncName, "Stream Info Vid: mismatch xxx extradataHex: " +
					"is=" + expectedSsInfoVid.extradataHex + ", exp=" + localSsInfoVid.extradataHex);
			return false;
		}
		return true;
	}

	private boolean checkAudio(
				@NonNull String fncName,
				@NonNull FfmpegDmxSubStreamInfoAudio expectedSsInfoAud,
				@NonNull FfmpegDmxSubStreamInfoAudio localSsInfoAud
			) {
		if (localSsInfoAud.subStreamIx != expectedSsInfoAud.subStreamIx) {
			logError(fncName, "Stream Info Aud: mismatch subStreamIx: " +
					"is=" + expectedSsInfoAud.subStreamIx + ", exp=" + localSsInfoAud.subStreamIx);
			return false;
		}
		if (localSsInfoAud.ffmpegCodec != expectedSsInfoAud.ffmpegCodec) {
			logError(fncName, "Stream Info Aud: mismatch ffmpegCodec: " +
					"is=" + expectedSsInfoAud.ffmpegCodec + ", exp=" + localSsInfoAud.ffmpegCodec);
			return false;
		}
		if (! localSsInfoAud.timeBasePts.equals(expectedSsInfoAud.timeBasePts)) {
			logError(fncName, "Stream Info Aud: mismatch timeBasePts: " +
					"is=" + expectedSsInfoAud.timeBasePts + ", exp=" + localSsInfoAud.timeBasePts);
			return false;
		}
		if (localSsInfoAud.sampleRate != expectedSsInfoAud.sampleRate) {
			logError(fncName, "Stream Info Aud: mismatch sampleRate: " +
					"is=" + expectedSsInfoAud.sampleRate + ", exp=" + localSsInfoAud.sampleRate);
			return false;
		}
		if (localSsInfoAud.channelCount != expectedSsInfoAud.channelCount) {
			logError(fncName, "Stream Info Aud: mismatch channelCount: " +
					"is=" + expectedSsInfoAud.channelCount + ", exp=" + localSsInfoAud.channelCount);
			return false;
		}
		if (! localSsInfoAud.extradataHex.equals(expectedSsInfoAud.extradataHex)) {
			logError(fncName, "Stream Info Aud: mismatch extradataHex: " +
					"is=" + expectedSsInfoAud.extradataHex + ", exp=" + localSsInfoAud.extradataHex);
			return false;
		}
		return true;
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private void logDebug(@NonNull String fncName, @NonNull String msg) {
		internalLog(RtxpLogLevel.DEBUG, fncName, msg);
	}

	@SuppressWarnings("unused")
	private void logInfo(@NonNull String fncName, @NonNull String msg) {
		internalLog(RtxpLogLevel.INFO, fncName, msg);
	}

	private void logError(@NonNull String fncName, @NonNull String msg) {
		internalLog(RtxpLogLevel.ERROR, fncName, msg);
	}

	private void internalLog(@NonNull RtxpLogLevel logLevel, @NonNull String fncName, @NonNull String msg) {
		logMsgInterface.addMsgForLogThread(logLevel, Thread.currentThread().getName(), fncName + ": " + msg);
	}

}
