# 🔥 Hot Take Ranker — Build Guide (v2)

*A step-by-step plan for building this yourself. Each step tells you what to build, why it matters, and gives you enough direction to figure out the implementation.*

**Key design decisions baked in:**
- 8 options per topic (not 4)
- Borda Count scoring: 1st = 8 points, 2nd = 7, ..., 8th = 1 point
- Elo rating system that ranks users by how well they predict crowd consensus
- Demographic breakdowns by gender, age group, and region

---

## Step 0: Project Setup

**Goal:** Get a running Spring Boot app with PostgreSQL connected.

1. Go to [start.spring.io](https://start.spring.io) and generate a project with:
   - Java 21, Gradle (Kotlin DSL), Spring Boot 3.x
   - Dependencies: **Spring Web**, **Spring Data JPA**, **PostgreSQL Driver**, **Spring Validation**, **Lombok** (optional but saves boilerplate), **Spring Boot DevTools**
2. Set up a local PostgreSQL database called `hottakeranker`
3. Configure `application.yml` with your DB connection, and set `spring.jpa.hibernate.ddl-auto` to `create-drop` for now (you'll switch to Flyway migrations later)
4. Create a simple `GET /api/health` endpoint that returns `"OK"` — hit it with curl or Postman to confirm everything's wired up

**Why this matters:** Interviewers love asking about project setup decisions. Know why you picked Gradle vs Maven, why `create-drop` is fine for dev but not prod, etc.

---

## Step 1: Topic Entity + Seeding

**Goal:** Model a topic and load starter data.

**What to build:**
- A `Topic` JPA entity with fields: `id`, `question`, `category`, `options` (the 8 rankable items), `activeDate`, `status` (PENDING, ACTIVE, ARCHIVED), `createdAt`
- A `TopicRepository` extending `JpaRepository`
- A `data.sql` or `CommandLineRunner` that seeds 10-15 topics on startup

**Design decisions you'll need to make:**
- How do you store `options`? It's a list of 8 strings. You have a few choices: a JSONB column (PostgreSQL-native, flexible), a separate `topic_options` join table (normalized), or a comma-delimited string (don't do this). Research the `@Type` annotation from Hibernate or the `@JdbcTypeCode` approach for JSONB. This is a great interview talking point.
- How should `category` be stored? Enum in Java? String in DB? Think about what happens when you want to add new categories later.

**Checkpoint:** You should be able to start the app and query `SELECT * FROM topics` in psql and see your seeded data.

---

## Step 2: Vote Submission

**Goal:** Let users submit a ranking for a topic.

**What to build:**
- A `Vote` entity with: `id`, `topicId`, `ranking` (the user's ordered indices), `createdAt`. Skip `userId` for now — we'll add auth later.
- A `VoteRepository`
- A `VoteController` with `POST /api/votes` that accepts a request body like:
  ```json
  {
    "topicId": 1,
    "ranking": [4, 0, 7, 2, 5, 1, 6, 3]
  }
  ```
  Where the array represents "I ranked option index 4 first, option 0 second, etc."
- A `VoteService` that validates the input and saves it
- A `VoteRequest` DTO — don't pass your entity directly to the controller

**Validation to implement:**
- `topicId` must reference an existing, ACTIVE topic
- `ranking` must be exactly 8 elements, containing indices 0-7 with no duplicates
- Use `@Valid` and custom validation or do it in the service layer — your call, but have a reason

**Checkpoint:** `POST /api/votes` with a valid body returns 201. Invalid bodies return 400 with a meaningful error message.

---

## Step 3: Results Aggregation (The Interesting Part)

**Goal:** Calculate the crowd's consensus ranking using Borda Count.

**What to build:**
- A `GET /api/topics/{id}/results` endpoint that returns the crowd ranking
- The aggregation logic in a `RankingAggregationService`

**How Borda Count works with 8 options:**
- For each vote, the item ranked 1st gets 8 points, 2nd gets 7, 3rd gets 6, ..., 8th gets 1 point
- Sum up points across all votes
- Sort by total points descending → that's the crowd ranking

**Example:** If 3 people vote on fast food chains (8 options):
- Vote 1: [0,1,2,3,4,5,6,7] → option 0 gets 8pts, option 1 gets 7pts, etc.
- Vote 2: [7,6,5,4,3,2,1,0] → option 7 gets 8pts, option 6 gets 7pts, etc.
- Vote 3: [0,3,1,7,2,6,4,5] → option 0 gets 8pts, option 3 gets 7pts, etc.
- Sum it up per option, sort, return the result

**Decision: Where does aggregation happen?**
- Option A: Query all votes, aggregate in Java. Simple, fine for MVP.
- Option B: Write a native SQL query that does the aggregation in PostgreSQL. More performant, shows off SQL skills. Try this if you're comfortable with `GROUP BY` and aggregate functions.
- Option C: Pre-compute and cache. Best for production but overkill right now.

Start with Option A, then refactor to Option B once it works. That refactoring story is great for interviews ("I started with in-memory aggregation, then moved it to the DB layer when I realized...").

**Response DTO should look something like:**
```json
{
  "topicId": 1,
  "question": "Best fast food chain?",
  "crowdRanking": ["Chick-fil-A", "In-N-Out", "Chipotle", "Raising Cane's", "Five Guys", "Wendy's", "Popeyes", "Taco Bell"],
  "totalVotes": 142,
  "scores": {
    "Chick-fil-A": 987,
    "In-N-Out": 901,
    "Chipotle": 878,
    "Raising Cane's": 812,
    "Five Guys": 756,
    "Wendy's": 698,
    "Popeyes": 645,
    "Taco Bell": 523
  }
}
```

**Checkpoint:** Submit 5-10 votes via Postman, then hit the results endpoint and verify the math is correct by hand.

---

## Step 4: User Model + Demographics

**Goal:** Add users with demographic info so we can slice results.

**What to build:**
- A `User` entity with: `id`, `displayName`, `email`, `passwordHash`, `gender`, `ageGroup`, `region`, `eloRating` (default 1000), `streak`, `createdAt`
- Update `Vote` to include a `userId` foreign key with a unique constraint on `(userId, topicId)` — one vote per user per topic
- `POST /api/auth/register` — for now just save the user with a hashed password (use BCrypt via Spring Security's `PasswordEncoder`). Don't worry about login/JWT yet.
- Update `POST /api/votes` to require a `userId`

**Demographic field choices:**
- `gender`: Keep it simple — `MALE`, `FEMALE`, `NON_BINARY`, `PREFER_NOT_TO_SAY`
- `ageGroup`: `18-24`, `25-34`, `35-44`, `45+` — store as a string/enum, not a computed field from birthdate (less PII to manage)
- `region`: For MVP, let users self-select from `NORTHEAST`, `SOUTH`, `MIDWEST`, `WEST`. Later you can auto-detect from IP using MaxMind GeoIP.

**Note on Elo:** Just add the `eloRating` field now with a default of 1000. We'll build the calculation logic in Step 6.

**Checkpoint:** Register a few users with different demographics, submit votes as each user.

---

## Step 5: Demographic Breakdowns (The Viral Feature)

**Goal:** "Men 25-34 in the South say X, but women on the West Coast say Y."

**What to build:**
- `GET /api/topics/{id}/results/demographics?groupBy=gender` → returns Borda count broken out by each gender
- Support `groupBy` values: `gender`, `ageGroup`, `region`
- A `DemographicService` that joins `votes` with `users` and runs the same Borda aggregation per group

**Response structure:**
```json
{
  "topicId": 1,
  "groupBy": "region",
  "breakdowns": {
    "SOUTH": {
      "crowdRanking": ["Chick-fil-A", "Raising Cane's", "Popeyes", "In-N-Out", "Chipotle", "Five Guys", "Wendy's", "Taco Bell"],
      "totalVotes": 48
    },
    "WEST": {
      "crowdRanking": ["In-N-Out", "Chipotle", "Chick-fil-A", "Five Guys", "Taco Bell", "Raising Cane's", "Popeyes", "Wendy's"],
      "totalVotes": 37
    }
  }
}
```

**SQL challenge:** Write a query that does the Borda aggregation grouped by a demographic field in one shot. This will involve joining `votes` and `users`, using JSONB functions to unpack the ranking array, and grouping by the demographic column. This is genuinely hard and will teach you a lot about PostgreSQL. If you get stuck, start with the Java in-memory approach and come back to the SQL version.

**Checkpoint:** Hit the demographics endpoint and verify the breakdowns make sense given the votes you submitted.

---

## Step 6: Elo Rating System

**Goal:** Rank users by how well their takes match the crowd consensus.

**Concept:** Every user starts at 1000 Elo. After a topic closes (enough votes or the day ends), you compare each user's ranking to the crowd consensus and adjust their Elo up or down based on how close they were.

**What to build:**
- An `EloService` with the core rating logic
- A method that triggers after a topic is archived (called from the scheduler in Step 9, but build the logic now)

**Similarity scoring — how "correct" is a user's ranking?**

You need a way to measure how close a user's ranking is to the crowd ranking. Research these options and pick one:

- **Spearman Rank Correlation:** Produces a score from -1 (perfectly opposite) to +1 (perfect match). Formula: `1 - (6 * Σd²) / (n * (n² - 1))` where `d` is the difference in positions for each item and `n` is the number of items (8). This is probably the best fit — it's well-understood and produces a clean score.
- **Kendall Tau Distance:** Counts the number of pairwise disagreements between two rankings. More granular but harder to normalize into an Elo adjustment.
- **Exact match counting:** Simple — count how many items are in the same position. Less nuanced (doesn't reward being "close").

**Elo adjustment formula:**

Once you have a similarity score (say, 0 to 1 using Spearman), you need to convert it to an Elo change. Here's a starting framework to think through:

1. Calculate the user's similarity score against the crowd ranking (0 to 1)
2. Determine an "expected score" based on their current Elo (higher-rated users are expected to do better)
3. Elo change = K * (actual_score - expected_score)
   - `K` is the K-factor (how volatile ratings are). Start with K=32 (same as chess for new players).
   - `actual_score` is their similarity to the crowd (0 to 1)
   - `expected_score` is derived from their Elo: `1 / (1 + 10^((1500 - elo) / 400))` — this means a 1500-rated user is expected to score 0.5

**Design decisions:**
- Should Elo be global or per-category? Per-category is more interesting ("I'm a Food expert but terrible at Sports takes") but adds complexity. Start global, add per-category later.
- What's the minimum number of votes on a topic before you calculate Elo? You don't want to adjust ratings based on a crowd of 3 people. Pick a threshold (e.g., 50 votes minimum).
- Should there be Elo decay? If someone doesn't vote for a week, does their rating slowly regress toward 1000? This encourages daily engagement.

**What to store:**
- `eloRating` on the User entity (already added in Step 4)
- Consider an `EloHistory` entity that logs each adjustment: `userId`, `topicId`, `previousElo`, `newElo`, `similarityScore`, `createdAt`. This lets you show users their Elo over time and debug the system.

**Checkpoint:** Manually trigger Elo calculation for a closed topic. Verify that a user who ranked close to the crowd gained Elo and a user who ranked far off lost Elo. Check that the math makes sense — a 1000-rated user who scores 0.8 similarity should gain more than a 1500-rated user who scores 0.8.

---

## Step 7: Elo Leaderboard

**Goal:** Give users a reason to come back — competitive rankings.

**What to build:**
- `GET /api/leaderboard` → top 50 users by Elo, with rank, displayName, eloRating
- `GET /api/leaderboard?region=SOUTH` → filtered leaderboard by demographic
- `GET /api/users/me/elo-history` → authenticated user's Elo changes over time

**Response structure:**
```json
{
  "leaderboard": [
    { "rank": 1, "displayName": "TasteGOAT", "eloRating": 1847, "totalVotes": 89 },
    { "rank": 2, "displayName": "CrowdWhisperer", "eloRating": 1792, "totalVotes": 112 },
    { "rank": 3, "displayName": "BasicTakes", "eloRating": 1756, "totalVotes": 67 }
  ],
  "totalUsers": 4821
}
```

**Elo history response:**
```json
{
  "currentElo": 1342,
  "history": [
    { "topicId": 12, "question": "Best fast food chain?", "eloChange": +18, "similarityScore": 0.82, "date": "2026-02-20" },
    { "topicId": 11, "question": "Best NBA duo ever?", "eloChange": -12, "similarityScore": 0.35, "date": "2026-02-19" }
  ]
}
```

**Bonus ideas to consider:**
- Elo tiers with names: Bronze (< 1200), Silver (1200-1400), Gold (1400-1600), Platinum (1600-1800), Diamond (1800+). Display the tier badge on the leaderboard.
- "Hot streak" indicator for users who gained Elo 5+ days in a row.

**Checkpoint:** Submit votes as multiple users, trigger Elo calculation, hit the leaderboard endpoint and verify the ordering is correct.

---

## Step 8: JWT Authentication

**Goal:** Proper auth so users can only vote once and we know who they are.

**What to build:**
- A `JwtTokenProvider` class that generates and validates JWT tokens
- A `JwtAuthFilter` that extends `OncePerRequestFilter` — intercepts requests, extracts the token from the `Authorization` header, and sets the security context
- A `SecurityConfig` class that:
  - Permits `/api/auth/**` and `GET /api/topics/**` and `GET /api/leaderboard` without auth
  - Requires auth for `POST /api/votes`, user profile endpoints, and Elo history
- `POST /api/auth/login` that returns a JWT
- Update vote submission to pull `userId` from the JWT instead of the request body

**Things to research:**
- How `SecurityFilterChain` works in Spring Boot 3 (it changed from the older `WebSecurityConfigurerAdapter` style)
- How to use `@AuthenticationPrincipal` to get the current user in your controllers
- Stateless session management (`SessionCreationPolicy.STATELESS`)

**Checkpoint:** Login, get a token, use it to submit a vote. Try submitting without a token and verify you get a 401. Try voting twice on the same topic and verify you get a 409.

---

## Step 9: Topic Suggestions

**Goal:** Let users submit their own debate topics.

**What to build:**
- A `TopicSuggestion` entity with: `id`, `userId`, `question`, `category`, `options` (8 items), `status` (PENDING, APPROVED, REJECTED), `upvotes`, `createdAt`
- `POST /api/topics/suggest` — authenticated users can submit suggestions
- `GET /api/topics/suggestions` — see pending suggestions (paginated)
- Admin approval is out of scope for now — you can approve directly in the DB or build a simple admin flag on the user model

**Validation:** Options must be exactly 8 items, no duplicates, reasonable length limits on strings.

**Checkpoint:** Submit a suggestion, see it in the DB, manually approve it, verify it shows up as an active topic.

---

## Step 10: Caching with Redis

**Goal:** The results, demographics, and leaderboard endpoints will get hammered. Cache them.

**What to build:**
- Add the `spring-boot-starter-data-redis` dependency
- Use `@Cacheable` on your results, demographics, and leaderboard service methods
- Use `@CacheEvict` when a new vote is submitted for that topic
- Configure TTL so cached results expire after ~5 minutes even without eviction

**Things to think about:**
- What's your cache key structure? `results:{topicId}` and `demographics:{topicId}:{groupBy}` and `leaderboard:{filter}` are reasonable.
- What happens if Redis is down? Your app should still work, just slower. Look into cache fallback strategies.
- When should you evict? Every single vote, or on a timer? Think about the tradeoff.
- Leaderboard cache should evict when Elo recalculations happen, not on every vote.

**Checkpoint:** Submit a vote, verify the cache is evicted (check Redis with `redis-cli`), hit the endpoint again and verify it's recalculated.

---

## Step 11: Daily Rotation Scheduler

**Goal:** Automatically cycle through topics on a daily basis.

**What to build:**
- A `@Scheduled` method in a `DailyTopicScheduler` that runs at midnight
- It archives today's active topics (set status to ARCHIVED), activates the next batch, and **triggers Elo recalculation** for the archived topics
- Decide how many topics are active per day (3-5 is a good number)

**Scheduler flow:**
1. Find all ACTIVE topics
2. Set their status to ARCHIVED
3. For each archived topic, call `eloService.calculateEloAdjustments(topicId)` to update all users who voted on it
4. Find the next batch of PENDING topics, set them to ACTIVE
5. Evict relevant caches (leaderboard, results)

**Things to consider:**
- What timezone? Store dates in UTC, display in the user's timezone on the frontend.
- What happens if you run out of pending topics? Log a warning? Pull from approved suggestions?
- Use `@Scheduled(cron = "...")` — know the cron expression format for interviews.
- Elo recalculation could be slow if a topic has thousands of votes. Consider doing it asynchronously with `@Async`.

**Checkpoint:** Temporarily set the cron to run every minute, watch topics cycle and Elo update, then set it back to midnight.

---

## Step 12: Share Card Generation

**Goal:** Wordle-style shareable results.

**What to build:**
- A service that generates the share text comparing the user's ranking to the crowd:
  - 🟩 = exact match (same position as crowd)
  - 🟨 = off by one position
  - 🟥 = off by 2+ positions
- Calculate a "match score" percentage
- Include the user's Elo tier in the share card
- `GET /api/share/{topicId}` (authenticated) → returns the formatted share text
- Later: generate an Open Graph image for link previews (this is a stretch goal)

**Share text format:**
```
🔥 Hot Take Ranker
Best fast food chain?

🟩🟨🟩🟥🟩🟨🟥🟩 68%
📊 Gold (1487 Elo)

hottakeranker.com
```

**Checkpoint:** Submit a ranking, hit the share endpoint, verify the emoji blocks match your ranking vs the crowd.

---

## Step 13: Testing

**Goal:** Solid test coverage that demonstrates professional practices.

**What to write:**
- **Unit tests** for `RankingAggregationService` — test Borda count math with known inputs/outputs for 8 options
- **Unit tests** for `EloService` — test that similarity scoring and Elo adjustments behave correctly across edge cases (perfect match, perfectly opposite, identical Elo users, high vs low Elo users)
- **Unit tests** for validation logic — bad rankings (wrong length, duplicates, out of range indices), duplicate votes, etc.
- **Integration tests** with `@SpringBootTest` for the full vote submission → results → Elo flow
- **Controller tests** with `MockMvc` for request validation and HTTP status codes
- **Repository tests** with `@DataJpaTest` for your custom queries

**Bonus:** Use Testcontainers to spin up a real PostgreSQL instance for integration tests instead of H2. This is a strong interview signal.

**Checkpoint:** `./gradlew test` passes with 80%+ coverage on service and controller layers.

---

## Step 14: React Frontend

**Goal:** Build the UI. Reference the prototype I made earlier for the design direction.

**Key screens:**
1. **Home** — Today's topics as cards, each showing category, question, vote count
2. **Rank** — Drag-to-reorder the 8 options, "Lock It In" button. Consider a "tap to rank" alternative for mobile since dragging 8 items can be clunky.
3. **Results** — Your ranking vs crowd, match score, demographic filter tabs (gender, age, region)
4. **Leaderboard** — Global and filtered rankings with Elo tiers, Elo history chart for the logged-in user
5. **Suggest** — Form to submit a new topic (8 options)
6. **Profile** — User's Elo, tier badge, voting history, Elo trend chart
7. **Share** — Copy-to-clipboard share text, shareable link

**Frontend architecture suggestions:**
- Use `axios` or `fetch` for API calls
- Store JWT in memory (not localStorage for security) or use httpOnly cookies
- Use React Router for navigation
- State management: `useState`/`useContext` is fine for this scale — no need for Redux

**Ranking UX for 8 items:**
Dragging 8 items is doable on desktop but rough on mobile. Consider a hybrid approach: show all 8 options as a grid, user taps them in order (#1 first, #2 second, etc.), each tap moves the item to the ranked list. This is how a lot of tournament bracket apps handle it. Research what feels best.

---

## Starter Topic Bank (8 Options Each)

Seed these into your database:

| # | Category | Question | Options |
|---|----------|----------|---------|
| 1 | Food | Best pizza topping | Pepperoni, Pineapple, Mushrooms, Jalapeños, Sausage, Bacon, Onions, Green Peppers |
| 2 | Food | Best fast food chain | Chick-fil-A, In-N-Out, Chipotle, Raising Cane's, Five Guys, Wendy's, Popeyes, Taco Bell |
| 3 | Food | Best breakfast food | Pancakes, Waffles, French Toast, Eggs Benedict, Omelette, Bagel & Lox, Breakfast Burrito, Cereal |
| 4 | Food | Best condiment | Ketchup, Ranch, Hot Sauce, Mayo, Mustard, BBQ Sauce, Sriracha, Honey Mustard |
| 5 | Food | Best cooking method for steak | Grilled, Pan-Seared, Sous Vide, Smoked, Broiled, Reverse Sear, Cast Iron, Air Fryer |
| 6 | Sports | Best NBA player of the 2010s | LeBron, Curry, KD, Kawhi, Harden, Westbrook, Giannis, AD |
| 7 | Sports | Best sport to watch | Football, Basketball, Soccer, Baseball, Hockey, Tennis, MMA, Golf |
| 8 | Sports | Best sports city | Boston, LA, New York, Chicago, Miami, Dallas, San Francisco, Philadelphia |
| 9 | Sports | Best NBA duo ever | MJ & Pippen, Kobe & Shaq, LeBron & Wade, Curry & KD, Magic & Kareem, Stockton & Malone, Duncan & Robinson, Bird & McHale |
| 10 | Sports | Best sports movie | Space Jam, Remember the Titans, Rocky, Moneyball, The Blind Side, Happy Gilmore, Hoosiers, Rudy |
| 11 | Entertainment | Best streaming platform | Netflix, HBO Max, Disney+, Hulu, Amazon Prime, Apple TV+, Peacock, Paramount+ |
| 12 | Entertainment | Best social media | Instagram, TikTok, Twitter/X, YouTube, Reddit, Snapchat, Threads, LinkedIn |
| 13 | Entertainment | Best decade for music | 60s, 70s, 80s, 90s, 2000s, 2010s, 2020s, 50s |
| 14 | Culture | Best US city to live in | NYC, Austin, Miami, Denver, Seattle, Nashville, San Diego, Chicago |
| 15 | Culture | Best season | Spring, Summer, Fall, Winter, Early Fall, Late Spring, Indian Summer, Holiday Season |
| 16 | Culture | Best vacation type | Beach, Mountains, City, Road Trip, Cruise, Camping, International, Staycation |
| 17 | Tech | Best programming language | Python, JavaScript, Java, Go, Rust, TypeScript, C#, Kotlin |
| 18 | Tech | Best laptop | MacBook Pro, ThinkPad, Dell XPS, Framework, Surface Laptop, Razer Blade, ASUS ROG, HP Spectre |
| 19 | Spicy | Best superpower | Flight, Invisibility, Teleportation, Time Travel, Mind Reading, Super Speed, Shape Shifting, Healing |
| 20 | Spicy | Most overrated food | Brunch, Sushi, Tacos, Avocado Toast, Charcuterie Boards, Acai Bowls, Kale, Truffle Fries |

---

## Interview Talking Points

This project demonstrates:
- **REST API design** with proper DTOs, validation, error handling
- **Database modeling** with JSONB, aggregation queries, indexing
- **Algorithm design** with Borda Count voting and Elo rating system (Spearman correlation, expected vs actual scoring)
- **Caching strategy** with Redis for high-read endpoints
- **Authentication** with JWT + Spring Security
- **Scheduled tasks** for daily content rotation and async Elo recalculation
- **Testing** with JUnit 5, MockMvc, Testcontainers
- **Full-stack delivery** from schema design to deployed product
- **Product thinking** — demographic breakdowns as the viral hook, Elo as the retention mechanic
