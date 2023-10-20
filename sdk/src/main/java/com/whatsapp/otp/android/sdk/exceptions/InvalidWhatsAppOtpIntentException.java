/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.whatsapp.otp.android.sdk.exceptions;

import androidx.annotation.NonNull;

public class InvalidWhatsAppOtpIntentException extends RuntimeException {

  public InvalidWhatsAppOtpIntentException(final @NonNull String message) {
    super(message);
  }
}
