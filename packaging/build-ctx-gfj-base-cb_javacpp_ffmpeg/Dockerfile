ARG CF_BASE_IMAGE=none

FROM ${CF_BASE_IMAGE} AS graalvm-base

ARG CF_GRAAL_ARCH=-unknown-
ARG CF_WITH_RKMPP=false
ARG CF_WITH_ROCKCHIP_FORK=false

# ----------------------------------------------------

# Install other dependencies
RUN \
	apt update \
	&& apt install --no-install-recommends -y \
		build-essential \
		yasm \
		nasm \
		pkgconf \
		autoconf \
		automake \
		libtool \
		git \
		tar \
		xz-utils \
		maven \
		zlib1g-dev

# ----------------------------------------------------
# Build Rockchip's Media Process Platform (MPP) library
# ----------------------------------------------------

RUN \
	if [ "${CF_GRAAL_ARCH}" = "aarch64" ]; then \
		apt install --no-install-recommends -y \
			meson \
			cmake \
			pkg-config \
			libdrm-dev \
		|| exit 1; \
	else echo -n; fi

RUN \
	if [ "${CF_GRAAL_ARCH}" = "aarch64" ] && [ "${CF_WITH_ROCKCHIP_FORK}" = "true" ]; then \
		git clone -b jellyfin-mpp --depth=1 https://github.com/nyanmisaka/mpp.git rkmpp \
		|| exit 1; \
	elif [ "${CF_GRAAL_ARCH}" = "aarch64" ] && [ "${CF_WITH_RKMPP}" = "true" ]; then \
		git clone --depth=1 https://github.com/rockchip-linux/mpp.git rkmpp \
		|| exit 1; \
	else echo -n; fi; \
	# note: when CF_WITH_ROCKCHIP_FORK is true then CF_WITH_RKMPP is also true \
	if [ "${CF_GRAAL_ARCH}" = "aarch64" ] && [ "${CF_WITH_RKMPP}" = "true" ]; then \
		cd rkmpp \
		&& mkdir rkmpp_build \
		&& cd rkmpp_build \
		&& cmake \
			-DCMAKE_INSTALL_PREFIX=/usr \
			-DCMAKE_BUILD_TYPE=Release \
			-DBUILD_SHARED_LIBS=ON \
			-DBUILD_TEST=OFF \
			-DHAVE_DRM=ON \
			-DRKPLATFORM=ON \
			.. \
		&& make -j$(nproc) \
		&& make install \
		|| exit 1; \
	else echo -n; fi

RUN \
	if [ "${CF_GRAAL_ARCH}" = "aarch64" ] && [ "${CF_WITH_ROCKCHIP_FORK}" = "true" ]; then \
		git clone -b jellyfin-rga --depth=1 https://github.com/nyanmisaka/rk-mirrors.git rkrga \
		&& meson setup rkrga rkrga_build \
			--prefix=/usr \
			--libdir=lib \
			--buildtype=release \
			--default-library=shared \
			-Dcpp_args=-fpermissive \
			-Dlibdrm=false \
			-Dlibrga_demo=false \
		&& meson configure rkrga_build \
		&& ninja -C rkrga_build install \
		|| exit 1; \
	elif [ "${CF_GRAAL_ARCH}" = "aarch64" ] && [ "${CF_WITH_RKMPP}" = "true" ]; then \
		git clone --depth=1 https://github.com/airockchip/librga.git \
		# \
		&& install -m 0755 librga/libs/Linux/gcc-aarch64/librga.so /usr/lib/librga.so \
		&& install -m 0644 librga/libs/Linux/gcc-aarch64/librga.a  /usr/lib/librga.a \
		&& install -d /usr/include/rga \
		&& install -m 0644 librga/include/*.h /usr/include/rga/ \
		|| exit 1; \
		# \
		# ffmpeg's configure relies on pkg-config, so add a librga.pc: \
		{ \
			echo 'prefix=/usr'; \
			echo 'exec_prefix=${prefix}'; \
			echo 'libdir=${exec_prefix}/lib'; \
			echo 'includedir=${prefix}/include'; \
			echo ''; \
			echo 'Name: librga'; \
			echo 'Description: Userspace interface to Rockchip RGA 2D accelerator'; \
			echo 'Version: 2.2.0'; \
			echo 'Libs: -L${libdir} -lrga'; \
			echo 'Cflags: -I${includedir}/rga'; \
		} > /usr/lib/pkgconfig/librga.pc; \
		ldconfig \
		|| exit 1; \
	else echo -n; fi

# ----------------------------------------------------
# Build FFmpeg libraries
# ----------------------------------------------------

ARG CF_FFMPEG_VERSION=-unknown-
ARG CF_FFMPEG_MAJMIN_VERSION=-unknown-

#ADD "files/ffmpeg-${CF_FFMPEG_VERSION}.tar.xz" /build/

RUN \
	if [ "${CF_GRAAL_ARCH}" = "aarch64" ] && [ "${CF_WITH_ROCKCHIP_FORK}" = "true" ]; then \
		git clone --branch ${CF_FFMPEG_MAJMIN_VERSION} --single-branch --depth=1 \
			https://github.com/nyanmisaka/ffmpeg-rockchip.git ffmpeg-${CF_FFMPEG_VERSION} \
		|| exit 1; \
	else \
		curl -LO "https://ffmpeg.org/releases/ffmpeg-${CF_FFMPEG_VERSION}.tar.xz" \
		&& tar -xf "ffmpeg-${CF_FFMPEG_VERSION}.tar.xz" \
		&& rm "ffmpeg-${CF_FFMPEG_VERSION}.tar.xz" \
		|| exit 1; \
	fi

RUN \
	apt install --no-install-recommends -y \
		libdrm-dev \
		libaom-dev \
		libfreetype-dev \
		libharfbuzz-dev \
		libmp3lame-dev \
		libopenh264-dev \
		libopus-dev \
		libpulse-dev \
		libspeex-dev \
		libsvtav1enc-dev \
		libtheora-dev \
		libvorbis-dev \
		libvpx-dev \
		libzimg-dev \
		libssl-dev

RUN \
	if [ "${CF_GRAAL_ARCH}" = "x64" ]; then \
		# compile the FFmpeg NVIDIA headers ("ffnvcodec"): \
		git clone --depth=1 https://git.videolan.org/git/ffmpeg/nv-codec-headers.git \
		&& cd nv-codec-headers \
		&& make \
		&& make install \
		|| exit 1; \
	else echo -n; fi;

RUN \
	TMP_ARG_FF=""; \
	if [ "${CF_GRAAL_ARCH}" = "aarch64" ] && [ "${CF_WITH_RKMPP}" = "true" ]; then \
		TMP_ARG_FF="--enable-rkmpp"; \
		if [ "${CF_WITH_ROCKCHIP_FORK}" = "true" ]; then \
			# note: this option is currently only available when using the repository 'ffmpeg-rockchip': \
			TMP_ARG_FF="${TMP_ARG_FF} --enable-rkrga"; \
		else echo -n; fi; \
	elif [ "${CF_GRAAL_ARCH}" = "x64" ]; then \
		# Vulkan support requires building the Vulkan SDK first - this cannot be resolved by a simple 'apt install libvulkan-dev' \
		TMP_ARG_FF="--disable-vulkan --disable-hwaccel=h264_vulkan --disable-hwaccel=hevc_vulkan --disable-hwaccel=av1_vulkan"; \
		# \
		TMP_ARG_FF="${TMP_ARG_FF} --enable-cuda --enable-cuvid --enable-nvenc --enable-nvdec"; \
	fi; \
	#TMP_ARG_FF="${TMP_ARG_FF} --disable-programs"; \
	cd ffmpeg-${CF_FFMPEG_VERSION} \
	#&& ./configure --help \
	&& ./configure \
		--prefix=/opt/ffmpeg \
		--enable-shared \
		# upgrade (L)GPL to version 3 \
		--enable-version3 \
		--enable-runtime-cpudetect \
		--disable-iconv --disable-opencl --disable-sdl2 --disable-bzlib --disable-lzma --disable-linux-perf --disable-xlib \
		--disable-doc --disable-debug \
		--enable-openssl --enable-zlib --enable-libdrm --enable-pthreads \
		# needed for dash and imf demuxing support \
		--disable-libxml2 \
		--disable-libwebp \
		# Secure Reliable Transport (SRT) is an open-source video transport protocol \
		--disable-libsrt \
		# needed for drawtext filter \
		--enable-libfreetype --enable-libharfbuzz \
		# enable z.lib, needed for zscale filter \
		--enable-libzimg \
		# enable Pulseaudio input via libpulse \
		--enable-libpulse \
		--enable-libtheora --enable-libvorbis \
		--enable-libmp3lame --enable-libspeex --enable-libopus \
		--enable-libopenh264 --enable-libvpx --enable-libsvtav1 \
		# enable AV1 video encoding/decoding via libaom \
		--enable-libaom \
		${TMP_ARG_FF} \
	#&& exit 1 \
	&& make -j$(getconf _NPROCESSORS_ONLN) \
	&& make install

# ----------------------------------------------------
# Build JavaCPP
# ----------------------------------------------------

ARG CF_JAVACPP_VERSION=-unknown-
ARG CF_JAVACPP_ARCH=-unknown-

#ADD "files/javacpp-presets-source-${CF_JAVACPP_VERSION}.tgz" /build/
#RUN \
#	cd javacpp-presets \
#	&& git config --global --add safe.directory /build/javacpp-presets
#	&& git checkout -b customtemp "${CF_JAVACPP_VERSION}"

RUN \
	git init javacpp-presets \
	&& cd javacpp-presets \
	&& git remote add origin https://github.com/bytedeco/javacpp-presets.git \
	&& git fetch --depth=1 origin "${CF_JAVACPP_VERSION}" \
	&& git checkout FETCH_HEAD

RUN \
	TMP_FF_JCPP_VERSION_SUFFIX="tsitle"; \
	if [ "${CF_GRAAL_ARCH}" = "aarch64" ] && [ "${CF_WITH_ROCKCHIP_FORK}" = "true" ]; then \
		TMP_FF_JCPP_VERSION_SUFFIX="${TMP_FF_JCPP_VERSION_SUFFIX}-rf"; \
	fi; \
	if [ "${CF_GRAAL_ARCH}" = "aarch64" ] && [ "${CF_WITH_RKMPP}" = "true" ]; then \
		TMP_FF_JCPP_VERSION_SUFFIX="${TMP_FF_JCPP_VERSION_SUFFIX}-rkmpp"; \
	fi; \
	cd javacpp-presets/ffmpeg \
	&& mkdir -p "cppbuild/linux-${CF_JAVACPP_ARCH}" \
	&& ln -s /opt/ffmpeg/include "cppbuild/linux-${CF_JAVACPP_ARCH}/include" \
	&& ln -s /opt/ffmpeg/lib "cppbuild/linux-${CF_JAVACPP_ARCH}/lib" \
	&& mvn versions:set \
		-DnewVersion="${CF_FFMPEG_VERSION}-${CF_JAVACPP_VERSION}-${TMP_FF_JCPP_VERSION_SUFFIX}" \
		-DgenerateBackupPoms=false \
	&& cd platform \
	&& mvn versions:set \
		-DnewVersion="${CF_FFMPEG_VERSION}-${CF_JAVACPP_VERSION}-${TMP_FF_JCPP_VERSION_SUFFIX}" \
		-DgenerateBackupPoms=false

RUN \
	cd javacpp-presets \
	&& mvn install \
		--projects ffmpeg \
		-Djavacpp.cppbuild.skip=true \
		-Djavacpp.platform=linux-${CF_JAVACPP_ARCH}

RUN \
	ln -s "javacpp-${CF_JAVACPP_VERSION}.jar" \
		"/root/.m2/repository/org/bytedeco/javacpp/${CF_JAVACPP_VERSION}/javacpp-${CF_JAVACPP_VERSION}-linux-${CF_JAVACPP_ARCH}.jar" \
	&& ln -s "javacpp-${CF_JAVACPP_VERSION}.jar.sha1" \
		"/root/.m2/repository/org/bytedeco/javacpp/${CF_JAVACPP_VERSION}/javacpp-${CF_JAVACPP_VERSION}-linux-${CF_JAVACPP_ARCH}.jar.sha1" \
	&& ln -s "javacpp-${CF_JAVACPP_VERSION}.pom" \
		"/root/.m2/repository/org/bytedeco/javacpp/${CF_JAVACPP_VERSION}/javacpp-${CF_JAVACPP_VERSION}-linux-${CF_JAVACPP_ARCH}.pom" \
	&& ln -s "javacpp-${CF_JAVACPP_VERSION}.pom.sha1" \
		"/root/.m2/repository/org/bytedeco/javacpp/${CF_JAVACPP_VERSION}/javacpp-${CF_JAVACPP_VERSION}-linux-${CF_JAVACPP_ARCH}.pom.sha1"

# ----------------------------------------------------
# Create symlinks for FFmpeg and JavaCPP shared libraries
# ----------------------------------------------------

# Copy custom JavaCPP build to /opt/javacpp-ffmpeg/
RUN \
	cp -r "javacpp-presets/ffmpeg/target/native/org/bytedeco/ffmpeg/linux-${CF_JAVACPP_ARCH}" /opt/javacpp-ffmpeg

# Create symlinks to custom FFmpeg and JavaCPP shared libraries
RUN \
	TMP_TARGET_DIR="/usr/local/lib" \
	&& for FN in /opt/ffmpeg/lib/*; do \
		test -f "${FN}" -o -h "${FN}" || continue; \
		test -f "${TMP_TARGET_DIR}/$(basename "${FN}")" || \
			ln -s "${FN}" "${TMP_TARGET_DIR}/"; \
		done \
	&& for FN in /opt/javacpp-ffmpeg/*; do \
		test -f "${FN}" -o -h "${FN}" || continue; \
		test -f "${TMP_TARGET_DIR}/$(basename "${FN}")" || \
			ln -s "${FN}" "${TMP_TARGET_DIR}/"; \
		done \
	&& ldconfig

# Create symlinks to custom FFmpeg binaries
RUN \
	TMP_TARGET_DIR="/usr/local/bin" \
	&& for FN in /opt/ffmpeg/bin/*; do \
		ln -s "${FN}" "${TMP_TARGET_DIR}/"; \
		done

# ----------------------------------------------------
# Additionally copy the custom-built FFmpeg and JavaCPP shared libraries to a common base directory.
# This is not required for using the libraries though.
# ----------------------------------------------------

ENV COPIED_LIBS_ROOT_PATH=/build/custom_ffmpeg_and_javacpp_ready_for_deployment

ENV TMP_BUILD_DIR_FFMPEG="/opt/ffmpeg"
ENV TMP_BUILD_DIR_JAVACPP="/build/javacpp-presets/ffmpeg/target/native/org/bytedeco/ffmpeg/linux-${CF_JAVACPP_ARCH}"

#
RUN \
	mkdir -p "${COPIED_LIBS_ROOT_PATH}/libs" \
	&& mkdir "${COPIED_LIBS_ROOT_PATH}/bin" \
	&& cp "${TMP_BUILD_DIR_FFMPEG}/bin/"* "${COPIED_LIBS_ROOT_PATH}/bin/" \
	&& cp -r "${TMP_BUILD_DIR_FFMPEG}/lib/" "${COPIED_LIBS_ROOT_PATH}/libs/ffmpeg/" \
	&& cp -r "${TMP_BUILD_DIR_JAVACPP}/" "${COPIED_LIBS_ROOT_PATH}/libs/javacpp-ffmpeg/" \
	# \
	&& TMP_TARGET_DIR="${COPIED_LIBS_ROOT_PATH}/bin" \
	# symlinks for FFmpeg libs \
	&& rm -r "${COPIED_LIBS_ROOT_PATH}/libs/ffmpeg/pkgconfig" \
	&& for FN in "${COPIED_LIBS_ROOT_PATH}/libs/ffmpeg"/*; do \
		test -f "${FN}" -o -h "${FN}" || continue; \
		test -f "${TMP_TARGET_DIR}/$(basename "${FN}")" || \
			ln -s "../libs/ffmpeg/$(basename "${FN}")" "${TMP_TARGET_DIR}/"; \
		done \
	# delete copies of FFmpeg libs in JavaCPP directory \
	&& for FN in "${COPIED_LIBS_ROOT_PATH}/libs/javacpp-ffmpeg"/*; do \
		test -f "${FN}" -o -h "${FN}" || continue; \
		test -f "${COPIED_LIBS_ROOT_PATH}/libs/ffmpeg/$(basename "${FN}")" && rm "${FN}" || echo -y; \
		done \
	# symlinks for JavaCPP libs \
	&& for FN in "${COPIED_LIBS_ROOT_PATH}/libs/javacpp-ffmpeg"/*; do \
		test -f "${FN}" -o -h "${FN}" || continue; \
		test -f "${TMP_TARGET_DIR}/$(basename "${FN}")" || \
			ln -s "../libs/javacpp-ffmpeg/$(basename "${FN}")" "${TMP_TARGET_DIR}/"; \
		done

# ----------------------------------------------------
# Copy the custom-built Rockchip Media Process Platform (MPP) library and the librga to a common base directory
# ----------------------------------------------------

RUN \
	test -d "${COPIED_LIBS_ROOT_PATH}/libs/rkmpp-${CF_GRAAL_ARCH}" || \
		mkdir "${COPIED_LIBS_ROOT_PATH}/libs/rkmpp-${CF_GRAAL_ARCH}"; \
	test -d "${COPIED_LIBS_ROOT_PATH}/libs/rkmpp-${CF_GRAAL_ARCH}" || exit 1; \
	TMP_RKMPP_OUT="${COPIED_LIBS_ROOT_PATH}/libs/rkmpp-${CF_GRAAL_ARCH}"; \
	if [ "${CF_GRAAL_ARCH}" = "aarch64" ] && [ "${CF_WITH_RKMPP}" = "true" ]; then \
		cp "/usr/lib/aarch64-linux-gnu/librockchip_mpp.so.0" "${TMP_RKMPP_OUT}/" \
		&& cp "/usr/lib/aarch64-linux-gnu/librockchip_vpu.so.0" "${TMP_RKMPP_OUT}/" \
		&& cp "/usr/lib/librga.so.2.1.0" "${TMP_RKMPP_OUT}/" \
		|| exit 1; \
	else \
		touch "${TMP_RKMPP_OUT}/librockchip_mpp.so.0" \
		&& touch "${TMP_RKMPP_OUT}/librockchip_vpu.so.0" \
		&& touch "${TMP_RKMPP_OUT}/librga.so.2.1.0" \
		|| exit 1; \
	fi

# ----------------------------------------------------

RUN \
	rm -rf \
		/usr/share/man \
		/usr/share/doc \
		/usr/share/icons \
		/usr/share/poppler \
		/usr/share/mime \
		/var/lib/apt/lists/*
