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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.content.Intent;
import com.whatsapp.otp.android.sdk.data.DebugSignal;
import com.whatsapp.otp.android.sdk.enums.WhatsAppOtpError;
import com.whatsapp.otp.android.sdk.exceptions.HandshakeIdInvalidFormatException;
import com.whatsapp.otp.android.sdk.exceptions.HandshakeIdMismatchException;
import com.whatsapp.otp.android.sdk.exceptions.HandshakeIdMissingException;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class WhatsAppOtpIncomingIntentHandlerTest {

  private static final String CODE = "567567";
  private static final String CODE_KEY = "code";
  private static final String OTP_ERROR_IDENTIFIER_KEY = "error";
  private static final String OTP_ERROR_MESSAGE_KEY = "error_message";
  private static final UUID REQUEST_ID = UUID.randomUUID();
  private static final String REQUEST_ID_KEY = "request_id";


  private Intent mockedIntent = Mockito.mock(Intent.class);
  private Consumer<DebugSignal> mockedDebugCodeConsumer = Mockito.mock(Consumer.class);
  private BiConsumer<WhatsAppOtpError, Exception> mockedErrorConsumer = Mockito.mock(
      BiConsumer.class);
  private Consumer<String> mockedSingleCodeConsumer = Mockito.mock(Consumer.class);

  private WhatsAppOtpIncomingIntentHandler incomingIntentHandler = new WhatsAppOtpIncomingIntentHandler();

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

  @Test
  public void test_isIntentFromWhatsApp_withMatchingHandshakeId_returnsTrue() {
    // setup
    doReturn(REQUEST_ID.toString()).when(mockedIntent).getStringExtra(REQUEST_ID_KEY);

    // test
    boolean result = incomingIntentHandler.isIntentFromWhatsApp(mockedIntent, REQUEST_ID.toString());

    // assertions
    assertThat(result).isTrue();
  }

  @Test
  public void test_isIntentFromWhatsApp_withMismatchedHandshakeId_returnsFalse() {
    // setup
    doReturn(REQUEST_ID.toString()).when(mockedIntent).getStringExtra(REQUEST_ID_KEY);
    String differentHandshakeId = UUID.randomUUID().toString();

    // test
    boolean result = incomingIntentHandler.isIntentFromWhatsApp(mockedIntent, differentHandshakeId);

    // assertions
    assertThat(result).isFalse();
  }

  @Test
  public void test_isIntentFromWhatsApp_withMissingHandshakeId_returnsFalse() {
    // setup - no handshake ID in intent

    // test
    boolean result = incomingIntentHandler.isIntentFromWhatsApp(mockedIntent, REQUEST_ID.toString());

    // assertions
    assertThat(result).isFalse();
  }

  @Test
  public void test_isIntentFromWhatsApp_withInvalidHandshakeId_returnsFalse() {
    // setup
    doReturn("invalid-uuid").when(mockedIntent).getStringExtra(REQUEST_ID_KEY);

    // test
    boolean result = incomingIntentHandler.isIntentFromWhatsApp(mockedIntent, REQUEST_ID.toString());

    // assertions
    assertThat(result).isFalse();
  }

  @Test
  public void test_processOtpCode_withExpectedHandshakeId_succeeds() {
    // setup
    doReturn(CODE).when(mockedIntent).getStringExtra(CODE_KEY);
    doReturn(REQUEST_ID.toString()).when(mockedIntent).getStringExtra(REQUEST_ID_KEY);

    // test
    incomingIntentHandler.processOtpCode(mockedIntent, REQUEST_ID.toString(), mockedSingleCodeConsumer, mockedErrorConsumer);

    // assertions
    verify(mockedSingleCodeConsumer).accept(eq(CODE));
    verify(mockedErrorConsumer, never()).accept(any(), any());
  }

  @Test
  public void test_processOtpCode_withMismatchedHandshakeId_fails() {
    // setup
    doReturn(CODE).when(mockedIntent).getStringExtra(CODE_KEY);
    doReturn(REQUEST_ID.toString()).when(mockedIntent).getStringExtra(REQUEST_ID_KEY);
    String differentHandshakeId = UUID.randomUUID().toString();

    // test
    incomingIntentHandler.processOtpCode(mockedIntent, differentHandshakeId, mockedSingleCodeConsumer, mockedErrorConsumer);

    // assertions
    verify(mockedSingleCodeConsumer, never()).accept(any());
    verify(mockedErrorConsumer).accept(eq(WhatsAppOtpError.HANDSHAKE_ID_MISMATCH), any(HandshakeIdMismatchException.class));
  }

  @Test
  public void test_getOtpCodeFromWhatsAppIntent_withExpectedHandshakeId_succeeds() {
    // setup
    doReturn(CODE).when(mockedIntent).getStringExtra(CODE_KEY);
    doReturn(REQUEST_ID.toString()).when(mockedIntent).getStringExtra(REQUEST_ID_KEY);

    // test
    String result = incomingIntentHandler.getOtpCodeFromWhatsAppIntent(mockedIntent, REQUEST_ID.toString());

    // assertions
    assertThat(result).isEqualTo(CODE);
  }

  @Test(expected = HandshakeIdMismatchException.class)
  public void test_getOtpCodeFromWhatsAppIntent_withMismatchedHandshakeId_throwsHandshakeIdMismatchException() {
    // setup
    doReturn(CODE).when(mockedIntent).getStringExtra(CODE_KEY);
    doReturn(REQUEST_ID.toString()).when(mockedIntent).getStringExtra(REQUEST_ID_KEY);
    String differentHandshakeId = UUID.randomUUID().toString();

    // test - should throw HandshakeIdMismatchException
    incomingIntentHandler.getOtpCodeFromWhatsAppIntent(mockedIntent, differentHandshakeId);
  }

  @Test(expected = HandshakeIdMissingException.class)
  public void test_getOtpCodeFromWhatsAppIntent_withMissingHandshakeId_throwsHandshakeIdMissingException() {
    // setup - no handshake ID in intent

    // test - should throw HandshakeIdMissingException
    incomingIntentHandler.getOtpCodeFromWhatsAppIntent(mockedIntent, REQUEST_ID.toString());
  }

  @Test(expected = HandshakeIdInvalidFormatException.class)
  public void test_getOtpCodeFromWhatsAppIntent_withInvalidHandshakeId_throwsHandshakeIdInvalidFormatException() {
    // setup
    doReturn("invalid-uuid").when(mockedIntent).getStringExtra(REQUEST_ID_KEY);

    // test - should throw HandshakeIdInvalidFormatException
    incomingIntentHandler.getOtpCodeFromWhatsAppIntent(mockedIntent, REQUEST_ID.toString());
  }

  private Intent mockWhatsAppIntentWithOtpError() {
    doReturn("key").when(mockedIntent).getStringExtra(OTP_ERROR_IDENTIFIER_KEY);
    doReturn("message").when(mockedIntent).getStringExtra(OTP_ERROR_MESSAGE_KEY);
    return mockedIntent;
  }
}
