package io.github.tsitle.lib_ffmpeg.mux;

import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.avutil.AVRational;
import org.bytedeco.ffmpeg.global.avutil;
import org.jspecify.annotations.NonNull;
import io.github.tsitle.lib_xrtxp.common.types.RationalNumber;

final class FFmpegMuxAdjustTimeline {

	private static final long MAX_DURATION = 200_000L;

	/**
	 * Per-stream start offset in µs. Set on the very first packet of each stream
	 * and then subtracted from every subsequent packet so that both streams start
	 * at 0 in the output regardless of whatever offset the source container carries.
	 * Long.MIN_VALUE means "not yet seen".
	 */
	private long videoStartOffsetUs = Long.MIN_VALUE;
	private long audioStartOffsetUs = Long.MIN_VALUE;

	private long lastVideoPtsUs = Long.MIN_VALUE;
	private long lastVideoDtsUs = Long.MIN_VALUE;
	private long lastAudioPtsUs = Long.MIN_VALUE;
	private long lastAudioDtsUs = Long.MIN_VALUE;

	private long lastVideoPtsTs = Long.MIN_VALUE;
	private long lastVideoDtsTs = Long.MIN_VALUE;
	private long lastAudioPtsTs = Long.MIN_VALUE;
	private long lastAudioDtsTs = Long.MIN_VALUE;

	private long fallbackVideoDurUs = 33_333L; // ~30fps until measured
	private long fallbackAudioDurUs = 21_333L; // ~1024 samples @ 48k until measured
	private long lockedVideoFrameDurUs = -1L;  // when set (from fps), enforce CFR pacing

	private long dbgOutputPktsVid = 0L;
	private long dbgOutputPktsAud = 0L;

	private double dbgLastPtsSecVideo = Double.NaN;
	private double dbgLastDtsSecVideo = Double.NaN;
	private double dbgLastPtsSecAudio = Double.NaN;
	private double dbgLastDtsSecAudio = Double.NaN;

	FFmpegMuxAdjustTimeline() { }

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	void setVideoFallbackDurationFromFps(@NonNull RationalNumber fps) {
		if (! fps.isValid()) {
			return;
		}
		int num = fps.getNumerator();
		int den = fps.getDenominator();
		if (num < 1 || den < 1) {
			return;
		}

		long durUs = Math.round(1_000_000.0 * (double) den / (double) num);
		if (durUs > 0L) {
			lockedVideoFrameDurUs = clampDurationUs(durUs, 40_000L, 10_000L);
			fallbackVideoDurUs = lockedVideoFrameDurUs;
		}
	}

	void adjustTimelineAv(boolean isVideo, @NonNull AVStream outStream, @NonNull AVPacket outPkt) {
		long ptsUs = streamTsToUs(outPkt.pts(), outStream);
		long dtsUs = streamTsToUs(outPkt.dts(), outStream);

		long lastPtsUs = (isVideo ? lastVideoPtsUs : lastAudioPtsUs);
		long lastDtsUs = (isVideo ? lastVideoDtsUs : lastAudioDtsUs);
		long fallbackDurUs = (isVideo ? fallbackVideoDurUs : fallbackAudioDurUs);

		if (ptsUs == avutil.AV_NOPTS_VALUE && dtsUs == avutil.AV_NOPTS_VALUE) {
			long seed = (lastDtsUs != Long.MIN_VALUE ? (lastDtsUs + fallbackDurUs) : 0L);
			ptsUs = seed;
			dtsUs = seed;
		} else {
			if (ptsUs == avutil.AV_NOPTS_VALUE) {
				ptsUs = (lastPtsUs != Long.MIN_VALUE ? (lastPtsUs + fallbackDurUs) : dtsUs);
			}
			if (dtsUs == avutil.AV_NOPTS_VALUE) {
				dtsUs = (lastDtsUs != Long.MIN_VALUE ? (lastDtsUs + fallbackDurUs) : ptsUs);
			}
		}

		if (ptsUs < dtsUs) {
			ptsUs = dtsUs;
		}

		/*
		 * Normalize each stream's start offset to 0.
		 * This corrects sources (e.g. Theora/Vorbis) where one stream starts at a
		 * non-zero PTS, which would otherwise cause av_interleaved_write_frame() to
		 * hold back packets from the other stream waiting for alignment, producing a
		 * gap at the beginning of the output.
		 */
		if (isVideo) {
			if (videoStartOffsetUs == Long.MIN_VALUE) {
				//noinspection DataFlowIssue
				videoStartOffsetUs = Math.min(ptsUs, dtsUs);
			}
			if (videoStartOffsetUs > 0L) {
				ptsUs -= videoStartOffsetUs;
				dtsUs -= videoStartOffsetUs;
				if (ptsUs < 0L) { ptsUs = 0L; }
				if (dtsUs < 0L) { dtsUs = 0L; }
			}
		} else {
			if (audioStartOffsetUs == Long.MIN_VALUE) {
				//noinspection DataFlowIssue
				audioStartOffsetUs = Math.min(ptsUs, dtsUs);
			}
			if (audioStartOffsetUs > 0L) {
				ptsUs -= audioStartOffsetUs;
				dtsUs -= audioStartOffsetUs;
				if (ptsUs < 0L) { ptsUs = 0L; }
				if (dtsUs < 0L) { dtsUs = 0L; }
			}
		}

		// hard-lock video cadence if fps is known (prevents 1ms frame bursts)
		if (isVideo && lockedVideoFrameDurUs > 0L && lastDtsUs != Long.MIN_VALUE) {
			dtsUs = lastDtsUs + lockedVideoFrameDurUs;
			ptsUs = dtsUs; // for no-B-frame pipeline, keep pts==dts and stable pacing
		}

		// keep monotonic in us
		if (lastDtsUs != Long.MIN_VALUE && dtsUs <= lastDtsUs) {
			dtsUs = lastDtsUs + 1L;
		}
		if (ptsUs < dtsUs) {
			ptsUs = dtsUs;
		}
		if (lastPtsUs != Long.MIN_VALUE && ptsUs <= lastPtsUs) {
			ptsUs = Math.max(dtsUs, lastPtsUs + 1L);
		}

		long deltaPts = (lastPtsUs != Long.MIN_VALUE ? (ptsUs - lastPtsUs) : -1L);
		if (isVideo) {
			if (lockedVideoFrameDurUs <= 0L) {
				fallbackVideoDurUs = clampDurationUs(deltaPts, fallbackVideoDurUs, 10_000L);
			}
			lastVideoPtsUs = ptsUs;
			lastVideoDtsUs = dtsUs;
		} else {
			fallbackAudioDurUs = clampDurationUs(deltaPts, fallbackAudioDurUs, 1_000L);
			lastAudioPtsUs = ptsUs;
			lastAudioDtsUs = dtsUs;
		}

		long ptsTs = usToStreamTs(ptsUs, outStream);
		long dtsTs = usToStreamTs(dtsUs, outStream);

		long prevPtsTs = (isVideo ? lastVideoPtsTs : lastAudioPtsTs);
		long prevDtsTs = (isVideo ? lastVideoDtsTs : lastAudioDtsTs);

		if (prevDtsTs != Long.MIN_VALUE && dtsTs <= prevDtsTs) {
			dtsTs = prevDtsTs + 1L;
		}
		if (ptsTs < dtsTs) {
			ptsTs = dtsTs;
		}
		if (prevPtsTs != Long.MIN_VALUE && ptsTs <= prevPtsTs) {
			ptsTs = Math.max(dtsTs, prevPtsTs + 1L);
		}

		if (isVideo) {
			lastVideoPtsTs = ptsTs;
			lastVideoDtsTs = dtsTs;
		} else {
			lastAudioPtsTs = ptsTs;
			lastAudioDtsTs = dtsTs;
		}

		outPkt.pts(ptsTs);
		outPkt.dts(dtsTs);
	}

	@SuppressWarnings("unused")
	void printDebugInfo(boolean isVideo, @NonNull AVStream outStream, @NonNull AVPacket outPkt) {
		if ((isVideo && dbgOutputPktsVid > 10L) || (! isVideo && dbgOutputPktsAud > 10L)) {
			return;
		}
		final long backupPts = outPkt.pts();
		final long backupDts = outPkt.dts();

		double tb = (double) outStream.time_base().num() / (double) outStream.time_base().den();
		double ptsSec = (backupPts == avutil.AV_NOPTS_VALUE ? Double.NaN : backupPts * tb);
		double dtsSec = (backupDts == avutil.AV_NOPTS_VALUE ? Double.NaN : backupDts * tb);

		double prevPtsSec = (isVideo ? dbgLastPtsSecVideo : dbgLastPtsSecAudio);
		double prevDtsSec = (isVideo ? dbgLastDtsSecVideo : dbgLastDtsSecAudio);
		double dPtsSec = (Double.isNaN(prevPtsSec) || Double.isNaN(ptsSec) ? Double.NaN : ptsSec - prevPtsSec);
		double dDtsSec = (Double.isNaN(prevDtsSec) || Double.isNaN(dtsSec) ? Double.NaN : dtsSec - prevDtsSec);

		if (isVideo) {
			dbgLastPtsSecVideo = ptsSec;
			dbgLastDtsSecVideo = dtsSec;
		} else {
			dbgLastPtsSecAudio = ptsSec;
			dbgLastDtsSecAudio = dtsSec;
		}

		double lastAvSkewPts = Double.NaN;
		if (! Double.isNaN(dbgLastPtsSecVideo) && ! Double.isNaN(dbgLastPtsSecAudio)) {
			lastAvSkewPts = dbgLastPtsSecVideo - dbgLastPtsSecAudio; // >0 => video ahead
		}

		System.out.printf(
				"MUXDBG %s pts=%d(%.6fs) dts=%d(%.6fs) dPts=%.6fs dDts=%.6fs avSkewPts=%.6fs  (TB=%d/%d)%n",
				(isVideo ? "V" : "A"),
				backupPts, ptsSec,
				backupDts, dtsSec,
				dPtsSec, dDtsSec,
				lastAvSkewPts,
				outStream.time_base().num(), outStream.time_base().den()
			);
		if (isVideo) {
			++dbgOutputPktsVid;
		} else {
			++dbgOutputPktsAud;
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private static long clampDurationUs(long valueUs, long fallbackUs, long minUs) {
		if (valueUs >= minUs && valueUs <= MAX_DURATION) {
			return valueUs;
		}
		return fallbackUs;
	}

	private static long streamTsToUs(long ts, @NonNull AVStream st) {
		if (ts == avutil.AV_NOPTS_VALUE) {
			return avutil.AV_NOPTS_VALUE;
		}
		try (AVRational tbUs = new AVRational()) {
			tbUs.num(1).den(1_000_000);
			return avutil.av_rescale_q(ts, st.time_base(), tbUs);
		}
	}

	private static long usToStreamTs(long tsUs, @NonNull AVStream st) {
		try (AVRational tbUs = new AVRational()) {
			tbUs.num(1).den(1_000_000);
			return avutil.av_rescale_q(tsUs, tbUs, st.time_base());
		}
	}

}
