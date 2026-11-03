package org.example.dmx_tc_mux;

import io.github.tsitle.lib_ffmpeg.FfmpegCodec;
import io.github.tsitle.lib_ffmpeg.FfmpegPktConvModeAac;
import io.github.tsitle.lib_ffmpeg.FfmpegPktConvModeH26x;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDmxSubStreamInfoAudio;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDmxSubStreamInfoVideo;
import io.github.tsitle.lib_ffmpeg.tc.FfmpegTcSettingsOutAudio;
import io.github.tsitle.lib_xrtxp.common.types.ImageDimensions;
import io.github.tsitle.lib_xrtxp.common.types.RationalNumber;
import io.github.tsitle.lib_xrtxp.common.types.SampleRateEnum;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MuxFromSubStreamsVp9Eac3Test {

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

	@Disabled
	@ParameterizedTest
	@ValueSource(strings = {"a2-tc-h264_mp3-mp4"})
	void test_inpA_a_tc_h264_mp3_mp4(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_H264,
				FfmpegCodec.A_MP3,
				false,
				false,
				false
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"b1-cpV_cpA-vp9_eac3"})
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

	@Disabled
	@ParameterizedTest
	@ValueSource(strings = {"b2-cpV_noA-vp9"})
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

	@Disabled
	@ParameterizedTest
	@ValueSource(strings = {"b3-noV_cpA-eac3"})
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

	@Disabled
	@ParameterizedTest
	@ValueSource(strings = {"c1-tcV_noA-av1"})
	void test_inpA_c_tcVid_noAud(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_AV1,
				FfmpegCodec.UNKNOWN,
				false,
				false,
				true
			);
	}

	@Disabled
	@ParameterizedTest
	@ValueSource(strings = {"c2-noV_tcA-ac3"})
	void test_inpA_c_noVid_tcAud(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.UNKNOWN,
				FfmpegCodec.A_AC3,
				false,
				false,
				false
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"d1-tcV_cpA-av1_eac3"})
	void test_inpA_d_tcVid_copyAud(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_AV1,
				FfmpegCodec.UNKNOWN,
				false,
				true,
				true
			);
	}

	@Disabled
	@ParameterizedTest
	@ValueSource(strings = {"d2-cpV_tcA-vp9_aac-cmPT"})
	void test_inpA_d_copyVid_tcAud_audCmPt(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_VP9,  // this will be ignored
				FfmpegCodec.A_AAC,
				true,
				false,
				true
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"d2-cpV_tcA-vp9_aac-cmDMS"})
	void test_inpA_d_copyVid_tcAud_audCmDmSt(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_VP9,  // this will be ignored
				FfmpegCodec.A_AAC,
				true,
				false,
				true,
				FfmpegTcSettingsOutAudio.ChannelConversionMode.DOWNMIX_STEREO
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
		inputSsInfoVid.ffmpegCodec = FfmpegCodec.V_VP9;
		inputSsInfoVid.timeBasePts = RationalNumber.of(1, 1000);
		inputSsInfoVid.fps = RationalNumber.ofFps(29.97);
		inputSsInfoVid.imgDims = ImageDimensions.of(1280, 720);
		inputSsInfoVid.extradataHex.clear();

		//
		inputSsInfoAud.clear();
		inputSsInfoAud.subStreamIx = 1;
		inputSsInfoAud.ffmpegCodec = FfmpegCodec.A_EAC3;
		inputSsInfoAud.timeBasePts = RationalNumber.of(1, 1000);
		inputSsInfoAud.sampleRate = SampleRateEnum.SR_044100;
		inputSsInfoAud.channelCount = 6;
		inputSsInfoAud.extradataHex.clear();

		return "bbb-cvVP9_EAC3-cq25-2997fps-720p-surround_51_44k-30s.mkv";
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
		internalTest_inputA(
				subTestNr,
				codecOutVid,
				codecOutAud,
				copyVideo,
				copyAudio,
				preferMkvOverMp4,
				FfmpegTcSettingsOutAudio.ChannelConversionMode.PASSTHROUGH
			);
	}

	/** Demux: Length-Prefixed+noADTS, Transcode: AnnexB+withADTS */
	private void internalTest_inputA(
				@NonNull String subTestNr,
				@NonNull FfmpegCodec codecOutVid,
				@NonNull FfmpegCodec codecOutAud,
				boolean copyVideo,
				boolean copyAudio,
				boolean preferMkvOverMp4,
				FfmpegTcSettingsOutAudio.@NonNull ChannelConversionMode audioChannelCm
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
		localCfg.tc.aud.audioChannelCm = audioChannelCm;

		localCfg.mux.outputFilePath = String.format("test-vp9_eac3-bbb-a-%s", subTestNr);
		localCfg.mux.preferMkvOverMp4 = preferMkvOverMp4;

		// ---------------------------------------------------------

		localCfg.dmx.inputFilePath = setInputSsInfo(inputSsInfoVid, inputSsInfoAud);

		// ---------------------------------------------------------

		MuxFromSubStreams mfss = new MuxFromSubStreams(inputSsInfoVid, inputSsInfoAud);
		assertTrue(mfss.start(localCfg));
	}

}
