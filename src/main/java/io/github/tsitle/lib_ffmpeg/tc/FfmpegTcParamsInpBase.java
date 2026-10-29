package io.github.tsitle.lib_ffmpeg.tc;

import org.jspecify.annotations.NonNull;
import io.github.tsitle.lib_ffmpeg.FfmpegCodec;
import io.github.tsitle.lib_xrtxp.avdata.extradata.ExtradataContainerHex;
import io.github.tsitle.lib_xrtxp.common.types.RationalNumber;

/**
 * Base class for Transcoder Input Parameters.
 */
public class FfmpegTcParamsInpBase {

	public @NonNull FfmpegCodec ffmpegCodec = FfmpegCodec.UNKNOWN;
	public @NonNull RationalNumber timeBase = RationalNumber.ofEmpty();
	public final @NonNull ExtradataContainerHex extradataHex = ExtradataContainerHex.ofEmpty();

	protected FfmpegTcParamsInpBase() { }

	protected void baseCopyFrom(@NonNull FfmpegTcParamsInpBase other) {
		ffmpegCodec = other.ffmpegCodec;
		timeBase.copyFrom(other.timeBase);
		extradataHex.copyFrom(other.extradataHex);
	}

}
