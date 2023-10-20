/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.whatsapp.otp.android.sdk;

import android.app.PendingIntent;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.whatsapp.otp.android.sdk.data.DebugSignal;
import com.whatsapp.otp.android.sdk.enums.WhatsAppClientType;
import com.whatsapp.otp.android.sdk.enums.WhatsAppOtpError;
import com.whatsapp.otp.android.sdk.exceptions.InvalidWhatsAppOtpIntentException;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Class to handle intents coming from WhatsApp
 */
public class WhatsAppOtpIncomingIntentHandler {

  private static final String CODE_KEY = "code";

  private static final String OTP_ERROR_IDENTIFIER_KEY = "error";

  private static final String OTP_ERROR_MESSAGE_KEY = "error_message";

  private static final WhatsAppClientType[] WA_PACKAGES = {
      WhatsAppClientType.CONSUMER,
      WhatsAppClientType.BUSINESS
  };

  /**
   * Process code using the {@code onCodePresent} if the code is present or handle error using
   * {@code errorHandler} if the code is not present, if the intent is not valid, or if something
   * else doesn't work out while extracting the code from the {@code intent}.
   *
   * @param intent        whatsapp intent
   * @param onCodePresent function that receives the code if the code is present
   * @param errorHandler  function to handle error scenarios
   */
  public void processOtpCode(Intent intent, Consumer<String> onCodePresent,
      BiConsumer<WhatsAppOtpError, Exception> errorHandler) {
    try {
      String otpCode = this.getOtpCodeFromWhatsAppIntent(intent);
      if (otpCode != null) {
        onCodePresent.accept(otpCode);
      } else {
        errorHandler.accept(WhatsAppOtpError.OTP_CODE_NOT_RECEIVED, null);
      }
    } catch (InvalidWhatsAppOtpIntentException e) {
      errorHandler.accept(WhatsAppOtpError.INTENT_IS_NOT_FROM_WHATSAPP, e);
    } catch (Exception e) {
      errorHandler.accept(WhatsAppOtpError.GENERIC_EXCEPTION, e);
    }
  }

  /**
   * Extract code from WhatsAppIntent
   *
   * @param intent WhatsApp intent
   * @return the code
   * @throws InvalidWhatsAppOtpIntentException if intent is not from WhatsApp
   */
  @Nullable
  public String getOtpCodeFromWhatsAppIntent(final @NonNull Intent intent) {
    // verify that it is whatsapp only that is sending the code.
    if (isIntentFromWhatsApp(intent)) {
      return intent.getStringExtra(CODE_KEY);
    }
    throw new InvalidWhatsAppOtpIntentException("Invalid Intent");
  }

  /**
   * Process debug signals using the {@code debugSignalConsumer} if debug signals are present or
   * handle the error using {@code  errorHandler} if the debugs signals are not present.
   * <p>
   * The debugSignalConsumer receives a {@link DebugSignal} as a parameter which contains the key
   * and message of the debug signal.
   * </p>
   * <p>
   * The errorHandler gets a {@link WhatsAppOtpError} and a {@link Exception} - if any - stating the
   * error.
   * </p>
   *
   * @param intent              a WhatsApp intent.
   * @param debugSignalConsumer a consumer for the debug signal
   * @param errorHandler        a consumer for handling errors that may occur while extracting the
   *                            debug signal
   */
  public void processOtpDebugSignals(final @NonNull Intent intent,
      final @NonNull Consumer<DebugSignal> debugSignalConsumer,
      final @NonNull BiConsumer<WhatsAppOtpError, Exception> errorHandler) {
    try {
      DebugSignal debugSignal = getDebugSignalFromWhatsAppIntent(intent);
      debugSignalConsumer.accept(debugSignal);
    } catch (InvalidWhatsAppOtpIntentException e) {
      errorHandler.accept(WhatsAppOtpError.INTENT_IS_NOT_FROM_WHATSAPP, e);
    } catch (Exception e) {
      errorHandler.accept(WhatsAppOtpError.GENERIC_EXCEPTION, e);
    }
  }

  /**
   * Extract {@link DebugSignal} from WhatsApp Intent
   *
   * @param intent WhatsApp intent
   * @return extracted debug signal
   * @throws {@link InvalidWhatsAppOtpIntentException} if the intent is not from WhatsApp.
   */
  public DebugSignal getDebugSignalFromWhatsAppIntent(final @NonNull Intent intent) {
    if (isIntentFromWhatsApp(intent)) {
      return extractDebugSignal(intent);
    }
    throw new InvalidWhatsAppOtpIntentException("Invalid Intent");
  }

  private DebugSignal extractDebugSignal(final Intent intent) {
    String otpErrorKey = intent.getStringExtra(OTP_ERROR_IDENTIFIER_KEY);
    String otpErrorMessage = intent.getStringExtra(OTP_ERROR_MESSAGE_KEY);
    return new DebugSignal(otpErrorKey, otpErrorMessage);
  }

  /**
   * Check if the {@code intent} is from WhatsApp app by checking its pending intent creator
   * package
   *
   * @param intent intent
   * @return true if the intent is from whatsapp, either com.whatsapp or com.whatsapp.w4b
   */
  public boolean isIntentFromWhatsApp(final @NonNull Intent intent) {
    PendingIntent pendingIntent = intent.getParcelableExtra(WhatsAppOtpIntentBuilder.CALLER_INFO);
    if (pendingIntent == null) {
      return false;
    }
    return isPendingIntentFromWhatsApp(pendingIntent, WA_PACKAGES);
  }

  /**
   * Check if the {@code intent} is from WhatsApp app by checking its pending intent creator
   * package
   *
   * @param intent     intent
   * @param clientType client type which can be CONSUMER, BUSINESS or both
   * @return true if the intent is from the specified app type
   */
  public boolean isIntentFromWhatsApp(final @NonNull Intent intent,
      final @NonNull WhatsAppClientType... clientType) {
    PendingIntent pendingIntent = intent.getParcelableExtra(WhatsAppOtpIntentBuilder.CALLER_INFO);
    if (pendingIntent == null) {
      return false;
    }
    return isPendingIntentFromWhatsApp(pendingIntent, clientType);
  }

  private static boolean isPendingIntentFromWhatsApp(final @NonNull PendingIntent pendingIntent,
      final @NonNull WhatsAppClientType... clientType) {
    String pendingIntentCreatorPackage = pendingIntent.getCreatorPackage();
    return Arrays.stream(clientType).map(WhatsAppClientType::getPackageName)
        .anyMatch(packageName -> packageName.equals(pendingIntentCreatorPackage));
  }
}
