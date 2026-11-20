#!/usr/bin/env bash

#
# by TS, Sep 2026
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
	echo "Usage: ${VAR_MYNAME} [cb_javacpp_ffmpeg [rkmpp]]"
	echo "Examples: ${VAR_MYNAME}"
	echo "          ${VAR_MYNAME} cb_javacpp_ffmpeg"
	echo "          ${VAR_MYNAME} cb_javacpp_ffmpeg rkmpp"
	echo ""
	echo "Options:"
	echo "  'cb_javacpp_ffmpeg': Use custom-built JavaCPP and FFmpeg version"
	echo "  'rkmpp'            : Use Docker Image with ARM64 RKMPP (Rockchip hardware accelerated video en-/decoders) support"
}

if [ $# -gt 2 ]; then
	echo -e "${VAR_MYNAME}: invalid number of arguments\n" >/dev/stderr
	printUsage >/dev/stderr
	exit 1
fi

LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG=false
LOPT_DO_RKMPP=false

while [ $# -gt 0 ]; do
	if [ "${1}" = "--help" ]; then
		printUsage
		exit 0
	fi
	if [ "${1}" = "cb_javacpp_ffmpeg" ]; then
		LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG=true
		shift
		continue
	fi
	if [ "${1}" = "rkmpp" ]; then
		LOPT_DO_RKMPP=true
		shift
		continue
	fi
	#
	echo "${VAR_MYNAME}: invalid arg '${1}'" >/dev/stderr
	exit 1
done

if [ "${LOPT_DO_RKMPP}" = "true" ] && [ "${LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG}" != "true" ]; then
	echo "${VAR_MYNAME}: Option 'rkmpp' is only allowed in combination with 'cb_javacpp_ffmpeg'" >/dev/stderr
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
if [ "${LVAR_DEBIAN_DIST}" = "arm64" ]; then
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
	--name "${LVAR_CONT_NAME}" \
	--entrypoint "/bin/bash" \
	${LVAR_SEC} \
	${LVAR_DEVICES} \
	"${LCFG_IMAGE_NAME}:latest"
