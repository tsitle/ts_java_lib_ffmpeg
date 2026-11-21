ARG CF_BASE_IMAGE=none

FROM ${CF_BASE_IMAGE} AS graalvm-ffmpeg-javacpp-base

# ----------------------------------------------------
# Prepare directory for JReleaser config file
# ----------------------------------------------------

RUN \
	mkdir /root/.jreleaser

# ----------------------------------------------------
# Compile the Java library
# ----------------------------------------------------

ARG CF_GRAAL_ARCH=-unknown-
ARG CF_TS_LIB_XRTXP_GIT_BRANCH_OR_COMMIT=-none-
ARG CF_TS_LIB_FFMPEG_GIT_BRANCH_OR_COMMIT=-none-
ARG CF_PUBLISH_TO_MAVEN=false

ENV GRADLE_OPTS="--enable-native-access=ALL-UNNAMED -Dorg.gradle.daemon=false"

# Clone the 'TS Lib XRTXP' repository and build the library
RUN \
	if [ -n "${CF_TS_LIB_XRTXP_GIT_BRANCH_OR_COMMIT}" ]; then \
		mkdir libs \
		&& cd libs \
		&& git clone https://github.com/tsitle/ts_java_lib_xrtxp.git lib-rtsp_xrtxp \
		&& cd lib-rtsp_xrtxp \
		&& git checkout "${CF_TS_LIB_XRTXP_GIT_BRANCH_OR_COMMIT}" \
		&& ./gradlew jar --no-daemon \
		|| exit 1; \
	else echo -n; fi

# Clone the 'TS Lib FFmpeg' repository
RUN \
	test -d libs || mkdir libs \
	&& cd libs \
	&& git clone https://github.com/tsitle/ts_java_lib_ffmpeg.git lib-ts_ffmpeg \
	&& cd lib-ts_ffmpeg \
	&& git checkout "${CF_TS_LIB_FFMPEG_GIT_BRANCH_OR_COMMIT}"

ARG CF_GRADLE_EXTRA_ARGS=""

# Build the library
RUN --mount=type=secret,id=jreleaser,target=/root/.jreleaser/config.toml \
	cd libs/lib-ts_ffmpeg \
	|| exit 1; \
	if [ "${CF_PUBLISH_TO_MAVEN}" = "true" ]; then \
		./gradlew publish --no-daemon ${CF_GRADLE_EXTRA_ARGS} \
		|| exit 1; \
	else \
		./gradlew shadowJar --no-daemon ${CF_GRADLE_EXTRA_ARGS} \
		|| exit 1; \
	fi

# Run one Unit Test
RUN \
	apt update \
	&& apt install --no-install-recommends -y \
		libva-drm2 \
	|| exit 1; \
	# \
	cd libs/lib-ts_ffmpeg \
	|| exit 1; \
	#./gradlew :test --tests "org.example.encoder_info.EncoderListPrinterTest.showEncoders" --no-daemon ${CF_GRADLE_EXTRA_ARGS}; \
	./gradlew :test --tests "io.github.tsitle.lib_ffmpeg.FfSharedLibInfoTest" --no-daemon ${CF_GRADLE_EXTRA_ARGS}

# Check if everything is ready for publishing
RUN --mount=type=secret,id=jreleaser,target=/root/.jreleaser/config.toml \
	if [ "${CF_PUBLISH_TO_MAVEN}" = "true" ]; then \
		cd libs/lib-ts_ffmpeg \
		&& ./gradlew jreleaserCheck --no-daemon ${CF_GRADLE_EXTRA_ARGS} \
		|| exit 1; \
	else echo -n; fi

# Actually publish to Maven Central Repository
RUN --mount=type=secret,id=jreleaser,target=/root/.jreleaser/config.toml \
	if [ "${CF_PUBLISH_TO_MAVEN}" = "true" ]; then \
		cd libs/lib-ts_ffmpeg \
		|| exit 1; \
		# the build fails regularly - even though the library has successfully been published. \
		# we therefore ignore the process failing here \
		./gradlew --info jreleaserFullRelease --no-daemon ${CF_GRADLE_EXTRA_ARGS} \
		|| echo -n; \
	else echo -n; fi

# Publish to local Maven Repository
RUN \
	cd libs/lib-ts_ffmpeg \
	&& ./gradlew publishToMavenLocal --no-daemon ${CF_GRADLE_EXTRA_ARGS}


# ----------------------------------------------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------------------------------------------
# ---- Runtime stage ----
# We use an slightly outdated Debian version here to have some backward compatibility.
# The current Debian's image uses Glibc 2.38, which is not compatible e.g. with Ubuntu 22.04 (which allows for <= 2.35)
FROM debian:12

#
RUN \
	apt update \
	&& apt install --no-install-recommends -y \
		sudo

# ----------------------------------------------------
# Copy only the compiled FAT JAR (shadow JAR)
# ----------------------------------------------------

ENV OUTPUT_ROOT_PATH=/build/lib

RUN \
	mkdir -p "${OUTPUT_ROOT_PATH}"

ENV TMP_BUILD_DIR_LIB="/build/libs/lib-ts_ffmpeg"

COPY --from=graalvm-ffmpeg-javacpp-base "${TMP_BUILD_DIR_LIB}/build/libs/*-full*.jar" "${OUTPUT_ROOT_PATH}/"

# ----------------------------------------------------
# Copy the local Maven repository directory for the custom-built version of JavaCPP and FFmpeg
# ----------------------------------------------------

RUN \
	mkdir -p /root/.m2/repository/org

COPY --from=graalvm-ffmpeg-javacpp-base /root/.m2/repository/org/bytedeco /root/.m2/repository/org/bytedeco

# ----------------------------------------------------
# Copy the local Maven repository directory for the freshly built version of TsLibFfmpeg
# ----------------------------------------------------

RUN \
	mkdir -p /root/.m2/repository/io/github

COPY --from=graalvm-ffmpeg-javacpp-base /root/.m2/repository/io/github/tsitle /root/.m2/repository/io/github/tsitle

# ----------------------------------------------------
# Copy the custom-built Rockchip Media Process Platform (MPP) library and the librga
# ----------------------------------------------------

ARG CF_GRAAL_ARCH=-unknown-
ARG CF_WITH_RKMPP=false

ENV COPIED_LIBS_ROOT_PATH=/build/custom_ffmpeg_and_javacpp_ready_for_deployment

COPY --from=graalvm-ffmpeg-javacpp-base "${COPIED_LIBS_ROOT_PATH}/libs/rkmpp-${CF_GRAAL_ARCH}/librockchip_mpp.so.0" "${OUTPUT_ROOT_PATH}/"
COPY --from=graalvm-ffmpeg-javacpp-base "${COPIED_LIBS_ROOT_PATH}/libs/rkmpp-${CF_GRAAL_ARCH}/librockchip_vpu.so.0" "${OUTPUT_ROOT_PATH}/"
COPY --from=graalvm-ffmpeg-javacpp-base "${COPIED_LIBS_ROOT_PATH}/libs/rkmpp-${CF_GRAAL_ARCH}/librga.so.2.1.0" "${OUTPUT_ROOT_PATH}/"

RUN \
	if [ "${CF_GRAAL_ARCH}" = "aarch64" ] && [ "${CF_WITH_RKMPP}" = "true" ]; then \
		ln -s "librockchip_mpp.so.0" "${OUTPUT_ROOT_PATH}/librockchip_mpp.so.1" \
		&& ln -s "librockchip_mpp.so.1" "${OUTPUT_ROOT_PATH}/librockchip_mpp.so" \
		&& ln -s "librockchip_vpu.so.0" "${OUTPUT_ROOT_PATH}/librockchip_vpu.so.1" \
		&& ln -s "librockchip_vpu.so.1" "${OUTPUT_ROOT_PATH}/librockchip_vpu.so" \
		&& ln -s "librga.so.2.1.0" "${OUTPUT_ROOT_PATH}/librga.so.2" \
		&& ln -s "librga.so.2" "${OUTPUT_ROOT_PATH}/librga.so" \
		|| exit 1; \
		ln -s "${OUTPUT_ROOT_PATH}/librockchip_mpp.so" /usr/local/lib/ \
		&& ln -s "${OUTPUT_ROOT_PATH}/librockchip_mpp.so.0" /usr/local/lib/ \
		&& ln -s "${OUTPUT_ROOT_PATH}/librockchip_mpp.so.1" /usr/local/lib/ \
		&& ln -s "${OUTPUT_ROOT_PATH}/librockchip_vpu.so" /usr/local/lib/ \
		&& ln -s "${OUTPUT_ROOT_PATH}/librockchip_vpu.so.0" /usr/local/lib/ \
		&& ln -s "${OUTPUT_ROOT_PATH}/librockchip_vpu.so.1" /usr/local/lib/ \
		&& ln -s "${OUTPUT_ROOT_PATH}/librga.so" /usr/local/lib/ \
		&& ln -s "${OUTPUT_ROOT_PATH}/librga.so.2" /usr/local/lib/ \
		&& ln -s "${OUTPUT_ROOT_PATH}/librga.so.2.1.0" /usr/local/lib/ \
		&& ldconfig \
		|| exit 1; \
	else \
		rm "${OUTPUT_ROOT_PATH}/librockchip_mpp.so.0" \
			"${OUTPUT_ROOT_PATH}/librockchip_vpu.so.0" \
			"${OUTPUT_ROOT_PATH}/librga.so.2.1.0" \
		|| echo -n; \
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

# ----------------------------------------------------

WORKDIR "${OUTPUT_ROOT_PATH}"

# ----------------------------------------------------

ENTRYPOINT ["/bin/bash"]
