# We use an slightly outdated Debian version here to have some backward compatibility.
# The current Debian's image uses Glibc 2.38, which is not compatible e.g. with Ubuntu 22.04 (which allows for <= 2.35)
FROM debian:12

# Add GraalVM
ARG CF_GRAAL_ARCH=-unknown-
ARG CF_GRAAL_VERSION_MAJOR=-unknown-

#ADD "files/graalvm-jdk-${CF_GRAAL_VERSION_MAJOR}_linux-${CF_GRAAL_ARCH}_bin.tar.gz" /opt/

RUN \
	apt update \
	&& apt install --no-install-recommends -y \
		ca-certificates \
		curl

RUN \
	curl -LO "https://download.oracle.com/graalvm/${CF_GRAAL_VERSION_MAJOR}/latest/graalvm-jdk-${CF_GRAAL_VERSION_MAJOR}_linux-${CF_GRAAL_ARCH}_bin.tar.gz" \
	&& tar -xf "graalvm-jdk-${CF_GRAAL_VERSION_MAJOR}_linux-${CF_GRAAL_ARCH}_bin.tar.gz" -C /opt \
	&& ln -s /opt/graalvm-jdk-${CF_GRAAL_VERSION_MAJOR}.* /opt/graalvm-jdk-${CF_GRAAL_VERSION_MAJOR}

ENV PATH="${PATH}:/opt/graalvm-jdk-${CF_GRAAL_VERSION_MAJOR}/bin"
ENV JAVA_HOME="/opt/graalvm-jdk-${CF_GRAAL_VERSION_MAJOR}"

RUN \
	java --version

# ----------------------------------------------------

# Install other dependencies
RUN \
	apt install --no-install-recommends -y \
		git \
		tar \
		zlib1g-dev

# Install en.utf-8 so we can use UTF-8 without restrictions (see 'java.nio.file.Path') in the native image compiled with GraalVM
RUN \
	apt install --no-install-recommends -y \
		locales \
	&& sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen \
	&& locale-gen

ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US:en
ENV LC_ALL=en_US.UTF-8

# ----------------------------------------------------

# Create dummy directories and files
ENV COPIED_LIBS_ROOT_PATH=/build/custom_ffmpeg_and_javacpp_ready_for_deployment

RUN \
	mkdir -p /root/.m2/repository/org/bytedeco \
	&& mkdir -p "${COPIED_LIBS_ROOT_PATH}/libs/rkmpp-${CF_GRAAL_ARCH}" \
	&& touch "${COPIED_LIBS_ROOT_PATH}/libs/rkmpp-${CF_GRAAL_ARCH}/librockchip_mpp.so.0" \
	&& touch "${COPIED_LIBS_ROOT_PATH}/libs/rkmpp-${CF_GRAAL_ARCH}/librockchip_vpu.so.0" \
	&& touch "${COPIED_LIBS_ROOT_PATH}/libs/rkmpp-${CF_GRAAL_ARCH}/librga.so.2.1.0"

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

WORKDIR /build
