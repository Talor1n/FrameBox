package net.talor1n.framebox.ui;

import lombok.extern.log4j.Log4j2;
import net.talor1n.framebox.repository.UserRepository;
import net.talor1n.framebox.util.FFmpegRecorder;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;
import org.bytedeco.opencv.global.opencv_videoio;
import org.bytedeco.opencv.global.opencv_imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Log4j2
public class MainWindow {

    private final JLabel cameraScreen;
    private final JComboBox<String> cameraList;
    private final JButton recordButton;
    private final JButton stopRecordButton;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Camera-Thread");
        t.setDaemon(true);
        return t;
    });
    private final UserRepository repo;
    private final Long userID;

    private volatile boolean running;
    private volatile boolean recording;
    private volatile int selectedCamera;
    private Future<?> cameraTask;

    private FFmpegRecorder recorder;
    private final int recordingWidth = 1280;
    private final int recordingHeight = 720;

    public MainWindow(UserRepository repo, Long userID) {
        this.repo = repo;
        this.userID = userID;

        var frame = new JFrame("Веб-камера — предпросмотр");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setLocationRelativeTo(null);

        cameraScreen = new JLabel("Ожидание камеры...", SwingConstants.CENTER);
        cameraScreen.setPreferredSize(new Dimension(640, 480));

        // Панель управления
        JPanel controlPanel = new JPanel();

        cameraList = new JComboBox<>();
        populateCameraList();
        cameraList.addActionListener(e -> switchCamera(cameraList.getSelectedIndex()));
        controlPanel.add(new JLabel("Выбор камеры:"));
        controlPanel.add(cameraList);

        // Кнопки записи
        recordButton = new JButton("Начать запись");
        stopRecordButton = new JButton("Стоп запись");
        stopRecordButton.setEnabled(false);

        recordButton.addActionListener(e -> startRecording());
        stopRecordButton.addActionListener(e -> stopRecording());

        controlPanel.add(recordButton);
        controlPanel.add(stopRecordButton);

        frame.setLayout(new BorderLayout());
        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(cameraScreen, BorderLayout.CENTER);

        // Обработчик закрытия окна
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                stopRecording();
                stopCamera();
                executor.shutdown();
            }
        });

        frame.setVisible(true);

        log.info("Окно инициализировано, стартуем первую камеру...");
        if (cameraList.getItemCount() > 0) {
            switchCamera(0);
        }
    }

    private void populateCameraList() {
        log.info("Поиск доступных камер...");
        for (int i = 0; i < 5; i++) {
            VideoCapture cap = new VideoCapture(i);
            try {
                if (cap.isOpened()) {
                    cameraList.addItem("Камера " + i);
                    log.info("Найдена камера {}", i);
                }
            } finally {
                cap.release();
            }
        }
        if (cameraList.getItemCount() == 0) {
            log.warn("Камеры не найдены!");
            cameraList.addItem("Нет доступных камер");
        }
    }

    private void switchCamera(int index) {
        log.info("Переключение на камеру {}", index);
        stopRecording();
        stopCamera();
        selectedCamera = index;
        running = true;
        cameraTask = executor.submit(this::startCameraLoop);
    }

    private void startCameraLoop() {
        log.info("Запуск камеры {}", selectedCamera);
        VideoCapture capture = null;
        Mat frameMat = null;
        Mat resizedMat = null;

        try {
            capture = new VideoCapture(selectedCamera);
            if (!capture.isOpened()) {
                log.error("Не удалось открыть камеру {}", selectedCamera);
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(null,
                                "Не удалось открыть камеру: " + selectedCamera,
                                "Ошибка", JOptionPane.ERROR_MESSAGE));
                return;
            }

            double fps = capture.get(opencv_videoio.CAP_PROP_FPS);
            if (fps <= 0) fps = 30.0;

            int width = (int) capture.get(opencv_videoio.CAP_PROP_FRAME_WIDTH);
            int height = (int) capture.get(opencv_videoio.CAP_PROP_FRAME_HEIGHT);

            long frameDelay = (long) (1000 / fps);
            log.info("FPS камеры {}: {}, размер: {}x{}", selectedCamera, fps, width, height);

            frameMat = new Mat();
            resizedMat = new Mat();

            while (running && !Thread.currentThread().isInterrupted()) {
                long start = System.nanoTime();

                if (capture.read(frameMat) && !frameMat.empty()) {
                    // 🔹 запись
                    if (recording && recorder != null) {
                        opencv_imgproc.resize(frameMat, resizedMat,
                                new org.bytedeco.opencv.opencv_core.Size(recordingWidth, recordingHeight));
                        recorder.record(resizedMat);
                    }

                    // 🔹 предпросмотр
                    Mat displayMat = frameMat;
                    if (frameMat.cols() > 800 || frameMat.rows() > 600) {
                        opencv_imgproc.resize(frameMat, resizedMat,
                                new org.bytedeco.opencv.opencv_core.Size(640, 480));
                        displayMat = resizedMat;
                    }

                    BufferedImage img = matToBufferedImage(displayMat);
                    if (img != null) {
                        BufferedImage finalImg = img;
                        SwingUtilities.invokeLater(() -> {
                            cameraScreen.setText(null); // убираем "Ожидание камеры..."
                            cameraScreen.setIcon(new ImageIcon(finalImg));
                        });
                    }
                }

                long elapsed = (System.nanoTime() - start) / 1_000_000;
                long sleepTime = frameDelay - elapsed;
                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Ошибка в цикле камеры", e);
        } finally {
            if (frameMat != null) frameMat.release();
            if (resizedMat != null) resizedMat.release();
            if (capture != null) capture.release();

            if (recording && recorder != null) {
                try {
                    recorder.stop();
                } catch (Exception e) {
                    log.error("Ошибка при остановке записи", e);
                }
                recorder = null;
                recording = false;
                SwingUtilities.invokeLater(() -> {
                    recordButton.setEnabled(true);
                    stopRecordButton.setEnabled(false);
                });
            }

            // 🔹 возвращаем надпись "Ожидание камеры..."
            SwingUtilities.invokeLater(() -> {
                cameraScreen.setIcon(null);
                cameraScreen.setText("Ожидание камеры...");
            });

            log.info("Остановка камеры {}", selectedCamera);
        }
    }

    public void stopCamera() {
        running = false;
        if (cameraTask != null) {
            cameraTask.cancel(true);
            cameraTask = null;
        }
    }

    private void startRecording() {
        if (recording) return;

        try {
            String folder = "videos";
            new File(folder).mkdirs();

            String filename = "capture-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".mp4";
            File file = new File(folder, filename);

            String fullPath = file.getAbsolutePath();

            repo.addVideoToUser(userID, fullPath);

            recorder = new FFmpegRecorder();
            recorder.start(fullPath, recordingWidth, recordingHeight, 30);
            recording = true;

            SwingUtilities.invokeLater(() -> {
                recordButton.setEnabled(false);
                stopRecordButton.setEnabled(true);
            });

            log.info("Начало записи в {} (размер: {}x{})", fullPath, recordingWidth, recordingHeight);

        } catch (Exception e) {
            log.error("Не удалось начать запись", e);
            recording = false;
            if (recorder != null) {
                try {
                    recorder.stop();
                } catch (Exception ex) {
                    log.error("Ошибка при очистке recorder после неудачного старта", ex);
                }
                recorder = null;
            }
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(null,
                            "Не удалось начать запись: " + e.getMessage(),
                            "Ошибка записи", JOptionPane.ERROR_MESSAGE));
        }
    }

    private void stopRecording() {
        if (!recording) return;

        recording = false;
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (Exception e) {
                log.error("Ошибка при остановке записи", e);
            }
            recorder = null;
        }

        SwingUtilities.invokeLater(() -> {
            recordButton.setEnabled(true);
            stopRecordButton.setEnabled(false);
        });

        log.info("Запись остановлена");
    }

    private static BufferedImage matToBufferedImage(Mat mat) {
        if (mat == null || mat.empty()) {
            return null;
        }

        try {
            int type = mat.channels() == 1
                    ? BufferedImage.TYPE_BYTE_GRAY
                    : BufferedImage.TYPE_3BYTE_BGR;
            BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);

            byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            mat.data().get(data);

            return image;
        } catch (Exception e) {
            System.err.println("Ошибка при конвертации Mat в BufferedImage: " + e.getMessage());
            return null;
        }
    }
}