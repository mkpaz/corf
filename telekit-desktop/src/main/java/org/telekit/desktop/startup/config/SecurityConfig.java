package org.telekit.desktop.startup.config;

import org.telekit.base.domain.SecuredData;
import org.telekit.base.preferences.ApplicationPreferences;
import org.telekit.base.preferences.PKCS12Vault;
import org.telekit.base.preferences.Security;
import org.telekit.base.preferences.Vault;
import org.telekit.base.util.PasswordGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;

import static org.telekit.base.Env.DEFAULT_ENCRYPTION_ALG;
import static org.telekit.base.preferences.Vault.MASTER_KEY_ALIAS;
import static org.telekit.base.service.Encryptor.generateKey;
import static org.telekit.base.util.PasswordGenerator.ASCII_LOWER_UPPER_DIGITS;

public final class SecurityConfig implements Config {

    private final ApplicationPreferences preferences;
    private Vault vault;

    public SecurityConfig(ApplicationPreferences preferences) {
        this.preferences = preferences;
        initialize();
    }

    private void initialize() {
        loadKeyVault();
    }

    private void loadKeyVault() {
        Security security = preferences.getSecurity();
        Path vaultFilePath = security.getVaultFilePath();

        vault = new PKCS12Vault(vaultFilePath);
        if (!Files.exists(vaultFilePath)) {
            Key key = generateKey(DEFAULT_ENCRYPTION_ALG);

            // if vault file is deleted, create a new one and update password in security preferences
            security.setVaultPassword(SecuredData.fromString(
                    PasswordGenerator.random(16, ASCII_LOWER_UPPER_DIGITS)
            ));
            byte[] vaultPassword = security.getDerivedVaultPassword();

            vault.unlock(vaultPassword);
            vault.putKey(MASTER_KEY_ALIAS, vaultPassword, key);
            vault.save(vaultPassword);

            // vault password was updated
            preferences.setDirty();
        }
    }

    public Vault getVault() {
        return vault;
    }
}
