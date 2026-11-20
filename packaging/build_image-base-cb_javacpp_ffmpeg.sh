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
	echo "  'rkmpp': Build with ARM64 RKMPP (Rockchip hardware accelerated video en-/decoders) support"
	#echo "  'rf'   : Build with FFmpeg-Rockchip fork (for better RKMPP support)"
}

if [ $# -gt 1 ]; then
	echo -e "${VAR_MYNAME}: invalid number of arguments\n" >/dev/stderr
	printUsage >/dev/stderr
	exit 1
fi

LOPT_DO_RKMPP=false
LOPT_DO_WITH_ROCKCHIP_FORK=false

while [ $# -gt 0 ]; do
	if [ "${1}" = "--help" ]; then
		printUsage
		exit 0
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
	#
	echo "${VAR_MYNAME}: invalid arg '${1}'" >/dev/stderr
	exit 1
done

# ----------------------------------------------------------
# - GraalVM + FFmpeg + JavaCPP base image ------------------
# ----------------------------------------------------------

cd build-ctx-gfj-base-cb_javacpp_ffmpeg || exit 1

LVAR_DEBIAN_DIST="$(_getCpuArch debian_dist)"

if [ "${LOPT_DO_RKMPP}" = "true" ] && [ "${LVAR_DEBIAN_DIST}" != "arm64" ]; then
	echo "${VAR_MYNAME}: Option 'rkmpp' is only allowed for ARM64" >/dev/stderr
	exit 1
fi
if [ "${LOPT_DO_WITH_ROCKCHIP_FORK}" = "true" ] && [ "${LVAR_DEBIAN_DIST}" != "arm64" ]; then
	echo "${VAR_MYNAME}: Option 'rf' is only allowed for ARM64" >/dev/stderr
	exit 1
fi

LVAR_GRAAL_ARCH="$(_getCpuArch graal)"
LVAR_GRAAL_VERSION_MAJOR="25"
LVAR_FFMPEG_MAJMIN_VERSION="8.1"
LVAR_FFMPEG_VERSION="${LVAR_FFMPEG_MAJMIN_VERSION}.2"
LVAR_JAVACPP_VERSION="1.5.14"
LVAR_JAVACPP_ARCH="$(_getCpuArch javacpp)"

LVAR_IMAGE_GFJ_BASE_NAME="lib-ts_ffmpeg-gfj_base-${LVAR_DEBIAN_DIST}"
LVAR_IMAGE_GFJ_BASE_VER="g${LVAR_GRAAL_VERSION_MAJOR}"

docker image inspect "${LVAR_IMAGE_GFJ_BASE_NAME}":"${LVAR_IMAGE_GFJ_BASE_VER}" >/dev/null 2>&1 || {
	echo "${VAR_MYNAME}: base image '${LVAR_IMAGE_GFJ_BASE_NAME}:${LVAR_IMAGE_GFJ_BASE_VER}' not found"
	exit 1
}

LVAR_IMAGE_GFJ_BASE_CUSTOM_NAME="lib-ts_ffmpeg-gfj_base-cb_javacpp_ffmpeg-${LVAR_DEBIAN_DIST}"
LVAR_IMAGE_GFJ_BASE_CUSTOM_VER="g${LVAR_GRAAL_VERSION_MAJOR}_f${LVAR_FFMPEG_VERSION}_j${LVAR_JAVACPP_VERSION}"
[ "${LOPT_DO_RKMPP}" = "true" ] && LVAR_IMAGE_GFJ_BASE_CUSTOM_VER+="_rkmpp"

echo -e "\n${VAR_MYNAME}: ---------------------------------------------------------------------------"
echo -e "${VAR_MYNAME}: building base image '${LVAR_IMAGE_GFJ_BASE_CUSTOM_NAME}:${LVAR_IMAGE_GFJ_BASE_CUSTOM_VER}'...\n"
docker build \
	-t "${LVAR_IMAGE_GFJ_BASE_CUSTOM_NAME}":"${LVAR_IMAGE_GFJ_BASE_CUSTOM_VER}" \
	--build-arg CF_BASE_IMAGE="${LVAR_IMAGE_GFJ_BASE_NAME}:${LVAR_IMAGE_GFJ_BASE_VER}" \
	--build-arg CF_GRAAL_ARCH="${LVAR_GRAAL_ARCH}" \
	--build-arg CF_GRAAL_VERSION_MAJOR="${LVAR_GRAAL_VERSION_MAJOR}" \
	--build-arg CF_FFMPEG_MAJMIN_VERSION="${LVAR_FFMPEG_MAJMIN_VERSION}" \
	--build-arg CF_FFMPEG_VERSION="${LVAR_FFMPEG_VERSION}" \
	--build-arg CF_JAVACPP_VERSION="${LVAR_JAVACPP_VERSION}" \
	--build-arg CF_JAVACPP_ARCH="${LVAR_JAVACPP_ARCH}" \
	--build-arg CF_WITH_RKMPP="${LOPT_DO_RKMPP}" \
	--build-arg CF_WITH_ROCKCHIP_FORK="${LOPT_DO_WITH_ROCKCHIP_FORK}" \
	.
