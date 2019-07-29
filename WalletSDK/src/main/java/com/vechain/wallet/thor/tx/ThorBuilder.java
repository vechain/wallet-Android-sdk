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

package com.vechain.wallet.thor.tx;

import java.util.ArrayList;
import java.util.List;


public class ThorBuilder {

    private String chainTag;
    private String blockRef;
    private String expiration;
    private List<Clause> clauses = new ArrayList<>();

    private String gasPriceCoef;
    private String gas;
    private String dependsOn;
    private String nonce;

    private List<byte[]> reserveds;

    //ChainTag is taken from the last byte of the block ID of the Genesis Block (16-digit String)
    public ThorBuilder setChainTag16(String chainTag) {
        this.chainTag = chainTag;

        return this;
    }

    //References to previous blocks, the first eight bytes of block ID (hexadecimal String)
    public ThorBuilder setBlockRef16(String blockRef) {
        this.blockRef = blockRef;
        return this;
    }

    //Deal expiration/expiration time in blocks (10-digit String)
    public ThorBuilder setExpiration10(String expiration) {
        this.expiration = expiration;
        return this;
    }

    //To (16-digit String), value (10-digit String), data (16-digit String, can be empty)
    public ThorBuilder setClause161016(String to, String value, String data) {
        if (to == null || value == null) return this;
        Clause clause = new Clause();
        clause.setTo(to);
        clause.setValue(value);
        clause.setData(data);

        if (clause != null)
            clauses.add(clause);

        return this;
    }


    public ThorBuilder setClause(Clause clause){
        if (clause != null)
            clauses.add(clause);
        return this;
    }

    public ThorBuilder setClauseList(List<Clause> clauseList){
        if (clauseList != null && !clauseList.isEmpty())
            clauses.addAll(clauseList);
        return this;
    }

    //This value adjusts the priority of the transaction and the current value range is between 0 and 255 (decimal)
    public ThorBuilder setGasPriceCoef10(String asPriceCoef) {
        this.gasPriceCoef = asPriceCoef;
        return this;
    }

    //允许消耗的gas总量(10进制String)
    public ThorBuilder setGas10(String gas) {
        this.gas = gas;
        return this;
    }

    //The ID of the dependent transaction. If the field is not empty, the transaction will be executed only if the specified transaction already exists.
    //(Hexadecimal String)
    public ThorBuilder setDependsOn16(String dependsOn) {
        this.dependsOn = dependsOn;
        return this;
    }

    //Changing Nonce can make the transaction have different ID, which can be used to accelerate the trader.
    //8-byte hexadecimal String
    public ThorBuilder setNonce16(String nonce) {
        this.nonce = nonce;
        return this;
    }

    //Currently empty, reserve fields. Reserved fields for backward compatibility
    public ThorBuilder setReserveds(List<byte[]> reserveds) {
        this.reserveds = reserveds;
        return this;
    }


    public String getChainTag() {
        return chainTag;
    }

    public String getBlockRef() {
        return blockRef;
    }

    public String getExpiration() {
        return expiration;
    }

    public List<Clause> getClauses() {
        return clauses;
    }

    public String getGasPriceCoef() {
        return gasPriceCoef;
    }

    public String getGas() {
        return gas;
    }

    public String getDependsOn() {
        return dependsOn;
    }

    public String getNonce() {
        return nonce;
    }

    public List<byte[]> getReserveds() {
        return reserveds;
    }
}
