# WhatsApp OTP Android SDK

This SDK provides an interface which simplifies integration with WhatsApp clients in order to automatically receive the one time password code on your app.

## Interfaces

The class ``WhatsAppOtpHandler`` holds logic that applies when sending an intent to WhatsApp, to do the handshake, as well as for checking for WhatsApp installation.

### Handshake

Sending an intent to WhatsApp to do the handshake as described on the [Authentication templates document](https://developers.facebook.com/docs/whatsapp/business-management-api/authentication-templates/) is as simple as:

```
WhatsAppOtpHandler whatsAppOtpHandler = new WhatsAppOtpHandler();
whatsAppOtpHandler.sendOtpIntentToWhatsApp(context);
```

This sends the handshake to bot the WhatsApp Consumer app and the WhatsApp Business app. If you want to specify a specific WhatsApp flavor, you can use the provided overloaded function like shown below:

```
whatsAppOtpHandler.sendOtpIntentToWhatsApp(context, WhatsAppClientType.CONSUMER);
```

When doing the handshake, we recommend you do it with both apps.

### Checking WhatsApp installation

We also offer two utility functions app developers can use to decide if they want to offer WhatsApp as a channel option to deliver a one time code.

To check WhatsApp installation you can use the ``isWhatsAppInstalled`` function. This function will return ``true`` if any WhatsApp client is installed. E.g:

```
if(whatsAppOtpHandler.isWhatsAppInstalled(context)) {
   // ... do something
} 
```

Or, if you want to check for a specific WhatsApp installation, you can also use the overloaded function which takes the ``WhatsAppClientType`` as a parameter.

```
if(whatsAppOtpHandler.isWhatsAppInstalled(context, WhatsAppClientType.BUSINESS)) {
   // ... do something
} 
```

You can also use ``isWhatsAppOtpHandshakeSupported`` function which checks if the installed version supports the handshake.

```
// checks if the installed version supports the handshake 
if(whatsAppOtpHandler.isWhatsAppOtpHandshakeSupported(context) {
   // ... do something
} 
```

*as of 21-Sept-2023, all versions are expected support the handshake.


### Receiving the code

Receiving the code from WhatsApp is handled by the ``WhatsAppOtpIncomingIntentHandler`` class.


We offer a functional interface that provides the code directly:

```
whatsAppOtpIncomingIntentHandler.processOtpCode(intent, 
                                               (code) -> { 
                                                  // ... use the code
                                               },
                                               (error, exception) -> {
                                                  // handle error 
                                               });
```

Utility functions are offered if you want to create your own abstraction. The following is a function to extract the code from the WhatsApp intent:

```
WhatsAppOtpIncomingIntentHandler whatsAppOtpIncomingIntentHandler = new WhatsAppOtpIncomingIntentHandler();
String code = whatsAppOtpIncomingIntentHandler.getOtpCodeFromWhatsAppIntent(whatsAppIntent);
```

This function throws an ``InvalidWhatsAppOtpIntentException`` if the PendingIntent within the intent is not from WhatsApp.

Additionally, you can check if the intent came from WhatsApp by using the function

```
boolean intentIsFromWhatsApp = whatsAppOtpIncomingIntentHandler.isIntentFromWhatsApp(whatsAppIntent);
```

### Receiving debug signals

Finally, we also offer a similar interface for you to handle error signals which is also described at the [Authentication Templates documentation](https://developers.facebook.com/docs/whatsapp/business-management-api/authentication-templates/).

The following functional interface is available to handle the debug signals:

```
whatsAppOtpIncomingIntentHandler.processOtpDebugSignals(whatsAppIntent,
                                                        (code) -> { 
                                                           // ... use the code
                                                        },
                                                        (error, exception) -> {
                                                           // handle error 
                                                        });
```

Or you can create your own abstraction by using the following function:

```
DebugSignal debugSignal = whatsAppOtpIncomingIntentHandler.getDebugSignalFromWhatsAppIntent(whatsAppIntent);
```

Similar to the function to get the code, this function throws an ``InvalidWhatsAppOtpIntentException`` if the PendingIntent within the intent is not from WhatsApp.

## License

WhatsApp OTP Android SDK is [MIT licensed](./LICENSE).