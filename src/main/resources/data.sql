BEGIN;

TRUNCATE TABLE vouchers, users RESTART IDENTITY CASCADE;

INSERT INTO users (id, username, first_name, last_name, password, user_role, phone_number, email, balance, user_status)
VALUES
-- admin123
('11111111-1111-1111-1111-111111111111', 'admin', 'Admin', 'Admin', '$2a$12$3e/PrWuIRyLjnGFKRfxfYOo9Kqf8uSSmPAo3vfLdsO6gexNpKDU6.', 'ADMIN', '+1234567890', 'admin@travel.com', 0.00, TRUE),

-- manager123
('22222222-2222-2222-2222-222222222222', 'manager_anya', 'Anya', 'Manager', '$2a$12$I2B/SnRFYLi2d5ElTrIfdedCZ8ybSrouvAz/GjKFOmOmcfNTrZIKS', 'MANAGER', '+1987654321', 'lisa@travel.com', 0.00, TRUE),

-- andriy2026
('33333333-3333-3333-3333-333333333333', 'traveler_andriy', 'Andriy', 'Traveler', '$2a$12$7WnO8kO.YxPqG2YmK6A6/.1C0vWvR6T6Uj0v6K6x6H6G6F6E6D6C6', 'USER', '+1122334455', 'andriy@traveler.com', 5000.00, TRUE),
-- student123
('44444444-4444-4444-4444-444444444444', 'poor_student', 'Denis', 'Radchenko', '$2a$12$ZV6JcqThPUCXALlU.lNBe.lK/RTzjT5EaxrWrIRkSeFA5mMK0sDF.', 'USER', NULL, 'student@uni.edu', 10.00, FALSE),

-- alice_wonder (password: alice123)
('55555555-5555-5555-5555-555555555555', 'alice_wonder', 'Alice', 'Smith', '$2a$12$8K7/j8N6H8HkE4B1H2W3eO.fKx.zM7Y7zG7yF7xG7wH7vG7uF7yS6', 'USER', '+442071234567', 'alice@wonderland.com', 12000.50, TRUE),

-- rich_uncle (password: moneybags)
('66666666-6666-6666-6666-666666666666', 'rich_uncle', 'Scrooge', 'McDuck', '$2a$12$M9uG9f8k7j6h5g4f3d2s1e0r9t8y7u6i5o4p3a2s1d0f9g8h7j6k5', 'USER', '+15559990000', 'gold@vault.com', 999999.99, TRUE),

-- travel_agent_mike (password: mike789)
('77777777-7777-7777-7777-777777777777', 'agent_mike', 'Michael', 'Jordan', '$2a$12$Zp4n3m2l1k0j9i8h7g6f5e4d3c2b1a0z9y8x7w6v5u4t3s2r1q0p9', 'MANAGER', '+12125551234', 'mike@agencitravel.com', 0.00, TRUE);


INSERT INTO vouchers (title, description, price, voucher_tour_type, voucher_transfer_type, voucher_hotel_type, voucher_status_type, arrival_date, eviction_date, user_id, is_hot)
VALUES
-- GROUP 1: HOT AND AVAILABLE
('🔥 Tropical Rainforest Expedition', 'Explore the hidden gems of the Amazon jungle', 2600.00, 'ADVENTURE', 'JEEPS', 'FIVE_STARS', 'CREATED', '2026-06-10', '2026-06-25', null, true),
('🔥 Sunset Safari in Serengeti', 'Witness lions and elephants at golden hour', 2700.00, 'SAFARI', 'JEEPS', 'FIVE_STARS', 'CREATED', '2026-07-01', '2026-07-14', null, true),
('🔥 Tokyo Cyberpunk Night', 'Modern technology and traditional neon streets of Shinjuku', 3100.00, 'CULTURAL', 'PLANE', 'FIVE_STARS', 'CREATED', '2026-05-15', '2026-05-22', null, true),
('🔥 Dubai Luxury Escape', 'Helicopter transfers and underwater suites', 8500.00, 'LEISURE', 'PRIVATE_CAR', 'FIVE_STARS', 'CREATED', '2026-04-10', '2026-04-17', null, true),
('🔥 Swiss Alps Heli-Skiing', 'Extreme skiing with professional mountain guides', 4200.00, 'SPORTS', 'PLANE', 'FIVE_STARS', 'CREATED', '2026-03-01', '2026-03-08', null, true),

-- GROUP 2: REGULAR AVAILABLE
('Historic Rome & Vatican', 'Guided cultural excursions through Rome', 1100.00, 'CULTURAL', 'PLANE', 'FOUR_STARS', 'CREATED', '2026-08-05', '2026-08-12', null, false),
('Eco Retreat in Finland', 'Peaceful lakeside cabins and nature walks', 500.00, 'ECO', 'BUS', 'THREE_STARS', 'CREATED', '2026-09-01', '2026-09-10', null, false),
('Wellness Spa in Budapest', 'Thermal baths and relaxation', 1250.00, 'HEALTH', 'PRIVATE_CAR', 'FIVE_STARS', 'CREATED', '2026-10-10', '2026-10-20', null, false),
('Iceland Adventure', 'Volcanoes, glaciers, and geysers', 2800.00, 'ADVENTURE', 'JEEPS', 'FOUR_STARS', 'CREATED', '2026-11-01', '2026-11-10', null, false),
('Northern Lights in Norway', 'Aurora Borealis viewing tour', 3000.00, 'ADVENTURE', 'PLANE', 'FIVE_STARS', 'CREATED', '2026-12-01', '2026-12-08', null, false),
('Cultural Japan', 'Temples, gardens, and traditional tea ceremonies', 2500.00, 'CULTURAL', 'PLANE', 'FIVE_STARS', 'CREATED', '2026-08-15', '2026-08-25', null, false),
('Safari in South Africa', 'Big five wildlife viewing and luxury lodge', 2400.00, 'SAFARI', 'JEEPS', 'FIVE_STARS', 'CREATED', '2026-06-20', '2026-07-01', null, false),
('Wine & Dine in Tuscany', 'Vineyards, olive groves, and cooking classes', 1600.00, 'WINE', 'BUS', 'FOUR_STARS', 'CREATED', '2026-09-10', '2026-09-18', null, false),
('Amazon River Cruise', 'Luxury cruise exploring the Amazon river', 2700.00, 'LEISURE', 'SHIP', 'FIVE_STARS', 'CREATED', '2026-07-10', '2026-07-20', null, false),
('Patagonia Hiking Expedition', 'Glaciers, mountains, and trekking', 2900.00, 'ADVENTURE', 'JEEPS', 'FOUR_STARS', 'CREATED', '2026-11-05', '2026-11-15', null, false),
('Mystical Temples of Bagan', 'Explore ancient pagodas and sunset views', 2100.00, 'CULTURAL', 'PLANE', 'FOUR_STARS', 'CREATED', '2026-08-20', '2026-08-30', null, false),
('Alpine Lakes Retreat', 'Hiking and relaxation in the Swiss Alps', 1800.00, 'ECO', 'BUS', 'THREE_STARS', 'CREATED', '2026-09-05', '2026-09-15', null, false),
('Spa & Wellness in Bali', 'Luxury spa and yoga sessions', 2400.00, 'HEALTH', 'PLANE', 'FIVE_STARS', 'CREATED', '2026-10-01', '2026-10-12', null, false),
('Galapagos Island Cruise', 'Wildlife and marine exploration', 3200.00, 'ADVENTURE', 'SHIP', 'FIVE_STARS', 'CREATED', '2026-11-10', '2026-11-20', null, false),
('Cherry Blossom Tour in Japan', 'Seasonal sakura viewing and city tours', 2600.00, 'CULTURAL', 'PLANE', 'FIVE_STARS', 'CREATED', '2026-03-25', '2026-04-05', null, false),
('Amazon Jungle Lodge', 'Tropical wildlife and river tours', 2800.00, 'ADVENTURE', 'JEEPS', 'FOUR_STARS', 'CREATED', '2026-07-15', '2026-07-25', null, false),
('Mediterranean Culinary Tour', 'Cooking classes and local delicacies', 1700.00, 'WINE', 'BUS', 'FOUR_STARS', 'CREATED', '2026-09-15', '2026-09-25', null, false),
('Canadian Rockies Adventure', 'Hiking, canoeing, and mountain views', 2200.00, 'ADVENTURE', 'JEEPS', 'FOUR_STARS', 'CREATED', '2026-08-10', '2026-08-20', null, false),
('Greek Heritage & Islands', 'History tours and island hopping', 2000.00, 'CULTURAL', 'SHIP', 'FIVE_STARS', 'CREATED', '2026-09-05', '2026-09-15', null, false),
('Moroccan Desert Escape', 'Camel trekking and oasis stays', 1800.00, 'ADVENTURE', 'JEEPS', 'THREE_STARS', 'CREATED', '2026-10-05', '2026-10-15', null, false),
('New Zealand Nature Quest', 'Glaciers, fjords, and adventure sports', 3100.00, 'ADVENTURE', 'PLANE', 'FIVE_STARS', 'CREATED', '2026-11-15', '2026-11-25', null, false),
('Scottish Highlands Retreat', 'Castles, lochs, and whisky tasting', 1500.00, 'CULTURAL', 'BUS', 'FOUR_STARS', 'CREATED', '2026-09-10', '2026-09-20', null, false),
('Norwegian Fjords Cruise', 'Scenic fjord exploration with onboard luxury', 3300.00, 'LEISURE', 'SHIP', 'FIVE_STARS', 'CREATED', '2026-08-20', '2026-08-30', null, false),
('Bali Adventure & Surfing', 'Beach activities and tropical exploration', 2200.00, 'ADVENTURE', 'PLANE', 'FOUR_STARS', 'CREATED', '2026-09-15', '2026-09-25', null, false),
('Icelandic Hot Springs & Volcanoes', 'Relax in geothermal spas and explore volcanoes', 2800.00, 'HEALTH', 'JEEPS', 'FOUR_STARS', 'CREATED', '2026-11-05', '2026-11-15', null, false),
('French Riviera Leisure', 'Luxury beaches and sightseeing in Nice & Cannes', 2500.00, 'LEISURE', 'BUS', 'FIVE_STARS', 'CREATED', '2026-07-10', '2026-07-20', null, false),
('Costa Rica Eco Tour', 'Rainforest trekking and wildlife spotting', 2000.00, 'ECO', 'JEEPS', 'FOUR_STARS', 'CREATED', '2026-08-01', '2026-08-10', null, false),
('Andean Explorer', 'Peru’s mountains, Machu Picchu, and cultural immersion', 2700.00, 'ADVENTURE', 'PLANE', 'FIVE_STARS', 'CREATED', '2026-09-05', '2026-09-15', null, false),
('Venetian Canal Discovery', 'Gondola tours and city sightseeing', 1500.00, 'CULTURAL', 'PLANE', 'FOUR_STARS', 'CREATED', '2026-08-20', '2026-08-27', null, false),
('Maldives Island Resort', 'Luxury overwater villas and spa treatments', 3500.00, 'LEISURE', 'PLANE', 'FIVE_STARS', 'CREATED', '2026-12-01', '2026-12-10', null, false),

-- GROUP 3: PURCHASED (PAID)
('Mediterranean Sea Cruise', 'Luxury liner experience', 5000.00, 'LEISURE', 'SHIP', 'FIVE_STARS', 'PAID', '2025-05-01', '2025-05-14', '33333333-3333-3333-3333-333333333333', false),
('Grand Canyon Hike', 'Sold tour for rich traveler', 4000.00, 'ADVENTURE', 'JEEPS', 'FIVE_STARS', 'PAID', '2025-08-10', '2025-08-17', '66666666-6666-6666-6666-666666666666', false),
('🔥 Old Hot Tour', 'This was a hot deal for Alice', 900.00, 'ADVENTURE', 'PLANE', 'FOUR_STARS', 'PAID', '2025-10-10', '2025-10-17', '55555555-5555-5555-5555-555555555555', true),

-- GROUP 4: CANCELED
('Canceled Mountain Trek', 'Tour canceled due to weather conditions', 300.00, 'SPORTS', 'BUS', 'ONE_STARS', 'CANCELED', '2026-01-01', '2026-01-05', '33333333-3333-3333-3333-333333333333', false),
('Sahara Sandstorm Tour', 'Technical cancellation', 1500.00, 'ADVENTURE', 'JEEPS', 'THREE_STARS', 'CANCELED', '2026-02-10', '2026-02-20', '55555555-5555-5555-5555-555555555555', false),

-- GROUP 5: SYSTEM CHECKS (Alphabet & Logic)
('A-Title Check', 'Sort test first', 100.00, 'LEISURE', 'TRAIN', 'ONE_STARS', 'CREATED', '2026-12-01', '2026-12-05', null, false),
('Z-Title Check', 'Sort test last', 100.00, 'LEISURE', 'TRAIN', 'ONE_STARS', 'CREATED', '2026-12-01', '2026-12-05', null, false);

COMMIT;