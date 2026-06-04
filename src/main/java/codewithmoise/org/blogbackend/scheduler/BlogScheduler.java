package codewithmoise.org.blogbackend.scheduler;


import codewithmoise.org.blogbackend.repository.BlogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class BlogScheduler {

    private static  final Logger logging = LoggerFactory.getLogger(BlogScheduler.class);
    private final BlogRepository blogRepository;

    public BlogScheduler(BlogRepository blogRepository){
        this.blogRepository = blogRepository;
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void publishScheduledBlogs(){
        int count =  blogRepository.publishDueBlogs(LocalDateTime.now());
        if (count > 0) {
            logging.info("Scheduled blog publisher: published: {} blog(s) at {}", count, LocalDateTime.now());
        }
     }
}
