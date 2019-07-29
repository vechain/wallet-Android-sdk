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

public class Block {

//            "number": 325324,
//            "id": "0x0004f6cc88bb4626a92907718e82f255b8fa511453a78e8797eb8cea3393b215",
//            "size": 373,
//            "parentID": "0x0004f6cb730dbd90fed09d165bfdf33cc0eed47ec068938f6ee7b7c12a4ea98d",
//            "timestamp": 1533267900,
//            "gasLimit": 11253579,
//            "beneficiary": "0xb4094c25f86d628fdd571afc4077f0d0196afb48",
//            "gasUsed": 21000,
//            "totalScore": 1029988,
//            "txsRoot": "0x89dfd9fcd10c9e53d68592cf8b540b280b72d381b868523223992f3e09a806bb",
//            "stateRoot": "0x86bcc6d214bc9d8d0dedba1012a63c8317d19ce97f60c8a2ef5c59bbd40d4261",
//            "receiptsRoot": "0x15787e2533c470e8a688e6cd17a1ee12d8457778d5f82d2c109e2d6226d8e54e",
//            "signer": "0xab7b27fc9e7d29f9f2e5bd361747a5515d0cc2d1",
//            "transactions": [
//            "0x284bba50ef777889ff1a367ed0b38d5e5626714477c40de38d71cedd6f9fa477"
//            ],
//            "isTrunk": true

    private long number;
    private String id;
    private int size;
    private String parentID;
    private long timestamp;
    private long gasLimit;
    private String beneficiary;
    private long gasUsed;
    private int totalScore;
    private String txsRoot;
    private String stateRoot;
    private String receiptsRoot;
    private String signer;
    private String[] transactions;
    private boolean isTrunk;
    private Integer txsFeatures;

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

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

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public String getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(String beneficiary) {
        this.beneficiary = beneficiary;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = gasUsed;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public String getTxsRoot() {
        return txsRoot;
    }

    public void setTxsRoot(String txsRoot) {
        this.txsRoot = txsRoot;
    }

    public String getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
    }

    public String getReceiptsRoot() {
        return receiptsRoot;
    }

    public void setReceiptsRoot(String receiptsRoot) {
        this.receiptsRoot = receiptsRoot;
    }

    public String getSigner() {
        return signer;
    }

    public void setSigner(String signer) {
        this.signer = signer;
    }

    public String[] getTransactions() {
        return transactions;
    }

    public void setTransactions(String[] transactions) {
        this.transactions = transactions;
    }

    public boolean isTrunk() {
        return isTrunk;
    }

    public void setTrunk(boolean trunk) {
        isTrunk = trunk;
    }

    public Integer getTxsFeatures() {
        return txsFeatures;
    }

    public void setTxsFeatures(Integer txsFeatures) {
        this.txsFeatures = txsFeatures;
    }
}
