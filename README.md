# EthBadge

### Description
EthBadge is an open-source app for Android devices that uses Ethereum Name Service (ENS) to verify the device's owner identity.
Source code and documentation: [GitHub](https://github.com/js0p/EthBadge)
Questions and suggestions: [Gitter](https://gitter.im/EthBadge/Lobby)

### Open BETA
App can be downloaded for beta-testing here:
[https://play.google.com/apps/testing/org.jmpm.ethbadge](https://play.google.com/apps/testing/org.jmpm.ethbadge)

### Usage
There are two possible roles when using the app: Visitor and Host.

* As a Visitor:
1) Tap on "Settings" and enter the private key of the address that owns your ENS domain name.
2) When close to the Host, tap on "Show badge" and select the Bluetooth ID of the Host among the list of available devices.

* As a Host:
Just open your app and tap on "Check badge" when a Visitor identification is requested.

### Use cases

- Building Access Control
Example: Visitor claims to own the domain name "JohnEmployee.Company.eth". Host checks that the domain name translates to the address 0xeF7726B981aD5b55D845475544c9e65A77244cF5. Host also checks that Visitor owns this address asking the Visitor to sign a message with the private key corresponding to that address.

- Courier Identification

Coming possible applications are:
- Business Card
- ID Card / Passport


### Q&A

- Do I need to store my private key?
No, the private key to be stored in the app is the one the ENS name translates to, not the owner of the name. This private key does not need to be your personal private key but from any wallet which can be created only for identification purposes.

- What if my device is stolen or hacked? Will the attacker be able to use my identity?
EthBadge uses Android's keystore system and AES encryption whose key is the hash of a password the user enters. If, for example, the device is stolen, the attacker would need to know that password too.


### Donations
To support the development of this app, ETH can be donated to 0x935bad297B03568DF27676244e5E47a148cffe82


The source code and documentation is available in [here](https://github.com/js0p/EthBadge)

