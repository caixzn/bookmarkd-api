package bookmarkd.api.service;

import java.util.List;
import java.util.Locale;

import bookmarkd.api.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class UserService {

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

	public List<User> listUsers(String username) {
		if (username == null || username.isBlank()) {
			return User.listAll();
		}

		var normalized = "%" + username.trim().toLowerCase(Locale.ROOT) + "%";
		return User.find("LOWER(username) like ?1", normalized).list();
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
}
