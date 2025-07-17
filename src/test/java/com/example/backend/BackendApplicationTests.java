package com.example.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.backend.config.TestMailConfig; // Импортируем вашу новую тестовую конфигурацию

@SpringBootTest(classes = TestMailConfig.class) // <--- Указываем, какие классы конфигурации использовать
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // Этот тест просто проверяет, успешно ли загружается контекст Spring.
    }
}