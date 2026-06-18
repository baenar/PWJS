# Maven + JDK 21 environment for VidSetPlanner.
# Useful for build/test/backend work without relying on local Java installation.
FROM maven:3-eclipse-temurin-21

WORKDIR /workspace

# Default command is intentionally conservative.
# Use docker compose services for specific commands.
CMD ["mvn", "test"]
