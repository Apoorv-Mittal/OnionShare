# OnionShare


![Screen 1](/readme-assets/s1.jpg "Screen 1")

Screen 1: Shows the splash screen with the spinner, where our apps connects to TOR network

![Screen 2](/readme-assets/s2.jpg "Screen 2")

Screen 2: Shows the data fetched over the TOR network (shows connectivity) 

![Screen 3](/readme-assets/s3.jpg "Screen 3")

Screen 3: Shows the URL of our apps server over the TOR network in the Upload Fragment with working copy button

![Screen 4](/readme-assets/s4.jpg "Screen 4")

Screen 4: Shows our Download Fragment with a working paste button


What is left:

- [ ] On the upload fragment actually be able to upload data
  - [ ] Show the Upload button and all the interactions related to that
  - [ ] After Selection upload to the network
  - [ ] Show all the uploaded files on the TextView
- [ ] On the download fragment. 
  - [ ] Implement the View Files button to fetch the data from the pasted URL
  - [ ] Show the fetched files in the Text View with a CheckBox 
  - [ ] Implement the Download Button 

**Right now the app can connect and access TOR network.**

- Cannot use NetCipher to send and receive files because it needs a orbot installed which is which is what actually makes the connection to TOR
  - <https://stackoverflow.com/questions/52547255/orbot-netcipher-connecting-to-onion-urls>
  - https://stackoverflow.com/questions/48628010/android-studio-how-to-use-netcipher
  - <https://github.com/guardianproject/NetCipher>
  - https://commonsware.com/misc/NetCipher.pdf
- Always remember that the the app will only work on the mobile/device and not on an emulator
- Could not use <https://github.com/thaliproject/Tor_Onion_Proxy_Library> because it was not available through <https://jitpack.io/> or maven repository so had to use a better documented fork of the same library
- Therefore used <https://github.com/jehy/Tor-Onion-Proxy-Library> and have incorporated this in the app
