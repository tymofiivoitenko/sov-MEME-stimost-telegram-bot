FROM ubuntu:18.04

ARG NODE_VERSION=v10.22.0
ENV SASS_LIBSASS_PATH=/usr/local/lib/libsass
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
ENV ANT_OPTS=-Dbuild.sysclasspath=ignore
ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8

RUN apt-get update \
    && apt install -y ant curl wget gnupg2 openjdk-8-jdk-headless and git-core sassc xfonts-base xfonts-75dpi make gcc g++-y ca-certificates-java xvfb libfontconfig build-essential \
    && wget http://nodejs.org/dist/${NODE_VERSION}/node-${NODE_VERSION}-linux-x64.tar.gz \
    && tar -C /usr/local --strip-components 1 -xzf node-${NODE_VERSION}-linux-x64.tar.gz \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && ln -s /usr/bin/sassc /usr/local/bin/sassc \
    && npm install -g requirejs clean-css-cli@"<5.0.0" yarn

WORKDIR /usr/local/lib/

RUN git clone https://github.com/sass/sassc.git --branch 3.4.2 --depth 1 \
    && git clone https://github.com/sass/libsass.git --branch 3.4-stable --depth 1 \
    && git clone https://github.com/sass/sass-spec.git --depth=1 \
    && make -C libsass \
    && make -C sassc \
    && make -C sassc install

RUN mkdir -p /app \
    && mkdir -p /var/log/mercato \
    && mkdir -p /mercato/web-content/{assets frontend view WEB-INF} \
    && ln -s /var/mercato-web /app
