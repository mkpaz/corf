package telekit.base.preferences;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import inet.ipaddr.HostName;
import org.jetbrains.annotations.Nullable;
import telekit.base.net.connection.ConnectionParams;
import telekit.base.preferences.internal.ManualProxy;

import java.util.Objects;
import java.util.regex.Pattern;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;
import static org.apache.commons.lang3.StringUtils.isBlank;

@JsonTypeInfo(use = NAME, include = PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ManualProxy.class, name = "manual"),
})
public interface Proxy {

    String DISABLED = "disabled";

    String getId();

    @Nullable ConnectionParams getConnectionParams(String ipOrHostname);

    /**
     * Checks whether provided address matches provided expression.
     * Input strings can be IP addresses or hostnames of equal type, which means
     * if expression string represents hostname pattern, address string must also
     * be a hostname.
     * <pre>
     * {@code
     *     192.168.1.1 ∋ 192.168.1.1 = true
     *     192.168.* ∋ 192.168.1.1 = true
     *     172.21.* ∋ 192.168.1.1 = false
     *     172.21.* ∋ null = false
     *     172.21.1.0/24 ∋ 172.21.1.0 = true
     *     *.example.com ∋ foo.example.com = true
     *     *.example.com ∋ google.com = false
     *     *.example.com ∋ com = false
     * }
     * </pre>
     * <p>
     * See tests for more example
     */
    static boolean match(String expression, String address) {
        if (isBlank(expression) || isBlank(address)) { return false; }

        String expressionClean = expression.trim().toLowerCase();
        String addressClean = address.trim().toLowerCase();

        if (Objects.equals(expressionClean, addressClean)) { return true; }

        HostName expressionHostname = new HostName(expressionClean);
        HostName addressHostname = new HostName(addressClean);

        // expression and address are of different types and one of them needs to be resolved
        // but since this supposed to be a very simple check, so no DNS, have false
        if (expressionHostname.isAddress() != addressHostname.isAddress()) { return false; }

        if (expressionHostname.isAddress()) {
            return expressionHostname.asAddress().contains(addressHostname.asAddress());
        } else {
            String globExpression = "^" + Pattern.quote(expressionClean).replace("*", "\\E.*\\Q") + "$";
            return addressClean.matches(globExpression);
        }
    }
}
