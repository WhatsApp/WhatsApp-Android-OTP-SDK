/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.whatsapp.otp.android.sdk.exceptions;

/**
 * Exception thrown when the handshake ID in the intent does not match the expected value.
 */
public class HandshakeIdMismatchException extends InvalidWhatsAppOtpIntentException {

  public HandshakeIdMismatchException() {
    super("Handshake ID does not match expected value");
  }
}
