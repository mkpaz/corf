package org.telekit.base.telecom.ss7;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.telekit.base.BaseSetup;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.telekit.base.telecom.ss7.SignallingPointCode.*;

@ExtendWith(BaseSetup.class)
public class SignallingPointCodeTest {

    @ParameterizedTest
    @MethodSource("ituSPCProvider")
    public void testITU(Map<Format, String> values) throws Exception {
        Type type = Type.ITU;
        assertThat(values.keySet()).containsAll(type.formats());
        for (Entry<Format, String> entry : values.entrySet()) {
            SignallingPointCode spc = parse(entry.getValue(), type, entry.getKey());
            assertAllEqual(spc, type, values);
        }
    }

    @ParameterizedTest
    @MethodSource("ansiSPCProvider")
    public void testANSI(Map<Format, String> values) throws Exception {
        Type type = Type.ANSI;
        assertThat(values.keySet()).containsAll(type.formats());
        for (Entry<Format, String> entry : values.entrySet()) {
            SignallingPointCode spc = parse(entry.getValue(), type, entry.getKey());
            assertAllEqual(spc, type, values);
        }
    }

    private void assertAllEqual(SignallingPointCode spc, Type type, Map<Format, String> values) {
        for (Format fmt : type.formats()) {
            String correctValue = values.get(fmt);
            String parsedValue = spc.toString(fmt);
            assertThat(parsedValue).isEqualTo(correctValue);
        }
    }

    public static Stream<Map<Format, String>> ituSPCProvider() {
        return Stream.of(
                Map.of(Format.DEC, "0", Format.HEX, "0", Format.BIN, "00000000000000",
                       Format.STRUCT_383, "0-0-0", Format.STRUCT_86, "0-0"),
                Map.of(Format.DEC, "160", Format.HEX, "A0", Format.BIN, "00000010100000",
                       Format.STRUCT_383, "0-20-0", Format.STRUCT_86, "2-32"),
                Map.of(Format.DEC, "16383", Format.HEX, "3FFF", Format.BIN, "11111111111111",
                       Format.STRUCT_383, "7-255-7", Format.STRUCT_86, "255-63")
        );
    }

    public static Stream<Map<Format, String>> ansiSPCProvider() {
        return Stream.of(
                Map.of(Format.DEC, "0", Format.HEX, "0", Format.BIN, "000000000000000000000000",
                       Format.STRUCT_888, "0-0-0"),
                Map.of(Format.DEC, "160", Format.HEX, "A0", Format.BIN, "000000000000000010100000",
                       Format.STRUCT_888, "0-0-160"),
                Map.of(Format.DEC, "16777215", Format.HEX, "FFFFFF", Format.BIN, "111111111111111111111111",
                       Format.STRUCT_888, "255-255-255")
        );
    }
}