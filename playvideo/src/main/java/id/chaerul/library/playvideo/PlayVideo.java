package id.chaerul.library.playvideo;

import android.app.DownloadManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlayVideo extends FrameLayout {
    private SeekBar seekbar;
    private ImageView image_download;
    private TextView text_size_video;
    private ImageView thumnb_video;
    private VideoView videoView;
    private LinearLayout linearLayout;
    private ImageView image_play;
    private String urlVideo;
    private int progress = 0;
    private Paint paintBg;
    private Paint paintFg;

    public PlayVideo(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context, attributeSet);
    }

    private void init(Context context, AttributeSet attributeSet) {
        inflate(context, R.layout.play_video, this);
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.PlayVideo);

        int thumbnailRes = a.getResourceId(R.styleable.PlayVideo_src, -1);
        int errorRes = a.getResourceId(R.styleable.PlayVideo_srcError, -1);

        if (thumbnailRes != -1) {
            thumnb_video.setImageResource(thumbnailRes);
        } else if (errorRes != -1) {
            thumnb_video.setImageResource(errorRes);
        }

        a.recycle();

        seekbar = findViewById(R.id.seekBar);
        image_download = findViewById(R.id.image_download);
        text_size_video = findViewById(R.id.text_size_video);
        thumnb_video = findViewById(R.id.image_video);
        videoView = findViewById(R.id.videoView);
        linearLayout = findViewById(R.id.linearLayout);
        image_play = findViewById(R.id.image_play);
        paintBg = new Paint();
        paintBg.setColor(Color.LTGRAY);
        paintBg.setStyle(Paint.Style.STROKE);
        paintBg.setStrokeWidth(8);
        paintBg.setAntiAlias(true);
        Handler handler = new Handler();
        Runnable updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (videoView.isPlaying()) {
                    int currentPosition = videoView.getCurrentPosition();
                    int totalDuration = videoView.getDuration();
                    seekbar.setMax(totalDuration);
                    seekbar.setProgress(currentPosition);
                }
                handler.postDelayed(this, 500);
            }
        };
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(progress);
                    image_play.setVisibility(View.GONE);
                    thumnb_video.setVisibility(View.GONE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // pause update saat drag
                handler.removeCallbacks(updateSeekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.postDelayed(updateSeekBar, 0); // lanjut update lagi
            }
        });

        handler.postDelayed(updateSeekBar, 0);

        paintFg = new Paint();
        paintFg.setColor(Color.GREEN); // warna progress
        paintFg.setStyle(Paint.Style.STROKE);
        paintFg.setStrokeWidth(8);
        paintFg.setAntiAlias(true);

        image_play.setOnClickListener(v -> {
            if (!videoView.isPlaying()) {
                videoView.start();
                image_play.setVisibility(View.GONE);
                thumnb_video.setVisibility(View.GONE);
            }
        });

        image_download.setOnClickListener(v -> {
            if (urlVideo != null && !urlVideo.isEmpty()) {
                simulateProgress(); // tambahkan ini
                downloadVideo(getContext(), urlVideo);
            }
        });

    }

    public void setVideoDownload(String urlVideo, String fileName, OnCallback callback) {
        this.urlVideo = urlVideo;
        new Thread(() -> {
            String size = getSizeVideo(urlVideo);
            post(() -> text_size_video.setText("Size " + size));
        }).start();

        File mediaDir = new File(
                Environment.getExternalStorageDirectory(),
                "Android/media/id.chaerul.library.video"
        );


        if (!mediaDir.exists()) {
            if (!mediaDir.mkdirs()) {
                callback.onError("Gagal buat folder penyimpanan.");
                return;
            }
        }

        File videoFile = new File(mediaDir, fileName);

        if (videoFile.exists()) {
            callback.onSuccess(videoFile.getAbsolutePath(), "File sudah tersedia.");
            return;
        }

        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlVideo));
            request.setTitle("Download Video");
            request.setDescription("Sedang mendownload video...");
            request.setDestinationUri(Uri.fromFile(videoFile));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                manager.enqueue(request);
                callback.onSuccess(videoFile.getAbsolutePath(), "Download dimulai...");
            } else {
                callback.onError("DownloadManager tidak tersedia.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError("Terjadi kesalahan: " + e.getMessage());
        }
    }


    public interface OnCallback {
        void onSuccess(String url, String message);
        void onError(String message);
    }


    private String getSizeVideo(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("HEAD");
            conn.connect();
            int length = conn.getContentLength(); // in bytes

            if (length < 1024) {
                return length + " B";
            } else if (length < 1024 * 1024) {
                return String.format("%.2f KB", length / 1024.0);
            } else if (length < 1024 * 1024 * 1024) {
                return String.format("%.2f MB", length / (1024.0 * 1024));
            } else {
                return String.format("%.2f GB", length / (1024.0 * 1024 * 1024));
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }


    private void setThumbnailVideo(String url) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(url, new java.util.HashMap<>());
        Bitmap bitmap = retriever.getFrameAtTime(1000000);
        thumnb_video.setImageBitmap(bitmap);
        retriever.release();
    }


    private void downloadVideo(Context context, String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Downloading video");
        request.setDescription("Saving video...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "video_download.mp4");

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    private void simulateProgress() {
        new Thread(() -> {
            for (int i = 0; i <= 100; i++) {
                progress = i;
                postInvalidate(); // trigger redraw
                try {
                    Thread.sleep(50); // delay buat simulasi
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int padding = 10;
        int size = Math.min(getWidth(), getHeight()) - padding * 2;
        RectF rect = new RectF(padding, padding, padding + size, padding + size);

        // background circle
        canvas.drawArc(rect, 0, 360, false, paintBg);

        // foreground progress
        float angle = 360 * progress / 100f;
        canvas.drawArc(rect, -90, angle, false, paintFg);
    }
}