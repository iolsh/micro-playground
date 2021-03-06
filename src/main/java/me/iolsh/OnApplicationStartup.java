package me.iolsh;

import com.github.javafaker.Faker;
import me.iolsh.infrastructure.messaging.LogMessageConsumer;
import me.iolsh.infrastructure.messaging.TickMessageProducer;
import me.iolsh.books.entity.Book;
import me.iolsh.books.entity.BookRepository;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.inject.Inject;
import java.util.stream.IntStream;

@Startup
@Singleton
public class OnApplicationStartup {

    @Inject
    private Faker faker;

    @Inject
    private BookRepository bookRepository;

    @Inject
    private LogMessageConsumer logMessageConsumer;

    @Inject
    private TickMessageProducer tickMessageProducer;

    @Inject
    @ConfigProperty(name = "rabbitmq.queue")
    private String queueName;

    @PostConstruct
    public void init() {
        populateBooksInDatabase();
        logMessageConsumer.start(queueName);
    }

    @Lock(LockType.READ)
    @Schedule(minute = "*/10", hour = "*", persistent = false)
    public void tickMessage() {
        tickMessageProducer.tick();
    }

    private void populateBooksInDatabase() {
        IntStream.range(1, 10).forEach(i -> createBook());
    }

    private void createBook() {
        bookRepository.create(randomBook());
    }

    private Book randomBook() {
        return Book.builder()
                .author(faker.book().author())
                .title(faker.book().title())
                .genre(faker.book().genre())
                .year(faker.number().numberBetween(1900, 2019))
                .build();
    }
}
