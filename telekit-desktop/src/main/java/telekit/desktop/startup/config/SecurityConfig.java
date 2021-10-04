package telekit.desktop.startup.config;

import telekit.base.domain.security.SecuredData;
import telekit.base.preferences.internal.ApplicationPreferences;
import telekit.base.preferences.internal.PKCS12Vault;
import telekit.base.preferences.internal.SecurityPreferences;
import telekit.base.preferences.internal.Vault;
import telekit.base.util.PasswordGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;

import static telekit.base.Env.DEFAULT_ENCRYPTION_ALG;
import static telekit.base.preferences.internal.Vault.MASTER_KEY_ALIAS;
import static telekit.base.service.crypto.Encryptor.generateKey;
import static telekit.base.util.PasswordGenerator.ASCII_LOWER_UPPER_DIGITS;

public final class SecurityConfig implements Config {

    private final ApplicationPreferences preferences;
    private Vault vault;

    public SecurityConfig(ApplicationPreferences preferences) {
        this.preferences = preferences;
        initialize();
    }

    private void initialize() {
        // set unlimited crypto policy
        java.security.Security.setProperty("crypto.policy", "unlimited");

        loadKeyVault();
    }

    private void loadKeyVault() {
        SecurityPreferences security = preferences.getSecurityPreferences();
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
