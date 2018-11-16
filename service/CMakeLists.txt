###############################
#1.cmake verson，指定cmake版本
cmake_minimum_required(VERSION 3.4.1)

#2.C++ 的编译选项是 CMAKE_CXX_FLAGS
# 指定编译参数，可选
#SET(CMAKE_C_FLAGS "-Wno-error=format-security -Wno-error=pointer-sign")

#3.设置cmake生成so输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})

#4.包含头文件，导入第三方动态库
include_directories(
     ${CMAKE_SOURCE_DIR}/src/main/jni/include
)

set(lib_src_DIR ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})
add_library(nanoGatt SHARED IMPORTED)
set_target_properties(nanoGatt PROPERTIES IMPORTED_LOCATION ${lib_src_DIR}/libnanoGatt.so)

#find_library( log-lib log )

#5.指定源文件和编译生成so名及类型
#  生成在intermediates/cmake/和以上指定的目录下(指定的话build.gradle设置pickFirst避免冲突)
add_library(nanogattJNI SHARED
         ${CMAKE_SOURCE_DIR}/src/main/jni/nanoGatt_jni.c)

#6.设置需要生成so的第三方链接库
target_link_libraries(
 nanogattJNI
 nanoGatt
 log
 android
)

#添加子目录,将会调用子目录中的CMakeLists.txt
#ADD_SUBDIRECTORY(one)
#ADD_SUBDIRECTORY(two)


