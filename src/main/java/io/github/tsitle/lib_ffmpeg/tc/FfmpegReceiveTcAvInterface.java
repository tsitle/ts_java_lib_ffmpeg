package io.github.tsitle.lib_ffmpeg.tc;

import org.jspecify.annotations.NonNull;
import io.github.tsitle.lib_ffmpeg.FfmpegAvPktBasics;
import io.github.tsitle.lib_ffmpeg.FfmpegCdcParamsAudio;
import io.github.tsitle.lib_ffmpeg.FfmpegCdcParamsVideo;
import io.github.tsitle.lib_ffmpeg.exceptions.FfmpegGenericException;

interface FfmpegReceiveTcAvInterface {

	void cbReceiveCdcParamsVideo(@NonNull FfmpegCdcParamsVideo params);
	void cbReceiveCdcParamsAudio(@NonNull FfmpegCdcParamsAudio params);

	void cbReceiveTranscodedVideoFrame(@NonNull FfmpegAvPktBasics videoFrame) throws FfmpegGenericException;
	void cbReceiveTranscodedAudioSamples(@NonNull FfmpegAvPktBasics audioSamples) throws FfmpegGenericException;

}
