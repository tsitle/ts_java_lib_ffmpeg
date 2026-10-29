package io.github.tsitle.lib_ffmpeg;

import org.jspecify.annotations.NonNull;
import io.github.tsitle.lib_xrtxp.common.types.ImageDimensions;
import io.github.tsitle.lib_xrtxp.common.types.RationalNumber;

/**
 * Video Codec Parameters.
 */
public final class FfmpegCdcParamsVideo extends FfmpegCdcParamsBase {

	public @NonNull ImageDimensions imgDims;
	public @NonNull RationalNumber fps;

	public FfmpegCdcParamsVideo() {
		super();

		clear();
	}

	public void clear() {
		baseClear();

		imgDims = ImageDimensions.ofEmpty();
		fps = RationalNumber.ofEmpty();
	}

	public void copyFrom(@NonNull FfmpegCdcParamsVideo other) {
		if (this == other) {
			return;
		}
		baseCopyFrom(other);

		imgDims = ImageDimensions.of(other.imgDims);
		fps = RationalNumber.of(other.fps);
	}

}
