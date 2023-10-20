/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.whatsapp.otp.android.sdk.enums;

public enum WhatsAppOtpError {
  /** Intent is not from WhatsApp */
  INTENT_IS_NOT_FROM_WHATSAPP,
  /** Otp code is not present (this should not happend)*/
  OTP_CODE_NOT_RECEIVED,
  /** Failure when extracting otp code*/
  GENERIC_EXCEPTION
}
