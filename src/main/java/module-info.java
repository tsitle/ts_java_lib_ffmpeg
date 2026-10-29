module io.github.tsitle.lib_ffmpeg {
	requires static org.jspecify;
	requires org.bytedeco.ffmpeg;
	requires io.github.tsitle.lib_xrtxp;

	exports io.github.tsitle.lib_ffmpeg;
	exports io.github.tsitle.lib_ffmpeg.demux;
	exports io.github.tsitle.lib_ffmpeg.exceptions;
	exports io.github.tsitle.lib_ffmpeg.helpers;
	exports io.github.tsitle.lib_ffmpeg.mux;
	exports io.github.tsitle.lib_ffmpeg.tc;
}
