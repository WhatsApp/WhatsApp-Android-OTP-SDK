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
import java.util.UUID;

/**
 * Class responsible for handling intents sent to WhatsApp
 */
public class WhatsAppOtpHandler {

  private static final String HANDSHAKE_ID = "request_id";

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
   * <p>Send an OTP intent with unique request identifier to both WhatsApp consumer and business
   * apps. This is the handshake step for WhatsApp authentication messages.</p>
   *
   * <p>An intent is created with a pending intent that WhatsApp uses to validate the creator
   * package name against the package name defined in the Authentication message template. The
   * included request ID allows for validating the authenticity of the received OTP code by
   * correlating it with the specific request that was initiated.</p>
   *
   * <p>This intent is sent to WhatsApp using the "com.whatsapp.otp.OTP_REQUESTED" action</p>
   *
   * <p>After receiving this handshake, WhatsApp will enable the autofill or zero tab feature based
   * on the message template configuration. The request ID helps ensure that the OTP code received
   * corresponds to this specific authentication request.</p>
   *
   * @param context Application context used to broadcast the intent to WhatsApp.
   * @return A randomly generated UUID that was used for this OTP request. Store this value to
   *         validate the authenticity of any received OTP codes.
   * @throws NullPointerException if context is null
   * @since 0.2.0
   */
  public UUID sendOtpIntentToWhatsApp(final @NonNull Context context) {
    if (context == null) {
      throw new NullPointerException("Context cannot be null");
    }
    UUID uuid = UUID.randomUUID();
    sendOtpIntentToWhatsApp(uuid, context, WhatsAppClientType.CONSUMER);
    sendOtpIntentToWhatsApp(uuid, context, WhatsAppClientType.BUSINESS);
    return uuid;
  }

  /**
   * <p>Send an OTP intent with unique request identifier to both WhatsApp consumer and business
   * apps. This is the handshake step for WhatsApp authentication messages.</p>
   *
   * <p>An intent is created with a pending intent that WhatsApp uses to validate the creator
   * package name against the package name defined in the Authentication message template. The
   * included request ID allows for validating the authenticity of the received OTP code by
   * correlating it with the specific request that was initiated.</p>
   *
   * <p>This intent is sent to WhatsApp using the "com.whatsapp.otp.OTP_REQUESTED" action</p>
   *
   * <p>After receiving this handshake, WhatsApp will enable the autofill feature based on the
   * message template configuration. The request ID helps ensure that the OTP code received
   * corresponds to this specific authentication request.</p>
   *
   * @param uuid    A unique identifier for this OTP request. Used for validating the authenticity
   *                of the received OTP code. This same ID will be sent to both consumer and
   *                business WhatsApp applications.
   * @param context Application context used to broadcast the intent to WhatsApp.
   * @return The same UUID that was passed as input.
   * @throws NullPointerException if uuid or context is null
   * @since 0.2.0
   */
  public UUID sendOtpIntentToWhatsApp(final @NonNull UUID uuid, final @NonNull Context context) {
    if (context == null) {
      throw new NullPointerException("Context cannot be null");
    }
    if (uuid == null) {
      throw new NullPointerException("uuid cannot be null");
    }
    sendOtpIntentToWhatsApp(uuid, context, WhatsAppClientType.CONSUMER);
    sendOtpIntentToWhatsApp(uuid, context, WhatsAppClientType.BUSINESS);
    return uuid;
  }

  /**
   * <p>Send an OTP intent to WhatsApp with a unique request identifier. This is the handshake step
   * for WhatsApp authentication message. An intent is created with a pending intent that WhatsApp
   * uses to validate the creator package name against the package name defined in the
   * Authentication message template.</p>
   *
   * <p>This intent is sent to WhatsApp using the "com.whatsapp.otp.OTP_REQUESTED" action.</p>
   *
   * <p>After receiving this handshake, WhatsApp will enable the autofill or zero tap feature
   * based on the message template configuration.</p>
   *
   * <p>You can choose to send to WhatsApp consumer or business, thought is recommended to send to
   * both using {@link #sendOtpIntentToWhatsApp(Context) sendOtpIntentToWhatsApp} method</p>
   *
   * @param uuid    A unique identifier for this OTP request. Used for validating the authenticity
   *                of the received OTP code. This value is added to the intent extras under the
   *                HANDSHAKE_ID key.
   * @param context Application context used to broadcast the intent to WhatsApp.
   * @param type    Specifies whether to target WhatsApp consumer or business application.
   * @return The intent that was sent to WhatsApp, including the request ID in extras.
   * @since 0.2.0
   */
  public Intent sendOtpIntentToWhatsApp(final @NonNull UUID uuid,
                                        final @NonNull Context context,
                                        final @NonNull WhatsAppClientType type) {
    Intent intent = this.whatsAppOtpIntentBuilder.create(context, type);
    String value = uuid.toString();
    intent.putExtra(HANDSHAKE_ID, value);
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
