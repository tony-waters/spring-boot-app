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
        demoDataSeederService.seedIfEmpty();
        log.info("Seed profile completed, shutting down application");
        System.exit(0);
    }
}