![](https://d-una-one.s3.us-east-2.amazonaws.com/gestionado_por_d-una.png)
# DeunaSDK Documentation
[![License](https://img.shields.io/github/license/deuna-developers/deuna-sdk-ios?style=flat-square)](https://github.com/deuna-developers/deuna-sdk-io/LICENSE)
[![Platform](https://img.shields.io/badge/platform-ios-blue?style=flat-square)](https://github.com/deuna-developers/deuna-sdk-ios#)

## Introduction

DeunaSDK is a Android-based SDK designed to facilitate integration with the DEUNA. This SDK provides a seamless way to initialize payments, handle success, error, and close actions, and manage configurations.

Get started with our [📚 integration guides](https://docs.deuna.com/docs/integraciones-del-android-sdk) and [example projects](https://github.com/DUNA-E-Commmerce/deuna-sdk-android/tree/master/examples/basic-integration)



## Installation

### Gradle

You can install DeunaSDK using by adding the following dependency to your `build.gradle` file:

  ```
    implementation("com.deuna.maven:deunasdk:2.0.0")
  ```

## Usage


### Initialization

To use the SDK you need to create one instance of `DeunaSDK`. There are 2 ways that you can create an instance of `DeunaSDK`:

1. Registing a singleton to use the same instance in any part of your code

    ```kotlin
    DeunaSDK.initialize(
        environment = Environment.DEVELOPMENT, // Environment.PRODUCTION , etc
        privateApiKey = "YOUR_PRIVATE_API_KEY",
        publicApiKey = "YOUR_PUBLIC_API_KEY"
    )
    ```
    Now you can use the same instance of DeunaSDK using `DeunaSDK.shared`

    ```kotlin
    DeunaSDK.shared.initCheckout(...)
    ```

2. Instantiation

    ```kotlin

    class MyClass {
        private lateinit val deunaSDK: DeunaSDK
    
        init {
            deunaSDK =  DeunaSDK(
                environment = Environment.DEVELOPMENT,
                privateApiKey = "YOUR_PRIVATE_API_KEY",
                publicApiKey = "YOUR_PUBLIC_API_KEY"
            )
        }

        fun buy(){
            deunaSdk.initCheckout(...)
        }
    }

    ```

### Launch the Checkout

This method lauches the checkout process. It sets up the WebView, checks for internet connectivity, and loads the payment link.

```kotlin
class MyClass: AppCompatActivity() {
    val deunaSDK: DeunaSDK ....

    fun buy(orderToken:String){
        val callbacks =  CheckoutCallbacks().apply {
            onSuccess = { response ->
                deunaSDK.closeCheckout()
               // show the success view
            }
            onError = { error ->
                if (error != null) {
                    deunaSDK.closeCheckout()
                }
            }
            eventListener = { type, response ->
                when(type){
                    ...
                }
            }
            onClose = {
                // the chekout view was closed
            }
        }

        deunaSDK.initCheckout(
            context = this,
            orderToken = orderToken,
            callbacks = callbacks,
            closeOnEvents = arrayOf(CheckoutEvent.linkFailed)
        )
    }
}
```


### Launch the VAULT WIDGET

This method lauches the elements process. It sets up the WebView, checks for internet connectivity, and loads the elements link.

```kotlin
class MyClass: AppCompatActivity() {
    val deunaSDK: DeunaSDK ....

    fun saveCard(userToken:String){
        val callbacks =  ElementsCallbacks().apply {
            onSuccess = { response ->
               deunaSDK.closeElements()
               // show the success view
            }
            onError = { error ->
                if (error != null) {
                    deunaSDK.closeElements()
                }
            }
            eventListener = { type, response ->
                when(type){
                    ...
                }
            }
            onClose = {
                // the elements view was closed
            }
        }

        deunaSDK.initElements(
            context = this,
            element = ElementType.VAULT,
            orderToken = userToken,
            callbacks = callbacks,
        )
    }
}
```


## CHANGELOG
Check all changes [here](https://github.com/DUNA-E-Commmerce/deuna-sdk-android/blob/master/CHANGELOG.md)

## Author
DUENA Inc.

## License
DEUNA's SDKs and Libraries are available under the MIT license. See the LICENSE file for more info.
