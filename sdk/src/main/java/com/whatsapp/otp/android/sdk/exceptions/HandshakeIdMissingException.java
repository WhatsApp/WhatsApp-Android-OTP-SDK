/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.whatsapp.otp.android.sdk.exceptions;

/**
 * Exception thrown when the handshake ID is missing from the WhatsApp intent.
 */
public class HandshakeIdMissingException extends InvalidWhatsAppOtpIntentException {

  public HandshakeIdMissingException() {
    super("Handshake ID is missing from intent");
  }
}
