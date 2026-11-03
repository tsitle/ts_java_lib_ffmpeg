package org.example.dmx_tc_mux;

import io.github.tsitle.lib_ffmpeg.FfmpegCodec;
import io.github.tsitle.lib_ffmpeg.FfmpegPktConvModeAac;
import io.github.tsitle.lib_ffmpeg.FfmpegPktConvModeH26x;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDmxSubStreamInfoAudio;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDmxSubStreamInfoVideo;
import io.github.tsitle.lib_xrtxp.avdata.extradata.ExtradataContainerHex;
import io.github.tsitle.lib_xrtxp.common.types.ImageDimensions;
import io.github.tsitle.lib_xrtxp.common.types.RationalNumber;
import io.github.tsitle.lib_xrtxp.common.types.SampleRateEnum;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MuxFromSubStreamsVp8OpusTest {

	@Disabled
	@ParameterizedTest
	@ValueSource(strings = {"a1-tc-h264_aac-mkv"})
	void test_inpA_a_tc_h264_aac_mkv(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_H264,
				FfmpegCodec.A_AAC,
				false,
				false,
				true
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"a2-tc-h264_ac3-mp4"})
	void test_inpA_a_tc_h264_ac3_mp4(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_H264,
				FfmpegCodec.A_AC3,
				false,
				false,
				false
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"b1-cpV_cpA-vp8_opus"})
	void test_inpA_b_copyVid_copyAud(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_VP8,  // this will be ignored
				FfmpegCodec.A_FLAC,  // this will be ignored
				true,
				true,
				true
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"b2-cpV_noA-vp8"})
	void test_inpA_b_copyVid_noAud(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_VP9,  // this will be ignored
				FfmpegCodec.UNKNOWN,
				true,
				false,
				true
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"b3-noV_cpA-opus"})
	void test_inpA_b_noVid_copyAud(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.UNKNOWN,
				FfmpegCodec.A_FLAC,  // this will be ignored
				false,
				true,
				true
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"c1-tcV_noA-vp9"})
	void test_inpA_c_tcVid_noAud(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_VP9,
				FfmpegCodec.UNKNOWN,
				false,
				false,
				true
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"c2-noV_tcA-eac3"})
	void test_inpA_c_noVid_tcAud(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.UNKNOWN,
				FfmpegCodec.A_EAC3,
				false,
				false,
				false
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"d1-tcV_cpA-vp9_opus"})
	void test_inpA_d_tcVid_copyAud(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_VP9,
				FfmpegCodec.UNKNOWN,
				false,
				true,
				true
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"d2-cpV_tcA-vp8_ac3"})
	void test_inpA_d_copyVid_tcAud(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_VP9,  // this will be ignored
				FfmpegCodec.A_AC3,
				true,
				false,
				true
			);
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private @NonNull String setInputSsInfo(
				@NonNull FfmpegDmxSubStreamInfoVideo inputSsInfoVid,
				@NonNull FfmpegDmxSubStreamInfoAudio inputSsInfoAud
			) {
		inputSsInfoVid.clear();
		inputSsInfoVid.subStreamIx = 0;
		inputSsInfoVid.ffmpegCodec = FfmpegCodec.V_VP8;
		inputSsInfoVid.timeBasePts = RationalNumber.of(1, 1000);
		inputSsInfoVid.fps = RationalNumber.of(30, 1);
		inputSsInfoVid.imgDims = ImageDimensions.of(1280, 720);
		inputSsInfoVid.extradataHex.clear();

		//
		inputSsInfoAud.clear();
		inputSsInfoAud.subStreamIx = 1;
		inputSsInfoAud.ffmpegCodec = FfmpegCodec.A_OPUS;
		inputSsInfoAud.timeBasePts = RationalNumber.of(1, 1000);
		inputSsInfoAud.sampleRate = SampleRateEnum.SR_048000;
		inputSsInfoAud.channelCount = 6;
		inputSsInfoAud.extradataHex.copyFrom(
				ExtradataContainerHex.ofOpus("4F707573486561640106380180BB00000000010402000401020305")
			);

		return "bbb-cvVP8_OPUS-cq25-30fps-720p-surround_51_48k-30s.webm";
	}

	/** Demux: Length-Prefixed+noADTS, Transcode: AnnexB+withADTS */
	private void internalTest_inputA(
				@NonNull String subTestNr,
				@NonNull FfmpegCodec codecOutVid,
				@NonNull FfmpegCodec codecOutAud,
				boolean copyVideo,
				boolean copyAudio,
				boolean preferMkvOverMp4
			) {
		FfmpegDmxSubStreamInfoVideo inputSsInfoVid = new FfmpegDmxSubStreamInfoVideo();
		FfmpegDmxSubStreamInfoAudio inputSsInfoAud = new FfmpegDmxSubStreamInfoAudio();

		ConfigDmxTcMux localCfg = new ConfigDmxTcMux();
		localCfg.dmx.demuxOnly_modeH26x = FfmpegPktConvModeH26x.LENGTH_PREFIXED;
		localCfg.dmx.demuxOnly_modeAac = FfmpegPktConvModeAac.NO_ADTS;

		localCfg.tc.vid.videoTargetCodec = codecOutVid;
		localCfg.tc.vid.copySubStreamVideo = copyVideo;
		localCfg.tc.vid.videoModeH26x = FfmpegPktConvModeH26x.ANNEXB;
		localCfg.tc.aud.audioTargetCodec = codecOutAud;
		localCfg.tc.aud.copySubStreamAudio = copyAudio;
		localCfg.tc.aud.audioModeAac = FfmpegPktConvModeAac.WITH_ADTS;

		localCfg.mux.outputFilePath = String.format("test-vp8_opus-bbb-a-%s", subTestNr);
		localCfg.mux.preferMkvOverMp4 = preferMkvOverMp4;

		// ---------------------------------------------------------

		localCfg.dmx.inputFilePath = setInputSsInfo(inputSsInfoVid, inputSsInfoAud);

		// ---------------------------------------------------------

		MuxFromSubStreams mfss = new MuxFromSubStreams(inputSsInfoVid, inputSsInfoAud);
		assertTrue(mfss.start(localCfg));
	}

}
