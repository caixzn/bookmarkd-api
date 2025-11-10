package bookmarkd.api.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import bookmarkd.api.entity.Book;
import bookmarkd.api.entity.Log;
import bookmarkd.api.entity.Log.Action;
import bookmarkd.api.entity.User;
import bookmarkd.api.resource.dto.LogDto;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.transaction.Transactional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class LogService {

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 50;

	public LogDto createLog(Long bookId, Long userId, String actionValue, LocalDateTime timestamp) {
		if (bookId == null) {
			throw new BadRequestException("bookId is required");
		}
		if (userId == null) {
			throw new BadRequestException("userId is required");
		}
		if (actionValue == null || actionValue.isBlank()) {
			throw new BadRequestException("action is required");
		}

		Book book = Book.findById(bookId);
		if (book == null) {
			throw new NotFoundException("Book not found for id: " + bookId);
		}

		User user = User.findById(userId);
		if (user == null) {
			throw new NotFoundException("User not found for id: " + userId);
		}

		var action = parseAction(actionValue);

		var log = new Log();
		log.book = book;
		log.user = user;
		log.action = action;
		log.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
		log.persist();

		return LogDto.from(log);
	}

	public List<LogDto> listLogs(Long bookId, Long userId, String actionValue, Integer page, Integer size) {
		// Assemble a flexible JPQL query based on the provided filters.
		var query = new StringBuilder();
		Map<String, Object> parameters = new HashMap<>();

		if (bookId != null) {
			appendPredicate(query, "book.id = :bookId");
			parameters.put("bookId", bookId);
		}
		if (userId != null) {
			appendPredicate(query, "user.id = :userId");
			parameters.put("userId", userId);
		}
		if (actionValue != null && !actionValue.isBlank()) {
			var action = parseAction(actionValue);
			appendPredicate(query, "action = :action");
			parameters.put("action", action);
		}

		PanacheQuery<Log> panacheQuery;
		if (query.length() == 0) {
			panacheQuery = Log.findAll();
		} else {
			panacheQuery = Log.find(query.toString(), parameters);
		}

		return panacheQuery.page(resolvePage(page, size))
				.list()
				.stream()
				.map(LogDto::from)
				.toList();
	}

	@Transactional
	public LogDto updateLog(Long logId, Long bookId, Long userId, String actionValue, LocalDateTime timestamp) {
		if (logId == null) {
			throw new BadRequestException("logId is required");
		}

		Log log = Log.findById(logId);
		if (log == null) {
			throw new NotFoundException("Log not found for id: " + logId);
		}

		if (bookId != null) {
			Book book = Book.findById(bookId);
			if (book == null) {
				throw new NotFoundException("Book not found for id: " + bookId);
			}
			log.book = book;
		}

		if (userId != null) {
			User user = User.findById(userId);
			if (user == null) {
				throw new NotFoundException("User not found for id: " + userId);
			}
			log.user = user;
		}

		if (actionValue != null && !actionValue.isBlank()) {
			log.action = parseAction(actionValue);
		}

		if (timestamp != null) {
			log.timestamp = timestamp;
		}

		return LogDto.from(log);
	}

	@Transactional
	public void deleteLog(Long logId) {
		if (logId == null) {
			throw new BadRequestException("logId is required");
		}

		Log log = Log.findById(logId);
		if (log == null) {
			throw new NotFoundException("Log not found for id: " + logId);
		}

		log.delete();
	}

	private void appendPredicate(StringBuilder query, String predicate) {
		if (query.length() > 0) {
			query.append(" and ");
		}
		query.append(predicate);
	}

	private Action parseAction(String actionValue) {
		try {
			return Action.valueOf(actionValue.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			throw new BadRequestException("Unknown action: " + actionValue);
		}
	}

	private Page resolvePage(Integer page, Integer size) {
		int pageNumber = (page == null || page < 1) ? 1 : page;
		int pageSize = (size == null || size < 1) ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
		return Page.of(pageNumber - 1, pageSize);
	}
}
