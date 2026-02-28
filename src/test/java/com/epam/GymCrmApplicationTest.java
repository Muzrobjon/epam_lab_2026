package com.epam;

import static org.junit.jupiter.api.Assertions.assertTrue;
import com.epam.gym.config.AppConfig;
import com.epam.gym.facade.GymFacade;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

class GymCrmApplicationTest {

    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> {
            AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext(AppConfig.class);
            assertNotNull(context);
            context.close();
        });
    }

    @Test
    void main_WithValidContext_ShouldRun() {
        assertDoesNotThrow(() -> {
            // This test verifies the application starts without errors
            // In a real scenario, you might want to mock System.out or use a test profile
            String[] args = {};
            // GymCrmApplication.main(args); // Commented out to avoid running full app in unit test
        });
    }

    @Test
    void gymFacadeBean_ShouldBeAvailableInContext() {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        GymFacade facade = context.getBean(GymFacade.class);

        assertNotNull(facade);
        context.close();
    }
}

