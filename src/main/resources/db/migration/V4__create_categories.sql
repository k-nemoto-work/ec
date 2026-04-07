CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- 初期カテゴリデータ
INSERT INTO categories (id, name) VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', '古着'),
    ('b2c3d4e5-f6a7-8901-bcde-f12345678901', '家電'),
    ('c3d4e5f6-a7b8-9012-cdef-123456789012', '雑貨'),
    ('d4e5f6a7-b8c9-0123-def0-234567890123', '本・メディア'),
    ('e5f6a7b8-c9d0-1234-ef01-345678901234', 'スポーツ・アウトドア');
