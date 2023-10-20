/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.whatsapp.otp.android.sdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;

/**
 * Class with some utility functions which helps manage Broadcast receivers and Activities
 */
public class WhatsAppOtpUtils {

  /**
   * Enable a given component. Use this function for instance if you want a given component to be
   * available only when your application is waiting for the otp code.
   *
   * @param context context
   * @param cls class of the component you want to be enabled.
   */
  public void enableComponent(final @NonNull Context context, final @NonNull Class<?> cls){
    changeComponentState(context, cls, PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
  }

  /**
   * Disable a given component. You can use this function to disable a component from being available
   * if your app is not waiting for an otp code.
   *
   * @param context context
   * @param cls class of the component you want to be enabled.
   */

  public void disableComponent(final @NonNull Context context, final @NonNull Class<?> cls){
    changeComponentState(context, cls, PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
  }

  private static void changeComponentState(final @NonNull Context context, final @NonNull Class<?> cls, int componentState) {
    PackageManager pm = context.getPackageManager();
    ComponentName componentName = new ComponentName(context, cls);
    pm.setComponentEnabledSetting(componentName,
        componentState,
        PackageManager.DONT_KILL_APP);
  }
}
