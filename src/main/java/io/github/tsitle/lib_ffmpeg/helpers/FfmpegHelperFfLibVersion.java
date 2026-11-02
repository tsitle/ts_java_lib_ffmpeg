package io.github.tsitle.lib_ffmpeg.helpers;

import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.BytePointer;
import org.jspecify.annotations.NonNull;

import java.nio.charset.StandardCharsets;

/**
 * Helper class for retrieving the version number from the FFmpeg shared libraries.
 */
public final class FfmpegHelperFfLibVersion {

	public record FfmpegSharedLibInfo(@NonNull String version, @NonNull String buildConfigInfo) { }

	private FfmpegHelperFfLibVersion() { }

	public static @NonNull FfmpegSharedLibInfo getFfmpegSharedLibInfo() throws UnsatisfiedLinkError, ExceptionInInitializerError {
		String avutilVersion;
		try (BytePointer bp = avutil.av_version_info()) {
			avutilVersion = (bp != null ? bp.getString(StandardCharsets.UTF_8) : "-unknown-");
		}
		if (avutilVersion.isBlank()) {
			avutilVersion = "-empty-";
		}

		String avutilBuildConfigInfo;
		try (BytePointer bp = avutil.avutil_configuration()) {
			avutilBuildConfigInfo = (bp != null ? bp.getString(StandardCharsets.UTF_8) : "-unknown-");
		}
		if (avutilBuildConfigInfo.isBlank()) {
			avutilBuildConfigInfo = "-empty-";
		}

		return new FfmpegSharedLibInfo(avutilVersion, avutilBuildConfigInfo);
	}

}
