package app.xtorayhd.core

import org.junit.Assert.assertEquals
import org.junit.Test

class SubscriptionParserTest {
    @Test fun parsesVless() {
        val p = SubscriptionParser.parse("vless://00000000-0000-0000-0000-000000000001@example.com:443?security=tls&sni=example.com#Test")
        assertEquals(1,p.size)
        assertEquals("VLESS",p.first().protocol)
        assertEquals(443,p.first().port)
    }
}
