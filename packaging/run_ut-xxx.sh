#!/usr/bin/env bash

#
# by TS, Nov 2026
#

VAR_MYNAME="$(basename "$0")"

# ----------------------------------------------------------

# Outputs CPU architecture string
#
# @param string $1 debian_rootfs|debian_dist
#
# @return int EXITCODE
function _getCpuArch() {
	case "$(uname -m)" in
		x86_64*)
			echo -n ""
			if [ "$1" = "debian_dist" ]; then
				echo -n "amd64"
			elif [ "$1" = "app" ]; then
				echo -n "x64"
			else
				echo "${VAR_MYNAME}: Error: invalid arg '$1'" >/dev/stderr
				return 1
			fi
			;;
		aarch64*)
			if [ "$1" = "debian_dist" ] || [ "$1" = "app" ]; then
				echo -n "arm64"
			else
				echo "${VAR_MYNAME}: Error: invalid arg '$1'" >/dev/stderr
				return 1
			fi
			;;
		*)
			echo "${VAR_MYNAME}: Error: Unknown CPU architecture '$(uname -m)'" >/dev/stderr
			return 1
			;;
	esac
	return 0
}

_getCpuArch debian_dist >/dev/null || exit 1

# ----------------------------------------------------------

function printUsage() {
	echo "Usage: ${VAR_MYNAME} [cob:<COMMIT_OR_BRANCH>] [cb_javacpp_ffmpeg [rkmpp]] <UNIT_TEST_CLASS> [<OUTPUT_FILE_TO_COPY>]"
	echo "Examples: ${VAR_MYNAME} 'io.github.tsitle.lib_ffmpeg.FfSharedLibInfoTest'"
	echo "          ${VAR_MYNAME}"
	echo "                  'org.example.dmx_tc_mux.MuxFromSubStreamsAv1Mp3Test.test_inpA_a_tc_h264_aac_mkv'"
	echo "                  'data/outp/test-av1_mp3-bbb-a-a1-tc-h264_aac-mkv.mkv'"
	echo "          ${VAR_MYNAME} cb_javacpp_ffmpeg 'io.github.tsitle.lib_ffmpeg.*'"
	echo "          ${VAR_MYNAME} cb_javacpp_ffmpeg rkmpp 'io.github.tsitle.lib_ffmpeg.*'"
	echo "          ${VAR_MYNAME} cob:new_feature cb_javacpp_ffmpeg rkmpp 'io.github.tsitle.lib_ffmpeg.*'"
	echo "          ${VAR_MYNAME} cob:dcb26c6a cb_javacpp_ffmpeg rkmpp 'io.github.tsitle.lib_ffmpeg.*'"
	echo ""
	echo "Options:"
	echo "  'cob:<COMMIT_OR_BRANCH>': Commit or branch to use"
	echo "  'cb_javacpp_ffmpeg'     : Use custom-built JavaCPP and FFmpeg version"
	echo "  'rkmpp'                 : Use Docker Image with ARM64 RKMPP (Rockchip hardware accelerated video en-/decoders) support"
	#echo "  'rf'                    : Use Docker Image with FFmpeg-Rockchip fork (for better RKMPP support)"
}

if [ $# -lt 1 ] || [ $# -gt 5 ]; then
	echo -e "${VAR_MYNAME}: invalid number of arguments\n" >/dev/stderr
	printUsage >/dev/stderr
	exit 1
fi

LOPT_LIB_FFMPEG_COMMIT_OR_BRANCH=""
LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG=false
LOPT_DO_RKMPP=false
LOPT_DO_WITH_ROCKCHIP_FORK=false

while [ $# -gt 0 ]; do
	if [ "${1}" = "--help" ]; then
		printUsage
		exit 0
	fi
	#
	if echo -n "${1}" | grep -q ":"; then
		TMP_ARG_K="$(echo -n "${1}" | cut -f1 -d:)"
		TMP_ARG_V="$(echo -n "${1}" | cut -f2 -d:)"
		if [ "${TMP_ARG_K}" = "cob" ]; then
			LOPT_LIB_FFMPEG_COMMIT_OR_BRANCH="${TMP_ARG_V}"
		else
			echo "${VAR_MYNAME}: invalid arg '${1}'" >/dev/stderr
			exit 1
		fi
		shift
		continue
	fi
	#
	if [ "${1}" = "cb_javacpp_ffmpeg" ]; then
		LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG=true
		shift
		continue
	fi
	if [ "${1}" = "rkmpp" ]; then
		LOPT_DO_RKMPP=true
		LOPT_DO_WITH_ROCKCHIP_FORK=true  # enabled by default
		shift
		continue
	fi
	#if [ "${1}" = "rf" ]; then
		#LOPT_DO_WITH_ROCKCHIP_FORK=true
		#shift
		#continue
	#fi
	break
done

if [ "${LOPT_DO_RKMPP}" = "true" ] && [ "${LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG}" != "true" ]; then
	echo "${VAR_MYNAME}: Option 'rkmpp' is only allowed in combination with 'cb_javacpp_ffmpeg'" >/dev/stderr
	exit 1
fi
if [ "${LOPT_DO_WITH_ROCKCHIP_FORK}" = "true" ] && [ "${LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG}" != "true" ]; then
	echo "${VAR_MYNAME}: Option 'rf' is only allowed in combination with 'cb_javacpp_ffmpeg'" >/dev/stderr
	exit 1
fi

# ----------------------------------------------------------

LVAR_DEBIAN_DIST="$(_getCpuArch debian_dist)"

LVAR_EXTRA_IMAGE_NAME_QUALIFIER=""
[ "${LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG}" = "true" ] && LVAR_EXTRA_IMAGE_NAME_QUALIFIER+="-cb_javacpp_ffmpeg"
[ "${LOPT_DO_RKMPP}" = "true" ] && LVAR_EXTRA_IMAGE_NAME_QUALIFIER+="-rkmpp"

LCFG_IMAGE_NAME="lib-ts_ffmpeg${LVAR_EXTRA_IMAGE_NAME_QUALIFIER}-test-${LVAR_DEBIAN_DIST}"

LVAR_SEC=""
LVAR_DEVICES=""
if [ "${LOPT_DO_RKMPP}" = "true" ] && [ "${LVAR_DEBIAN_DIST}" = "arm64" ]; then
	LVAR_SEC="--security-opt systempaths=unconfined"
	LVAR_SEC+=" --security-opt apparmor=unconfined"
	#LVAR_SEC+=" --privileged"
	LVAR_DEVICES="--device /dev/dri:/dev/dri"
	LVAR_DEVICES+=" --device /dev/dma_heap:/dev/dma_heap"
	LVAR_DEVICES+=" --device /dev/mpp_service:/dev/mpp_service"
	LVAR_DEVICES+=" --device /dev/rga:/dev/rga"
	LVAR_DEVICES+=" --device /dev/mali0:/dev/mali0"
fi

LVAR_CONT_NAME="lib-ts_ffmpeg-test"
[ "${LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG}" = "true" ] && LVAR_CONT_NAME+="-cb_javacpp_ffmpeg"
[ "${LOPT_DO_RKMPP}" = "true" ] && LVAR_CONT_NAME+="-rkmpp"

docker run \
	-it --rm \
	-d \
	--name "${LVAR_CONT_NAME}" \
	--entrypoint "/bin/bash" \
	${LVAR_SEC} \
	${LVAR_DEVICES} \
	-e GRADLE_OPTS="--enable-native-access=ALL-UNNAMED -Dorg.gradle.daemon=false" \
	-e TS_LIB_FFMPEG_UT_DISABLED_DECODERS="av1,av1_cuvid,av1_qsv,h264_cuvid,hevc_cuvid,mjpeg_cuvid,mpeg2_cuvid,mpeg4_cuvid,vp8_cuvid,vp8_rkmpp,vp9_cuvid,vp9_rkmpp" \
	-e TS_LIB_FFMPEG_UT_DISABLED_ENCODERS="av1_nvenc,av1_qsv,h264_nvenc,h264_qsv,hevc_nvenc,hevc_v4l2m2m,hevc_qsv" \
	"${LCFG_IMAGE_NAME}:latest"

TMP_GRADLE_EXTRA_ARGS="-PuseCustomLibFromMavenCentral_xrtxp"
[ "${LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG}" = "true" ] && TMP_GRADLE_EXTRA_ARGS+=" -PuseCustomBuiltJavaCppAndFfmpeg"
test "${LOPT_DO_WITH_ROCKCHIP_FORK}" = "true" && TMP_GRADLE_EXTRA_ARGS+=" -PwithRockchipFork"
test "${LOPT_DO_RKMPP}" = "true" && TMP_GRADLE_EXTRA_ARGS+=" -PwithRkmpp"

docker exec -it \
	"${LVAR_CONT_NAME}" \
	git pull || exit 1
if [ -n "${LOPT_LIB_FFMPEG_COMMIT_OR_BRANCH}" ]; then
	docker exec -it \
		"${LVAR_CONT_NAME}" \
		git checkout "${LOPT_LIB_FFMPEG_COMMIT_OR_BRANCH}" || exit 1
fi
docker exec -it \
	"${LVAR_CONT_NAME}" \
	./gradlew :test --tests "${1}" --no-daemon ${TMP_GRADLE_EXTRA_ARGS}
TMP_R=$?

if [ ${TMP_R} -eq 0 ] && [ -n "${2}" ]; then
	echo "${VAR_MYNAME}: Copying '${2}' from Containter to 'tests_output/'..."
	test -d "tests_output" || mkdir "tests_output"
	#docker cp "${LVAR_CONT_NAME}":"/build/ts_java_lib_ffmpeg/${2}" "tests_output/"  # doesn't work with wildcards
	docker exec \
		"${LVAR_CONT_NAME}" \
		sh -c "tar -C /build/ts_java_lib_ffmpeg/ -cf - ${2}" | tar -C "tests_output/" -xf -
fi

docker stop "${LVAR_CONT_NAME}"
echo "${VAR_MYNAME}: Done."

exit ${TMP_R}
