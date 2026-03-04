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
            new Topic("Best pizza topping", "Food",
                List.of("Pepperoni", "Pineapple", "Mushrooms", "Jalapeños", "Sausage", "Bacon", "Onions", "Green Peppers"),
                TopicStatus.ACTIVE),
            new Topic("Best fast food chain", "Food",
                List.of("Chick-fil-A", "In-N-Out", "Chipotle", "Raising Cane's", "Five Guys", "McDonalds", "Popeyes", "Taco Bell"),
                TopicStatus.ACTIVE),
            new Topic("Best breakfast food", "Food",
                List.of("Pancakes", "Waffles", "French Toast", "Eggs Benedict", "Omelette", "Bagel & Lox", "Breakfast Burrito", "Cereal"),
                TopicStatus.ACTIVE),
						new Topic("Best pokemon starter", "Games",
								List.of("Squirtle", "Charmander", "Bulbasaur", "Pikachu", "Cyndaquil", "Totodile", "Chikorita", "Mudkip"),
								TopicStatus.ACTIVE),
            new Topic("Best condiment", "Food",
                List.of("Ketchup", "Ranch", "Hot Sauce", "Mayo", "Mustard", "BBQ Sauce", "Sriracha", "Honey Mustard"),
                TopicStatus.ACTIVE),
						new Topic("Best FPS game series", "Games",
								List.of("Counter-Strike", "Valorant", "Call of Duty", "Rainbow Six Siege", "Half Life", "Apex Legends", "Fortnite", "Halo"),
								TopicStatus.ACTIVE),
            new Topic("Best cooking method for steak", "Food",
                List.of("Grilled", "Pan-Seared", "Sous Vide", "Smoked", "Broiled", "Reverse Sear", "Cast Iron", "Air Fryer"),
                TopicStatus.PENDING),
            new Topic("Best NBA player of the 2010s", "Sports",
                List.of("LeBron", "Curry", "KD", "Kawhi", "Harden", "Westbrook", "Giannis", "AD"),
                TopicStatus.PENDING),
            new Topic("Best sport to watch", "Sports",
                List.of("Football", "Basketball", "Soccer", "Baseball", "Hockey", "Tennis", "MMA", "Golf"),
                TopicStatus.PENDING),
            new Topic("Best sports city", "Sports",
                List.of("Boston", "LA", "New York", "Chicago", "Miami", "Dallas", "San Francisco", "Philadelphia"),
                TopicStatus.PENDING),
            new Topic("Best NBA duo ever", "Sports",
                List.of("MJ & Pippen", "Kobe & Shaq", "LeBron & Wade", "Curry & KD", "Magic & Kareem", "Stockton & Malone", "Duncan & Robinson", "Bird & McHale"),
                TopicStatus.PENDING),
            new Topic("Best sports movie", "Sports",
                List.of("Space Jam", "Remember the Titans", "Rocky", "Moneyball", "The Blind Side", "Happy Gilmore", "Hoosiers", "Rudy"),
                TopicStatus.PENDING),
            new Topic("Best streaming platform", "Entertainment",
                List.of("Netflix", "HBO Max", "Disney+", "Hulu", "Amazon Prime", "Apple TV+", "Peacock", "Paramount+"),
                TopicStatus.PENDING),
            new Topic("Best social media", "Entertainment",
                List.of("Instagram", "TikTok", "Twitter/X", "YouTube", "Reddit", "Snapchat", "Threads", "LinkedIn"),
                TopicStatus.PENDING),
            new Topic("Best decade for music", "Entertainment",
                List.of("60s", "70s", "80s", "90s", "2000s", "2010s", "2020s", "50s"),
                TopicStatus.PENDING),
            new Topic("Best US city to live in", "Culture",
                List.of("NYC", "Austin", "Miami", "Denver", "Seattle", "Nashville", "San Diego", "Chicago"),
                TopicStatus.PENDING),
            new Topic("Best season", "Culture",
                List.of("Spring", "Summer", "Fall", "Winter", "Early Fall", "Late Spring", "Indian Summer", "Holiday Season"),
                TopicStatus.PENDING),
            new Topic("Best vacation type", "Culture",
                List.of("Beach", "Mountains", "City", "Road Trip", "Cruise", "Camping", "International", "Staycation"),
                TopicStatus.PENDING),
            new Topic("Best programming language", "Tech",
                List.of("Python", "JavaScript", "Java", "Go", "Rust", "TypeScript", "C#", "Kotlin"),
                TopicStatus.PENDING),
            new Topic("Best laptop", "Tech",
                List.of("MacBook Pro", "ThinkPad", "Dell XPS", "Framework", "Surface Laptop", "Razer Blade", "ASUS ROG", "HP Spectre"),
                TopicStatus.PENDING),
            new Topic("Best superpower", "Spicy",
                List.of("Flight", "Invisibility", "Teleportation", "Time Travel", "Mind Reading", "Super Speed", "Shape Shifting", "Healing"),
                TopicStatus.PENDING),
            new Topic("Most overrated food", "Spicy",
                List.of("Brunch", "Sushi", "Tacos", "Avocado Toast", "Charcuterie Boards", "Acai Bowls", "Kale", "Truffle Fries"),
                TopicStatus.PENDING)
        );

        topicRepository.saveAll(topics);
        System.out.println("Seeded " + topics.size() + " topics");
    }
}
