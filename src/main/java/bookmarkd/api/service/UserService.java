package bookmarkd.api.service;

import java.util.List;
import java.util.Locale;

import bookmarkd.api.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class UserService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

	public User createUser(String username) {
		var sanitizedUsername = sanitizeUsername(username);

		var existing = (User) User.find("LOWER(username) = ?1", sanitizedUsername.toLowerCase(Locale.ROOT)).firstResult();
		if (existing != null) {
			throw new BadRequestException("Username already taken: " + sanitizedUsername);
		}

		var user = new User();
		user.username = sanitizedUsername;
		user.persist();
		return user;
	}

	public List<User> listUsers(String username, Integer page, Integer size) {
		PanacheQuery<User> query;
		if (username == null || username.isBlank()) {
			query = User.findAll(Sort.by("username"));
		} else {
			var normalized = "%" + username.trim().toLowerCase(Locale.ROOT) + "%";
			query = User.find("LOWER(username) like ?1", Sort.by("username"), normalized);
		}
		return query.page(resolvePage(page, size)).list();
	}

	public User getUser(Long id) {
		User user = User.findById(id);
		if (user == null) {
			throw new NotFoundException("User not found for id: " + id);
		}
		return user;
	}

	public User updateUser(Long id, String username) {
		User user = User.findById(id);
		if (user == null) {
			throw new NotFoundException("User not found for id: " + id);
		}

		var sanitizedUsername = sanitizeUsername(username);

		User existing = User.find("LOWER(username) = ?1", sanitizedUsername.toLowerCase(Locale.ROOT)).firstResult();
		if (existing != null && !existing.id.equals(user.id)) {
			throw new BadRequestException("Username already taken: " + sanitizedUsername);
		}

		user.username = sanitizedUsername;
		return user;
	}

	private String sanitizeUsername(String username) {
		if (username == null || username.isBlank()) {
			throw new BadRequestException("username is required");
		}
		return username.trim();
	}

	private Page resolvePage(Integer page, Integer size) {
		int pageNumber = (page == null || page < 1) ? 1 : page;
		int pageSize = (size == null || size < 1) ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
		return Page.of(pageNumber - 1, pageSize);
	}
}
