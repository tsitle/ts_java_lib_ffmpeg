package io.github.tsitle.lib_ffmpeg.tc;

import io.github.tsitle.lib_ffmpeg.FfmpegCodec;
import org.bytedeco.ffmpeg.avcodec.AVCodec;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.PointerPointer;
import org.jspecify.annotations.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.*;

class FfmpegTcEnDeCoderListsBase {

	protected FfmpegTcEnDeCoderListsBase() { }

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	protected static @NonNull Set<@NonNull String> getUnfilteredEnDeCoders(boolean getEncoders) {
		// make sure the FFmpeg/JavaCPP libraries are loaded
		try (BytePointer bp = avutil.av_version_info()) {
			String avutilVersion = (bp != null ? bp.getString(StandardCharsets.UTF_8) : "-unknown-");
			if (avutilVersion.isBlank()) {
				return Collections.emptySet();
			}
		}

		//
		Set<String> resSet = new TreeSet<>();
		PointerPointer<Pointer> opaque = new PointerPointer<>(1);
		//noinspection RedundantCast
		opaque.put(0, (Pointer)null);
		AVCodec codec;

		while ((codec = avcodec.av_codec_iterate(opaque)) != null) {
			if ((getEncoders && avcodec.av_codec_is_encoder(codec) == 0) ||
					(! getEncoders && avcodec.av_codec_is_decoder(codec) == 0)) {
				continue;
			}
			final String codName = codec.name().getString(StandardCharsets.UTF_8).toLowerCase().strip();
			resSet.add(codName);
		}
		return resSet;
	}

	protected static @NonNull Map<@NonNull FfmpegCodec, @NonNull List<@NonNull String>> getAvailableEnDeCoders(
				@NonNull Map<@NonNull FfmpegCodec, @NonNull List<@NonNull String>> searchMapPtr,
				@NonNull Set<@NonNull String> unfilteredEnDeCoders,
				@NonNull Set<@NonNull String> cfgDisabledEnDeCodersList
			) {
		Map<FfmpegCodec, List<String>> resM = new TreeMap<>();

		for (Map.Entry<FfmpegCodec, List<String>> tmpEnDeCodersForCodec : searchMapPtr.entrySet()) {
			resM.put(tmpEnDeCodersForCodec.getKey(), new ArrayList<>());

			for (String edNameSearch : tmpEnDeCodersForCodec.getValue()) {
				edNameSearch = edNameSearch.toLowerCase().strip();
				if (! unfilteredEnDeCoders.contains(edNameSearch)) {
					continue;
				}
				//
				if (! cfgDisabledEnDeCodersList.isEmpty()) {
					boolean tmpIsDisabled = false;
					for (String tmpDisabledEntry : cfgDisabledEnDeCodersList) {
						if (tmpDisabledEntry.toLowerCase().strip().equals(edNameSearch)) {
							tmpIsDisabled = true;
							break;
						}
					}
					if (tmpIsDisabled) {
						continue;
					}
				}
				//
				resM.get(tmpEnDeCodersForCodec.getKey()).add(edNameSearch);
			}
		}
		return resM;
	}

}
