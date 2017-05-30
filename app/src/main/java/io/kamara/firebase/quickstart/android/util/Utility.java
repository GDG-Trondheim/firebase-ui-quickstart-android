package io.kamara.firebase.quickstart.android.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import io.kamara.firebase.quickstart.android.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ernestkamara on 29/05/17.
 */

public class Utility {
    public static final int RC_SIGN_IN = 123;
    public static final int READ_EXTERNAL_STORAGE_REQUEST = 224;

    public interface SignOutDelegate {
        void onSignOut();
    }

    public static void requestStoragePermission(FragmentActivity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_REQUEST);
    }

    public static void signOut(FragmentActivity fragmentActivity, final SignOutDelegate delegate) {
        AuthUI.getInstance()
              .signOut(fragmentActivity)
              .addOnCompleteListener(new OnCompleteListener<Void>() {
                  public void onComplete(@NonNull Task<Void> task) {
                      delegate.onSignOut();
                  }
              });
    }

    public static void requestSignIn(FragmentActivity activity) {
        List<AuthUI.IdpConfig> idpConfigs = Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                          new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
        activity.startActivityForResult(AuthUI.getInstance()
                                     .createSignInIntentBuilder()
                                     .setProviders(idpConfigs)
                                     .setTheme(R.style.AppTheme)
                                     .setLogo(R.mipmap.ic_launcher_round)
                                     .setAllowNewEmailAccounts(true)
                                     .setIsSmartLockEnabled(false)
                                     .build(),
                               RC_SIGN_IN);
    }

    public static Intent newImagePickerIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return intent;
    }

    public static boolean hasReadStoragePermission(Context context) {
        int permission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean shouldShowReadStorageRequestPermissionRationale(AppCompatActivity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
    }
}
