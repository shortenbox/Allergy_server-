USE infant_meal_db;
SHOW TABLES;

CREATE TABLE IF NOT EXISTS food_dictionary (
    id INT AUTO_INCREMENT PRIMARY KEY,
    food_name VARCHAR(100) NOT NULL,
    search_name VARCHAR(100),
    UNIQUE KEY uk_food_dictionary_food_name (food_name)
);


INSERT IGNORE INTO food_dictionary (food_name, search_name) VALUES
('김치찌개', '김치찌개'),
('묵은지김치찌개', '김치찌개'),
('된장찌개', '된장찌개'),
('계란말이', '계란말이'),
('두부부침', '두부부침'),
('소고기덮밥', '불고기덮밥'),
('불고기덮밥', '불고기덮밥'),
('김밥', '김밥'),
('볶음밥', '볶음밥'),
('야채볶음밥', '볶음밥'),
('햄버거', '햄버거'),
('핫도그', '핫도그');

SELECT * FROM food_dictionary;
