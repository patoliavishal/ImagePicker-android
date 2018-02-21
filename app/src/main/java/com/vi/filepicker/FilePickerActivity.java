package com.vi.filepicker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.vi.filepicker.utils.PathUtil;

import java.io.File;
import java.net.URISyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.vi.filepicker.utils.PathUtil.getTemporaryCameraFile;

/**
 * Created by VickyBoy on 21-Feb-18.
 */
public class FilePickerActivity extends RuntimePermissionsActivity {

    @BindView(R.id.file_ivHolder)
    ImageView mFileIvHolder;
    private Uri mFileUri;
    private static final int REQUEST_PERMISSIONS_CAMERA = 101;
    private static final int REQUEST_PERMISSIONS_GALLERY = 102;
    private static final int PICK_CAMERA_REQUEST = 103;
    private static final int PICK_IMAGE_REQUEST = 104;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);
        ButterKnife.bind(this);
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CAMERA:
                takeCamera();
                break;
            case REQUEST_PERMISSIONS_GALLERY:
                selectGallery();
                break;
        }
    }

    /**
     * Take image using camera
     */
    private void takeCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = getTemporaryCameraFile();
            mFileUri = FileProvider.getUriForFile(getApplicationContext(),
                    getPackageName() + ".provider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                takePictureIntent.setClipData(ClipData.newRawUri("", mFileUri));
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            startActivityForResult(takePictureIntent, PICK_CAMERA_REQUEST);
        }
    }

    /**
     * Select image using gallery
     */
    private void selectGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        if (Build.VERSION.SDK_INT <= 23) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image_gallery)), PICK_IMAGE_REQUEST);
    }

    @OnClick({R.id.file_btnCamera, R.id.file_btnGallery})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.file_btnCamera:
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestAppPermissions(new
                            String[]{Manifest.permission.CAMERA}, R.string.runtime_camera_permission, REQUEST_PERMISSIONS_CAMERA);
                } else {
                    takeCamera();
                }
                break;
            case R.id.file_btnGallery:
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestAppPermissions(new
                            String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, R.string.runtime_gallery_permission, REQUEST_PERMISSIONS_CAMERA);
                } else {
                    selectGallery();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            try {
                loadImage(new File(PathUtil.getPath(this, data.getData())).getAbsolutePath());
            } catch (URISyntaxException aE) {
                aE.printStackTrace();
            }
        } else if (requestCode == PICK_CAMERA_REQUEST && resultCode == RESULT_OK) {
            Intent aIntentData = new Intent();
            aIntentData.setData(Uri.fromFile(PathUtil.getLastUsedCameraFile()));
            mFileUri = aIntentData.getData();
            loadImage(mFileUri.getPath());
        }
    }

    /**
     * Using Glide to load image
     *
     * @param url
     */
    void loadImage(String url) {
        Glide.with(this)
                .load(url)
                .into(mFileIvHolder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_item_share:
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, "https://github.com/patoliavishal/FilePicker-android");
                startActivity(Intent.createChooser(share, "Share Github Link"));
                break;
            case R.id.menu_item_thanks:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("I warmly thanks Mr. Sandip Kalola for helping.")
                        .setCancelable(false)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.setTitle("Thank you");
                alert.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
