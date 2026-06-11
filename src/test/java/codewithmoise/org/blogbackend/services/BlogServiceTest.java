package codewithmoise.org.blogbackend.services;

import codewithmoise.org.blogbackend.DTO.requests.BlogPostRequest;
import codewithmoise.org.blogbackend.DTO.requests.BlogUpdateRequest;
import codewithmoise.org.blogbackend.DTO.responses.BlogResponse;
import codewithmoise.org.blogbackend.enums.BlogCategory;
import codewithmoise.org.blogbackend.enums.UserRoles;
import codewithmoise.org.blogbackend.models.Blog;
import codewithmoise.org.blogbackend.models.Tag;
import codewithmoise.org.blogbackend.models.User;
import codewithmoise.org.blogbackend.repository.BlogRepository;
import codewithmoise.org.blogbackend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagService tagService;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private BlogService blogService;

    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthentication(Long userId) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userId);
    }

    @Test
    void getBlogById_WhenBlogExists_ReturnsBlogResponse() {
        Long blogId = 1L;
        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setTitle("Secure Coding Practices");
        blog.setSlug("secure-coding-practices");
        blog.setContent("Ensure input validation.");
        blog.setPublished(true);

        User author = new User();
        author.setId(10L);
        author.setUsername("secops");
        author.setEmail("secops@blogbackend.org");
        blog.setUser(author);

        when(blogRepository.findById(blogId)).thenReturn(Optional.of(blog));

        Optional<BlogResponse> responseOpt = blogService.getBlogById(blogId);

        assertTrue(responseOpt.isPresent());
        BlogResponse response = responseOpt.get();
        assertEquals(blogId, response.getId());
        assertEquals("Secure Coding Practices", response.getTitle());
        assertEquals("secops", response.getAuthor().getName());
    }

    @Test
    void getBlogById_WhenBlogDoesNotExist_ReturnsEmpty() {
        Long blogId = 999L;
        when(blogRepository.findById(blogId)).thenReturn(Optional.empty());

        Optional<BlogResponse> responseOpt = blogService.getBlogById(blogId);

        assertFalse(responseOpt.isPresent());
    }

    @Test
    void getBlogBySlug_WhenBlogExists_IncrementsViewsAndSaves() {
        String slug = "test-slug";
        Blog blog = new Blog();
        blog.setSlug(slug);
        blog.setViews(10);

        when(blogRepository.findBySlug(slug)).thenReturn(Optional.of(blog));
        when(blogRepository.save(any(Blog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<BlogResponse> responseOpt = blogService.getBlogBySlug(slug);

        assertTrue(responseOpt.isPresent());
        assertEquals(11, blog.getViews());
        verify(blogRepository).save(blog);
    }

    @Test
    void createBlog_WhenAuthenticated_Success() throws IOException {
        Long userId = 2L;
        mockAuthentication(userId);

        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        BlogPostRequest request = new BlogPostRequest();
        request.setTitle("Threat Modeling 101");
        request.setContent("Identify resources and threats.");
        request.setCategory(BlogCategory.TECHNOLOGY);
        request.setTags(List.of("security", "threat-modeling"));

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        request.setImage(mockFile);
        when(cloudinaryService.uploadImage(mockFile)).thenReturn("https://cloudinary/image.png");

        Tag tagSec = new Tag();
        tagSec.setName("security");
        Tag tagTm = new Tag();
        tagTm.setName("threat-modeling");
        when(tagService.findOrCreateTags(anyList())).thenReturn(List.of(tagSec, tagTm));

        when(blogRepository.save(any(Blog.class))).thenAnswer(invocation -> {
            Blog b = invocation.getArgument(0);
            b.setId(200L);
            return b;
        });

        BlogResponse response = blogService.createBlog(request);

        assertNotNull(response);
        assertEquals(200L, response.getId());
        assertEquals("Threat Modeling 101", response.getTitle());
        assertEquals("https://cloudinary/image.png", response.getCoverImage());
        assertTrue(response.getTags().contains("security"));
        verify(cloudinaryService).uploadImage(mockFile);
        verify(tagService).findOrCreateTags(List.of("security", "threat-modeling"));
    }

    @Test
    void createBlog_WhenNotAuthenticated_ThrowsAccessDeniedException() {
        when(securityContext.getAuthentication()).thenReturn(null);
        BlogPostRequest request = new BlogPostRequest();

        assertThrows(AccessDeniedException.class, () -> blogService.createBlog(request));
    }

    @Test
    void updateBlog_WhenCurrentUserIsOwner_Success() {
        Long userId = 2L;
        Long blogId = 5L;
        mockAuthentication(userId);

        User user = new User();
        user.setId(userId);
        user.setRole(UserRoles.AUTHOR);

        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setUser(user);
        blog.setTitle("Old Title");

        when(blogRepository.findById(blogId)).thenReturn(Optional.of(blog));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(blogRepository.save(any(Blog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlogUpdateRequest updateRequest = new BlogUpdateRequest();
        updateRequest.setTitle("New Title");

        BlogResponse response = blogService.updateBlog(updateRequest, blogId);

        assertEquals("New Title", response.getTitle());
        verify(blogRepository).save(blog);
    }

    @Test
    void updateBlog_WhenCurrentUserIsAdmin_Success() {
        Long adminId = 1L;
        Long authorId = 2L;
        Long blogId = 5L;
        mockAuthentication(adminId);

        User author = new User();
        author.setId(authorId);

        User admin = new User();
        admin.setId(adminId);
        admin.setRole(UserRoles.ADMIN);

        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setUser(author);
        blog.setTitle("Old Title");

        when(blogRepository.findById(blogId)).thenReturn(Optional.of(blog));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(blogRepository.save(any(Blog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlogUpdateRequest updateRequest = new BlogUpdateRequest();
        updateRequest.setTitle("Admin Overwritten Title");

        BlogResponse response = blogService.updateBlog(updateRequest, blogId);

        assertEquals("Admin Overwritten Title", response.getTitle());
        verify(blogRepository).save(blog);
    }

    @Test
    void updateBlog_WhenCurrentUserIsNotOwnerOrAdmin_ThrowsAccessDeniedException() {
        Long strangerId = 3L;
        Long authorId = 2L;
        Long blogId = 5L;
        mockAuthentication(strangerId);

        User author = new User();
        author.setId(authorId);

        User stranger = new User();
        stranger.setId(strangerId);
        stranger.setRole(UserRoles.AUTHOR);

        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setUser(author);

        when(blogRepository.findById(blogId)).thenReturn(Optional.of(blog));
        when(userRepository.findById(strangerId)).thenReturn(Optional.of(stranger));

        BlogUpdateRequest updateRequest = new BlogUpdateRequest();

        assertThrows(AccessDeniedException.class, () -> blogService.updateBlog(updateRequest, blogId));
        verify(blogRepository, never()).save(any(Blog.class));
    }

    @Test
    void deleteBlog_WhenCurrentUserIsOwner_DeletesSuccessfully() throws IOException {
        Long userId = 2L;
        Long blogId = 5L;
        mockAuthentication(userId);

        User user = new User();
        user.setId(userId);
        user.setRole(UserRoles.AUTHOR);

        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setUser(user);
        blog.setImageUrl("https://res.cloudinary.com/demo/image/upload/sample.jpg");

        when(blogRepository.findById(blogId)).thenReturn(Optional.of(blog));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        blogService.deleteBlog(blogId);

        verify(cloudinaryService).deleteImage(blog.getImageUrl());
        verify(blogRepository).delete(blog);
    }

    @Test
    void deleteBlog_WhenCurrentUserIsNotOwnerOrAdmin_ThrowsAccessDeniedException() {
        Long strangerId = 3L;
        Long authorId = 2L;
        Long blogId = 5L;
        mockAuthentication(strangerId);

        User author = new User();
        author.setId(authorId);

        User stranger = new User();
        stranger.setId(strangerId);
        stranger.setRole(UserRoles.AUTHOR);

        Blog blog = new Blog();
        blog.setId(blogId);
        blog.setUser(author);

        when(blogRepository.findById(blogId)).thenReturn(Optional.of(blog));
        when(userRepository.findById(strangerId)).thenReturn(Optional.of(stranger));

        assertThrows(AccessDeniedException.class, () -> blogService.deleteBlog(blogId));
        verify(blogRepository, never()).delete(any(Blog.class));
    }
}
