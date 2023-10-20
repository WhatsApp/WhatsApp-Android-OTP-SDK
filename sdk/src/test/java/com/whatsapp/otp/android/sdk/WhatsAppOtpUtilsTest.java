/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.whatsapp.otp.android.sdk;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WhatsAppOtpUtilsTest {

  private final WhatsAppOtpUtils whatsAppOtpUtils = new WhatsAppOtpUtils();

  @Mock
  private Context context;

  @Mock
  private PackageManager packageManager;

  @Before
  public void setup() {
    Mockito.doReturn(packageManager).when(context).getPackageManager();
  }

  @Test
  public void testEnableComponent() {

    // Test
    whatsAppOtpUtils.enableComponent(context, TestReceiver.class);

    // Assertions
    verify(packageManager)
        .setComponentEnabledSetting(isA(ComponentName.class),
            eq(PackageManager.COMPONENT_ENABLED_STATE_ENABLED),
            eq(PackageManager.DONT_KILL_APP));
  }

  @Test
  public void testDisableComponent() {

    // Test
    whatsAppOtpUtils.disableComponent(context, TestReceiver.class);

    // Assertions
    verify(packageManager)
        .setComponentEnabledSetting(isA(ComponentName.class),
            eq(PackageManager.COMPONENT_ENABLED_STATE_DISABLED),
            eq(PackageManager.DONT_KILL_APP));
  }

  private static class TestReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

    }
  }

}