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

import com.vechain.wallet.key.KeyPair;
import com.vechain.wallet.bip32.ValidationException;
import com.vechain.wallet.key.ECDSASignature;
import com.vechain.wallet.thor.rlp.RLP;
import com.vechain.wallet.utils.HexUtils;

import org.spongycastle.jcajce.provider.digest.Blake2b;
import org.spongycastle.util.BigIntegers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.cedarsoftware.util.ArrayUtilities.isEmpty;

public class ThorTransaction {

    //    private static final Logger logger = LoggerFactory.getLogger(EthTransaction.class);
    private static final BigInteger DEFAULT_GAS_PRICE = new BigInteger("10000000000000");
    private static final BigInteger DEFAULT_BALANCE_GAS = new BigInteger("21000");

    public static final int HASH_LENGTH = 32;
    public static final int ADDRESS_LENGTH = 20;

    /**
     * Since EIP-155, we could encode chainId in V
     */
    private static final int CHAIN_ID_INC = 35;
    private static final int LOWER_REAL_V = 27;

    /* the elliptic curve signature
     * (including public key recovery bits) */
    private ECDSASignature signature;

    /* Tx in encoded form */
    protected byte[] rlpEncoded;
    private byte[] rlpRaw;
    /* Indicates if this transaction has been parsed
     * from the RLP-encoded data */
    protected boolean parsed = false;

    /* SHA3 hash of the RLP encoded transaction */
    private byte[] hash;


    private byte[] chainTag;
    private byte[] blockRef;
    private byte[] expiration;
    private List<byte[][]> clauses = new ArrayList<>();

    private byte[] gasPriceCoef;
    private byte[] gas;
    private byte[] dependsOn;
    private byte[] nonce;
    private byte[][] reserveds;

//    rlp.Encode(hw, []interface{}{
//        t.body.ChainTag,
//                t.body.BlockRef,
//                t.body.Expiration,
//                t.body.Clauses,
//                t.body.GasPriceCoef,
//                t.body.Gas,
//                t.body.DependsOn,
//                t.body.Nonce,
//                t.body.Reserved,
//    })


    public ThorTransaction(ThorBuilder builder) {


        chainTag = getHexBytes(builder.getChainTag());

        blockRef = getHexBytes(builder.getBlockRef());

        expiration = getDecBytes(builder.getExpiration());

        List<Clause> clauseList = builder.getClauses();
        if (clauseList != null && !clauseList.isEmpty()) {
            for (Clause item : clauseList) {
                clauses.add(getClause(item));
            }
        }

        gasPriceCoef = getDecBytes(builder.getGasPriceCoef());

        gas = getDecBytes(builder.getGas());

        dependsOn = getHexBytes(builder.getDependsOn());

        nonce = getHexBytes(builder.getNonce());

        reserveds = null;//预留

        parsed = true;

    }


    public void setSignature(ECDSASignature signature){
        this.rlpEncoded = null;
        this.signature = signature;
    }

    private byte[] getHexBytes(String hex) {
        byte[] data = null;
        if (hex != null) {
            hex = HexUtils.cleanHexPrefix(hex);
            if (hex == null || hex.isEmpty()) return data;
            BigInteger bigInteger = new BigInteger(hex, 16);
            data = BigIntegers.asUnsignedByteArray(bigInteger);
            bigInteger = null;
        }
        return data;
    }

    private byte[] getPadingHexBytes(String hex) {
        byte[] data = null;
        if (hex != null) {
            hex = HexUtils.cleanHexPrefix(hex);
            BigInteger bigInteger = new BigInteger(hex, 16);
            data = HexUtils.toBytesPadded(bigInteger,20);
            bigInteger = null;
        }
        return data;
    }

    private byte[] getDecBytes(String dec) {
        byte[] data = null;

        if (dec != null) {
            BigInteger bigInteger = new BigInteger(dec);
            data = BigIntegers.asUnsignedByteArray(bigInteger);
            bigInteger = null;
        }
        return data;
    }

    private byte[][] getClause(Clause clause) {
        byte[][] data = new byte[3][];

        data[0] = getPadingHexBytes(clause.getTo());
        data[1] = getDecBytes(clause.getValue());
        data[2] = getHexBytes(clause.getData());

        return data;
    }


    public ThorTransaction(byte[] rawData) {
        this.rlpEncoded = rawData;
        parsed = false;
    }


    public boolean isParsed() {
        return parsed;
    }

    public byte[] getHash() {
        if (!isEmpty(hash)) return hash;

        byte[] plainMsg = this.getEncoded();
//        return HashUtil.sha3(plainMsg);
//        return KECCAK256.keccak256(plainMsg);

        Blake2b.Blake2b256 blake2b256 = new Blake2b.Blake2b256();
        return blake2b256.digest(plainMsg);
//        return SHA3Digest
    }

    public byte[] getRawHash() {
        byte[] plainMsg = this.getEncodedRaw();
//        return HashUtil.sha3(plainMsg);
//        return KECCAK256.keccak256(plainMsg);
        Blake2b.Blake2b256 blake2b256 = new Blake2b.Blake2b256();
        return blake2b256.digest(plainMsg);
    }


    public byte[] sign(KeyPair key) throws ValidationException {
        this.signature = key.sign(this.getRawHash());

        //签名信息V值非0或1
        if(signature==null || !signature.signIsInRegion()){
            rlpRaw = null;//需要重新去生成nonce值,重新签名
            return null;
        }

        this.rlpEncoded = null;
        return getSignBytes();
    }

    public byte[] getSignBytes() {
        return getEncoded();
    }


    /**
     * For signatures you have to keep also
     * RLP of the transaction without any signature data
     */
    public byte[] getEncodedRaw() {

        if (rlpRaw != null) return rlpRaw;

        // parse null as 0 for nonce
        byte[] enNonce = null;
        if (this.nonce == null || this.nonce.length == 1 && this.nonce[0] == 0) {
            enNonce = RLP.encodeElement(null);
        } else {
            enNonce = RLP.encodeElement(this.nonce);
        }


        byte[] enChainTag = RLP.encodeElement(this.chainTag);
        byte[] enBlockRef = RLP.encodeElement(this.blockRef);
        byte[] enExpiration = RLP.encodeElement(this.expiration);

        byte[] enClauseAll = null;
        byte[][] enClauses = new byte[clauses.size()][];
        int i = 0;
        for (byte[][] item : clauses) {
            byte[] cell1 = RLP.encodeElement(item[0]);
            byte[] cell2 = RLP.encodeElement(item[1]);
            byte[] cell3 = RLP.encodeElement(item[2]);
            enClauses[i] = RLP.encodeList(cell1, cell2, cell3);
            i++;
        }
        enClauseAll = RLP.encodeList(enClauses);

        byte[] enAsPriceCoef = RLP.encodeElement(this.gasPriceCoef);
        byte[] enGas = RLP.encodeElement(this.gas);
        byte[] enDependsOn = RLP.encodeElement(this.dependsOn);
        byte[] enReserveds = RLP.encodeList(this.reserveds);


        rlpRaw = RLP.encodeList(enChainTag, enBlockRef, enExpiration, enClauseAll,
                enAsPriceCoef, enGas, enDependsOn, enNonce, enReserveds);

        return rlpRaw;
    }

    public byte[] getEncoded() {

        if (rlpEncoded != null) return rlpEncoded;

        // parse null as 0 for nonce
        byte[] enNonce = null;
        if (this.nonce == null || this.nonce.length == 1 && this.nonce[0] == 0) {
            enNonce = RLP.encodeElement(null);
        } else {
            enNonce = RLP.encodeElement(this.nonce);
        }


        byte[] enChainTag = RLP.encodeElement(this.chainTag);
        byte[] enBlockRef = RLP.encodeElement(this.blockRef);
        byte[] enExpiration = RLP.encodeElement(this.expiration);

        byte[] enClauseAll = null;
        byte[][] enClauses = new byte[clauses.size()][];
        int i = 0;
        for (byte[][] item : clauses) {
            byte[] cell1 = RLP.encodeElement(item[0]);
            byte[] cell2 = RLP.encodeElement(item[1]);
            byte[] cell3 = RLP.encodeElement(item[2]);
            enClauses[i] = RLP.encodeList(cell1, cell2, cell3);
            i++;
        }
        enClauseAll = RLP.encodeList(enClauses);

        byte[] enAsPriceCoef = RLP.encodeElement(this.gasPriceCoef);
        byte[] enGas = RLP.encodeElement(this.gas);
        byte[] enDependsOn = RLP.encodeElement(this.dependsOn);
        byte[] enReserveds = RLP.encodeList(this.reserveds);

//        byte[] v, r, s;
//
//        if (signature != null) {
//            int encodeV;
//            if (chainId == null) {
//                encodeV = signature.v;
//            } else {
//                encodeV = signature.v - LOWER_REAL_V;
//                encodeV += chainId * 2 + CHAIN_ID_INC;
//            }
//
//            v = RLP.encodeInt(encodeV);
//            r = RLP.encodeElement(BigIntegers.asUnsignedByteArray(signature.r));
//            s = RLP.encodeElement(BigIntegers.asUnsignedByteArray(signature.s));
//        } else {
//            // Since EIP-155 use chainId for v
//            v = chainId == null ? RLP.encodeElement(EMPTY_BYTE_ARRAY) : RLP.encodeInt(chainId);
//            r = RLP.encodeElement(EMPTY_BYTE_ARRAY);
//            s = RLP.encodeElement(EMPTY_BYTE_ARRAY);
//        }

        byte[] enSign = null;
        if (signature != null)
            enSign = RLP.encodeElement(signature.toByteArray());


        this.rlpEncoded = RLP.encodeList(enChainTag, enBlockRef, enExpiration, enClauseAll,
                enAsPriceCoef, enGas, enDependsOn, enNonce, enReserveds, enSign);

        //this.hash = this.getHash();

        return rlpEncoded;
    }

    @Override
    public int hashCode() {

        byte[] hash = this.getHash();
        int hashCode = 0;

        for (int i = 0; i < hash.length; ++i) {
            hashCode += hash[i] * i;
        }

        return hashCode;
    }


    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof ThorTransaction)) return false;
        ThorTransaction tx = (ThorTransaction) obj;

        return tx.hashCode() == this.hashCode();
    }


    //TxId Generated in Transfer
    /**
     *
     * Transaction ID
     *
     * Generally speaking, Hash in all fields of the transaction subject can be used as the sole symbol of the transaction, but Thor redefines the transaction ID for the sake of semantics, extensibility and functional requirements, that is, the ID is only related to the content of the transaction and the initiator, and has nothing to do with the signature algorithm.
     *
     * (New expiration, no change to ID definition, but expiration must be included in signing Has) NEW!
     * @param walletAddress
     * @return txId
     */
    public String getTransactionId(String walletAddress){

        byte[] rawHash = getRawHash();

        byte[] address = HexUtils.hexStringToByteArray(walletAddress);

        int length = rawHash.length + address.length;
        byte[] data = new byte[length];
        System.arraycopy(rawHash, 0, data, 0, rawHash.length);
        System.arraycopy(address, 0, data, rawHash.length, address.length);

        Blake2b.Blake2b256 blake2b256 = new Blake2b.Blake2b256();
        data = blake2b256.digest(data);

        return HexUtils.toHexString(data);
    }

}
