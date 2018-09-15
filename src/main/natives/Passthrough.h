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
#ifndef __PASSTRHROUGH__
#define __PASSTRHROUGH__
#include "Common.h" 

static inline void passThrough(struct GlobalSettings *settings,jfloat *input, jfloat *output,jint inputChannels) {
    jint inputIndex = 0;
    jint outputIndex = 0;
    jint ml = settings->nOutputChannels / inputChannels;
    while (inputIndex < settings->frameSize*inputChannels) {
        for(jint j=0;j<ml;j++){
            output[outputIndex++] = input[inputIndex];
        }
        inputIndex++;
    }
}

static inline  void  passThroughMixer(struct GlobalSettings *settings,jfloat** inputs,jint nInputs,jfloat *output){
    for (jint i = 0; i < settings->frameSize * settings->nOutputChannels; i++) {
        jfloat res = 0;
        for (jint j = 0; j < nInputs; j++) {
            res += inputs[j][i];
        }
        res /= nInputs;
        output[i] = res;
    }
}

#endif