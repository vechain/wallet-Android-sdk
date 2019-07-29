
package com.vechain.wallet.thor.rlp;

import java.io.Serializable;


public interface RLPElement extends Serializable {

    byte[] getRLPData();
}
