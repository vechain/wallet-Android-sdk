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

package com.vechain.wallet.network.bean.core;

import com.vechain.wallet.thor.tx.Clause;

import java.util.List;

public class TransactionItem {

//    {
//        "id": "0x4de71f2d588aa8a1ea00fe8312d92966da424d9939a511fc0be81e65fad52af8",
//            "chainTag": 1,
//            "blockRef": "0x00000001511fc0be",
//            "expiration": 30,
//            "clauses": [],
//        "gasPriceCoef": 128,
//            "gas": 21000,
//            "origin": "0x7567d83b7b8d80addcb281a71d54fc7b3364ffed",
//            "nonce": "0xd92966da424d9939",
//            "dependsOn": null,
//            "size": 180,
//            "meta": {
//        "blockID": "0x00000001c458949985a6d86b7139690b8811dd3b4647c02d4f41cdefb7d32327",
//                "blockNumber": 1,
//                "blockTimestamp": 1523156271
//    }
//    }


    private String id;
    private int chainTag;
    private String blockRef;
    private int expiration;
    private List<Clause> clauses;
    private int gasPriceCoef;
    private long gas;
    private String origin;
    private String nonce;
    private String dependsOn;
    private int size;
    private LogMeta meta;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getChainTag() {
        return chainTag;
    }

    public void setChainTag(int chainTag) {
        this.chainTag = chainTag;
    }

    public String getBlockRef() {
        return blockRef;
    }

    public void setBlockRef(String blockRef) {
        this.blockRef = blockRef;
    }

    public int getExpiration() {
        return expiration;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public List<Clause> getClauses() {
        return clauses;
    }

    public void setClauses(List<Clause> clauses) {
        this.clauses = clauses;
    }

    public int getGasPriceCoef() {
        return gasPriceCoef;
    }

    public void setGasPriceCoef(int gasPriceCoef) {
        this.gasPriceCoef = gasPriceCoef;
    }

    public long getGas() {
        return gas;
    }

    public void setGas(long gas) {
        this.gas = gas;
    }

    public String getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(String dependsOn) {
        this.dependsOn = dependsOn;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }


    public LogMeta getMeta() {
        return meta;
    }

    public void setMeta(LogMeta meta) {
        this.meta = meta;
    }
}
