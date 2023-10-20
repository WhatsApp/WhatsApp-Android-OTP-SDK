/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.whatsapp.otp.android.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.app.PendingIntent;
import android.content.Intent;
import com.whatsapp.otp.android.sdk.data.DebugSignal;
import com.whatsapp.otp.android.sdk.enums.WhatsAppClientType;
import com.whatsapp.otp.android.sdk.enums.WhatsAppOtpError;
import com.whatsapp.otp.android.sdk.exceptions.InvalidWhatsAppOtpIntentException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class WhatsAppOtpIncomingIntentHandlerTest {

  private static final String CODE = "567567";
  private static final String CODE_KEY = "code";
  private static final String CALLER_INFO = "_ci_";
  private static final String OTP_ERROR_IDENTIFIER_KEY = "error";
  private static final String OTP_ERROR_MESSAGE_KEY = "error_message";

  private Intent mockedIntent = Mockito.mock(Intent.class);
  private PendingIntent mockedPendingIntent = Mockito.mock(PendingIntent.class);
  private Consumer<String> mockedCodeConsumer = Mockito.mock(Consumer.class);
  private Consumer<DebugSignal> mockedDebugCodeConsumer = Mockito.mock(Consumer.class);
  private BiConsumer<WhatsAppOtpError, Exception> mockedErrorConsumer = Mockito.mock(
      BiConsumer.class);

  private WhatsAppOtpIncomingIntentHandler incomingIntentHandler = new WhatsAppOtpIncomingIntentHandler();

  @Test
  public void test_getOtpCodeFromWhatsAppIntent_succeeds() {
    // setup
    doReturn(CODE).when(mockedIntent).getStringExtra(CODE_KEY);
    mockIntentWithPendingIntentFromPackage(WhatsAppClientType.BUSINESS.getPackageName());

    // test
    String otpCodeFromWhatsAppIntent = incomingIntentHandler.getOtpCodeFromWhatsAppIntent(
        mockedIntent);

    // assertions
    assertThat(otpCodeFromWhatsAppIntent).isEqualTo("567567");
  }

  @Test
  public void test_processOtpCode_succeeds() {
    // setup
    doReturn(CODE).when(mockedIntent).getStringExtra(CODE_KEY);
    mockIntentWithPendingIntentFromPackage(WhatsAppClientType.BUSINESS.getPackageName());

    // test
    incomingIntentHandler.processOtpCode(mockedIntent, mockedCodeConsumer, mockedErrorConsumer);

    // assertions
    verify(mockedCodeConsumer).accept(eq(CODE));
    verify(mockedErrorConsumer, never()).accept(any(), any());
  }

  @Test
  public void test_processOtpCode_failsIntentNotFromWhatsApp() {
    // setup
    doReturn(CODE).when(mockedIntent).getStringExtra(CODE_KEY);
    mockIntentWithPendingIntentFromPackage("com.not.from.wa");

    // test
    incomingIntentHandler.processOtpCode(mockedIntent, mockedCodeConsumer, mockedErrorConsumer);

    // assertions
    verify(mockedCodeConsumer, never()).accept(any());
    verify(mockedErrorConsumer).accept(eq(WhatsAppOtpError.INTENT_IS_NOT_FROM_WHATSAPP),
        any(InvalidWhatsAppOtpIntentException.class));
  }

  @Test
  public void test_processOtpCode_failsWithGenericException() {
    // setup
    doThrow(new RuntimeException()).when(mockedIntent).getParcelableExtra(eq(CALLER_INFO));
    doReturn(WhatsAppClientType.BUSINESS.getPackageName()).when(mockedPendingIntent)
        .getCreatorPackage();

    // test
    incomingIntentHandler.processOtpCode(mockedIntent, mockedCodeConsumer, mockedErrorConsumer);

    // assertions
    verify(mockedCodeConsumer, never()).accept(any());
    verify(mockedErrorConsumer).accept(eq(WhatsAppOtpError.GENERIC_EXCEPTION),
        any(RuntimeException.class));

  }

  @Test
  public void test_processOtpCode_failsMissingCode() {
    mockIntentWithPendingIntentFromPackage(WhatsAppClientType.CONSUMER.getPackageName());

    // test
    incomingIntentHandler.processOtpCode(mockedIntent, mockedCodeConsumer, mockedErrorConsumer);

    // assertions
    verify(mockedCodeConsumer, never()).accept(any());
    verify(mockedErrorConsumer).accept(eq(WhatsAppOtpError.OTP_CODE_NOT_RECEIVED), isNull());
  }

  @Test
  public void test_getOtpDebugFromWhatsAppIntent_succeeds() {
    // setup
    Intent whatsAppIntent = mockWhatsAppIntentWithOtpError();
    // test
    DebugSignal debugSignal = incomingIntentHandler.getDebugSignalFromWhatsAppIntent(
        whatsAppIntent);

    // assertions
    assertThat(debugSignal).isNotNull();
    assertThat(debugSignal.otpErrorIdentifier).isEqualTo("key");
    assertThat(debugSignal.otpErrorMessage).isEqualTo("message");
  }

  @Test
  public void test_processOtpDebugSignals_succeeds() {
    // setup
    Intent whatsAppIntent = mockWhatsAppIntentWithOtpError();

    // test
    incomingIntentHandler.processOtpDebugSignals(whatsAppIntent, mockedDebugCodeConsumer, mockedErrorConsumer);

    // assertions
    ArgumentCaptor<DebugSignal> debugSignalArgumentCaptor = ArgumentCaptor.forClass(DebugSignal.class);
    verify(mockedDebugCodeConsumer).accept(debugSignalArgumentCaptor.capture());
    verify(mockedErrorConsumer, never()).accept(any(), any());
    DebugSignal debugSignal = debugSignalArgumentCaptor.getValue();
    assertThat(debugSignal).isNotNull();
    assertThat(debugSignal.otpErrorIdentifier).isEqualTo("key");
    assertThat(debugSignal.otpErrorMessage).isEqualTo("message");
  }

  private Intent mockWhatsAppIntentWithOtpError() {
    doReturn("key").when(mockedIntent).getStringExtra(OTP_ERROR_IDENTIFIER_KEY);
    doReturn("message").when(mockedIntent).getStringExtra(OTP_ERROR_MESSAGE_KEY);
    doReturn(mockedPendingIntent).when(mockedIntent).getParcelableExtra(eq(CALLER_INFO));
    doReturn(WhatsAppClientType.BUSINESS.getPackageName()).when(mockedPendingIntent)
        .getCreatorPackage();
    return mockedIntent;
  }

  @Test
  public void test_isIntentFromWhatsApp_itIs() {
    // setup
    mockIntentWithPendingIntentFromPackage("com.whatsapp");
    // test
    boolean isIntentFromWhatsApp = incomingIntentHandler.isIntentFromWhatsApp(mockedIntent);
    // assertions
    assertThat(isIntentFromWhatsApp).isTrue();
  }


  @Test
  public void test_isIntentFromWhatsAppConsumer_itIs() {
    // setup
    mockIntentWithPendingIntentFromPackage("com.whatsapp");
    // test
    boolean isIntentFromWhatsApp = incomingIntentHandler.isIntentFromWhatsApp(mockedIntent,
        WhatsAppClientType.CONSUMER);
    // assertions
    assertThat(isIntentFromWhatsApp).isTrue();
  }

  @Test
  public void test_isIntentFromWhatsAppBusiness_itIs() {
    // setup
    mockIntentWithPendingIntentFromPackage("com.whatsapp.w4b");
    // test
    boolean isIntentFromWhatsApp = incomingIntentHandler.isIntentFromWhatsApp(mockedIntent,
        WhatsAppClientType.BUSINESS);
    // assertions
    assertThat(isIntentFromWhatsApp).isTrue();
  }

  @Test
  public void test_isIntentFromWhatsAppBusinessOrConsumer_itIs() {
    // setup
    mockIntentWithPendingIntentFromPackage("com.whatsapp.w4b");
    // test
    boolean isIntentFromWhatsApp = incomingIntentHandler.isIntentFromWhatsApp(mockedIntent,
        WhatsAppClientType.BUSINESS, WhatsAppClientType.CONSUMER);
    // assertions
    assertThat(isIntentFromWhatsApp).isTrue();
  }

  @Test
  public void test_isIntentFromWhatsApp_itIsNot() {
    mockIntentWithPendingIntentFromPackage("com.not.from.wa");
    // test
    boolean isIntentFromWhatsApp = incomingIntentHandler.isIntentFromWhatsApp(mockedIntent);
    // assertions
    assertThat(isIntentFromWhatsApp).isFalse();
  }

  @Test
  public void test_isIntentFromWhatsAppConsumer_itIsNot() {
    // setup
    mockIntentWithPendingIntentFromPackage("com.whatsapp.w4b");
    // test
    boolean isIntentFromWhatsApp = incomingIntentHandler.isIntentFromWhatsApp(mockedIntent,
        WhatsAppClientType.CONSUMER);
    // assertions
    assertThat(isIntentFromWhatsApp).isFalse();
  }

  @Test
  public void test_isIntentFromWhatsAppBusiness_itIsNot() {

    // setup
    mockIntentWithPendingIntentFromPackage("com.whatsapp");
    // test
    boolean isIntentFromWhatsApp = incomingIntentHandler.isIntentFromWhatsApp(mockedIntent,
        WhatsAppClientType.BUSINESS);
    // assertions
    assertThat(isIntentFromWhatsApp).isFalse();
  }

  private void mockIntentWithPendingIntentFromPackage(String packageSource) {
    doReturn(mockedPendingIntent).when(mockedIntent).getParcelableExtra(eq(CALLER_INFO));
    doReturn(packageSource).when(mockedPendingIntent)
        .getCreatorPackage();
  }
}