/*
 * Vechain Wallet SDK is licensed under the MIT LICENSE, also included in LICENSE file in the repository.
 *
 * Copyright (c) 2019 VeChain support@vechain.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.vechain.wallet.network;

import com.google.gson.reflect.TypeToken;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.vechain.wallet.network.bean.common.HttpResult;
import com.vechain.wallet.network.bean.core.AccountCode;
import com.vechain.wallet.network.bean.core.AccountStorage;
import com.vechain.wallet.network.bean.core.call.BatchCallData;
import com.vechain.wallet.network.bean.core.Block;
import com.vechain.wallet.network.bean.core.call.CallData;
import com.vechain.wallet.network.bean.core.Peer;
import com.vechain.wallet.network.bean.core.SendTransactionRaw;
import com.vechain.wallet.network.bean.core.SendTransactionResult;
import com.vechain.wallet.network.bean.core.TokenBalance;
import com.vechain.wallet.network.bean.core.TransactionReceipt;
import com.vechain.wallet.network.bean.core.TransactionItem;
import com.vechain.wallet.network.bean.core.VETBalance;
import com.vechain.wallet.network.bean.core.call.CallResult;
import com.vechain.wallet.network.bean.logs.EventFilter;
import com.vechain.wallet.network.bean.logs.LogEvent;
import com.vechain.wallet.network.bean.logs.LogTransfer;
import com.vechain.wallet.network.bean.logs.TransferFilter;
import com.vechain.wallet.network.bean.version.LastVersion;
import com.vechain.wallet.network.bean.version.Version;
import com.vechain.wallet.network.engine.NetCallBack;
import com.vechain.wallet.network.engine.Network;
import com.vechain.wallet.utils.StringUtils;
import com.vechain.wallet.thor.tx.Clause;
import com.vechain.wallet.utils.HexUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;


public class Api {


    //Block chain node url
    private static NodeUrl nodeUrl = NodeUrl.MAIN_NODE;

    public static NodeUrl getNodeUrl() {
        return nodeUrl;
    }

    public static void setNodeUrl(NodeUrl nodeUrl) {
        Api.nodeUrl = nodeUrl;
    }

    public static HashMap<String, String> getHeader() {
        HashMap<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");
        header.put("platformType", "Android");
        header.put("mobileType", android.os.Build.MODEL);
        header.put("osVersion", String.valueOf(android.os.Build.VERSION.SDK_INT));
        //header.put("softwareVersion", "1.0.0");

        header.put("requestId", String.valueOf(System.currentTimeMillis()) + StringUtils.getRandomString(4));

        return header;
    }

    public static String getBlockChainHost() {

        return nodeUrl.getUrl();
    }


    public static void getAccountCode(String address, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();


        String host = getBlockChainHost();

        String url = host + "/accounts/" + address + "/code";

        Type type = new TypeToken<AccountCode>() {
        }.getType();

        Network.load(url, header, querys, type, callBack, provider);
    }

    public static void getAccountStorage(String address, String key, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();


        String host = getBlockChainHost();

        String url = host + "/accounts/" + address + "/storage/" + key;

        Type type = new TypeToken<AccountStorage>() {
        }.getType();

        Network.load(url, header, querys, type, callBack, provider);
    }

    //vet balance
    public static void getVETBalance(String address, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();


        String host = getBlockChainHost();

        String url = host + "/accounts/" + address;

        Type type = new TypeToken<VETBalance>() {
        }.getType();

        Network.load(url, header, querys, type, callBack, provider);
    }

    //token balance
    public static void getTokenBalance(String tokenAddress, String walletAddress, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();


        String host = getBlockChainHost();

        String url = host + "/accounts/" + tokenAddress;

        Type type = new TypeToken<TokenBalance>() {
        }.getType();

        String walletHex = HexUtils.cleanHexPrefix(walletAddress);

        Clause item = new Clause();
        item.setValue("0x0");
        String prefix = "0x70a08231";
        String address = prefix + "000000000000000000000000" + walletHex;
        item.setData(address);


        Network.upload(url, header, querys, item, type, callBack, provider);

    }

    //Contract Balance
    public static void getMethodBalance(String methodId, String methodAddress, String methodData,
                                        NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();


        String host = getBlockChainHost();

        String url = host + "/accounts/" + methodAddress;

        Type type = new TypeToken<TokenBalance>() {
        }.getType();

        String data = HexUtils.cleanHexPrefix(methodData);
        data = StringUtils.getParamTo32BitsLength(data);
        data = data.toLowerCase();

        Clause item = new Clause();
        item.setValue("0x0");
        String method = methodId + data;
        item.setData(method);


        Network.upload(url, header, querys, item, type, callBack, provider);

    }


    // to simulate execution of a transaction.
    public static void simulateBatchContractCall(BatchCallData data, String revision, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();

        String host = getBlockChainHost();

        if (revision == null || revision.isEmpty())
            revision = "best";

        String url = host + "/accounts/*?revision=" + revision;


        Type type = new TypeToken<List<CallResult>>() {
        }.getType();

        Network.upload(url, header, querys, data, type, callBack, provider);
    }

    // to simulate execution of a transaction.
    public static void simulateAContractCall(CallData data, String contractAddress, String revision, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();

        String host = getBlockChainHost();

        if (revision == null || revision.isEmpty())
            revision = "best";

        String url = host + "/accounts/" + contractAddress + "?revision=" + revision;


        Type type = new TypeToken<CallResult>() {
        }.getType();

        Network.upload(url, header, querys, data, type, callBack, provider);
    }

    // to simulate execution of a transaction.
    public static void simulateAContractCall(CallData data, String revision, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();

        String host = getBlockChainHost();

        if (revision == null || revision.isEmpty())
            revision = "best";

        String url = host + "/accounts?revision=" + revision;


        Type type = new TypeToken<CallResult>() {
        }.getType();

        Network.upload(url, header, querys, data, type, callBack, provider);
    }

    ///logs/event:Filter event logs
    // Event logs are produced by OP_LOG in EVM.
    public static void logEvent(EventFilter eventFilter, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();

        String host = getBlockChainHost();


        String url = host + "/logs/event";


        Type type = new TypeToken<List<LogEvent>>() {
        }.getType();

        Network.upload(url, header, querys, eventFilter, type, callBack, provider);

    }


    ///logs/transfer :Filter transfer logs
    // Transfer logs are recorded on VET transferring.
    public static void logTransfer(TransferFilter transferFilter, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {

        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();

        String host = getBlockChainHost();


        String url = host + "/logs/transfer";


        Type type = new TypeToken<List<LogTransfer>>() {
        }.getType();

        Network.upload(url, header, querys, transferFilter, type, callBack, provider);
    }


    // get baseGasPrice
    public static void getBaseGasPrice(NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();


        String host = getBlockChainHost();

        String url = host + "/accounts/0x0000000000000000000000000000506172616D73";

        Type type = new TypeToken<TokenBalance>() {
        }.getType();


        Clause item = new Clause();
        item.setValue("0x0");
        item.setData("0x8eaa6ac0000000000000000000000000000000000000626173652d6761732d7072696365");


        Network.upload(url, header, querys, item, type, callBack, provider);

    }


    //transfer
    public static void sendTransaction(String raw, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {

        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();


        String host = getBlockChainHost();

        String url = host + "/transactions";

        Type type = new TypeToken<SendTransactionResult>() {
        }.getType();

        SendTransactionRaw sendRaw = new SendTransactionRaw();
        sendRaw.setRaw(raw);

        Network.upload(url, header, querys, sendRaw, type, callBack, provider);
    }


    //Get Genesis Block
    public static void getZeroBlock(NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        getBlock("0", callBack, provider);
    }

    //Get the latest block information
    public static void getBestBlock(NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        getBlock("best", callBack, provider);
    }

    public static void getBlock(String blockId, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();


        String host = getBlockChainHost();

        String url = host + "/blocks/" + blockId;

        Type type = new TypeToken<Block>() {
        }.getType();

        Network.load(url, header, querys, type, callBack, provider);
    }

    public static void getPeers(NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();


        String host = getBlockChainHost();

        String url = host + "/node/network/peers";

        Type type = new TypeToken<List<Peer>>() {
        }.getType();

        Network.load(url, header, querys, type, callBack, provider);
    }

//    public static void getTransactionById(String id,NetCallBack callBack, LifecycleProvider<ActivityEvent> provider){
//        HashMap<String, String> header = new HashMap<>();
//        header.put("Content-Type", "application/json");
//
//        HashMap<String, String> querys = new HashMap<>();
//
//
//        String host = GlobalConfig.getBlockChainHost();
//
//        String url = host + "/transactions/"+id;
//
//        Type type = new TypeToken<TransanctionFromId>() {
//        }.getType();
//
//        Network.load(url, header, querys, type, callBack,provider);
//    }

    public static void getTransactionById(String id, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = new HashMap<>();
        header.put("Content-Type", "application/json");

        HashMap<String, String> querys = new HashMap<>();


        String host = getBlockChainHost();

        String url = host + "/transactions/" + id;

        Type type = new TypeToken<TransactionItem>() {
        }.getType();

        Network.load(url, header, querys, type, callBack, provider);
    }

    //Get details of the transaction
    public static void getTransactionReceipt(String id, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();


        String host = getBlockChainHost();

        String url = host + "/transactions/" + id + "/receipt";

        Type type = new TypeToken<TransactionReceipt>() {
        }.getType();

        Network.load(url, header, querys, type, callBack, provider);
    }


    //token symbol:
    //Keccak-256("symbol()")="0x95d89b41e2f5f391a79ec54e9d87c79d6e777c63e32c28da95b4e9e4a79250ec"
    //Take the first 4 bytes：0x95d89b41
    //Reference：
    //1.https://www.4byte.directory/signatures/
    //2.https://github.com/vechain/VIPs/blob/master/VIP-180.md
    public static void getTokenSymbol(String tokenAddress, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();


        String host = getBlockChainHost();

        String url = host + "/accounts/" + tokenAddress;

        Type type = new TypeToken<TokenBalance>() {
        }.getType();


        Clause item = new Clause();
        item.setValue("0x0");
        String methodId = "0x95d89b41";
        item.setData(methodId);


        Network.upload(url, header, querys, item, type, callBack, provider);
    }

    //token decimals:
    //Keccak-256("decimals()")="0x313ce567add4d438edf58b94ff345d7d38c45b17dfc0f947988d7819dca364f9"
    //Take the first 4 bytes：0x313ce567
    //Reference：
    //1.https://www.4byte.directory/signatures/
    //2.https://github.com/vechain/VIPs/blob/master/VIP-180.md
    public static void getTokenDecimals(String tokenAddress, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();


        String host = getBlockChainHost();

        String url = host + "/accounts/" + tokenAddress;

        Type type = new TypeToken<TokenBalance>() {
        }.getType();

        Clause item = new Clause();
        item.setValue("0x0");
        String methodId = "0x313ce567";
        item.setData(methodId);


        Network.upload(url, header, querys, item, type, callBack, provider);
    }

    public static Call<ResponseBody> getUrl(String url, HashMap<String, String> header) {

        return Network.download(url, header);
    }


    public static void getVersion(String language, NetCallBack callBack, LifecycleProvider<ActivityEvent> provider) {
        HashMap<String, String> header = getHeader();

        HashMap<String, String> querys = new HashMap<>();


        String host = getUpdateVersionUrl();

        String url = host + "/api/v1/version/";

        Type type = new TypeToken<HttpResult<LastVersion>>() {
        }.getType();

        Version version = new Version();
        version.setAppid(getAppId());
        version.setLanguage(language);

        Network.upload(url, header, querys, version, type, callBack, provider);
    }

    private static String getUpdateVersionUrl() {

        if (nodeUrl == NodeUrl.MAIN_NODE) {
            return "https://version-management.vechain.com";
        } else {
            return "https://version-management-test.vechaindev.com";
        }
    }

    private static String getAppId(){
        if (nodeUrl == NodeUrl.MAIN_NODE) {
            return "5c50bb8d9736b60f1f0f4f5c56604326";
        } else {
            return "27a7898b733ce99d90ec5338de5ced52";
        }
    }

}
