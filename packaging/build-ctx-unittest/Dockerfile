ARG CF_BASE_IMAGE=none

FROM ${CF_BASE_IMAGE} AS ts_ffmpeg

# Add GraalVM
ARG CF_GRAAL_ARCH=-unknown-
ARG CF_GRAAL_VERSION_MAJOR=-unknown-

ADD "files/graalvm-jdk-${CF_GRAAL_VERSION_MAJOR}_linux-${CF_GRAAL_ARCH}_bin.tar.gz" /opt/

#RUN \
#	curl -LO "https://download.oracle.com/graalvm/${CF_GRAAL_VERSION_MAJOR}/latest/graalvm-jdk-${CF_GRAAL_VERSION_MAJOR}_linux-${CF_GRAAL_ARCH}_bin.tar.gz" \
#	&& tar -xf "graalvm-jdk-${CF_GRAAL_VERSION_MAJOR}_linux-${CF_GRAAL_ARCH}_bin.tar.gz" -C /opt

RUN \
	ln -s /opt/graalvm-jdk-${CF_GRAAL_VERSION_MAJOR}.* /opt/graalvm-jdk-${CF_GRAAL_VERSION_MAJOR}

ENV PATH="${PATH}:/opt/graalvm-jdk-${CF_GRAAL_VERSION_MAJOR}/bin"
ENV JAVA_HOME="/opt/graalvm-jdk-${CF_GRAAL_VERSION_MAJOR}"

RUN \
	java --version

# ----------------------------------------------------

RUN \
	apt update \
	&& apt install --no-install-recommends -y \
		git \
		ca-certificates \
		vim

RUN \
	cd /build \
	&& git clone https://github.com/tsitle/ts_java_lib_ffmpeg.git

#
ENV GRADLE_OPTS="--enable-native-access=ALL-UNNAMED -Dorg.gradle.daemon=false"

# download Gradle
RUN \
	cd /build/ts_java_lib_ffmpeg \
	&& ./gradlew :wrapper --no-daemon

# note: library versions are for Debian 12 \
RUN \
	apt install --no-install-recommends -y \
		libva-drm2 \
		libaom3 \
		libfreetype6 \
		libharfbuzz0b \
		libmp3lame0 \
		libopenh264-7 \
		libopus0 \
		libpulse0 \
		libspeex1 \
		libsvtav1enc1 \
		libtheora0 \
		libvorbis0a \
		libvpx7 \
		libzimg2 \
		libssl3

WORKDIR "/build/ts_java_lib_ffmpeg"
