package io.kamara.firebase.quickstart.android.demo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.kamara.firebase.quickstart.android.R;
import io.kamara.firebase.quickstart.android.util.Utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DemoActivity extends AppCompatActivity {
    private final int SELECT_PHOTO = 1;

    @BindView(R.id.authBtn)
    Button mAuthButton;

    @BindView(R.id.mainContainer)
    RelativeLayout mMainContainer;

    @BindView(R.id.photoImageView)
    ImageView mPhotoImageView;

    @BindView(R.id.progressBar)
    TextView mProgressTextView;

    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        ButterKnife.bind(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAuthUI();
    }

    @OnClick(R.id.authBtn)
    protected void authenticateUser() {
        if (mFirebaseAuth.getCurrentUser() != null) {
            Utility.signOut(this, new Utility.SignOutDelegate() {
                @Override
                public void onSignOut() {
                    showSnackBar("Sign-out successful");
                    mAuthButton.setText("Sign-In");
                }
            });
        } else {
            Utility.requestSignIn(this);
        }
    }

    @OnClick(R.id.photoImageView)
    protected void selectPhoto () {
        boolean storagePermission = Utility.hasReadStoragePermission(this);
        if (storagePermission) {
            startPhotoPickIntent();
        } else {
            Utility.requestStoragePermission(this);
        }
    }

    @OnClick(R.id.uploadBtn)
    protected void uploadPhoto () {
        if (mFirebaseAuth.getCurrentUser()!= null) {
            mPhotoImageView.setDrawingCacheEnabled(true);
            mPhotoImageView.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable)mPhotoImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            uploadStory(baos.toByteArray());
        } else {
            showSnackBar("Sing-In required, please Sign-In");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Utility.READ_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPhotoPickIntent();
            } else {
                if (Utility.shouldShowReadStorageRequestPermissionRationale(this)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("Storage permission is needed to select photo on your device!")
                            .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Utility.requestStoragePermission(DemoActivity.this);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showPermissionDeniedSnackBar();
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case Utility.RC_SIGN_IN:
                IdpResponse response = IdpResponse.fromResultIntent(data);
                handleSignInResponse(resultCode, response);
                break;
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    mPhotoImageView.setImageBitmap(null);
                    Uri photoUri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                        mPhotoImageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showSnackBar("Error picking photo");
                    }

                } else {
                    showSnackBar("Error picking photo");
                }
                break;
        }
    }

    private void handleSignInResponse(int resultCode, IdpResponse response) {
        if (resultCode == ResultCodes.OK) {
            updateAuthUI();
            return;
        } else {
            if (response == null) {
                showSnackBar("Sign in cancelled");
                return;
            }

            if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackBar("No internet connection");
                return;
            }

            if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                showSnackBar("Unknown error");
                return;
            }
        }

        showSnackBar("Unknown sign_in response");
    }


    private void updateAuthUI(){
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        String snackBarText;
        String authButtontext;
        if (currentUser != null) {
            snackBarText = "Welcome, " + currentUser.getDisplayName();
            authButtontext = "Sign-Out";
        } else {
            snackBarText = "You are not Signed-In";
            authButtontext = "Sign-In";
        }
        mAuthButton.setText(authButtontext);
        showSnackBar(snackBarText);
    }

    private void startPhotoPickIntent() {
        Intent intent = Utility.newImagePickerIntent();
        startActivityForResult(Intent.createChooser(intent, "Select photo"), SELECT_PHOTO);
    }

    private void showSnackBar(String message) {
        Snackbar.make(mMainContainer, message, Snackbar.LENGTH_LONG).show();
    }

    private void showPermissionDeniedSnackBar() {
        Snackbar snackbar = Snackbar.make(mMainContainer, "Storage permission required!", Snackbar.LENGTH_LONG);
        snackbar.setAction("Settings", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Context context = mMainContainer.getContext();
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                context.startActivity(intent);
            }
        });
        snackbar.show();
    }

    private void uploadStory(byte[] data){
        mProgressTextView.setText("");

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        String filename = String.valueOf(System.currentTimeMillis());

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference photoReference = storageReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                          .child(filename);

        photoReference.putBytes(data, metadata)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                saveStory(taskSnapshot);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showSnackBar("Failed to upload photo");
                            }
                        })
                      .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                          @Override
                          public void onProgress(UploadTask.TaskSnapshot snapshot) {
                              int progress = (int) ((100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
                              mProgressTextView.setText("Upload is " + progress + "% done");
                          }
                      });
    }

    private void saveStory(UploadTask.TaskSnapshot snapshot) {
        DatabaseReference stories = FirebaseDatabase.getInstance().getReference("stories").push();
        Story story = new Story(snapshot.getDownloadUrl().toString(), snapshot.getMetadata().getPath(), "My Awesome Photo", null);
        stories.setValue(story)
               .addOnSuccessListener(new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void aVoid) {
                       new AlertDialog.Builder(DemoActivity.this)
                               .setTitle("Congratulation!")
                               .setMessage("Photo successfully saved to Database.\nGo to the Firebase console and verify photo upload in storage, " +
                                                   "new story and user added to database & authentication respectively!\nHappy Coding")
                               .setPositiveButton("Exit Demo", new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialog, int which) {
                                       finish();
                                       dialog.dismiss();
                                   }
                               })
                               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                   @Override
                                   public void onClick(DialogInterface dialog, int which) {
                                       dialog.dismiss();
                                   }
                               })
                               .show();
                   }
               })
               .addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       showSnackBar("Failed to save photo to Database");
                   }
               });
    }

    public static class Story {
        public String downloadUrl;
        public String title;
        public String uuid;
        public String filePath;

        public Story() {
            // Default constructor required for calls to DataSnapshot.getValue(Story.class)
        }

        public Story(String downloadUrl, String filePath, String title, String uuid) {
            this.downloadUrl = downloadUrl;
            this.filePath = filePath;
            this.title = title;
            this.uuid = uuid;
        }
    }
}
