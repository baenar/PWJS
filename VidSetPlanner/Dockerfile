# sbt + JDK 21 environment for VidSetPlanner.
# Useful for build/test/backend work without relying on local Java/sbt installation.
FROM eclipse-temurin:21

ARG SBT_VERSION=1.10.11

RUN apt-get update \
    && apt-get install -y --no-install-recommends bash ca-certificates curl tar \
    && curl -fsSL "https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.tgz" \
        | tar -xz -C /opt \
    && ln -s /opt/sbt/bin/sbt /usr/local/bin/sbt \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

# Default command is intentionally conservative.
# Use docker compose services for specific commands.
CMD ["sbt", "test"]
