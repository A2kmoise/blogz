package codewithmoise.org.blogbackend.scheduler;

import codewithmoise.org.blogbackend.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TagCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(TagCleanupScheduler.class);
    private final TagRepository tagRepository;

    public TagCleanupScheduler(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    //fixedRate = 6000
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOrphanedTags() {
        log.info("Tag cleanup started...");
        tagRepository.deleteOrphanedTags();
        log.info("Tag cleanup complete.");
    }
}