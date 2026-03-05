package com.hottakeranker.config;

import com.hottakeranker.entity.Topic;
import com.hottakeranker.enums.TopicStatus;
import com.hottakeranker.repository.TopicRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!prod")
public class DataSeeder implements CommandLineRunner {

    private final TopicRepository topicRepository;

    public DataSeeder(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    @Override
    public void run(String... args) {
        seedTopics();
    }

    private void seedTopics() {
        if (topicRepository.count() > 0) {
            return;
        }

        List<Topic> topics = List.of(
            // Food
            new Topic("Best pizza topping", "Food",
                List.of("Pepperoni", "Pineapple", "Mushrooms", "Jalapeños", "Sausage", "Bacon", "Onions", "Green Peppers"),
                TopicStatus.ACTIVE),
            new Topic("Rank these fast food chains", "Food",
                List.of("McDonald's", "Chick-fil-A", "Wendy's", "In-N-Out", "Popeyes", "Taco Bell", "Five Guys", "Chipotle"),
                TopicStatus.ACTIVE),
            new Topic("Rank these pizza styles", "Food",
                List.of("New York", "Chicago Deep Dish", "Detroit", "Neapolitan", "Sicilian", "New Haven", "St. Louis", "Frozen"),
                TopicStatus.ACTIVE),
            new Topic("Rank these breakfast items", "Food",
                List.of("Bacon", "Eggs", "Pancakes", "Waffles", "Cereal", "Bagel", "Avocado Toast", "Breakfast Burrito"),
                TopicStatus.ACTIVE),
            new Topic("Best condiment", "Food",
                List.of("Ketchup", "Ranch", "Hot Sauce", "Mayo", "Mustard", "BBQ Sauce", "Sriracha", "Honey Mustard"),
                TopicStatus.ACTIVE),
            new Topic("Best cooking method for steak", "Food",
                List.of("Grilled", "Pan-Seared", "Sous Vide", "Smoked", "Broiled", "Reverse Sear", "Cast Iron", "Air Fryer"),
                TopicStatus.ACTIVE),

            // Games
            new Topic("Best pokemon starter", "Games",
                List.of("Squirtle", "Charmander", "Bulbasaur", "Pikachu", "Cyndaquil", "Totodile", "Chikorita", "Mudkip"),
                TopicStatus.ACTIVE),
            new Topic("Best FPS game series", "Games",
                List.of("Counter-Strike", "Valorant", "Call of Duty", "Rainbow Six Siege", "Half Life", "Apex Legends", "Fortnite", "Halo"),
                TopicStatus.ACTIVE),

            // Sports
            new Topic("Rank these GOATs", "Sports",
                List.of("Jordan", "LeBron", "Brady", "Messi", "Serena", "Gretzky", "Tiger", "Bolt"),
                TopicStatus.ACTIVE),
            new Topic("Rank these sports to watch", "Sports",
                List.of("Football", "Basketball", "Soccer", "Baseball", "MMA", "F1", "Tennis", "Golf"),
                TopicStatus.ACTIVE),
            new Topic("Best NBA player of the 2010s", "Sports",
                List.of("LeBron", "Curry", "KD", "Kawhi", "Harden", "Westbrook", "Giannis", "AD"),
                TopicStatus.ACTIVE),
            new Topic("Best sports city", "Sports",
                List.of("Boston", "LA", "New York", "Chicago", "Miami", "Dallas", "San Francisco", "Philadelphia"),
                TopicStatus.ACTIVE),
            new Topic("Best NBA duo ever", "Sports",
                List.of("MJ & Pippen", "Kobe & Shaq", "LeBron & Wade", "Curry & KD", "Magic & Kareem", "Stockton & Malone", "Duncan & Robinson", "Bird & McHale"),
                TopicStatus.ACTIVE),
            new Topic("Best sports movie", "Sports",
                List.of("Space Jam", "Remember the Titans", "Rocky", "Moneyball", "The Blind Side", "Happy Gilmore", "Hoosiers", "Rudy"),
                TopicStatus.ACTIVE),

            // Entertainment
            new Topic("Rank these movie franchises", "Entertainment",
                List.of("Marvel", "Star Wars", "Harry Potter", "Lord of the Rings", "Fast & Furious", "Batman", "James Bond", "Jurassic Park"),
                TopicStatus.ACTIVE),
            new Topic("Rank these streaming services", "Entertainment",
                List.of("Netflix", "Hulu", "Disney+", "HBO Max", "Prime Video", "Apple TV+", "Peacock", "YouTube Premium"),
                TopicStatus.ACTIVE),
            new Topic("Rank these decades for music", "Entertainment",
                List.of("60s", "70s", "80s", "90s", "2000s", "2010s", "2020s", "Music peaked before I was born"),
                TopicStatus.ACTIVE),
            new Topic("Best social media", "Entertainment",
                List.of("Instagram", "TikTok", "Twitter/X", "YouTube", "Reddit", "Snapchat", "Threads", "LinkedIn"),
                TopicStatus.ACTIVE),

            // Culture / Lifestyle
            new Topic("Rank what matters most in a partner", "Lifestyle",
                List.of("Humor", "Looks", "Intelligence", "Ambition", "Kindness", "Money", "Shared Interests", "Chemistry"),
                TopicStatus.ACTIVE),
            new Topic("Rank these cities to live in", "Culture",
                List.of("NYC", "LA", "Miami", "Austin", "Chicago", "Denver", "Seattle", "Nashville"),
                TopicStatus.ACTIVE),
            new Topic("Rank these ways to spend a Saturday", "Lifestyle",
                List.of("Sleep In", "Gym", "Brunch", "Gaming", "Hiking", "Bar Hopping", "Netflix Binge", "Shopping"),
                TopicStatus.ACTIVE),
            new Topic("Best morning routine activity", "Lifestyle",
                List.of("Coffee", "Exercise", "Meditation", "Journaling", "Cold Shower", "Reading", "Stretching", "Big Breakfast"),
                TopicStatus.ACTIVE),
            new Topic("Best pet", "Lifestyle",
                List.of("Dog", "Cat", "Fish", "Bird", "Hamster", "Rabbit", "Reptile", "Guinea Pig"),
                TopicStatus.ACTIVE),
            new Topic("Best season", "Culture",
                List.of("Spring", "Summer", "Fall", "Winter", "Early Fall", "Late Spring", "Indian Summer", "Holiday Season"),
                TopicStatus.ACTIVE),
            new Topic("Best vacation type", "Culture",
                List.of("Beach", "Mountains", "City", "Road Trip", "Cruise", "Camping", "International", "Staycation"),
                TopicStatus.ACTIVE),

            // Spicy / Troll Tier
            new Topic("Rank these red flags in a person", "Spicy",
                List.of("Bad Tipper", "Rude to Waiters", "No Hobbies", "Posts Everything on Social Media", "Never Reads", "Talks About Ex Constantly", "Doesn't Like Dogs", "Chews With Mouth Open"),
                TopicStatus.ACTIVE),
            new Topic("Rank these overrated things", "Spicy",
                List.of("College", "Marriage", "Owning a Home", "Having Kids", "Traveling", "Crypto", "The Gym", "Brunch"),
                TopicStatus.ACTIVE),
            new Topic("Rank these hill-you'd-die-on opinions", "Spicy",
                List.of("Cereal Is Soup", "Hot Dogs Are Sandwiches", "Water Is Wet", "GIF Is Pronounced Jif", "Pineapple on Pizza Is Good", "Tipping Culture Should End", "Astrology Is Fake", "Die Hard Is a Christmas Movie"),
                TopicStatus.ACTIVE),
            new Topic("Best superpower", "Spicy",
                List.of("Flight", "Invisibility", "Teleportation", "Time Travel", "Mind Reading", "Super Speed", "Shape Shifting", "Healing"),
                TopicStatus.ACTIVE),
            new Topic("Most overrated food", "Spicy",
                List.of("Brunch", "Sushi", "Tacos", "Avocado Toast", "Charcuterie Boards", "Acai Bowls", "Kale", "Truffle Fries"),
                TopicStatus.ACTIVE),

            // Politics
            new Topic("Most important political issue", "Politics",
                List.of("Economy", "Healthcare", "Climate Change", "Immigration", "Education", "National Security", "Gun Control", "Housing"),
                TopicStatus.ACTIVE),
            new Topic("Best form of government", "Politics",
                List.of("Democracy", "Constitutional Republic", "Parliamentary", "Direct Democracy", "Federalism", "Socialism", "Libertarianism", "Technocracy"),
                TopicStatus.ACTIVE),
            new Topic("Most influential US president", "Politics",
                List.of("Lincoln", "FDR", "Washington", "Jefferson", "Teddy Roosevelt", "Reagan", "Obama", "JFK"),
                TopicStatus.ACTIVE),

            // Films
            new Topic("Best movie genre", "Films",
                List.of("Action", "Comedy", "Drama", "Sci-Fi", "Horror", "Thriller", "Romance", "Documentary"),
                TopicStatus.ACTIVE),
            new Topic("Best movie of the 2000s", "Films",
                List.of("The Dark Knight", "Lord of the Rings", "Inception", "No Country for Old Men", "There Will Be Blood", "Gladiator", "The Departed", "Superbad"),
                TopicStatus.ACTIVE),
            new Topic("Best animated studio", "Films",
                List.of("Pixar", "Studio Ghibli", "Disney", "DreamWorks", "Laika", "Illumination", "Blue Sky", "Aardman"),
                TopicStatus.ACTIVE),

            // Music
            new Topic("Best music genre", "Music",
                List.of("Hip-Hop", "Rock", "Pop", "R&B", "Country", "Electronic", "Jazz", "Classical"),
                TopicStatus.ACTIVE),
            new Topic("Best rapper of all time", "Music",
                List.of("Kendrick Lamar", "Jay-Z", "Nas", "Tupac", "Biggie", "Eminem", "Lil Wayne", "Andre 3000"),
                TopicStatus.ACTIVE),
            new Topic("Best music festival", "Music",
                List.of("Coachella", "Lollapalooza", "Bonnaroo", "Glastonbury", "Rolling Loud", "Tomorrowland", "Austin City Limits", "Burning Man"),
                TopicStatus.ACTIVE),

            // Tech
            new Topic("Best programming language", "Tech",
                List.of("Python", "JavaScript", "Java", "Go", "Rust", "TypeScript", "C#", "Kotlin"),
                TopicStatus.ACTIVE),
            new Topic("Best laptop", "Tech",
                List.of("MacBook Pro", "ThinkPad", "Dell XPS", "Framework", "Surface Laptop", "Razer Blade", "ASUS ROG", "HP Spectre"),
                TopicStatus.ACTIVE),

            // Science
            new Topic("Most important scientific discovery", "Science",
                List.of("Penicillin", "Electricity", "DNA Structure", "Theory of Relativity", "Evolution", "Germ Theory", "Vaccination", "Internet"),
                TopicStatus.ACTIVE),
            new Topic("Best planet to colonize", "Science",
                List.of("Mars", "Moon", "Venus", "Titan", "Europa", "Enceladus", "Proxima b", "Kepler-442b"),
                TopicStatus.ACTIVE)
        );

        topicRepository.saveAll(topics);
        System.out.println("Seeded " + topics.size() + " topics");
    }
}
