package org.example.dmx_tc_mux;

import io.github.tsitle.lib_ffmpeg.FfmpegAvPktBasics;
import io.github.tsitle.lib_ffmpeg.FfmpegCdcParamsAudio;
import io.github.tsitle.lib_ffmpeg.FfmpegCdcParamsVideo;
import io.github.tsitle.lib_ffmpeg.FfmpegCodec;
import io.github.tsitle.lib_ffmpeg.exceptions.FfmpegGenericException;
import io.github.tsitle.lib_ffmpeg.mux.FfmpegMuxer;
import io.github.tsitle.lib_ffmpeg.tc.FfmpegTcAvPacketBacklog;
import io.github.tsitle.lib_ffmpeg.tc.FfmpegTcAvPacketList;
import io.github.tsitle.lib_ffmpeg.tc.FfmpegTranscoder;
import org.jspecify.annotations.NonNull;

final class MuxerHelperFncs {

	private MuxerHelperFncs() { }

	static boolean muxerOpen(
				@NonNull String cfgOutputFilePath,
				boolean cfgMuxerPreferMkvOverMp4,
				boolean cfgTcCopySubStreamVideo,
				boolean cfgTcCopySubStreamAudio,
				@NonNull FfmpegTranscoder ffmpegTc,
				@NonNull FfmpegMuxer ffmpegMuxer
			) throws FfmpegGenericException {
		FfmpegCdcParamsVideo tmpMuxParamsVideo = new FfmpegCdcParamsVideo();
		FfmpegCdcParamsAudio tmpMuxParamsAudio = new FfmpegCdcParamsAudio();
		try {
			ffmpegTc.getCdcParamsVideo(tmpMuxParamsVideo);
			ffmpegTc.getCdcParamsAudio(tmpMuxParamsAudio);
		} catch (IllegalStateException e) {
			//logDebug(FNC_NAME, "(" + e.getMessage() + ")");
			return false;
		}

		String tmpOpFext = FfmpegMuxer.getOutputFileExtension(
				tmpMuxParamsVideo.ffmpegCodec,
				tmpMuxParamsAudio.ffmpegCodec,
				cfgMuxerPreferMkvOverMp4
			);

		if (cfgTcCopySubStreamVideo || tmpMuxParamsVideo.ffmpegCodec != FfmpegCodec.UNKNOWN) {
			ffmpegMuxer.setCodecParamsVideo(tmpMuxParamsVideo);
		}
		if (cfgTcCopySubStreamAudio || tmpMuxParamsAudio.ffmpegCodec != FfmpegCodec.UNKNOWN) {
			ffmpegMuxer.setCodecParamsAudio(tmpMuxParamsAudio);
		}
		ffmpegMuxer.openOutput(cfgOutputFilePath + "." + tmpOpFext);
		return true;
	}

	static void muxerWriteAllTranscodedPackets(
				@NonNull FfmpegTcAvPacketBacklog tcAvPktBacklog,
				@NonNull FfmpegTcAvPacketList tcAvPktListPtr,
				@NonNull FfmpegMuxer ffmpegMuxer
			) throws FfmpegGenericException {
		if (! tcAvPktBacklog.isEmpty()) {
			for (FfmpegAvPktBasics tcAvPkt : tcAvPktBacklog) {
				ffmpegMuxer.writePacket(tcAvPkt);
			}
			tcAvPktBacklog.clear();
		}
		for (FfmpegAvPktBasics tcAvPkt : tcAvPktListPtr) {
			ffmpegMuxer.writePacket(tcAvPkt);
		}
	}

	static void muxerDrainTranscoder(
				@NonNull FfmpegTranscoder ffmpegTc,
				@NonNull FfmpegTcAvPacketBacklog tcAvPktBacklog,
				@NonNull FfmpegMuxer ffmpegMuxer
			) throws FfmpegGenericException {
		ffmpegTc.close();  // drain the encoder etc.
		//
		for (FfmpegAvPktBasics tcAvPkt : tcAvPktBacklog) {
			ffmpegMuxer.writePacket(tcAvPkt);
		}
		for (FfmpegAvPktBasics tcAvPkt : ffmpegTc.getRemainingPackets(true)) {
			ffmpegMuxer.writePacket(tcAvPkt);
		}
		for (FfmpegAvPktBasics tcAvPkt : ffmpegTc.getRemainingPackets(false)) {
			ffmpegMuxer.writePacket(tcAvPkt);
		}
	}

}
