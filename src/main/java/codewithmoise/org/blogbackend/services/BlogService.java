package codewithmoise.org.blogbackend.services;

import codewithmoise.org.blogbackend.DTO.requests.BlogPostRequest;
import codewithmoise.org.blogbackend.DTO.requests.BlogUpdateRequest;
import codewithmoise.org.blogbackend.DTO.responses.BlogResponse;
import codewithmoise.org.blogbackend.models.Blog;
import codewithmoise.org.blogbackend.models.User;
import codewithmoise.org.blogbackend.repository.BlogRepository;
import codewithmoise.org.blogbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;

    public BlogService(BlogRepository blogRepository, UserRepository userRepository){
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
    }

    public List<BlogResponse> getBlogs() {

        List<Blog> blogs = blogRepository.findAll();

        return blogs.stream().map(blog -> {

            BlogResponse response = new BlogResponse();

            response.setId(blog.getId());
            response.setTitle(blog.getTitle());
            response.setContent(blog.getContent());
            response.setAuthorId(blog.getUser().getId());

            return response;

        }).toList();
    }
    public Optional<Blog> getBlogById(Long id){
        return blogRepository.findById(id);
    }
    public BlogResponse createBlog(BlogPostRequest blogPostRequest){

        User user = userRepository.findById(blogPostRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("user not found"));

        Blog blog = new Blog();

        blog.setTitle(blogPostRequest.getTitle());
        blog.setContent(blogPostRequest.getContent());
        blog.setCategory(blogPostRequest.getCategory());
        blog.setTags(blogPostRequest.getTags());
        blog.setUser(user);

        Blog savedBlog = blogRepository.save(blog);

        BlogResponse response = new BlogResponse();

        response.setId(savedBlog.getId());
        response.setTitle(savedBlog.getTitle());
        response.setAuthorId(savedBlog.getUser().getId());
        return response;
    }
    public BlogResponse updateBlog(BlogUpdateRequest blogUpdateRequest, Long blogId){
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(()->  new RuntimeException("Blog not found"));

            blog.setContent(blogUpdateRequest.getContent());
            blog.setTags(blogUpdateRequest.getTags());

            Blog updatedBlog = blogRepository.save(blog);

                BlogResponse response = new BlogResponse();
                response.setContent(updatedBlog.getContent());
                response.setTags(updatedBlog.getTags());

                return response;

    }

    public void deleteBlog(Long blogId){
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(()-> new RuntimeException("Blog not found"));

         blogRepository.delete(blog);
    }
}
