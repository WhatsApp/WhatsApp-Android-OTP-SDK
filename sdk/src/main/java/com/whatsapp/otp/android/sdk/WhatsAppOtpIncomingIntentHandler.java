/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.whatsapp.otp.android.sdk;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.whatsapp.otp.android.sdk.data.DebugSignal;
import com.whatsapp.otp.android.sdk.enums.WhatsAppOtpError;
import com.whatsapp.otp.android.sdk.exceptions.HandshakeIdInvalidFormatException;
import com.whatsapp.otp.android.sdk.exceptions.HandshakeIdMismatchException;
import com.whatsapp.otp.android.sdk.exceptions.HandshakeIdMissingException;
import com.whatsapp.otp.android.sdk.exceptions.InvalidWhatsAppOtpIntentException;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Class to handle intents coming from WhatsApp
 */
public class WhatsAppOtpIncomingIntentHandler {

  private static final String CODE_KEY = "code";

  private static final String HANDSHAKE_ID = "request_id";

  private static final String OTP_ERROR_IDENTIFIER_KEY = "error";

  private static final String OTP_ERROR_MESSAGE_KEY = "error_message";

  /**
   * Process code with handshake ID validation. Use this method to validate that the incoming
   * intent contains the expected handshake ID before processing the OTP code.
   *
   * @param intent whatsapp intent
   * @param expectedHandshakeId the handshake ID returned by sendOtpIntentToWhatsApp()
   * @param onCodePresent function that receives the code if valid
   * @param errorHandler function to handle error scenarios
   */
  public void processOtpCode(
      Intent intent,
      String expectedHandshakeId,
      Consumer<String> onCodePresent,
      BiConsumer<WhatsAppOtpError, Exception> errorHandler) {
    try {
      String otpCode = this.getOtpCodeFromWhatsAppIntent(intent, expectedHandshakeId);
      if (otpCode != null) {
        onCodePresent.accept(otpCode);
      } else {
        errorHandler.accept(WhatsAppOtpError.OTP_CODE_NOT_RECEIVED, null);
      }
    } catch (HandshakeIdMissingException e) {
      errorHandler.accept(WhatsAppOtpError.HANDSHAKE_ID_MISSING, e);
    } catch (HandshakeIdInvalidFormatException e) {
      errorHandler.accept(WhatsAppOtpError.HANDSHAKE_ID_INVALID_FORMAT, e);
    } catch (HandshakeIdMismatchException e) {
      errorHandler.accept(WhatsAppOtpError.HANDSHAKE_ID_MISMATCH, e);
    } catch (Exception e) {
      errorHandler.accept(WhatsAppOtpError.GENERIC_EXCEPTION, e);
    }
  }

  /**
   * Extract code from WhatsAppIntent after validating and matching the handshake ID.
   * Returns the OTP code only if the handshake ID in the intent matches the expected value.
   *
   * @param intent WhatsApp intent
   * @param expectedHandshakeId The handshake ID returned by sendOtpIntentToWhatsApp()
   * @return the code if handshake ID matches, null otherwise
   * @throws HandshakeIdMissingException if handshake ID is missing from intent
   * @throws HandshakeIdInvalidFormatException if handshake ID has invalid format
   * @throws HandshakeIdMismatchException if handshake ID does not match expected value
   */
  @Nullable
  public String getOtpCodeFromWhatsAppIntent(
      final @NonNull Intent intent,
      final @NonNull String expectedHandshakeId) {
    validateHandshakeId(intent, expectedHandshakeId);
    return intent.getStringExtra(CODE_KEY);
  }

  /**
   * Extract handshake ID from WhatsApp intent.
   *
   * @param intent WhatsApp intent
   * @return the handshake ID string, or null if not present or empty
   */
  @Nullable
  public String getHandshakeIdFromWhatsAppIntent(final @NonNull Intent intent) {
    String handshakeId = intent.getStringExtra(HANDSHAKE_ID);
    if (handshakeId == null) {
      return null;
    }
    handshakeId = handshakeId.trim();
    if (handshakeId.isEmpty()) {
      return null;
    }
    return handshakeId;
  }

  public boolean isValidHandshakeId(final @NonNull String handshakeId) {
    try {
      UUID.fromString(handshakeId.trim());
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }

  /**
   * Validate that the handshake ID in the intent matches the expected value.
   *
   * @param intent The incoming WhatsApp intent
   * @param expectedHandshakeId The handshake ID returned by sendOtpIntentToWhatsApp()
   * @throws HandshakeIdMissingException if handshake ID is missing from intent
   * @throws HandshakeIdInvalidFormatException if handshake ID has invalid format
   * @throws HandshakeIdMismatchException if handshake ID does not match expected value
   */
  private void validateHandshakeId(
      final @NonNull Intent intent,
      final @NonNull String expectedHandshakeId) {
    String handshakeId = this.getHandshakeIdFromWhatsAppIntent(intent);
    if (handshakeId == null) {
      throw new HandshakeIdMissingException();
    }
    if (!this.isValidHandshakeId(handshakeId)) {
      throw new HandshakeIdInvalidFormatException();
    }
    if (!handshakeId.equals(expectedHandshakeId)) {
      throw new HandshakeIdMismatchException();
    }
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
    } catch (Exception e) {
      errorHandler.accept(WhatsAppOtpError.GENERIC_EXCEPTION, e);
    }
  }

  /**
   * Extract {@link DebugSignal} from WhatsApp Intent
   *
   * @param intent WhatsApp intent
   * @return extracted debug signal
   */
  public DebugSignal getDebugSignalFromWhatsAppIntent(final @NonNull Intent intent) {
    return extractDebugSignal(intent);
  }

  private DebugSignal extractDebugSignal(final Intent intent) {
    String otpErrorKey = intent.getStringExtra(OTP_ERROR_IDENTIFIER_KEY);
    String otpErrorMessage = intent.getStringExtra(OTP_ERROR_MESSAGE_KEY);
    return new DebugSignal(otpErrorKey, otpErrorMessage);
  }

  /**
   * Check if the intent is from WhatsApp by validating the handshake ID.
   * The handshake ID in the intent must match the expected handshake ID
   * that was returned when sending the OTP request.
   *
   * @param intent The incoming WhatsApp intent
   * @param expectedHandshakeId The handshake ID returned by sendOtpIntentToWhatsApp()
   * @return true if the handshake ID matches, false otherwise
   */
  public boolean isIntentFromWhatsApp(
          final @NonNull Intent intent,
          final @NonNull String expectedHandshakeId) {
    try {
      validateHandshakeId(intent, expectedHandshakeId);
      return true;
    } catch (InvalidWhatsAppOtpIntentException e) {
      return false;
    }
  }
}
