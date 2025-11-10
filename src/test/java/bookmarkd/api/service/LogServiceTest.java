package bookmarkd.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.List;

import bookmarkd.api.entity.Log;
import bookmarkd.api.entity.Log.Action;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class LogServiceTest {

    @Inject
    LogService logService;

    @Test
    @Transactional
    void createLog_persistsAndDefaultsTimestamp() {
        TestDataUtil.clearDatabase();
        var user = TestDataUtil.persistUser("reader");
        var book = TestDataUtil.persistBook("Test Book", "Author", "2020");

        Log log = logService.createLog(book.id, user.id, "started_reading", null);

        assertNotNull(log.id);
        assertEquals(Action.STARTED_READING, log.action);
        assertNotNull(log.timestamp);
    }

    @Test
    @Transactional
    void listLogs_supportsFilteringByAction() {
        TestDataUtil.clearDatabase();
        var user = TestDataUtil.persistUser("reader");
        var book = TestDataUtil.persistBook("Filter Book", "Author", "2021");

        logService.createLog(book.id, user.id, "started_reading", null);
        logService.createLog(book.id, user.id, "finished_reading", null);

        List<Log> finished = logService.listLogs(book.id, user.id, "finished_reading");

        assertEquals(1, finished.size());
        assertEquals(Action.FINISHED_READING, finished.get(0).action);
    }

    @Test
    @Transactional
    void createLog_rejectsUnknownAction() {
        TestDataUtil.clearDatabase();
        var user = TestDataUtil.persistUser("reader");
        var book = TestDataUtil.persistBook("Invalid Action Book", "Author", "2022");

        assertThrows(BadRequestException.class, () -> logService.createLog(book.id, user.id, "invalid", null));
    }

    @Test
    @Transactional
    void updateLog_modifiesSelectedFields() {
        TestDataUtil.clearDatabase();
        var user = TestDataUtil.persistUser("reader");
        var book = TestDataUtil.persistBook("Original", "Author", "2020");
        var newBook = TestDataUtil.persistBook("Updated", "Author", "2021");

        Log log = logService.createLog(book.id, user.id, "started_reading", null);
        var newTimestamp = LocalDateTime.now().minusDays(1);

        Log updated = logService.updateLog(log.id, newBook.id, null, "finished_reading", newTimestamp);

        assertEquals(Action.FINISHED_READING, updated.action);
        assertEquals(newTimestamp, updated.timestamp);
        assertEquals(newBook.id, updated.book.id);
    }

    @Test
    @Transactional
    void deleteLog_removesEntry() {
        TestDataUtil.clearDatabase();
        var user = TestDataUtil.persistUser("reader");
        var book = TestDataUtil.persistBook("Delete", "Author", "2023");

        Log log = logService.createLog(book.id, user.id, "started_reading", null);
        logService.deleteLog(log.id);

        assertNull(Log.findById(log.id));
    }
}
