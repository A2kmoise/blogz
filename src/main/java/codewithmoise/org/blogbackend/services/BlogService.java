package codewithmoise.org.blogbackend.services;

import codewithmoise.org.blogbackend.DTO.requests.BlogPostRequest;
import codewithmoise.org.blogbackend.DTO.requests.BlogUpdateRequest;
import codewithmoise.org.blogbackend.models.Blog;
import codewithmoise.org.blogbackend.repository.BlogRepository;

import java.util.Optional;

public class BlogService {

    private final BlogRepository blogRepository;

    public BlogService(BlogRepository blogRepository){
        this.blogRepository = blogRepository;
    }

    public Blog getBlogs(){
       return (Blog) blogRepository.findAll();
    }
    public Optional<Blog> getBlogById(Long id){
        return blogRepository.findById(id);
    }
    public void createBlog(BlogPostRequest blogPostRequest){
        Blog blog = new Blog();

        blog.setTitle(blogPostRequest.getTitle());
        blog.setContent(blogPostRequest.getContent());
        blog.setCategory(blogPostRequest.getCategory());
        blog.setTags(blogPostRequest.getTags());
    }
    public void updateBlog(BlogUpdateRequest blogUpdateRequest, Long blogId){
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(()->  new RuntimeException("Blog not found"));

            blog.setContent(blogUpdateRequest.getContent());
            blog.setTags(blogUpdateRequest.getTags());

    }
    public void deleteBlog(Long blogId){
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(()-> new RuntimeException("Blog not found"));

         blogRepository.delete(blog);
    }
}
