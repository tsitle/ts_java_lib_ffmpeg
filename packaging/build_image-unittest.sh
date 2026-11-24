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
	echo "Usage: ${VAR_MYNAME} [cb_javacpp_ffmpeg [rkmpp]]"
	echo "Examples: ${VAR_MYNAME}"
	echo "          ${VAR_MYNAME} cb_javacpp_ffmpeg"
	echo "          ${VAR_MYNAME} cb_javacpp_ffmpeg rkmpp"
	echo ""
	echo "Options:"
	echo "  'cb_javacpp_ffmpeg': Use custom-built JavaCPP and FFmpeg version"
	echo "  'rkmpp'            : Build with ARM64 RKMPP (Rockchip hardware accelerated video en-/decoders) support"
	#echo "  'rf'               : Build with FFmpeg-Rockchip fork (for better RKMPP support)"
}

if [ $# -gt 2 ]; then
	echo -e "${VAR_MYNAME}: invalid number of arguments\n" >/dev/stderr
	printUsage >/dev/stderr
	exit 1
fi

LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG=false
LOPT_DO_RKMPP=false
LOPT_DO_WITH_ROCKCHIP_FORK=false

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
	#
	echo "${VAR_MYNAME}: invalid arg '${1}'" >/dev/stderr
	exit 1
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
# - Test image ---------------------------------------------
# ----------------------------------------------------------

cd build-ctx-unittest || exit 1

LVAR_DEBIAN_DIST="$(_getCpuArch debian_dist)"

LVAR_GRAAL_ARCH="$(_getCpuArch graal)"
LVAR_GRAAL_VERSION_MAJOR="25"

# ----------------------------------------------------------

TMP_GRAALVM_OUTP_FN="graalvm-jdk-${LVAR_GRAAL_VERSION_MAJOR}_linux-${LVAR_GRAAL_ARCH}_bin.tar.gz"
if [ ! -f "files/${TMP_GRAALVM_OUTP_FN}" ]; then
	echo -e "\n${VAR_MYNAME}: ---------------------------------------------------------------------------"
	echo -e "${VAR_MYNAME}: downloading GraalVM JDK...\n"
	curl -LO "https://download.oracle.com/graalvm/${LVAR_GRAAL_VERSION_MAJOR}/latest/${TMP_GRAALVM_OUTP_FN}" \
		--output-dir "files/" || exit 1
fi

# ----------------------------------------------------------

LVAR_EXTRA_IMAGE_NAME_QUALIFIER=""
[ "${LOPT_DO_CUSTOM_BUILD_JAVACPP_FFMPEG}" = "true" ] && LVAR_EXTRA_IMAGE_NAME_QUALIFIER+="-cb_javacpp_ffmpeg"
[ "${LOPT_DO_RKMPP}" = "true" ] && LVAR_EXTRA_IMAGE_NAME_QUALIFIER+="-rkmpp"

LVAR_IMAGE_BASE_NAME="lib-ts_ffmpeg${LVAR_EXTRA_IMAGE_NAME_QUALIFIER}-${LVAR_DEBIAN_DIST}"
LVAR_IMAGE_BASE_VER="latest"

LCFG_IMAGE_NAME="lib-ts_ffmpeg${LVAR_EXTRA_IMAGE_NAME_QUALIFIER}-test-${LVAR_DEBIAN_DIST}"

echo -e "\n${VAR_MYNAME}: ---------------------------------------------------------------------------"
echo -e "${VAR_MYNAME}: building test image '${LCFG_IMAGE_NAME}'...\n"
docker build \
	-t "${LCFG_IMAGE_NAME}":"latest" \
	--build-arg CF_BASE_IMAGE="${LVAR_IMAGE_BASE_NAME}:${LVAR_IMAGE_BASE_VER}" \
	--build-arg CF_GRAAL_ARCH="${LVAR_GRAAL_ARCH}" \
	--build-arg CF_GRAAL_VERSION_MAJOR="${LVAR_GRAAL_VERSION_MAJOR}" \
	.
