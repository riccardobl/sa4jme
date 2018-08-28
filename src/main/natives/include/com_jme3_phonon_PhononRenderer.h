/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_jme3_phonon_PhononRenderer */

#ifndef _Included_com_jme3_phonon_PhononRenderer
#define _Included_com_jme3_phonon_PhononRenderer
#ifdef __cplusplus
extern "C" {
#endif
#undef com_jme3_phonon_PhononRenderer_MIN_PRIORITY
#define com_jme3_phonon_PhononRenderer_MIN_PRIORITY 1L
#undef com_jme3_phonon_PhononRenderer_NORM_PRIORITY
#define com_jme3_phonon_PhononRenderer_NORM_PRIORITY 5L
#undef com_jme3_phonon_PhononRenderer_MAX_PRIORITY
#define com_jme3_phonon_PhononRenderer_MAX_PRIORITY 10L
/*
 * Class:     com_jme3_phonon_PhononRenderer
 * Method:    initNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_initNative
  (JNIEnv *, jobject);

/*
 * Class:     com_jme3_phonon_PhononRenderer
 * Method:    updateNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_updateNative
  (JNIEnv *, jobject);

/*
 * Class:     com_jme3_phonon_PhononRenderer
 * Method:    destroyNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_destroyNative
  (JNIEnv *, jobject);

/*
 * Class:     com_jme3_phonon_PhononRenderer
 * Method:    loadChannel
 * Signature: (IJII)V
 */
JNIEXPORT void JNICALL Java_com_jme3_phonon_PhononRenderer_loadChannel
  (JNIEnv *, jobject, jint, jlong, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
