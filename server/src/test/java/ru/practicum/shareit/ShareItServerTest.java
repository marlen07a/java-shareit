package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.ShareItServer;

@SpringBootTest
class ShareItServerTest {

    @Test
    void contextLoads() {
        ShareItServer.main(new String[]{});
    }
}