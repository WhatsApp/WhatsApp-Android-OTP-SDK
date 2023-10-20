/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.whatsapp.otp.android.sdk;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import androidx.annotation.NonNull;
import com.whatsapp.otp.android.sdk.enums.WhatsAppClientType;
import java.util.List;

/**
 * Class responsible for handling intents sent to WhatsApp
 */
public class WhatsAppOtpHandler {

  private final WhatsAppOtpIntentBuilder whatsAppOtpIntentBuilder;

  /**
   * Default constructor
   */
  public WhatsAppOtpHandler() {
    this.whatsAppOtpIntentBuilder = new WhatsAppOtpIntentBuilder();
  }

  /**
   * Constructor which lets developer define if sdk version should be sent in the request.

   * @param sendSdkVersion if the sdk version should be sent
   */
  public WhatsAppOtpHandler(boolean sendSdkVersion) {
    this.whatsAppOtpIntentBuilder = new WhatsAppOtpIntentBuilder(sendSdkVersion);
  }

  /**
   * Constructor. Use this constructor if you want to provide your WhatsAppOtpIntentBuilder
   * implementation
   *
   * @param whatsAppOtpIntentBuilder builder responsible for creating Intent to WhatsApp.
   */
  public WhatsAppOtpHandler(WhatsAppOtpIntentBuilder whatsAppOtpIntentBuilder) {
    this.whatsAppOtpIntentBuilder = whatsAppOtpIntentBuilder;
  }

  /**
   * <p>Send and otp intent to both WhatsApp consumer and business app. This is the handshake step
   * for WhatsApp authentication messages.
   *
   * <p>Basically an intent to WhatsApp is created with a pending intent. This pending intent is
   * used by WhatsApp merely to validate the creator package name against the package name defined
   * in the Authentication message template.</p>
   *
   * <p>This intent is sent to WhatsApp using the "com.whatsapp.otp.OTP_REQUESTED" action</p>
   *
   * <p>After receiving this handshake, WhatsApp will enable the autofill feature based on the
   * message template configuration</p>
   *
   * @param context application context to broadcast the intent to WhatsApp.
   */
  public void sendOtpIntentToWhatsApp(final @NonNull Context context) {
    if (context == null) {
      throw new NullPointerException("Context cannot be null");
    }
    sendOtpIntentToWhatsApp(context, WhatsAppClientType.CONSUMER);
    sendOtpIntentToWhatsApp(context, WhatsAppClientType.BUSINESS);
  }

  /**
   * <p>Send and otp intent to WhatsApp. This is the handshake step for WhatsApp authentication
   * message. Basically a intent to WhatsApp is created with a pending intent. This pending intent
   * is used by WhatsApp merely to validate the creator package name against the package name
   * defined in the Authentication message template.</p>
   *
   * <p>This intent is sent to WhatsApp using the "com.whatsapp.otp.OTP_REQUESTED" action</p>
   *
   * <p>After receiving this handshake, WhatsApp will enable the autofill feature based on the
   * message template configuration</p>
   *
   * <p>You can choose to send to WhatsApp consumer or business, thought is recommended to send to
   * both using {@link #sendOtpIntentToWhatsApp(Context) sendOtpIntentToWhatsApp} method</p>
   *
   * @param context application context to broadcast the intent to WhatsApp.
   * @param type    You can define wither consumer or business application.
   * @return
   */
  public Intent sendOtpIntentToWhatsApp(final @NonNull Context context,
      final @NonNull WhatsAppClientType type) {
    Intent intent = this.whatsAppOtpIntentBuilder.create(context, type);
    context.sendBroadcast(intent);
    return intent;
  }

  /**
   * Checks if either WhatsApp consumer or business has support for the otp handshake if installed.
   * <p>
   * The handshake support is provided by the presence of a receiver on WhatsApp with the following
   * action: "com.whatsapp.otp.OTP_REQUESTED"
   * </p>
   * <p>
   * If WhatsApp is not installed, this function will return false.
   * <p/>
   *
   * @param context application context
   * @return true if the receiver is available on either WhatsApp consumer or business, false
   * otherwise
   */
  public boolean isWhatsAppOtpHandshakeSupported(final @NonNull Context context) {
    return isWhatsAppOtpHandshakeSupported(context, WhatsAppClientType.CONSUMER)
        || isWhatsAppOtpHandshakeSupported(context, WhatsAppClientType.BUSINESS);
  }

  /**
   * Checks if WhatsApp has support for the otp handshake.
   * <p>
   * The handshake support is provided by the presence of a receiver on WhatsApp with the following
   * action: "com.whatsapp.otp.OTP_REQUESTED"
   * </p>
   * <p>
   * If WhatsApp is not installed, this function will return false.
   * <p/>
   * <p>
   * You need to have the following definition in your AndroidManifest for this function to work:
   * </p>
   * <pre>
   *     &#60;queries&#62;
   *         &#60;package android:name="com.whatsapp"/&#62;
   *         &#60;package android:name="com.whatsapp.w4b"/&#62;
   *     &#60;/queries&#62;
   * </pre>
   *
   * @param context application context
   * @param type    WhatsApp client to be checked, can be either consumer or business
   * @return true if the receiver is available on either WhatsApp consumer or business, false
   * otherwise
   */
  public boolean isWhatsAppOtpHandshakeSupported(final @NonNull Context context,
      final @NonNull WhatsAppClientType type) {
    final Intent intent = new Intent();
    intent.setPackage(type.getPackageName());
    intent.setAction("com.whatsapp.otp.OTP_REQUESTED");
    PackageManager packageManager = context.getPackageManager();
    List<ResolveInfo> receivers = packageManager.queryBroadcastReceivers(intent, 0);
    return !receivers.isEmpty();
  }


  /**
   * Check if either WhatsApp consumer or business app is installed on the device
   * <p>
   * You need to have the following definition in your AndroidManifest for this function to work
   * properly:
   * </p>
   * <pre>
   *     &#60;queries&#62;
   *         &#60;package android:name="com.whatsapp"/&#62;
   *         &#60;package android:name="com.whatsapp.w4b"/&#62;
   *     &#60;/queries&#62;
   * </pre>
   *
   * @param context application context
   * @return true if WhatsApp is installed or false otherwise
   */
  public boolean isWhatsAppInstalled(final @NonNull Context context) {
    return isWhatsAppInstalled(context, WhatsAppClientType.CONSUMER)
        || isWhatsAppInstalled(context, WhatsAppClientType.BUSINESS);
  }

  /**
   * Check if WhatsApp is installed on the device.
   * <p>
   * You need to have the following definition in your AndroidManifest for this function to work
   * properly:
   * </p>
   * <pre>
   *     &#60;queries&#62;
   *         &#60;package android:name="com.whatsapp"/&#62;
   *         &#60;package android:name="com.whatsapp.w4b"/&#62;
   *     &#60;/queries&#62;
   * </pre>
   *
   * @param context application context
   * @param type    WhatsApp type, can be either consumer or business
   * @return true if WhatsApp is installed or false otherwise
   */
  public boolean isWhatsAppInstalled(final @NonNull Context context,
      final @NonNull WhatsAppClientType type) {
    try {
      PackageManager packageManager = context.getPackageManager();
      packageManager.getPackageInfo(type.getPackageName(), 0);
      return true;
    } catch (PackageManager.NameNotFoundException e) {
      return false;
    }
  }
}
