package org.telekit.ui.tools.import_file_builder;

import org.junit.jupiter.api.Test;
import org.telekit.base.util.DesktopUtils;
import org.telekit.base.util.PlaceholderReplacer;

import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

class PreviewRendererTest {

    @Test
    void render() throws Exception {
        File outputFile = Paths.get(System.getProperty("java.io.tmpdir"))
                .resolve("import-file-builder.preview.html")
                .toFile();

        Template template = new Template();
        template.setId(UUID.randomUUID().toString());
        template.setName("Test Preview");
        template.setDelimiter("|");
        template.setHeader(
                "IMS User Subscription|||||IMS Private User Identity|||||||||IMS Public User Identity|||||||||||Access Gateway Control Function||||||||||||||||||||IMS Public Identities from implicit registration set||||Telephony Application Server|\n" +
                        "Name *|Capabilities Set|Preferred S-CSCF Set|Current S-CSCF Name|Current Diameter Name|Identity|Secret Key K *|Authorization Schemes|Default Auth. Scheme|AMF|OP|SQN|Early IMS IP|DSL Line Identifier|Identity *|Type|Barred public user identity|Service Profile|Implicit Set Identity|Charging Info|Wildcard PSI|Display Name|PSI Activation|Can Register|Allowed Roaming|Type *|Node *|Public Id Alias *|URI Type *|Interface *|Access *|Access Variant *|RTP Profile *|Password *|Private Id Alias|Embed telURI into SIP URI|DTMF Authorization|Out of Service Indication|Active Subscriber|Initiate registration at system startup|Display/Ring Type|In-band Indication Type|Tariff Origin Code|Standalone Mode Calls|Hotline Enable|Public Id Alias|URI Type|Hotline Enable|Set as MSN Number|TAS Node|TAS Public Id Alias|Supplementary Service Set|Concurrent Sessions|License Type|Subscriber Category|SIP Profile|Business Group|Custom Service Set|Time Zone|Geographical Area|MAD *|Edge SIP Profile *|Tgrp|Concurrent Calls|Registration Mode|Registration Expires in|Subscription Expires in |Type of User|Concurrent Session Type|"
        );
        template.setPattern("%(_csv0)|0|2|||%(_csv0)@ims.rt.ru|${password}|255|4|||000000000000|||sip:%(_csv0)@ims.rt.ru|Public User Identity|no|1|sip:%(_csv0)@ims.rt.ru||||no|yes|1|Analog Subscriber|640120|%(_csv0)|telUri|${interface}|%(_csv1)|6||${password}||yes|yes|no|yes|yes|Analog Public|ISDN Public|1|no|none|||||490030|sip:%(_csv0)@ims.rt.ru|902|1|advancedLicense|56|92|||0|64|||||||||1|");
        template.setParams(Set.of(
                new Param("par1", Param.Type.CONSTANT, 0),
                new Param("par2", Param.Type.PASSWORD, 10),
                new Param("par3", Param.Type.PASSWORD_BASE64, 10),
                new Param("par4", Param.Type.UUID, 10)
        ));

        String html = PreviewRenderer.render(template);
        try (PrintWriter writer = new PrintWriter(outputFile)) {
            writer.println(html);
        }
        DesktopUtils.browse(outputFile.toURI());
    }
}