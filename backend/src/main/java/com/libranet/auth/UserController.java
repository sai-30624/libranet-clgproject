package com.libranet.auth;

import com.libranet.resource.Resource;
import com.libranet.resource.ResourceRepository;
import com.libranet.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.libranet.resource.ReviewRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final BookmarkRepository bookmarkRepository;
    private final ResourceRepository resourceRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository; // Added to save profile changes
    private final PasswordEncoder passwordEncoder;

    public UserController(BookmarkRepository bookmarkRepository, 
                          ResourceRepository resourceRepository,
                          ReviewRepository reviewRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.bookmarkRepository = bookmarkRepository;
        this.resourceRepository = resourceRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setCreatedAt(user.getCreatedAt());
        
        response.setBookmarkCount(bookmarkRepository.findByUserId(user.getId()).size());
        response.setReviewCount(reviewRepository.countByUserId(user.getId()));
        // Mock download count for now (or integrate with a download audit table if available)
        response.setDownloadCount(0); 

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Transactional
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal User user, @RequestBody java.util.Map<String, String> updates) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (updates.containsKey("name")) {
            existingUser.setName(updates.get("name"));
        }

        if (updates.containsKey("currentPassword") && updates.containsKey("newPassword")) {
            if (!passwordEncoder.matches(updates.get("currentPassword"), existingUser.getPassword())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }
            String newPassword = updates.get("newPassword");
            if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 72) {
                throw new IllegalArgumentException("New password must be 8-72 characters");
            }
            existingUser.setPassword(passwordEncoder.encode(updates.get("newPassword")));
        }

        userRepository.save(existingUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me/bookmarks")
    public ResponseEntity<List<Bookmark>> getMyBookmarks(@AuthenticationPrincipal User user) {
        List<Bookmark> bookmarks = bookmarkRepository.findByUserId(user.getId());
        bookmarks.forEach(b -> b.getResource().setBookmarked(true));
        return ResponseEntity.ok(bookmarks);
    }

    @PostMapping("/me/bookmarks/{resourceId}")
    public ResponseEntity<Void> addBookmark(@AuthenticationPrincipal User user, @PathVariable Long resourceId) {
        if (bookmarkRepository.existsByUserIdAndResourceId(user.getId(), resourceId)) {
            return ResponseEntity.ok().build();
        }
        
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        
        Bookmark bookmark = new Bookmark(user, resource);
        bookmarkRepository.save(bookmark);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me/bookmarks/{resourceId}")
    @Transactional
    public ResponseEntity<Void> removeBookmark(@AuthenticationPrincipal User user, @PathVariable Long resourceId) {
        bookmarkRepository.deleteByUserIdAndResourceId(user.getId(), resourceId);
        return ResponseEntity.ok().build();
    }
}
