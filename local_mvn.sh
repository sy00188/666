#!/bin/bash

# Enhanced Maven simulation script for Spring Boot projects
echo "[INFO] Starting Maven build..."

# Maven repository path
M2_REPO="$HOME/.m2/repository"

# Build classpath with essential dependencies
build_classpath() {
    local CP=""
    
    # Core Spring Boot dependencies
    local SPRING_BOOT_JARS=(
        "org/springframework/boot/spring-boot/3.2.2/spring-boot-3.2.2.jar"
        "org/springframework/boot/spring-boot-autoconfigure/3.2.2/spring-boot-autoconfigure-3.2.2.jar"
        "org/springframework/spring-core/6.1.3/spring-core-6.1.3.jar"
        "org/springframework/spring-context/6.1.3/spring-context-6.1.3.jar"
        "org/springframework/spring-beans/6.1.3/spring-beans-6.1.3.jar"
        "org/springframework/spring-web/6.1.3/spring-web-6.1.3.jar"
        "org/springframework/spring-webmvc/6.1.3/spring-webmvc-6.1.3.jar"
    )
    
    # Essential dependencies
    local ESSENTIAL_JARS=(
        "org/projectlombok/lombok/1.18.36/lombok-1.18.36.jar"
        "jakarta/validation/jakarta.validation-api/3.0.2/jakarta.validation-api-3.0.2.jar"
        "io/swagger/core/v3/swagger-core-jakarta/2.2.19/swagger-core-jakarta-2.2.19.jar"
        "io/swagger/core/v3/swagger-annotations-jakarta/2.2.19/swagger-annotations-jakarta-2.2.19.jar"
        "com/fasterxml/jackson/core/jackson-core/2.16.1/jackson-core-2.16.1.jar"
        "com/fasterxml/jackson/core/jackson-databind/2.16.1/jackson-databind-2.16.1.jar"
        "com/fasterxml/jackson/core/jackson-annotations/2.16.1/jackson-annotations-2.16.1.jar"
        "org/slf4j/slf4j-api/2.0.12/slf4j-api-2.0.12.jar"
    )
    
    # Add current directory and target/classes
    CP=".:target/classes"
    
    # Add Spring Boot JARs
    for jar in "${SPRING_BOOT_JARS[@]}"; do
        if [ -f "$M2_REPO/$jar" ]; then
            CP="$CP:$M2_REPO/$jar"
        fi
    done
    
    # Add essential JARs
    for jar in "${ESSENTIAL_JARS[@]}"; do
        if [ -f "$M2_REPO/$jar" ]; then
            CP="$CP:$M2_REPO/$jar"
        fi
    done
    
    # ZXing dependencies (for QR code generation)
    local ZXING_JARS=(
        "com/google/zxing/core/3.5.1/core-3.5.1.jar"
        "com/google/zxing/javase/3.5.1/javase-3.5.1.jar"
    )
    
    # Add ZXing JARs
    for jar in "${ZXING_JARS[@]}"; do
        if [ -f "$M2_REPO/$jar" ]; then
            CP="$CP:$M2_REPO/$jar"
        fi
    done
    
    # Apache POI dependencies (for Excel processing)
     local POI_JARS=(
         "org/apache/poi/poi/5.2.4/poi-5.2.4.jar"
         "org/apache/poi/poi-ooxml/5.2.4/poi-ooxml-5.2.4.jar"
         "org/apache/poi/poi-ooxml-lite/5.2.4/poi-ooxml-lite-5.2.4.jar"
         "org/apache/poi/poi-scratchpad/5.2.4/poi-scratchpad-5.2.4.jar"
     )
     
     # Add POI JARs
     for jar in "${POI_JARS[@]}"; do
         if [ -f "$M2_REPO/$jar" ]; then
             CP="$CP:$M2_REPO/$jar"
         fi
     done
     
     # Add all JARs from common dependency directories
    local COMMON_DIRS=(
        "org/springframework"
        "jakarta"
        "io/swagger"
        "com/fasterxml/jackson"
        "org/slf4j"
        "ch/qos/logback"
        "org/apache/tomcat"
        "org/hibernate"
        "com/baomidou"
        "org/mybatis"
        "com/google/zxing"
    )
    
    for dir in "${COMMON_DIRS[@]}"; do
        if [ -d "$M2_REPO/$dir" ]; then
            while IFS= read -r -d '' jar; do
                CP="$CP:$jar"
            done < <(find "$M2_REPO/$dir" -name "*.jar" -not -name "*sources*" -not -name "*javadoc*" -print0 2>/dev/null)
        fi
    done
    
    echo "$CP"
}

case "$1" in
    "clean")
        echo "[INFO] Cleaning project..."
        if [ -d "target" ]; then
            rm -rf target
            echo "[INFO] Deleted target directory"
        fi
        echo "[INFO] BUILD SUCCESS"
        ;;
    "compile")
        echo "[INFO] Compiling project..."
        mkdir -p target/classes
        
        # Build classpath
        echo "[INFO] Building classpath..."
        CLASSPATH=$(build_classpath)
        
        # Find all Java files
        find src/main/java -name "*.java" > java_files.txt
        if [ -s java_files.txt ]; then
            echo "[INFO] Compiling $(wc -l < java_files.txt) source files"
            echo "[INFO] Using classpath with $(echo "$CLASSPATH" | tr ':' '\n' | wc -l) entries"
            
            # Compile with annotation processing for Lombok
            javac -cp "$CLASSPATH" \
                  -d target/classes \
                  -processor lombok.launch.AnnotationProcessorHider\$AnnotationProcessor \
                  -parameters \
                  -encoding UTF-8 \
                  @java_files.txt
            
            if [ $? -eq 0 ]; then
                echo "[INFO] BUILD SUCCESS"
                echo "[INFO] Compiled classes are in target/classes"
                
                # Copy resources
                if [ -d "src/main/resources" ]; then
                    echo "[INFO] Copying resources..."
                    cp -r src/main/resources/* target/classes/ 2>/dev/null || true
                fi
            else
                echo "[ERROR] BUILD FAILURE"
                rm -f java_files.txt
                exit 1
            fi
        else
            echo "[INFO] No source files to compile"
        fi
        rm -f java_files.txt
        ;;
    *)
        if [ "$1" = "clean" ] && [ "$2" = "compile" ]; then
            echo "[INFO] Cleaning and compiling project..."
            if [ -d "target" ]; then
                rm -rf target
                echo "[INFO] Deleted target directory"
            fi
            mkdir -p target/classes
            
            # Build classpath
            echo "[INFO] Building classpath..."
            CLASSPATH=$(build_classpath)
            
            find src/main/java -name "*.java" > java_files.txt
            if [ -s java_files.txt ]; then
                echo "[INFO] Compiling $(wc -l < java_files.txt) source files"
                echo "[INFO] Using classpath with $(echo "$CLASSPATH" | tr ':' '\n' | wc -l) entries"
                
                # Compile with annotation processing for Lombok
                javac -cp "$CLASSPATH" \
                      -d target/classes \
                      -processor lombok.launch.AnnotationProcessorHider\$AnnotationProcessor \
                      -parameters \
                      -encoding UTF-8 \
                      @java_files.txt
                
                if [ $? -eq 0 ]; then
                    echo "[INFO] BUILD SUCCESS"
                    echo "[INFO] Compiled classes are in target/classes"
                    
                    # Copy resources
                    if [ -d "src/main/resources" ]; then
                        echo "[INFO] Copying resources..."
                        cp -r src/main/resources/* target/classes/ 2>/dev/null || true
                    fi
                else
                    echo "[ERROR] BUILD FAILURE"
                    rm -f java_files.txt
                    exit 1
                fi
            fi
            rm -f java_files.txt
        else
            echo "Usage: ./local_mvn.sh [clean] [compile]"
        fi
        ;;
esac
