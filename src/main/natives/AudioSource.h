#ifndef __SOURCE_BIND__
#define __SOURCE_BIND__ 1

#include "Common.h"
#include "UList.h"
#include "memory_layout/AUDIOSOURCE_LAYOUT.h"

struct AudioSource {
    jfloat *data; // float 32 little endian, multiple sources can share the same audio data
    jint numSamples;// length of audio data in samples
    jint numFrames; // How many frame in this source
    jint lastReadFrameIndex; // Position where we were the last time we read the source
    


    vec3 _position; 
    vec3 _direction;   
    vec3 _up;         
    vec3 _right;    
    drt _directivity;

    jfloat pitch; //source pitch

    jboolean loop;

    void *connectedLine; // Pointer to the line to which the source is connected
    void *phononContext; // Pointer to the phonon context (nb. must be manually freed)
    struct UListNode* uNode; // U-List node


    jfloat* sceneData; // Physical data, passed by Java thread
    jint sourceIndex; // Audio source index
};

void asInit(struct GlobalSettings *settings, struct AudioSource *source);

/**
 * Allocates one or more AudioSources
 */
struct AudioSource *asNew(struct GlobalSettings *settings, jint n);

/**
 * Deallocates one or more AudioSources
 * This function undoes asNew
 */
void asDestroy(struct GlobalSettings *settings, struct AudioSource *source, jint n);

jboolean asIsConnected(struct AudioSource *source);
/**
 * Read the next frame from the audio source, restart from the beginning when the end is reached
 * @return true if the end of the source has been reached
 */
jboolean asReadNextFrame(struct GlobalSettings *settings, struct AudioSource *source, jfloat *store);

jfloat asGetVolume(struct GlobalSettings *settings, struct AudioSource *source);
vec3 *asGetSourcePosition(struct GlobalSettings *settings, struct AudioSource *source);
vec3 *asGetSourceDirection(struct GlobalSettings *settings, struct AudioSource *source);

vec3 *asGetSourceUp(struct GlobalSettings *settings, struct AudioSource *source);
vec3 *asGetSourceRight(struct GlobalSettings *settings, struct AudioSource *source);

drt *asGetSourceDirectivity(struct GlobalSettings *settings, struct AudioSource *source);

void asSetSceneData(struct GlobalSettings *settings, struct AudioSource *source, jfloat *data);

jint asGetNumChannels(struct GlobalSettings *settings, struct AudioSource *source);
#define asHasFlag(settings, source, flag) (_asHasFlag(settings, source, asFlag(flag)))
jboolean _asHasFlag(struct GlobalSettings *settings, struct AudioSource *source, jint flag);

#endif