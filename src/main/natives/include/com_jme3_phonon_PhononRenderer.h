/**
* Copyright (c) 2018, Riccardo Balbo - Lorenzo Catania
* All rights reserved.
*
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* - Redistributions of source code must retain the above copyright
*      notice, this list of conditions and the following disclaimer.
*
* - Redistributions in binary form must reproduce the above copyright
*      notice, this list of conditions and the following disclaimer in the
*      documentation and/or other materials provided with the distribution.
*
* - Neither the name of the developers nor the
*      names of the contributors may be used to endorse or promote products
*      derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/
/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_jme3_phonon_PhononRenderer */

#ifndef _Included_com_jme3_phonon_PhononRenderer
#define _Included_com_jme3_phonon_PhononRenderer
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_jme3_phonon_PhononRenderer
 * Method:    setEnvironmentNative
 * Signature: ([F)V
 */
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_setEnvironmentNative
  (JNIEnv *, jobject, jfloatArray);

/*
 * Class:     com_jme3_phonon_PhononRenderer
 * Method:    connectSourceNative
 * Signature: (IJ)I
 */
JNIEXPORT jint JNICALL Java_com_jme3_phonon_PhononRenderer_connectSourceNative
  (JNIEnv *, jobject, jint, jlong);

/*
 * Class:     com_jme3_phonon_PhononRenderer
 * Method:    disconnectSourceNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_disconnectSourceNative
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_jme3_phonon_PhononRenderer
 * Method:    initLineNative
 * Signature: (IJ)V
 */
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_initLineNative
  (JNIEnv *, jobject, jint, jlong);

/*
 * Class:     com_jme3_phonon_PhononRenderer
 * Method:    updateNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_updateNative
  (JNIEnv *, jobject);

/*
 * Class:     com_jme3_phonon_PhononRenderer
 * Method:    initNative
 * Signature: (IIIIIIJ[JZ)V
 */
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_initNative
  (JNIEnv *, jobject, jint, jint, jint, jint, jint, jint, jlong, jlongArray, jboolean);

/*
 * Class:     com_jme3_phonon_PhononRenderer
 * Method:    destroyNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_destroyNative
  (JNIEnv *, jobject);

/*
 * Class:     com_jme3_phonon_PhononRenderer
 * Method:    startThreadNative
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_startThreadNative
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     com_jme3_phonon_PhononRenderer
 * Method:    createStaticMeshNative
 * Signature: (IIJJJ)J
 */
JNIEXPORT jlong JNICALL Java_com_jme3_phonon_PhononRenderer_createStaticMeshNative
  (JNIEnv *, jobject, jint, jint, jlong, jlong, jlong);

/*
 * Class:     com_jme3_phonon_PhononRenderer
 * Method:    destroyStaticMeshNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_destroyStaticMeshNative
  (JNIEnv *, jobject, jlong);

/*
 * Class:     com_jme3_phonon_PhononRenderer
 * Method:    saveSceneAsObjNative
 * Signature: (J[B)V
 */
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_saveSceneAsObjNative
  (JNIEnv *, jobject, jlong, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif
