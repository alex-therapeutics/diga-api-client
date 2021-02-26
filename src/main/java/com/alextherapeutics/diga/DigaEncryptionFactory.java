package com.alextherapeutics.diga;

import com.alextherapeutics.diga.model.DigaDecryption;
import com.alextherapeutics.diga.model.DigaEncryption;

/**
 * A factory for creating new encryption and decryption attempts.
 */
public interface DigaEncryptionFactory {
    /**
     * Start a new encryption attempt with the factory defaults.
     * @return A {@link DigaEncryption.DigaEncryptionBuilder} with some defaults configured.
     */
    DigaEncryption.DigaEncryptionBuilder newEncryption();

    /**
     * Start a new decryption attempt with the factory defaults.
     * @return A {@link DigaDecryption.DigaDecryptionBuilder} with some defaults configured.
     */
    DigaDecryption.DigaDecryptionBuilder newDecryption();
}
