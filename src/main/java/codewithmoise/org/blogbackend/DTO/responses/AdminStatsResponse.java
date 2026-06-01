package codewithmoise.org.blogbackend.DTO.responses;

import lombok.Data;

@Data
public class AdminStatsResponse {
    private long totalAuthors;
    private long totalBlogs;
    private long suspendedUsers;

    public  AdminStatsResponse(long totalAuthors, long totalBlogs,long suspendedUsers){
        this.suspendedUsers = suspendedUsers;
        this.totalAuthors = totalAuthors;
        this.totalBlogs = totalBlogs;
    }
}
