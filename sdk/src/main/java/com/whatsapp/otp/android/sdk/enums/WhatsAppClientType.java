/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.whatsapp.otp.android.sdk.enums;

public enum WhatsAppClientType {

  CONSUMER("com.whatsapp"),
  BUSINESS("com.whatsapp.w4b");

  private String packageName;

  WhatsAppClientType(String packageName) {
    this.packageName = packageName;
  }

  public String getPackageName() {
    return this.packageName;
  }
}
