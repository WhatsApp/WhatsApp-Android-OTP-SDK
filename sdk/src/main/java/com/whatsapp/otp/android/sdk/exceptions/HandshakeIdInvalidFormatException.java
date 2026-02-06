/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.whatsapp.otp.android.sdk.exceptions;

/**
 * Exception thrown when the handshake ID has an invalid format (not a valid UUID).
 */
public class HandshakeIdInvalidFormatException extends InvalidWhatsAppOtpIntentException {

  public HandshakeIdInvalidFormatException() {
    super("Handshake ID has invalid format");
  }
}
