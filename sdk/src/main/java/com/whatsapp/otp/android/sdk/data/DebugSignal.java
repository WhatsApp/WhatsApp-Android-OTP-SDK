/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.whatsapp.otp.android.sdk.data;

/**
 * Holder for debug signals attributes
 */
public class DebugSignal {

  /**
   * An identifier for the error
   */
  public final String otpErrorIdentifier;
  /**
   * A message for the error
   */
  public final String otpErrorMessage;

  /**
   * Constructor
   * @param otpErrorIdentifier an identifier for the error
   * @param otpErrorMessage the error message
   */
  public DebugSignal(final String otpErrorIdentifier, final String otpErrorMessage) {
    this.otpErrorIdentifier = otpErrorIdentifier;
    this.otpErrorMessage = otpErrorMessage;
  }
}
