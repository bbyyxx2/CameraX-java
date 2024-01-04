package com.bbyyxx2.camerax;

import static androidx.camera.core.ImageCapture.FLASH_MODE_AUTO;
import static androidx.camera.core.ImageCapture.FLASH_MODE_OFF;
import static androidx.camera.core.ImageCapture.FLASH_MODE_ON;

import android.Manifest;
import android.animation.Animator;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bbyyxx2.camerax.databinding.ActivityCameraxBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CameraXActivity extends AppCompatActivity {

    private ActivityCameraxBinding binding;

    // 相机进程提供者
    private ProcessCameraProvider cameraProvider;

    // 相机进程提供者 Future，相机初始化完成后返回相机进程提供者实例化对象
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    // 预览组件
    private PreviewView previewView;
    // 相机模组选择器
    private CameraSelector cameraSelector;
    // 预览对象
    private Preview preview;
    // 相机对象
//    private Camera camera;
    private ImageCapture imageCapture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraxBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initListener();
        initPermission();
    }

    private void initListener() {
        binding.btnFlash.setOnClickListener(v -> {
            Animator animator = ViewAnimationUtils.createCircularReveal(binding.llFlashOptions,
                    Math.round(v.getX()), Math.round(v.getY()), 0f, getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? v.getWidth() : v.getHeight());
            animator.setDuration(500).addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animation) {
                    binding.llFlashOptions.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(@NonNull Animator animation) {

                }

                @Override
                public void onAnimationCancel(@NonNull Animator animation) {

                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animation) {

                }
            });
            animator.start();
        });

        binding.btnFlashOn.setOnClickListener(v -> {
            closeFlashAndSelect(FLASH_MODE_ON);
        });

        binding.btnFlashOff.setOnClickListener(v -> {
            closeFlashAndSelect(FLASH_MODE_OFF);
        });

        binding.btnFlashAuto.setOnClickListener(v -> {
            closeFlashAndSelect(FLASH_MODE_AUTO);
        });
    }
    private int flashMode = FLASH_MODE_OFF;
    private void closeFlashAndSelect(int flash){
        ImageButton v = binding.btnFlash;
        Animator animator = ViewAnimationUtils.createCircularReveal(binding.llFlashOptions,
                Math.round(v.getX()) + v.getWidth() / 2, Math.round(v.getY()) + v.getHeight() / 2,
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? v.getWidth() : v.getHeight(),
                0f);
        animator.setDuration(500).addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                flashMode = flash;
                switch (flash){
                    case FLASH_MODE_OFF:
                        v.setImageResource(R.drawable.ic_flash_off);
                        break;
                    case FLASH_MODE_ON:
                        v.setImageResource(R.drawable.ic_flash_on);
                        break;
                    case FLASH_MODE_AUTO:
                        v.setImageResource(R.drawable.ic_flash_auto);
                        break;
                }

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                binding.llFlashOptions.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });
        animator.start();
    }

    private static final int MY_PERMISSIONS_REQUEST = 1;
    private void initPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);
        } else {
            // Permission has already been granted
            Log.d("CameraXActivity1111", "Permission has already been granted");
            startCamera();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                Log.d("CameraXActivity1111", "Permission is granted");
                startCamera();
            } else {
                Toast.makeText(this,
                        "Permission is revoked.",
                        Toast.LENGTH_SHORT).show();
                // Permission is revoked
                Log.d("CameraXActivity1111", "Permission is revoked");
            }
        }
    }

    private void startCamera(){
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            preview = new Preview.Builder().build();
            preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

            imageCapture = new ImageCapture.Builder().build();
            imageCapture.setFlashMode(flashMode);

            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();
            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), image -> {

            });

            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

            try {

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
            } catch (Exception e) {
                Log.e("CameraXActivity1111", "Exception: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

}
