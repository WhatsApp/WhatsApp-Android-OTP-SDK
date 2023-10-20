/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.whatsapp.otp.android.sdk;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import com.whatsapp.otp.android.sdk.enums.WhatsAppClientType;
import java.util.List;

/**
 * Class used to build an intent to send to WhatsApp
 *
 * Extends this class if you want to change anything
 */
public class WhatsAppOtpIntentBuilder {

  private static final String TAG = WhatsAppOtpIntentBuilder.class.getSimpleName();

  public static final String CALLER_INFO = "_ci_";
  public static final String SDK_VERSION = "SDK_VERSION";
  public static final String DEFAULT_VERSION = "0.1.0_not_from_manifest";

  private boolean sendSdkVersion;

  public WhatsAppOtpIntentBuilder() {
    sendSdkVersion = true;
  }

  public WhatsAppOtpIntentBuilder(boolean sendSdkVersion) {
    this.sendSdkVersion = sendSdkVersion;
  }

  /**
   * Create a intent to WhatsApp
   * @param context application context
   * @param type consumer or business
   * @return an intent to be sent to WhatsApp.
   */
  @NonNull
  public synchronized Intent create(final @NonNull Context context,
      final @NonNull WhatsAppClientType type) {
    if (type == null) {
      throw new NullPointerException("WhatsApp application type must be defined.");
    }
    if (context == null) {
      throw new NullPointerException("Context cannot be null.");
    }
    Intent intent = createOtpRequestedIntentForWhatsApp(context, type.getPackageName());
    addPendingIntentForOtp(context, intent);
    return intent;
  }

  @NonNull
  private Intent createOtpRequestedIntentForWhatsApp(final Context context,
      final String whatsAppPackageName) {
    Intent listenIntent = new Intent();
    listenIntent.setPackage(whatsAppPackageName);
    listenIntent.setAction("com.whatsapp.otp.OTP_REQUESTED");
    if (BuildConfig.DEBUG) {
      try {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(whatsAppPackageName,
            PackageManager.GET_ACTIVITIES);
        Log.d(TAG, "Package info: " + packageInfo.toString());
        List<ResolveInfo> resolveInfoList = packageManager.queryBroadcastReceivers(listenIntent, 0);
        resolveInfoList.forEach(item -> Log.d(TAG, item.toString()));
      } catch (PackageManager.NameNotFoundException exception) {
        Log.i(TAG, "Package " + whatsAppPackageName
            + " not found. Did add it to <queries> section on the  manifest file?", exception);
      }
    }
    return listenIntent;
  }

  private void addPendingIntentForOtp(Context context, Intent intent) {
    int flag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0;
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flag);
    Bundle extras = intent.getExtras();
    if (extras == null) {
      extras = new Bundle();
    }
    extras.putParcelable(CALLER_INFO, pendingIntent);
    if (sendSdkVersion) {
      final String implementationVersionFromManifest = getSdkVersion();
      extras.putString(SDK_VERSION,
          implementationVersionFromManifest);
    }
    intent.putExtras(extras);
  }

  private String getSdkVersion() {
    final Package aPackage = this.getClass().getPackage();
    // If the manifest is absent, such as when running unit tests, the DEFAULT_VERSION will be provided
    final String versionFromPackage = aPackage != null ? aPackage.getImplementationVersion() : null;
    return versionFromPackage != null ? versionFromPackage : DEFAULT_VERSION;
  }
}
