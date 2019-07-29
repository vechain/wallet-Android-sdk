package com.vechain.wallet.bip44;

import com.vechain.wallet.bip32.ExtendedKey;
import com.vechain.wallet.bip32.Index;
import com.vechain.wallet.bip32.ValidationException;
import com.vechain.wallet.key.KeyPair;
import com.vechain.wallet.key.ThorKeyPair;
import com.vechain.wallet.utils.Hash;
import com.vechain.wallet.utils.StringUtils;

import java.util.Map;
import java.util.WeakHashMap;


public class CoinPairDerive {
    private static Map<String, ExtendedKey> extendedKeyMap = new WeakHashMap<>();

    private ExtendedKey extendedKey;

    public CoinPairDerive(ExtendedKey extendedKey) {
        this.extendedKey = extendedKey;
    }

    public ExtendedKey deriveByExtendedKey(AddressIndex addressIndex) throws ValidationException {
        String keyStr = StringUtils.toHex(extendedKey.getChainCode()) + StringUtils.toHex(extendedKey.getMaster().getRawPublicKey())
                + addressIndex.toString();
        byte[] byteKey = Hash.sha256(keyStr.getBytes());
        String extendKeyAddress = StringUtils.toHex(byteKey);
        ExtendedKey extendedKey = extendedKeyMap.get(extendKeyAddress);
        if (extendedKey != null) {
            return extendedKey;
        }
        int address = addressIndex.getValue();
        int change = addressIndex.getParent().getValue();
        int account = addressIndex.getParent().getParent().getValue();
        CoinType coinType = addressIndex.getParent().getParent().getParent().getValue();
        int purpose = addressIndex.getParent().getParent().getParent().getParent().getValue();

        ExtendedKey child = this.extendedKey
                .getChild(Index.hard(purpose))
                .getChild(Index.hard(coinType.getType()))
                .getChild(Index.hard(account))
                .getChild(change)
                .getChild(address);
        extendedKeyMap.put(extendKeyAddress, child);
        return child;
    }

    public KeyPair derive(AddressIndex addressIndex) throws ValidationException {
        CoinType coinType = addressIndex.getParent().getParent().getParent().getValue();
        ExtendedKey child = deriveByExtendedKey(addressIndex);
        KeyPair keyPair = convertKeyPair(child, coinType);
        return keyPair;
    }

    public KeyPair convertKeyPair(ExtendedKey child, CoinType coinType) throws ValidationException {
        switch (coinType) {
            case VET:
                return ThorKeyPair.parse(child.getMaster());
            default:
                return ThorKeyPair.parse(child.getMaster());
        }
    }
}