package com.example.safetyapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EmergencyPhotoPoster {

    private final Activity activity;
    private Uri capturedImageUri;
    private File imageFile;

    public EmergencyPhotoPoster(Activity activity) {
        this.activity = activity;
    }

    public void capturePhoto() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            imageFile = createImageFile();
            capturedImageUri = FileProvider.getUriForFile(
                    activity,
                    activity.getPackageName() + ".provider",
                    imageFile
            );
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
            activity.startActivityForResult(cameraIntent, 200); // Handle in onActivityResult
        } catch (IOException e) {
            Toast.makeText(activity, "Failed to open camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("SOS_IMG_" + timeStamp, ".jpg", storageDir);
    }

    public void postToFacebook(String message) {
        if (capturedImageUri == null) {
            Toast.makeText(activity, "No image to post", Toast.LENGTH_SHORT).show();
            return;
        }

        SharePhoto photo = new SharePhoto.Builder()
                .setImageUrl(capturedImageUri)
                .build();

        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
//                .setPhotos(message)
                .build();

        ShareDialog dialog = new ShareDialog(activity);
        dialog.show(content);
    }

    public Uri getCapturedImageUri() {
        return capturedImageUri;
    }

    public File getImageFile() {
        return imageFile;
    }
}
