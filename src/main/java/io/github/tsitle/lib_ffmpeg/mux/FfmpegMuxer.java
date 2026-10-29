package io.github.tsitle.lib_ffmpeg.mux;

import io.github.tsitle.lib_ffmpeg.*;
import io.github.tsitle.lib_ffmpeg.helpers.*;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVIOContext;
import org.bytedeco.ffmpeg.avformat.AVOutputFormat;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.avutil.AVChannelLayout;
import org.bytedeco.ffmpeg.avutil.AVRational;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avformat;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.BytePointer;
import io.github.tsitle.lib_ffmpeg.*;
import io.github.tsitle.lib_ffmpeg.helpers.*;
import io.github.tsitle.lib_xrtxp.avdata.extradata.ExtradataContainerHex;
import io.github.tsitle.lib_xrtxp.avdata.extradata.ExtradataConvHexFmtHelper;
import io.github.tsitle.lib_xrtxp.common.buffers.BufferView;
import io.github.tsitle.lib_xrtxp.common.types.SampleRateEnum;
import io.github.tsitle.lib_ffmpeg.exceptions.FfmpegGenericException;
import io.github.tsitle.lib_xrtxp.common.logmsgs.LogMsgInterface;
import io.github.tsitle.lib_xrtxp.common.logmsgs.RtxpLogLevel;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Muxer for A/V streams.
 */
public final class FfmpegMuxer implements AutoCloseable {

	private static class AvSkewVars {
		double lastPtsSecVideo = Double.NaN;
		double lastDtsSecVideo = Double.NaN;
		double lastPtsSecAudio = Double.NaN;
		double lastDtsSecAudio = Double.NaN;
		double lastAvSkewPts = Double.NaN;
	}

	private final @Nullable LogMsgInterface logMsgInterface;
	private final boolean fixTimeline;

	private boolean haveCodecParamsVideo = false;
	private final @NonNull FfmpegCdcParamsVideo codecParamsVid = new FfmpegCdcParamsVideo();
	private boolean haveCodecParamsAudio = false;
	private final @NonNull FfmpegCdcParamsAudio codecParamsAud = new FfmpegCdcParamsAudio();

	private @Nullable AVFormatContext outFmtCtx = null;
	private @Nullable AVStream outVideoStream = null;
	private @Nullable AVStream outAudioStream = null;
	private boolean isOpened = false;
	private boolean haveWrittenFileHeader = false;

	private boolean haveCheckedForH26xAnnexB = false;
	private boolean needToConvertH26xToLp = false;

	private final @NonNull FFmpegMuxAdjustTimeline adjustTimeline = new FFmpegMuxAdjustTimeline();
	private final @NonNull AvSkewVars avSkewVars = new AvSkewVars();

	/**
	 * Constructor.
	 * @param logMsgInterface 'Log message' instance
	 * @param fixTimeline If the input packets for audio/video do not originate from the Demuxer, set to true
	 */
	private FfmpegMuxer(@Nullable LogMsgInterface logMsgInterface, boolean fixTimeline) {
		this.logMsgInterface = logMsgInterface;
		this.fixTimeline = fixTimeline;
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	public static @NonNull FfmpegMuxer createMuxer(
				@Nullable LogMsgInterface logMsgInterface,
				boolean fixTimeline
			) {
		return new FfmpegMuxer(logMsgInterface, fixTimeline);
	}

	// -----------------------------------------------------------------------------------------------------------------

	public void setCodecParamsVideo(@NonNull FfmpegCdcParamsVideo params) {
		haveCodecParamsVideo = true;
		codecParamsVid.copyFrom(params);

		if (fixTimeline) {
			adjustTimeline.setVideoFallbackDurationFromFps(codecParamsVid.fps);
		}
	}

	public void setCodecParamsAudio(@NonNull FfmpegCdcParamsAudio params) {
		haveCodecParamsAudio = true;
		codecParamsAud.copyFrom(params);
	}

	@SuppressWarnings("SameParameterValue")
	public static @NonNull String getOutputFileExtension(
				@NonNull FfmpegCodec destFfmpegCodecVideo,
				@NonNull FfmpegCodec destFfmpegCodecAudio,
				boolean preferMkvOverMp4
			) {
		final String FNC_NAME = FfmpegMuxer.class.getSimpleName() + ".getOutputFileExtension()";

		if (destFfmpegCodecVideo == FfmpegCodec.UNKNOWN && destFfmpegCodecAudio == FfmpegCodec.UNKNOWN) {
			throw new IllegalArgumentException(FNC_NAME + ": Need video and/or audio codec");
		}
		if (destFfmpegCodecAudio == FfmpegCodec.A_PCM_F32BE || destFfmpegCodecAudio == FfmpegCodec.A_PCM_S16BE ||
				destFfmpegCodecAudio == FfmpegCodec.A_PCM_S24BE || destFfmpegCodecAudio == FfmpegCodec.A_PCM_S32BE) {
			throw new IllegalArgumentException(FNC_NAME + ": " +
					"Audio codecs PCM_F32BE/PCM_S16BE/PCM_S24BE/PCM_S32BE are not supported");
		}
		/*
		 * MOV:
		 *   H264/H265/MPEG2/MPEG4 + (AAC/AC3/EAC3/MP3/ALAC_16_24)/PCM_16_24
		 */
		if ((destFfmpegCodecVideo == FfmpegCodec.V_H264 || destFfmpegCodecVideo == FfmpegCodec.V_H265 ||
					destFfmpegCodecVideo == FfmpegCodec.V_MPEG2 || destFfmpegCodecVideo == FfmpegCodec.V_MPEG4) &&
				(destFfmpegCodecAudio == FfmpegCodec.A_PCM_S16LE || destFfmpegCodecAudio == FfmpegCodec.A_PCM_S24LE)) {
			return "mov";
		}
		if (destFfmpegCodecVideo != FfmpegCodec.UNKNOWN && destFfmpegCodecAudio.isPcmAudio()) {
			throw new IllegalArgumentException(FNC_NAME + ": " +
					"PCM audio supported is limited to H264/H265/MPEG2/MPEG4 + PCM_S16LE/PCM_S24LE");
		}
		/*
		 * WEBM:
		 *   (AV1)/VP8/VP9 + OPUS/VORBIS
		 */
		if ((destFfmpegCodecVideo == FfmpegCodec.V_VP8 || destFfmpegCodecVideo == FfmpegCodec.V_VP9) &&
				(destFfmpegCodecAudio == FfmpegCodec.UNKNOWN ||
					destFfmpegCodecAudio == FfmpegCodec.A_OPUS || destFfmpegCodecAudio == FfmpegCodec.A_VORBIS)) {
			return "webm";
		}
		/*
		 * MP4:
		 *   AV1/H264/H265/MPEG2/MPEG4/(VP9) + AAC/AC3/ALAC_16_24/EAC3/FLAC_16_24/MP3/(OPUS/PCM_16_24)
		 */
		if ((destFfmpegCodecVideo == FfmpegCodec.V_AV1 ||
					destFfmpegCodecVideo == FfmpegCodec.V_H264 || destFfmpegCodecVideo == FfmpegCodec.V_H265 ||
					destFfmpegCodecVideo == FfmpegCodec.V_MPEG2 || destFfmpegCodecVideo == FfmpegCodec.V_MPEG4) &&
				(destFfmpegCodecAudio == FfmpegCodec.UNKNOWN ||
					destFfmpegCodecAudio == FfmpegCodec.A_AAC || destFfmpegCodecAudio == FfmpegCodec.A_AC3 ||
					destFfmpegCodecAudio == FfmpegCodec.A_ALAC || destFfmpegCodecAudio == FfmpegCodec.A_EAC3 ||
					destFfmpegCodecAudio == FfmpegCodec.A_FLAC || destFfmpegCodecAudio == FfmpegCodec.A_MP3)) {
			return (preferMkvOverMp4 && destFfmpegCodecAudio != FfmpegCodec.A_FLAC ? "mkv" : "mp4");
		}
		if (destFfmpegCodecVideo != FfmpegCodec.UNKNOWN) {
			return "mkv";
		}
		return switch (destFfmpegCodecAudio) {
				case FfmpegCodec.A_AAC -> "aac";
				case FfmpegCodec.A_AC3 -> "ac3";
				case FfmpegCodec.A_ALAC -> "m4a";
				case FfmpegCodec.A_EAC3 -> "eac3";
				case FfmpegCodec.A_FLAC -> "flac";
				case FfmpegCodec.A_MP2 -> "mp2";
				case FfmpegCodec.A_MP3 -> "mp3";
				case FfmpegCodec.A_OPUS -> "opus";  // even though Opus files are actually Ogg files
				case FfmpegCodec.A_VORBIS -> "ogg";
				default -> "wav";
			};
	}

	public Optional<Double> getLastAvSkew() {
		if (Double.isNaN(avSkewVars.lastAvSkewPts)) {
			return Optional.empty();
		}
		return Optional.of(avSkewVars.lastAvSkewPts);
	}

	// -----------------------------------------------------------------------------------------------------------------

	public void openOutput(@NonNull String outputPath) throws FfmpegGenericException {
		final String FNC_NAME = getClass().getSimpleName() + ".openOutput()";

		if (isOpened || outFmtCtx != null) {
			throw new IllegalStateException(FNC_NAME + ": Muxer already opened");
		}

		checkCodecParams(FNC_NAME);

		//
		logDebug(FNC_NAME, "Opening output file: '" + outputPath + "'");

		try {
			Path tmpOutpFilePath = Path.of(outputPath);
			if (tmpOutpFilePath.toFile().isFile() && tmpOutpFilePath.toFile().exists()) {
				Files.deleteIfExists(tmpOutpFilePath);
			}
		} catch (IOException e) {
			throw new FfmpegGenericException(FNC_NAME + ": could not delete output file");
		}

		//
		outFmtCtx = avformat.avformat_alloc_context();
		if (outFmtCtx == null) {
			throw new RuntimeException(FNC_NAME + ": Cannot allocate Output Format Context");
		}

		//noinspection RedundantCast
		AVOutputFormat guessedFmt = avformat.av_guess_format((String)null, outputPath, null);
		if (guessedFmt == null) {
			throw new IllegalArgumentException(FNC_NAME + ": Cannot guess output format from path: '" + outputPath + "'");
		}
		outFmtCtx.oformat(guessedFmt);

		//
		if (codecParamsVid.ffmpegCodec != FfmpegCodec.UNKNOWN) {
			internalCreateFfStreamVideo(FNC_NAME);
		}
		if (codecParamsAud.ffmpegCodec != FfmpegCodec.UNKNOWN) {
			internalCreateFfStreamAudio(FNC_NAME);
		}

		//
		if ((outFmtCtx.oformat().flags() & avformat.AVFMT_NOFILE) == 0) {
			AVIOContext pb = new AVIOContext();
			int r = avformat.avio_open(pb, outputPath, avformat.AVIO_FLAG_WRITE);
			FfmpegHelperFfError.checkFfmpegResult(FNC_NAME, "avio_open()", r);
			outFmtCtx.pb(pb);
		}

		isOpened = true;
		haveWrittenFileHeader = false;
	}

	@Override
	public void close() {
		if (outFmtCtx == null) {
			return;
		}

		if (isOpened && haveWrittenFileHeader) {
			avformat.av_write_trailer(outFmtCtx);
		}

		if ((outFmtCtx.oformat().flags() & avformat.AVFMT_NOFILE) == 0 && outFmtCtx.pb() != null) {
			avformat.avio_closep(outFmtCtx.pb());
		}

		avformat.avformat_free_context(outFmtCtx);
		outFmtCtx = null;
		outVideoStream = null;
		outAudioStream = null;
	}

	// -----------------------------------------------------------------------------------------------------------------

	public void writePacket(@NonNull FfmpegAvPktBasics pkt) throws FfmpegGenericException {
		if (pkt.isVideo && outVideoStream == null || ! pkt.isVideo && outAudioStream == null) {
			throw new IllegalStateException(getClass().getSimpleName() + ".writePacket(): Output Stream is null");
		}
		internalWritePacket(pkt, pkt.isVideo ? outVideoStream : outAudioStream);
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private void checkCodecParams(@NonNull String fncName) {
		if (! (haveCodecParamsVideo || haveCodecParamsAudio)) {
			throw new IllegalArgumentException(fncName + ": Need either Video or Audio Codec Parameters");
		}

		if (codecParamsVid.ffmpegCodec == FfmpegCodec.UNKNOWN && codecParamsAud.ffmpegCodec == FfmpegCodec.UNKNOWN) {
			throw new IllegalArgumentException(fncName + ": Need either Video or Audio FFmpeg Codec");
		}
		if (codecParamsVid.ffmpegCodec != FfmpegCodec.UNKNOWN && codecParamsVid.imgDims.isEmpty()) {
			throw new IllegalArgumentException(fncName + ": Video Image Dimensions must be set");
		}

		if (codecParamsAud.ffmpegCodec != FfmpegCodec.UNKNOWN && codecParamsAud.sampleRate == SampleRateEnum.UNKNOWN) {
			throw new IllegalArgumentException(fncName + ": Audio Sample Rate must be set");
		}
		if (codecParamsAud.ffmpegCodec != FfmpegCodec.UNKNOWN && codecParamsAud.channelCount < 1) {
			throw new IllegalArgumentException(fncName + ": Audio Channels must be set");
		}
	}

	private void internalCreateFfStreamVideo(@NonNull String fncName) throws FfmpegGenericException {
		if (outVideoStream != null) {
			throw new IllegalStateException(fncName + ": Video stream already created");
		}
		outVideoStream = avformat.avformat_new_stream(outFmtCtx, null);
		if (outVideoStream == null) {
			throw new RuntimeException(fncName + ": Cannot create output video stream");
		}
		//outVideoStream.id(0);
		outVideoStream.codecpar().codec_type(avutil.AVMEDIA_TYPE_VIDEO);
		outVideoStream.codecpar().codec_id(codecParamsVid.ffmpegCodec.getFfmpegId());
		outVideoStream.codecpar().width(codecParamsVid.imgDims.imgWidth());
		outVideoStream.codecpar().height(codecParamsVid.imgDims.imgHeight());

		try (AVRational vtb = new AVRational()) {
			vtb.num(1).den(90000);
			outVideoStream.time_base(vtb);
		}

		internalSetFfStreamCodecParamsExtradata(fncName, codecParamsVid, -1, outVideoStream);
	}

	private void internalCreateFfStreamAudio(@NonNull String fncName) throws FfmpegGenericException {
		if (outAudioStream != null) {
			throw new IllegalStateException(fncName + ": Audio stream already created");
		}
		outAudioStream = avformat.avformat_new_stream(outFmtCtx, null);
		if (outAudioStream == null) {
			throw new RuntimeException(fncName + ": Cannot create output audio stream");
		}
		//outAudioStream.id(1);
		outAudioStream.codecpar().codec_type(avutil.AVMEDIA_TYPE_AUDIO);
		outAudioStream.codecpar().codec_id(codecParamsAud.ffmpegCodec.getFfmpegId());
		outAudioStream.codecpar().sample_rate(codecParamsAud.sampleRate.getSrHz());

		try (AVChannelLayout tmpChLayout = createDefaultLayout(codecParamsAud.channelCount)) {
			tmpChLayout.nb_channels(codecParamsAud.channelCount);

			int r = avutil.av_channel_layout_copy(outAudioStream.codecpar().ch_layout(), tmpChLayout);
			FfmpegHelperFfError.checkFfmpegResult(fncName, "av_channel_layout_copy()", r);
		}

		try (AVRational atb = new AVRational()) {
			atb.num(1).den(codecParamsAud.sampleRate.getSrHz());  // sample timeline for audio stream
			outAudioStream.time_base(atb);
		}

		internalSetFfStreamCodecParamsExtradata(fncName, codecParamsAud, codecParamsAud.channelCount, outAudioStream);
	}

	private void internalSetFfStreamCodecParamsExtradata(
				@NonNull String fncName,
				@NonNull FfmpegCdcParamsBase codecParams,
				int audioChannelCount,
				@NonNull AVStream outStream
			) throws FfmpegGenericException {
		FfmpegHelperValidateExtradata.validateForCodec(
				fncName,
				codecParams.ffmpegCodec,
				audioChannelCount,
				codecParams.extradataHex
			);

		/*
		 * The 'extradata' is critical for many container codecs (e.g., AAC/AV1/H264/H265/Opus)
		 */
		ExtradataContainerHex tmpEdHexPtr = codecParams.extradataHex;
		/*if (tmpEdHexPtr.isCodecH264() || tmpEdHexPtr.isCodecH265()) {
			logDebug(getClass().getSimpleName() + ".internalSetFfStreamCodecParamsExtradata()",
					"**************************** extradataHex.h26x.annex=" + tmpEdHexPtr.isFmtH26xAnnexB());
		}*/
		if (codecParams.ffmpegCodec == FfmpegCodec.V_H264) {
			tmpEdHexPtr = ExtradataConvHexFmtHelper.convertH264EncoderExtradata(false, tmpEdHexPtr);
		} else if (codecParams.ffmpegCodec == FfmpegCodec.V_H265) {
			tmpEdHexPtr = ExtradataConvHexFmtHelper.convertH265EncoderExtradata(false, tmpEdHexPtr);
		}
		//
		byte[] tmpEdBa = HexFormat.of().parseHex(tmpEdHexPtr.getEd());
		int size = tmpEdBa.length;
		BytePointer extraBp = new BytePointer(avutil.av_mallocz(size + avcodec.AV_INPUT_BUFFER_PADDING_SIZE));
		extraBp.put(tmpEdBa, 0, size);
		outStream.codecpar().extradata(extraBp);
		outStream.codecpar().extradata_size(size);
	}

	// -----------------------------------------------------------------------------------------------------------------

	private void internalWritePacket(@NonNull FfmpegAvPktBasics outputPktBasics, @NonNull AVStream outStream)
			throws FfmpegGenericException {
		final String FNC_NAME = getClass().getSimpleName() + ".internalWritePacket(" + (outputPktBasics.isVideo ? "V" : "A") + ")";

		if (! isOpened) {
			throw new IllegalStateException(FNC_NAME + ": Muxer has not been opened");
		}
		if (outFmtCtx == null || outputPktBasics.pktBe.isEmpty()) {
			return;
		}

		//
		if (! haveWrittenFileHeader) {
			writePacket_writeHeader();
		}

		//
		BufferView outPktPayloadBv = new BufferView(outputPktBasics.pktBe);

		//
		writePacket_applyBsfPre(outputPktBasics, outPktPayloadBv);

		//
		AVPacket outPkt = avcodec.av_packet_alloc();
		if (outPkt == null) {
			throw new RuntimeException(FNC_NAME + ": Cannot allocate AVPacket");
		}
		try {
			writePacket_createOutputPkt(
					outputPktBasics,
					outPktPayloadBv,
					outStream,
					outPkt
				);
			//
			writePacket_applyBsfPost(outputPktBasics.isVideo, outPkt);
			//
			writePacket_toOutput(outputPktBasics.isVideo, outPkt);
		} finally {
			avcodec.av_packet_free(outPkt);
		}
	}

	private void writePacket_applyBsfPre(
				@NonNull FfmpegAvPktBasics outputPktBasics,
				@NonNull BufferView outPktPayloadBv
			) {
		if (outputPktBasics.isVideo || codecParamsAud.ffmpegCodec != FfmpegCodec.A_AAC) {
			return;
		}
		// this is extremely fast - no need for additional checks
		FfmpegHelperBsfAacNoAdts.removeAdts(outPktPayloadBv);
	}

	private void writePacket_writeHeader() throws FfmpegGenericException {
		final String FNC_NAME = getClass().getSimpleName() + ".writePacket_writeHeader()";

		if (haveWrittenFileHeader) {
			return;
		}
		/*
		 * This can change the time base of the output stream(s).
		 * If the time base for an output stream was initially set to 1/1000,
		 * it can, for instance, be changed to 1/16000.
		 */
		int r = avformat.avformat_write_header(outFmtCtx, (org.bytedeco.ffmpeg.avutil.AVDictionary)null);
		FfmpegHelperFfError.checkFfmpegResult(FNC_NAME, "avformat_write_header()", r);
		haveWrittenFileHeader = true;
	}

	private void writePacket_createOutputPkt(
				@NonNull FfmpegAvPktBasics inputPktBas,
				@NonNull BufferView inputPktPayloadBv,
				@NonNull AVStream outStream,
				@NonNull AVPacket outPkt
			) throws FfmpegGenericException {
		final String FNC_NAME = getClass().getSimpleName() + ".writePacket_createOutputPkt(" + (inputPktBas.isVideo ? "V" : "A") + ")";

		// since [outPkt] is allocated for each call, we don't need to unref it here

		int r = avcodec.av_new_packet(outPkt, inputPktPayloadBv.getLength());
		FfmpegHelperFfError.checkFfmpegResult(FNC_NAME, "av_new_packet()", r);

		FfmpegHelperPktConverter.convertBasicsToFf(
				inputPktBas,
				inputPktPayloadBv,
				outStream.index(),
				outPkt
			);

		// rescale PTS/DTS
		try (AVRational srcTb = new AVRational()) {
			srcTb.num(inputPktBas.timeBase.getNumerator());
			srcTb.den(inputPktBas.timeBase.getDenominator());

			avcodec.av_packet_rescale_ts(outPkt, srcTb, outStream.time_base());
		}

		// custom timeline adjustments
		if (fixTimeline) {
			adjustTimeline.adjustTimelineAv(inputPktBas.isVideo, outStream, outPkt);
		}
		//adjustTimeline.printDebugInfo(inputPktBas.isVideo, outStream, outPkt);
		updateAvSkew(inputPktBas.isVideo, outStream, outPkt);
	}

	private void writePacket_applyBsfPost(boolean isVideo, @NonNull AVPacket outPkt) throws FfmpegGenericException {
		final String FNC_NAME = getClass().getSimpleName() + ".writePacket_applyBsfPost(" + (isVideo ? "V" : "A") + ")";

		if (! isVideo ||
				! (codecParamsVid.ffmpegCodec == FfmpegCodec.V_H264 || codecParamsVid.ffmpegCodec == FfmpegCodec.V_H265)) {
			return;
		}
		if (! haveCheckedForH26xAnnexB) {
			needToConvertH26xToLp = FfmpegHelperBsfH26xToLengthPrefixed.isAnnexB(outPkt);
			/*logDebug(FNC_NAME,
					"**************************** needToConvertH26xToLp=" + needToConvertH26xToLp);*/
			haveCheckedForH26xAnnexB = true;
		}
		if (! needToConvertH26xToLp) {
			return;
		}
		try (FfmpegHelperBsfH26xToLengthPrefixed tmpBsf = new FfmpegHelperBsfH26xToLengthPrefixed()) {
			tmpBsf.setInputPacket(outPkt);
			if (! tmpBsf.receiveOneConvertedPacket(outPkt)) {
				throw new RuntimeException(FNC_NAME + ": Converting packet failed");
			}
		}
	}

	private void writePacket_toOutput(boolean isVideo, @NonNull AVPacket outPkt) throws FfmpegGenericException {
		final String FNC_NAME = getClass().getSimpleName() + ".writePacket_toOutput(" + (isVideo ? "V" : "A") + ")";

		long backupPts = outPkt.pts();
		long backupDts = outPkt.dts();

		int r = avformat.av_interleaved_write_frame(outFmtCtx, outPkt);
		if (r == avutil.AVERROR_EINVAL()) {
			throw new FfmpegGenericException(FNC_NAME + ": av_interleaved_write_frame() rejected invalid data " +
					String.format("(PTS=%d, DTS=%d)", backupPts, backupDts));
		} else {
			FfmpegHelperFfError.checkFfmpegResult(FNC_NAME, "av_interleaved_write_frame()", r);
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	private static @NonNull AVChannelLayout createDefaultLayout(int channelCount) {
		AVChannelLayout layout = new AVChannelLayout();
		avutil.av_channel_layout_default(layout, channelCount);
		return layout;
	}

	// -----------------------------------------------------------------------------------------------------------------

	private void updateAvSkew(boolean isVideo, @NonNull AVStream outStream, @NonNull AVPacket outPkt) {
		final long backupPts = outPkt.pts();
		final long backupDts = outPkt.dts();

		double tb = (double)outStream.time_base().num() / (double)outStream.time_base().den();
		double ptsSec = (backupPts == avutil.AV_NOPTS_VALUE ? Double.NaN : backupPts * tb);
		double dtsSec = (backupDts == avutil.AV_NOPTS_VALUE ? Double.NaN : backupDts * tb);

		if (isVideo) {
			avSkewVars.lastPtsSecVideo = ptsSec;
			avSkewVars.lastDtsSecVideo = dtsSec;
		} else {
			avSkewVars.lastPtsSecAudio = ptsSec;
			avSkewVars.lastDtsSecAudio = dtsSec;
		}

		if (! Double.isNaN(avSkewVars.lastPtsSecVideo) && ! Double.isNaN(avSkewVars.lastPtsSecAudio)) {
			avSkewVars.lastAvSkewPts = avSkewVars.lastPtsSecVideo - avSkewVars.lastPtsSecAudio;  // >0 => video ahead
		}
	}

	// -----------------------------------------------------------------------------------------------------------------

	private void logDebug(@NonNull String fncName, @NonNull String msg) {
		internalLog(RtxpLogLevel.DEBUG, fncName, msg);
	}

	@SuppressWarnings("SameParameterValue")
	private void internalLog(@NonNull RtxpLogLevel logLevel, @NonNull String fncName, @NonNull String msg) {
		if (logMsgInterface == null) {
			return;
		}
		logMsgInterface.addMsgForLogThread(logLevel, Thread.currentThread().getName(), fncName + ": " + msg);
	}

}
