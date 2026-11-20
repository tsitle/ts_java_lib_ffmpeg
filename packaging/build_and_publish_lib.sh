#!/usr/bin/env bash

#
# by TS, Oct 2026
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
	echo "Usage: ${VAR_MYNAME} <LIB_VERSION> <COMMIT_OR_BRANCH> [lib_xrtxp:<COMMIT_OR_BRANCH>] [cb_javacpp_ffmpeg [rkmpp]] [pub]"
	echo "Examples: ${VAR_MYNAME} 1.0-SNAPSHOT testing"
	echo "          ${VAR_MYNAME} 1.1.4 ce3991c3"
	echo "          ${VAR_MYNAME} 1.1.4 ce3991c3 lib_xrtxp:a1c2e3g4"
	echo "          ${VAR_MYNAME} 1.1.4 ce3991c3 pub"
	echo ""
	echo "Options:"
	echo "  'lib_xrtxp:<COMMIT_OR_BRANCH>': build ts_lib_xrtxp from source code (instead of using Maven Central Repository version)"
	echo "  'cb_javacpp_ffmpeg'           : Use custom-built JavaCPP and FFmpeg version"
	echo "  'rkmpp'                       : Build with ARM64 RKMPP (Rockchip hardware accelerated video en-/decoders) support"
	#echo "  'rf'                          : Build with FFmpeg-Rockchip fork (for better RKMPP support)"
	echo "  'pub'                         : Publish to Maven Central Repository"
}

if [ $# -gt 0 ] && [ "${1}" = "--help" ]; then
	printUsage
	exit 0
fi
if [ $# -lt 2 ] || [ $# -gt 6 ]; then
	echo -e "${VAR_MYNAME}: invalid number of arguments\n" >/dev/stderr
	printUsage >/dev/stderr
	exit 1
fi
if [ -z "${1}" ]; then
	echo "${VAR_MYNAME}: empty <LIB_VERSION>" >/dev/stderr
	exit 1
fi
if [ -z "${2}" ]; then
	echo "${VAR_MYNAME}: empty <COMMIT_OR_BRANCH>" >/dev/stderr
	exit 1
fi

LOPT_LIB_VERSION="${1}"
shift
LOPT_LIB_FFMPEG_COMMIT_OR_BRANCH="${1}"
shift
LOPT_LIB_XRTXP_COMMIT_OR_BRANCH=""
LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG=false
LOPT_DO_RKMPP=false
LOPT_DO_WITH_ROCKCHIP_FORK=false
LOPT_DO_PUBLISH=false

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
		LOPT_DO_WITH_ROCKCHIP_FORK=true  # enabled by default
		shift
		continue
	fi
	#if [ "${1}" = "rf" ]; then
		#LOPT_DO_WITH_ROCKCHIP_FORK=true
		#shift
		#continue
	#fi
	if [ "${1}" = "pub" ]; then
		LOPT_DO_PUBLISH=true
		shift
		continue
	fi
	#
	if ! echo -n "${1}" | grep -q ":"; then
		echo "${VAR_MYNAME}: invalid arg '${1}'" >/dev/stderr
		exit 1
	fi
	TMP_ARG_K="$(echo -n "${1}" | cut -f1 -d:)"
	TMP_ARG_V="$(echo -n "${1}" | cut -f2 -d:)"
	if [ "${TMP_ARG_K}" = "lib_xrtxp" ]; then
		LOPT_LIB_XRTXP_COMMIT_OR_BRANCH="${TMP_ARG_V}"
	else
		echo "${VAR_MYNAME}: invalid arg '${1}'" >/dev/stderr
		exit 1
	fi
	shift
done

if [ "${LOPT_DO_RKMPP}" = "true" ] && [ "${LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG}" != "true" ]; then
	echo "${VAR_MYNAME}: Option 'rkmpp' is only allowed in combination with 'cb_javacpp_ffmpeg'" >/dev/stderr
	exit 1
fi
if [ "${LOPT_DO_WITH_ROCKCHIP_FORK}" = "true" ] && [ "${LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG}" != "true" ]; then
	echo "${VAR_MYNAME}: Option 'rf' is only allowed in combination with 'cb_javacpp_ffmpeg'" >/dev/stderr
	exit 1
fi

if [ "${LOPT_DO_PUBLISH}" = "true" ] && [ "${LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG}" = "true" ]; then
	echo "${VAR_MYNAME}: Option 'pub' is only allowed without 'cb_javacpp_ffmpeg'" >/dev/stderr
	exit 1
fi
if [ "${LOPT_DO_PUBLISH}" = "true" ] && [ -n "${LOPT_LIB_XRTXP_COMMIT_OR_BRANCH}" ]; then
	echo "${VAR_MYNAME}: Option 'pub' is only allowed without 'lib_xrtxp:<COMMIT_OR_BRANCH>'" >/dev/stderr
	exit 1
fi

# ----------------------------------------------------------
# - GraalVM + FFmpeg + JavaCPP base image ------------------
# ----------------------------------------------------------

LVAR_DEBIAN_DIST="$(_getCpuArch debian_dist)"

if [ "${LOPT_DO_RKMPP}" = "true" ] && [ "${LVAR_DEBIAN_DIST}" != "arm64" ]; then
	echo "${VAR_MYNAME}: Option 'rkmpp' is only allowed for ARM64" >/dev/stderr
	exit 1
fi

LVAR_GRAAL_ARCH="$(_getCpuArch graal)"
LVAR_GRAAL_VERSION_MAJOR="25"
LVAR_FFMPEG_VERSION="8.1.2"
LVAR_JAVACPP_VERSION="1.5.14"
LVAR_JAVACPP_ARCH="$(_getCpuArch javacpp)"

LVAR_EXTRA_BASE_IMAGE_NAME_QUALIFIER=""
[ "${LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG}" = "true" ] && LVAR_EXTRA_BASE_IMAGE_NAME_QUALIFIER+="-cb_javacpp_ffmpeg"

LVAR_IMAGE_GFJ_BASE_NAME="lib-ts_ffmpeg-gfj_base${LVAR_EXTRA_BASE_IMAGE_NAME_QUALIFIER}-${LVAR_DEBIAN_DIST}"
LVAR_IMAGE_GFJ_BASE_VER="g${LVAR_GRAAL_VERSION_MAJOR}"
[ "${LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG}" = "true" ] && LVAR_IMAGE_GFJ_BASE_VER+="_f${LVAR_FFMPEG_VERSION}_j${LVAR_JAVACPP_VERSION}"
[ "${LOPT_DO_RKMPP}" = "true" ] && LVAR_IMAGE_GFJ_BASE_VER+="_rkmpp"

docker image inspect "${LVAR_IMAGE_GFJ_BASE_NAME}":"${LVAR_IMAGE_GFJ_BASE_VER}" >/dev/null 2>&1 || {
	echo "${VAR_MYNAME}: base image '${LVAR_IMAGE_GFJ_BASE_NAME}:${LVAR_IMAGE_GFJ_BASE_VER}' not found"
	exit 1
}

# ---------------------------------------------------------
# - Build and publish library to Maven Central Repository -
# ---------------------------------------------------------

cd build-ctx-lib || exit 1

LVAR_EXTRA_IMAGE_NAME_QUALIFIER=""
[ "${LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG}" = "true" ] && LVAR_EXTRA_IMAGE_NAME_QUALIFIER+="-cb_javacpp_ffmpeg"
[ "${LOPT_DO_RKMPP}" = "true" ] && LVAR_EXTRA_IMAGE_NAME_QUALIFIER+="-rkmpp"

LVAR_IMAGE_LIB_NAME="lib-ts_ffmpeg${LVAR_EXTRA_IMAGE_NAME_QUALIFIER}-${LVAR_DEBIAN_DIST}"
LVAR_IMAGE_LIB_VER="${LOPT_LIB_VERSION}"

docker image inspect "${LVAR_IMAGE_LIB_NAME}":"latest" >/dev/null 2>&1 && {
	docker image rm "${LVAR_IMAGE_LIB_NAME}:latest"
}
docker image inspect "${LVAR_IMAGE_LIB_NAME}":"${LVAR_IMAGE_LIB_VER}" >/dev/null 2>&1 && {
	docker image rm "${LVAR_IMAGE_LIB_NAME}":"${LVAR_IMAGE_LIB_VER}"
}

echo -e "\n${VAR_MYNAME}: ---------------------------------------------------------------------------"
TMP_LOG_MSG=""
TMP_JRELEASER_SECRETS=""
if [ "${LOPT_DO_PUBLISH}" = "true" ]; then
	TMP_LOG_MSG="and publishing"
	TMP_JRELEASER_SECRETS="SECRET"
else
	TMP_LOG_MSG="(but not publishing)"
	TMP_JRELEASER_SECRETS="DUMMY"
fi

TMP_GRADLE_EXTRA_ARGS=""
test -z "${LOPT_LIB_XRTXP_COMMIT_OR_BRANCH}" && TMP_GRADLE_EXTRA_ARGS+=" -PuseCustomLibFromMaven_xrtxp"
test "${LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG}" = "true" && TMP_GRADLE_EXTRA_ARGS+=" -PuseCustomBuiltJavaCppAndFfmpeg"
test "${LOPT_DO_WITH_ROCKCHIP_FORK}" = "true" && TMP_GRADLE_EXTRA_ARGS+=" -PwithRockchipFork"
test "${LOPT_DO_RKMPP}" = "true" && TMP_GRADLE_EXTRA_ARGS+=" -PwithRkmpp"

echo -e "${VAR_MYNAME}: building ${TMP_LOG_MSG} library (Docker image '${LVAR_IMAGE_LIB_NAME}:${LVAR_IMAGE_LIB_VER}')...\n"
docker build \
	-t "${LVAR_IMAGE_LIB_NAME}":"${LVAR_IMAGE_LIB_VER}" \
	--build-arg CF_BASE_IMAGE="${LVAR_IMAGE_GFJ_BASE_NAME}:${LVAR_IMAGE_GFJ_BASE_VER}" \
	--build-arg CF_GRAAL_ARCH="${LVAR_GRAAL_ARCH}" \
	--build-arg CF_TS_LIB_XRTXP_GIT_BRANCH_OR_COMMIT="${LOPT_LIB_XRTXP_COMMIT_OR_BRANCH}" \
	--build-arg CF_TS_LIB_FFMPEG_GIT_BRANCH_OR_COMMIT="${LOPT_LIB_FFMPEG_COMMIT_OR_BRANCH}" \
	--build-arg CF_JAVACPP_ARCH="${LVAR_JAVACPP_ARCH}" \
	--build-arg CF_WITH_RKMPP="${LOPT_DO_RKMPP}" \
	--build-arg CF_PUBLISH_TO_MAVEN="${LOPT_DO_PUBLISH}" \
	--build-arg CF_GRADLE_EXTRA_ARGS="${TMP_GRADLE_EXTRA_ARGS}" \
	--secret id=jreleaser,src="$(pwd)/files/config-jreleaser-${TMP_JRELEASER_SECRETS}.toml" \
	. \
	|| exit 1

docker tag "${LVAR_IMAGE_LIB_NAME}":"${LVAR_IMAGE_LIB_VER}" "${LVAR_IMAGE_LIB_NAME}":latest
