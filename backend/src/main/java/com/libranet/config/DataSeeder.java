package com.libranet.config;

import com.libranet.auth.Role;
import com.libranet.auth.User;
import com.libranet.auth.UserRepository;
import com.libranet.auth.UserActivity;
import com.libranet.auth.UserActivityRepository;
import com.libranet.resource.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;

import com.libranet.auth.BookmarkRepository;
import com.libranet.resource.ReviewRepository;

/**
 * Wipes and reseeds demo data on startup. Runs ONLY with the "dev" profile:
 * without this guard every production restart deleted all users and resources.
 */
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    @Value("${app.seed.admin-password:}")
    private String adminPassword;

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ResourceRepository resourceRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserActivityRepository userActivityRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ReviewRepository reviewRepository;

    public DataSeeder(UserRepository userRepository, 
                      CategoryRepository categoryRepository, 
                      TagRepository tagRepository, 
                      ResourceRepository resourceRepository, 
                      PasswordEncoder passwordEncoder,
                      UserActivityRepository userActivityRepository,
                      BookmarkRepository bookmarkRepository,
                      ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.resourceRepository = resourceRepository;
        this.passwordEncoder = passwordEncoder;
        this.userActivityRepository = userActivityRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Clearing and Seeding Libranet Data...");
        
        userActivityRepository.deleteAll();
        bookmarkRepository.deleteAll();
        reviewRepository.deleteAll();
        resourceRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();
        tagRepository.deleteAll();

        System.out.println("Seeding Libranet Comprehensive Realistic Data...");

        Random rand = new Random();

        // 1. Users (20+ users for realistic stats)
        List<User> users = new ArrayList<>();
        String seededAdminPassword = adminPassword;
        if (seededAdminPassword == null || seededAdminPassword.isBlank()) {
            seededAdminPassword = UUID.randomUUID().toString();
            System.out.println("APP_SEED_ADMIN_PASSWORD not set: admin@libranet.com gets a random password (admin login disabled).");
        }
        users.add(new User("Admin", "admin@libranet.com", passwordEncoder.encode(seededAdminPassword), Role.ROLE_ADMIN));
        
        String[] names = {"Jane Doe", "John Smith", "Alice Johnson", "Bob Brown", "Charlie Davis", "Eva Wilson", "Frank Moore", "Grace Taylor", "Henry Anderson", "Ivy Thomas", "Jack Jackson", "Kelly White", "Liam Harris", "Mia Martin", "Noah Thompson", "Olivia Garcia", "Peter Martinez", "Quinn Robinson", "Rose Clark", "Sam Rodriguez"};
        for (String name : names) {
            User u = new User(name, name.toLowerCase().replace(" ", ".") + "@university.edu", passwordEncoder.encode(UUID.randomUUID().toString()), Role.ROLE_USER);
            users.add(u);
        }
        userRepository.saveAll(users);

        // 2. Categories
        Category cs = new Category("Computer Science", "Algorithms, Data Structures, and Software");
        Category ds = new Category("Data Science", "Machine Learning, Deep Learning, and AI");
        Category math = new Category("Mathematics", "Calculus, Linear Algebra, and Statistics");
        Category physics = new Category("Physics", "Mechanics, Quantum Physics, and Thermodynamics");
        Category business = new Category("Business", "Management, Finance, and Economics");
        Category lit = new Category("Literature", "Classic Novels and Poetry");
        Category art = new Category("Art & Design", "Visual Arts, UI/UX, and Architecture");
        Category history = new Category("History", "World History and Archaeology");
        categoryRepository.saveAll(List.of(cs, ds, math, physics, business, lit, art, history));

        // 3. Tags
        Tag programming = new Tag("Programming");
        Tag java = new Tag("Java");
        Tag python = new Tag("Python");
        Tag ai = new Tag("AI");
        Tag ml = new Tag("Machine Learning");
        Tag web = new Tag("Web Development");
        Tag cloud = new Tag("Cloud Computing");
        Tag algorithms = new Tag("Algorithms");
        Tag economics = new Tag("Economics");
        Tag management = new Tag("Management");
        Tag calculus = new Tag("Calculus");
        Tag fiction = new Tag("Fiction");
        Tag design = new Tag("Design");
        Tag react = new Tag("React");
        tagRepository.saveAll(List.of(programming, java, python, ai, ml, web, cloud, algorithms, economics, management, calculus, fiction, design, react));

        // 4. Resources (30+ resources)
        List<Resource> resources = new ArrayList<>();
        User adminUser = users.get(0);
        User studentUser = users.get(1);

        resources.add(createResource("Clean Code", "A Handbook of Agile Software Craftsmanship.", cs, adminUser, Set.of(programming, java), "Robert C. Martin", rand, 30));
        resources.add(createResource("Introduction to Algorithms", "A comprehensive update of the leading algorithms text.", cs, adminUser, Set.of(programming, algorithms), "Thomas H. Cormen", rand, 45));
        resources.add(createResource("Deep Learning", "An introduction to a broad range of topics in deep learning.", ds, adminUser, Set.of(ai, ml), "Ian Goodfellow", rand, 20));
        resources.add(createResource("Pattern Recognition and Machine Learning", "First textbook on pattern recognition to present the Bayesian viewpoint.", ds, adminUser, Set.of(ml, algorithms), "Christopher M. Bishop", rand, 60));
        resources.add(createResource("Calculus: Early Transcendentals", "Renowned for their mathematical precision and accuracy.", math, adminUser, Set.of(calculus), "James Stewart", rand, 90));
        resources.add(createResource("The Lean Startup", "How Today's Entrepreneurs Use Continuous Innovation to Create Radically Successful Businesses.", business, adminUser, Set.of(management, economics), "Eric Ries", rand, 15));
        resources.add(createResource("Thinking, Fast and Slow", "The phenomenal international bestseller that will change the way you make decisions.", business, adminUser, Set.of(management), "Daniel Kahneman", rand, 25));
        resources.add(createResource("Python Crash Course", "A Hands-On, Project-Based Introduction to Programming.", cs, studentUser, Set.of(programming, python), "Eric Matthes", rand, 10));
        resources.add(createResource("Designing Data-Intensive Applications", "The Big Ideas Behind Reliable, Scalable, and Maintainable Systems.", cs, adminUser, Set.of(cloud, web), "Martin Kleppmann", rand, 40));
        resources.add(createResource("The Great Gatsby", "The authentic edition from Fitzgerald's original publisher.", lit, studentUser, Set.of(fiction), "F. Scott Fitzgerald", rand, 100));
        resources.add(createResource("To Kill a Mockingbird", "Harper Lee's Pulitzer Prize-winning masterwork.", lit, users.get(2), Set.of(fiction), "Harper Lee", rand, 80));
        resources.add(createResource("Artificial Intelligence: A Modern Approach", "The most comprehensive introduction to the theory and practice of AI.", ds, adminUser, Set.of(ai), "Stuart Russell", rand, 35));
        resources.add(createResource("Spring in Action", "The bestselling guide to Spring.", cs, studentUser, Set.of(java, web), "Craig Walls", rand, 12));
        resources.add(createResource("Cloud Native Patterns", "Designing change-tolerant software.", cs, adminUser, Set.of(cloud), "Cornelia Davis", rand, 18));
        resources.add(createResource("Principles of Corporate Finance", "The gold standard in corporate finance.", business, users.get(15), Set.of(economics), "Richard A. Brealey", rand, 50));
        
        // Add more varied resources
        resources.add(createResource("React Up & Running", "Building Web Applications.", cs, adminUser, Set.of(web, react), "Stoyan Stefanov", rand, 5));
        resources.add(createResource("Eloquent JavaScript", "A Modern Introduction to Programming.", cs, studentUser, Set.of(programming, web), "Marijn Haverbeke", rand, 8));
        resources.add(createResource("The Design of Everyday Things", "Cognitive psychology applied to design.", art, adminUser, Set.of(design), "Don Norman", rand, 70));
        resources.add(createResource("Sapiens", "A Brief History of Humankind.", history, adminUser, Set.of(fiction), "Yuval Noah Harari", rand, 30));
        resources.add(createResource("Linear Algebra and Its Applications", "Fundamentals of linear algebra.", math, users.get(5), Set.of(calculus), "Gilbert Strang", rand, 55));
        resources.add(createResource("The Pragmatic Programmer", "Your journey to mastery.", cs, adminUser, Set.of(programming), "Andrew Hunt", rand, 42));
        resources.add(createResource("Cracking the Coding Interview", "189 Programming Questions and Solutions.", cs, studentUser, Set.of(algorithms, programming), "Gayle Laakmann McDowell", rand, 22));
        resources.add(createResource("Refactoring", "Improving the Design of Existing Code.", cs, adminUser, Set.of(programming, java), "Martin Fowler", rand, 38));
        resources.add(createResource("Code Complete", "A Practical Handbook of Software Construction.", cs, users.get(10), Set.of(programming), "Steve McConnell", rand, 50));
        resources.add(createResource("Zero to One", "Notes on Startups, or How to Build the Future.", business, adminUser, Set.of(management), "Peter Thiel", rand, 14));

        resourceRepository.saveAll(resources);

        // 5. Seed Mock User Activity (Views & Downloads)
        List<UserActivity> activities = new ArrayList<>();
        List<User> studentUsers = new ArrayList<>(users);
        studentUsers.remove(0); // Remove admin
        
        for (int i = 0; i < 100; i++) {
            User user = studentUsers.get(rand.nextInt(studentUsers.size()));
            Resource resource = resources.get(rand.nextInt(resources.size()));
            UserActivity.ActivityType type = rand.nextBoolean() ? UserActivity.ActivityType.VIEW : UserActivity.ActivityType.DOWNLOAD;
            activities.add(new UserActivity(user, resource, type));
        }
        userActivityRepository.saveAll(activities);

        System.out.println("Libranet Comprehensive Realistic Data Seeded!");
    }

    private Resource createResource(String title, String desc, Category category, User user, Set<Tag> tags, String author, Random rand, int daysOld) {
        Resource r = new Resource();
        r.setTitle(title);
        r.setDescription(desc);
        r.setCategory(category);
        r.setUploadedBy(user);
        r.setTags(tags);
        r.setAuthor(author);
        r.setViews((long) (rand.nextInt(5000) + 100));
        r.setDownloads((long) (rand.nextInt(1000) + 50));
        r.setAvgRating(Math.round((3.0 + rand.nextDouble() * 2.0) * 10.0) / 10.0);
        r.setReviewCount(rand.nextInt(200) + 10);
        r.setCreatedAt(LocalDateTime.now().minusDays(daysOld));
        
        // File details for preview and download
        r.setFileName("sample.pdf");
        r.setFileType("pdf");
        r.setFileSize((long) (rand.nextInt(5000000) + 500000)); // 0.5MB to 5MB
        // Frontend expects fileUrl to be the API endpoint for the iframe preview
        r.setFileUrl("/api/files/download/" + (System.currentTimeMillis() % 1000)); // Temporary, ID will be set by JPA
        
        return r;
    }
}
