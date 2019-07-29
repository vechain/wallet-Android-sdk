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

import com.vechain.wallet.dapp.utils.ClauseUtil;
import com.vechain.wallet.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TransferParameter {

    private List<Clause> clauses;

    private long gas;

    //Genesis block ID last byte hexadecimal
    private String chainTag;

    //Eight bytes in hexadecimal after reference blockId
    private String blockRef;

    //Changing Nonce can make the transaction have different ID, which can be used to accelerate the trader.
    //8-byte hexadecimal String
    private String nonce;

    //This value adjusts the priority of the transaction and the current value range is between 0 and 255 (decimal)
    private int gasPriceCoef = 0;


    //Deal expiration/expiration time in blocks (10-digit String)
    private String expiration = "720";//The default is 720.

    //The ID of the dependent transaction. If the field is not empty, the transaction will be executed only if the specified transaction already exists.
    //(Hexadecimal String)
    private String dependsOn;//The default is null


    //Reserve fields. Reserved fields for backward compatibility
    private List<byte[]> reserveds;//The default is null

    private TransferParameter() {
        clauses = new ArrayList<>();
    }


    public static class Builder {

        private TransferParameter transferParameter;

        public Builder() {
            transferParameter = new TransferParameter();
        }

        public Builder setClause(Clause clause) {
            if (clause != null)
                transferParameter.clauses.add(clause);
            return this;
        }

        public Builder setClauseList(List<Clause> clauses) {
            if (clauses != null && !clauses.isEmpty())
                transferParameter.clauses.addAll(clauses);
            return this;
        }

        public Builder setGas(long gas) {
            transferParameter.gas = gas;
            return this;
        }

        public Builder setChainTag(String chainTag) {
            transferParameter.chainTag = chainTag;
            return this;
        }

        public Builder setBlockRef(String blockRef) {
            transferParameter.blockRef = blockRef;
            return this;
        }

        public Builder setNonce(String nonce) {
            transferParameter.nonce = nonce;
            return this;
        }

        public Builder setGasPriceCoef(int gasPriceCoef) {
            transferParameter.gasPriceCoef = gasPriceCoef;
            return this;
        }

        public Builder setExpiration(String expiration) {
            transferParameter.expiration = expiration;
            return this;
        }

        public Builder setDependsOn(String dependsOn) {
            transferParameter.dependsOn = dependsOn;
            return this;
        }

        public Builder setReserveds(List<byte[]> reserveds) {
            //transfer.reserveds = reserveds;
            return this;
        }

        public TransferParameter build() throws ParameterException{

            if (!checkClauseList()) return null;
            if (!checkGas()) return null;
            if (!checkChainTag()) return null;
            if (!checkBlockReference()) return null;
            if (!checkNonce()) return null;
            if (!checkGasPriceCoef()) return null;
            if (!checkExpiration()) return null;
            if (!checkDependsOn()) return null;
            if (!checkReserveds()) return null;

            return transferParameter;
        }

        private boolean checkClauseList() throws ParameterException{

            List<Clause> clauseList = transferParameter.clauses;
            if (clauseList == null || clauseList.isEmpty()) return false;
            for (Clause item : clauseList) {
                if (!ClauseUtil.checkClause(item)) {
                    return false;
                }
            }

            return true;
        }

        private boolean checkGas() throws ParameterException{
//            long value = transferParameter.gas;
//            if (value <= 0)
//                throw new ParameterException("Gas must not be less than or equal to zero");
            return true;
        }

        private boolean checkChainTag() throws ParameterException{

            //一个字节
            String chainTag = transferParameter.chainTag;
            if (chainTag == null || chainTag.length() != 4)
                throw new ParameterException("chainTag must be a string of length 4");
            if (!StringUtils.isHexString(chainTag))
                throw new ParameterException("chainTag must be a hex string");

            return true;
        }

        private boolean checkBlockReference() throws ParameterException{

            //8个字节
            String blockReference = transferParameter.blockRef;
            if (blockReference == null || blockReference.length() != 18)
                throw new ParameterException("BlockReference must be a string of length 18");
            if (!StringUtils.isHexString(blockReference))
                throw new ParameterException("BlockReference must be a hex string");

            return true;
        }

        private boolean checkNonce() throws ParameterException{
            //8个字节
            String nonce = transferParameter.nonce;
            if (nonce == null || nonce.length() != 18)
                throw new ParameterException("Nonce must be a string of length 18");
            if (!StringUtils.isHexString(nonce))
                throw new ParameterException("Nonce must be a hex string");

            return true;
        }

        private boolean checkGasPriceCoef() throws ParameterException{
            int value = transferParameter.gasPriceCoef;
            if (value < 0 || value > 255)
                throw new ParameterException("GasPriceCoef is an integer of 0 to 255");

            return true;
        }

        private boolean checkExpiration() throws ParameterException{
            String expiration = transferParameter.expiration;
            if (expiration == null)
                throw new ParameterException("Expiration is null");

            int value = StringUtils.string2Int(expiration);
            if (value < 0)
                throw new ParameterException("Expiration cannot be less than or equal to 0");

            return true;
        }

        private boolean checkDependsOn() throws ParameterException{
            String dependsOn = transferParameter.dependsOn;
            if (dependsOn == null) return true;

            if (!StringUtils.isHexString(dependsOn))
                throw new ParameterException("DependsOn must be a hex string");

            return true;
        }

        private boolean checkReserveds() throws ParameterException{
//            List<byte[]> reserveds = transfer.reserveds;
//            if(reserveds==null)return true;

            return true;
        }
    }


    public List<Clause> getClauses() {
        return clauses;
    }

    public void setGas(long gas) {
        this.gas = gas;
    }

    public long getGas() {
        return gas;
    }

    public String getChainTag() {
        return chainTag;
    }

    public String getBlockRef() {
        return blockRef;
    }

    public String getNonce() {
        return nonce;
    }

    public int getGasPriceCoef() {
        return gasPriceCoef;
    }

    public String getExpiration() {
        return expiration;
    }

    public String getDependsOn() {
        return dependsOn;
    }

    public List<byte[]> getReserveds() {
        return reserveds;
    }
}
