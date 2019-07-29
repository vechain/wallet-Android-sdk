The wallet function is implemented by calling class: **com.vechain.wallet.WalletUtils.**

### SDK initialization

SDK initialization
Inherit the android.app.Application class and implement the following methods:

```java
protected void attachBaseContext(Context base) {
        super.attachBaseContext(LanguageManager.setLocale(base));
        
        //Set it as a main_net environment
        WalletUtils.setNodeUrl(NodeUrl.MAIN_NODE);
 }
```



#### Set node url

> Annotation
> 
> Node: Any form of server in a block chain network.
> 
> Node url: Unified resource location address for participating block chain servers.


When app is released, it is set as a main_net environment:


```java
WalletUtils.setNodeUrl(NodeUrl.MAIN_NODE);
```


Or if you have a corresponding node url, you can change it to your own node url:
```java
//String customUrl = "https://www.yourCustomNodeUrl.com";
NodeUrl.CUSTOM_NODE.setUrl(customUrl);
WalletUtils.setNodeUrl(NodeUrl.CUSTOM_NODE);
```
            

Switching test_net environment:
```java
WalletUtils.setNodeUrl(NodeUrl.TEST_NODE);
```
If nodeUrl is not set, the default value is NodeUrl.MAIN_NODE.
            

### Get node url
```java
String nodeUrl = WalletUtils.getNodeUrl();
```

#### Create Wallet
After entering the wallet password, the user can create the wallet through the following methods:

```java
WalletUtils.OnCreateWalletCallback callback = new WalletUtils.OnCreateWalletCallback() {
    @Override
    public void onCreate(WalletUtils.Wallet wallet) {

        closeDialog();

        if (wallet == null) {
            //fail
            createFail();

        }else {
            //success
            words = wallet.getWords();
            keystore = wallet.getKeystore();
            String privateKey = wallet.getPrivateKey();
            String address = wallet.getAddress();

            //Keystore is saved in files or databases
            FileUtils.writeCache(CreateWalletActivity.this, FileUtils.KEYSTORE_NAME, keystore);

            createSuccess();
        }
    }
};

WalletUtils.createWallet(password, callback);
```
When the wallet is created successfully, the wallet object is obtained:com.vechain.wallet.WalletUtils.Wallet.If the creation fails, WalletUtils.Wallet wallet is null.
After successful creation, keystore security can be cached to local files or databases.

####  Create wallet with mnemonic words

When the user has mnemonic words, enter the mnemonic words and the password of the keystore of the wallet to import the wallet. The method of using the mnemonic words as follows:

```java
WalletUtils.OnCreateWalletCallback callback = new WalletUtils.OnCreateWalletCallback() {
    @Override
    public void onCreate(WalletUtils.Wallet wallet) {

        closeLoading();
        if(wallet!=null) {
            keystore = wallet.getKeystore();
            if (!TextUtils.isEmpty(keystore))
               
               //Keystore is saved in files or databases
               //FileUtils.writeCache(getContext(), FileUtils.KEYSTORE_NAME, keystore);

            //success
            onImportSuccess();
        }else{
            //errors
            onImportWalletError(false);
        }
    }

};
WalletUtils.createWallet(words, password, callback);
```

#### Verify the mnemonic words


```java
/**
 * Whether mnemonic words is valid or not
 *
 * @param The range of mnemonic words is limited to 12, 15, 18, 21 and 24.
 * @return Is it valid or not
 */
public static boolean isValidMnemonicWords(List<String> words)
```

Eg.
```java
String words = "enact again rate alone congress scheme solid theory flush length twenty head";
List<String> mnemonicWords = new ArrayList<>();
String[] strings = words.split(" ");

for (String item : strings) {
    mnemonicWords.add(item);
}
boolean isValid = WalletUtils.isValidMnemonicWords(mnemonicWords);
//isValid:true

```




#### Verify keystore format


```java
/**
 * Whether keystore for JSON format is correct or not
 *
 * @param keystore JSON encryption format for user wallet private key
 * @return Is the format correct or not?
 */
public static boolean isValidKeystore(String keystore)
```
Eg.

```java
String keystoreJson = "{\"address\":\"36d7189625587d7c4c806e0856b6926af8d36fea\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"cipherparams\":{\"iv\":\"c4a723d57e1325a99d88572651959a9d\"},\"ciphertext\":\"73a4a3a6e8706d099b536e41f6799e71ef9ff3a9f115e21c58d9e81ade036705\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"a322d4dce0f075f95a7748c008048bd3f80dbb5645dee37576ea93fd119feda2\"},\"mac\":\"66744cc5967ff5858266c247dbb088e0986c6f1d50156b5e2ce2a19afdc0e498\"},\"id\":\"0fe540de-1957-4bfe-a326-16772e61f677\",\"version\":3}";

boolean isValid = WalletUtils.isValidKeystore(keystoreJson);
//isValid:true
```


#### Get address from keystore 


```java
/**
 * Get the address from the keystore
 *
 * @param keystoreJson
 * @return wallet address
 */
public static String getAddress(String keystoreJson)
```
Eg.


```java
String keystoreJson = "{\"address\":\"36d7189625587d7c4c806e0856b6926af8d36fea\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"cipherparams\":{\"iv\":\"c4a723d57e1325a99d88572651959a9d\"},\"ciphertext\":\"73a4a3a6e8706d099b536e41f6799e71ef9ff3a9f115e21c58d9e81ade036705\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"a322d4dce0f075f95a7748c008048bd3f80dbb5645dee37576ea93fd119feda2\"},\"mac\":\"66744cc5967ff5858266c247dbb088e0986c6f1d50156b5e2ce2a19afdc0e498\"},\"id\":\"0fe540de-1957-4bfe-a326-16772e61f677\",\"version\":3}";

String address = WalletUtils.getAddress(keystoreJson);

//address:0x36D7189625587D7C4c806E0856b6926Af8d36FEa
```



#### Verify the keystore with password

 Verify that the keystore password is correct or not when the keystore is cached into local file or database.
```java
WalletUtils.OnVerifyPasswordCallback callback = new WalletUtils.OnVerifyPasswordCallback() {
    @Override
    public void onCheckPassword(boolean success) {
        if (success) {
            //success
            onImportSuccess();
           
           //Keystore is saved in files or databases
           //FileUtils.writeCache(getActivity(), FileUtils.KEYSTORE_NAME, keystoreJson);
        } else {
            //errors
            onImportWalletError();
        }
    }
};
WalletUtils.verifyKeystorePassword(keystoreJson, password, callback);
```

####  Modify password of keystore



```java
WalletUtils.OnModifyKeystorePasswordCallback callback = new WalletUtils.OnModifyKeystorePasswordCallback() {
    @Override
    public void onModifyResult(String newKeystore) {

        if (!TextUtils.isEmpty(newKeystore)) {
            keystoreJson = newKeystore;
           
        //Keystore is saved in files or databases
        //FileUtils.writeCache(getBaseContext(), FileUtils.KEYSTORE_NAME, newKeystore);

            //success
            onModifyPasswordSuccess();
        } else {
            //errors
            onModifyPasswordError();
        }
    }
};
WalletUtils.modifyKeystorePassword(keystoreJson, oldPassword, newPassword, callback);
```



####  Decrypt keystore


```java
/**
* Keystore decrypts private key
*
* @param keystoreJson
* @param password
* @param callback private key
*/
public static void decryptKeystore(final String keystoreJson, final String password,
                               final DecryptKeystoreCallback callback)
```
Eg.


```java
String keystoreJson = "{\"address\":\"36d7189625587d7c4c806e0856b6926af8d36fea\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"cipherparams\":{\"iv\":\"c4a723d57e1325a99d88572651959a9d\"},\"ciphertext\":\"73a4a3a6e8706d099b536e41f6799e71ef9ff3a9f115e21c58d9e81ade036705\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"a322d4dce0f075f95a7748c008048bd3f80dbb5645dee37576ea93fd119feda2\"},\"mac\":\"66744cc5967ff5858266c247dbb088e0986c6f1d50156b5e2ce2a19afdc0e498\"},\"id\":\"0fe540de-1957-4bfe-a326-16772e61f677\",\"version\":3}";

WalletUtils.DecryptKeystoreCallback callback = new WalletUtils.DecryptKeystoreCallback() {
    @Override
    public void decryptResult(String privateKey) {

        Log.v("TAG", privateKey);
        //privateKey:0xbc9fe2428a8faec37674412c113f4a9a66b2e40076014547bfe7bbdc2c5a85ee
    }
};
WalletUtils.decryptKeystore(keystoreJson, "123456", callback);
```



#### Encrypted private key


```java
/**
* Private key encryption generates keystore
*
* @param privateKey
* @param password
* @param callback keystore
*/
public static void encryptPrivateKey(final String privateKey, final String password,
                                 final EncryptPrivateKeyCallback callback)
```
Eg.


```java
String privateKey = "0xbc9fe2428a8faec37674412c113f4a9a66b2e40076014547bfe7bbdc2c5a85ee";
WalletUtils.EncryptPrivateKeyCallback callback = new WalletUtils.EncryptPrivateKeyCallback() {
    @Override
    public void encryptPrivateKeyResult(String keystoreJson) {

        String address = WalletUtils.getAddress(keystoreJson);
        //address:0x36D7189625587D7C4c806E0856b6926Af8d36FEa
    }
};
WalletUtils.encryptPrivateKey(privateKey, "123", callback);
```




#### Get checksum address

   
```java
/**
 * Generate wallet addresses in check format
 *
 * @param address Wallet address
 * @return Address with Check Format
 */
public static String getChecksumAddress(String address)
```
Eg.


```java
String address = "0x36d7189625587d7c4c806e0856b6926af8d36fea";

String checkSumAddress = WalletUtils.getChecksumAddress(address);

//checkSumAddress:0x36D7189625587D7C4c806E0856b6926Af8d36FEa
```



####  Get reference of block chain

Get the last eight bytes of the latest block ID and output it with a hexadecimal string (blockRef).

```java
WalletUtils.OnGetBlockReferenceCallback callback = new WalletUtils.OnGetBlockReferenceCallback() {
    @Override
    public void onGetBlockReferenceResult(String blockRef) {
        blockReference = blockRef;
    }
};
WalletUtils.getBlockReference(callback);
```
####  Get chainTag of block chain

Get the tag of the current block chain,Tag is used to distinguish different chains

```java
WalletUtils.OnGetChainTagCallback callback = new WalletUtils.OnGetChainTagCallback() {
    @Override
    public void onGetChainTagResult(String chainTag) {
        this.chainTag = chainTag;
        //MAIN_NODE chainTag:0x4a
        //TEST_NODE chainTag:0x27
    }
};
WalletUtils.getChainTag(callback);
```


####  Sign and send Transaction

Sign the transaction and send it to the block chain.

```java
WalletUtils.OnSignCallback callback = new WalletUtils.OnSignCallback() {
    @Override
    public void onSignResult(String txId) {

        if (transferResultListener != null)
            transferResultListener.onTransferResult(txId);


    }
};
WalletUtils.signAndSendTransfer(keystoreJson, password, transferParameter, callback);
```
After the transaction is sent, txId can be acquired. Through txId, the transaction status can be further acquired, and the success of the transaction can be confirmed by multiple blocks.

- keystoreJson: JSON format for private key encryption

- password: keystore password

- transferParameter:


```java
String hexNonce = null;
byte[] nonceBytes = new byte[8];
Random random = new Random();
random.nextBytes(nonceBytes);
hexNonce = HexUtils.toHexString(nonceBytes);

TransferParameter.Builder builder = new TransferParameter.Builder();
TransferParameter transferParameter = null;
try {
    transferParameter = builder.setClauseList(clauseList)
            .setGas(gas)
            .setChainTag(chainTag)
            .setBlockRef(blockRef)
            .setNonce(hexNonce)
            .build();
} catch (ParameterException e) {
    e.printStackTrace();
}
```
TransferParameter attribute description：

- clauses : List<Clause> - Multi-transaction information

- gas : long  - Enforces the specified number as the maximum gas that can be consumed for the transaction

- chainTag : String - Genesis block ID last byte hexadecimal.Reference: WalletUtils.getChainTag().

- blockRef : String - Refer to the last 8 bytes of blockId in hexadecimal.Reference: WalletUtils.getBlockReference().

- nonce : String  - The random number of trading entities. Changing Nonce can make the transaction have different IDs, which can be used to accelerate the trader.

- gasPriceCoef : int - This value adjusts the priority of the transaction, and the current value range is between 0 and 255 (decimal),The default value is "0"

- expiration : String - Deal expiration/expiration time in blocks,The default value is "720"

- dependsOn :String - The ID of the dependent transaction. If the field is not empty, the transaction will be executed only if the transaction specified by it already exists.The default value is null.

- reserveds: List  -  Currently empty, reserve fields. Reserved fields for backward compatibility,The default value is null



        
####  Sign transaction

RLP encoding and signature of transactions will not be sent to the block chain.The result is retrieved by callback, which retrieves the raw is hexadecimal string.

```java
WalletUtils.OnSignCallback callback = new WalletUtils.OnSignCallback() {
    @Override
    public void onSignResult(String raw) {
        //raw: RLP encode data and signature
    }
};
WalletUtils.signTransfer(keystoreJson, password, transferParameter, callback);

```





####  Support DApp development environment in Webview
To support the Dapp function, WebView needs the following initialization before opening Dapp.
Initialization is mainly JS injected into connex and web3.
[connex reference.](https://github.com/vechain/connex/blob/master/docs/api.md/)



```java
WebSettings set = webView.getSettings();
set.setJavaScriptEnabled(true);

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    set.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
}


webView.setWebViewClient(new MyWebClient());
webView.setWebChromeClient(new MyWebChromeClient(pluginManager));        

```
MyWebChromeClient is implemented as follows. If you want to implement WebChromeClient, your WebChromeClient must inherit DappWebChromeClient.

 
```java
class MyWebChromeClient extends DAppWebChromeClient {

    public MyWebChromeClient(PluginManager pluginManager){
        super(pluginManager);
    }
    @Override
    public void onReceivedTitle(WebView view, String title) {
        //must be implemented to support the DApp environment
        super.onReceivedTitle(view, title);
        ....
    }    
    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {

        //must be implemented to support the DApp environment
        return super.onJsPrompt(view,url,message,defaultValue,result);
    }

}
```
MyWebClient is implemented as follows. If you want to implement WebViewClient, your WebViewClient must inherit DAppWebViewClient.


```java
class MyWebClient extends DAppWebViewClient {

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

        //must be implemented to support the DApp environment
        return super.shouldInterceptRequest(view, request);
    }
}
```


At the same time, the wallet must be initialized before calling WebView to open Url's corresponding Dapp, as follows：Implementing DAppAction Interface
        
```java
public class DAppWebActivity extends Activity implements DAppAction{
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ...
        //Support for DApp must be initialized
        pluginManager = new PluginManager();
        pluginManager.setdAppAction(this);
        ...
    }
    
    ...
    
    /**
     *  Interfaces that must be implemented to support the DApp environment
     *  App developer implementation when dapp calls checkOwn address function
     *  
     *  When will this method be called? When DAPP needs to know if it has address locally
     *  
     * @param address Address from dapp
     * @param dAppCallback Callback after the end，notify DAP if the address is locally owned
     */
    public void onCheckOwnAddress(String address, OwnedAddressCallback dAppCallback){

        //Read the wallet keystore from a file or database
        String keystore = FileUtils.readCache(this, FileUtils.KEYSTORE_NAME);
        final String walletAddress = WalletUtils.getAddress(keystore);
        boolean isOwn = walletAddress.equalsIgnoreCase(address);
        if(dAppCallback!=null)
            dAppCallback.onOwned(isOwn);
    }

        /**
     * Interfaces that must be implemented to support the DApp environment
     *
     * When will this method be called? This method is called when DAPP needs to issue certificate using a local Wallet
     *
     * @param message  Information to be signed
     * @param dAppCallback Callback method after signature information is completed，notify DAPP when the result is completed
     */
    @Override
    public void onCertificate(final String message, final String signer,final CertificateCallback dAppCallback) {
        //Interfaces that must be implemented to support the DApp environment
        //Read the wallet keystore from a file or database
        String keystore = FileUtils.readCache(this, FileUtils.KEYSTORE_NAME);
        final String walletAddress = WalletUtils.getAddress(keystore);

        String certificateMessage = message;
        // if signer not null, Enforces the specified address to sign the certificate
        if(signer!=null && !signer.equalsIgnoreCase(walletAddress)){
            if (dAppCallback != null) dAppCallback.onCertificate(null,null);
            return;
        }else{
            // if signer is null,Add signer for certificate information
            if(signer==null)
                certificateMessage = WalletUtils.addSigner(message,walletAddress.toLowerCase());
        }


        //Prompt user to enter wallet password
        CertificatePasswordDialog.takePassword(this, keystore, certificateMessage,new CertificateSignListener() {
            @Override
            public void deliverCertificate(String signature) {
                // notify DApp
                if(dAppCallback!=null)dAppCallback.onCertificate(walletAddress,signature);
            }
        });
    }

    /**
     * Interfaces that must be implemented to support the DApp environment
     *
     * When will this method be called? When DAPP needs to use a local wallet to send transactions
     *
     * @param clauses Dapp group of transaction parameters
     * @param gas Miners'Fees Needed by Exchanges
     * @param dAppCallback Transaction Sending Completion Callback Method,notify Dapp the results of the transaction and the address of the wallet that sent the transaction
     */
    @Override
    public void onTransfer(List<Clause> clauses, long gas, final String signer,final TransferResultCallback dAppCallback) {

        //Read the wallet keystore from a file or database
        String keystore = FileUtils.readCache(this, FileUtils.KEYSTORE_NAME);
        final String address = WalletUtils.getAddress(keystore);

        // if signer not null, Enforces the specified address to sign the transaction
        if(signer!=null && !signer.equalsIgnoreCase(address)){
            if(dAppCallback!=null)dAppCallback.onTransferResult(null,null);
            return;
        }

        PasswordDialog.showInputPassword(this, keystore, clauses, gas, new TransferResultListener() {
            @Override
            public void onTransferResult(String result) {

                if(dAppCallback!=null)dAppCallback.onTransferResult(address,result);
            }
        });

    }

    /**
     * Interfaces that must be implemented to support the DApp environment
     *
     * When will this method be called? When DAPP needs to get a list of local wallet addresses
     *
     * Get a list of wallet addresses from a local file or database
     *
     * @param dAppCallback Address list to DApp
     */
    @Override
    public void onGetWalletAddress(GetWalletAddressCallback dAppCallback) {
        //Get the wallet keystore from the local database or file cache
        String keystore = FileUtils.readCache(this, FileUtils.KEYSTORE_NAME);
        String address = WalletUtils.getAddress(keystore);

        //DApp must set addresses
        List<String> addresses = new ArrayList<>();
        addresses.add(address.toLowerCase());

        //Callback method must be executed after completion
        if (dAppCallback != null) dAppCallback.setWalletAddress(addresses);
    }

    ....

}
```
When closing WebView, you can do the following:

```java
protected void onDestroy() {
        if (webView != null)
            webView.destroy();
        webView = null;
        //Memory Recycling to Prevent Memory Leakage
        pluginManager.setdAppAction(null);
        pluginManager.clearWebView();
        pluginManager = null;
        super.onDestroy();
    }
```
#### Tips：
Before the release of DApp, it is recommended that different versions of WebView be adapted to ensure the reliable and stable operation of HTML 5 pages.

## Several main data structures

### 1.keystore

```java
/**
*  Keystore is a json string. Its file structure is as follows:
*
*  — — — — — — — — — — — — — — — — — — — — — — — — — — ——
*      {
*          "version": 3,
*          "id": "F56FDA19-FB1B-4752-8EF6-E2F50A93BFB8",
*          "kdf": "scrypt",
*          "mac": "9a1a1db3b2735c36015a3893402b361d151b4d2152770f4a51729e3ac416d79f",
*          "cipher": "aes-128-ctr"
*          "address": "ea8a62180562ab3eac1e55ed6300ea7b786fb27d"
*          "crypto": {
*                      "ciphertext": "d2820582d2434751b83c2b4ba9e2e61d50fa9a8c9bb6af64564fc6df2661f4e0",
*                      "cipherparams": {
*                                          "iv": "769ef3174114a270f4a2678f6726653d"
*                                      },
*                      "kdfparams": {
*                              "r": 8,
*                              "p": 1,
*                              "n": 262144,
*                              "dklen": 32,
*                              "salt": "67b84c3b75f9c0bdf863ea8be1ac8ab830698dd75056b8133350f0f6f7a20590"
*                      },
*          },
*      }
*
*  — — — — — — — — — — — — — — — — — — — — — — — — — — ——
*  Field description:
*          version: This is a version information, when you decryption, you should use the same version.
*          id: You can ignore.
*          Kdf: This is a encryption function.
*          mac: This is the mac deveice infomation.
*          cipher: Describes the encryption algorithm used.
*          address：The wallet address.
*          crypto: This section is the main encryption area.
*
*  If you want to recover a wallet by keystore, you should have the correct password.
*
*/

```

### 2.Hexadecimal must start with 0x.

### 3.Address : 20 bytes hex string and start with 0x.

### 4.Availabel DApp List
```
https://vechainstats.com
https://oceanex.pro/reg/v190528
https://connex.vexchange.io/swap
https://bmac.vecha.in/generate
https://bc66.github.io/lucky-airdrop/#/
https://explore.veforge.com/
https://laalaguer.github.io/vechain-token-transfer/
https://doc.vechainworld.io/docs
https://vechainstore.com/ide
https://vepress.org/
https://inspector.vecha.in/
https://insight.vecha.in
https://vechaininsider.com/
```