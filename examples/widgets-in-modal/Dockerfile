# Dockerfile for DEUNA Android SDK Integration Tests
# Optimized for Ubuntu/Linux with KVM acceleration

# Use budtmo/docker-android - popular image with Android SDK pre-installed
FROM budtmo/docker-android:emulator_13.0

# Switch to root for installations
USER root

# Install JDK 17 and other required tools
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    curl \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME for JDK 17
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Verify installations and show Android SDK info
RUN java -version
RUN echo "ANDROID_HOME: $ANDROID_HOME"
RUN echo "ANDROID_SDK_ROOT: $ANDROID_SDK_ROOT"

# Install required Android SDK components (x86_64 for KVM acceleration)
RUN yes | sdkmanager --licenses
RUN sdkmanager "system-images;android-31;google_apis;x86_64"
RUN sdkmanager "platforms;android-31"
RUN sdkmanager "build-tools;31.0.0"

# Switch to androidusr to create AVD in the correct user context
USER androidusr

# Create AVD for testing (API 31 x86_64 - optimized for KVM on Ubuntu)
RUN echo "no" | avdmanager create avd \
    --force \
    --name test_avd \
    --package "system-images;android-31;google_apis;x86_64" \
    --abi google_apis/x86_64

# Verify AVD was created
RUN avdmanager list avd

# Switch back to root for project setup
USER root

# Set working directory
WORKDIR /app

# Copy project files
COPY . /app

# Enable widgets-in-modal module (same as GitHub Actions)
RUN sed -i 's|//include( "widgets-in-modal")|include("widgets-in-modal")|' settings.gradle.kts && \
    sed -i 's|//project(":widgets-in-modal").projectDir|project(":widgets-in-modal").projectDir|' settings.gradle.kts

# Make gradlew executable
RUN chmod +x ./gradlew

# Download Gradle dependencies (with better error handling)
RUN ./gradlew --no-daemon --stacktrace dependencies || echo "Dependencies download completed with warnings"

# Copy and make test execution script executable
COPY examples/widgets-in-modal/run-android-tests.sh /app/run-android-tests.sh
RUN chmod +x /app/run-android-tests.sh

# Create test results directory
RUN mkdir -p /test-results

# Switch back to androidusr for execution
USER androidusr

# Expose ADB ports
EXPOSE 5037
EXPOSE 5554
EXPOSE 5555

# Set entry point
ENTRYPOINT ["/app/run-android-tests.sh"]