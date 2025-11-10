package bookmarkd.api.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

@Entity
public class Review extends PanacheEntity {
    public enum Rating {
        ONE_STAR,
        TWO_STARS,
        THREE_STARS,
        FOUR_STARS,
        FIVE_STARS
    }

    @Column(nullable = false, length = 4000)
    public String content;

    @Column(nullable = false)
    public LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    public Rating rating;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    public User author;

    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id")
    public Book book;

    @ManyToMany
    @JoinTable(name = "review_likes",
            joinColumns = @JoinColumn(name = "review_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    public List<User> likedBy = new ArrayList<>();
}
