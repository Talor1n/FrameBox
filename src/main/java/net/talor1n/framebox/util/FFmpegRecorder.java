package net.talor1n.framebox.util;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;

public class FFmpegRecorder {
    private FFmpegFrameRecorder recorder;
    private final OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
    private boolean initialized = false;

    public void start(String outputPath, int width, int height, double fps) throws Exception {
        recorder = new FFmpegFrameRecorder(outputPath, width, height);
        recorder.setFormat("mp4");
        recorder.setFrameRate(fps);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setVideoBitrate(2000_000); // 2 Mbps
        recorder.start();
        initialized = true;
    }

    public void record(Mat mat) throws Exception {
        if (mat.cols() != recorder.getImageWidth() || mat.rows() != recorder.getImageHeight()) {
            Mat resized = new Mat();
            org.bytedeco.opencv.global.opencv_imgproc.resize(mat, resized,
                    new org.bytedeco.opencv.opencv_core.Size(recorder.getImageWidth(), recorder.getImageHeight()));
            recorder.record(converter.convert(resized));
            resized.release();
        } else {
            recorder.record(converter.convert(mat));
        }
    }

    public void stop() {
        try {
            if (recorder != null) {
                recorder.stop();
                recorder.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            recorder = null;
            initialized = false;
        }
    }

    public boolean isRecording() {
        return initialized;
    }
}
