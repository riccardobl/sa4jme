#/bin/sh
echo "Premake $PWD"
mkdir -p tmp/tools
mkdir -p tmp/cache
mkdir -p build/natives

source premake.dep/safeRm.sh
source premake.dep/findJava.sh

# Get steam audio
if [ "$STEAM_AUDIO_URL" = "" ];
then
    export STEAM_AUDIO_URL="https://github.com/ValveSoftware/steam-audio/releases/download/v2.0-beta.14/steamaudio_api_2.0-beta.14.zip"
fi

export STEAM_AUDIO_URL_HASH="`echo "$STEAM_AUDIO_URL" | sha256sum | cut -d' ' -f1`"

if [  ! -f src/steamaudio/include/phonon.h -o  "$REGEN_BINDINGS" != "" ];
then
    safeRm tmp/ext_sta
    mkdir -p tmp/ext_sta
    if [ ! -f tmp/cache/$STEAM_AUDIO_URL_HASH.zip ];
    then
        echo "Download steam audio"
        wget "$STEAM_AUDIO_URL" -O tmp/cache/$STEAM_AUDIO_URL_HASH.zip
       cp tmp/cache/$STEAM_AUDIO_URL_HASH.zip tmp/ext_sta/steamaudio.zip
    else
        echo "Use steam audio from cache tmp/cache/$STEAM_AUDIO_URL_HASH.zip"
        cp tmp/cache/$STEAM_AUDIO_URL_HASH.zip tmp/ext_sta/steamaudio.zip
    fi
    cd tmp/ext_sta
    unzip steamaudio.zip
    safeRm steamaudio.zip
    
    safeRm ../../src/steamaudio
    mkdir -p ../../src/steamaudio
    cp -Rf steamaudio_api/* ../../src/steamaudio/
    
    cd ..
    safeRm ext_sta

    cd ../src/steamaudio/

    cp -Rf bin/* lib/
    safeRm bin

    # mkdir -p lib/linux-x86
    # cp lib/Linux/x86/*.so lib/linux-x86/

    # mkdir -p lib/linux-x86-64
    # cp lib/Linux/x64/*.so lib/linux-x86-64

    # mkdir -p lib/win32-x86
    # cp bin/Windows/x86/*.dll lib/win32-x86

    # mkdir -p lib/win32-x86-64
    # cp bin/Windows/x64/*.dll lib/win32-x86-64

    # mkdir -p lib/darwin
    # cp lib/OSX/*.dylib lib/darwin

    # rm -Rf lib/Android
    # rm -Rf lib/Linux
    # rm -Rf lib/OSX
    # rm -Rf lib/Windows

    # rm -Rf bin

    #apt-get install htmldoc archmage
    if [ "$CONVERT_DOC" == "true" ];
    then
        cd doc
        archmage -c pdf steamaudio_api.chm steamaudio_api.pdf
        # archmage -c html steamaudio_api.chm steamaudio_api.html
        cd ..
    fi
    cd ../../
fi
################


function clean {
    # safeRm build/classes
    # safeRm build/libs
    # safeRm build/natives
    # safeRm build/reports
    # safeRm build/resources
    # safeRm build/test-results
    # safeRm bin

    safeRm build
    safeRm tmp
    safeRm bin
    safeRm src/steamaudio
    gradle clean
}


# function deepClean {
#     clean

   
# }


#####
# build 
#    compiler  [ gcc,g++,clang... ]
#    platform [ Windows,Linux,OSX,Android ]
#    arch [ x86,x64 ]
#    args [ custom args ]
#    liboutfolder [ where to copy phonon lib ]
#    file [ optional ]
function build {
    compiler="$1"
    platform="$2"
    arch="$3"
        liboutfolder="$4"
    args="$5"
    args2="$6"
    libs="$7"

    echo "Compile for $platofrm $arch with $compiler"

    arch_flag="-m64"
    if [ "$arch" = "x86" ];
    then
        arch_flag="-m32"
    fi

    echo '' > tmp/build_IIlist.txt.tmp
    for line in $(cat  tmp/build_IIlist.txt); do
        echo "-I$line " >>  tmp/build_IIlist.txt.tmp 
    done
    cat tmp/build_IIlist.txt.tmp > tmp/build_IIlist.txt
    
    platform_libprefix="lib"
    platform_libsuffix=".so"
   
    if [ "$platform" = "Windows" ];
    then
        platform_libprefix=""
        platform_libsuffix=".dll"
    elif [ "$platform" = "OSX" ];
    then
        platform_libprefix=""
        platform_libsuffix=".dylib"
    fi


  

    build_script="
    $compiler -mtune=generic  
    -fmessage-length=0 
    -fpermissive 
    -O0 -fno-rtti -shared
    -fPIC  
    -Wall 
    -Lsrc/steamaudio/lib/$platform/$arch
    -Isrc/steamaudio/include
    $arch_flag   
    $(cat  tmp/build_IIlist.txt)
    $args 
    $(cat  tmp/build_cpplist.txt) 
    $args2  -Wl,-Bdynamic -lphonon $libs"
    cp "src/steamaudio/lib/$platform/$arch/${platform_libprefix}phonon${platform_libsuffix}" "$liboutfolder/"
    echo "Run $build_script"

    $build_script
    if [ $? -ne 0 ]; then exit 1; fi
}



function buildNativeTests {
    echo '' > tmp/build_cpplist.txt
    echo '' > tmp/build_IIlist.txt

    # rm -Rvf build/tests
    mkdir -p build/tests
    cp -Rf src/tests/resources/* build/tests/
    cp -Rf src/steamaudio/lib/Linux/x64/* build/tests/
    
    cd build/tests/resources/
    safeRm inputaudio.raw
    ffmpeg -i 399354__romariogrande__eastandw_mono.ogg -f f32be -acodec pcm_f32le inputaudio.raw
    cd ../../..
    
    build "g++"  \
    "Linux" "x64" \
    "build/test/" \
    "--std=c++11 -obuild/test/SampleApplication1.64" src/test/native/SampleApplication1.cpp
    
    #build "g++"  "Linux" "x64" "--std=c++11 -obuild/tests/SampleApplication2.64" src/tests/SampleApplication2.cpp

  
}
#ffmpeg -i 399354__romariogrande__eastandw_mono.ogg -f f32be -acodec pcm_f32le inputaudio.raw



# buildTests
function removeEmptyJNIh {
    file=$1
    if ! grep -q "JNIEXPORT" "$file" ;
    then 
        safeRm "$file"
    fi 
}
export -f removeEmptyJNIh

## build natives
function genJNI {
    classpath="$1"
    rootDir="$2"
    output="$3"
    file="$4"
    file="${file#$rootDir/}"
    file="${file//\//.}"
    file="${file%.class}"
    echo "Gen JNI header for $file in $output" 
    mkdir -p "$output"
    javah -jni -d "$output" -classpath "$classpath" "$file"
    # jnih="${file//./_}"
    # jnih="$output/${jnih//\$/_}.h"
    # echo "Check $jnih"
    find "$output" -name "*.h" -exec   bash -c "removeEmptyJNIh {}" \;

    # if ! grep -q "JNIEXPORT" "$jnih" ;
    # then
    #     echo "rm Empty binding $jnih"
    #     rm "$jnih"
    # fi  
}
export -f genJNI

function updateJNIHeaders {
    echo "Update JNI headers..."
    safeRm src/main/natives/include
    classpath=$1
    rootDir=$2
    output=$3
    echo "Search $rootDir for class files"
    find "$rootDir" -name "*.class" -exec bash -c "genJNI \"$classpath\" \"$rootDir\" \"$output\" {} " \;
}


function buildNatives {
    safeRm build/natives
    mkdir -p build/natives

    echo '' > tmp/build_cpplist.txt
    echo '' > tmp/build_IIlist.txt
    
    find src/main/natives -type f -name '*.c' >> tmp/build_cpplist.txt
    
    #Linux 64
    dest="build/natives/linux-x86-64"
    mkdir -p $dest
    build "gcc" \
    "Linux" "x64" \
    "$dest" \
    "--std=c99 
    -Isrc/main/natives/include
    -Isrc/main/natives
    -I$JDK_ROOT/include
    -I$JDK_ROOT/include/linux" \
    "-Wl,-soname,jmephonon.so  -obuild/natives/linux-x86-64/libjmephonon.so" \
    ""

    #Linux 32
    dest="build/natives/linux-x86"
    mkdir -p $dest
    build "gcc" \
    "Linux" "x86" \
    "$dest" \
    "--std=c99
    -Wint-to-pointer-cast
    -Isrc/main/natives/include
    -Isrc/main/natives
    -I$JDK_ROOT/include
    -I$JDK_ROOT/include/linux" \
    "-Wl,-soname,jmephonon.so  -obuild/natives/linux-x86/libjmephonon.so" \
    ""

    #Force update vscode
    if [ -d build/resources ];
    then
        cp -Rf build/natives/* build/resources/
    fi
    if [ -d bin ];
    then
        cp -Rf build/natives/* bin/
    fi
}

$@