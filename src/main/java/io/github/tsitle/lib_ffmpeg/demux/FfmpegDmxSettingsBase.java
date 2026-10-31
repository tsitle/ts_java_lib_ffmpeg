package io.github.tsitle.lib_ffmpeg.demux;

import org.jspecify.annotations.NonNull;
import io.github.tsitle.lib_ffmpeg.FfmpegCodec;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for demuxing settings.
 */
public class FfmpegDmxSettingsBase {

	/** Select a specific video stream by number (a value less or equal to 0 means auto-selection) */
	public int cfgSelectStreamNumberVideo = -1;
	/** Select a specific audio stream by number (a value less or equal to 0 means auto-selection) */
	public int cfgSelectStreamNumberAudio = -1;

	/** Allow only specific codecs for video (true) or all? */
	public boolean cfgAllowOnlySpecificCodecsVideo = false;
	/** Allow only specific codecs for audio (true) or all? */
	public boolean cfgAllowOnlySpecificCodecsAudio = false;
	/** Allowed video codecs (only used if [cfgAllowOnlySpecificCodecsVideo] is true) */
	public @NonNull Set<@NonNull FfmpegCodec> cfgAllowedCodecsVideo = new HashSet<>();
	/** Allowed audio codecs (only used if [cfgAllowOnlySpecificCodecsAudio] is true) */
	public @NonNull Set<@NonNull FfmpegCodec> cfgAllowedCodecsAudio = new HashSet<>();

	protected FfmpegDmxSettingsBase() { }

	protected void copyFrom(@NonNull FfmpegDmxSettingsBase other) {
		cfgSelectStreamNumberVideo = other.cfgSelectStreamNumberVideo;
		cfgSelectStreamNumberAudio = other.cfgSelectStreamNumberAudio;

		cfgAllowOnlySpecificCodecsVideo = other.cfgAllowOnlySpecificCodecsVideo;
		cfgAllowOnlySpecificCodecsAudio = other.cfgAllowOnlySpecificCodecsAudio;
		if (cfgAllowOnlySpecificCodecsVideo) {
			cfgAllowedCodecsVideo = new HashSet<>(other.cfgAllowedCodecsVideo);
		}
		if (cfgAllowOnlySpecificCodecsAudio) {
			cfgAllowedCodecsAudio = new HashSet<>(other.cfgAllowedCodecsAudio);
		}
	}

}
