# DS Card Scanner.

This is a credit and debit card scanner successfull for most of the card formats. We are updating it with live card scanning asap.

## How to implement ?

To implement it in your project you can add the below dependency in your `app/buid.gradle`

```gradle
dependencies {
    ...
	implementation 'com.github.gsazu:dscardscanner:1.0.3'
}
```

In your `settings.gradle`, add the below maven url:
```gradle
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
            ...
			maven { url 'https://jitpack.io' }
		}
	}
```


## How to call ?

To call the scanner, you can use the below example code:

```koltin
DsScannerFactory.create(this).openScanner(object: DsScannerFactory.OnCardScanResponseListener {
                override fun cardScanResponse(cardDetails: CardDetails) {
                    binding.tvHelloWorld.text = "Card Name: ${cardDetails.cardName}\n" +
                            "Card Number: ${cardDetails.cardNumber}\n" +
                            "Card Expiry: ${cardDetails.cardExpiry}\n" +
                            "Card CVV: ${cardDetails.cardCvv}"
                }

            })
```
