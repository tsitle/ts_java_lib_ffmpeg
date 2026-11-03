package org.example.dmx_tc_mux;

import io.github.tsitle.lib_ffmpeg.FfmpegCodec;
import io.github.tsitle.lib_ffmpeg.FfmpegPktConvModeAac;
import io.github.tsitle.lib_ffmpeg.FfmpegPktConvModeH26x;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDmxSubStreamInfoAudio;
import io.github.tsitle.lib_ffmpeg.demux.FfmpegDmxSubStreamInfoVideo;
import io.github.tsitle.lib_ffmpeg.tc.FfmpegTcSettingsOutVideo;
import io.github.tsitle.lib_xrtxp.avdata.extradata.ExtradataContainerHex;
import io.github.tsitle.lib_xrtxp.avdata.extradata.ExtradataContainerSdp;
import io.github.tsitle.lib_xrtxp.avdata.extradata.ExtradataForSdpHelper;
import io.github.tsitle.lib_xrtxp.avdata.extradata.ExtradataFromSdpHelper;
import io.github.tsitle.lib_xrtxp.common.types.ImageDimensions;
import io.github.tsitle.lib_xrtxp.common.types.RationalNumber;
import io.github.tsitle.lib_xrtxp.common.types.SampleRateEnum;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MuxFromSubStreamsH264AacTest {

	@ParameterizedTest
	@ValueSource(strings = {"a1-tc-h265_ac3-mkv"})
	void test_inpA_a_tc_mkv(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_H265,
				FfmpegCodec.A_AC3,
				false,
				false,
				true
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"a2-tc-h265_ac3-mp4"})
	void test_inpA_a_tc_mp4(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_H265,
				FfmpegCodec.A_AC3,
				false,
				false,
				false
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"b1-cpV_cpA-h264_aac-mkv"})
	void test_inpA_b_copyVid_copyAud_mkv(String subTestNr) {
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
	@ValueSource(strings = {"b2-cpV_cpA-h264_aac-mp4"})
	void test_inpA_b_copyVid_copyAud_mp4(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_VP8,  // this will be ignored
				FfmpegCodec.A_FLAC,  // this will be ignored
				true,
				true,
				false
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"c1-tcV_noA-vp8"})
	void test_inpA_c_tcVid_noAud(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_VP8,
				FfmpegCodec.UNKNOWN,
				false,
				false,
				true
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"c2-noV_tcA-aac"})
	void test_inpA_c_noVid_tcAud(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.UNKNOWN,
				FfmpegCodec.A_AAC,
				false,
				false,
				false
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"d1-tcV_cpA-vp8_aac"})
	void test_inpA_d_tcVid_copyAud(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_VP8,
				FfmpegCodec.UNKNOWN,
				false,
				true,
				true
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"d2-cpV_tcA-h264_ac3"})
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

	@ParameterizedTest
	@ValueSource(strings = {"e1-tcV_cpA-mjpeg_rtp_aac"})
	void test_inpA_e_tcVid_cpAud(String subTestNr) {
		internalTest_inputA(
				subTestNr,
				FfmpegCodec.V_MJPEG,
				FfmpegCodec.A_AC3,  // this will be ignored
				false,
				true,
				true,
				true
			);
	}

	// -----------------------------------------------------------------------------------------------------------------

	@ParameterizedTest
	@ValueSource(strings = {"a1-tc-h265_ac3-mkv"})
	void test_inpB_a_tc_mkv(String subTestNr) {
		internalTest_inputB(
				subTestNr,
				FfmpegCodec.V_H265,
				FfmpegCodec.A_AC3,
				false,
				false,
				true
			);
	}

	@Disabled
	@ParameterizedTest
	@ValueSource(strings = {"a2-tc-h265_ac3-mp4"})
	void test_inpB_a_tc_mp4(String subTestNr) {
		internalTest_inputB(
				subTestNr,
				FfmpegCodec.V_H265,
				FfmpegCodec.A_AC3,
				false,
				false,
				false
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"b1-cpV_cpA-h264_aac-mkv"})
	void test_inpB_b_copyVid_copyAud_mkv(String subTestNr) {
		internalTest_inputB(
				subTestNr,
				FfmpegCodec.V_VP8,  // this will be ignored
				FfmpegCodec.A_FLAC,  // this will be ignored
				true,
				true,
				true
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"b2-cpV_cpA-h264_aac-mp4"})
	void test_inpB_b_copyVid_copyAud_mp4(String subTestNr) {
		internalTest_inputB(
				subTestNr,
				FfmpegCodec.V_VP8,  // this will be ignored
				FfmpegCodec.A_FLAC,  // this will be ignored
				true,
				true,
				false
			);
	}

	@Disabled
	@ParameterizedTest
	@ValueSource(strings = {"c1-tcV_noA-vp8"})
	void test_inpB_c_tcVid_noAud(String subTestNr) {
		internalTest_inputB(
				subTestNr,
				FfmpegCodec.V_VP8,
				FfmpegCodec.UNKNOWN,
				false,
				false,
				true
			);
	}

	@Disabled
	@ParameterizedTest
	@ValueSource(strings = {"c2-noV_tcA-aac"})
	void test_inpB_c_noVid_tcAud(String subTestNr) {
		internalTest_inputB(
				subTestNr,
				FfmpegCodec.UNKNOWN,
				FfmpegCodec.A_AAC,
				false,
				false,
				false
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"d1-tcV_cpA-vp8_aac"})
	void test_inpB_d_tcVid_cpAud(String subTestNr) {
		internalTest_inputB(
				subTestNr,
				FfmpegCodec.V_VP8,
				FfmpegCodec.UNKNOWN,
				false,
				true,
				true
			);
	}

	@ParameterizedTest
	@ValueSource(strings = {"d2-cpV_tcA-h264_ac3"})
	void test_inpB_d_copyVid_tcAud(String subTestNr) {
		internalTest_inputB(
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
		inputSsInfoVid.ffmpegCodec = FfmpegCodec.V_H264;
		inputSsInfoVid.timeBasePts = RationalNumber.of(1, 1000);
		inputSsInfoVid.fps = RationalNumber.of(25, 1);
		inputSsInfoVid.imgDims = ImageDimensions.of(1280, 720);
		// do some unnecessary conversion here to exersice the ExtradataForSdpHelper
		ExtradataContainerHex tmpEch = ExtradataContainerHex.ofH264_annexB(
				"00000001674D401F965280A00B7602D404040500000303E80000C350E000000AAE6000006ACFC2EF2E0F8E14CB0000000168EB8F20"
			);
		ExtradataContainerSdp tmpEcs = ExtradataForSdpHelper.buildH264ExtradataForSdp(tmpEch);
		inputSsInfoVid.extradataHex.copyFrom(ExtradataFromSdpHelper.buildH264EncoderExtradataFromSdp(
				true,  // <-- doesn't matter since the Transcoder will convert the extradata if necessary
				tmpEcs
			));

		//
		inputSsInfoAud.clear();
		inputSsInfoAud.subStreamIx = 1;
		inputSsInfoAud.ffmpegCodec = FfmpegCodec.A_AAC;
		inputSsInfoAud.timeBasePts = RationalNumber.of(1, 1000);
		inputSsInfoAud.sampleRate = SampleRateEnum.SR_048000;
		inputSsInfoAud.channelCount = 2;
		//
		inputSsInfoAud.extradataHex.copyFrom(ExtradataContainerHex.ofAac("119056E500"));

		return "bbb-cvH264_AAC-cq25-25fps-720p-stereo_48k-30s.mkv";
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
				false
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
				boolean useMjpegRtp
			) {
		FfmpegDmxSubStreamInfoVideo inputSsInfoVid = new FfmpegDmxSubStreamInfoVideo();
		FfmpegDmxSubStreamInfoAudio inputSsInfoAud = new FfmpegDmxSubStreamInfoAudio();

		ConfigDmxTcMux localCfg = new ConfigDmxTcMux();
		localCfg.dmx.demuxOnly_modeH26x = FfmpegPktConvModeH26x.LENGTH_PREFIXED;
		localCfg.dmx.demuxOnly_modeAac = FfmpegPktConvModeAac.NO_ADTS;

		localCfg.tc.vid.videoTargetCodec = codecOutVid;
		localCfg.tc.vid.copySubStreamVideo = copyVideo;
		localCfg.tc.vid.videoModeH26x = FfmpegPktConvModeH26x.ANNEXB;
		if (useMjpegRtp) {
			localCfg.tc.vid.videoModeMjpeg = FfmpegTcSettingsOutVideo.OutputModeMjpeg.RTP;
		}
		localCfg.tc.aud.audioTargetCodec = codecOutAud;
		localCfg.tc.aud.copySubStreamAudio = copyAudio;
		localCfg.tc.aud.audioModeAac = FfmpegPktConvModeAac.WITH_ADTS;

		localCfg.mux.outputFilePath = String.format("test-h264_aac-bbb-a-%s", subTestNr);
		localCfg.mux.preferMkvOverMp4 = preferMkvOverMp4;

		// ---------------------------------------------------------

		localCfg.dmx.inputFilePath = setInputSsInfo(inputSsInfoVid, inputSsInfoAud);

		// ---------------------------------------------------------

		MuxFromSubStreams mfss = new MuxFromSubStreams(inputSsInfoVid, inputSsInfoAud);
		assertTrue(mfss.start(localCfg));
	}

	/** Demux: AnnexB+withADTS, Transcode: Length-Prefixed+noADTS */
	private void internalTest_inputB(
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
		localCfg.dmx.demuxOnly_modeH26x = FfmpegPktConvModeH26x.ANNEXB;
		localCfg.dmx.demuxOnly_modeAac = FfmpegPktConvModeAac.WITH_ADTS;

		localCfg.tc.vid.videoTargetCodec = codecOutVid;
		localCfg.tc.vid.copySubStreamVideo = copyVideo;
		localCfg.tc.vid.videoModeH26x = FfmpegPktConvModeH26x.LENGTH_PREFIXED;
		localCfg.tc.aud.audioTargetCodec = codecOutAud;
		localCfg.tc.aud.copySubStreamAudio = copyAudio;
		localCfg.tc.aud.audioModeAac = FfmpegPktConvModeAac.NO_ADTS;

		localCfg.mux.outputFilePath = String.format("test-h264_aac-bbb-b-%s", subTestNr);
		localCfg.mux.preferMkvOverMp4 = preferMkvOverMp4;

		// ---------------------------------------------------------

		localCfg.dmx.inputFilePath = setInputSsInfo(inputSsInfoVid, inputSsInfoAud);

		// ---------------------------------------------------------

		MuxFromSubStreams mfss = new MuxFromSubStreams(inputSsInfoVid, inputSsInfoAud);
		assertTrue(mfss.start(localCfg));
	}

}
