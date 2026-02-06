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
  /** /** Handshake ID is missing from the Intent*/
  HANDSHAKE_ID_MISSING,
  /** Handshake ID is not a valid UUID format*/
  HANDSHAKE_ID_INVALID_FORMAT,
  /** Handshake ID does not match the expected value*/
  HANDSHAKE_ID_MISMATCH,
  /** Failure when extracting otp code*/
  GENERIC_EXCEPTION
}
