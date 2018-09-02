package com.jme3.phonon;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

import com.jme3.audio.AudioData;
import com.jme3.audio.AudioParam;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.AudioSource;
import com.jme3.audio.Environment;
import com.jme3.audio.Filter;
import com.jme3.audio.Listener;
import com.jme3.audio.ListenerParam;
import com.jme3.phonon.utils.Clock;
import com.jme3.phonon.utils.DirectBufferUtils;
import com.jme3.phonon.utils.Sleeper;
import com.jme3.phonon.player.PhononPlayer;
import com.jme3.system.NativeLibraryLoader;
import com.jme3.system.Platform;

/**
 * PhononRenderer
 */
public class PhononRenderer implements AudioRenderer {
	int CHANNEL_LIMIT = 16;

    private final Map<AudioData, F32leAudioData> conversionCache = new WeakHashMap<AudioData, F32leAudioData>();
	private final PhononChannel[] channels = new PhononChannel[CHANNEL_LIMIT];
	private final ConcurrentLinkedQueue<PhononPlayer> enqueuedPlayers = new ConcurrentLinkedQueue<>();
	private final LinkedList<PhononPlayer> players = new LinkedList<>();

	static{
		NativeLibraryLoader.registerNativeLibrary("Phonon", Platform.Linux32, "linux-x86/libphonon.so");
		NativeLibraryLoader.registerNativeLibrary("Phonon", Platform.Linux64, "linux-x86-64/libphonon.so");
		NativeLibraryLoader.registerNativeLibrary("JMEPhonon", Platform.Linux32, "linux-x86/libjmephonon.so");
		NativeLibraryLoader.registerNativeLibrary("JMEPhonon", Platform.Linux64, "linux-x86-64/libjmephonon.so");
		// TODO: Windows
		// TODO: OSX
		// MAYBE TODO: Android
	}
	final int _OUTPUT_FRAME_SIZE;
	final int _OUTPUT_BUFFER_SIZE ;

	Clock CLOCK=Clock.NANOSECONDS;
	Sleeper WAIT_MODE = Sleeper.BUSYSLEEP;

	
	


	public PhononRenderer(int frameSize, int bufferSize) {
		_OUTPUT_FRAME_SIZE = frameSize;
		_OUTPUT_BUFFER_SIZE = bufferSize;
		NativeLibraryLoader.loadNativeLibrary("Phonon", true);
		NativeLibraryLoader.loadNativeLibrary("JMEPhonon", true);
	}

	
	public PhononChannel getChannel(int i) {
		return channels[i];
	}

	void preInit() {
		initNative();
		for (int i = 0; i < channels.length; i++) {
			channels[i] = new PhononChannel(_OUTPUT_FRAME_SIZE, _OUTPUT_BUFFER_SIZE);
			loadChannelNative(i, channels[i].getAddress(), channels[i].getFrameSize(), channels[i].getBufferSize());
		}
	}

	Thread playeThread;
	@Override
	public void initialize() {
		preInit();		
		Thread decoderThread = new Thread(() -> runDecoder());
		 playeThread = new Thread(() -> runPlayer());
		decoderThread.setDaemon(true);
		decoderThread.start();
		playeThread.setDaemon(true);
		playeThread.start();

	}

	@Override
	public void cleanup() {
		destroyNative();
	}

	native void initNative();
	native void updateNative();
	native void destroyNative();
	native void connectSourceNative(int channelId, int length, long sourceAddr);
	native void disconnectSourceNative(int channelId);
	
	/**
	 * @param addr Output buffer address
	 * @param frameSize samples per frame
	 * @param bufferSize total number of frames in this buffer
	 */
	native void loadChannelNative(int id,long addr,int frameSize,int bufferSize);


	public void connectSource(F32leAudioData audioData, int channelId) {
		System.out.println("Connect source [" + audioData.getAddress() + "] of size " + audioData.getSizeInSamples()
				+ " samples, to channel " + channelId);
		int length = audioData.getSizeInSamples();
		long addr = audioData.getAddress();

		channels[channelId].reset();
		connectSourceNative(channelId, length,addr);
	}

	
	public void connectSourceRaw(int channelId, int length, ByteBuffer source) {
		long addr = DirectBufferUtils.getAddr(source);
		connectSourceNative(channelId, length, addr);
		channels[channelId].reset();
	}

	public void disconnectSourceRaw(int channelId) {
		disconnectSourceNative(channelId);
	}

	public void attachPlayer(PhononPlayer player) {
		enqueuedPlayers.add(player);
	}	

	public void runPlayer() {
		while (true) {
			while(!enqueuedPlayers.isEmpty()) {
				players.add(enqueuedPlayers.poll());
			}

			int stalling = players.size();
			for (PhononPlayer player : players) {
				byte res = player.playLoop();
				if (res == 0) {
					stalling--;
				}
			}

			if (stalling == players.size()) {
				try {
					synchronized(playeThread){
					playeThread.wait();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		// 	try{
		// 	Thread.sleep(10);
		// 	} catch (Exception e) {
		// 	}
		}
	}


	
	

	// long UPDATE_RATE = 50* 1000000l;
	public void runDecoder() {
		double deltaS=  1./(44100 / _OUTPUT_FRAME_SIZE) ;
		long updateRateNanos = CLOCK.getExpectedTimeDelta(deltaS);
     
		System.out.println("Updates per S " + deltaS 
				+ " expected delta " + updateRateNanos);
		
		long startTime = 0;
		while (true) {
			startTime = CLOCK.measure();
		
			updateNative();
			synchronized(playeThread){
				playeThread.notify();
				}

			try {
				// Thread.sleep((int)deltaS * 1000);
				WAIT_MODE.wait(CLOCK, startTime, updateRateNanos);
				// if(!WAIT_MODE.wait(CLOCK, startTime, expectedTimeDelta))System.err.println("FIXME: Phonon is taking too long");
				} catch (Exception e) {
					e.printStackTrace();
				}
		

		}
	}


    private F32leAudioData toF32leData(AudioData d) {
        F32leAudioData o=conversionCache.get(d);
        if (o == null) {
            o = new F32leAudioData(d);
            conversionCache.put(d,o);
        }
		return o;
	}




	
	@Override
	public void setListener(Listener listener) {
		
	}

	@Override
	public void setEnvironment(Environment env) {
		
	}

	@Override
    public void playSourceInstance(AudioSource src) {
        F32leAudioData data=toF32leData(src.getAudioData());

		
	}

	@Override
    public void playSource(AudioSource src) {
        F32leAudioData data=toF32leData(src.getAudioData());

		
	}

	@Override
	public void pauseSource(AudioSource src) {
		F32leAudioData data=toF32leData(src.getAudioData());

	}

	@Override
	public void stopSource(AudioSource src) {
		
	}

	@Override
	public void updateSourceParam(AudioSource src, AudioParam param) {
		
	}

	@Override
	public void updateListenerParam(Listener listener, ListenerParam param) {
		
	}

	@Override
	public float getSourcePlaybackTime(AudioSource src) {
		return 0;
	}

	@Override
	public void deleteFilter(Filter filter) {
		
	}

	@Override
	public void deleteAudioData(AudioData ad) {
		
	}



	@Override
	public void update(float tpf) {
		
	}

	@Override
	public void pauseAll() {
		
	}

	@Override
	public void resumeAll() {
		
	}


    
}