package bookmarkd.api.entity;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Log extends PanacheEntity {
    public enum Action {
        STARTED_READING,
        FINISHED_READING,
        DID_NOT_FINISH,
        PAUSED_READING
    }

    @Column(nullable = false)
    public LocalDateTime timestamp;

    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id")
    public Book book;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    public User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    public Action action;
}
