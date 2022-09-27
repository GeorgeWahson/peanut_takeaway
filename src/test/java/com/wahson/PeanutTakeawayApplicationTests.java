package com.wahson;

import com.wahson.util.SMSUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PeanutTakeawayApplicationTests {

    @Test
    void testFileLoad() {
        SMSUtils.sendMessage(null,null, null, "1234");
    }

}
