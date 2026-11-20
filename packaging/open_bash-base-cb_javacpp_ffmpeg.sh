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
			if [ "$1" = "debian_rootfs" ] || [ "$1" = "debian_dist" ]; then
				echo -n "amd64"
			elif [ "$1" = "graal" ]; then
				echo -n "x64"
			elif [ "$1" = "javacpp" ]; then
				echo -n "x86_64"
			else
				echo "${VAR_MYNAME}: Error: invalid arg '$1'" >/dev/stderr
				return 1
			fi
			;;
		aarch64*)
			if [ "$1" = "debian_rootfs" ]; then
				echo -n "arm64v8"
			elif [ "$1" = "debian_dist" ] || [ "$1" = "javacpp" ]; then
				echo -n "arm64"
			elif [ "$1" = "graal" ]; then
				echo -n "aarch64"
			else
				echo "${VAR_MYNAME}: Error: invalid arg '$1'" >/dev/stderr
				return 1
			fi
			;;
		armv7*)
			if [ "$1" = "debian_rootfs" ]; then
				echo -n "arm32v7"
			elif [ "$1" = "debian_dist" ]; then
				echo -n "armhf"
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
	echo "Usage: ${VAR_MYNAME} [rkmpp]"
	echo "Examples: ${VAR_MYNAME}"
	echo "          ${VAR_MYNAME} rkmpp"
	echo ""
	echo "Options:"
	echo "  'rkmpp': Use Docker Image with ARM64 RKMPP (Rockchip hardware accelerated video en-/decoders) support"
}

if [ $# -gt 1 ]; then
	echo -e "${VAR_MYNAME}: invalid number of arguments\n" >/dev/stderr
	printUsage >/dev/stderr
	exit 1
fi

LOPT_DO_RKMPP=false

while [ $# -gt 0 ]; do
	if [ "${1}" = "--help" ]; then
		printUsage
		exit 0
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

# ----------------------------------------------------------

LVAR_DEBIAN_DIST="$(_getCpuArch debian_dist)"

LVAR_GRAAL_VERSION_MAJOR="25"
LVAR_FFMPEG_VERSION="8.1.2"
LVAR_JAVACPP_VERSION="1.5.14"

LVAR_IMAGE_GFJ_BASE_NAME="lib-ts_ffmpeg-gfj_base-cb_javacpp_ffmpeg-${LVAR_DEBIAN_DIST}"
LVAR_IMAGE_GFJ_BASE_VER="g${LVAR_GRAAL_VERSION_MAJOR}_f${LVAR_FFMPEG_VERSION}_j${LVAR_JAVACPP_VERSION}"
[ "${LOPT_DO_RKMPP}" = "true" ] && LVAR_IMAGE_GFJ_BASE_VER+="_rkmpp"

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

LVAR_CONT_NAME="lib-ts_ffmpeg-gfj_base-cb_javacpp_ffmpeg"
[ "${LOPT_DO_RKMPP}" = "true" ] && LVAR_CONT_NAME+="-rkmpp"

docker run \
	-it --rm \
	--name "${LVAR_CONT_NAME}" \
	--entrypoint "/bin/bash" \
	${LVAR_SEC} \
	${LVAR_DEVICES} \
	"${LVAR_IMAGE_GFJ_BASE_NAME}:${LVAR_IMAGE_GFJ_BASE_VER}"
