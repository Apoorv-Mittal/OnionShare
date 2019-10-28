# OnionShare

Right now the app can connect and access TOR network.

- Cannot use NetCipher to send and receive files because it needs a orbot installed which is which is what actually makes the connection to TOR
  - <https://stackoverflow.com/questions/52547255/orbot-netcipher-connecting-to-onion-urls>
  - https://stackoverflow.com/questions/48628010/android-studio-how-to-use-netcipher
  - <https://github.com/guardianproject/NetCipher>
  - https://commonsware.com/misc/NetCipher.pdf
- Always remember that the the app will only work on the mobile/device and not on an emulator
- Could not use <https://github.com/thaliproject/Tor_Onion_Proxy_Library> because it was not available through <https://jitpack.io/> or maven repository so had to use a better documented fork of the same library
- Therefore used <https://github.com/jehy/Tor-Onion-Proxy-Library> and have incorporated this in the app
