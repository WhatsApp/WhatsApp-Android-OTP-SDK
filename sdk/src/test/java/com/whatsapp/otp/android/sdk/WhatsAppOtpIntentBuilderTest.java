/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.whatsapp.otp.android.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.test.core.app.ApplicationProvider;
import com.whatsapp.otp.android.sdk.enums.WhatsAppClientType;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class WhatsAppOtpIntentBuilderTest {

  private final Context context = ApplicationProvider.getApplicationContext();

  @Test
  public void test_createForWhatsAppConsumer_succeeds() {
    WhatsAppOtpIntentBuilder whatsAppOtpIntent = new WhatsAppOtpIntentBuilder();
    Intent otpIntentToWhatsApp = whatsAppOtpIntent.create(context,
        WhatsAppClientType.CONSUMER);
    assertThat(otpIntentToWhatsApp).isNotNull();
    assertThat(otpIntentToWhatsApp.getAction()).isEqualTo("com.whatsapp.otp.OTP_REQUESTED");
    assertThat(otpIntentToWhatsApp.getPackage()).isEqualTo("com.whatsapp");
    PendingIntent pendingIntent = otpIntentToWhatsApp.getParcelableExtra("_ci_");
    assertThat(pendingIntent).isNotNull();
    final String sdkVersion = otpIntentToWhatsApp.getStringExtra("SDK_VERSION");
    assertThat(sdkVersion).isEqualTo("0.1.0_not_from_manifest");
  }

  @Test
  public void test_createForWhatsAppConsumerNoVersion_succeeds() {
    WhatsAppOtpIntentBuilder whatsAppOtpIntent = new WhatsAppOtpIntentBuilder(false);
    Intent otpIntentToWhatsApp = whatsAppOtpIntent.create(context,
        WhatsAppClientType.CONSUMER);
    assertThat(otpIntentToWhatsApp).isNotNull();
    assertThat(otpIntentToWhatsApp.getAction()).isEqualTo("com.whatsapp.otp.OTP_REQUESTED");
    assertThat(otpIntentToWhatsApp.getPackage()).isEqualTo("com.whatsapp");
    PendingIntent pendingIntent = otpIntentToWhatsApp.getParcelableExtra("_ci_");
    assertThat(pendingIntent).isNotNull();
    String sdkVersion = otpIntentToWhatsApp.getStringExtra("SDK_VERSION");
    assertThat(sdkVersion).isNull();
  }

  @Test
  public void test_createForWhatsAppBusiness_succeeds() {
    WhatsAppOtpIntentBuilder whatsAppOtpIntent = new WhatsAppOtpIntentBuilder();
    Intent otpIntentToWhatsApp = whatsAppOtpIntent.create(context,
        WhatsAppClientType.BUSINESS);
    assertThat(otpIntentToWhatsApp).isNotNull();
    assertThat(otpIntentToWhatsApp.getAction()).isEqualTo("com.whatsapp.otp.OTP_REQUESTED");
    assertThat(otpIntentToWhatsApp.getPackage()).isEqualTo("com.whatsapp.w4b");
    PendingIntent pendingIntent = otpIntentToWhatsApp.getParcelableExtra("_ci_");
    assertThat(pendingIntent).isNotNull();
  }

  @Test
  public void test_createWithMissingType_failsWithNullPointerException() {
    Assertions.assertThatNullPointerException().isThrownBy(() -> {
      new WhatsAppOtpIntentBuilder().create(context, null);
    });
  }

  @Test
  public void test_createWithMissingContext_failsWithNullPointerException() {
    Assertions.assertThatNullPointerException().isThrownBy(() -> {
      new WhatsAppOtpIntentBuilder().create(null, WhatsAppClientType.CONSUMER);
    });
  }
}