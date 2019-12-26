package corf.base.preferences;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import inet.ipaddr.HostName;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import corf.base.net.ConnectionParams;
import corf.base.preferences.internal.ManualProxy;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Basic interface that represents generic proxy.
 * Implementations can provide e.g. manual proxy (static data), PAC script,
 * OS-wide proxy etc.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(@JsonSubTypes.Type(value = ManualProxy.class, name = ManualProxy.ID))
public interface Proxy {

    String OFF = "off";

    String getId();

    @Nullable ConnectionParams getConnectionParams(String ipOrHostname);

    /**
     * Checks whether provided address matches provided expression.
     * Input strings can be either IP addresses or hostnames of equal type, which
     * that means if expression string represents hostname pattern, address string
     * MUST also be a hostname. In other words, DNS resolution should happen prior
     * to calling this method.
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
     * See unit tests for more examples.
     */
    static boolean match(String expression, String address) {
        if (StringUtils.isBlank(expression) || StringUtils.isBlank(address)) {
            return false;
        }

        String expressionClean = expression.trim().toLowerCase();
        String addressClean = address.trim().toLowerCase();

        if (Objects.equals(expressionClean, addressClean)) { return true; }

        var expressionHostname = new HostName(expressionClean);
        var addressHostname = new HostName(addressClean);

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
