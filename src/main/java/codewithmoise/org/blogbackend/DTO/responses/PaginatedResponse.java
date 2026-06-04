package codewithmoise.org.blogbackend.DTO.responses;

import lombok.Data;
import java.util.List;

@Data
public class PaginatedResponse<T> {
    private List<T> items;
    private long total;
    private int page;
    private int pageSize;

    public PaginatedResponse(List<T> items, long total, int page, int pageSize) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }
}
