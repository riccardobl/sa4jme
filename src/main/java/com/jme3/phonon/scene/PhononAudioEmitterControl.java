package com.jme3.phonon.scene;

import java.io.IOException;

import com.jme3.asset.AssetNotFoundException;
import com.jme3.audio.AudioContext;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioKey;
import com.jme3.audio.AudioParam;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.AudioSource;
import com.jme3.audio.AudioStream;
import com.jme3.audio.Filter;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.math.Vector3f;
import com.jme3.phonon.PhononSettings.PhononDirectOcclusionMethod;
import com.jme3.phonon.PhononSettings.PhononDirectOcclusionMode;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.jme3.util.PlaceholderAssets;
import com.jme3.util.clone.Cloner;

public class PhononAudioEmitterControl extends AbstractControl implements AudioSource {

    public static final int SAVABLE_VERSION = 1;
    protected boolean loop = false;
    protected float volume = 1;
    protected float pitch = 1;
    protected float timeOffset = 0;
    protected AudioKey audioKey;
    protected transient AudioData data = null;
    protected transient volatile AudioSource.Status status = AudioSource.Status.Stopped;
    protected transient volatile int channel = -1;
    private boolean directional = false;
    protected Vector3f direction = new Vector3f(0, 0, 1);
    protected Vector3f previousWorldTranslation = Vector3f.ZERO;
    protected boolean positional = true;

    // Phonon source settings
    protected float dipoleWeight = 0f;
    protected float dipolePower = 1f;
    protected boolean applyAirAbsorption = false;
    protected PhononDirectOcclusionMode directOcclusionMode = PhononDirectOcclusionMode.IPL_DIRECTOCCLUSION_NONE; 
    protected PhononDirectOcclusionMethod directOcclusionMethod = PhononDirectOcclusionMethod.IPL_DIRECTOCCLUSION_RAYCAST;
    protected float sourceRadius = 0f;


    /**
     * Creates a new <code>AudioEmitterControl</code> without any audio data set.
     */
    public PhononAudioEmitterControl() { }

    /**
     * Creates a new <code>AudioEmitterControl</code> with the given data and key.
     *
     * @param audioData The audio data contains the audio track to play.
     * @param audioKey The audio key that was used to load the AudioData
     */
    public PhononAudioEmitterControl(AudioData audioData, AudioKey audioKey) {
        setAudioData(audioData, audioKey);
    }

    protected AudioRenderer getRenderer() {
        AudioRenderer result = AudioContext.getAudioRenderer();
        if( result == null )
            throw new IllegalStateException( "No audio renderer available, make sure call is being performed on render thread." );
        return result;
    }

    /**
     * Start playing the audio.
     */
    public void play(){
        if (positional && data.getChannels() > 1) {
            throw new IllegalStateException("Only mono audio is supported for positional audio nodes");
        }
        getRenderer().playSource(this);
    }

    /**
     * Start playing an instance of this audio. This method can be used
     * to play the same <code>AudioEmitterControl</code> multiple times. Note
     * that changes to the parameters of this AudioEmitterControl will not affect the
     * instances already playing.
     */
    public void playInstance(){
        if (positional && data.getChannels() > 1) {
            throw new IllegalStateException("Only mono audio is supported for positional audio nodes");
        }
        getRenderer().playSourceInstance(this);
    }

    /**
     * Stop playing the audio that was started with {@link PhononAudioEmitterControl#play() }.
     */
    public void stop(){
        getRenderer().stopSource(this);
    }

    /**
     * Pause the audio that was started with {@link PhononAudioEmitterControl#play() }.
     */
    public void pause(){
        getRenderer().pauseSource(this);
    }

    /** DO NOT USE THESE METHODS **/
    /** They are defined for compatibility with AudioSource **/
    public float getRefDistance() { return 0f; }
    public float getOuterAngle() { return 0f; }
    public float getInnerAngle() { return 0f; }
    public float getMaxDistance() { return 0f; }
    public Vector3f getVelocity () { return Vector3f.NAN; }
    public Filter getDryFilter() { return null; }
    public Filter getReverbFilter() { return null; }
    public boolean isReverbEnabled() { return false; } // FIXME: Returns true when native reverb is enabled

    /**
     * Do not use.
     */
    public final void setChannel(int channel) {
        if (status != AudioSource.Status.Stopped) {
            throw new IllegalStateException("Can only set source id when stopped");
        }

        this.channel = channel;
    }

    /**
     * Do not use.
     */
    public int getChannel() {
        return channel;
    }


    /**
     * Set the audio data to use for the audio. Note that this method
     * can only be called once, if for example the audio node was initialized
     * without an {@link AudioData}.
     *
     * @param audioData The audio data contains the audio track to play.
     * @param audioKey The audio key that was used to load the AudioData
     */
    public void setAudioData(AudioData audioData, AudioKey audioKey) {
        if (data != null) {
            throw new IllegalStateException("Cannot change data once its set");
        }

        data = audioData;
        this.audioKey = audioKey;
    }

    /**
     * @return The {@link AudioData} set previously with
     * {@link PhononAudioEmitterControl#setAudioData(com.jme3.audio.AudioData, com.jme3.audio.AudioKey) }
     * or any of the constructors that initialize the audio data.
     */
    public AudioData getAudioData() {
        return data;
    }

    /**
     * @return The {@link Status} of the audio node.
     * The status will be changed when either the {@link PhononAudioEmitterControl#play() }
     * or {@link PhononAudioEmitterControl#stop() } methods are called.
     */
    public AudioSource.Status getStatus() {
        return status;
    }

    /**
     * Do not use.
     */
    public final void setStatus(AudioSource.Status status) {
        this.status = status;
    }

    /**
     * Get the Type of the underlying AudioData to see if it's streamed or buffered.
     * This is a shortcut to getAudioData().getType()
     * <b>Warning</b>: Can return null!
     * @return The {@link com.jme3.audio.AudioData.DataType} of the audio node.
     */
    public DataType getType() {
        if (data == null)
            return null;
        else
            return data.getDataType();
    }

    /**
     * @return True if the audio will keep looping after it is done playing,
     * otherwise, false.
     * @see PhononAudioEmitterControl#setLooping(boolean)
     */
    public boolean isLooping() {
        return loop;
    }

    /**
     * Set the looping mode for the audio node. The default is false.
     *
     * @param loop True if the audio should keep looping after it is done playing.
     */
    public void setLooping(boolean loop) {
        this.loop = loop;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioParam.Looping);
    }

    /**
     * @return The pitch of the audio, also the speed of playback.
     *
     * @see PhononAudioEmitterControl#setPitch(float)
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Set the pitch of the audio, also the speed of playback.
     * The value must be between 0.5 and 2.0.
     *
     * @param pitch The pitch to set.
     * @throws IllegalArgumentException If pitch is not between 0.5 and 2.0.
     */
    public void setPitch(float pitch) {
        if (pitch < 0.5f || pitch > 2.0f) {
            throw new IllegalArgumentException("Pitch must be between 0.5 and 2.0");
        }

        this.pitch = pitch;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioParam.Pitch);
    }

    /**
     * @return The volume of this audio node.
     *
     * @see PhononAudioEmitterControl#setVolume(float)
     */
    public float getVolume() {
        return volume;
    }

    /**
     * Set the volume of this audio node.
     *
     * The volume is specified as gain. 1.0 is the default.
     *
     * @param volume The volume to set.
     * @throws IllegalArgumentException If volume is negative
     */
    public void setVolume(float volume) {
        if (volume < 0f) {
            throw new IllegalArgumentException("Volume cannot be negative");
        }

        this.volume = volume;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioParam.Volume);
    }

    /**
     * @return the time offset in the sound sample when to start playing.
     */
    public float getTimeOffset() {
        return timeOffset;
    }

    /**
     * Set the time offset in the sound sample when to start playing.
     *
     * @param timeOffset The time offset
     * @throws IllegalArgumentException If timeOffset is negative
     */
    public void setTimeOffset(float timeOffset) {
        if (timeOffset < 0f) {
            throw new IllegalArgumentException("Time offset cannot be negative");
        }

        this.timeOffset = timeOffset;
        if (data instanceof AudioStream) {
            ((AudioStream) data).setTime(timeOffset);
        }else if(status == AudioSource.Status.Playing){
            stop();
            play();
        }
    }

    @Override
    public float getPlaybackTime() {
        if (channel >= 0)
            return getRenderer().getSourcePlaybackTime(this);
        else
            return 0;
    }

    public Vector3f getPosition() {
        return spatial.getWorldTranslation();
    }

    /**
     * @return True if the audio node is directional
     *
     * @see PhononAudioEmitterControl#setDirectional(boolean)
     */
    public boolean isDirectional() {
        return directional;
    }

    /**
     * Set the audio node to be directional.
     * Does nothing if the audio node is not positional.
     * <br/>
     * After setting directional, you should call
     * {@link PhononAudioEmitterControl#setDirection(com.jme3.math.Vector3f) }
     * to set the audio node's direction.
     *
     * @param directional If the audio node is directional
     */
    public void setDirectional(boolean directional) {
        this.directional = directional;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioParam.IsDirectional);
    }

    /**
     * @return The direction of this audio node.
     *
     * @see PhononAudioEmitterControl#setDirection(com.jme3.math.Vector3f)
     */
    public Vector3f getDirection() {
        return direction;
    }

    /**
     * Set the direction of this audio node.
     * Does nothing if the audio node is not directional.
     *
     * @param direction
     * @see PhononAudioEmitterControl#setDirectional(boolean)
     */
    public void setDirection(Vector3f direction) {
        this.direction = direction;
        if (channel >= 0)
            getRenderer().updateSourceParam(this, AudioParam.Direction);
    }

    /**
     * @return True if the audio node is positional.
     *
     * @see PhononAudioEmitterControl#setPositional(boolean)
     */
    public boolean isPositional() {
        return positional;
    }

    /**
     * Set the audio node as positional.
     * The position, velocity, and distance parameters affect positional
     * audio nodes. Set to false if the audio node should play in "headspace".
     *
     * @param positional True if the audio node should be positional, otherwise
     * false if it should be headspace.
     */
    public void setPositional(boolean positional) {
        this.positional = positional;
        if (channel >= 0) {
            getRenderer().updateSourceParam(this, AudioParam.IsPositional);
        }
    }

    public Vector3f getUp() {
        return spatial.getWorldRotation().getRotationColumn(1);
    }

    public Vector3f getRight() {
        return spatial.getWorldRotation().getRotationColumn(0).negate();
    }

    public void setDipoleWeight(float dipoleWeight) {
        this.dipoleWeight = dipoleWeight;
    }

    public float getDipoleWeight() {
        return dipoleWeight;
    }

    public void setDipolePower(float dipolePower) {
        this.dipolePower = dipolePower;
    }

    public float getDipolePower() {
        return dipolePower;
    }

    public void setApplyAirAbsorption(boolean applyAirAbsorption) {
        this.applyAirAbsorption = applyAirAbsorption;
    }

    public boolean isAirAbsorptionApplied() {
        return applyAirAbsorption;
    }

    public void setDirectOcclusionMode(PhononDirectOcclusionMode directOcclusionMode) {
        this.directOcclusionMode = directOcclusionMode;
    }

    public PhononDirectOcclusionMode getDirectOcclusionMode() {
        return directOcclusionMode;
    }

    public void setDirectOcclusionMethod(PhononDirectOcclusionMethod directOcclusionMethod) {
        this.directOcclusionMethod = directOcclusionMethod;
    }

    public PhononDirectOcclusionMethod getDirectOcclusionMethod() {
        return directOcclusionMethod;
    }

    public void setSourceRadius(float sourceRadius) {
        this.sourceRadius = sourceRadius;
    }

    public float getSourceRadius() {
        return sourceRadius;
    }

    protected void controlUpdate(float tpf) { }

    protected void controlRender(RenderManager rm, ViewPort vp) {
        if (channel < 0 || spatial.getParent() == null) return;
        Vector3f currentWorldTranslation = getPosition();

        if (!previousWorldTranslation.equals(currentWorldTranslation)) {
            getRenderer().updateSourceParam(this, AudioParam.Position);

            previousWorldTranslation.set(currentWorldTranslation);
        }
    }

    @Override
    public PhononAudioEmitterControl clone() {
        try {
            return (PhononAudioEmitterControl) super.clone();
        } catch (CloneNotSupportedException e) {
            System.err.println("Failed to clone AudioEmitterControl");
            return null;
        }
    }

    /**
     *  Called internally by com.jme3.util.clone.Cloner.  Do not call directly.
     */
    @Override
    public void cloneFields( Cloner cloner, Object original ) {
        super.cloneFields(cloner, original); 

        this.direction=cloner.clone(direction);
        this.previousWorldTranslation=Vector3f.NAN.clone();
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(audioKey, "audio_key", null);
        oc.write(loop, "looping", false);
        oc.write(volume, "volume", 1);
        oc.write(pitch, "pitch", 1);
        oc.write(timeOffset, "time_offset", 0);

        oc.write(directional, "directional", false);
        oc.write(direction, "direction", null);

        oc.write(positional, "positional", false);

        //Phonon specific settings
        oc.write(dipoleWeight, "dipole_weight", 1f);
        oc.write(dipolePower, "dipole_power", 0f);
        oc.write(applyAirAbsorption, "apply_air_absorption", false);
        oc.write(directOcclusionMode.ordinal(), "direct_occlusion_mode", PhononDirectOcclusionMode.IPL_DIRECTOCCLUSION_NONE.ordinal());
        oc.write(directOcclusionMethod.ordinal(), "direct_occlusion_method", PhononDirectOcclusionMethod.IPL_DIRECTOCCLUSION_RAYCAST.ordinal());
        oc.write(sourceRadius, "source_radius", 0f);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule ic = im.getCapsule(this);

        // NOTE: In previous versions of jME3, audioKey was actually
        // written with the name "key". This has been changed
        // to "audio_key" in case Spatial's key will be written as "key".
        if (ic.getSavableVersion(PhononAudioEmitterControl.class) == 0){
            audioKey = (AudioKey) ic.readSavable("key", null);
        }else{
            audioKey = (AudioKey) ic.readSavable("audio_key", null);
        }

        loop = ic.readBoolean("looping", false);
        volume = ic.readFloat("volume", 1);
        pitch = ic.readFloat("pitch", 1);

        timeOffset = ic.readFloat("time_offset", 0);
        
        directional = ic.readBoolean("directional", false);
        direction = (Vector3f) ic.readSavable("direction", null);

        positional = ic.readBoolean("positional", false);

        // Phonon specific settings
        dipoleWeight = ic.readFloat("dipole_weight", 1f);
        dipolePower = ic.readFloat("dipole_power", 0f);
        applyAirAbsorption = ic.readBoolean("apply_air_absorption", false);
        directOcclusionMode = PhononDirectOcclusionMode.values()
            [ic.readInt("direct_occlusion_mode", PhononDirectOcclusionMode.IPL_DIRECTOCCLUSION_NONE.ordinal())];
        directOcclusionMethod = PhononDirectOcclusionMethod.values()
            [ic.readInt("direct_occlusion_method", PhononDirectOcclusionMethod.IPL_DIRECTOCCLUSION_RAYCAST.ordinal())];
        sourceRadius = ic.readFloat("source_radius", 0f);

        if (audioKey != null) {
            try {
                data = im.getAssetManager().loadAsset(audioKey);
            } catch (AssetNotFoundException ex){
                // TODO: Restore exception logging
                // Logger.getLogger(AudioEmitterControl.class.getName()).log(Level.FINE, "Cannot locate {0} for audio node {1}", new Object[]{audioKey, key});
                data = PlaceholderAssets.getPlaceholderAudio();
            }
        }
    }

    @Override
    public String toString() {
        String ret = getClass().getSimpleName()
                + "[status=" + status;
        if (volume != 1f) {
            ret += ", vol=" + volume;
        }
        if (pitch != 1f) {
            ret += ", pitch=" + pitch;
        }
        return ret + "]";
    }
}