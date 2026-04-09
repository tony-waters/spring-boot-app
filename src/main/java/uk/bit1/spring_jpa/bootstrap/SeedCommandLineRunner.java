package uk.bit1.spring_jpa.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("seed")
@RequiredArgsConstructor
public class SeedCommandLineRunner implements CommandLineRunner {

    private final DemoDataSeederService demoDataSeederService;

    @Override
    public void run(String... args) {
        log.info("SEEDING! Starting seed process!");
        demoDataSeederService.seedIfEmpty();
        log.info("SEEDING! Seed profile completed, shutting down application");
        System.exit(0);
    }
}