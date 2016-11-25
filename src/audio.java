import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.SwingWorker;

/**
 * This class creates GUI and implements methods for audio recording
 * @author Pavel Dusek
 */
public class audio implements Runnable {
    private static final float SAMPLE_RATE = 8000.0f; //8kHz
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
    private ByteArrayOutputStream out;
    private AudioRecorderTask audioRecorderTask;
    private AudioFormat format = new AudioFormat( SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);

    
    public static void main(String[] args) {
        audio aud = new audio();
        
    }
    
    public void run() {
    	try {
        	System.out.println("RECORDING");
        	record();
        	Thread.sleep(5000);
            System.out.println("Stopping");
            stop();
        	Thread.sleep(2000);
            System.out.println("PLAYING");
            play();
            System.out.println("finish audio");
        } catch (InterruptedException e) {
        	return;
        }
    }

    private void play() {
        byte[] audio = out.toByteArray();
        InputStream in = new ByteArrayInputStream(audio);
        AudioInputStream ais = new AudioInputStream(in, format, audio.length / format.getFrameSize());
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        try {
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(format);
            speaker.start();
            int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
            byte[] buffer = new byte[bufferSize];
            int count;
            while ((count = ais.read(buffer, 0, buffer.length)) != -1) {
                if (count > 0) {
                    speaker.write(buffer, 0, count);
                }
            }
            speaker.drain();
            speaker.close();
        } catch (Exception excp) {
            excp.printStackTrace();
        }
        
    }

    private void stop() {
        audioRecorderTask.cancel(true);
        audioRecorderTask = null;
    }

    private void record() {
        (audioRecorderTask = new AudioRecorderTask()).execute();
    }

    private class AudioRecorderTask extends SwingWorker<Void,byte[]> {

        @Override
        protected Void doInBackground() {
            TargetDataLine microphone;
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Error! Audio System line not supported.");
            } else {
                //TODO open mixer's getLine()?
                try {
                    microphone = AudioSystem.getTargetDataLine(format);
                    microphone.open(format);
                    out = new ByteArrayOutputStream();
                    int numBytesRead;
                    byte[] data = new byte[microphone.getBufferSize()/5];
                    microphone.start();
                    while (!isCancelled()) {
                        numBytesRead = microphone.read(data, 0, data.length);
                        out.write(data, 0, numBytesRead);
                    }
                    out.close();
                } catch (Exception excp) {
                    System.out.println("Error! Could not open Audio System line!");
                    excp.printStackTrace();
                }
            }
            return null;
        }
    }
}