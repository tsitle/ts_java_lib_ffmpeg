module org.tsitle.lib_ffmpeg {
	requires static org.jspecify;
	requires org.bytedeco.ffmpeg;
	requires org.tsitle.lib_xrtxp;

	exports org.tsitle.lib_ffmpeg;
	exports org.tsitle.lib_ffmpeg.demux;
	exports org.tsitle.lib_ffmpeg.exceptions;
	exports org.tsitle.lib_ffmpeg.helpers;
	exports org.tsitle.lib_ffmpeg.mux;
	exports org.tsitle.lib_ffmpeg.tc;
}
