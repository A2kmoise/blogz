package codewithmoise.org.blogbackend.DTO.responses;

import lombok.Data;
import java.util.List;

@Data
public class AdminStatsResponse {
    private long totalBlogs;
    private long totalAuthors;
    private long totalAdmins;
    private List<ActivityResponse> recentActivity;

    public AdminStatsResponse(long totalBlogs, long totalAuthors, long totalAdmins, List<ActivityResponse> recentActivity) {
        this.totalBlogs = totalBlogs;
        this.totalAuthors = totalAuthors;
        this.totalAdmins = totalAdmins;
        this.recentActivity = recentActivity;
    }

    @Data
    public static class ActivityResponse {
        private String id;
        private String text;
        private String date;

        public ActivityResponse(String id, String text, String date) {
            this.id = id;
            this.text = text;
            this.date = date;
        }
    }
}
