BEGIN;
-- item no.1
INSERT INTO coupon_info (name, discount_type, discount_value, start_at, end_at,
                         issued_quantity, remaining_quantity, requires_concurrency_control,
                         target_img_url, min_purchase_amount, max_discount_amount)
VALUES ('나이키 에어포스 1 특가 할인', 'FIXED_AMOUNT', 15000, '2023-04-01T00:00:00', '2023-12-31T23:59:59',
        100, 100, 0,
        'https://contents.lotteon.com/itemimage/20231031160737/LE/12/09/70/22/15/_1/25/24/37/81/3/LE1209702215_1252437813_5.jpg/dims/resizef/554X554', 0, null);
INSERT INTO coupon_applies_to (coupon_info_id, applies_to_id, applies_to_type) VALUES (LAST_INSERT_ID(), 1, 'PRODUCT');

-- item no.2
INSERT INTO coupon_info (name, discount_type, discount_value, start_at, end_at,
                         issued_quantity, remaining_quantity, requires_concurrency_control,
                         target_img_url, min_purchase_amount, max_discount_amount)
VALUES ('[겨울특가] 나이키 에어포스 1 옐로우 2만원 할인', 'FIXED_AMOUNT', 20000, '2023-04-01T00:00:00', '2023-12-31T23:59:59',
        100, 100, 0,
        'https://contents.lotteon.com/itemimage/20231031160737/LE/12/09/70/22/15/_1/25/24/37/81/3/LE1209702215_1252437813_5.jpg/dims/resizef/554X554', 0, null);
INSERT INTO coupon_applies_to (coupon_info_id, applies_to_id, applies_to_type) VALUES (LAST_INSERT_ID(), 2, 'PRODUCT');

-- item no.3
INSERT INTO coupon_info (name, discount_type, discount_value, start_at, end_at,
                         issued_quantity, remaining_quantity, requires_concurrency_control,
                         target_img_url, min_purchase_amount, max_discount_amount)
VALUES ('남성 상의 전 품목 7% 특가할인', 'PERCENTAGE', 7, '2023-04-01T00:00:00', '2023-12-31T23:59:59',
        100, 100, 0,
        'http://example.com/img3.jpg', 0, NULL);
INSERT INTO coupon_applies_to (coupon_info_id, applies_to_id, applies_to_type) VALUES (LAST_INSERT_ID(), 3, 'CATEGORY');

-- item no.4
INSERT INTO coupon_info (name, discount_type, discount_value, start_at, end_at,
                         issued_quantity, remaining_quantity, requires_concurrency_control,
                         target_img_url, min_purchase_amount, max_discount_amount)
VALUES ('10_PERCENT', 'PERCENTAGE', 10, '2023-04-01T00:00:00', '2023-12-31T23:59:59',
        100, 100, 0,
        'http://example.com/img4.jpg', 4999, NULL);
INSERT INTO coupon_applies_to (coupon_info_id, applies_to_id, applies_to_type) VALUES (LAST_INSERT_ID(), 4, 'CATEGORY');

-- item no.5
INSERT INTO coupon_info (name, discount_type, discount_value, start_at, end_at,
                         issued_quantity, remaining_quantity, requires_concurrency_control,
                         target_img_url, min_purchase_amount, max_discount_amount)
VALUES ('15_PERCENT', 'PERCENTAGE', 15, '2023-04-01T00:00:00', '2023-12-31T23:59:59',
        100, 100, 0,
        'http://example.com/img5.jpg', 0, 5000);
INSERT INTO coupon_applies_to (coupon_info_id, applies_to_id, applies_to_type) VALUES (LAST_INSERT_ID(), 5, 'CATEGORY');

-- item no.6
INSERT INTO coupon_info (name, discount_type, discount_value, start_at, end_at,
                         issued_quantity, remaining_quantity, requires_concurrency_control,
                         target_img_url, min_purchase_amount, max_discount_amount)
VALUES ('25_PERCENT_OFF_FOR_CAT_5', 'PERCENTAGE', 25, '2023-12-25T00:00:00', '2023-12-31T23:59:59',
        100, 100, 0,
        'http://example.com/img5.jpg', 50000, 50000);
INSERT INTO coupon_applies_to (coupon_info_id, applies_to_id, applies_to_type) VALUES (LAST_INSERT_ID(), 5, 'CATEGORY');


COMMIT;

INSERT INTO member_coupon (member_id, coupon_info_id, created_at, is_used)
VALUES
    (1, 1, '2023-04-10T12:00:00', 0),
    (1, 2, '2023-04-10T12:00:00', 0),
    (2, 1, '2023-04-10T12:00:00', 0),
    (2, 3, '2023-04-10T12:00:00', 0),
    (2, 5, '2023-04-10T12:00:00', 0);
