/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.whatsapp.otp.android.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import com.whatsapp.otp.android.sdk.enums.WhatsAppClientType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class WhatsAppOtpHandlerTest {

  private final Context context = ApplicationProvider.getApplicationContext();

  private final WhatsAppOtpIntentBuilder mockedWhatsAppOtpIntentBuilder = Mockito.mock(
      WhatsAppOtpIntentBuilder.class);
  private final Context mockedContext = Mockito.mock(Context.class);

  private WhatsAppOtpHandler waIntentHandlerWithMockedBuilder = new WhatsAppOtpHandler(
      mockedWhatsAppOtpIntentBuilder);


  @Test
  public void test_sendOtpIntentToWhatsAppConsumer_succeeds() {
    // setup
    WhatsAppOtpHandler waIntentHandler = new WhatsAppOtpHandler();
    // test
    Intent waIntent = waIntentHandler.sendOtpIntentToWhatsApp(context, WhatsAppClientType.CONSUMER);
    // assertions
    assertThat(waIntent).isNotNull();
    assertThat(waIntent.getAction()).isEqualTo("com.whatsapp.otp.OTP_REQUESTED");
    assertThat(waIntent.getPackage()).isEqualTo("com.whatsapp");
    String sdkVersion = waIntent.getStringExtra("SDK_VERSION");
    assertThat(sdkVersion).isEqualTo("0.1.0_not_from_manifest");
  }

  @Test
  public void test_sendOtpIntentToWhatsAppConsumerWithoutVersion_succeeds() {
    // setup
    WhatsAppOtpHandler waIntentHandler = new WhatsAppOtpHandler(false);
    // test
    Intent waIntent = waIntentHandler.sendOtpIntentToWhatsApp(context, WhatsAppClientType.CONSUMER);
    // assertions
    assertThat(waIntent).isNotNull();
    assertThat(waIntent.getAction()).isEqualTo("com.whatsapp.otp.OTP_REQUESTED");
    assertThat(waIntent.getPackage()).isEqualTo("com.whatsapp");
    String sdkVersion = waIntent.getStringExtra("SDK_VERSION");
    assertThat(sdkVersion).isNull();
  }

  @Test
  public void test_sendOtpIntentToWhatsAppBusiness_succeeds() {
    // setup
    WhatsAppOtpHandler waIntentHandler = new WhatsAppOtpHandler();
    // test
    Intent waIntent = waIntentHandler.sendOtpIntentToWhatsApp(context, WhatsAppClientType.BUSINESS);
    // assertions
    assertThat(waIntent).isNotNull();
    assertThat(waIntent.getAction()).isEqualTo("com.whatsapp.otp.OTP_REQUESTED");
    assertThat(waIntent.getPackage()).isEqualTo("com.whatsapp.w4b");
  }

  @Test
  public void test_sendOtpIntentToWhatsAppBusinessAndConsumer_succeeds() {
    // setup
    doReturn(Mockito.mock(Intent.class)).when(mockedWhatsAppOtpIntentBuilder)
        .create(eq(mockedContext), any());
    // test
    waIntentHandlerWithMockedBuilder.sendOtpIntentToWhatsApp(mockedContext);
    // assertions
    verify(mockedWhatsAppOtpIntentBuilder).create(eq(mockedContext),
        eq(WhatsAppClientType.BUSINESS));
    verify(mockedWhatsAppOtpIntentBuilder).create(eq(mockedContext),
        eq(WhatsAppClientType.CONSUMER));
    verify(mockedContext, times(2)).sendBroadcast(any(Intent.class));
  }

  @Test
  public void test_isWhatsAppOtpHandshakeSupported_returnsTrue() {
    // setup
    PackageManager pm = mockPackageManager();
    List<ResolveInfo> receivers = mockedResolveInfoList(true); // true has elements
    doReturn(receivers).when(pm).queryBroadcastReceivers(any(Intent.class), eq(0));

    // test
    boolean installed = waIntentHandlerWithMockedBuilder.isWhatsAppOtpHandshakeSupported(
        mockedContext, WhatsAppClientType.CONSUMER);
    // assertions
    assertThat(installed).isTrue();
  }

  @Test
  public void test_isWhatsAppOtpHandshakeSupportedNoReceiver_returnsFalse() {
    // setup
    mockQueryBroadcastReceivers(false); // false -> has no elements

    // test
    boolean installed = waIntentHandlerWithMockedBuilder.isWhatsAppOtpHandshakeSupported(
        mockedContext, WhatsAppClientType.CONSUMER);
    // assertions
    assertThat(installed).isFalse();
  }

  @Test
  public void test_isWhatsAppInstalledWithConsumerTypeParameter_returnsTrue() {
    // setup
    mockPackageManager();
    // test
    boolean installed = waIntentHandlerWithMockedBuilder.isWhatsAppInstalled(mockedContext,
        WhatsAppClientType.CONSUMER);
    // assertions
    assertThat(installed).isTrue();
  }

  @Test
  public void test_isWhatsAppInstalledBusinessTypeParameterNotInstalled_returnsFalse()
      throws NameNotFoundException {
    // setup
    PackageManager pm = mockPackageManager();
    doThrow(new NameNotFoundException()).when(pm)
        .getPackageInfo(eq(WhatsAppClientType.BUSINESS.getPackageName()), eq(0));
    // test
    boolean installed = waIntentHandlerWithMockedBuilder.isWhatsAppInstalled(mockedContext,
        WhatsAppClientType.BUSINESS);
    // assertions
    assertThat(installed).isFalse();
  }

  @Test
  public void test_IsWhatsAppInstalledWithConsumerTypeParameter_returnsTrue()
      throws NameNotFoundException {
    PackageManager pm = mockPackageManager();
    doThrow(new NameNotFoundException()).when(pm)
        .getPackageInfo(eq(WhatsAppClientType.CONSUMER.getPackageName()), eq(0));
    // test
    boolean installed = waIntentHandlerWithMockedBuilder.isWhatsAppInstalled(mockedContext,
        WhatsAppClientType.BUSINESS);
    // assertions
    assertThat(installed).isTrue();
  }

  @Test
  public void test_isWhatsAppInstalledUsingAppTypeConsumerNotInstalled_returnsFalse()
      throws NameNotFoundException {
    // setup
    // this is what ensures there is copy which is basically if whatsapp is installed
    PackageManager pm = mockPackageManager();
    doThrow(new NameNotFoundException()).when(pm).getPackageInfo(eq("com.whatsapp"), eq(0));
    // test
    boolean installed = waIntentHandlerWithMockedBuilder.isWhatsAppInstalled(
        mockedContext, WhatsAppClientType.CONSUMER);
    // assertions
    assertThat(installed).isFalse();
  }

  @Test
  public void test_isWhatsAppInstalledUsingAppTypeBothInstalled_returnsTrue() {
    // setup
    // this is what ensures there is copy which is basically if whatsapp is installed
    PackageManager pm = mockPackageManager();
    // test
    boolean isOtpAvailableOnWhatsAppConsumer = waIntentHandlerWithMockedBuilder.isWhatsAppInstalled(
        mockedContext, WhatsAppClientType.CONSUMER);
    boolean isOtpAvailableOnWhatsApp4Business = waIntentHandlerWithMockedBuilder.isWhatsAppInstalled(
        mockedContext, WhatsAppClientType.BUSINESS);
    // assertions
    assertThat(isOtpAvailableOnWhatsAppConsumer).isTrue();
    assertThat(isOtpAvailableOnWhatsApp4Business).isTrue();
  }

  @Test
  public void test_isWhatsAppOtpInstalled_returnsFalse() throws NameNotFoundException {
    // setup
    PackageManager pm = mockPackageManager();
    doThrow(new NameNotFoundException()).when(pm).getPackageInfo(anyString(), eq(0));

    // test
    boolean installed = waIntentHandlerWithMockedBuilder.isWhatsAppInstalled(mockedContext);
    // assertions
    assertThat(installed).isFalse();
  }

  @Test
  public void test_IsWhatsAppInstalled_returnsTrue() {
    mockPackageManager();
    // test
    boolean installed = waIntentHandlerWithMockedBuilder.isWhatsAppInstalled(mockedContext);
    // assertions
    assertThat(installed).isTrue();
  }

  @Test
  public void test_isWhatsAppOtpHandshakeSupportedConsumerAppHasReceiver_returnsTrue() {

    // setup
    PackageManager pm = mockQueryBroadcastReceivers(true); // true-> has elements

    // test
    boolean installed = waIntentHandlerWithMockedBuilder.isWhatsAppOtpHandshakeSupported(
        mockedContext, WhatsAppClientType.CONSUMER);
    // assertions
    assertThat(installed).isTrue();
    assertHandshakeSupportedQuery(pm, WhatsAppClientType.CONSUMER);

    ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
    verify(pm, never()).queryIntentActivities(intentArgumentCaptor.capture(), eq(0));
  }

  @Test
  public void test_isWhatsAppOtpHandshakeSupportedConsumerHasNoReceiver_returnsFalse() {
    // setup
    PackageManager pm = mockPackageManager();

    // test
    boolean supported = waIntentHandlerWithMockedBuilder.isWhatsAppOtpHandshakeSupported(
        mockedContext, WhatsAppClientType.CONSUMER);
    // assertions
    assertThat(supported).isFalse();
    assertHandshakeSupportedQuery(pm, WhatsAppClientType.CONSUMER);
  }

  @Test
  public void test_isWhatsAppOtpHandshakeSupportedBusinessAppHasReceiver_returnsTrue() {

    // setup
    PackageManager pm = mockQueryBroadcastReceivers(true); // true-> has elements

    // test
    boolean installed = waIntentHandlerWithMockedBuilder.isWhatsAppOtpHandshakeSupported(
        mockedContext, WhatsAppClientType.BUSINESS);
    // assertions
    assertThat(installed).isTrue();
    assertHandshakeSupportedQuery(pm, WhatsAppClientType.BUSINESS);

    ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
    verify(pm, never()).queryIntentActivities(intentArgumentCaptor.capture(), eq(0));
  }

  @Test
  public void test_isWhatsAppOtpHandshakeSupportedBusinessHasNoReceiver_returnsFalse() {
    // setup
    PackageManager pm = mockPackageManager();

    // test
    boolean supported = waIntentHandlerWithMockedBuilder.isWhatsAppOtpHandshakeSupported(
        mockedContext, WhatsAppClientType.BUSINESS);
    // assertions
    assertThat(supported).isFalse();
    assertHandshakeSupportedQuery(pm, WhatsAppClientType.BUSINESS);
  }

  @Test
  public void test_isWhatsAppOtpHandshakeSupportedHasBroadcastReceiver_returnsTrue() {

    // setup
    PackageManager pm = mockQueryBroadcastReceivers(true); // true-> has elements

    // test
    boolean installed = waIntentHandlerWithMockedBuilder.isWhatsAppOtpHandshakeSupported(
        mockedContext);
    // assertions
    assertThat(installed).isTrue();
    assertHandshakeSupportedQuery(pm, WhatsAppClientType.CONSUMER);
  }

  @Test
  public void test_isWhatsAppOtpHandshakeSupportedNoBroadcastReceiver_returnsFalse() {
    // setup
    PackageManager pm = mockPackageManager();

    // test
    boolean supported = waIntentHandlerWithMockedBuilder.isWhatsAppOtpHandshakeSupported(
        mockedContext);
    // assertions
    assertThat(supported).isFalse();
    assertHandshakeSupportedQuery(pm, WhatsAppClientType.BUSINESS, WhatsAppClientType.CONSUMER);
  }

  @NonNull
  private static Set<String> collectActionsFromIntent(
      ArgumentCaptor<Intent> argumentCaptorForZeroTap) {
    return argumentCaptorForZeroTap.getAllValues().stream().map(Intent::getAction)
        .collect(Collectors.toSet());
  }

  @NonNull
  private static List<String> getPackagesChecked(ArgumentCaptor<Intent> intentArgumentCaptor) {
    List<String> packages = intentArgumentCaptor.getAllValues().stream()
        .map(Intent::getPackage).collect(Collectors.toList());
    return packages;
  }

  private PackageManager mockPackageManager() {
    PackageManager pm = Mockito.mock(PackageManager.class);
    doReturn(pm).when(mockedContext).getPackageManager();
    return pm;
  }

  @NonNull
  private static List<ResolveInfo> mockedResolveInfoList(boolean hasElements) {
    List<ResolveInfo> receivers = new ArrayList<>();
    if (hasElements) {
      ResolveInfo mockedResolveInfo = Mockito.mock(ResolveInfo.class);
      receivers.add(mockedResolveInfo);
    }
    return receivers;
  }

  private PackageManager mockQueryBroadcastReceivers(boolean hasElements) {
    PackageManager pm = mockPackageManager();
    doReturn(mockedResolveInfoList(hasElements)).when(pm).queryBroadcastReceivers(any(), eq(0));
    return pm;
  }

  private static void assertHandshakeSupportedQuery(PackageManager pm,
      WhatsAppClientType... expectedTypes) {
    ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
    verify(pm, times(expectedTypes.length)).queryBroadcastReceivers(intentCaptor.capture(), eq(0));
    assertPackagesQueried(intentCaptor, expectedTypes);
    assertQueryAction(intentCaptor);
  }

  private static void assertQueryAction(ArgumentCaptor<Intent> intentCaptor) {
    Set<String> actions = collectActionsFromIntent(intentCaptor);
    assertThat(actions).hasSize(1);
    assertThat(actions).contains("com.whatsapp.otp.OTP_REQUESTED");
  }

  private static void assertPackagesQueried(final ArgumentCaptor<Intent> intentCaptor,
      final WhatsAppClientType... expectedTypes) {
    List<String> packagesChecked = getPackagesChecked(intentCaptor);
    List<String> packages = Arrays.stream(expectedTypes).map(WhatsAppClientType::getPackageName)
        .collect(Collectors.toList());
    assertThat(packagesChecked).containsAll(packages).hasSize(packages.size());
  }
}